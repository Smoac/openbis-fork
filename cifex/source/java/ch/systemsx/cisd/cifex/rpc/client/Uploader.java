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

package ch.systemsx.cisd.cifex.rpc.client;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.EnumSet;
import java.util.List;

import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.UploadState;
import ch.systemsx.cisd.cifex.rpc.UploadStatus;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.cifex.rpc.client.gui.IUploadProgressListener;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Class which uploads file via an implementation of {@link ICIFEXRPCService}, handling the
 * low-level protocol. Registered {@link IProgressListener} instances will be informed what's going
 * on during uploading.
 * 
 * @author Franz-Josef Elmer
 */
public final class Uploader extends AbstractUploadDownload
{
    private static final EnumSet<UploadState> RUNNING_STATES =
            EnumSet.of(UploadState.READY_FOR_NEXT_FILE, UploadState.UPLOADING);

    /**
     * Creates an instance for the specified service and session ID.
     */
    public Uploader(ICIFEXRPCService service, String sessionID)
    {
        super(service, sessionID);
    }

    /**
     * Adds a listener for upload events.
     */
    public void addProgressListener(final IUploadProgressListener uploadListener)
    {
        listeners.add(uploadListener);
    }

    /**
     * Adds a listener for progress events.
     */
    public void addProgressListener(final IProgressListener listener)
    {
        listeners.add(new IUploadProgressListener()
        {

            public void exceptionOccured(Throwable throwable)
            {
                listener.exceptionOccured(throwable);
            }

            public void finished(boolean successful)
            {
                listener.finished(successful);
            }

            public void reportProgress(int percentage, long numberOfBytes)
            {
                listener.reportProgress(percentage, numberOfBytes);
            }

            public void start(File file, long fileSize)
            {
                listener.start(file, fileSize);
            }

            public void warningOccured(String warningMessage)
            {
                listener.warningOccured(warningMessage);
            }
            
            public void fileUploaded()
            {
            }

            public void reset()
            {
            }

        });
    }

    /**
     * Returns <code>true</code> if this uploader is still working.
     */
    public boolean isUploading()
    {
        try
        {
            UploadStatus status = service.getUploadStatus(sessionID);
            return RUNNING_STATES.contains(status.getUploadState());
        } catch (RuntimeException ex)
        {
            fireExceptionEvent(ex);
            return false;
        }
    }

    /**
     * Uploads the specified files for the specified recipients.
     * 
     * @param recipients Comma or space-separated list of e-mail addresses or user ID's in the form
     *            <code>id:<i>user ID</i></code>. Can be an empty string.
     * @param comment Optional comment added to the outgoing e-mails. Can be an empty string.
     */
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
            fireExceptionEvent(ex);
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }

        try
        {
            RandomAccessFileProvider fileProvider = null;
            long fileSize = 0;
            boolean running = true;
            while (running)
            {
                UploadStatus status = service.getUploadStatus(sessionID);
                switch (status.getUploadState())
                {
                    case INITIALIZED:
                        service.defineUploadParameters(sessionID, paths, recipients, comment);
                        break;
                    case READY_FOR_NEXT_FILE:
                        if (fileProvider != null)
                        {
                            fileProvider.closeFile();
                            fireUploadedEvent();
                        }
                        File file = new File(status.getCurrentFile());
                        fileSize = file.length();
                        fileProvider = new RandomAccessFileProvider(file, "r");
                        fireStartedEvent(file, fileSize);
                        service.startUploading(sessionID);
                        break;
                    case UPLOADING:
                        uploadNextBlock(fileProvider, status);
                        fireProgressEvent(status.getFilePointer(), fileSize);
                        break;
                    case FINISHED:
                        service.finish(sessionID, true);
                        fireFinishedEvent(true);
                        running = false;
                        break;
                    case ABORTED:
                        service.finish(sessionID, false);
                        fireFinishedEvent(false);
                        running = false;
                        break;
                }
            }
        } catch (Throwable throwable)
        {
            fireExceptionEvent(throwable);
            try
            {
                service.finish(sessionID, false);
            } catch (Throwable throwable2)
            {
                fireExceptionEvent(throwable2);
                throwable = throwable2;
            }
            fireFinishedEvent(false);
            throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
        } finally
        {
            fireResetEvent();
        }
    }

    private void uploadNextBlock(RandomAccessFileProvider fileProvider, UploadStatus status)
            throws IOException, EnvironmentFailureException
    {
        final RandomAccessFile randomAccessFile = fileProvider.getRandomAccessFile();
        int blockSize = BLOCK_SIZE;
        final long fileSize = randomAccessFile.length();
        final long filePointer = status.getFilePointer();
        final boolean lastBlock = filePointer + blockSize >= fileSize;
        if (lastBlock)
        {
            blockSize = (int) (fileSize - filePointer);
        }
        final byte[] bytes = new byte[blockSize];
        randomAccessFile.seek(filePointer);
        randomAccessFile.readFully(bytes, 0, blockSize);
        RemoteAccessException lastExceptionOrNull = null;
        for (int i = 0; i < MAX_RETRIES; ++i)
        {
            try
            {
                service.uploadBlock(sessionID, filePointer, bytes, lastBlock);
                lastExceptionOrNull = null;
                break;
            } catch (RemoteAccessException ex)
            {
                lastExceptionOrNull = ex;
                fireWarningEvent("Error during upload: " + ex.getClass().getSimpleName() + ": "
                        + ex.getMessage() + ", will retry download soon...");
                ConcurrencyUtilities.sleep(WAIT_AFTER_FAILURE_MILLIS);
            }
        }
        if (lastExceptionOrNull != null)
        {
            throw lastExceptionOrNull;
        }
    }

    private void fireUploadedEvent()
    {
        for (IProgressListener listener : listeners)
        {
            ((IUploadProgressListener) listener).fileUploaded();
        }
    }

    private void fireResetEvent()
    {
        for (IProgressListener listener : listeners)
        {
            ((IUploadProgressListener) listener).reset();
        }
    }

}
