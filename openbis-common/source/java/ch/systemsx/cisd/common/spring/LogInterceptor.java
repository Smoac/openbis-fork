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

package ch.systemsx.cisd.common.spring;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;

/**
 * Interceptor for objects which provide their own logger.
 * 
 * @author Franz-Josef Elmer
 */
public final class LogInterceptor implements MethodInterceptor, Serializable
{
    private static final class InvocationLoggerContext implements IInvocationLoggerContext
    {
        private final String sessionToken;

        boolean invocationSuccessful;

        long elapsedTime;

        InvocationLoggerContext(String sessionTokenOrNull)
        {
            this.sessionToken = sessionTokenOrNull;
        }

        @Override
        public String tryToGetSessionToken()
        {
            return sessionToken;
        }

        @Override
        public boolean invocationWasSuccessful()
        {
            return invocationSuccessful;
        }

        @Override
        public long getElapsedTime()
        {
            return elapsedTime;
        }

    }

    private static final long serialVersionUID = 1L;

    private final Stopwatch timer = new Stopwatch();

    //
    // MethodInterceptor
    //

    @Override
    public final Object invoke(final MethodInvocation invocation) throws Throwable
    {
        final Object wrappedObject = invocation.getThis();
        if (wrappedObject instanceof IInvocationLoggerFactory<?> == false)
        {
            throw new IllegalArgumentException("Wrapped object isn't a "
                    + IInvocationLoggerFactory.class.getName() + ": " + wrappedObject);
        }
        final IInvocationLoggerFactory<?> loggerFactory =
                (IInvocationLoggerFactory<?>) wrappedObject;
        final Object[] arguments = invocation.getArguments();
        String sessionTokenOrNull = tryToGetSessionToken(arguments);
        InvocationLoggerContext invocationLoggerContext =
                new InvocationLoggerContext(sessionTokenOrNull);
        final Object logger = loggerFactory.createLogger(invocationLoggerContext);
        try
        {
            timer.start();
            final Object result = invocation.proceed();
            invocationLoggerContext.invocationSuccessful = true;
            return result;
        } catch (final Throwable th)
        {
            logError(invocation, wrappedObject, th);
            throw th;
        } finally
        {
            timer.stop();
            invocationLoggerContext.elapsedTime = timer.getTimeElapsed();
            final Method method = invocation.getMethod();
            try
            {
                method.invoke(logger, arguments);
            } catch (IllegalArgumentException ex)
            {
                // ignored if 'method' isn't a method of 'logger'
            }
        }
    }

    private void logError(final MethodInvocation invocation, final Object wrappedObject,
            final Throwable th)
    {
        final Class<? extends Object> clazz = wrappedObject.getClass();
        final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, clazz);
        String errorMsg =
                String.format("An exception has occurred while processing method: '%s'.",
                        MethodUtils.describeMethod(invocation.getMethod()));
        try
        {
            operationLog.error(errorMsg, th);
        } catch (Exception ex)
        {
            operationLog.error(errorMsg);
            operationLog.error("It was not possible to log the exception which caused the problem",
                    ex);
        }
    }

    private String tryToGetSessionToken(final Object[] arguments)
    {
        if (arguments.length == 0)
        {
            return null;
        }
        Object firstArgument = arguments[0];
        return firstArgument instanceof String ? (String) firstArgument : null;
    }

    //
    // Helper classes
    //

    private final static class Stopwatch implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private long start = 0;

        private long stop = 0;

        void start()
        {
            start = System.currentTimeMillis();
        }

        void stop()
        {
            stop = System.currentTimeMillis();
        }

        long getTimeElapsed()
        {
            return stop - start;

        }
    }
}
