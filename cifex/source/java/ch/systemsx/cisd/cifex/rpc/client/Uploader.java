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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.cifex.rpc.FilePreregistrationDTO;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.cifex.rpc.client.gui.IUploadProgressListener;
import ch.systemsx.cisd.cifex.rpc.io.ResumingAndChecksummingInputStream;
import ch.systemsx.cisd.cifex.rpc.io.ResumingAndChecksummingInputStream.ChecksumHandling;
import ch.systemsx.cisd.cifex.rpc.io.ResumingAndChecksummingInputStream.IWriteProgressListener;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.common.concurrent.MonitoringProxy;
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

    private static interface IFileUploader
    {
        boolean uploadFile(File file, String comment, Set<Long> fileIds) throws IOException;
    }

    private final MonitoringProxy<IFileUploader> proxyForOperation;

    private final IFileUploader retryingFileUploader;

    /**
     * Creates an instance for the specified service and session ID.
     */
    public Uploader(ICIFEXRPCService service, String sessionID)
    {
        super(service, sessionID);
        this.proxyForOperation = createFileUploaderProxy();
        this.retryingFileUploader = proxyForOperation.get();
    }

    @Override
    protected MonitoringProxy<?> getProxyForOperation()
    {
        return proxyForOperation;
    }

    private MonitoringProxy<IFileUploader> createFileUploaderProxy()
    {
        IFileUploader rawFileUploader = new IFileUploader()
            {
                // This method can return false only if error occurred
                public boolean uploadFile(File file, String comment, Set<Long> fileIds)
                        throws IOException
                {
                    doUploadFile(file, comment, fileIds);
                    return true;
                }
            };
        final InvocationLogger logger = new InvocationLogger(this);
        return MonitoringProxy.create(IFileUploader.class, rawFileUploader).sensor(
                getActivitySensor()).exceptionClassSuitableForRetrying(RemoteAccessException.class)
                .timing(TIMING).errorValueOnInterrupt().errorTypeValueMapping(Boolean.TYPE, false)
                .invocationLog(logger);
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

                public void finished(boolean successful, List<String> warningMessages,
                        List<Throwable> exceptions)
                {
                    listener.finished(successful, warningMessages, exceptions);
                }

                public void reportProgress(int percentage, long numberOfBytes)
                {
                    listener.reportProgress(percentage, numberOfBytes);
                }

                public void start(File file, long fileSize, Long fileIdOrNull)
                {
                    listener.start(file, fileSize, fileIdOrNull);
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
     * @param recipientsOrNull Comma or space-separated list of e-mail addresses or user ID's in the
     *            form <code>id:<i>user ID</i></code>. Can be an empty string.
     * @param comment Optional comment added to the outgoing e-mails. Can be an empty string.
     */
    public void upload(List<File> files, String recipientsOrNull, String comment)
    {
        resetCancel();
        if (files.isEmpty())
        {
            return;
        }
        try
        {
            inProgress.set(true);
            Set<Long> fileIds = new HashSet<Long>();
            for (File file : files)
            {
                if (isCancelled())
                {
                    fireFinishedEvent(false);
                    return;
                }
                boolean ok = retryingFileUploader.uploadFile(file, comment, fileIds);
                if (ok == false)
                {
                    return; // upload cancelled
                }
            }
            if (recipientsOrNull != null && recipientsOrNull.trim().length() > 0)
            {
                service.shareFiles(sessionID, new ArrayList<Long>(fileIds), recipientsOrNull);
            }
            fireFinishedEvent(true);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            fireResetEvent();
            inProgress.set(false);
        }
    }

    // NOTE: this is a non-retrying version used by retryingFileUloader
    private void doUploadFile(File file, String comment, Set<Long> fileIds) throws IOException
    {
        final long fileSize = file.length();
        final FilePreregistrationDTO fileSpecs =
                new FilePreregistrationDTO(file.getCanonicalPath(), fileSize);
        ResumingAndChecksummingInputStream contentStream = null;
        try
        {
            fireStartedEvent(file, fileSize, null);
            contentStream =
                    new ResumingAndChecksummingInputStream(file, PROGRESS_REPORT_BLOCK_SIZE,
                            new IWriteProgressListener()
                                {
                                    long lastUpdated = System.currentTimeMillis();

                                    public void update(long bytesRead, int crc32Value)
                                    {
                                        if (isCancelled())
                                        {
                                            throw new InterruptedExceptionUnchecked();
                                        }
                                        observerSensor.update();
                                        final long now = System.currentTimeMillis();
                                        if (now - lastUpdated > PROGRESS_UPDATE_MIN_INTERVAL_MILLIS
                                                || bytesRead == fileSize)
                                        {
                                            fireProgressEvent(bytesRead, fileSize);
                                            lastUpdated = now;
                                        }
                                    }
                                }, ChecksumHandling.COMPUTE_AND_APPEND);
            final FileInfoDTO resumeFileInfoOrNull =
                    tryGetFileInfoForResumeUpload(contentStream, fileSpecs);
            final long fileId;
            if (resumeFileInfoOrNull != null)
            {
                fireProgressEvent(resumeFileInfoOrNull.getSize(), fileSize);
                service.resumeUpload(sessionID, resumeFileInfoOrNull.getID(), resumeFileInfoOrNull
                        .getSize(), comment, contentStream);
                fileId = resumeFileInfoOrNull.getID();
            } else
            {
                fireProgressEvent(0L, fileSize);
                fileId = service.upload(sessionID, fileSpecs, comment, contentStream);
            }
            fileIds.add(fileId);
            if (fileSize != file.length())
            {
                // File size has changed during upload
                service.deleteFile(sessionID, fileId);
                throw EnvironmentFailureException.fromTemplate(
                        "File size has changed during upload [expected: %d, found: %d]", fileSize,
                        file.length());
            }
        } finally
        {
            IOUtils.closeQuietly(contentStream);
        }
        fireUploadedEvent();
    }

    private FileInfoDTO tryGetFileInfoForResumeUpload(
            final ResumingAndChecksummingInputStream contentStream,
            final FilePreregistrationDTO fileSpecs) throws IOException
    {
        final FileInfoDTO uploadCandidateOrNull =
                service.tryGetUploadResumeCandidate(sessionID, fileSpecs);
        if (uploadCandidateOrNull != null
                && uploadCandidateOrNull.getSize() <= contentStream.getLength())
        {
            // Verify the checksum of the candidate
            contentStream.setStartPos(uploadCandidateOrNull.getSize());
            if (contentStream.getCrc32Value() == uploadCandidateOrNull.getCrc32Value())
            {
                return uploadCandidateOrNull;
            } else
            {
                contentStream.setStartPos(0L);
            }
        }
        return null;
    }

    private void fireUploadedEvent()
    {
        for (IProgressListener listener : listeners)
        {
            try
            {
                ((IUploadProgressListener) listener).fileUploaded();
            } catch (Throwable th)
            {
                th.printStackTrace();
            }
        }
    }

    private void fireResetEvent()
    {
        for (IProgressListener listener : listeners)
        {
            try
            {
                ((IUploadProgressListener) listener).reset();
            } catch (Throwable th)
            {
                th.printStackTrace();
            }
        }
    }

}
