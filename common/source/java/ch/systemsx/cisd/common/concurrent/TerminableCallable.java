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

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.HardStopException;
import ch.systemsx.cisd.common.exceptions.StopException;
import ch.systemsx.cisd.common.utilities.ITerminable;

/**
 * A wrapper for {@link Callable}s that offers a {@link #terminate()} method to safely terminated
 * the callable and providing an optional hook for clean up after the callable is either finished or
 * terminated. It delegates it's {@link Callable#call()} to a delegate.
 * <p>
 * The {@link Callable#call()} method can only be called once. Do not try to re-use an instance of
 * this class, but create a new one instead!
 * <p>
 * All code in the delegate {@link #call()} that cannot be interrupted but safely be stopped (see
 * {@link Thread#interrupt()} and <code>Thread.stop()</code> for details) should be executed in
 * the <var>stoppableExecutor</var>.
 * <p>
 * <strong>Note: Code executed in the <var>stoppableExecutor</var> must <i>not</i> change
 * variables or data structures used by several threads or else the problems described in <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html">"Why is
 * <code>Thread.stop()</code> deprecated?"</a> apply to your code! Watch out for static
 * thread-safe variables like e.g. the ones of type {@link ThreadLocal}!</strong>
 * <p>
 * The <var>stoppableExecutor</var> is in general supposed to be used for time-consuming
 * algorithmic code which cannot be interrupted (because it doesn't check the thread's interrupt
 * state). The simplest case is one big algorithmic block which uses final variables of the caller
 * as input and delivers its result as return value:
 * 
 * <pre>
 * public Double call(IStoppableExecutor&lt;Double&gt; stoppableExecutor)
 * {
 *    final double a = ...;
 *    final double b = ...;
 *    return stoppableExecutor.execute(new Callable&lt;Double&gt;()
 *      {
 *        double d = 0.0;
 *        // Now do some heavy time-consuming number crunching on a and b that cannot be interrupted 
 *        // but stopped.
 *        return d;
 *      });
 * }
 * </pre>
 * 
 * Here is a slightly more complex example:
 * 
 * <pre>
 * public final BlockingQueue&lt;double[]&gt; queue = new LinkedBlockingQueue&lt;double[]&gt;();
 * public final Lock resultLock = new ReentrantLock();
 * ...
 * public Object call(IStoppableExecutor&lt;Object&gt; stoppableExecutor)
 * {
 *    do
 *    {
 *      final double[] a = queue.take();
 *      final double r = stoppableExecutor.execute(new Callable&lt;Double&gt;()
 *      {
 *        double d = 0.0;
 *        // Now do some heavy time-consuming number crunching on a that cannot be interrupted but 
 *        // stopped.
 *        return d;
 *      });
 *      resultLock.lock();
 *      try
 *      {
 *        // Update data structures that are shared between threads with the new result.
 *      } finally
 *      {
 *        resultLock.unlock();
 *      }
 *    } while (true);
 * }
 * </pre>
 * 
 * @author Bernd Rinn
 */
public final class TerminableCallable<V> implements Callable<V>, ITerminable

{
    /** A constant indicating not to wait and to return immediately. */
    public static final long NO_WAIT_MILLIS = 0L;

    /** A constant indicating to wait indefinitely. */
    public static final long WAIT_FOREVER_MILLIS = Long.MAX_VALUE;

    /**
     * The default time (in milli-seconds) to wait for {@link Thread#interrupt()} to terminate the
     * callable.
     */
    public static final long DEFAULT_WAIT_INTERRUPT_MILLIS = 100L;

    private final ICallable<V> delegate;

    private final ICleaner cleanerOrNull;

    /** The thread to interrupt or stop. */
    private volatile Thread thread;

    /** This latch signals that the callable has actually started. */
    private final CountDownLatch started = new CountDownLatch(1);

    /** This latch signals that the callable has finished. */
    private final CountDownLatch finished = new CountDownLatch(1);

    /** This latch signals that the callable has finished clean up after an interrupt or stop. */
    private final CountDownLatch cleanedUp = new CountDownLatch(1);

