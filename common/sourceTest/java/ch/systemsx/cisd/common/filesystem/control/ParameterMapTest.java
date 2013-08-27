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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author anttil
 */
public class ParameterMapTest
{
    IEventProvider eventProvider;

    Mockery context;

    ParameterMap map;

    @BeforeMethod
    public void fixture()
    {
        context = new Mockery();
        eventProvider = context.mock(IEventProvider.class);
        map = new ParameterMap(eventProvider);
    }

    @Test
    public void registeredParametersReturnDefaultValue() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    allowing(eventProvider).getNewEvents();
                    will(returnValue(new HashMap<String, String>()));
                }
            });

        map.addParameter("parameter", "value");
        assertThat(map.get("parameter"), is("value"));
    }

    @Test
    public void parametersCanBeUpdated() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    allowing(eventProvider).getNewEvents();
                    will(returnValue(getUpdateOn("parameter")));
                }
            });
        map.addParameter("parameter", "default value");
        assertThat(map.get("parameter"), is("updated value"));
    }

    private Map<String, String> getUpdateOn(String... keys)
    {
        Map<String, String> updates = new HashMap<String, String>();
        for (String key : keys)
        {
            updates.put(key, "updated value");
        }
        return updates;
    }

}
