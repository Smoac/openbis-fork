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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.StopException;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * Concurrency related utility methods.
 * 
 * @author Bernd Rinn
 */
public final class ConcurrencyUtilities
{

    /** Corresponds to no timeout at all when waiting for the future. */
    public static final long NO_TIMEOUT = -1L;

    /** Corresponds to an immediate timeout when waiting for the future. */
    public static final long IMMEDIATE_TIMEOUT = 0L;

    /**
     * Tries to get the result of a <var>future</var>, maximally waiting <var>timeoutMillis</var>
     * for the result to become available. Any {@link ExecutionException} that might occur in the
     * future task is unwrapped and re-thrown.
     * 
     * @param future The future representing the execution to wait for.
     * @param timeoutMillis The time-out (in milliseconds) to wait for the execution to finish. If
     *            it is smaller than 0, no time-out will apply.
     * @return The result of the future, or <code>null</code>, if the result does not become
     *         available within <var>timeoutMillis</var> ms.
     * @throws StopException If the thread got interrupted.
     */
    public static <T> T tryGetResult(Future<T> future, long timeoutMillis)
            throws StopException
    {
        return tryGetResult(future, timeoutMillis, null, null, true);
    }

    /**
     * Tries to get the result of a <var>future</var>, maximally waiting <var>timeoutMillis</var>
     * for the result to become available. Any {@link ExecutionException} that might occur in the
     * future task is unwrapped and re-thrown.
     * 
     * @param future The future representing the execution to wait for.
     * @param timeoutMillis The time-out (in milliseconds) to wait for the execution to finish. If
     *            it is smaller than 0, no time-out will apply.
     * @return The result of the future, or <code>null</code>, if the result does not become
     *         available within <var>timeoutMillis</var> ms.
     * @throws StopException If the thread got interrupted and <var>stopOnInterrupt</var>
     *             is <code>true</code>.
     */
    public static <T> T tryGetResult(Future<T> future, long timeoutMillis, boolean stopOnInterrupt)
            throws StopException
    {
        return tryGetResult(future, timeoutMillis, null, null, stopOnInterrupt);
    }

