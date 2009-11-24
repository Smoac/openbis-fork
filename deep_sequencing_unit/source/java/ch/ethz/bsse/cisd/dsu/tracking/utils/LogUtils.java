/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.dsu.tracking.utils;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author Tomasz Pylak
 */
public class LogUtils
{
    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, LogUtils.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, LogUtils.class);

    public static void notify(EnvironmentFailureException ex)
    {
        String causeMsg =
                (ex.getCause() == null) ? "" : "Error cause: " + ex.getCause().getMessage();
        notificationLog
                .error("An environment exception occured why trying to send emails with changes.\n"
                        + "Check and correct the configuration.\n" + "Error details: "
                        + ex.getMessage() + "\n" + causeMsg);
    }

    public static void notify(Throwable ex)
    {
        notificationLog
                .error("An unexpected exception occured why trying to send emails with changes.\n"
                        + "Error details: " + ex.getMessage());
    }

    public static EnvironmentFailureException envErr(String message, Throwable exception)
    {
        String fullMsg =
                message + " The following exception has been thrown: " + exception.getMessage();
        return new EnvironmentFailureException(fullMsg, exception);
    }

    public static EnvironmentFailureException environmentError(String msgFormat, Object... params)
    {
        String fullMsg = String.format(msgFormat, params);
        return new EnvironmentFailureException(fullMsg);
    }
}
