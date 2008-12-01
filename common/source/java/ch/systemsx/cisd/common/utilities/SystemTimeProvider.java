/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import java.io.Serializable;

/**
 * Implementation of time provider based on {@link System#currentTimeMillis()}.
 *
 * @author Franz-Josef Elmer
 */
public class SystemTimeProvider implements ITimeProvider, Serializable
{
    private static final long serialVersionUID = 1L;
    
    /** The one and only one instance of this class. */
    public static final ITimeProvider SYSTEM_TIME_PROVIDER = new SystemTimeProvider();
    
    private SystemTimeProvider()
    {
    }
    
    public long getTimeInMilliseconds()
    {
        return System.currentTimeMillis();
    }

}
