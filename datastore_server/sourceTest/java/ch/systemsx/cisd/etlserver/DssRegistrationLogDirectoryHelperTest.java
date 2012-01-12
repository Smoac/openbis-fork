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

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DssRegistrationLogDirectoryHelperTest extends AbstractFileSystemTestCase
{
    private DssRegistrationLogDirectoryHelper dssRegistrationLogDirHelper;

    @BeforeMethod
    @Override
    public void setUp() throws IOException
    {
        super.setUp();
        dssRegistrationLogDirHelper = new DssRegistrationLogDirectoryHelper(workingDirectory);
    }

    @Test
    public void testLogFilenameGeneration()
    {
        String logFilename = dssRegistrationLogDirHelper.generateLogFileName("filename", "threadname");
        Pattern p = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}-\\d{3}_(.+)_(.+).log$");
        Matcher m = p.matcher(logFilename);
        assertTrue("Log filename does not match expected pattern", m.matches());
        String threadnameString = m.group(1);
        String filenameString = m.group(2);

        assertEquals("threadname", threadnameString);
        assertEquals("filename", filenameString);
    }

    @Test
    public void testFileCreation()
    {
        DssRegistrationLogger logFile = createLogFile();
        assertTrue(logFile.getFile().exists());
        assertEquals("in-process", logFile.getFile().getParentFile().getName());
    }

    @Test
    public void testLogging()
    {
        DssRegistrationLogger logFile = createLogFile();
        logFile.log("1: The message");
        logFile.log("2: The message");
        List<String> contents = FileUtilities.loadToStringList(logFile.getFile());
        assertTrue(contents.get(0), Pattern.matches("^\\d{2}:\\d{2}:\\d{2} 1: The message$", contents.get(0)));
        assertTrue(contents.get(1), Pattern.matches("^\\d{2}:\\d{2}:\\d{2} 2: The message$", contents.get(1)));
    }

    @Test
    public void testLoggingWithTruncating()
    {
        DssRegistrationLogger logFile = createLogFile();
        logFile.logTruncatingIfNecessary("This is a very long string, in fact, a string that is longer than the limit for the length of an allowed string");
        String contents = FileUtilities.loadToString(logFile.getFile());
        assertTrue(contents,
                Pattern.matches(
                        "^\\d{2}:\\d{2}:\\d{2} This is a very long string, in fact, a string that is longer than the limit for the length of an...\\n$",
                        contents));
    }

    private DssRegistrationLogger createLogFile()
    {
        dssRegistrationLogDirHelper.initializeSubdirectories();
        return dssRegistrationLogDirHelper.createNewLogFile("filename", "threadname", FileOperations.getInstance());
    }

}
