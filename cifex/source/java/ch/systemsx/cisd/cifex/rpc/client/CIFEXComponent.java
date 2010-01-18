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

import java.lang.reflect.Method;

import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.common.TimingParameters;
import ch.systemsx.cisd.common.concurrent.ExecutionResult;
import ch.systemsx.cisd.common.concurrent.ExecutionStatus;
import ch.systemsx.cisd.common.concurrent.IMonitoringProxyLogger;
import ch.systemsx.cisd.common.concurrent.MonitoringProxy;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * The implementation of {@link ICIFEXComponent}.
 * 
 * @author Bernd Rinn
 */
public class CIFEXComponent implements ICIFEXComponent
{

    private static final int MAX_RETRIES = 600;

    private static final long WAIT_AFTER_FAILURE_MILLIS = 10 * 1000L;

    private static final long TIMEOUT_MILLIS = 3600 * 1000L; // FIXME: 20 * 1000L;

    private static final TimingParameters TIMING =
            TimingParameters.create(TIMEOUT_MILLIS, MAX_RETRIES, WAIT_AFTER_FAILURE_MILLIS);

    private final ICIFEXRPCService service;

    public CIFEXComponent(ICIFEXRPCService service)
    {
        this.service = service;
    }

    /**
     * An invocation logger for exceptional situations in an upload or download invocation.
     */
    private static class InvocationLogger implements IMonitoringProxyLogger
    {
        private final AbstractUploadDownload uploadDownload;

        public InvocationLogger(AbstractUploadDownload uploadDownload)
        {
            this.uploadDownload = uploadDownload;
        }

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
            if (result.getStatus() != ExecutionStatus.COMPLETE && willRetry == false)
            {
                uploadDownload.fireFinishedEvent(false);
            }
        }

        private void logException(boolean willRetry, Throwable originalException)
        {
            final Throwable th = unwrapException(originalException);
            if (th instanceof RemoteAccessException && willRetry)
            {
                String warningMessage;
                if (th.getMessage() != null)
                {
                    warningMessage =
                            "Remote operation failed: " + th.getClass().getSimpleName() + ": '"
                                    + th.getMessage() + "', will retry soon...";
                } else
                {
                    warningMessage =
                            "Remote operation failed: " + th.getClass().getSimpleName()
                                    + ", will retry soon...";
                }
                uploadDownload.fireWarningEvent(warningMessage);
            } else
            {
                uploadDownload.fireExceptionEvent(originalException);
            }
        }

        private Throwable unwrapException(Throwable oirginalException)
        {
            return (oirginalException instanceof Exception ? CheckedExceptionTunnel
                    .unwrapIfNecessary((Exception) oirginalException) : oirginalException);
        }

    }

    public ICIFEXDownloader createDownloader(final String sessionID)
    {
        final Downloader downloader = new Downloader(service, sessionID);
        final InvocationLogger logger = new InvocationLogger(downloader);
        final ICIFEXDownloader proxy =
                MonitoringProxy.create(ICIFEXDownloader.class, downloader).sensor(
                        downloader.getActivitySensor()).exceptionClassSuitableForRetrying(
                        RemoteAccessException.class).timing(TIMING).errorValueOnInterrupt()
                        .invocationLog(logger).get();
        return proxy;
    }

    public ICIFEXUploader createUploader(final String sessionID)
    {
        final Uploader uploader = new Uploader(service, sessionID);
        final InvocationLogger logger = new InvocationLogger(uploader);
        return MonitoringProxy.create(ICIFEXUploader.class, uploader).sensor(
                uploader.getActivitySensor()).exceptionClassSuitableForRetrying(
                RemoteAccessException.class).timing(TIMING).errorValueOnInterrupt().invocationLog(
                logger).get();
    }

    public void deleteFile(final String sessionID, final long fileId)
            throws InvalidSessionException, UserFailureException
    {
        service.deleteFile(sessionID, fileId);
    }

    public FileInfoDTO[] listDownloadFiles(final String sessionID) throws InvalidSessionException,
            EnvironmentFailureException
    {
        return service.listDownloadFiles(sessionID);
    }

    public FileInfoDTO[] listOwnedFiles(final String sessionID) throws InvalidSessionException,
            EnvironmentFailureException
    {
        return service.listOwnedFiles(sessionID);
    }

    public String login(final String user, final String password)
            throws AuthorizationFailureException
    {
        return service.login(user, password);
    }

    public void logout(final String sessionID)
    {
        service.logout(sessionID);
    }

    public void checkSession(final String sessionID) throws InvalidSessionException
    {
        service.checkSession(sessionID);
    }

}
