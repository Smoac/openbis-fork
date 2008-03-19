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

package ch.systemsx.cisd.cifex.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.util.Log4jConfigListener;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;

/**
 * Extension of Spring's <code>ContextLoaderListener</code> which first initializes logging via
 * {@link LogInitializer#init()}.
 * 
 * @see Log4jConfigListener
 * @see ServletContextListener
 * @author Christian Ribeaud
 */
public final class CIFEXContextLoaderListener extends ContextLoaderListener
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CIFEXContextLoaderListener.class);

    private final static void printBuildAndEnvironmentInfo()
    {
        for (final String line : BuildAndEnvironmentInfo.INSTANCE.getEnvironmentInfo())
        {
            operationLog.info(line);
        }
    }

    //
    // ContextLoaderListener
    //

    @Override
    public final void contextInitialized(final ServletContextEvent event)
    {
        registerDefaultUncaughtExceptionHandler();
        LogInitializer.init();
        // Must be call after having initialized the log as it uses itself an operation log.
        printBuildAndEnvironmentInfo();
        super.contextInitialized(event);
    }

    private void registerDefaultUncaughtExceptionHandler()
    {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
            {
                public void uncaughtException(Thread thread, Throwable th)
                {
                    operationLog.error(String.format("An unexpected error occured in thread [%s].",
                            thread.getName()), th);
                }
            });
    }

}
