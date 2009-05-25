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

package ch.systemsx.cisd.base.tests;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;

/**
 * An <code>abstract</code> test case which accesses the file system.
 * <p>
 * It constructs an appropriate working directory which is test class specific.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractFileSystemTestCase extends AssertJUnit
{
    protected static final String UNIT_TEST_WORKING_DIRECTORY = "unit-test-wd";

    protected static final String TARGETS_DIRECTORY = "targets";

    private static final File UNIT_TEST_ROOT_DIRECTORY =
            new File(TARGETS_DIRECTORY + File.separator + UNIT_TEST_WORKING_DIRECTORY);

    protected final File workingDirectory;

    private final boolean cleanAfterMethod;

    protected AbstractFileSystemTestCase()
    {
        this(true);
    }

    protected AbstractFileSystemTestCase(final boolean cleanAfterMethod)
    {
        workingDirectory = createWorkingDirectory();
        this.cleanAfterMethod = cleanAfterMethod;
    }

    private final File createWorkingDirectory()
    {
        final File directory = new File(UNIT_TEST_ROOT_DIRECTORY, getClass().getName());
        directory.mkdirs();
        directory.deleteOnExit();
        return directory;
    }

    @BeforeMethod
    public void setUp() throws IOException
    {
        FileUtils.deleteDirectory(workingDirectory);
        workingDirectory.mkdir();
        assertTrue(workingDirectory.isDirectory() && workingDirectory.listFiles().length == 0);
    }

    @AfterClass
    public void afterClass() throws IOException
    {
        if (cleanAfterMethod == false)
        {
            return;
        }
        FileUtils.deleteDirectory(workingDirectory);
    }
}
