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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author anttil
 */
public class ParameterMap
{

    private final Map<String, String> values;

    private final Map<String, IValueFilter> filters;

    private final IEventProvider eventProvider;

    public ParameterMap(IEventProvider eventProvider)
    {
        this.eventProvider = eventProvider;
        this.values = new HashMap<String, String>();
        this.filters = new HashMap<String, IValueFilter>();
    }

    public void addParameter(String key, String defaultValue)
    {
        addParameter(key, defaultValue, dummyFilter());
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
        Map<String, String> newEvents = eventProvider.getNewEvents(values.keySet());
        for (String parameter : newEvents.keySet())
        {
            String newValue = newEvents.get(parameter);
            if (filters.get(parameter).isValid(newValue))
            {
                values.put(parameter, newValue);
            }
        }
        return values.get(key);
    }

    private IValueFilter dummyFilter()
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

    public static void main(String args[])
    {
        ParameterMap map = new ParameterMap(
                new DelayingDecorator(5000,
                        new FileSystemBasedEventProvider(new File("/tmp/test"))));

        map.addParameter("parameter", "100");

        while (true)
        {
            System.out.println(map.get("parameter"));
            try
            {
                Thread.sleep(100);
            } catch (InterruptedException ex)
            {
            }
        }
    }
}
