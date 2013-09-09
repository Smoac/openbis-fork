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

import ch.systemsx.cisd.common.filesystem.control.ControlDirectoryEventFeed;
import ch.systemsx.cisd.common.filesystem.control.DelayingDecorator;
import ch.systemsx.cisd.common.filesystem.control.IValueFilter;
import ch.systemsx.cisd.common.filesystem.control.Parameter;
import ch.systemsx.cisd.common.filesystem.control.ParameterMap;
import ch.systemsx.cisd.common.logging.event.BooleanEvent;
import ch.systemsx.cisd.common.logging.event.LongEvent;

/**
 * @author pkupczyk
 */
public class ControlFileBasedLogConfiguration
{

    private static final String CONTROL_FILE_DIRECTORY = ".control";

    private static final long CONTROL_FILE_MAX_DELAY = 10 * 1000L;

    private static final String ON = "on";

    private static final String OFF = "off";

    private ParameterMap map;

    public ControlFileBasedLogConfiguration()
    {
        this(new File(CONTROL_FILE_DIRECTORY), CONTROL_FILE_MAX_DELAY);
    }

    public ControlFileBasedLogConfiguration(File controlFileDirectory, long controlFileMaxDelay)
    {
        map = new ParameterMap(new DelayingDecorator(controlFileMaxDelay, new ControlDirectoryEventFeed(controlFileDirectory)));
    }

    public synchronized void addBooleanParameter(String parameterName, boolean defaultValue)
    {
        map.addParameter(parameterName, defaultValue ? ON : OFF, new IValueFilter()
            {
                @Override
                public boolean isValid(String value)
                {
                    return ON.equalsIgnoreCase(value) || OFF.equalsIgnoreCase(value);
                }
            });
    }

    public synchronized void addBooleanEvent(String eventName)
    {
        map.addParameter(eventName, null, new IValueFilter()
            {
                @Override
                public boolean isValid(String value)
                {
                    return value == null || ON.equalsIgnoreCase(value) || OFF.equalsIgnoreCase(value);
                }
            });
    }

    public synchronized void addLongParameter(String parameterName, long defaultValue)
    {
        map.addParameter(parameterName, String.valueOf(defaultValue), new IValueFilter()
            {
                @Override
                public boolean isValid(String value)
                {
                    if (value == null)
                    {
                        return false;
                    } else
                    {
                        try
                        {
                            Long.valueOf(value);
                            return true;
                        } catch (NumberFormatException e)
                        {
                            return false;
                        }
                    }
                }
            });
    }

    public synchronized void addLongEvent(String eventName)
    {
        map.addParameter(eventName, null, new IValueFilter()
            {
                @Override
                public boolean isValid(String value)
                {
                    if (value == null)
                    {
                        return true;
                    } else
                    {
                        try
                        {
                            Long.valueOf(value);
                            return true;
                        } catch (NumberFormatException e)
                        {
                            return false;
                        }
                    }
                }
            });
    }

    public synchronized Boolean getBooleanParameterValue(String parameterName)
    {
        Parameter parameter = map.getParameterValue(parameterName);

        if (parameter == null)
        {
            return null;
        } else
        {
            if (parameter.getValue() == null)
            {
                return null;
            } else
            {
                return ON.equalsIgnoreCase(parameter.getValue());
            }
        }
    }

    public synchronized Long getLongParameterValue(String parameterName)
    {
        Parameter parameter = map.getParameterValue(parameterName);

        if (parameter == null)
        {
            return null;
        } else
        {
            if (parameter.getValue() == null)
            {
                return null;
            } else
            {
                return Long.valueOf(parameter.getValue());
            }
        }
    }

    public synchronized BooleanEvent getBooleanEvent(String eventName)
    {
        Parameter parameter = map.getParameterValue(eventName);

        if (parameter == null)
        {
            return null;
        } else
        {
            map.removeParameterValue(eventName);
            return new BooleanEvent(eventName, parameter.getValue() != null ? ON.equalsIgnoreCase(parameter.getValue()) : null);
        }
    }

    public synchronized LongEvent getLongEvent(String eventName)
    {
        Parameter parameter = map.getParameterValue(eventName);

        if (parameter == null)
        {
            return null;
        } else
        {
            map.removeParameterValue(eventName);
            return new LongEvent(eventName, parameter.getValue() != null ? Long.valueOf(parameter.getValue()) : null);
        }
    }

}
