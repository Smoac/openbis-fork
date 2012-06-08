/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.FastDateFormat;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;

/**
 * Interface for logging into the dss registration log.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssRegistrationLogger
{
    private File file;

    private final DssRegistrationLogDirectoryHelper helper;

    private final IFileOperations fileOperations;

    /**
     * gets the handle to the file where this logger is logging
     */
    public File getFile()
    {
        return file;
    }

    public DssRegistrationLogger(File file, DssRegistrationLogDirectoryHelper helper, IFileOperations fileOperations)
    {
        super();
        this.file = file;
        this.helper = helper;
        this.fileOperations = fileOperations;
    }

    /**
     * Change the state to Failed
     */
    public void registerFailure()
    {
        moveToDir(helper.getFailedDir());
    }

    /**
     * Change the state to Succeeded
     */
    public void registerSuccess()
    {
        moveToDir(helper.getSucceededDir());
    }

    private static FastDateFormat simpleNoISODateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Logs a message.
     */
    public void log(String message)
    {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(simpleNoISODateFormat.format(new Date()));
        logMessage.append(" ");
        logMessage.append(message);
        logMessage.append("\n");
        FileUtilities.appendToFile(file, logMessage.toString(), false);
    }

    /**
     * Logs class and message of exception.
     */
    public void log(Throwable ex, String message)
    {
        log(message+": " + ex.toString());
    }

    /**
     * Logs a message, truncating the content if it exceeds the length limit.
     */
    public void logTruncatingIfNecessary(String message)
    {
        String truncatedMessage = message;
        if (message.length() > 100)
        {
            truncatedMessage = message.substring(0, 96) + "...";
        }
        log(truncatedMessage);
    }

    private void moveToDir(File dir)
    {
        assert dir.isDirectory();

        fileOperations.move(file, dir);
        file = new File(dir, file.getName());
    }

    /**
     * Log either success or the failure with error details.
     * Registers success only if <code>encounteredErrors</code> is empty.
     */
    public void logDssRegistrationResult(List<Throwable> encounteredErrors)
    {
        if (0 == encounteredErrors.size())
        {
            registerSuccess();
        } else
        {
            // Construct a message to add to the registration log
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("Registration failed with the following errors\n");
            for (Throwable error : encounteredErrors)
            {
                logMessage.append("\t");
                logMessage.append(error.toString());
            }
            log(logMessage.toString());
            registerFailure();
        }
    }
}
