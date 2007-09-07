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

package ch.systemsx.cisd.datamover.filesystem.remote;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.datamover.filesystem.intf.IReadPathOperations;
import ch.systemsx.cisd.datamover.intf.ITimingParameters;

/**
 * A <code>CopyActivityMonitor</code> monitors write activity on a <var>destinationPath</var> and triggers an alarm
 * if there was a period of inactivity that exceeds a given inactivity period.
 * 
 * @author Bernd Rinn
 */
public class CopyActivityMonitor
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, CopyActivityMonitor.class);

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, CopyActivityMonitor.class);

    private final File destinationDirectory;

    private final IReadPathOperations readOperations;

    private final long checkIntervallMillis;

    private final String threadNamePrefix;

    /**
     * We need to keep a reference to the timer since we want to be able to cancel it and because otherwise the timer
     * thread will terminate.
     */
    private Timer activityMonitoringTimer;

    /**
     * Unfortunately there is no way for a {@link TimerTask} to know whether it has been cancelled. So we have to keep
     * it around in order to be able to terminate it directly.
     */
    private ActivityMonitoringTimerTask activityMonitoringTimerTask;

    /**
     * The current number of the activity monitor. Starts with 1 is in increased by 1 every time a new activity monitor
     * is started because the old one has get stuck.
     */
    private int currentNumberOfActivityMonitor;

    /**
     * We need to keep a reference to the timer since otherwise the timer thread will terminate.
     */
    private final Timer inactivityReportingTimer;

    /**
     * A <code>null</code> reference means: no monitoring.
     */
    private final AtomicReference<File> pathToBeCopied;

    /**
     * The time in milliseconds since start of the epoch when the monitored path has last been changed.
     */
    private final AtomicLong monitoredPathLastChanged;

    /**
     * The time in milliseconds since start of the epoch when the monitored path has last been checked for changes.
     */
    private final AtomicLong monitoredPathLastChecked;

    /**
     * Creates a monitor.
     * 
     * @param destinationDirectory The directory to monitor for write access.
     * @param readOperations Provides read-only access to the file system.
     * @param copyProcess The {@link ITerminable} representing the copy process. This will get terminated if the copy
     *            process gets stuck.
     * @param timingParameters The {@link ITimingParameters} to get the check interval and the inactivity period from.
     */
    public CopyActivityMonitor(File destinationDirectory, IReadPathOperations readOperations, ITerminable copyProcess,
            ITimingParameters timingParameters)
    {
        this.monitoredPathLastChecked = new AtomicLong(0);
        this.monitoredPathLastChanged = new AtomicLong(0);
        this.pathToBeCopied = new AtomicReference<File>(null);

        assert destinationDirectory != null;
        assert readOperations != null;
        assert copyProcess != null;
        assert timingParameters != null;

        this.destinationDirectory = destinationDirectory;
        this.readOperations = readOperations;
        this.checkIntervallMillis = timingParameters.getCheckIntervalMillis();

        assert this.checkIntervallMillis > 0;

        final String currentThreadName = Thread.currentThread().getName();
        if ("main".equals(currentThreadName))
        {
            this.threadNamePrefix = "";
        } else
        {
            this.threadNamePrefix = currentThreadName + " - ";
        }

        this.currentNumberOfActivityMonitor = 0;
        startNewActivityMonitor();

        this.inactivityReportingTimer = new Timer(threadNamePrefix + "Inactivity Reporter", true);
        this.inactivityReportingTimer.schedule(new InactivityReportingTimerTask(copyProcess, timingParameters
                .getInactivityPeriodMillis()), 0, timingParameters.getCheckIntervalMillis());
    }

    /**
     * Starts a new activity monitor and {@link Timer#cancel()}s the old one if any.
     */
    private void startNewActivityMonitor()
    {
        if (activityMonitoringTimer != null)
        {
            // This may or may not help to get rid of the thread, but at least we try it.
            activityMonitoringTimer.cancel();
            activityMonitoringTimerTask.terminate();
        }
        ++currentNumberOfActivityMonitor;
        activityMonitoringTimer =
                new Timer(threadNamePrefix + "Activity Monitor " + currentNumberOfActivityMonitor, true);
        activityMonitoringTimerTask = new ActivityMonitoringTimerTask();
        activityMonitoringTimer.schedule(activityMonitoringTimerTask, 0, checkIntervallMillis);
    }

    /**
     * Starts the activity monitoring.
     * 
     * @param newPathToBeCopied The path that will be copied to the destination directory and whose write progress
     *            should be monitored.
     */
    public void start(File newPathToBeCopied)
    {
        assert newPathToBeCopied != null;

        // Ensure the alarm won't be run before the copier has a chance to get active.
        final long now = System.currentTimeMillis();
        monitoredPathLastChecked.set(now);
        monitoredPathLastChanged.set(now);
        pathToBeCopied.set(newPathToBeCopied);
    }

    /**
     * Stops the activity monitoring.
     */
    public void stop()
    {
        pathToBeCopied.set(null);
    }

    /**
     * A {@link TimerTask} that monitors writing activity on a directory.
     */
    private final class ActivityMonitoringTimerTask extends TimerTask implements ITerminable
    {

        private AtomicBoolean terminated = new AtomicBoolean(false);

        private ActivityMonitoringTimerTask()
        {
            assert readOperations != null;
            assert pathToBeCopied != null;
            assert monitoredPathLastChanged != null;
            assert destinationDirectory != null;
        }

        @Override
        public void run()
        {
            final File path = pathToBeCopied.get();
            if (path == null)
            {
                return;
            }

            if (operationLog.isTraceEnabled())
            {
                operationLog.trace("Start activity monitoring run.");
            }

            try
            {
                final File pathToCheck = new File(destinationDirectory, path.getName());
                if (operationLog.isTraceEnabled())
                {
                    operationLog.trace(String.format("Asking checker %s for last change time of path '%s'.",
                            readOperations.getClass().getName(), pathToCheck));
                }
                if (readOperations.exists(pathToCheck) == false)
                {
                    operationLog.warn(String.format("File or directory '%s' does not (yet?) exist.", pathToCheck));
                    monitoredPathLastChecked.set(System.currentTimeMillis());
                    return;
                }
                final long lastChangedAsFoundByPathChecker = readOperations.lastChanged(pathToCheck);
                if (operationLog.isTraceEnabled())
                {
                    operationLog.trace(String.format(
                            "Checker %s reported last changed time of path '%s' to be %3$tF %3$tT.", readOperations
                                    .getClass().getName(), pathToCheck.getPath(), lastChangedAsFoundByPathChecker));
                }
                if (terminated.get()) // Don't modify the time variables any more if we got terminated.
                {
                    operationLog.warn("Activity monitor got terminated.");
                    return;
                }
                final long lastChecked = monitoredPathLastChecked.get();
                final long lastLastChanged = monitoredPathLastChanged.get();
                final long now = System.currentTimeMillis();
                // This catches the case where since the last check copying a files has been finished (and consequently
                // the
                // "last changed" time has been set to that of the source file), but copying of the next file has not
                // yet been
                // started.
                final long lastChanged =
                        Math.max(lastChangedAsFoundByPathChecker, lastLastChanged + (now - lastChecked) - 1);
                if (lastChanged > now) // That can happen if the system clock of the data producer is screwed up.
                {
                    machineLog.error(String.format("Found \"last changed time\" in the future (%1$tF %1$tT), "
                            + "check system clock of data producer.", lastChanged));
                }
                monitoredPathLastChecked.set(now);
                monitoredPathLastChanged.set(lastChanged);
            } finally
            {
                if (operationLog.isTraceEnabled())
                {
                    operationLog.trace("Finished activity monitoring run.");
                }
            }
        }

        /**
         * @return Always <code>true</code>.
         */
        public boolean terminate()
        {
            terminated.set(true);
            return true;
        }

    }

    /**
     * A {@link TimerTask} that reports a lack of write activity on a directory which is supposed to be a stall in the
     * copy operation of an item to this directory.
     */
    private final class InactivityReportingTimerTask extends TimerTask
    {

        private static final String ACTIVITY_MONITOR_STUCK_TEMPLATE =
                "The activity monitor timer thread %d got stuck, starting a new one.";

        private static final String TERMINATION_LOG_TEMPLATE = "Terminating %s due to a lack of activity.";

        private static final String INACTIVITY_REPORT_TEMPLATE =
                "No progress on copying '%s' to '%s' for %f seconds - network connection might be stalled.";

        private final long inactivityPeriodMillis;

        private final ITerminable terminable;

        public InactivityReportingTimerTask(ITerminable terminable, long inactivityPeriodMillis)
        {
            assert terminable != null;
            assert inactivityPeriodMillis > 0;
            assert monitoredPathLastChanged != null;
            assert destinationDirectory != null;

            this.terminable = terminable;
            this.inactivityPeriodMillis = inactivityPeriodMillis;
        }

        @Override
        public void run()
        {
            final File path = pathToBeCopied.get();
            if (path == null)
            {
                return;
            }

            if (operationLog.isTraceEnabled())
            {
                operationLog.trace("Start inactivity reporting run.");
            }

            final long now = System.currentTimeMillis();
            final long noCheckSinceMillis = now - monitoredPathLastChecked.get();
            final long noProgressSinceMillis = now - monitoredPathLastChanged.get();
            if (noCheckSinceMillis > Math.min(checkIntervallMillis * 3, inactivityPeriodMillis))
            {
                operationLog.warn(String.format(ACTIVITY_MONITOR_STUCK_TEMPLATE, currentNumberOfActivityMonitor));
                startNewActivityMonitor();
            }
            if (noProgressSinceMillis > inactivityPeriodMillis)
            {
                machineLog.error(String.format(INACTIVITY_REPORT_TEMPLATE, path, destinationDirectory,
                        noProgressSinceMillis / 1000.0f));
                operationLog.warn(String.format(TERMINATION_LOG_TEMPLATE, terminable.getClass().getName()));
                terminable.terminate();
                stop();
            }

            if (operationLog.isTraceEnabled())
            {
                operationLog.trace("Finished inactivity reporting run.");
            }
        }

    }

}
