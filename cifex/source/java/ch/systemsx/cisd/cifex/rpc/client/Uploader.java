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
import java.util.ArrayList;
import java.util.List;

import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.cifex.rpc.FilePreregistrationDTO;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.cifex.rpc.client.gui.IUploadProgressListener;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Class which uploads file via an implementation of {@link ICIFEXRPCService}, handling the
 * low-level protocol. Registered {@link IProgressListener} instances will be informed what's going
 * on during uploading.
 * 
 * @author Franz-Josef Elmer
 */
public final class Uploader extends AbstractUploadDownload implements ICIFEXUploader
{
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
     * Uploads the specified files for the specified recipients.
     * 
     * @param recipientsOrNull Comma or space-separated list of e-mail addresses or user ID's in the form
     *            <code>id:<i>user ID</i></code>. Can be an empty string.
     * @param comment Optional comment added to the outgoing e-mails. Can be an empty string.
     */
    public void upload(List<File> files, String recipientsOrNull, String comment)
    {
        cancelled.set(false);
        if (files.isEmpty())
        {
            return;
        }
        try
        {
            inProgress.set(true);
            List<Long> fileIds = new ArrayList<Long>(files.size());
            for (File file : files)
            {
                try
                {
                    crc32.reset();
                    final long fileSize = file.length();
                    final FilePreregistrationDTO filePreDTO =
                            new FilePreregistrationDTO(file.getCanonicalPath(), fileSize);
                    final RandomAccessFileProvider fileProvider =
                            new RandomAccessFileProvider(file, "r");
                    try
                    {
                        fireStartedEvent(file, fileSize);
                        final long fileId = service.startUploading(sessionID, filePreDTO, comment);
                        fileIds.add(fileId);
                        long filePointer = 0L;
                        fireProgressEvent(filePointer, fileSize);
                        while (filePointer < fileSize)
                        {
                            final int blockSize =
                                    (int) Math.min(fileSize - filePointer, BLOCK_SIZE);
                            uploadNextBlock(fileProvider, filePointer, blockSize);
                            if (cancelled.get())
                            {
                                service.finish(sessionID, false);
                                fireFinishedEvent(false);
                                return;
                            }
                            filePointer += blockSize;
                            fireProgressEvent(filePointer, fileSize);
                        }
                    } finally
                    {
                        fileProvider.closeFile();
                    }
                    fireUploadedEvent();
                    service.finish(sessionID, true);
                } catch (Throwable th1)
                {
                    try
                    {
                        service.finish(sessionID, false);
                    } catch (Throwable th2)
                    {
                        // Nothing we can do here.
                    }
                    throw CheckedExceptionTunnel.wrapIfNecessary(th1);
                }
            }
            if (recipientsOrNull != null && recipientsOrNull.trim().length() > 0)
            {
                service.shareFiles(sessionID, fileIds, recipientsOrNull);
            }
            fireFinishedEvent(true);
        } catch (Throwable th2)
        {
            fireExceptionEvent(th2 instanceof Exception ? CheckedExceptionTunnel
                    .unwrapIfNecessary((Exception) th2) : th2);
            fireFinishedEvent(false);
            throw CheckedExceptionTunnel.wrapIfNecessary(th2);
        } finally
        {
            fireResetEvent();
            inProgress.set(false);
        }
    }

    private void uploadNextBlock(final RandomAccessFileProvider fileProvider,
            final long filePointer, final int blockSize) throws IOException,
            EnvironmentFailureException
    {
        final RandomAccessFile randomAccessFile = fileProvider.getRandomAccessFile();
        final byte[] bytes = new byte[blockSize];
        randomAccessFile.seek(filePointer);
        randomAccessFile.readFully(bytes, 0, blockSize);
        crc32.update(bytes);
        final int runningCrc32Value = (int) crc32.getValue();
        RemoteAccessException lastExceptionOrNull = null;
        for (int i = 0; i < MAX_RETRIES; ++i)
        {
            if (cancelled.get())
            {
                return;
            }
            try
            {
                service.uploadBlock(sessionID, filePointer, runningCrc32Value, bytes);
                lastExceptionOrNull = null;
                break;
            } catch (RemoteAccessException ex)
            {
                lastExceptionOrNull = ex;
                fireWarningEvent("Error during upload: " + ex.getClass().getSimpleName() + ": "
                        + ex.getMessage() + ", will retry upload soon...");
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
