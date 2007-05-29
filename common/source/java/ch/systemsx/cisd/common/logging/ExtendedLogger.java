/*
 * Copyright 2007 ETH Zuerich, CISD
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

import org.apache.log4j.Logger;

/**
 * This <code>Logger</code> extension checks the priority level before making the logging output.
 * 
 * @author Christian Ribeaud
 */
public final class ExtendedLogger extends Logger
{

    public ExtendedLogger(String name)
    {
        super(name);
    }

    ///////////////////////////////////////////////////////
    // Logger
    ///////////////////////////////////////////////////////

    @Override
    public final void info(Object message)
    {
        if (isInfoEnabled())
        {
            super.info(message);
        }
    }

    @Override
    public final void info(Object message, Throwable t)
    {
        if (isInfoEnabled())
        {
            super.info(message, t);
        }
    }

    @Override
    public final void debug(Object message)
    {
        if (isDebugEnabled())
        {
            super.debug(message);
        }
    }

    @Override
    public final void debug(Object message, Throwable t)
    {
        if (isDebugEnabled())
        {
            super.debug(message, t);
        }
    }
}