    /**
     * Tries to get the result of a <var>future</var>, maximally waiting <var>timeoutMillis</var>
     * for the result to become available. Any {@link ExecutionException} that might occur in the
     * future task is unwrapped and re-thrown (wrapped in a {@link CheckedExceptionTunnel} if
     * necessary.
     * 
     * @param future The future representing the execution to wait for.
     * @param timeoutMillis The time-out (in milliseconds) to wait for the execution to finish. If
     *            it is smaller than 0, no time-out will apply.
     * @param loggerOrNull The logger to use for logging note-worthy information, or
     *            <code>null</code>, if nothing should be logged.
     * @param operationNameOrNull The name of the operation performed, for log messages, or
     *            <code>null</code>, if it is not known or deemed unimportant.
     * @param stopOnInterrupt If <code>true</code>, throw a {@link StopException} if
     *            the thread gets interrupted while waiting on the future.
     * @return The result of the future, or <code>null</code>, if the result does not become
     *         available within <var>timeoutMillis</var> ms or if the waiting thread gets
     *         interrupted.
     * @throws StopException If the thread got interrupted and <var>stopOnInterrupt</var>
     *             is <code>true</code>.
     */
    public static <T> T tryGetResult(Future<T> future, long timeoutMillis,
            ISimpleLogger loggerOrNull, String operationNameOrNull, boolean stopOnInterrupt)
            throws StopException
    {
        final ExecutionResult<T> result =
                getResult(future, timeoutMillis, loggerOrNull, operationNameOrNull);
        switch (result.getStatus())
        {
            case COMPLETE:
            {
                return result.tryGetResult();
            }
            case EXCEPTION:
            {
                final Throwable cause = result.tryGetException();
                assert cause != null;
                if (cause instanceof Error)
                {
                    throw (Error) cause;
                } else
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary((Exception) cause);
                }
            }
            case INTERRUPTED:
            {
                if (stopOnInterrupt)
                {
                    throw new StopException();
                } else
                {
                    return null;
                }
            }
            default:
            {
                return null;
            }
        }
    }

    /**
     * Returns the result of a <var>future</var>, maximally waiting <var>timeoutMillis</var> for
     * the result to become available. The return value is never <code>null</code>, but always a
     * {@link ExecutionResult} that describes the outcome of the execution. The possible outcomes
     * are:
     * <ul>
     * <li> {@link ExecutionStatus#COMPLETE}: The execution has been performed correctly and a
     * result is available, if provided.</li>
     * <li> {@link ExecutionStatus#EXCEPTION}: The execution has been terminated by an exception.</li>
     * <li> {@link ExecutionStatus#TIMED_OUT}: The execution timed out.</li>
     * <li> {@link ExecutionStatus#INTERRUPTED}: The thread of the execution was interrupted (see
     * {@link Thread#interrupt()}).</li>
     * </ul>
     * 
     * @param future The future representing the execution to wait for.
     * @param timeoutMillis The time-out (in milliseconds) to wait for the execution to finish. If
     *            it is smaller than 0, no time-out will apply.
     * @return The {@link ExecutionResult} of the <var>future</var>. May correspond to each one of
     *         the {@link ExecutionStatus} values.
     */
    public static <T> ExecutionResult<T> getResult(Future<T> future, long timeoutMillis)
    {
        return getResult(future, timeoutMillis, null, null);
    }

    /**
     * Returns the result of a <var>future</var>, maximally waiting <var>timeoutMillis</var> for
     * the result to become available. The return value is never <code>null</code>, but always a
     * {@link ExecutionResult} that describes the outcome of the execution. The possible outcomes
     * are:
     * <ul>
     * <li> {@link ExecutionStatus#COMPLETE}: The execution has been performed correctly and a
     * result is available, if provided.</li>
     * <li> {@link ExecutionStatus#EXCEPTION}: The execution has been terminated by an exception.</li>
     * <li> {@link ExecutionStatus#TIMED_OUT}: The execution timed out.</li>
     * <li> {@link ExecutionStatus#INTERRUPTED}: The thread of the execution was interrupted (see
     * {@link Thread#interrupt()}).</li>
     * </ul>
     * 
     * @param future The future representing the execution to wait for.
     * @param timeoutMillis The time-out (in milliseconds) to wait for the execution to finish. If
     *            it is smaller than 0, no time-out will apply.
     * @param loggerOrNull The logger to use for logging note-worthy information, or
     *            <code>null</code>, if nothing should be logged.
     * @param operationNameOrNull The name of the operation performed, for log messages, or
     *            <code>null</code>, if it is not known or deemed unimportant.
     * @return The {@link ExecutionResult} of the <var>future</var>. May correspond to each one of
     *         the {@link ExecutionStatus} values.
     */
    public static <T> ExecutionResult<T> getResult(Future<T> future, long timeoutMillis,
            ISimpleLogger loggerOrNull, String operationNameOrNull)
    {
        return getResult(future, timeoutMillis, true, loggerOrNull, operationNameOrNull);
    }

    /**
     * Returns the result of a <var>future</var>, maximally waiting <var>timeoutMillis</var> for
     * the result to become available. The return value is never <code>null</code>, but always a
     * {@link ExecutionResult} that describes the outcome of the execution. The possible outcomes
     * are:
     * <ul>
     * <li> {@link ExecutionStatus#COMPLETE}: The execution has been performed correctly and a
     * result is available, if provided.</li>
     * <li> {@link ExecutionStatus#EXCEPTION}: The execution has been terminated by an exception.</li>
     * <li> {@link ExecutionStatus#TIMED_OUT}: The execution timed out.</li>
     * <li> {@link ExecutionStatus#INTERRUPTED}: The thread of the execution was interrupted (see
     * {@link Thread#interrupt()}).</li>
     * </ul>
     * 
     * @param future The future representing the execution to wait for.
     * @param timeoutMillis The time-out (in milliseconds) to wait for the execution to finish. If
     *            it is smaller than 0, no time-out will apply.
     * @param cancelOnTimeout If <code>true</code>, the <var>future</var> will be canceled on
     *            time-out.
     * @param loggerOrNull The logger to use for logging note-worthy information, or
     *            <code>null</code>, if nothing should be logged.
     * @param operationNameOrNull The name of the operation performed, for log messages, or
     *            <code>null</code>, if it is not known or deemed unimportant.
     * @return The {@link ExecutionResult} of the <var>future</var>. May correspond to each one of
     *         the {@link ExecutionStatus} values.
     */
    public static <T> ExecutionResult<T> getResult(Future<T> future, long timeoutMillis,
            boolean cancelOnTimeout, ISimpleLogger loggerOrNull, String operationNameOrNull)
    {
        final String operationName =
                (operationNameOrNull == null) ? "UNKNOWN" : operationNameOrNull;
        try
        {
            return ExecutionResult.create(future.get(transform(timeoutMillis),
                    TimeUnit.MILLISECONDS));
        } catch (TimeoutException ex)
        {
            if (cancelOnTimeout)
            {
                future.cancel(true);
            }
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.DEBUG, String.format(
                        "%s took longer than %f s, cancelled.", operationName,
                        timeoutMillis / 1000f));
            }
            return ExecutionResult.createTimedOut();
        } catch (InterruptedException ex)
        {
            future.cancel(true);
            if (loggerOrNull != null)
            {
                loggerOrNull.log(LogLevel.DEBUG, String
                        .format("%s got interrupted.", operationName));
            }
            return ExecutionResult.createInterrupted();
        } catch (ExecutionException ex)
        {
            final Throwable cause = ex.getCause();
            if (loggerOrNull != null)
            {
                final String message =
                        (cause == null || cause.getMessage() == null) ? "<no message>" : cause
                                .getMessage();
                final String className =
                        (cause == null) ? "<unknown class>" : cause.getClass().getSimpleName();
                loggerOrNull.log(LogLevel.ERROR, String.format(
                        "%s has caused an exception: %s [%s]", operationName, message, className));
            }
            return ExecutionResult.createExceptional(cause == null ? ex : cause);
        }
    }

    private static long transform(long timeoutMillis)
    {
        return (timeoutMillis < 0) ? Long.MAX_VALUE : timeoutMillis;
    }
}
