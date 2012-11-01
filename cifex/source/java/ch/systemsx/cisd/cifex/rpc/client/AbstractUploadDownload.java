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
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.client.gui.IProgressListener;
import ch.systemsx.cisd.common.concurrent.ExecutionResult;
import ch.systemsx.cisd.common.concurrent.ExecutionStatus;
import ch.systemsx.cisd.common.concurrent.IMonitoringProxyLogger;
import ch.systemsx.cisd.common.concurrent.MonitoringProxy;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.MasqueradingException;
import ch.systemsx.cisd.common.time.TimingParameters;

/**
 * A common subclass for file upload and download.
 * 
 * @author Bernd Rinn
 */
public abstract class AbstractUploadDownload implements ICIFEXOperation
{
    protected static final int PROGRESS_REPORT_BLOCK_SIZE = 128 * 1024; // 128kB

    protected static final int PROGRESS_UPDATE_MIN_INTERVAL_MILLIS = 1 * 1000; // 1 second

    private static final long TIMEOUT_MILLIS = 20 * 1000L;

    protected static final int MAX_RETRIES = 600;

    private static final long WAIT_AFTER_FAILURE_MILLIS = 10 * 1000L;

    protected static final TimingParameters TIMING =
            TimingParameters.create(TIMEOUT_MILLIS, MAX_RETRIES, WAIT_AFTER_FAILURE_MILLIS);

    protected final ICIFEXRPCService service;

    protected final String sessionID;

    protected final Set<IProgressListener> listeners = new LinkedHashSet<IProgressListener>();

    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    protected final AtomicBoolean inProgress = new AtomicBoolean(false);

    protected final boolean reportFinalException;

    abstract protected MonitoringProxy<?> getProxyForOperation();

    /**
     * Creates an instance for the specified service and session ID.
     */
    public AbstractUploadDownload(ICIFEXRPCService service, String sessionID)
    {
        this(service, sessionID, true);
    }

    /**
     * Creates an instance for the specified service and session ID.
     */
    public AbstractUploadDownload(ICIFEXRPCService service, String sessionID,
            boolean reportFinalException)
    {
        this.service = service;
        this.sessionID = sessionID;
        this.reportFinalException = reportFinalException;
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
    @Override
    public boolean isInProgress()
    {
        return inProgress.get();
    }

    /**
     * Cancels the operation (upload or download).
     */
    @Override
    public void cancel()
    {
        getProxyForOperation().cancelCurrentOperations();
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

    protected void fireStartedEvent(File file, String operationName, long fileSize,
            Long fileIdOrNull)
    {
        for (IProgressListener listener : listeners)
        {
            try
            {
                listener.start(file, operationName, fileSize, fileIdOrNull);
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
                listener.finished(successful);
            } catch (Throwable th)
            {
                th.printStackTrace();
            }
        }
    }

    protected void fireExceptionEvent(Throwable throwable)
    {
        for (IProgressListener listener : listeners)
        {
            try
            {
                listener.exceptionOccured(throwable);
            } catch (Throwable th)
            {
                th.printStackTrace();
            }
        }
    }

    protected void fireWarningEvent(String warningMessage)
    {
        for (IProgressListener listener : listeners)
        {
            try
            {
                listener.warningOccured(warningMessage);
            } catch (Throwable th)
            {
                th.printStackTrace();
            }
        }
    }

    /**
     * An invocation logger for exceptional situations in an upload or download invocation.
     */
    static class InvocationLogger implements IMonitoringProxyLogger
    {
        private final AbstractUploadDownload uploadDownload;

        private final boolean reportFinalException;

        public InvocationLogger(AbstractUploadDownload uploadDownload, boolean reportFinalException)
        {
            this.uploadDownload = uploadDownload;
            this.reportFinalException = reportFinalException;
        }

        @Override
        public void log(Method method, ExecutionResult<Object> result, boolean willRetry)
        {
            // We log only exceptional invocations here, the regular progress logging is done in the
            // uploader / downloader itself.
            if (result.getStatus() == ExecutionStatus.EXCEPTION)
            {
                Throwable originalException = result.tryGetException();
                logException(willRetry, originalException);
            }
            if (result.getStatus() == ExecutionStatus.TIMED_OUT)
            {
                uploadDownload.fireWarningEvent("Remote operation timed out"
                        + (willRetry ? ", will retry soon." : "."));
            }
        }

        private void logException(boolean willRetry, Throwable originalException)
        {
            if (willRetry)
            {
                String warningMessage = getMessageForThrowable(unwrapException(originalException));
                uploadDownload.fireWarningEvent(warningMessage);
            } else
            {
                if (reportFinalException)
                {
                    uploadDownload.fireExceptionEvent(originalException);
                }
            }
        }

        private String getMessageForThrowable(final Throwable th)
        {
            if (th instanceof MasqueradingException)
            {
                return th.toString();
            } else if (th.getMessage() != null)
            {
                return "Remote operation failed: " + th.getClass().getSimpleName() + ": '"
                        + th.getMessage() + "', will retry soon...";
            } else
            {
                return "Remote operation failed: " + th.getClass().getSimpleName()
                        + ", will retry soon...";
            }
        }

        private Throwable unwrapException(Throwable oirginalException)
        {
            return (oirginalException instanceof Exception ? CheckedExceptionTunnel
                    .unwrapIfNecessary((Exception) oirginalException) : oirginalException);
        }

    }

}