/*
 * Copyright 2009 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.cifex.upload.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.cifex.upload.IUploadService;
import ch.systemsx.cisd.cifex.upload.UploadState;
import ch.systemsx.cisd.cifex.upload.UploadStatus;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.WrappedIOException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class Uploader
{
    private static final EnumSet<UploadState> FINAL_STATES =
            EnumSet.of(UploadState.FINISHED, UploadState.ABORTED);

    private static final EnumSet<UploadState> RUNNING_STATES =
            EnumSet.of(UploadState.READY_FOR_NEXT_FILE, UploadState.UPLOADING);

    private static final class RandomAccessFileProvider
    {
        private final File file;
        private RandomAccessFile randomAccessFile;

        RandomAccessFileProvider(File file)
        {
            this.file = file;
        }
        
        RandomAccessFile getRandomAccessFile()
        {
            if (randomAccessFile == null)
            {
                try
                {
                    randomAccessFile = new RandomAccessFile(file, "r");
                } catch (FileNotFoundException ex)
                {
                    throw new WrappedIOException(ex);
                }
            }
            return randomAccessFile;
        }
        
        void closeFile()
        {
            if (randomAccessFile != null)
            {
                try
                {
                    randomAccessFile.close();
                } catch (IOException ex)
                {
                    throw new WrappedIOException(ex);
                }
            }
        }
    }
    
    private static final int BLOCK_SIZE = 64 * 1024;
    
    private final Set<IUploadListener> listeners = new LinkedHashSet<IUploadListener>();
    private final IUploadService uploadService;
    private final String uploadSessionID;
    
    public Uploader(IUploadService uploadService, String uploadSessionID)
    {
        this.uploadService = uploadService;
        this.uploadSessionID = uploadSessionID;
    }
    
    public void addUploadListener(IUploadListener uploadListener)
    {
        listeners.add(uploadListener);
    }
    
    public boolean isUploading()
    {
        try
        {
            UploadStatus status = uploadService.getUploadStatus(uploadSessionID);
            return RUNNING_STATES.contains(status.getUploadState());
        } catch (RuntimeException ex)
        {
            ex.printStackTrace();
            return false;
        }
    }
    
    public void cancel()
    {
        try
        {
            uploadService.cancel(uploadSessionID);
        } catch (RuntimeException ex)
        {
            ex.printStackTrace();
        }
    }
    
    public void upload(List<File> files, String recipients, String comment)
    {
        String[] paths = new String[files.size()];
        try
        {
            for (int i = 0; i < files.size(); i++)
            {
                paths[i] = files.get(i).getCanonicalPath();
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        
        UploadStatus status = uploadService.getUploadStatus(uploadSessionID);
        byte[] bytes = new byte[BLOCK_SIZE];
        RandomAccessFileProvider fileProvider = null;
        long fileSize = 0;
        while (FINAL_STATES.contains(status.getUploadState()) == false)
        {
            switch (status.getUploadState())
            {
                case INITIALIZED:
                    status = uploadService.defineUploadParameters(uploadSessionID, paths, recipients, comment);
                    break;
                case READY_FOR_NEXT_FILE:
                    if (fileProvider != null)
                    {
                        fileProvider.closeFile();
                        fireUploadedEvent();
                    }
                    File file = new File(status.getCurrentFile());
                    fileSize = file.length();
                    fileProvider = new RandomAccessFileProvider(file);
                    fireStartedEvent(file, fileSize);
                    status = uploadService.startUploading(uploadSessionID);
                    break;
                case UPLOADING:
                    status = uploadNextBlock(fileProvider, status.getFilePointer(), bytes);
                    fireProgressEvent(status.getFilePointer(), fileSize);
                    break;
                case FINISHED:
                case ABORTED:
                    break;
            }
        }
        boolean successful = status.getUploadState() == UploadState.FINISHED;
        uploadService.finish(uploadSessionID, successful);
        fireFinishedEvent(successful);
    }

    private UploadStatus uploadNextBlock(RandomAccessFileProvider fileProvider, long filePointer, byte[] bytes)
    {
        try
        {
            RandomAccessFile randomAccessFile = fileProvider.getRandomAccessFile();
            int blockSize = bytes.length;
            long fileSize = randomAccessFile.length();
            boolean lastBlock = filePointer + blockSize >= fileSize;
            if (lastBlock)
            {
                blockSize = (int) (fileSize - filePointer);
            }
            randomAccessFile.seek(filePointer);
            randomAccessFile.readFully(bytes, 0, blockSize);
            return uploadService.uploadBlock(uploadSessionID, bytes, blockSize, lastBlock);
        } catch (IOException ex)
        {
            throw new WrappedIOException(ex);
        }
    }

    private void fireStartedEvent(File file, long fileSize)
    {
        for (IUploadListener listener : listeners)
        {
            listener.uploadingStarted(file, fileSize);
        }
    }
    
    private void fireProgressEvent(long numberOfBytes, long fileSize)
    {
        int percentage = (int) ((numberOfBytes * 100) / Math.max(1, fileSize));
        for (IUploadListener listener : listeners)
        {
            listener.uploadingProgress(percentage, numberOfBytes);
        }
    }

    private void fireFinishedEvent(boolean successful)
    {
        for (IUploadListener listener : listeners)
        {
            listener.uploadingFinished(successful);
        }
    }
    
    private void fireUploadedEvent()
    {
        for (IUploadListener listener : listeners)
        {
            listener.fileUploaded();
        }
    }

}
