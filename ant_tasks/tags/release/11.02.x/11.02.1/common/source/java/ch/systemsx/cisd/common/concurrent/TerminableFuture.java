/*
 * Copyright 2008 ETH Zuerich, CISD
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

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;

/**
 * Implementation of a {@link ITerminableFuture} that delegates to appropriate classes.
 * 
 * @author Bernd Rinn
 */
final class TerminableFuture<V> implements ITerminableFuture<V>
{
    private final Future<V> delegateFuture;

    private final TerminableCallable<V> delegateTerminableCallable;

    public TerminableFuture(Future<V> delegateFuture,
            TerminableCallable<V> delegateTerminableCallable)
    {
        this.delegateFuture = delegateFuture;
        this.delegateTerminableCallable = delegateTerminableCallable;
    }

    public boolean cancel(boolean mayInterruptIfRunning)
    {
        final boolean canceled = delegateTerminableCallable.cancel(mayInterruptIfRunning);
        delegateFuture.cancel(false);
        return canceled;
    }

    public V get() throws InterruptedException, ExecutionException
    {
        return delegateFuture.get();
    }

    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
            TimeoutException
    {
        return delegateFuture.get(timeout, unit);
    }

    public boolean isCancelled()
    {
        return delegateTerminableCallable.isCancelled();
    }

    public boolean isDone()
    {
        return delegateFuture.isDone();
    }

    public boolean isRunning()
    {
        return delegateTerminableCallable.isRunning();
    }

    public boolean hasStarted()
    {
        return delegateTerminableCallable.hasStarted();
    }

    public boolean waitForFinished(long timeoutMillis) throws InterruptedExceptionUnchecked
    {
        return delegateTerminableCallable.waitForFinished(timeoutMillis);
    }

    public boolean hasFinished()
    {
        return delegateTerminableCallable.hasFinished();
    }

    public boolean terminate()
    {
        cancel(false);
        return delegateTerminableCallable.terminate();
    }

    public final boolean terminate(long timeoutMillis) throws InterruptedExceptionUnchecked
    {
        cancel(false);
        return delegateTerminableCallable.terminate(timeoutMillis);
    }

}
