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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author Tomasz Pylak
 */
class LogUtils
{
    private static final String USER_LOG_FILE = "error-log.txt";

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, LogUtils.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, LogUtils.class);

    public static void error(File loggingDir, String messageFormat, Object... arguments)
    {
        notifyUser(loggingDir, "ERROR", messageFormat, arguments);
        adminError(messageFormat, arguments);
    }

    public static void warn(File loggingDir, String messageFormat, Object... arguments)
    {
        notifyUser(loggingDir, "WARNING", messageFormat, arguments);
        adminWarn(messageFormat, arguments);
    }

    private static void notifyUser(File loggingDir, String messageKind, String messageFormat,
            Object... arguments)
    {
        String now = new Date().toString();
        String message = now + " " + messageKind + ": " + format(messageFormat, arguments);
        OutputStream output;
        try
        {
            output = new FileOutputStream(getUserLogFile(loggingDir), true);
            IOUtils.writeLines(Arrays.asList(message), "\n", output);
        } catch (IOException ex)
        {
            adminError("Cannot notify a user: " + ex.getMessage());
        }
    }

    private static File getUserLogFile(File loggingDir)
    {
        return new File(loggingDir, USER_LOG_FILE);
    }

    public static void adminError(String messageFormat, Object... arguments)
    {
        notificationLog.error(format(messageFormat, arguments));
    }

    public static void adminWarn(String messageFormat, Object... arguments)
    {
        operationLog.warn(format(messageFormat, arguments));
    }

    public static void info(String messageFormat, Object... arguments)
    {
        operationLog.info(format(messageFormat, arguments));
    }

    private static String format(String messageFormat, Object... arguments)
    {
        return String.format(messageFormat, arguments);
    }

    public static boolean isUserLog(File file)
    {
        return file.getName().equals(USER_LOG_FILE);
    }

    public static void deleteUserLog(File loggingDir)
    {
        File file = getUserLogFile(loggingDir);
        if (file.isFile())
        {
            file.delete();
        }
    }
}
