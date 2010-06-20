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
import ch.systemsx.cisd.common.concurrent.MonitoringProxy;
import ch.systemsx.cisd.common.concurrent.MonitoringProxy.IMonitorCommunicator;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOverwriteStrategy;

/**
 * Class which downloads file via an implementation of {@link ICIFEXRPCService}, handling the
 * low-level protocol.
 * 
 * @author Bernd Rinn
 */
public final class Downloader extends AbstractUploadDownload implements ICIFEXDownloader
{

    private interface IFileDownloader
    {
        FileInfoDTO getFileInfo(long fileID);

        boolean download(FileInfoDTO fileInfo, File file,
                MonitoringProxy.IMonitorCommunicator communcator);
    }

    /**
     * Creates an instance for the specified service and session ID.
     */
    public static ICIFEXDownloader create(ICIFEXRPCService service, String sessionID)
    {
        return new Downloader(service, sessionID);
    }

    private final MonitoringProxy<IFileDownloader> proxyForOperation;

    private final IFileDownloader retryingFileDownloader;

    private Downloader(ICIFEXRPCService service, String sessionID)
    {
        super(service, sessionID, false);
        this.proxyForOperation = createFileDownloaderProxy();
        this.retryingFileDownloader = proxyForOperation.get();
    }

    @Override
    protected MonitoringProxy<?> getProxyForOperation()
    {
        return proxyForOperation;
    }

    private MonitoringProxy<IFileDownloader> createFileDownloaderProxy()
    {
        IFileDownloader downloader = new IFileDownloader()
            {
                // This method can return false only if error occurred
                public boolean download(FileInfoDTO fileInfo, File file,
                        IMonitorCommunicator communcator)
                {
                    doDownloadFile(fileInfo, file, communcator);
                    return true;
                }

                public FileInfoDTO getFileInfo(long fileID)
                {
                    return service.getFileInfo(sessionID, fileID);
                }
            };
        final InvocationLogger logger = new InvocationLogger(this, reportFinalException);
        return MonitoringProxy.create(IFileDownloader.class, downloader)
                .exceptionClassSuitableForRetrying(RemoteAccessException.class).timing(TIMING)
                .errorValueOnInterrupt().invocationLog(logger);
    }

    /**
     * Adds a listener for progress events.
     */
    public void addProgressListener(IProgressListener listener)
    {
        listeners.add(listener);
    }

    public File download(final long fileID, final File directoryToDownloadOrNull,
            final String fileNameOrNull)
    {
        return download(fileID, directoryToDownloadOrNull, fileNameOrNull, false);
    }

    public File download(final long fileID, final File directoryToDownloadOrNull,
            final String fileNameOrNull, final boolean overwriteOutFile)
    {
        return download(fileID, directoryToDownloadOrNull, fileNameOrNull,
                new IFileOverwriteStrategy()
                    {
                        public boolean overwriteAllowed(File outputFile)
                        {
                            return overwriteOutFile;
                        }
                    });
    }

    public File download(final long fileID, final File directoryToDownloadOrNull,
            final String fileNameOrNull, final IFileOverwriteStrategy fileOverwriteStrategy)
    {
        resetCancel();
        try
        {
            inProgress.set(true);
            final FileInfoDTO fileInfo = retryingFileDownloader.getFileInfo(fileID);
            final File directory =
                    (directoryToDownloadOrNull != null) ? directoryToDownloadOrNull : new File(".");
            final String fileName = (fileNameOrNull != null) ? fileNameOrNull : fileInfo.getName();
            final File file = new File(directory, fileName);
            FileUtilities.checkOutputFile(file, fileOverwriteStrategy);
            fireStartedEvent(file, "Downloading", fileInfo.getSize(), fileID);
            final boolean ok =
                    retryingFileDownloader.download(fileInfo, file,
                            MonitoringProxy.MONITOR_COMMUNICATOR);
            fireFinishedEvent(ok);
            return file;
        } catch (Exception ex)
        {
            fireFinishedEvent(false);
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            inProgress.set(false);
        }
    }

    // NOTE: this is a non-retrying version used by retryingFileDownloader
    private void doDownloadFile(FileInfoDTO fileInfo, File file,
            final MonitoringProxy.IMonitorCommunicator communicator)
    {
        InputStream input = null;
        final long fileSize = fileInfo.getSize();
        final long fileID = fileInfo.getID();
        ResumingAndChecksummingOutputStream output = null;
        final long clientFileSize = file.length();
        if (isCancelled() || communicator.isCancelled())
        {
            throw new InterruptedExceptionUnchecked();
        }
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
                                        if (isCancelled() || communicator.isCancelled())
                                        {
                                            throw new InterruptedExceptionUnchecked();
                                        }
                                        communicator.update();
                                        final long now = System.currentTimeMillis();
                                        if (now - lastUpdated > PROGRESS_UPDATE_MIN_INTERVAL_MILLIS
                                                || bytesWritten == fileSize)
                                        {
                                            fireProgressEvent(bytesWritten, fileSize);
                                            lastUpdated = now;
                                        }
                                    }
                                }, clientFileSize);
            if (isCancelled() || communicator.isCancelled())
            {
                throw new InterruptedExceptionUnchecked();
            }
            IOUtils.copyLarge(input, output);
            if (communicator.isCancelled())
            {
                throw new InterruptedExceptionUnchecked();
            }
            final int crc32Value = output.getCrc32Value();
            if (fileInfo.getCrc32Value() != null && crc32Value != fileInfo.getCrc32Value())
            {
                file.delete();
                throw new CRCCheckumMismatchException(fileInfo.getName(), crc32Value, fileInfo
                        .getCrc32Value(), "File has been deleted");
            }
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(input);
            if (output != null)
            {
                try
                {
                    output.close();
                } catch (IOException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }
    }

}
