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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.CRC32;

import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;

/**
 * A common subclass for file upload and download.
 * 
 * @author Bernd Rinn
 */
public abstract class AbstractUploadDownload implements ICIFEXOperation
{
    protected static final int BLOCK_SIZE = 256 * 1024;

    protected static final int MAX_RETRIES = 30;

    protected static final long WAIT_AFTER_FAILURE_MILLIS = 10 * 1000L;

    protected final ICIFEXRPCService service;

    protected final String sessionID;

    protected final Set<IProgressListener> listeners = new LinkedHashSet<IProgressListener>();

    protected final CRC32 crc32 = new CRC32();

    protected final AtomicBoolean cancelled = new AtomicBoolean(false);
    
    protected final AtomicBoolean inProgress = new AtomicBoolean(false);
    
    /**
     * Creates an instance for the specified service and session ID.
     */
    public AbstractUploadDownload(ICIFEXRPCService service, String sessionID)
    {
        this.service = service;
        this.sessionID = sessionID;
        checkService();
    }

    protected void checkService() throws InvalidSessionException, EnvironmentFailureException
    {
        final int serverVersion = service.getVersion();
        if (ICIFEXRPCService.VERSION != serverVersion)
        {
            throw new EnvironmentFailureException(
                    "This client has the wrong service version for the server (client: "
                            + ICIFEXRPCService.VERSION + ", server: " + serverVersion + ").");
        }
        service.checkSession(sessionID);
    }

    /**
     * Returns <code>true</code> if the operation (upload or download) is still in progress.
     */
    public boolean isInProgress()
    {
        return inProgress.get();
    }

    /**
     * Cancels the operation (upload or download).
     */
    public void cancel()
    {
        cancelled.set(true);
    }

    protected void fireStartedEvent(File file, long fileSize)
    {
        for (IProgressListener listener : listeners)
        {
            listener.start(file, fileSize);
        }
    }

    protected void fireProgressEvent(long numberOfBytes, long fileSize)
    {
        int percentage = (int) ((numberOfBytes * 100) / Math.max(1, fileSize));
        for (IProgressListener listener : listeners)
        {
            listener.reportProgress(percentage, numberOfBytes);
        }
    }

    protected void fireFinishedEvent(boolean successful)
    {
        for (IProgressListener listener : listeners)
        {
            listener.finished(successful);
        }
    }

    protected void fireExceptionEvent(Throwable throwable)
    {
        for (IProgressListener listener : listeners)
        {
            listener.exceptionOccured(throwable);
        }
    }

    protected void fireWarningEvent(String warningMessage)
    {
        for (IProgressListener listener : listeners)
        {
            listener.warningOccured(warningMessage);
        }
    }

}