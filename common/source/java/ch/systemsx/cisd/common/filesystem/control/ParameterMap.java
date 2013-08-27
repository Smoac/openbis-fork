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

package ch.systemsx.cisd.common.filesystem.control;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collection of key-value pairs that are updated by given event feed. Events have to be of format "key-value", other events are ignored.
 * 
 * @author anttil
 */
public class ParameterMap
{
    private final Map<String, String> values;

    private final Map<String, IValueFilter> filters;

    private final IEventFeed eventFeed;

    public ParameterMap(IEventFeed eventFeed)
    {
        this.eventFeed = eventFeed;
        this.values = new HashMap<String, String>();
        this.filters = new HashMap<String, IValueFilter>();
    }

    public void addParameter(String key, String defaultValue)
    {
        addParameter(key, defaultValue, acceptAllFilter());
    }

    public synchronized void addParameter(String key, String defaultValue, IValueFilter filter)
    {
        if (filter.isValid(defaultValue) == false)
        {
            throw new IllegalArgumentException("Default value " + defaultValue + " is not valid value for parameter " + key);
        }

        filters.put(key, filter);
        values.put(key, defaultValue);
    }

    public synchronized String get(String key)
    {
        List<String> events = eventFeed.getNewEvents(eventFilter(values.keySet()));
        for (String event : events)
        {
            String parameter = event.substring(0, event.lastIndexOf("-"));
            String value = event.substring(event.lastIndexOf("-") + 1);
            IValueFilter filter = filters.get(parameter);

            if (filter != null && filter.isValid(value))
            {
                values.put(parameter, value);
            }
        }
        return values.get(key);
    }

    private IEventFilter eventFilter(final Set<String> keySet)
    {
        return new IEventFilter()
            {

                @Override
                public boolean accepts(String event)
                {
                    for (String parameter : keySet)
                    {
                        if (event.startsWith(parameter + "-"))
                        {
                            return true;
                        }
                    }
                    return false;
                }
            };
    }

    private IValueFilter acceptAllFilter()
    {
        return new IValueFilter()
            {
                @Override
                public boolean isValid(String value)
                {
                    return true;
                }

            };
    }
}
