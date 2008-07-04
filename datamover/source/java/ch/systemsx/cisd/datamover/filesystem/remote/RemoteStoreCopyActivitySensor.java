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

package ch.systemsx.cisd.datamover.filesystem.remote;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.InactivityMonitor.IActivitySensor;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.DateStatus;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;

/**
 * A {@link IActivitySensor} that senses changes in copy operations to a {@link StoreItem} in a
 * remote store.
 * 
 * @author Bernd Rinn
 */
public class RemoteStoreCopyActivitySensor implements IActivitySensor
{
    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, RemoteStoreCopyActivitySensor.class);

    private static final int MAX_ERROR_COUNT = 1;

    private final int maxErrorsToIgnore;

    private final IFileStore destinationStore;

    private final StoreItem copyItem;

    private final long timeOfCreation = System.currentTimeMillis();

    private long timeOfLastConfirmedActivity = timeOfCreation;

    private long timeOfLastReportedActivity = timeOfCreation;

    private long lastNonErrorResult = -1L;

    private DateStatus currentResult;

    private int errorCount = 0;

    public RemoteStoreCopyActivitySensor(IFileStore destinationStore, StoreItem copyItem)
    {
        this(destinationStore, copyItem, MAX_ERROR_COUNT);
    }

    public RemoteStoreCopyActivitySensor(IFileStore destinationStore, StoreItem copyItem,
            int maxErrorsToIgnore)
    {
        this.destinationStore = destinationStore;
        this.copyItem = copyItem;
        this.maxErrorsToIgnore = maxErrorsToIgnore;
        this.currentResult =
                DateStatus.createError(String.format(
                        "Last activity on item '%s' of store '%s' never checked", copyItem,
                        destinationStore));
    }

    public long getTimeOfLastActivityMoreRecentThan(long thresholdMillis)
    {
        currentResult = destinationStore.lastChangedRelative(copyItem, thresholdMillis);
        final long now = System.currentTimeMillis();
        if (currentResult.isError())
        {
            ++errorCount;
            if (errorCount <= maxErrorsToIgnore)
            {
                timeOfLastReportedActivity = now;
                machineLog.error(describeInactivity(now)
                        + String.format(" (error count: %d <= %d, goes unreported)", errorCount,
                                maxErrorsToIgnore));
            } else
            {
                machineLog.error(describeInactivity(now)
                        + " (error count: %s, reported to monitor)");
            }
        } else
        {
            if (currentResult.getResult() != lastNonErrorResult)
            {
                timeOfLastConfirmedActivity = now;
                lastNonErrorResult = currentResult.getResult();
                if (machineLog.isDebugEnabled())
                {
                    machineLog.debug(String.format(
                            "Observing write activity on item '%s' in store '%s'", copyItem,
                            destinationStore));
                }
            }
            // Implementation note: This means we can report an older time of activity than what we
            // reported the last time if the last time we had an error. This is on purpose as it
            // helps avoiding a situation where error and non-error situations do "flip-flop" and we
            // could report progress where there is no progress.
            timeOfLastReportedActivity = timeOfLastConfirmedActivity;
            errorCount = 0;
        }

        return timeOfLastReportedActivity;
    }

    public String describeInactivity(long now)
    {
        if (currentResult.isError())
        {
            final String msg = currentResult.tryGetMessage();
            if (StringUtils.isBlank(msg))
            {
                return String.format("Error: Unable to determine the time of write activity "
                        + "on item '%s' in store '%s'", copyItem, destinationStore);
            } else
            {
                return String.format("Error [%s]: Unable to determine the time of write activity "
                        + "on item '%s' in store '%s'", msg, copyItem, destinationStore);
            }
        } else
        {
            final String inactivityPeriod =
                    DurationFormatUtils.formatDurationHMS(now - timeOfLastConfirmedActivity);
            return String.format("No write activity on item '%s' in store '%s' for %s", copyItem,
                    destinationStore, inactivityPeriod);
        }
    }

}
