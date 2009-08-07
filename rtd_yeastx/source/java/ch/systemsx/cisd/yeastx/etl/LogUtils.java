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
import ch.systemsx.cisd.common.mail.IMailClient;

/**
 * @author Tomasz Pylak
 */
class LogUtils
{
    private static final String ERROR_NOTIFICATION_EMAIL_SUBJECT =
            "[openBIS] problems with datasets upload in directory: %s";

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, LogUtils.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, LogUtils.class);

    private final File loggingDir;

    private final StringBuffer errorMessages;

    public LogUtils(File loggingDir)
    {
        this.loggingDir = loggingDir;
        this.errorMessages = new StringBuffer();
    }

    /** Logs errors about one dataset mapping. Uses user log file and email notification. */
    public void datasetMappingError(DataSetMappingInformation mapping, String errorMessageFormat,
            Object... arguments)
    {
        datasetMappingError(mapping.getFileName(), errorMessageFormat, arguments);
    }

    /**
     * Logs exception which occurred while storing one dataset file. Uses user log file and email
     * notification.
     */
    public void datasetFileError(File dataset, Throwable exception)
    {
        String message = exception.getMessage();
        if (message == null)
        {
            message = exception.toString();
        }
        datasetFileError(dataset, message);
    }

    /** Logs errors about one dataset file. Uses user log file and email notification. */
    public void datasetFileError(File dataset, String errorMessageFormat, Object... arguments)
    {
        datasetMappingError(dataset.getName(), errorMessageFormat, arguments);
    }

    private void datasetMappingError(String fileName, String errorMessageFormat,
            Object... arguments)
    {
        String errorMessage = String.format(errorMessageFormat, arguments);
        error(fileName + " - cannot upload the file: " + errorMessage);
    }

    /**
     * Logs an error about the syntax of the mapping file. Uses user log file and email
     * notification.
     */
    public void mappingFileError(File mappingFile, String messageFormat, Object... arguments)
    {
        String errorMessage = String.format(messageFormat, arguments);
        error("No datasets could be processed, because there is an error in the mapping file "
                + mappingFile.getName() + ": " + errorMessage);
    }

    /** Uses user log file and email notification to log an error. */
    public void error(String messageFormat, Object... arguments)
    {
        logError(loggingDir, messageFormat, arguments);
        appendNotification(messageFormat, arguments);
    }

    /** Uses user log file and email notification to log a warning. */
    public void warning(String messageFormat, Object... arguments)
    {
        logWarning(loggingDir, messageFormat, arguments);
        appendNotification(messageFormat, arguments);
    }

    private void appendNotification(String messageFormat, Object... arguments)
    {
        errorMessages.append(String.format(messageFormat, arguments));
        errorMessages.append("\r\n");
    }

    /** has to be called at the end to send all notifications in one email */
    public void sendNotificationsIfNecessary(IMailClient mailClient, String notificationEmailOrNull)
    {
        if (notificationEmailOrNull != null && errorMessages.length() > 0)
        {
            sendErrorMessage(mailClient, notificationEmailOrNull);
        }
    }

    private void sendErrorMessage(IMailClient mailClient, String notificationEmail)
    {
        String subject = String.format(ERROR_NOTIFICATION_EMAIL_SUBJECT, loggingDir.getName());
        mailClient.sendMessage(subject, createErrorNotificationContent(), null, notificationEmail);
    }

    private String createErrorNotificationContent()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Hello,\n");
        sb.append("This email has been generated automatically by openBIS.\n\n");
        sb.append("The upload of some datasets from '");
        sb.append(loggingDir.getName());
        sb.append("' directory has failed.\nThere are following errors:\n");
        sb.append(errorMessages);
        sb.append("\n");
        sb.append("If you are not sure how to correct the errors and you cannot find the answer"
                + " in the documentation, ask for help your openBIS administrator.\n\n");
        sb.append("Kind regards,\n");
        sb.append("   openBIS Team");
        return sb.toString();
    }

    /** Adds an entry about an error to the user log file. Does not send an email. */
    private static void logError(File loggingDir, String messageFormat, Object... arguments)
    {
        String message = createUserMessage("ERROR", messageFormat, arguments);
        notifyUserByLogFile(loggingDir, message);
    }

    /** Adds an entry about a warning to the user log file. Does not send an email. */
    private static void logWarning(File loggingDir, String messageFormat, Object... arguments)
    {
        String message = createUserMessage("WARNING", messageFormat, arguments);
        notifyUserByLogFile(loggingDir, message);
    }

    private static void notifyUserByLogFile(File loggingDir, String message)
    {
        OutputStream output;
        try
        {
            output = new FileOutputStream(getUserLogFile(loggingDir), true);
            IOUtils.writeLines(Arrays.asList(message), "", output);
        } catch (IOException ex)
        {
            adminError("Cannot notify a user because " + ex.getMessage() + "\n The message was: "
                    + message);
        }
    }

    private static String createUserMessage(String messageKind, String messageFormat,
            Object... arguments)
    {
        String now = new Date().toString();
        String message = now + " " + messageKind + ": " + format(messageFormat, arguments) + "\r\n";
        return message;
    }

    private static File getUserLogFile(File loggingDir)
    {
        return new File(loggingDir, ConstantsYeastX.USER_LOG_FILE);
    }

    public static void adminError(String messageFormat, Object... arguments)
    {
        notificationLog.error(format(messageFormat, arguments));
    }

    public static void adminWarn(String messageFormat, Object... arguments)
    {
        operationLog.warn(format(messageFormat, arguments));
    }

    public static void adminInfo(String messageFormat, Object... arguments)
    {
        operationLog.info(format(messageFormat, arguments));
    }

    private static String format(String messageFormat, Object... arguments)
    {
        return String.format(messageFormat, arguments);
    }

    public static boolean isUserLog(File file)
    {
        return file.getName().equals(ConstantsYeastX.USER_LOG_FILE);
    }

    public static boolean deleteUserLog(File loggingDir)
    {
        File file = getUserLogFile(loggingDir);
        if (file.isFile())
        {
            return file.delete();
        }
        return true;
    }
}
