/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.ISimpleLogger.Level;

/**
 * Concurrency related utility methods.
 * 
 * @author Bernd Rinn
 */
public final class ConcurrencyUtilities
{

    /**
     * Creates an {@link ExecutorService} where threads have a name starting with <var>name</var>.
     * 
     * @param name The name prefix of new threads started by this pool.
     * @param corePoolSize The number of threads that should be kept running even if less theads are needed.
     * @param maximumPoolSize The number of threads that this executor service is maximally allowed to spawn.
     */
    public static ExecutorService newNamedPool(final String name, int corePoolSize, int maximumPoolSize)
    {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadFactory()
                    {
                        private int executorThreadCount = 1;

                        public Thread newThread(Runnable r)
                        {
                            return new Thread(r, name + " " + executorThreadCount);
                        }
                    });
    }

    /**
     * Tries to get the result of a <var>future</var>, maximally waiting <var>timeoutMillis</var> for the result to
     * become available. Any {@link ExecutionException} that might occur in the future task is unwrapped and re-thrown.
     * 
     * @return The result of the future, or <code>null</code>, if the result does not become available within
     *         <var>timeoutMillis</var> ms.
     * @throws CheckedExceptionTunnel of an {@link InterruptedException} if the current thread gets interrupted during
     *             waiting for the result.
     */
    public static <T> T tryGetResult(Future<T> future, long timeoutMillis)
    {
        return tryGetResult(future, timeoutMillis, null, null);
    }

    /**
     * Tries to get the result of a <var>future</var>, maximally waiting <var>timeoutMillis</var> for the result to
     * become available. Any {@link ExecutionException} that might occur in the future task is unwrapped and re-thrown.
     * 
     * @return The result of the future, or <code>null</code>, if the result does not become available within
     *         <var>timeoutMillis</var> ms or if the waiting thread gets interrupted.
     */
    public static <T> T tryGetResult(Future<T> future, long timeoutMillis, ISimpleLogger loggerOrNull,
            String operationNameOrNull)
    {
        try
        {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex)
        {
            future.cancel(true);
            if (loggerOrNull != null)
            {
                loggerOrNull
                        .log(Level.DEBUG, String.format("%s took longer than %f s, cancelled.",
                                operationNameOrNull == null ? "UNKNOWN OPERATION" : operationNameOrNull,
                                timeoutMillis / 1000f));
            }
            return null;
        } catch (InterruptedException ex)
        {
            future.cancel(true);
            if (loggerOrNull != null)
            {
                loggerOrNull.log(Level.DEBUG, String.format("%s got interrupted.",
                        operationNameOrNull == null ? "UNKNOWN OPERATION" : operationNameOrNull));
            }
            return null;
        } catch (ExecutionException ex)
        {
            final Throwable cause = ex.getCause();
            if (loggerOrNull != null)
            {
                loggerOrNull.log(Level.ERROR, String.format("%s has caused an exception: %s",
                        operationNameOrNull == null ? "UNKNOWN OPERATION" : operationNameOrNull, cause.getClass()
                                .getSimpleName(), cause.getMessage() != null ? cause.getMessage() : "<no message>"));
            }
            if (cause instanceof Error)
            {
                throw (Error) cause;
            } else
            {
                throw CheckedExceptionTunnel.wrapIfNecessary((Exception) cause);
            }
        }
    }
}
