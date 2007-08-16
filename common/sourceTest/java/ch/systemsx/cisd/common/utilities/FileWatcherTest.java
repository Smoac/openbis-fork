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

package ch.systemsx.cisd.common.utilities;

import static org.testng.AssertJUnit.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * Test cases for the {@link FileWatcher}.
 * <p>
 * Note that this test is suite aware. This test will probably fail if you call it alone.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class FileWatcherTest
{
    private static final File unitTestRootDirectory = new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "FileWatcherTest");

    private static final File touchedFile1 = new File(workingDirectory, "touchedFile1");

    private PrintStream systemOut;

    private PrintStream systemErr;

    private ByteArrayOutputStream logRecorder;

    private final static void createNewFile(File file) throws IOException
    {
        FileUtils.touch(file);
        assert file.exists();
        file.deleteOnExit();
    }

    @BeforeSuite
    public final void beforeSuite() throws IOException
    {
        workingDirectory.mkdirs();
        assert workingDirectory.isDirectory();
        createNewFile(touchedFile1);
        workingDirectory.deleteOnExit();
    }

    @BeforeClass
    public final void setUp() throws IOException
    {
        LogInitializer.init();
        logRecorder = new ByteArrayOutputStream();
        systemOut = System.out;
        systemErr = System.err;
        System.setErr(new PrintStream(logRecorder));
        System.setOut(new PrintStream(logRecorder));
        Properties properties = new Properties();
        properties.setProperty("log4j.rootLogger", "TRACE, TestAppender");
        properties.setProperty("log4j.appender.TestAppender", ConsoleAppender.class.getName());
        properties.setProperty("log4j.appender.TestAppender.layout", PatternLayout.class.getName());
        properties.setProperty("log4j.appender.TestAppender.layout.ConversionPattern", "%m%n");
        PropertyConfigurator.configure(properties);
    }

    @AfterClass
    public final void tearDown()
    {
        FileUtilities.deleteRecursively(workingDirectory);
        if (systemOut != null)
        {
            System.setOut(systemOut);
        }
        if (systemErr != null)
        {
            System.setErr(systemErr);
        }
    }

    @BeforeMethod
    public final void beforeMethod()
    {
        logRecorder.reset();
    }

    private String getLogContent()
    {
        return new String(logRecorder.toByteArray()).trim();
    }

    @Test
    public final void testWithNonExistingFile()
    {
        File file = new File(workingDirectory, "doesNotExist");
        assert file.exists() == false;
        new TestFileWatcher(file).run();
        assertEquals(String.format(FileWatcher.DOES_NOT_EXIST_FORMAT, file), getLogContent());
    }

    @Test
    public final void testNonChangingFile()
    {
        new TestFileWatcher(touchedFile1).run();
        assertEquals(String.format(FileWatcher.HAS_NOT_CHANGED_FORMAT, touchedFile1), getLogContent());
    }

    // @Test
    public final void testFileHasChanged() throws IOException
    {
        FileWatcher fileWatcher = new TestFileWatcher(touchedFile1);
        FileUtils.touch(touchedFile1);
        fileWatcher.run();
        assertEquals(String.format(FileWatcher.HAS_CHANGED_FORMAT, touchedFile1), getLogContent());
    }

    //
    // Helper classes
    //

    private final class TestFileWatcher extends FileWatcher
    {

        TestFileWatcher(File file)
        {
            super(file);
        }

        @Override
        protected void onChange()
        {

        }
    }
}
