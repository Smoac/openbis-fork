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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.UnknownLastChangedException;
import ch.systemsx.cisd.datamover.intf.ITimingParameters;

/**
 * A <code>CopyActivityMonitor</code> monitors write activity on a <var>destinationStore</var>
 * and triggers an alarm if there was a period of inactivity that exceeds a given inactivity period.
 * 
 * @author Bernd Rinn
 */
public class CopyActivityMonitor
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CopyActivityMonitor.class);

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, CopyActivityMonitor.class);

    private final IFileStore destinationStore;

    private final long checkIntervallMillis;

    private final long inactivityPeriodMillis;

    private final String threadNamePrefix;

    private final ITerminable terminable;

    /**
     * We need to keep a reference to the timer since we want to be able to cancel it and because
     * otherwise the timer thread will terminate.
     */
    private Timer activityMonitoringTimer;

    /**
     * Unfortunately there is no way for a {@link TimerTask} to know whether it has been canceled.
     * So we have to keep it around in order to be able to terminate it directly.
     */
    private ActivityMonitoringTimerTask activityMonitoringTimerTask;

    /**
     * Creates a monitor.
     * 
     * @param destinationStore The file store to monitor for write access.
     * @param copyProcess The {@link ITerminable} representing the copy process. This will get
     *            terminated if the copy process gets stuck.
     * @param timingParameters The {@link ITimingParameters} to get the check interval and the
     *            inactivity period from.
     */
    public CopyActivityMonitor(IFileStore destinationStore, ITerminable copyProcess,
            ITimingParameters timingParameters)
    {
        assert destinationStore != null;
        assert copyProcess != null;
        assert timingParameters != null;

        this.destinationStore = destinationStore;
        this.terminable = copyProcess;
        this.checkIntervallMillis = timingParameters.getCheckIntervalMillis();
        this.inactivityPeriodMillis = timingParameters.getInactivityPeriodMillis();

        assert this.checkIntervallMillis > 0;

        final String currentThreadName = Thread.currentThread().getName();
        if ("main".equals(currentThreadName))
        {
            this.threadNamePrefix = "";
        } else
        {
            this.threadNamePrefix = currentThreadName + " - ";
        }
    }

    /**
     * Starts the activity monitoring.
     * 
     * @param itemToBeCopied The item that will be copied to the destination file store and whose
     *            write progress should be monitored.
     */
    public void start(StoreItem itemToBeCopied)
    {
        assert itemToBeCopied != null;

        activityMonitoringTimer = new Timer(threadNamePrefix + "Activity Monitor", true);
        activityMonitoringTimerTask = new ActivityMonitoringTimerTask(itemToBeCopied);
        // we start the timer after some delay to let the copy process be started
        activityMonitoringTimer.schedule(activityMonitoringTimerTask, checkIntervallMillis / 2,
                checkIntervallMillis);
    }

    /**
     * Stops the activity monitoring. The activity monitor must not be used after calling this
     * method.
     */
    public void stop()
    {
        activityMonitoringTimer.cancel();
    }

    private static interface LastChangeItemChecker
    {
        // returns 0 when an error or timeout occurs during the check
        long lastChanged(long previousCheck);
    }

    /**
     * A value object that holds the information about the last check performed for a path.
     */
    public static final class PathCheckRecord
    {
        final private long timeChecked;

        final private long timeOfLastModification;

        public PathCheckRecord(final long timeChecked, final long timeLastChanged)
        {
            this.timeChecked = timeChecked;
            this.timeOfLastModification = timeLastChanged;
        }

        /**
         * The time when the entry was checked.
         */
        public long getTimeChecked()
        {
            return timeChecked;
        }

        /**
         * The newest last modification time found during the check.
         */
        public long getTimeOfLastModification()
        {
            return timeOfLastModification;
        }
    }

    /**
     * A {@link TimerTask} that monitors writing activity on a directory.
     */
    private final class ActivityMonitoringTimerTask extends TimerTask
    {

        private static final String TERMINATION_LOG_TEMPLATE =
                "Terminating %s due to a lack of activity.";

        private static final String INACTIVITY_REPORT_TEMPLATE =
                "No progress on copying '%s' to '%s' for %f seconds - network connection might be stalled.";

        private final ExecutorService lastChangedExecutor =
                new NamingThreadPoolExecutor("Last Changed Explorer", 1, Integer.MAX_VALUE);

        private final StoreItem itemToBeCopied;

        private final LastChangeItemChecker lastChangeChecker;

        private PathCheckRecord lastCheckOrNull;

        private ActivityMonitoringTimerTask(StoreItem itemToBeCopied)
        {
            assert terminable != null;
            assert itemToBeCopied != null;
            assert destinationStore != null;

            this.lastCheckOrNull = null;
            this.itemToBeCopied = itemToBeCopied;
            this.lastChangeChecker = createLastChangedChecker();
        }

        @Override
        public void run()
        {
            if (operationLog.isTraceEnabled())
            {
                operationLog.trace("Start activity monitoring run.");
            }

            try
            {
                if (operationLog.isTraceEnabled())
                {
                    operationLog.trace(String.format(
                            "Asking for last change time of '%s' inside '%s'.", itemToBeCopied,
                            destinationStore));
                }
                if (destinationStore.exists(itemToBeCopied) == false)
                {
                    operationLog.warn(String.format(
                            "File or directory '%s' inside '%s' does not (yet?) exist.",
                            itemToBeCopied, destinationStore));
                    return;
                }
                final long now = System.currentTimeMillis();
                if (isQuietFor(inactivityPeriodMillis, now))
                {
                    final long noProgressSinceMillis = now - lastCheckOrNull.getTimeChecked();
                    machineLog.error(String.format(INACTIVITY_REPORT_TEMPLATE, itemToBeCopied,
                            destinationStore, noProgressSinceMillis / 1000.0f));

                    operationLog.warn(String.format(TERMINATION_LOG_TEMPLATE, terminable.getClass()
                            .getName()));
                    terminable.terminate();
                    stop();
                }
            } catch (CheckedExceptionTunnel ex)
            {
                if (ex.getCause() instanceof InterruptedException)
                {
                    operationLog.warn("Activity monitor got terminated.");
                } else
                {
                    throw ex;
                }
            } finally
            {
                if (operationLog.isTraceEnabled())
                {
                    operationLog.trace("Finished activity monitoring run.");
                }
            }
        }

        private LastChangeItemChecker createLastChangedChecker()
        {
            return new LastChangeItemChecker()
                {
                    public long lastChanged(long previousCheck)
                    {
                        final Long lastChanged = tryLastChanged(destinationStore, itemToBeCopied);
                        if (operationLog.isTraceEnabled() && lastChanged != null)
                        {
                            String msgTemplate =
                                    "Checker reported last changed time of '%s' inside '%s' to be %3$tF %3$tT.";
                            String msg =
                                    String.format(msgTemplate, itemToBeCopied, destinationStore,
                                            lastChanged);
                            operationLog.trace(msg);
                        }
                        return (lastChanged != null) ? lastChanged : 0;
                    }
                };
        }

        // true if nothing has changed during the specified period
        private boolean isQuietFor(long quietPeriodMillis, long now)
        {
            if (lastCheckOrNull == null) // never checked before
            {
                setFirstModificationDate(now);
                return false;
            } else
            {
                final boolean oldIsUnknown = (lastCheckOrNull.getTimeOfLastModification() == 0);
                // no need to check yet
                if (now - lastCheckOrNull.getTimeChecked() < quietPeriodMillis)
                {
                    // if last check finished with an error, try to redo it and save it with the
                    // previous check time
                    if (oldIsUnknown)
                    {
                        setFirstModificationDate(lastCheckOrNull.getTimeChecked());
                    }
                    return false;
                } else if (oldIsUnknown)
                {
                    // during the whole period modification time could not be fetched. It could be
                    // unchanged, trying to fetch it now will give us no information.
                    return true;
                } else
                {
                    return checkIfModifiedAndSet(now);
                }
            }
        }

        // check if item has been modified since last check by comparing its current modification
        // time to the one acquired in the past
        private boolean checkIfModifiedAndSet(long now)
        {
            final long prevModificationTime = lastCheckOrNull.getTimeOfLastModification();
            final long newModificationTime = lastChangeChecker.lastChanged(prevModificationTime);
            boolean newIsKnown = (newModificationTime != 0);
            if (newIsKnown && newModificationTime != prevModificationTime)
            {
                lastCheckOrNull = new PathCheckRecord(now, newModificationTime);
                return false;
            } else
            {
                return true; // item unchanged or we could not fetch this information
            }
        }

        private void setFirstModificationDate(final long timeChecked)
        {
            long lastChanged = lastChangeChecker.lastChanged(0L); // 0 if error
            lastCheckOrNull = new PathCheckRecord(timeChecked, lastChanged);
        }

        private Long tryLastChanged(IFileStore store, StoreItem item)
        {
            final ISimpleLogger simpleMachineLog = new Log4jSimpleLogger(machineLog);
            final Future<Long> lastChangedFuture =
                    lastChangedExecutor.submit(createCheckerCallable(store, item,
                            minusSafetyMargin(inactivityPeriodMillis)));
            final long timeoutMillis = Math.min(checkIntervallMillis * 3, inactivityPeriodMillis);
            try
            {
                final Long lastChanged =
                        ConcurrencyUtilities.getResult(lastChangedFuture, timeoutMillis,
                                simpleMachineLog, "Check for recent paths").tryGetResult();
                if (lastChanged == null)
                {
                    operationLog.error(String.format(
                            "Could not determine \"last changed time\" of %s: time out.", item));
                    return null;
                }
                return lastChanged;
            } catch (UnknownLastChangedException ex)
            {
                operationLog.error(String.format(
                        "Could not determine \"last changed time\" of %s: %s", item, ex));
                return null;
            }
        }

        private long minusSafetyMargin(long period)
        {
            return Math.max(0L, period - 1000L);
        }

        private Callable<Long> createCheckerCallable(final IFileStore store, final StoreItem item,
                final long stopWhenYoungerThan)
        {
            return new Callable<Long>()
                {
                    public Long call() throws Exception
                    {
                        if (machineLog.isTraceEnabled())
                        {
                            machineLog.trace("Starting quick check for recent paths on '" + item
                                    + "'.");
                        }
                        try
                        {
                            final long lastChanged =
                                    store.lastChangedRelative(item, stopWhenYoungerThan);
                            if (machineLog.isTraceEnabled())
                            {
                                machineLog
                                        .trace(String
                                                .format(
                                                        "Finishing quick check for recent paths on '%s', found to be %2$tF %2$tT.",
                                                        item, lastChanged));
                            }
                            return lastChanged;
                        } catch (RuntimeException ex)
                        {
                            if (machineLog.isTraceEnabled())
                            {
                                final Throwable th =
                                        (ex instanceof CheckedExceptionTunnel) ? ex.getCause() : ex;
                                machineLog.trace("Failed on quick check for recent paths on '"
                                        + item + "'.", th);
                            }
                            throw ex;
                        }
                    }
                };
        }

    }

}
