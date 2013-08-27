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

import java.util.UUID;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * @author anttil
 */
public class Matchers
{
    static TypeSafeMatcher<IEventFilter> eventFilterAccepting(final String value)
    {
        return new TypeSafeMatcher<IEventFilter>()
            {

                @Override
                public void describeTo(Description description)
                {
                    description.appendText("IEventFilter accepting '" + value + "-*'");
                }

                @Override
                public boolean matchesSafely(IEventFilter filter)
                {
                    return filter.accepts(value + "-" + UUID.randomUUID().toString());
                }
            };
    }
}
