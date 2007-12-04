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
import org.apache.log4j.Priority;

/**
 * A {@link ISimpleLogger} that is based on log4j.
 * 
 * @author Bernd Rinn
 */
public class Log4jSimpleLogger implements ISimpleLogger
{
    private final Priority log4jOverridePriorityOrNull;

    private final Logger log4jLogger;

    private static final Priority toLog4jPriority(Level level)
    {
        switch (level)
        {
            case ERROR:
                return org.apache.log4j.Level.ERROR;
            case WARN:
                return org.apache.log4j.Level.WARN;
            case INFO:
                return org.apache.log4j.Level.INFO;
            case DEBUG:
                return org.apache.log4j.Level.DEBUG;
            default:
                throw new IllegalArgumentException("Unknown log level " + level);
        }
    }

    /**
     * Creates a logger that uses <var>log4jLogger<var> to do the real logging.
     * 
     * @param log4jLogger The log4j logger to use.
     * @param log4jOverridePriorityOrNull If not <code>null</code>, use this log level instead of the one provided to
     *            the {@link ISimpleLogger#log(ch.systemsx.cisd.common.logging.ISimpleLogger.Level, String)}.
     */
    public Log4jSimpleLogger(Logger log4jLogger, Priority log4jOverridePriorityOrNull)
    {
        this.log4jOverridePriorityOrNull = log4jOverridePriorityOrNull;
        this.log4jLogger = log4jLogger;
    }

    /**
     * Creates a logger that uses <var>log4jLogger<var> to do the real logging.
     * 
     * @param log4jLogger The log4j logger to use.
     */
    public Log4jSimpleLogger(Logger log4jLogger)
    {
        this(log4jLogger, null);
    }

    public void log(Level level, String message)
    {
        if (log4jOverridePriorityOrNull != null)
        {
            log4jLogger.log(log4jOverridePriorityOrNull, message);
        } else
        {
            log4jLogger.log(toLog4jPriority(level), message);
        }
    }

}
