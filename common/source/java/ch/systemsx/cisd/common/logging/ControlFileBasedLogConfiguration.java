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

package ch.systemsx.cisd.common.logging;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.common.filesystem.control.ControlDirectoryEventFeed;
import ch.systemsx.cisd.common.filesystem.control.DelayingDecorator;
import ch.systemsx.cisd.common.filesystem.control.IEventFilter;
import ch.systemsx.cisd.common.filesystem.control.IValueFilter;
import ch.systemsx.cisd.common.filesystem.control.ParameterMap;

/**
 * @author pkupczyk
 */
public class ControlFileBasedLogConfiguration
{

    private static final String CONTROL_FILE_DIRECTORY = ".control";

    private static final long CONTROL_FILE_MAX_DELAY = 10 * 1000L;

    private static final String ON = "on";

    private static final String OFF = "off";

    private ControlDirectoryEventFeed eventFeed;

    private ParameterMap parameterMap;

    public ControlFileBasedLogConfiguration()
    {
        this(new File(CONTROL_FILE_DIRECTORY), CONTROL_FILE_MAX_DELAY);
    }

    public ControlFileBasedLogConfiguration(File controlFileDirectory, long controlFileMaxDelay)
    {
        eventFeed = new ControlDirectoryEventFeed(controlFileDirectory);
        parameterMap =
                new ParameterMap(new DelayingDecorator(controlFileMaxDelay, eventFeed));
    }

    public void addBooleanParameter(String parameterName, boolean defaultValue)
    {
        parameterMap.addParameter(parameterName, defaultValue ? ON : OFF, new IValueFilter()
            {

                @Override
                public boolean isValid(String value)
                {
                    return ON.equalsIgnoreCase(value) || OFF.equalsIgnoreCase(value);
                }
            });
    }

    public boolean getBooleanParameterValue(String parameterName)
    {
        String value = parameterMap.get(parameterName);
        return ON.equalsIgnoreCase(value);
    }

    public boolean hasEvent(final String eventName)
    {
        List<String> events = eventFeed.getNewEvents(new IEventFilter()
            {
                @Override
                public boolean accepts(String value)
                {
                    return eventName.equals(value);
                }
            });
        return events != null && events.isEmpty() == false;
    }

}
