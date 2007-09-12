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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import ch.systemsx.cisd.common.logging.LogCategory;

/**
 * A class that allows to monitor the log for unit tests.
 * 
 * @author Bernd Rinn
 */
public final class LogMonitoringAppender extends AppenderSkeleton
{

    private static Map<LogMonitoringAppender, String> appenderMap = new HashMap<LogMonitoringAppender, String>();

    private final String messagePart;

    private LogMonitoringAppender(String messagePart)
    {
        this.messagePart = messagePart;
    }

    private boolean logHappened = false;

    /**
     * Creates an appender that monitors for <var>messagePart</var> and adds it to the {@link Logger} for
     * <code>category</code> and <code>clazz</code>.
     * 
     * @return The created appender.
     */
    public static synchronized LogMonitoringAppender addAppender(LogCategory category, String messagePart)
    {
        final LogMonitoringAppender appender = new LogMonitoringAppender(messagePart);
        final String loggerName = category.name();
        Logger.getLogger(loggerName).addAppender(appender);
        appenderMap.put(appender, loggerName);
        return appender;
    }

    /**
     * Removes the given <var>appender</var>.
     */
    public static synchronized void removeAppender(LogMonitoringAppender appender)
    {
        final String loggerName = appenderMap.get(appender);
        if (loggerName != null)
        {
            Logger.getLogger(loggerName).removeAppender(appender);
            appenderMap.remove(appender);
        } else
        {
            // This means that the caller tries to remove the appender twice - nothing to do here really.
        }
    }

    private String getThrowableStr(LoggingEvent event)
    {
        final ThrowableInformation info = event.getThrowableInformation();
        if (info == null)
        {
            return "";
        } else
        {
            return info.getThrowableStrRep()[0];
        }
    }

    @Override
    protected void append(LoggingEvent event)
    {
        if (event.getMessage().toString().contains(messagePart) || getThrowableStr(event).contains(messagePart))
        {
            logHappened = true;
        }
    }

    @Override
    public void close()
    {
        // Nothing to do here.
    }

    @Override
    public boolean requiresLayout()
    {
        return false;
    }

    public void verifyLogHasHappened()
    {
        assert logHappened : "Following log snippet has been missed: " + messagePart;
    }
}
