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

import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Class which downloads file via an implementation of {@link ICIFEXRPCService}, handling the
 * low-level protocol.
 * 
 * @author Bernd Rinn
 */
public final class Downloader extends AbstractUploadDownload
{

    /**
     * Creates an instance for the specified service URL and credentials.
     */
    public Downloader(String serviceURL, String username, String passwd)
            throws AuthorizationFailureException, EnvironmentFailureException
    {
        super(serviceURL, username, passwd);
    }

    /**
     * Creates an instance for the specified service URL and session ID.
     */
    public Downloader(String serviceURL, String sessionID, boolean getCertiticateFromServer)
    {
        super(serviceURL, sessionID, getCertiticateFromServer);
    }

    /**
     * Creates an instance for the specified service and session ID.
     */
    public Downloader(ICIFEXRPCService service, String sessionID)
    {
        super(service, sessionID);
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
        try
        {
            final FileInfoDTO fileInfo = service.startDownloading(sessionID, fileID);
            final File directory =
                    directoryToDownloadOrNull != null ? directoryToDownloadOrNull : new File(".");
            final String fileName = fileNameOrNull != null ? fileNameOrNull : fileInfo.getName();
            final File file = new File(directory, fileName);
            final long fileSize = fileInfo.getSize();
            fireStartedEvent(file, fileSize);
            final RandomAccessFileProvider fileProvider = new RandomAccessFileProvider(file, "rw");
            try
            {
                long filePointer = 0L;
                while (filePointer < fileSize)
                {
                    final int blockSize =
                            (int) Math.min(fileInfo.getSize() - filePointer, BLOCK_SIZE);
                    downloadAndStoreBlock(fileProvider, filePointer, blockSize);
                    filePointer += blockSize;
                    fireProgressEvent(filePointer, fileSize);
                }
                fireFinishedEvent(true);
            } finally
            {
                fileProvider.closeFile();
            }
        } catch (Throwable th)
        {
            fireFinishedEvent(false);
            throw CheckedExceptionTunnel.wrapIfNecessary(th);
        }
    }

    private void downloadAndStoreBlock(final RandomAccessFileProvider fileProvider,
            long filePointer, final int blockSize) throws IOException
    {
        RemoteAccessException lastExceptionOrNull = null;
        for (int i = 0; i < MAX_RETRIES; ++i)
        {
            try
            {
                byte[] block = service.downloadBlock(sessionID, filePointer, blockSize);
                fileProvider.getRandomAccessFile().seek(filePointer);
                fileProvider.getRandomAccessFile().write(block);
                lastExceptionOrNull = null;
                break;
            } catch (RemoteAccessException ex)
            {
                lastExceptionOrNull = ex;
                fireWarningEvent("Error during download: " + ex.getClass().getSimpleName() + ": "
                        + ex.getMessage() + ", will retry download soon...");
                ConcurrencyUtilities.sleep(WAIT_AFTER_FAILURE_MILLIS);
            }
        }
        if (lastExceptionOrNull != null)
        {
            throw lastExceptionOrNull;
        }
    }
}
