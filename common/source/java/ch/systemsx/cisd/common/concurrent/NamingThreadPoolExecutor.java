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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A {@link ThreadPoolExecutor} that allows to attach names to the threads it manages. These names
 * can come either from {@link NamedRunnable}s or {@link NamedCallable}s, or, if their standard
 * counterparts are submitted, a default name is used.
 * 
 * @author Bernd Rinn
 */
public class NamingThreadPoolExecutor extends ThreadPoolExecutor
{

    /**
     * The default time (in milli-seconds) to keep threads alive that are above the core pool size.
     */
    public final static long DEFAULT_KEEP_ALIVE_TIME_MILLIS = 10000L;

    /**
     * Creates a new <tt>NamingThreadPoolExecutor</tt> with the given initial parameters.
     * 
     * @param poolName the default name for new threads
     */
    public NamingThreadPoolExecutor(String poolName)
    {
        super(1, Integer.MAX_VALUE, DEFAULT_KEEP_ALIVE_TIME_MILLIS, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new NamingThreadFactory(poolName));
    }

    /**
     * Creates a new <tt>NamingThreadPoolExecutor</tt> with the given initial parameters.
     * 
     * @param poolName the default name for new threads
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle.
     * @throws IllegalArgumentException if corePoolSize less than zero.
     */
    public NamingThreadPoolExecutor(String poolName, int corePoolSize)
    {
        super(corePoolSize, Integer.MAX_VALUE, DEFAULT_KEEP_ALIVE_TIME_MILLIS, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new NamingThreadFactory(poolName));
    }

