/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.dbmigration.logging;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author pkupczyk
 */
public class DbConnectionLogAppender extends DailyRollingFileAppender
{

    @Override
    public void append(LoggingEvent event)
    {
        if (DbConnectionLogConfiguration.getInstance().isDbConnectionsSeparateLogFileEnabled())
        {
            super.append(new LoggingEvent(event.getFQNOfLoggerClass(),
                    event.getLogger(),
                    event.getTimeStamp(),
                    event.getLevel(),
                    event.getMessage(),
                    Thread.currentThread().getName().replaceAll("\\s", "_"),
                    event.getThrowableInformation(),
                    event.getNDC(),
                    event.getLocationInformation(),
                    event.getProperties()));
        }
    }
}
