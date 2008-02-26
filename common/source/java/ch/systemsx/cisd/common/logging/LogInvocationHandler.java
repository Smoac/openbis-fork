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

package ch.systemsx.cisd.common.logging;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Invocation handler used to log invocations.
 * 
 * @author Franz-Josef Elmer
 */
public final class LogInvocationHandler implements InvocationHandler
{
    private final Object object;

    private final String name;

    private final Level defaultLogLevel;

    private final Class<?> classUsedToNameLogger;

    /**
     * Creates a new instance.
     * 
     * @param object Object whose invocations should be logged.
     * @param name Meaningful name of <code>object</code>. Will be used in the log message.
     * @param logLevel The log level to use for normal (successful) events.
     * @param classUsedToNameLogger Class used to specify the name of the logger.
     */
    public LogInvocationHandler(Object object, String name, Level logLevel, Class<?> classUsedToNameLogger)
    {
        this.object = object;
        this.name = name;
        this.defaultLogLevel = logLevel;
        this.classUsedToNameLogger = classUsedToNameLogger;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        long time = System.currentTimeMillis();
        Throwable throwable = null;
        try
        {
            return method.invoke(object, args);
        } catch (InvocationTargetException e)
        {
            throwable = e.getCause();
            throw throwable;
        } catch (UndeclaredThrowableException e)
        {
            throwable = e.getCause();
            throw throwable;
        } catch (Throwable t)
        {
            throwable = t;
            throw t;
        } finally
        {
            final Level logLevel = getLogLevel(method);
            final Logger logger = createLogger(method);
            if (throwable != null || logger.isEnabledFor(logLevel))
            {
                final StringBuilder builder = new StringBuilder(throwable == null ? "Successful" : "Failed");
                builder.append(" invocation of ");
                builder.append(name).append('.').append(method.getName()).append('(');
                if (args != null)
                {
                    for (int i = 0; i < args.length; i++)
                    {
                        builder.append(args[i]);
                        if (i < args.length - 1)
                        {
                            builder.append(", ");
                        }
                    }
                }
                builder.append(") took ").append(System.currentTimeMillis() - time).append(" msec");
                if (throwable == null)
                {
                    logger.log(logLevel, builder.toString());
                } else
                {
                    logger.error(builder.toString(), throwable);
                }
            }
        }
    }

    private Level getLogLevel(Method method)
    {
        final LogAnnotation annotation = method.getAnnotation(LogAnnotation.class);
        if (annotation == null)
        {
            return Level.DEBUG;
        } else if (annotation.logLevel().equals(LogLevel.UNDEFINED))
        {
            return defaultLogLevel;
        } else
        {
            return Log4jSimpleLogger.toLog4jPriority(annotation.logLevel());
        }
    }

    private Logger createLogger(Method method)
    {
        final LogAnnotation annotation = method.getAnnotation(LogAnnotation.class);
        final LogCategory logCategory = (annotation == null) ? LogCategory.OPERATION : annotation.logCategory();
        return LogFactory.getLogger(logCategory, classUsedToNameLogger);
    }
}