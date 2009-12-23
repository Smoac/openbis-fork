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
import java.util.List;

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
    public Uploader(ICIFEXRPCService service, String sessionID, int dummyBlockSize)
    {
        super(service, sessionID);
    }

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
     * @param recipientsOrNull Comma or space-separated list of e-mail addresses or user ID's in the
     *            form <code>id:<i>user ID</i></code>. Can be an empty string.
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
            RemoteAccessException lastExceptionOrNull = null;
            for (File file : files)
            {
                for (int i = 0; i < MAX_RETRIES; ++i)
                {
                    if (cancelled.get())
                    {
                        fireFinishedEvent(false);
                        return;
                    }
                    try
                    {
                        uploadFile(file, comment, fileIds);
                        lastExceptionOrNull = null;
                        break;
                    } catch (RemoteAccessException ex)
                    {
                        if (cancelled.get())
                        {
                            fireFinishedEvent(false);
                            return;
                        }
                        lastExceptionOrNull = ex;
                        if (ex.getMessage() != null)
                        {
                            fireWarningEvent("Error during upload: " + ex.getClass().getSimpleName()
                                    + ": '" + ex.getMessage() + "', will retry download soon...");
                        } else
                        {
                            fireWarningEvent("Error during upload: " + ex.getClass().getSimpleName()
                                    + ", will retry download soon...");
                        }
                        sleepAfterFailure();
                    }
                }
            }
            if (lastExceptionOrNull != null)
            {
                throw lastExceptionOrNull;
            }
            if (recipientsOrNull != null && recipientsOrNull.trim().length() > 0)
            {
                service.shareFiles(sessionID, fileIds, recipientsOrNull);
            }
            fireFinishedEvent(true);
        } catch (Throwable th2)
        {
            if (th2 instanceof InterruptedExceptionUnchecked)
            {
                fireFinishedEvent(false);
                return;
            }
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

    private void uploadFile(File file, String comment, List<Long> fileIds) throws IOException
    {
        final long fileSize = file.length();
        final FilePreregistrationDTO fileSpecs =
                new FilePreregistrationDTO(file.getCanonicalPath(), fileSize);
        ResumingAndChecksummingInputStream contentStream = null;
        try
        {
            fireStartedEvent(file, fileSize);
            contentStream =
                    new ResumingAndChecksummingInputStream(file, PROGRESS_REPORT_BLOCK_SIZE,
                            new IWriteProgressListener()
                                {
                                    long lastUpdated = System.currentTimeMillis();

                                    public void update(long bytesRead, int crc32Value)
                                    {
                                        if (cancelled.get())
                                        {
                                            throw new InterruptedExceptionUnchecked();
                                        }
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
