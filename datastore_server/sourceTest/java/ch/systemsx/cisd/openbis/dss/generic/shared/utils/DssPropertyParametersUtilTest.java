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

package ch.systemsx.cisd.openbis.dss.generic.shared.utils;

import java.io.File;
import java.util.Properties;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.IFileOperations;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DssPropertyParametersUtil.class)
public class DssPropertyParametersUtilTest extends AssertJUnit
{
    private static final File EMPTY_TEST_FILE = new File(DssPropertyParametersUtil.EMPTY_TEST_FILE_NAME);

    private Mockery context;

    private IFileOperations fileOperations;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        fileOperations = context.mock(IFileOperations.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testGetDssInternalTempDirWithDefaultValue()
    {
        File expectedDir = new File(System.getProperty("user.dir"), "dss-tmp");
        prepareForMkdirs(expectedDir);
        prepareForExists(expectedDir);
        prepareForCreateNewFile(EMPTY_TEST_FILE);
        File movedEmptyTestFile = new File(expectedDir, EMPTY_TEST_FILE.getName());
        prepareForRenameFile(EMPTY_TEST_FILE, movedEmptyTestFile, true);
        prepareForDeleteFile(EMPTY_TEST_FILE);
        prepareForDeleteFile(movedEmptyTestFile);

        File tempDir =
                DssPropertyParametersUtil.getDssInternalTempDir(fileOperations, new Properties());

        assertEquals(expectedDir, tempDir);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetDssInternalTempDirWithValidValue()
    {
        File expectedDir = new File("my-dss-tmp");
        Properties properties = new Properties();
        properties.setProperty(DssPropertyParametersUtil.DSS_TEMP_DIR_PATH, expectedDir.getPath());
        prepareForMkdirs(expectedDir);
        prepareForExists(expectedDir);
        prepareForCreateNewFile(EMPTY_TEST_FILE);
        File movedEmptyTestFile = new File(expectedDir, EMPTY_TEST_FILE.getName());
        prepareForRenameFile(EMPTY_TEST_FILE, movedEmptyTestFile, true);
        prepareForDeleteFile(EMPTY_TEST_FILE);
        prepareForDeleteFile(movedEmptyTestFile);

        File tempDir = DssPropertyParametersUtil.getDssInternalTempDir(fileOperations, properties);

        assertEquals(expectedDir, tempDir);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetDssInternalTempDirWhichDoesNotExists()
    {
        File expectedDir = new File("my-dss-tmp");
        Properties properties = new Properties();
        properties.setProperty(DssPropertyParametersUtil.DSS_TEMP_DIR_PATH, expectedDir.getPath());
        prepareForMkdirs(expectedDir);
        prepareForNonExists(expectedDir);

        try
        {
            DssPropertyParametersUtil.getDssInternalTempDir(fileOperations, properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Could not create an internal temp directory for "
                    + "the data store server at path: my-dss-tmp. "
                    + "Please make sure this directory exists on the local file system and "
                    + "is writable by the data store server or provide such a directory "
                    + "by the configuration parameter 'dss-temp-dir'.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetDssInternalTempDirWhichIsNotLocal()
    {
        File expectedDir = new File("my-dss-tmp");
        Properties properties = new Properties();
        properties.setProperty(DssPropertyParametersUtil.DSS_TEMP_DIR_PATH, expectedDir.getPath());
        prepareForMkdirs(expectedDir);
        prepareForExists(expectedDir);
        prepareForCreateNewFile(EMPTY_TEST_FILE);
        File movedEmptyTestFile = new File(expectedDir, EMPTY_TEST_FILE.getName());
        prepareForRenameFile(EMPTY_TEST_FILE, movedEmptyTestFile, false);
        prepareForDeleteFile(EMPTY_TEST_FILE);
        prepareForDeleteFile(movedEmptyTestFile);

        try
        {
            DssPropertyParametersUtil.getDssInternalTempDir(fileOperations, properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Directory at path 'my-dss-tmp' is not on the local file system. "
                    + "Please make sure this directory exists on the local file system and "
                    + "is writable by the data store server or provide such a directory "
                    + "by the configuration parameter 'dss-temp-dir'.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetDssRegistrationLogDirWithDefaultValue()
    {
        File expectedDir = new File(System.getProperty("user.dir"), "log-registrations");
        prepareForMkdirs(expectedDir);
        prepareForExists(expectedDir);
        prepareForCreateNewFile(EMPTY_TEST_FILE);
        File movedEmptyTestFile = new File(expectedDir, EMPTY_TEST_FILE.getName());
        prepareForRenameFile(EMPTY_TEST_FILE, movedEmptyTestFile, true);
        prepareForDeleteFile(EMPTY_TEST_FILE);
        prepareForDeleteFile(movedEmptyTestFile);

        File tempDir =
                DssPropertyParametersUtil
                        .getDssRegistrationLogDir(fileOperations, new Properties());

        assertEquals(expectedDir, tempDir);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetDssRegistrationLogDirWithValidValue()
    {
        File expectedDir = new File("my-dss-log");
        Properties properties = new Properties();
        properties.setProperty(DssPropertyParametersUtil.DSS_REGISTRATION_LOG_DIR_PATH,
                expectedDir.getPath());
        prepareForMkdirs(expectedDir);
        prepareForExists(expectedDir);
        prepareForCreateNewFile(EMPTY_TEST_FILE);
        File movedEmptyTestFile = new File(expectedDir, EMPTY_TEST_FILE.getName());
        prepareForRenameFile(EMPTY_TEST_FILE, movedEmptyTestFile, true);
        prepareForDeleteFile(EMPTY_TEST_FILE);
        prepareForDeleteFile(movedEmptyTestFile);

        File tempDir =
                DssPropertyParametersUtil.getDssRegistrationLogDir(fileOperations, properties);

        assertEquals(expectedDir, tempDir);
        context.assertIsSatisfied();
    }

    @Test
    public void testGetDssRegistrationLogDirWhichDoesNotExists()
    {
        File expectedDir = new File("my-dss-log");
        Properties properties = new Properties();
        properties.setProperty(DssPropertyParametersUtil.DSS_REGISTRATION_LOG_DIR_PATH,
                expectedDir.getPath());
        prepareForMkdirs(expectedDir);
        prepareForNonExists(expectedDir);

        try
        {
            DssPropertyParametersUtil.getDssRegistrationLogDir(fileOperations, properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Could not create a directory for storing "
                    + "registration logs at path: my-dss-log. "
                    + "Please make sure this directory exists on the local file system and "
                    + "is writable by the data store server or provide such a directory "
                    + "by the configuration parameter 'dss-registration-log-dir'.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetDssRegistrationLogDirWhichIsNotLocal()
    {
        File expectedDir = new File("my-dss-log");
        Properties properties = new Properties();
        properties.setProperty(DssPropertyParametersUtil.DSS_REGISTRATION_LOG_DIR_PATH,
                expectedDir.getPath());
        prepareForMkdirs(expectedDir);
        prepareForExists(expectedDir);
        prepareForCreateNewFile(EMPTY_TEST_FILE);
        File movedEmptyTestFile = new File(expectedDir, EMPTY_TEST_FILE.getName());
        prepareForRenameFile(EMPTY_TEST_FILE, movedEmptyTestFile, false);
        prepareForDeleteFile(EMPTY_TEST_FILE);
        prepareForDeleteFile(movedEmptyTestFile);

        try
        {
            DssPropertyParametersUtil.getDssRegistrationLogDir(fileOperations, properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException ex)
        {
            assertEquals("Directory at path 'my-dss-log' is not on the local file system. "
                    + "Please make sure this directory exists on the local file system and "
                    + "is writable by the data store server or provide such a directory "
                    + "by the configuration parameter 'dss-registration-log-dir'.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    private void prepareForMkdirs(final File dir)
    {
        context.checking(new Expectations()
            {
                {
                    one(fileOperations).mkdirs(dir);
                    will(returnValue(true));
                }
            });

    }

    private void prepareForExists(final File dir)
    {
        context.checking(new Expectations()
            {
                {
                    one(fileOperations).exists(dir);
                    will(returnValue(true));
                }
            });
    }

    private void prepareForNonExists(final File dir)
    {
        context.checking(new Expectations()
            {
                {
                    one(fileOperations).exists(dir);
                    will(returnValue(false));
                }
            });
    }

    class FileNameStarsWithMatcher extends BaseMatcher<File>
    {

        String expectedPrefix;

        public FileNameStarsWithMatcher(File fileWithExpectedPrefix)
        {
            this.expectedPrefix = fileWithExpectedPrefix.getPath();
        }

        @Override
        public boolean matches(Object file)
        {
            if (file == null || false == file instanceof File)
            {
                return false;
            }
            return ((File) file).getPath().startsWith(this.expectedPrefix);
        }

        @Override
        public void describeTo(Description description)
        {
            description.appendText("Expected " + this.expectedPrefix + " prefix in the file name");
        }

    }

    private FileNameStarsWithMatcher fileStartingWith(File file)
    {
        return new FileNameStarsWithMatcher(file);
    }

    private void prepareForCreateNewFile(final File file)
    {
        context.checking(new Expectations()
            {
                {
                    one(fileOperations).createNewFile(with(fileStartingWith(file)));
                    will(returnValue(true));
                }
            });
    }

    private void prepareForRenameFile(final File source, final File destination,
            final boolean success)
    {
        context.checking(new Expectations()
            {
                {
                    one(fileOperations).rename(with(fileStartingWith(source)), with(fileStartingWith(destination)));
                    will(returnValue(success));
                }
            });
    }

    private void prepareForDeleteFile(final File file)
    {
        context.checking(new Expectations()
            {
                {
                    one(fileOperations).delete(with(fileStartingWith(file)));
                    will(returnValue(true));
                }
            });
    }

}
