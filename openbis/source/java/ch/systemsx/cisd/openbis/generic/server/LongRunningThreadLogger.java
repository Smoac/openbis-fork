/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.LongRunningThreadInterceptor.InvocationStart;

public class LongRunningThreadLogger implements Runnable
{
    private final static Logger logger =
            LogFactory.getLogger(LogCategory.ACCESS, LongRunningThreadLogger.class);

    private final LongRunningThreadLogConfiguration config;

    private final LongRunningThreadInterceptor interceptor;

    public LongRunningThreadLogger(LongRunningThreadInterceptor interceptor, LongRunningThreadLogConfiguration config)
    {
        this.interceptor = interceptor;
        this.config = config;
    }

    @Override
    public void run()
    {
        while (true)
        {
            Set<InvocationStart> invocations = interceptor.getInvocationsRunningLongerThan(config.maxValidInvocationLength());
            if (config.isLoggingEnabled())
            {
                log(invocations);
            }

            try
            {
                Thread.sleep(config.logInterval());
            } catch (InterruptedException ex)
            {
            }
        }
    }

    private void log(Set<InvocationStart> invocations)
    {
        if (invocations.size() != 0)
        {
            logger.warn("Long running invocation report at " + format(System.currentTimeMillis()));
            for (InvocationStart invocation : invocations)
            {
                Method method = invocation.invocation.getMethod();
                long duration = (System.currentTimeMillis() - invocation.startTime) / 1000;
                logger.warn("Thread " + invocation.threadName + " has been executing " + method.getDeclaringClass().getCanonicalName() + "."
                        + method.getName() + "(" + argumentsToString(invocation.invocation.getArguments()) + ") for "
                        + String.format("%d:%02d:%02d", duration / 3600, (duration % 3600) / 60, (duration % 60)));

            }
            logger.info("-----------");
        }
    }

    private String argumentsToString(Object[] arguments)
    {
        if (arguments.length == 0)
        {
            return "";
        }
        String args = "";
        for (Object argument : arguments)
        {
            if (argument != null)
            {
                args += ", " + argument.toString();
            } else
            {
                args += ", null";
            }
        }
        return args.substring(2);
    }

    private String format(long time)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date(time));
    }
}
