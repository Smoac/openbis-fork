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
import org.apache.log4j.spi.LoggerFactory;

/**
 * This class is used to create loggers (using <code>log4j</code>).
 * 
 * @author Bernd Rinn
 */
public final class LogFactory
{

    private LogFactory()
    {
        // Can not be instantiated.
    }

    /**
     * @return The logger name for the given {@link LogCategory} and {@link Class}. It will contain the name of the
     *         <var>category</var>, followed by the canonical name of <var>clazz</var>.
     */
    public static String getLoggerName(LogCategory category, Class clazz)
    {
        return category.name() + "." + clazz.getCanonicalName();
    }

    /**
     * @return The logger for the given {@link LogCategory} and {@link Class}. The name of the logger will contain the
     *         name of the <var>category</var>, followed by the canonical name of <var>clazz</var>.
     */
    public static Logger getLogger(LogCategory category, Class clazz)
    {
        return Logger.getLogger(getLoggerName(category, clazz));
    }

    /**
     * Returns the logger for the given {@link LogCategory} and {@link Class}. The name of the logger will contain the
     * name of the <var>category</var>, followed by the canonical name of <var>clazz</var>.
     * <p>
     * The returned version of <code>Logger</code> checks the priority level before doing the logging output.
     * </p>
     */
    public final static Logger getExtendedLogger(LogCategory category, Class clazz) {
        return Logger.getLogger(getLoggerName(category, clazz), new ExtendedLoggerFactory());
    }
    
    ///////////////////////////////////////////////////////
    // Helper Classes
    ///////////////////////////////////////////////////////

    private final static class ExtendedLoggerFactory implements LoggerFactory {
        
        ///////////////////////////////////////////////////////
        // LoggerFactory
        ///////////////////////////////////////////////////////

        public Logger makeNewLoggerInstance(String name)
        {
            return new ExtendedLogger(name);
        }
    }
}
