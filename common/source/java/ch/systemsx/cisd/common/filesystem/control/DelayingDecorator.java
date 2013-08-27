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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DelayingDecorator implements IEventProvider
{
    private final IEventProvider provider;

    private IClock clock;

    private final long interval;

    private long lastCall;

    public DelayingDecorator(long interval, IEventProvider provider)
    {
        this(interval, provider, new SystemClock());
    }

    DelayingDecorator(long interval, IEventProvider provider, IClock clock)
    {
        this.provider = provider;
        this.clock = clock;
        this.interval = interval;
        this.lastCall = 0;
    }

    @Override
    public Map<String, String> getNewEvents(Collection<String> parameters)
    {
        long currentTime = clock.getTime();
        if (currentTime - lastCall > interval)
        {
            lastCall = currentTime;
            return provider.getNewEvents(parameters);
        } else
        {
            return new HashMap<String, String>();
        }
    }
}
