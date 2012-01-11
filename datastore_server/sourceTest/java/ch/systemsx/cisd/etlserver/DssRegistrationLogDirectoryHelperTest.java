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
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileOperations;

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
        dssRegistrationLogDirHelper.initializeSubdirectories();
        DssRegistrationLogger logFile = dssRegistrationLogDirHelper.createNewLogFile("filename", "threadname", FileOperations.getInstance());
        assertTrue(logFile.getFile().exists());
        assertEquals("in-process", logFile.getFile().getParentFile().getName());
    }
}
