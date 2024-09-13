package ch.ethz.sis.afsserver.server.common;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.DefaultRepositorySelector;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.RootLogger;

public class ApacheLog4j1Configuration
{

    public static void reconfigureToUseAFSLogging()
    {
        final LoggerFactory loggerFactory = new LoggerFactory()
        {
            @Override
            public Logger makeNewLoggerInstance(String name)
            {
                return new NewLogger(name);
            }
        };

        LogManager.setRepositorySelector(new DefaultRepositorySelector(new Hierarchy(new RootLogger(Level.DEBUG))
        {
            @Override
            public Logger getLogger(String name)
            {
                return super.getLogger(name, loggerFactory);
            }

        }), null);
    }

    private static class NewLogger extends Logger
    {

        private final ch.ethz.sis.shared.log.Logger afsLogger;

        protected NewLogger(final String name)
        {
            super(name);
            afsLogger = ch.ethz.sis.shared.log.LogManager.getLogger(name);
        }

        @Override public void log(final Priority priority, final Object message, final Throwable t)
        {
            if (priority.equals(Level.TRACE) || priority.equals(Level.DEBUG))
            {
                trace(message, t);
            } else if (priority.equals(Level.INFO))
            {
                info(message, t);
            } else if (priority.equals(Level.WARN) || priority.equals(Level.ERROR) || priority.equals(Level.FATAL))
            {
                error(message, t);
            } else
            {
                throw new RuntimeException("Unsupported log level: " + priority);
            }
        }

        @Override public void log(final Priority priority, final Object message)
        {
            log(priority, message, null);
        }

        @Override public void log(final String callerFQCN, final Priority priority, final Object message, final Throwable t)
        {
            log(priority, message, t);
        }

        @Override public boolean isEnabledFor(final Priority priority)
        {
            if (priority.equals(Level.TRACE) || priority.equals(Level.DEBUG))
            {
                return afsLogger.isTraceEnabled();
            } else if (priority.equals(Level.INFO))
            {
                return afsLogger.isInfoEnabled();
            } else if (priority.equals(Level.WARN) || priority.equals(Level.ERROR) || priority.equals(Level.FATAL))
            {
                return afsLogger.isErrorEnabled();
            } else
            {
                throw new RuntimeException("Unsupported log level: " + priority);
            }
        }

        @Override public boolean isTraceEnabled()
        {
            return afsLogger.isTraceEnabled();
        }

        @Override public boolean isDebugEnabled()
        {
            return afsLogger.isTraceEnabled();
        }

        @Override public boolean isInfoEnabled()
        {
            return afsLogger.isInfoEnabled();
        }

        @Override public void trace(final Object message)
        {
            afsLogger.traceAccess(String.valueOf(message));
        }

        @Override public void trace(final Object message, final Throwable t)
        {
            afsLogger.traceAccess(String.valueOf(message), t);
        }

        @Override public void debug(final Object message)
        {
            trace(message);
        }

        @Override public void debug(final Object message, final Throwable t)
        {
            trace(message, t);
        }

        @Override public void info(final Object message)
        {
            afsLogger.info(String.valueOf(message));
        }

        @Override public void info(final Object message, final Throwable t)
        {
            afsLogger.info(String.valueOf(message), t);
        }

        @Override public void warn(final Object message)
        {
            error(message);
        }

        @Override public void warn(final Object message, final Throwable t)
        {
            error(message, t);
        }

        @Override public void error(final Object message)
        {
            afsLogger.catching(new RuntimeException(String.valueOf(message)));
        }

        @Override public void error(final Object message, final Throwable t)
        {
            afsLogger.catching(new RuntimeException(String.valueOf(message), t));
        }

        @Override public void fatal(final Object message)
        {
            error(message);
        }

        @Override public void fatal(final Object message, final Throwable t)
        {
            error(message, t);
        }
    }

}
