package ch.ethz.sis.afsserver.server.log;

import org.apache.commons.logging.Log;

import ch.ethz.sis.shared.log.LogManager;
import ch.ethz.sis.shared.log.Logger;

public class LogApacheCommonsLogging implements Log
{

    private final Logger logger;

    public LogApacheCommonsLogging(String name) throws Exception
    {
        this.logger = LogManager.getLogger(Class.forName(name));
    }

    @Override public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }

    @Override public boolean isErrorEnabled()
    {
        return logger.isErrorEnabled();
    }

    @Override public boolean isFatalEnabled()
    {
        // TODO
        return false;
    }

    @Override public boolean isInfoEnabled()
    {
        return logger.isInfoEnabled();
    }

    @Override public boolean isTraceEnabled()
    {
        // TODO
        return false;
    }

    @Override public boolean isWarnEnabled()
    {
        return logger.isWarnEnabled();
    }

    @Override public void trace(final Object o)
    {
        trace(o, null);
    }

    @Override public void trace(final Object o, final Throwable throwable)
    {
        // TODO
    }

    @Override public void debug(final Object o)
    {
        debug(o, null);
    }

    @Override public void debug(final Object o, final Throwable throwable)
    {
        logger.debug((String) o);
    }

    @Override public void info(final Object o)
    {
        info(o, null);
    }

    @Override public void info(final Object o, final Throwable throwable)
    {
        logger.info((String) o);
    }

    @Override public void warn(final Object o)
    {
        warn(o, null);
    }

    @Override public void warn(final Object o, final Throwable throwable)
    {
        logger.warn((String) o);
    }

    @Override public void error(final Object o)
    {
        error(o, null);
    }

    @Override public void error(final Object o, final Throwable throwable)
    {
        logger.error((String) o);
    }

    @Override public void fatal(final Object o)
    {
        // TODO
    }

    @Override public void fatal(final Object o, final Throwable throwable)
    {
        // TODO
    }
}
