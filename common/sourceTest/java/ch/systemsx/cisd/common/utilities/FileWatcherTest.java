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
import java.util.Timer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

    private static final File tmpFile1 = new File(workingDirectory, "tmpFile1");

    private static final File tmpFile2 = new File(workingDirectory, "tmpFile2");

    private PrintStream systemOut;

    private PrintStream systemErr;

    private ByteArrayOutputStream logRecorder;

    private volatile boolean onChangeCalled;

    private final static void createNewFile(File file) throws IOException
    {
        FileUtils.touch(file);
        file.setLastModified(0);
        assert file.exists();
        file.deleteOnExit();
    }

    private final static Properties createLogProperties()
    {
        Properties properties = new Properties();
        properties.setProperty("log4j.rootLogger", "TRACE, TestAppender");
        properties.setProperty("log4j.appender.TestAppender", ConsoleAppender.class.getName());
        properties.setProperty("log4j.appender.TestAppender.layout", PatternLayout.class.getName());
        properties.setProperty("log4j.appender.TestAppender.layout.ConversionPattern", "%m%n");
        return properties;
    }

    private final String getLogContent()
    {
        return new String(logRecorder.toByteArray()).trim();
    }

    @BeforeClass
    public final void setUp() throws IOException
    {
        logRecorder = new ByteArrayOutputStream();
        systemOut = System.out;
        systemErr = System.err;
        System.setErr(new PrintStream(logRecorder));
        System.setOut(new PrintStream(logRecorder));
        PropertyConfigurator.configure(createLogProperties());
        workingDirectory.mkdirs();
        assert workingDirectory.isDirectory();
        createNewFile(tmpFile1);
        createNewFile(tmpFile2);
        workingDirectory.deleteOnExit();
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
        new TestFileWatcher(tmpFile1).run();
        assertEquals(String.format(FileWatcher.HAS_NOT_CHANGED_FORMAT, tmpFile1), getLogContent());
    }

    @Test
    public final void testFileHasChanged() throws IOException
    {
        onChangeCalled = false;
        FileWatcher fileWatcher = new TestFileWatcher(tmpFile1);
        FileUtils.touch(tmpFile1);
        fileWatcher.run();
        assertEquals(String.format(FileWatcher.HAS_CHANGED_FORMAT, tmpFile1), getLogContent());
        assertEquals(true, onChangeCalled);
    }

    @Test
    public final void testWithTimer() throws IOException
    {
        onChangeCalled = false;
        FileWatcher fileWatcher = new TestFileWatcher(tmpFile2);
        FileUtils.touch(tmpFile2);
        Timer timer = new Timer(true);
        timer.schedule(fileWatcher, 0);
    }

    @Test(dependsOnMethods = "testWithTimer", timeOut = 5000, groups = "slow")
    public final void testOnChangeCalled() throws InterruptedException
    {
        while (onChangeCalled == false)
        {
            Thread.sleep(200);
        }
        assertEquals(StringUtils.EMPTY, getLogContent());
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
            onChangeCalled = true;
        }
    }
}
