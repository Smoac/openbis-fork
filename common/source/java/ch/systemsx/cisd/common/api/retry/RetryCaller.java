/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.api.retry;

import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.RemoteConnectFailureException;

import ch.systemsx.cisd.common.api.retry.config.DefaultRetryConfiguration;
import ch.systemsx.cisd.common.api.retry.config.RetryConfiguration;

/**
 * @author pkupczyk
 */
public abstract class RetryCaller<T, E extends Throwable>
{

    private RetryConfiguration configuration;

    private int retryCounter;

    private int waitingTime;

    public RetryCaller()
    {
        this(DefaultRetryConfiguration.getInstance());
    }

    public RetryCaller(RetryConfiguration configuration)
    {
        if (configuration == null)
        {
            throw new IllegalArgumentException("Configuration was null");
        }
        if (configuration.getMaximumNumberOfRetries() < 0)
        {
            throw new IllegalArgumentException("MaximumNumberOfRetries must be >= 0");
        }
        if (configuration.getWaitingTimeBetweenRetries() <= 0)
        {
            throw new IllegalArgumentException("WaitingTimeBetweenRetries must be > 0");
        }
        if (configuration.getWaitingTimeBetweenRetriesIncreasingFactor() <= 0)
        {
            throw new IllegalArgumentException(
                    "WaitingTimeBetweenRetriesIncreasingFactor must be > 0");
        }

        this.configuration = configuration;
        this.waitingTime = configuration.getWaitingTimeBetweenRetries();
    }

    protected abstract T call() throws E;

    public T callWithRetry() throws E
    {
        while (true)
        {
            try
            {
                T result = call();
                return result;
            } catch (RuntimeException e)
            {
                if (isCommunicationException(e))
                {
                    if (shouldRetry())
                    {
                        System.err.println("Communication failed - will retry");
                        waitForRetry();
                    } else
                    {
                        System.err.println("Communication failed - will NOT retry");
                        throw e;
                    }
                } else
                {
                    throw e;
                }
            }
        }
    }

    private boolean isCommunicationException(RuntimeException e)
    {
        if (e instanceof RemoteConnectFailureException)
        {
            return true;
        }
        if (e instanceof RemoteAccessException)
        {
            return e.getCause() instanceof SocketTimeoutException
                    || e.getCause() instanceof SocketException;
        }
        return false;
    }

    private boolean shouldRetry()
    {
        return retryCounter < configuration.getMaximumNumberOfRetries();
    }

    private void waitForRetry()
    {
        try
        {
            Thread.sleep(waitingTime);
            waitingTime *= configuration.getWaitingTimeBetweenRetriesIncreasingFactor();
            retryCounter++;
        } catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

}