    /** The lock that guards stopping the thread. */
    private final Lock stopLock = new ReentrantLock();

    /** The time (in milli-seconds) to wait for {@link Thread#interrupt()} to work. */
    private final long waitInterruptMillis;

    /** The time (in milli-seconds) to wait for {@link #terminate()} to finish up. */
    private final long timeoutTerminateMillis;

    /** Indicator for why the callable finished. */
    public enum FinishCause
    {
        /** The callable completed normally. */
        COMPLETED,

        /** The thread of the callable got interrupted. */
        INTERRUPTED,

        /** The thread of the callable got stopped. */
        STOPPED,

        /** The callable was terminated with an exception. */
        EXCEPTION,
    }

    /**
     * A role that executes {@link Runnable}s and {@link Callable}s immediately in the current
     * thread and that marks the code it runs as suitable for <code>Thread.stop()</code>.
     */
    public interface IStoppableExecutor<V>
    {
        public void execute(Runnable runnable) throws Exception;

        public V execute(Callable<V> callable) throws Exception;
    }

    /**
     * A {@link Callable} that has available a {@link IStoppableExecutor} for running code that
     * cannot be interrupted but stopped.
     */
    public interface ICallable<V>
    {
        public V call(IStoppableExecutor<V> stoppableExecutor) throws Exception;
    }

    /**
     * A role that can perform a clean-up.
     */
    public interface ICleaner
    {
        /**
         * The method that is called whenever the
         * {@link ch.systemsx.cisd.common.concurrent.TerminableCallable.ICallable#call(
         * ch.systemsx.cisd.common.concurrent.TerminableCallable.IStoppableExecutor)} method
         * finishes or terminates.
         * <p>
         * Note that this method is <i>always</i> called, no matter what the cause is. If you want
         * to perform clean up only for some causes, check <var>cause</var> first.
         * <p>
         * <strong>Don't perform any time consuming operations in this method and avoid any
         * operations that can fail with an exception.</strong>
         * 
         * @param cause The cause why the
         *            {@link ch.systemsx.cisd.common.concurrent.TerminableCallable.ICallable#call(
         *            ch.systemsx.cisd.common.concurrent.TerminableCallable.IStoppableExecutor)}
         *            method finished.
         */
        public void cleanUp(FinishCause cause);
    }

    /**
     * A roles that implements both the {@link ICallable} and {@link ICleaner}.
     */
    public interface ICallableCleaner<V> extends ICallable<V>, ICleaner
    {
    }

    /**
     * Factory method that creates a {@link TerminableCallable} with a <var>delegate</var>. It sets
     * a time of {@link #DEFAULT_WAIT_INTERRUPT_MILLIS} to wait for {@link Thread#interrupt()} to
     * terminate the callable.
     * <p>
     * Convenience wrapper for
     * {@link #create(ch.systemsx.cisd.common.concurrent.TerminableCallable.ICallable, 
     * ch.systemsx.cisd.common.concurrent.TerminableCallable.ICleaner, long, long)} with
     * <var>waitInterruptMillis</var> set to {@link #DEFAULT_WAIT_INTERRUPT_MILLIS} and
     * <var>timeoutTerminateMillis</var> set to {@link #WAIT_FOREVER_MILLIS}.
     */
    public static <V> TerminableCallable<V> create(ICallable<V> delegate)
    {
        return create(delegate, null, DEFAULT_WAIT_INTERRUPT_MILLIS, WAIT_FOREVER_MILLIS);
    }

    /**
     * Factory method that creates a {@link TerminableCallable} with a <var>delegateWithCleaner</var>
     * which is also used to clean up after the call. It sets a time of
     * {@link #DEFAULT_WAIT_INTERRUPT_MILLIS} to wait for {@link Thread#interrupt()} to terminate
     * the callable.
     * <p>
     * Convenience wrapper for
     * {@link #create(ch.systemsx.cisd.common.concurrent.TerminableCallable.ICallable, 
     * ch.systemsx.cisd.common.concurrent.TerminableCallable.ICleaner, long, long)} with
     * <var>waitInterruptMillis</var> set to {@link #DEFAULT_WAIT_INTERRUPT_MILLIS} and
     * <var>timeoutTerminateMillis</var> set to {@link #WAIT_FOREVER_MILLIS}.
     */
    public static <V> TerminableCallable<V> create(ICallableCleaner<V> delegateWithCleaner)
    {
        return create(delegateWithCleaner, delegateWithCleaner, DEFAULT_WAIT_INTERRUPT_MILLIS,
                WAIT_FOREVER_MILLIS);
    }

