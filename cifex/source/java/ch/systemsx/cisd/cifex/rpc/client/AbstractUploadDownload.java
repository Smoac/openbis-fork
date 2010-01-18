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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.common.concurrent.IActivitySensor;
import ch.systemsx.cisd.common.concurrent.RecordingActivityObserverSensor;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;

/**
 * A common subclass for file upload and download.
 * 
 * @author Bernd Rinn
 */
public abstract class AbstractUploadDownload implements ICIFEXOperation
{
    protected static final int PROGRESS_REPORT_BLOCK_SIZE = 128 * 1024; // 128kB

    protected static final int PROGRESS_UPDATE_MIN_INTERVAL_MILLIS = 1 * 1000; // 1 second

    protected static final int MAX_RETRIES = 600;

    private static final long WAIT_AFTER_FAILURE_MILLIS = 10 * 1000L;

    protected final ICIFEXRPCService service;

    protected final String sessionID;

    protected final Set<IProgressListener> listeners = new LinkedHashSet<IProgressListener>();

    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    protected final AtomicBoolean inProgress = new AtomicBoolean(false);

    protected final RecordingActivityObserverSensor observerSensor =
            new RecordingActivityObserverSensor();

    private final List<String> encounteredWarningMessages;

    private final List<Throwable> encounteredExceptions;

    /**
     * Creates an instance for the specified service and session ID.
     */
    public AbstractUploadDownload(ICIFEXRPCService service, String sessionID)
    {
        this.service = service;
        this.sessionID = sessionID;
        this.encounteredWarningMessages = new ArrayList<String>();
        this.encounteredExceptions = new ArrayList<Throwable>();
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
     * Returns the activity sensor of this uplaoder / downloader.
     */
    public IActivitySensor getActivitySensor()
    {
        return observerSensor;
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

    /**
     * Resets the state of cancellation.
     */
    protected void resetCancel()
    {
        cancelled.set(false);
        Thread.interrupted();
    }

    /**
     * Checks whether the operation has been cancelled. Resets the cancel state, i.e. if you call it
     * twice, the second call will return <code>false</code>.
     */
    protected boolean isCancelled()
    {
        final boolean cancelCalled = cancelled.get();
        resetCancel();
        return cancelCalled || Thread.interrupted();
    }

    protected void fireStartedEvent(File file, long fileSize)
    {
        for (IProgressListener listener : listeners)
        {
            try
            {
                listener.start(file, fileSize);
            } catch (Throwable th)
            {
                th.printStackTrace();
            }
        }
    }

    protected void fireProgressEvent(long numberOfBytes, long fileSize)
    {
        int percentage = (int) ((numberOfBytes * 100) / Math.max(1, fileSize));
        for (IProgressListener listener : listeners)
        {
            try
            {
                listener.reportProgress(percentage, numberOfBytes);
            } catch (Throwable th)
            {
                th.printStackTrace();
            }
        }
    }

    protected void fireFinishedEvent(boolean successful)
    {
        for (IProgressListener listener : listeners)
        {
            try
            {
                listener.finished(successful, encounteredWarningMessages, encounteredExceptions);
            } catch (Throwable th)
            {
                th.printStackTrace();
            }
        }
        encounteredExceptions.clear();
        encounteredWarningMessages.clear();
    }

    protected void fireExceptionEvent(Throwable throwable)
    {
        encounteredExceptions.add(throwable);
    }

    protected void fireWarningEvent(String warningMessage)
    {
        encounteredWarningMessages.add(warningMessage);
    }

    /**
     * The same as {@link Thread#sleep(long)} but throws a {@link InterruptedExceptionUnchecked} on
     * interruption rather than a {@link InterruptedException}.
     */
    protected static void sleepAfterFailure() throws InterruptedExceptionUnchecked
    {
        try
        {
            Thread.sleep(WAIT_AFTER_FAILURE_MILLIS);
        } catch (InterruptedException ex)
        {
            throw new InterruptedExceptionUnchecked(ex);
        }
    }

}