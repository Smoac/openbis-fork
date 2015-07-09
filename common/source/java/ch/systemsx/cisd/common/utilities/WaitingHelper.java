/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.utilities;

import org.apache.commons.lang.time.DateUtils;

import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.time.DateTimeUtils;

/**
 * Helper class for waiting on a condition to be fulfilled.  
 * 
 * @author Franz-Josef Elmer
 */
public class WaitingHelper
{
    private static final long MINIMUM_LOG_INTERVAL = DateUtils.MILLIS_PER_MINUTE;

    private static final long MAXIMUM_LOG_INTERVAL = DateUtils.MILLIS_PER_HOUR;
    
    private static final double FACTOR = (Math.sqrt(5) + 1) / 2;

    private static final ISimpleLogger DUMMY_LOGGER = new ISimpleLogger()
        {
            @Override
            public void log(LogLevel level, String message, Throwable throwableOrNull)
            {
            }

            @Override
            public void log(LogLevel level, String message)
            {
            }
        };

    private final long timeOut;

    private final long pollingTime;

    private final ITimeAndWaitingProvider provider;

    private final ISimpleLogger logger;

    public WaitingHelper(long timeOut, long pollingTime, ISimpleLogger loggerOrNull)
    {
        this(timeOut, pollingTime, SystemTimeProvider.SYSTEM_TIME_PROVIDER, loggerOrNull);
    }

    public WaitingHelper(long timeOut, long pollingTime, ITimeAndWaitingProvider provider, ISimpleLogger loggerOrNull)
    {
        this.timeOut = timeOut;
        this.pollingTime = pollingTime;
        this.provider = provider;
        logger = loggerOrNull != null ? loggerOrNull : DUMMY_LOGGER;
    }

    /**
     * Waits until specified condition is fulfilled.
     * 
     * @return <code>true</code> if waiting stops because condition has been fulfilled. 
     *      If this isn't the case after the specified time out <code>false</code> will be returned.
     */
    public boolean waitOn(IWaitingCondition condition)
    {
        return waitOn(provider.getTimeInMilliseconds(), condition);
    }

    /**
     * Waits until specified condition is fulfilled.
     * 
     * @param startTime Start time. Waiting times out after startTime + timeOut.
     *      * @return <code>true</code> if waiting stops because condition has been fulfilled. 
     *      If this isn't the case after the specified time out <code>false</code> will be returned.
     */
    public boolean waitOn(long startTime, IWaitingCondition condition)
    {
        long t = provider.getTimeInMilliseconds();
        long logInterval = MINIMUM_LOG_INTERVAL;
        long lastLogTime = t - logInterval;
        while (t < startTime + timeOut)
        {
            long duration = t - startTime;
            String renderedDuration = DateTimeUtils.renderDuration(duration);
            if (condition.conditionFulfilled())
            {
                log(renderedDuration, condition, true);
                return true;
            }
            if (duration == 0 || t >= lastLogTime + logInterval)
            {
                log(renderedDuration, condition, false);
                lastLogTime = t;
                logInterval = Math.min(MAXIMUM_LOG_INTERVAL, Math.round(logInterval * FACTOR));
            }
            provider.sleep(pollingTime);
            t = provider.getTimeInMilliseconds();
        }
        return false;
    }

    private void log(String renderedDuration, IWaitingCondition condition, boolean fulfilled)
    {
        logger.log(LogLevel.INFO, "Condition " + (fulfilled ? "" : "still not ") + "fulfilled after " 
                + renderedDuration + ", condition: " + condition);
    }

}
