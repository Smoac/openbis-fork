package ch.ethz.sis.afsserver.server.shuffling;

import ch.ethz.sis.shared.log.Logger;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

public class SimpleLogger implements ISimpleLogger
{

    private final Logger logger;

    public SimpleLogger(Logger logger)
    {
        this.logger = logger;
    }

    @Override public void log(final LogLevel level, final String message)
    {
        log(level, message, null);
    }

    @Override public void log(final LogLevel level, final String message, final Throwable throwableOrNull)
    {
        if (LogLevel.INFO.equals(level))
        {
            logger.info(message);
        } else if (LogLevel.WARN.equals(level))
        {
            logger.catching(new RuntimeException(message));
        } else if (LogLevel.ERROR.equals(level))
        {
            logger.catching(new RuntimeException(message, throwableOrNull));
        } else if (LogLevel.TRACE.equals(level))
        {
            logger.traceAccess(message);
        } else
        {
            logger.info(message, throwableOrNull);
            throw new UnsupportedOperationException("Unsupported log level: " + level);
        }
    }
}
