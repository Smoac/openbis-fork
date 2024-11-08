/*
 * Copyright ETH 2018 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.shared.log.log4j2;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;

class Log4JLogger extends ExtendedLoggerWrapper implements ch.ethz.sis.shared.log.Logger
{

    private final String FQCN;

    Log4JLogger(final Logger logger)
    {
        super((AbstractLogger) logger, logger.getName(), logger.getMessageFactory());
        FQCN = this.getClass().getName();
    }

    @Override
    public void traceAccess(String message, Object... args)
    {
        if(this.getLevel() == Level.TRACE)
        {
            traceAccess(message, null, args);
        }
    }

    @Override public void traceAccess(String message, Throwable ex, Object... args)
    {
        if(this.getLevel() == Level.TRACE)
        {
            this.logMessage(FQCN,
                    Level.TRACE,
                    ENTRY_MARKER,
                    entryMsg(message, args),
                    ex);
        }
    }

    @Override
    public <R> R traceExit(R result)
    {
        if(this.getLevel() == Level.TRACE)
        {
            this.logMessage(FQCN,
                    Level.TRACE,
                    EXIT_MARKER,
                    exitMsg((String) null, result),
                    (Throwable) null);
        }
        return result;
    }

    @Override
    public void catching(Throwable ex)
    {
        this.logMessage(FQCN,
                Level.ERROR,
                CATCHING_MARKER,
                catchingMsg(ex),
                ex);
    }

    @Override
    public <T extends Throwable> T throwing(T ex)
    {
        this.logMessage(FQCN,
                Level.ERROR,
                THROWING_MARKER,
                throwingMsg(ex),
                ex);
        return ex;
    }

    @Override public void debug(String message, Object... args)
    {
        if(this.getLevel() == Level.DEBUG || this.getLevel() == Level.TRACE)
        {
            this.logMessage(FQCN,
                    Level.DEBUG,
                    null,
                    logger.getMessageFactory().newMessage(message, args),
                    (Throwable) null);
        }
    }

    @Override
    public void info(String message, Object... args)
    {
        info(message, null, args);
    }

    @Override public void info(String message, Throwable ex, Object... args)
    {
        this.logMessage(FQCN,
                Level.INFO,
                null,
                logger.getMessageFactory().newMessage(message, args),
                ex);
    }

    @Override public void warn(String message, Object... args)
    {
        if(this.getLevel() == Level.WARN || this.getLevel() == Level.INFO || this.getLevel() == Level.DEBUG || this.getLevel() == Level.TRACE)
        {
            this.logMessage(FQCN,
                    Level.WARN,
                    null,
                    logger.getMessageFactory().newMessage(message, args),
                    (Throwable) null);
        }
    }
}
