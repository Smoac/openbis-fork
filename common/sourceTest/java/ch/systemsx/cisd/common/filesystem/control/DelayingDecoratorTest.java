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
public class DelayingDecoratorTest
{
    Mockery context;

    private MockClock clock;

    private IEventProvider provider;

    private IEventProvider decorator;

    private static final long INTERVAL = 500;

    @BeforeMethod
    public void fixture()
    {
        clock = new MockClock(System.currentTimeMillis());
        context = new Mockery();
        provider = context.mock(IEventProvider.class);
        decorator = new DelayingDecorator(INTERVAL, provider, clock);
    }

    @Test
    public void eventsAreDelayed() throws Exception
    {
        context.checking(new Expectations()
            {
                {
                    Map<String, String> updates = new HashMap<String, String>();
                    updates.put("parameter", "update");
                    exactly(2).of(provider).getNewEvents();
                    will(returnValue(updates));
                }
            });

        assertThat(decorator.getNewEvents().size(), is(1));

        clock.setTime(clock.getTime() + INTERVAL - 1);
        assertThat(decorator.getNewEvents().size(), is(0));

        clock.setTime(clock.getTime() + INTERVAL);
        assertThat(decorator.getNewEvents().size(), is(1));

        context.assertIsSatisfied();
    }
}