    /**
     * Factory method that creates a {@link TerminableCallable} with a <var>delegate</var> and a
     * separate <var>cleaner</var>.
     * <p>
     * Convenience wrapper for
     * {@link #create(ch.systemsx.cisd.common.concurrent.TerminableCallable.ICallable, 
     * ch.systemsx.cisd.common.concurrent.TerminableCallable.ICleaner, long, long)} with
     * <var>waitInterruptMillis</var> set to {@link #DEFAULT_WAIT_INTERRUPT_MILLIS} and
     * <var>timeoutTerminateMillis</var> set to {@link #WAIT_FOREVER_MILLIS}.
     */
    public static <V> TerminableCallable<V> create(ICallable<V> delegate, ICleaner cleaner)
    {
        return create(delegate, cleaner, DEFAULT_WAIT_INTERRUPT_MILLIS, WAIT_FOREVER_MILLIS);
    }

    /**
     * Factory method that creates a {@link TerminableCallable} with a <var>delegate</var> and a
     * separate <var>cleanerOrNull</var>.
     * 
     * @param waitInterruptMillis The time (in milli-seconds) that {@link #terminate(long)} should
     *            wait after calling the {@link Thread#interrupt()} method for the callable to
     *            terminate. After that time, it will try to stop the thread.
     * @param timeoutTerminateMillis The time (in milli-seconds) that {@link #terminate()} should
     *            wait for the callable to finish up, including time required for clean up.
     */
    public static <V> TerminableCallable<V> create(ICallable<V> delegate, ICleaner cleanerOrNull,
            long waitInterruptMillis, long timeoutTerminateMillis)
    {
        return new TerminableCallable<V>(delegate, cleanerOrNull, waitInterruptMillis,
                timeoutTerminateMillis);
    }

    /**
     * Factory method that creates a {@link TerminableCallable} with a <var>delegate</var> that
     * only runs code which is safe to stop.
     * <p>
     * <strong>Note: Code executed in the <var>delegate</var> must <i>not</i> change variables or
     * data structures used by several threads or else the problems described in <a
     * href="http://java.sun.com/j2se/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html">"Why is
     * <code>Thread.stop()</code> deprecated?"</a> apply to your code! Watch out for static
     * thread-safe variables like e.g. the ones of type {@link ThreadLocal}!</strong>
     * <p>
     * Convenience wrapper for
     * {@link #create(ch.systemsx.cisd.common.concurrent.TerminableCallable.ICallable, 
     * ch.systemsx.cisd.common.concurrent.TerminableCallable.ICleaner, long, long)} with
     * <var>waitInterruptMillis</var> set to 0 and <var>timeoutTerminateMillis</var> set to
     * {@link #WAIT_FOREVER_MILLIS}.
     */
    public static <V> TerminableCallable<V> createStoppable(final Callable<V> delegate)
    {
        return new TerminableCallable<V>(new ICallable<V>()
            {
                public V call(IStoppableExecutor<V> stoppableExecutor) throws Exception
                {
                    return stoppableExecutor.execute(delegate);
                }
            }, null, 0, WAIT_FOREVER_MILLIS);
    }

    /**
     * Constructs a {@link TerminableCallable}.
     * 
     * @param delegate The callable to delegate the call to.
     * @param cleanerOrNull The {@link ICleaner} to use for cleaning up after the callable is
     *            finished.
     * @param waitInterruptMillis The time to wait for {@link Thread#interrupt()} to finish the
     *            callable.
     * @param timeoutTerminateMillis The time (in milli-seconds) to wait for {@link #terminate()} to
     *            finish up.
     */
    private TerminableCallable(ICallable<V> delegate, ICleaner cleanerOrNull,
            long waitInterruptMillis, long timeoutTerminateMillis)
    {
        assert delegate != null;

        this.delegate = delegate;
        this.cleanerOrNull = cleanerOrNull;
        this.waitInterruptMillis = waitInterruptMillis;
        this.timeoutTerminateMillis = timeoutTerminateMillis;
    }

