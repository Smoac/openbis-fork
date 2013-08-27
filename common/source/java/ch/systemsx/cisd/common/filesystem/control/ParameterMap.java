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

    private final Map<String, String> map;

    private final IEventProvider eventProvider;

    public ParameterMap(IEventProvider eventProvider)
    {
        this.eventProvider = eventProvider;
        map = new HashMap<String, String>();
    }

    public void addParameter(String key, String defaultValue)
    {
        map.put(key, defaultValue);
    }

    public String get(String key)
    {
        Map<String, String> newEvents = eventProvider.getNewEvents();
        map.putAll(newEvents);
        return map.get(key);
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
