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
import java.io.FileNotFoundException;
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
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.WrappedIOException;

/**
 * Class which uploads file via an implementation of {@link ICIFEXRPCService}. It handles the
 * protocol of the contract of <code>IUploadService</code>. Registered {@link IUploadListener}
 * instances will be informed what's going on during uploading.
 * 
 * @author Franz-Josef Elmer
 */
public final class Uploader
{
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

    private static final int BLOCK_SIZE = 1 * 1024 * 1024; // 1MB

    private static final int MAX_RETRIES = 30; // Retry for 5 minutes in total.

    private static final long WAIT_AFTER_FAILURE_MILLIS = 10 * 1000L; // 10s

    private final Set<IUploadListener> listeners = new LinkedHashSet<IUploadListener>();

    private final ICIFEXRPCService uploadService;

    private final String sessionID;

    /**
     * Creates an instance for the specified service URL and credentials.
     */
    public Uploader(String serviceURL, String username, String passwd)
            throws AuthorizationFailureException, EnvironmentFailureException
    {
        this.uploadService = RPCServiceFactory.createServiceProxy(serviceURL);
        this.sessionID = uploadService.login(username, passwd);
        checkService();
    }

    /**
     * Creates an instance for the specified service URL and session ID.
     */
    public Uploader(String serviceURL, String sessionID)
    {
        this(RPCServiceFactory.createServiceProxy(serviceURL), sessionID);
    }

    /**
     * Creates an instance for the specified service and session ID.
     */
    public Uploader(ICIFEXRPCService uploadService, String uploadSessionID)
    {
        this.uploadService = uploadService;
        this.sessionID = uploadSessionID;
        checkService();
    }

    private void checkService() throws InvalidSessionException, EnvironmentFailureException
    {
        final int serverVersion = uploadService.getVersion();
        if (ICIFEXRPCService.VERSION != serverVersion)
        {
            throw new EnvironmentFailureException(
                    "This client has the wrong service version for the server (client: "
                            + ICIFEXRPCService.VERSION + ", server: " + serverVersion + ").");
        }
        uploadService.checkSession(sessionID);
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
            UploadStatus status = uploadService.getUploadStatus(sessionID);
            return RUNNING_STATES.contains(status.getUploadState());
        } catch (RuntimeException ex)
        {
            fireExceptionEvent(ex);
            return false;
        }
    }

    /**
     * Cancels uploading.
     */
    public void cancel()
    {
        try
        {
            uploadService.cancel(sessionID);
        } catch (RuntimeException ex)
        {
            fireExceptionEvent(ex);
        }
    }

    /**
     * Logout from session.
     */
    public void logout()
    {
        try
        {
            uploadService.logout(sessionID);
        } catch (RuntimeException ex)
        {
            ex.printStackTrace();
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
                UploadStatus status = uploadService.getUploadStatus(sessionID);
                switch (status.getUploadState())
                {
                    case INITIALIZED:
                        uploadService.defineUploadParameters(sessionID, paths, recipients, comment);
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
                        uploadService.startUploading(sessionID);
                        break;
                    case UPLOADING:
                        uploadNextBlock(fileProvider, status);
                        fireProgressEvent(status.getFilePointer(), fileSize);
                        break;
                    case FINISHED:
                        uploadService.finish(sessionID, true);
                        uploadService.logout(sessionID);
                        fireFinishedEvent(true);
                        running = false;
                        break;
                    case ABORTED:
                        System.out.println(status);
                        uploadService.finish(sessionID, false);
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
                uploadService.finish(sessionID, false);
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
        RandomAccessFile randomAccessFile = fileProvider.getRandomAccessFile();
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
                uploadService.uploadBlock(sessionID, filePointer, bytes, lastBlock);
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

    private void fireExceptionEvent(Throwable throwable)
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
