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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.UploadState;
import ch.systemsx.cisd.cifex.rpc.UploadStatus;
import ch.systemsx.cisd.cifex.rpc.client.gui.IUploadListener;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * Class which uploads file via an implementation of {@link ICIFEXRPCService}, handling the
 * low-level protocol. Registered {@link IUploadListener} instances will be informed what's going on
 * during uploading.
 * 
 * @author Franz-Josef Elmer
 */
public final class Uploader extends AbstractUploadDownload
{
    private static final EnumSet<UploadState> RUNNING_STATES =
        EnumSet.of(UploadState.READY_FOR_NEXT_FILE, UploadState.UPLOADING);

    private final Set<IUploadListener> listeners = new LinkedHashSet<IUploadListener>();

    /**
     * Creates an instance for the specified service URL and credentials.
     */
    public Uploader(String serviceURL, String username, String passwd)
            throws AuthorizationFailureException, EnvironmentFailureException
    {
        super(serviceURL, username, passwd);
    }

    /**
     * Creates an instance for the specified service URL and session ID.
     */
    public Uploader(String serviceURL, String sessionID)
    {
        super(serviceURL, sessionID);
    }

    /**
     * Creates an instance for the specified service and session ID.
     */
    public Uploader(ICIFEXRPCService service, String sessionID)
    {
        super(service, sessionID);
    }

    protected void logException(RuntimeException ex)
    {
        fireExceptionEvent(ex);
    }

    /**
     * Adds a listener for upload events.
     */
    public void addUploadListener(IUploadListener uploadListener)
    {
        listeners.add(uploadListener);
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
                        fileProvider = new RandomAccessFileProvider(file);
                        fireStartedEvent(file, fileSize);
                        service.startUploading(sessionID);
                        break;
                    case UPLOADING:
                        uploadNextBlock(fileProvider, status);
                        fireProgressEvent(status.getFilePointer(), fileSize);
                        break;
                    case FINISHED:
                        service.finish(sessionID, true);
                        service.logout(sessionID);
                        fireFinishedEvent(true);
                        running = false;
                        break;
                    case ABORTED:
                        System.out.println(status);
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
        for (int i = 0; i < MAX_RETRIES; ++i)
        {
            try
            {
                service.uploadBlock(sessionID, filePointer, bytes, lastBlock);
                break;
            } catch (RemoteAccessException ex)
            {
                ex.printStackTrace();
                System.err.println("Waiting for " + WAIT_AFTER_FAILURE_MILLIS
                        + "ms before retrying...");
                ConcurrencyUtilities.sleep(WAIT_AFTER_FAILURE_MILLIS);
            }
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

    void fireExceptionEvent(Throwable throwable)
    {
        for (IUploadListener listener : listeners)
        {
            listener.exceptionOccured(throwable);
        }
    }

    private void fireResetEvent()
    {
        for (IUploadListener listener : listeners)
        {
            listener.reset();
        }
    }

}
