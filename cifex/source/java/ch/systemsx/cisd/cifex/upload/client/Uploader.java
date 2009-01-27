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
import java.util.Set;

import ch.systemsx.cisd.cifex.upload.IUploadService;
import ch.systemsx.cisd.cifex.upload.UploadState;
import ch.systemsx.cisd.cifex.upload.UploadStatus;
import ch.systemsx.cisd.common.exceptions.WrappedIOException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class Uploader
{
    private final Set<IUploadListener> listeners = new LinkedHashSet<IUploadListener>();
    private final IUploadService uploadService;
    private final String uploadSessionID;

    Uploader(IUploadService uploadService, String uploadSessionID)
    {
        this.uploadService = uploadService;
        this.uploadSessionID = uploadSessionID;
    }
    
    void addUploadListener(IUploadListener uploadListener)
    {
        listeners.add(uploadListener);
    }
    
    void upload()
    {
        UploadStatus status = getStatus();
        while (status.getUploadState() != UploadState.FINISHED)
        {
            status = upload(status);
        }
    }

    private UploadStatus upload(UploadStatus status)
    {
        UploadStatus currentStatus = status;
        UploadState state = currentStatus.getUploadState();
        File file = new File(currentStatus.getCurrentFile());
        RandomAccessFileProvider fileProvider = new RandomAccessFileProvider(file);
        byte[] bytes = new byte[UploadStatus.BLOCK_SIZE];
        EnumSet<UploadState> set = EnumSet.of(UploadState.INIT, UploadState.UPLOADING);
        while (true)
        {
            if (state == UploadState.INIT)
            {
                fireStartedEvent(file);
            } 
            if (state == UploadState.INIT || state == UploadState.UPLOADING)
            {
                currentStatus = uploadNextBlock(fileProvider, currentStatus.getBlockIndex(), bytes);
                state = currentStatus.getUploadState();
                if (state != UploadState.UPLOADING)
                {
                    fileProvider.closeFile();
                    return currentStatus;
                }
            }
        }
    }
    
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
    
    private UploadStatus uploadNextBlock(RandomAccessFileProvider fileProvider, long blockIndex, byte[] bytes)
    {
        try
        {
            RandomAccessFile randomAccessFile = fileProvider.getRandomAccessFile();
            int blockSize = bytes.length;
            long filePointer = blockIndex * blockSize;
            long fileSize = randomAccessFile.length();
            boolean lastBlock = filePointer + blockSize >= fileSize;
            if (lastBlock)
            {
                blockSize = (int) (fileSize - filePointer);
            }
            randomAccessFile.seek(filePointer);
            randomAccessFile.readFully(bytes, 0, blockSize);
            fireProgressEvent(filePointer, fileSize);
            return uploadService.uploadBlock(uploadSessionID, bytes, blockSize, lastBlock);
        } catch (IOException ex)
        {
            throw new WrappedIOException(ex);
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

    private void fireStartedEvent(File file)
    {
        for (IUploadListener listener : listeners)
        {
            listener.uploadingStarted(file);
        }
    }
    
    private UploadStatus getStatus()
    {
        return uploadService.getUploadStatus(uploadSessionID);
    }
}