    /**
     * Creates a new <tt>NamingThreadPoolExecutor</tt> with the given initial parameters.
     * 
     * @param poolName the default name for new threads
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the pool.
     * @param keepAliveTimeMillis when the number of threads is greater than the core, this is the
     *            maximum time in milliseconds that excess idle threads will wait for new tasks
     *            before terminating.
     * @throws IllegalArgumentException if corePoolSize, or keepAliveTime less than zero, or if
     *             maximumPoolSize less than or equal to zero, or if corePoolSize greater than
     *             maximumPoolSize.
     */
    public NamingThreadPoolExecutor(String poolName, int corePoolSize, int maximumPoolSize,
            long keepAliveTimeMillis)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTimeMillis, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new NamingThreadFactory(poolName));
    }

    /**
     * Creates a new <tt>NamingThreadPoolExecutor</tt> with the given initial parameters.
     * 
     * @param poolName the default name for new threads
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the pool.
     * @throws IllegalArgumentException if corePoolSize less than zero, or if maximumPoolSize less
     *             than or equal to zero, or if corePoolSize greater than maximumPoolSize.
     */
    public NamingThreadPoolExecutor(String poolName, int corePoolSize, int maximumPoolSize)
    {
        super(corePoolSize, maximumPoolSize, DEFAULT_KEEP_ALIVE_TIME_MILLIS, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new NamingThreadFactory(poolName));
    }

    /**
     * Creates a new <tt>NamingThreadPoolExecutor</tt> with the given initial parameters.
     * 
     * @param poolName the default name for new threads
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the pool.
     * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
     *            time that excess idle threads will wait for new tasks before terminating.
     * @param unit the time unit for the keepAliveTime argument.
     * @param workQueue the queue to use for holding tasks before they are executed. This queue will
     *            hold only the <tt>Runnable</tt> tasks submitted by the <tt>execute</tt>
     *            method.
     * @param handler the handler to use when execution is blocked because the thread bounds and
     *            queue capacities are reached.
     * @throws IllegalArgumentException if corePoolSize, or keepAliveTime less than zero, or if
     *             maximumPoolSize less than or equal to zero, or if corePoolSize greater than
     *             maximumPoolSize.
     * @throws NullPointerException if <tt>workQueue</tt> or <tt>threadFactory</tt> or
     *             <tt>handler</tt> are null.
     */
    public NamingThreadPoolExecutor(String poolName, int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
            RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                new NamingThreadFactory(poolName), handler);
    }

    /**
     * Creates a new <tt>NamingThreadPoolExecutor</tt> with the given initial parameters.
     * 
     * @param poolName the default name for new threads
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the pool.
     * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
     *            time that excess idle threads will wait for new tasks before terminating.
     * @param unit the time unit for the keepAliveTime argument.
     * @param workQueue the queue to use for holding tasks before they are executed. This queue will
     *            hold only the <tt>Runnable</tt> tasks submitted by the <tt>execute</tt>
     *            method.
     * @throws IllegalArgumentException if corePoolSize, or keepAliveTime less than zero, or if
     *             maximumPoolSize less than or equal to zero, or if corePoolSize greater than
     *             maximumPoolSize.
     * @throws NullPointerException if <tt>workQueue</tt> or <tt>threadFactory</tt> are null.
     */
    public NamingThreadPoolExecutor(String poolName, int corePoolSize, int maximumPoolSize,
            long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                new NamingThreadFactory(poolName));
    }

    /**
     * Creates a new <tt>NamingThreadPoolExecutor</tt> with the given initial parameters.
     * 
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the pool.
     * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
     *            time that excess idle threads will wait for new tasks before terminating.
     * @param unit the time unit for the keepAliveTime argument.
     * @param workQueue the queue to use for holding tasks before they are executed. This queue will
     *            hold only the <tt>Runnable</tt> tasks submitted by the <tt>execute</tt>
     *            method.
     * @param threadFactory the factory to use when the executor creates a new thread.
     * @param handler the handler to use when execution is blocked because the thread bounds and
     *            queue capacities are reached.
     * @throws IllegalArgumentException if corePoolSize, or keepAliveTime less than zero, or if
     *             maximumPoolSize less than or equal to zero, or if corePoolSize greater than
     *             maximumPoolSize.
     * @throws NullPointerException if <tt>workQueue</tt> or <tt>threadFactory</tt> or
     *             <tt>handler</tt> are null.
     */
    public NamingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
            TimeUnit unit, BlockingQueue<Runnable> workQueue, NamingThreadFactory threadFactory,
            RejectedExecutionHandler handler)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    /**
     * Creates a new <tt>NamingThreadPoolExecutor</tt> with the given initial parameters.
     * 
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the pool.
     * @param keepAliveTime when the number of threads is greater than the core, this is the maximum
     *            time that excess idle threads will wait for new tasks before terminating.
     * @param unit the time unit for the keepAliveTime argument.
     * @param workQueue the queue to use for holding tasks before they are executed. This queue will
     *            hold only the <tt>Runnable</tt> tasks submitted by the <tt>execute</tt>
     *            method.
     * @param threadFactory the factory to use when the executor creates a new thread.
     * @throws IllegalArgumentException if corePoolSize, or keepAliveTime less than zero, or if
     *             maximumPoolSize less than or equal to zero, or if corePoolSize greater than
     *             maximumPoolSize.
     * @throws NullPointerException if <tt>workQueue</tt> or <tt>threadFactory</tt> are null.
     */
    public NamingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
            TimeUnit unit, BlockingQueue<Runnable> workQueue, NamingThreadFactory threadFactory)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r)
    {
        if (r instanceof NamedRunnable == false)
        {
            return;
        }
        final String runnableName = ((NamedRunnable) r).getRunnableName();
        if (t instanceof PoolNameThread)
        {
            ((PoolNameThread) t).setRunnableName(runnableName);
        } else
        {
            t.setName(runnableName);
        }
        super.beforeExecute(t, r);
    }

    @Override
    public Future<?> submit(Runnable task)
    {
        if (task == null)
        {
            throw new NullPointerException();
        }

        final FutureTask<Object> ftask;
        if (task instanceof NamedRunnable)
        {
            ftask = new NamedFutureTask<Object>((NamedRunnable) task, null);
        } else
        {
            ftask = new FutureTask<Object>(task, null);
        }
        execute(ftask);
        return ftask;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result)
    {
        if (task == null)
        {
            throw new NullPointerException();
        }

        final FutureTask<T> ftask;
        if (task instanceof NamedRunnable)
        {
            ftask = new NamedFutureTask<T>((NamedRunnable) task, result);
        } else
        {
            ftask = new FutureTask<T>(task, result);
        }
        execute(ftask);
        return ftask;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task)
    {
        if (task == null)
        {
            throw new NullPointerException();
        }
        final FutureTask<T> ftask;
        if (task instanceof NamedCallable)
        {
            ftask = new NamedFutureTask<T>((NamedCallable<T>) task);
        } else
        {
            ftask = new FutureTask<T>(task);
        }
        execute(ftask);
        return ftask;
    }

}