    /** Return <code>true</code>, if the <var>latch</var> becomes 0 in due time. */
    private static boolean wait(CountDownLatch latch, long timeoutMillis) throws StopException
    {
        try
        {
            return latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex)
        {
            throw new StopException(ex);
        }
    }

    private boolean stop(Thread t, long timeoutMillis) throws StopException
    {
        final boolean gotIt;
        try
        {
            gotIt = stopLock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex)
        {
            throw new StopException(ex);
        }
        if (gotIt == false)
        {
            return false;
        }
        try
        {
            stopNow(t);
            return true;
        } finally
        {
            stopLock.unlock();
        }
    }

    @SuppressWarnings("deprecation")
    private void stopNow(Thread t)
    {
        t.stop(new HardStopException());
    }

    private void cleanUp()
    {
        cleanUp(null);
    }

    private void cleanUp(Throwable throwableOrNull)
    {
        if (cleanerOrNull != null)
        {
            final FinishCause cause;
            if (throwableOrNull == null)
            {
                cause = FinishCause.COMPLETED;
            } else if (throwableOrNull instanceof HardStopException)
            {
                cause = FinishCause.STOPPED;
            } else if (throwableOrNull instanceof StopException
                    || throwableOrNull instanceof InterruptedException)
            {
                cause = FinishCause.INTERRUPTED;
            } else
            {
                cause = FinishCause.EXCEPTION;
            }
            cleanerOrNull.cleanUp(cause);
        }
    }

    private void markStarted()
    {
        thread = Thread.currentThread();
        started.countDown();
    }

    private void markFinished()
    {
        thread = null;
        finished.countDown();
    }

    private void markCleanedUp()
    {
        cleanedUp.countDown();
    }

    private InterruptedException getOrCreateInterruptedException(StopException stopEx)
    {
        final InterruptedException causeOrNull = (InterruptedException) stopEx.getCause();
        return (causeOrNull != null) ? causeOrNull : new InterruptedException();
    }

