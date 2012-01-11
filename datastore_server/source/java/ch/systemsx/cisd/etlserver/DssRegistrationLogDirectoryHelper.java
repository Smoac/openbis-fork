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
import java.util.GregorianCalendar;

import org.apache.commons.lang.time.DateFormatUtils;

/**
 * A utility class for working with the directory structure of the "log-registrations" directory
 * which stores log information about data set registrations.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DssRegistrationLogDirectoryHelper
{
    private final static String IN_PROCESS_DIR_NAME = "in-process";

    private final static String SUCCEEDED_DIR_NAME = "succeeded";

    private final static String FAILED_DIR_NAME = "failed";

    private final File dssRegistrationLogDir;

    public DssRegistrationLogDirectoryHelper(File dssRegistrationLogDir)
    {
        this.dssRegistrationLogDir = dssRegistrationLogDir;
    }

    /**
     * Initialize the subdirectory structure for the logs
     */
    public void initializeSubdirectories()
    {
        createDirectoryIfNecessary(getInProcessDir());
        createDirectoryIfNecessary(getSucceededDir());
        createDirectoryIfNecessary(getFailedDir());
    }

    /**
     * Return the directory used for files that are still in process.
     */
    public File getInProcessDir()
    {
        return new File(dssRegistrationLogDir, IN_PROCESS_DIR_NAME);
    }

    /**
     * Return the directory used for files that were successfully registered.
     */
    public File getSucceededDir()
    {
        return new File(dssRegistrationLogDir, SUCCEEDED_DIR_NAME);
    }

    /**
     * Return the directory used for files that were not successfully registered.
     */
    public File getFailedDir()
    {
        return new File(dssRegistrationLogDir, FAILED_DIR_NAME);
    }

    /**
     * Create a new log file located in the inProcessDir.
     */
    public File createNewLogFile(String name, String threadName)
    {
        String logFilename = generateLogFileName(name, threadName);
        return new File(getInProcessDir(), logFilename);
    }

    String generateLogFileName(String name, String threadName)
    {
        String sectionSeparator = "_";

        // The log file name is YYYY-MM-DD_HH-mm-ss-SSS_threadName_name.log
        StringBuilder logFilename = new StringBuilder();
        GregorianCalendar calendar = new GregorianCalendar();

        String dateSection = DateFormatUtils.ISO_DATE_FORMAT.format(calendar);
        logFilename.append(dateSection);
        logFilename.append(sectionSeparator);

        String timeSection = DateFormatUtils.format(calendar, "HH-mm-ss-SSS");
        logFilename.append(timeSection);
        logFilename.append(sectionSeparator);

        logFilename.append(threadName);
        logFilename.append(sectionSeparator);

        logFilename.append(name);
        logFilename.append(".log");

        return logFilename.toString();
    }

    private void createDirectoryIfNecessary(File dir)
    {
        if (false == dir.exists())
        {
            dir.mkdir();
        }
    }
}