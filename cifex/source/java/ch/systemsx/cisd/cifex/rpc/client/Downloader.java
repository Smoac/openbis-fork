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
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.cifex.rpc.CRCCheckumMismatchException;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.cifex.rpc.io.ResumingAndChecksummingOutputStream;
import ch.systemsx.cisd.cifex.rpc.io.ResumingAndChecksummingOutputStream.IWriteProgressListener;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;

/**
 * Class which downloads file via an implementation of {@link ICIFEXRPCService}, handling the
 * low-level protocol.
 * 
 * @author Bernd Rinn
 */
public final class Downloader extends AbstractUploadDownload implements ICIFEXDownloader
{

    /**
     * Creates an instance for the specified service and session ID.
     */
    public Downloader(ICIFEXRPCService service, String sessionID)
    {
        super(service, sessionID);
    }

    /**
     * Creates an instance for the specified service and session ID.
     */
    public Downloader(ICIFEXRPCService service, String sessionID, int blockSize)
    {
        super(service, sessionID, blockSize);
    }

    /**
     * Adds a listener for progress events.
     */
    public void addProgressListener(IProgressListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Downloads the file identified by <var>fileID</var> to the local <var>file</var>.
     * 
     * @param fileID The id of the file in CIFEX.
     * @param directoryToDownloadOrNull The directory to download the file to, or <code>null</code>,
     *            if the file should be downloaded to the current working directory.
     * @param fileNameOrNull The file name to save the file to, or <code>null</code>, if the name
     *            stored in CIFEX should be used.
     */
    public void download(long fileID, File directoryToDownloadOrNull, String fileNameOrNull)
    {
        cancelled.set(false);
        try
        {
            inProgress.set(true);
            final FileInfoDTO fileInfo = service.getFileInfo(sessionID, fileID);
            InputStream input = null;
            final File directory =
                    directoryToDownloadOrNull != null ? directoryToDownloadOrNull : new File(".");
            final String fileName = fileNameOrNull != null ? fileNameOrNull : fileInfo.getName();
            final File file = new File(directory, fileName);
            final long fileSize = fileInfo.getSize();
            fireStartedEvent(file, fileSize);
            RemoteAccessException lastExceptionOrNull = null;
            for (int i = 0; i < MAX_RETRIES; ++i)
            {
                if (cancelled.get())
                {
                    return;
                }
                try
                {
                    ResumingAndChecksummingOutputStream output = null;
                    final long clientFileSize = file.length();
                    try
                    {
                        input = service.download(sessionID, fileID, clientFileSize);
                        output =
                                new ResumingAndChecksummingOutputStream(file, PROGRESS_REPORT_BLOCK_SIZE,
                                        new IWriteProgressListener()
                                            {
                                                long lastUpdated = System.currentTimeMillis();
        
                                                public void update(long bytesWritten, int crc32Value)
                                                {
                                                    if (cancelled.get())
                                                    {
                                                        throw new InterruptedExceptionUnchecked();
                                                    }
                                                    final long now = System.currentTimeMillis();
                                                    if (now - lastUpdated > PROGRESS_UPDATE_MIN_INTERVAL_MILLIS
                                                            || bytesWritten == fileSize)
                                                    {
                                                        fireProgressEvent(bytesWritten, fileSize);
                                                        lastUpdated = now;
                                                    }
                                                }
                                            }, clientFileSize);
                        IOUtils.copyLarge(input, output);
                        final int crc32Value = output.getCrc32Value();
                        if (fileInfo.getCrc32Value() != null && crc32Value != fileInfo.getCrc32Value())
                        {
                            throw new CRCCheckumMismatchException(fileInfo.getName(), crc32Value, fileInfo
                                    .getCrc32Value());
                        }
                    } finally
                    {
                        IOUtils.closeQuietly(input);
                        if (output != null)
                        {
                            output.close();
                        }
                    }
                    lastExceptionOrNull = null;
                    break;
                } catch (RemoteAccessException ex)
                {
                    if (cancelled.get())
                    {
                        return;
                    }
                    lastExceptionOrNull = ex;
                    fireWarningEvent("Error during download: " + ex.getClass().getSimpleName() + ": "
                            + ex.getMessage() + ", will retry download soon...");
                    sleepAfterFailure();
                }
            }
            if (lastExceptionOrNull != null)
            {
                throw lastExceptionOrNull;
            }
            fireFinishedEvent(true);
        } catch (Throwable th)
        {
            if (th instanceof InterruptedExceptionUnchecked)
            {
                return;
            }
            fireFinishedEvent(false);
            throw CheckedExceptionTunnel.wrapIfNecessary(th);
        } finally
        {
            inProgress.set(false);
        }
    }

}
