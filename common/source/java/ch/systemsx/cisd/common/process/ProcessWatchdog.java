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

package ch.systemsx.cisd.common.process;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Destroys a process running for too long.
 * 
 * @author Christian Ribeaud
 */
public final class ProcessWatchdog implements Runnable
{
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private final TimeUnit timeUnit;

    private final long timeOut;

    private Process process;

    /** say whether or not the watchdog is currently monitoring a process */
    private boolean watch;

    /** say whether or not the process was killed due to running overtime */
    private boolean processKilled;

    private ScheduledFuture<?> scheduledFuture;

    public ProcessWatchdog(final long timeOutInMillis)
    {
        this(TimeUnit.MILLISECONDS, timeOutInMillis);
    }

    public ProcessWatchdog(final TimeUnit timeUnit, final long timeOut)
    {
        assert timeOut > 0L : "Unspecified time out";
        this.timeUnit = timeUnit;
        this.timeOut = timeOut;
    }

    /**
     * Watches the given process and terminates it, if it runs for too long. All information from
     * the previous run are reset.
     * 
     * @param p the process to monitor. It cannot be <tt>null</tt>. *
     * @throws IllegalStateException if a process is still being monitored.
     */
    public synchronized void start(final Process p)
    {
        assert p != null : "Unspecified process";
        if (this.process != null)
        {
            throw new IllegalStateException("Already running.");
        }
        this.process = p;
        this.processKilled = false;
        this.watch = true;
        this.process = p;
        scheduledFuture = executorService.schedule(this, timeOut, timeUnit);
    }

    /**
     * Stops the watcher.
     */
    public final synchronized void stop()
    {
        if (scheduledFuture != null)
        {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
        }
        watch = false;
        process = null;
    }

    /**
     * reset the monitor flag and the process.
     */
    protected void cleanUp()
    {
        watch = false;
        process = null;
    }

    /**
     * Indicates whether or not the watchdog is still monitoring the process.
     * 
     * @return <tt>true</tt> if the process is still running, otherwise <tt>false</tt>.
     */
    public final boolean isWatching()
    {
        return watch;
    }

    /**
     * Indicates whether the last process run was killed on timeout or not.
     * 
     * @return <tt>true</tt> if the process was killed otherwise <tt>false</tt>.
     */
    public final boolean isProcessKilled()
    {
        return processKilled;
    }

    //
    // Runnable
    //

    public final void run()
    {
        try
        {
            try
            {
                // We must check if the process was not stopped
                // before being here
                process.exitValue();
            } catch (final IllegalThreadStateException itse)
            {
                // the process is not terminated, if this is really
                // a timeout and not a manual stop then kill it.
                if (watch)
                {
                    processKilled = true;
                    process.destroy();
                }
            }
        } finally
        {
            cleanUp();
        }
    }
}
