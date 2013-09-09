/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.io.File;

import ch.systemsx.cisd.common.filesystem.control.ControlDirectoryEventFeed;
import ch.systemsx.cisd.common.filesystem.control.DelayingDecorator;
import ch.systemsx.cisd.common.filesystem.control.IValueFilter;
import ch.systemsx.cisd.common.filesystem.control.ParameterMap;

/**
 * @author anttil
 */
public class LongRunningThreadLogConfiguration
{
    private static final String LONG_RUNNING_THREAD_LOGGING = "long-running-thread-logging";

    private static final String LONG_RUNNING_THREAD_LOGGING_INTERVAL = "long-running-thread-logging-interval";

    private static final String LONG_RUNNING_THREAD_ALERT_THRESHOLD = "long-running-thread-alert-threshold";

    private final ParameterMap pm;

    public LongRunningThreadLogConfiguration()
    {
        pm = new ParameterMap(new DelayingDecorator(5000, new ControlDirectoryEventFeed(new File(".control"))));
        pm.addParameter(LONG_RUNNING_THREAD_LOGGING, "on", new IValueFilter()
            {
                @Override
                public boolean isValid(String value)
                {
                    return "on".equalsIgnoreCase(value) || "off".equalsIgnoreCase(value);
                }
            });
        pm.addParameter(LONG_RUNNING_THREAD_LOGGING_INTERVAL, "60000", new IValueFilter()
            {
                @Override
                public boolean isValid(String value)
                {
                    return isLong(value);
                }
            });

        pm.addParameter(LONG_RUNNING_THREAD_ALERT_THRESHOLD, "15000", new IValueFilter()
            {
                @Override
                public boolean isValid(String value)
                {
                    return isLong(value);
                }
            });

    }

    public boolean isLoggingEnabled()
    {
        return "on".equals(pm.getParameterValue(LONG_RUNNING_THREAD_LOGGING).getValue());
    }

    public long logInterval()
    {
        return Long.parseLong(pm.getParameterValue(LONG_RUNNING_THREAD_LOGGING_INTERVAL).getValue());
    }

    public long maxValidInvocationLength()
    {
        return Long.parseLong(pm.getParameterValue(LONG_RUNNING_THREAD_ALERT_THRESHOLD).getValue());
    }

    private boolean isLong(String value)
    {
        try
        {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e)
        {
            return false;
        }
    }

}
