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

package ch.systemsx.cisd.datamover.filesystem;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.ExecutionResult;
import ch.systemsx.cisd.common.concurrent.ExecutionStatus;
import ch.systemsx.cisd.common.concurrent.IActivityObserver;
import ch.systemsx.cisd.common.concurrent.InactivityMonitor;
import ch.systemsx.cisd.common.concurrent.InactivityMonitor.IDescribingActivitySensor;
import ch.systemsx.cisd.common.concurrent.InactivityMonitor.IInactivityObserver;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathRemover;

/**
 * A class which supports removal of paths on a file system that is mounted locally.
 * <p>
 * Supports retrying if the operation fails for the first time.
 * 
 * @author Bernd Rinn
 */
final class RetryingPathRemover implements IPathRemover
{
    private static final long DELETE_ONE_FILE_TIMEOUT_MILLIS =
            Constants.MILLIS_TO_WAIT_BEFORE_TIMEOUT;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RetryingPathRemover.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, RetryingPathRemover.class);

    private final static ExecutorService executor =
            new NamingThreadPoolExecutor("Deletion Thread").daemonize();

    private final int maxRetriesOnFailure;

    private final long millisToSleepOnFailure;

    RetryingPathRemover(int maxRetriesOnFailure, long millisToSleepOnFailure)
    {
        this.maxRetriesOnFailure = maxRetriesOnFailure;
        this.millisToSleepOnFailure = millisToSleepOnFailure;
    }

    private final static Status STATUS_FAILED_DELETION =
            Status.createError("Failed to remove path.");

    public Status remove(File path)
    {
        assert path != null;

        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Removing path '%s'", path.getPath()));
        }
        int failures = 0;
        boolean deletionOK = false;
        while (true)
        {
            deletionOK = deleteAndMonitor(path);
            if (deletionOK)
            {
                break;
            } else
            {
                if (path.exists() == false)
                {
                    operationLog.warn(String.format(
                            "Path '%s' doesn't exist, so it can't be removed.", path));
                    break;
                }
                ++failures;
                operationLog.warn(String.format("Removing path '%s' failed (attempt %d).", path,
                        failures));
                if (failures >= maxRetriesOnFailure)
                {
                    break;
                }
                try
                {
                    Thread.sleep(millisToSleepOnFailure);
                } catch (InterruptedException ex)
                {
                    break;
                }
            }
        }

        if (deletionOK == false)
        {
            notificationLog.error(String.format("Removing path '%s' failed, giving up.", path));
            return STATUS_FAILED_DELETION;
        } else
        {
            return Status.OK;
        }
    }

    @Private
    static class DeleteActivityDetector implements IDescribingActivitySensor, IActivityObserver
    {
        private volatile long lastActivityMillis = System.currentTimeMillis();

        private final File path;

        public DeleteActivityDetector(File path)
        {
            this.path = path;
        }

        // called each time when one file gets deleted
        synchronized public void update()
        {
            lastActivityMillis = System.currentTimeMillis();
        }

        synchronized public String describeInactivity(long now)
        {
            return "No delete activity of path " + path.getPath() + " for "
                    + DurationFormatUtils.formatDurationHMS(now - lastActivityMillis);
        }

        synchronized public long getLastActivityMillisMoreRecentThan(long thresholdMillis)
        {
            return lastActivityMillis;
        }

        synchronized public boolean hasActivityMoreRecentThan(long thresholdMillis)
        {
            return (System.currentTimeMillis() - lastActivityMillis) < thresholdMillis;
        }

    }

    // if there is no progress during deletion, it will be stopped
    private boolean deleteAndMonitor(final File path)
    {
        final DeleteActivityDetector sensor = new DeleteActivityDetector(path);
        Callable<Boolean> deleteCallable = new Callable<Boolean>()
            {
                public Boolean call() throws Exception
                {
                    return FileUtilities.deleteRecursively(path, null, sensor);
                }
            };
        return executeAndMonitor(sensor, deleteCallable, DELETE_ONE_FILE_TIMEOUT_MILLIS);
    }

    @Private
    Boolean executeAndMonitor(final IDescribingActivitySensor sensor, final Callable<Boolean> deleteCallable,
            final long inactivityThresholdMillis)
    {
        final Future<Boolean> deleteFuture = executor.submit(deleteCallable);

        IInactivityObserver inactivityObserver = new IInactivityObserver()
            {
                // called when inactivity took longer than a timeout
                public void update(long inactiveSinceMillis, String descriptionOfInactivity)
                {
                    operationLog.error(descriptionOfInactivity);
                    deleteFuture.cancel(true);
                }
            };
        InactivityMonitor inactivityMonitor =
                new InactivityMonitor(sensor, inactivityObserver, inactivityThresholdMillis, true);

        ExecutionResult<Boolean> executionResult =
                ConcurrencyUtilities.getResult(deleteFuture, ConcurrencyUtilities.NO_TIMEOUT);
        inactivityMonitor.stop();

        Boolean result = executionResult.tryGetResult();
        if (result != null)
        {
            return result.booleanValue();
        } else
        {
            operationLog.error("Removal operation terminated with an error status: "
                    + executionResult.getStatus());
            if (executionResult.getStatus() == ExecutionStatus.EXCEPTION)
            {
                operationLog.error(executionResult.tryGetException());
            }
            return false;
        }
    }
}