    public V call() throws InterruptedException
    {
        stopLock.lock();
        try
        {
            final V result;
            markStarted();
            try
            {
                result = delegate.call(new IStoppableExecutor<V>()
                    {
                        public V execute(Callable<V> callable) throws Exception
                        {
                            stopLock.unlock();
                            try
                            {
                                return callable.call();
                            } finally
                            {
                                stopLock.lock();
                            }
                        }

                        public void execute(Runnable runnable) throws Exception
                        {
                            stopLock.unlock();
                            try
                            {
                                runnable.run();
                            } finally
                            {
                                stopLock.lock();
                            }
                        }
                    });
            } catch (Throwable th)
            {
                markFinished();
                cleanUp(th);
                if (th instanceof StopException)
                {
                    throw getOrCreateInterruptedException((StopException) th);
                } else
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(th);
                }
            }
            markFinished();
            cleanUp();
            return result;
        } finally
        {
            markCleanedUp();
        }
    }

    /**
     * Returns this {@link Callable} as a {@link Runnable}.
     */
    public Runnable asRunnable()
    {
        return new Runnable()
            {
                public void run()
                {
                    try
                    {
                        call();
                    } catch (InterruptedException ex)
                    {
                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                    }
                }
            };
    }

    /**
     * Returns <code>true</code>, if the callable has already started running.
     */
    public boolean hasStarted()
    {
        try
        {
            return started.await(0L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex)
        {
            return false; // Shouldn't happen.
        }
    }

    /**
     * Returns <code>true</code>, if the callable has already finished running.
     */
    public boolean hasFinished()
    {
        try
        {
            return finished.await(0L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex)
        {
            return false; // Shouldn't happen.
        }
    }

    /**
     * Returns <code>true</code>, if the callable has already called the
     * {@link ICleaner#cleanUp(ch.systemsx.cisd.common.concurrent.TerminableCallable.FinishCause)}
     * method, if any.
     */
    public boolean hasCleanedUp()
    {
        try
        {
            return cleanedUp.await(0L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex)
        {
            return false; // Shouldn't happen.
        }
    }

    /**
     * Returns <code>true</code>, if the callable is currently running and <code>false</code>
     * otherwise.
     */
    public boolean isRunning()
    {
        return (thread != null);
    }

    /**
     * Terminates this {@link TerminableCallable}. A convenience wrapper for
     * {@link #terminate(long)} with <var>timeoutMillis</var> set to <var>timeoutTerminateMillis</var>
     * as set in the factory method (see
     * {@link #create(ch.systemsx.cisd.common.concurrent.TerminableCallable.ICallable, 
     * ch.systemsx.cisd.common.concurrent.TerminableCallable.ICleaner, long, long)}.
     * 
     * @return <code>true</code>, if the callable is confirmed to be terminated and cleaned up
     *         successfully in due time, or <code>false</code>, if a timeout has occurred.
     * @throws StopException If the current thread is interrupted.
     */
    public boolean terminate() throws StopException
    {
        return terminate(timeoutTerminateMillis);
    }

    /**
     * Tries to terminate this {@link TerminableCallable}. Note that this is a synchronous call
     * that returns only when either the callable has been terminated (and cleaned up) or when a
     * timeout has occurred. Note also that even when providing <var>timeoutMillis</var> as 0, this
     * method may wait up to <var>timeoutInterruptMillis</var> milli-seconds for the
     * {@link Thread#interrupt()} call to terminate the callable.
     * <p>
     * The following steps are performed:
     * <ol>
     * <li> Wait for the callable to start up, if that has not happened. If it doesn't start up in
     * due time, return with <code>false</code>. </li>
     * <li> If the callable has already terminated, wait for the clean up to finish and return
     * <code>true</code>, if the clean up finishes in due time and <code>false</code>
     * otherwise. </li>
     * <li> Call <code>Thread.terminate()</code> on the thread that is running the callable. </li>
     * <li> Wait for the callable to terminate in response to the call above (wait for
     * <var>timeoutInterruptMillis</var> milli-seconds, as provided to the
     * {@link #create(ch.systemsx.cisd.common.concurrent.TerminableCallable.ICallable, 
     * ch.systemsx.cisd.common.concurrent.TerminableCallable.ICleaner, long, long)} method. </li>
     * <li> If the callable didn't terminate, try to call <code>Thread.stop()</code> on the thread
     * that is running the callable. This can only succeed, if the callable is currently running
     * code in the <var>stoppableExecutor</var>.</li>
     * <li> If either interrupt or stop took effect in due time, wait for clean up to finish. </li>
     * <li> If the clean up finishes in due time, return <code>true</code>, otherwise
     * <code>false</code>. </li>
     * </ol>
     * 
     * @param timeoutMillis If <code>== 0</code>, the method will wait for the callable to stop,
     *            if <code>&gt; 0</code>, the method will wait at most this time.
     * @return <code>true</code>, if the callable is confirmed to be terminated and cleaned up
     *         successfully in due time, or <code>false</code>, if a timeout has occurred.
     * @throws StopException If the current thread is interrupted.
     */
    public synchronized boolean terminate(long timeoutMillis) throws StopException
    {
        final long start = System.currentTimeMillis();
        if (wait(started, timeoutMillis) == false)
        {
            return false;
        }
        final Thread t = thread;
        if (t == null)
        {
            return wait(cleanedUp, timeoutMillis - (System.currentTimeMillis() - start));
        }
        t.interrupt();
        if (wait(finished, waitInterruptMillis) == false)
        {
            if (stop(t, timeoutMillis - (System.currentTimeMillis() - start)) == false)
            {
                return false;
            }
        }
        return wait(cleanedUp, timeoutMillis - (System.currentTimeMillis() - start));
    }

}
