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

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.ExecutionResult;
import ch.systemsx.cisd.common.concurrent.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.BooleanStatus;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.NumberStatus;
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

    private final IFileStoreMonitor destinationStore;

    private final long checkIntervallMillis;

    private final long inactivityPeriodMillis;

    private final String threadNamePrefix;

    private final ExecutorService lastChangedExecutor =
            new NamingThreadPoolExecutor("Last Changed Explorer").daemonize();

    /** handler to terminate monitored process if the observed store item does not change */
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
        this(createFileStoreMonitor(destinationStore), copyProcess, timingParameters);
    }

    @Private
    CopyActivityMonitor(IFileStoreMonitor destinationStoreMonitor, ITerminable copyProcess,
            ITimingParameters timingParameters)
    {
        assert destinationStoreMonitor != null;
        assert copyProcess != null;
        assert timingParameters != null;

        this.destinationStore = destinationStoreMonitor;
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

    // Used for all file system operations in this class.
    @Private
    static interface IFileStoreMonitor
    {
        NumberStatus lastChangedRelative(StoreItem item, long stopWhenYoungerThan);

        BooleanStatus exists(StoreItem item);

        // description of the store for logging purposes
        String toString();
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

    /**
     * A value object that holds the information about the last check performed for a path.
     */
    public static final class PathCheckRecord
    {
        final private long timeChecked;

        final private NumberStatus timeOfLastModification;

        public PathCheckRecord(final long timeChecked, final NumberStatus timeLastChanged)
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
        public NumberStatus getTimeOfLastModification()
        {
            return timeOfLastModification;
        }
    }

    private static IFileStoreMonitor createFileStoreMonitor(final IFileStore destinationStore)
    {
        return new IFileStoreMonitor()
            {
                public NumberStatus lastChangedRelative(StoreItem item,
                        long stopWhenFindYoungerRelative)
                {
                    return destinationStore.lastChangedRelative(item, stopWhenFindYoungerRelative);
                }

                public BooleanStatus exists(StoreItem item)
                {
                    return destinationStore.exists(item);
                }

                @Override
                public String toString()
                {
                    return destinationStore.toString();
                }
            };
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

        private final StoreItem itemToBeCopied;

        private PathCheckRecord lastCheckOrNull;

        private ActivityMonitoringTimerTask(StoreItem itemToBeCopied)
        {
            assert terminable != null;
            assert itemToBeCopied != null;
            assert destinationStore != null;

            this.lastCheckOrNull = null;
            this.itemToBeCopied = itemToBeCopied;
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

        // true if nothing has changed during the specified period
        private boolean isQuietFor(long quietPeriodMillis, long now)
        {
            if (destinationStore.exists(itemToBeCopied).isSuccess() == false)
            {
                return checkNonexistentPeriod(quietPeriodMillis, now);
            }
            if (lastCheckOrNull == null) // never checked before
            {
                setFirstModificationDate(now);
                return false;
            } else
            {
                final boolean oldIsUnknown = lastCheckOrNull.getTimeOfLastModification().isError();
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
                    long prevModificationTime =
                            lastCheckOrNull.getTimeOfLastModification().getResult();
                    return checkIfUnmodifiedAndSet(now, prevModificationTime);
                }
            }
        }

        // Checks how much time elapsed since the last check without looking into file system.
        // Returns true if it's more than quietPeriodMillis.
        // Used to stop the copy process if file does not appear at all for a long time.
        private boolean checkNonexistentPeriod(long quietPeriodMillis, long now)
        {
            if (lastCheckOrNull == null)
            {
                lastCheckOrNull = new PathCheckRecord(now, NumberStatus.createError());
                return false;
            } else
            {
                if (lastCheckOrNull.getTimeOfLastModification().isError() == false)
                {
                    operationLog.warn(String.format(
                            "File or directory '%s' has vanished from '%s'.", itemToBeCopied,
                            destinationStore));
                } else
                {
                    operationLog.warn(String.format(
                            "File or directory '%s' inside '%s' does not (yet?) exist.",
                            itemToBeCopied, destinationStore));
                }
                return (now - lastCheckOrNull.getTimeChecked() >= quietPeriodMillis);
            }
        }

        // check if item has been unmodified ("quite") since last check by comparing its current
        // modification time to the one acquired in the past.
        private boolean checkIfUnmodifiedAndSet(long now, long prevModificationTime)
        {
            final NumberStatus newModificationTime = lastChanged(itemToBeCopied);
            if (newModificationTime.isError() == false
                    && newModificationTime.getResult() != prevModificationTime)
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
            NumberStatus lastChanged = lastChanged(itemToBeCopied);
            lastCheckOrNull = new PathCheckRecord(timeChecked, lastChanged);
        }
    }

    private NumberStatus lastChanged(StoreItem item)
    {
        final NumberStatus lastChanged = lastChanged(destinationStore, item);
        if (lastChanged.isError())
        {
            operationLog.error(lastChanged.tryGetMessage());
        } else if (operationLog.isTraceEnabled())
        {
            String msgTemplate =
                    "Checker reported last changed time of '%s' inside '%s' to be %3$tF %3$tT.";
            String msg =
                    String.format(msgTemplate, item, destinationStore, lastChanged.getResult());
            operationLog.trace(msg);
        }
        return lastChanged;
    }

    private long minusSafetyMargin(long period)
    {
        return Math.max(0L, period - 1000L);
    }

    private NumberStatus lastChanged(IFileStoreMonitor store, StoreItem item)
    {
        long stopWhenFindYoungerRelative = minusSafetyMargin(inactivityPeriodMillis);
        final long timeoutMillis = Math.min(checkIntervallMillis * 3, inactivityPeriodMillis);
        final ISimpleLogger simpleMachineLog = new Log4jSimpleLogger(machineLog);
        final Future<NumberStatus> lastChangedFuture =
                lastChangedExecutor.submit(createCheckerCallable(store, item,
                        stopWhenFindYoungerRelative));
        ExecutionResult<NumberStatus> executionResult =
                ConcurrencyUtilities.getResult(lastChangedFuture, timeoutMillis, simpleMachineLog,
                        "Check for recent paths");
        NumberStatus result = executionResult.tryGetResult();
        if (result == null)
        {
            return NumberStatus.createError(String.format(
                    "Could not determine \"last changed time\" of %s: time out.", item));
        } else
        {
            return result;
        }
    }

    private static Callable<NumberStatus> createCheckerCallable(final IFileStoreMonitor store,
            final StoreItem item, final long stopWhenYoungerThan)
    {
        return new Callable<NumberStatus>()
            {
                public NumberStatus call() throws Exception
                {
                    if (machineLog.isTraceEnabled())
                    {
                        machineLog
                                .trace("Starting quick check for recent paths on '" + item + "'.");
                    }
                    final NumberStatus lastChanged =
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
                }
            };
    }

}
