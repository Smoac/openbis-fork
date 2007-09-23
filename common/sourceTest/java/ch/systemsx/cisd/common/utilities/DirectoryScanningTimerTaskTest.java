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
import static ch.systemsx.cisd.common.utilities.FileUtilities.ACCEPT_ALL_FILTER;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.logging.LogMonitoringAppender;
import ch.systemsx.cisd.common.utilities.CollectionIO;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IPathHandler;
import ch.systemsx.cisd.common.utilities.StoringUncaughtExceptionHandler;

/**
 * Test cases for the {@link DirectoryScanningTimerTask}.
 * 
 * @author Bernd Rinn
 */
public class DirectoryScanningTimerTaskTest
{

    private static final File unitTestRootDirectory = new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "DirectoryScanningTimerTaskTest");

    private final StoringUncaughtExceptionHandler exceptionHandler = new StoringUncaughtExceptionHandler();

    private final static FileFilter ALWAYS_FALSE_FILE_FILTER = new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return false;
            }
        };

    private final static String EXCEPTION_THROWING_FILE_FILTER_MESSAGE = "Exception throwing file filter does its job.";

    private final static FileFilter EXCEPTION_THROWING_FILE_FILTER = new FileFilter()
        {
            public boolean accept(File pathname)
            {
                throw new RuntimeException(EXCEPTION_THROWING_FILE_FILTER_MESSAGE);
            }
        };

    private final MockPathHandler mockPathHandler = new MockPathHandler();

    /**
     * A mock implementation that stores the handled paths.
     */
    public static class MockPathHandler implements IPathHandler
    {

        final List<File> handledPaths = new ArrayList<File>();

        public void clear()
        {
            handledPaths.clear();
        }

        public boolean handle(File path)
        {
            handledPaths.add(path);
            path.delete();
            return true;
        }

    }

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
        unitTestRootDirectory.mkdirs();
        assert unitTestRootDirectory.isDirectory();
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
    }

    @BeforeMethod
    public void setUp()
    {
        FileUtilities.deleteRecursively(workingDirectory);
        workingDirectory.mkdirs();
        workingDirectory.deleteOnExit();
        mockPathHandler.clear();
    }

    @AfterMethod
    public void checkException()
    {
        exceptionHandler.checkAndRethrowException();
    }

    @Test(expectedExceptions =
        { ConfigurationFailureException.class })
    public void testFailedConstructionNonExistent()
    {
        final File nonExistentFile = new File(unitTestRootDirectory, "non-existent");
        nonExistentFile.delete();
        final DirectoryScanningTimerTask task =
                new DirectoryScanningTimerTask(nonExistentFile, ACCEPT_ALL_FILTER, mockPathHandler);
        task.check();
    }

    @Test(expectedExceptions =
        { ConfigurationFailureException.class })
    public void testFailedConstructionFileInsteadOfDirectory() throws IOException
    {
        final File file = new File(unitTestRootDirectory, "existent_file");
        file.delete();
        file.deleteOnExit();
        file.createNewFile();
        final DirectoryScanningTimerTask task = new DirectoryScanningTimerTask(file, ACCEPT_ALL_FILTER, mockPathHandler);
        task.check();
   }

    @Test(groups =
        { "requires_unix" }, expectedExceptions =
        { ConfigurationFailureException.class })
    public void testFailedConstructionReadOnly() throws IOException, InterruptedException
    {
        final File readOnlyDirectory = new File(unitTestRootDirectory, "read_only_directory");
        readOnlyDirectory.delete();
        readOnlyDirectory.mkdir();
        readOnlyDirectory.deleteOnExit();
        assert readOnlyDirectory.setReadOnly();

        try
        {
            // Here we should get an AssertationError
            final DirectoryScanningTimerTask task = new DirectoryScanningTimerTask(readOnlyDirectory, ACCEPT_ALL_FILTER, mockPathHandler);
            task.check();
        } finally
        {
            // Unfortunately, with JDK 5 there is no portable way to set a file or directory read/write, once
            // it has been set read-only, thus this test 'requires_unix' for the time being.
            Runtime.getRuntime().exec(String.format("/bin/chmod u+w %s", readOnlyDirectory.getPath())).waitFor();
            if (readOnlyDirectory.canWrite() == false)
            {
                // Can't use assert here since we expect an AssertationError
                throw new IllegalStateException();
            }
        }
    }

    @Test
    public void testFaultyPathsDeletion()
    {
        final File faultyPaths = new File(workingDirectory, DirectoryScanningTimerTask.FAULTY_PATH_FILENAME);
        CollectionIO.writeIterable(faultyPaths, Collections.singleton("some_path"));
        new DirectoryScanningTimerTask(workingDirectory, ACCEPT_ALL_FILTER, mockPathHandler);
        assert faultyPaths.length() == 0;
    }

    @Test
    public void testProcessOK() throws IOException
    {
        final File someFile = new File(workingDirectory, "some_file");
        someFile.createNewFile();
        someFile.deleteOnExit();
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(workingDirectory, ACCEPT_ALL_FILTER, mockPathHandler);
        assertEquals(0, mockPathHandler.handledPaths.size());
        scanner.run();
        assertEquals(1, mockPathHandler.handledPaths.size());
        assertEquals(someFile, mockPathHandler.handledPaths.get(0));
    }

    @Test
    public void testFileFilterUsed() throws IOException
    {
        final File someFile = new File(workingDirectory, "some_file");
        someFile.createNewFile();
        someFile.deleteOnExit();
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(workingDirectory, ALWAYS_FALSE_FILE_FILTER, mockPathHandler);
        assertEquals(0, mockPathHandler.handledPaths.size());
        scanner.run();
        assertEquals(0, mockPathHandler.handledPaths.size());
    }

    @Test
    public void testManipulateFaultyPaths() throws IOException
    {
        final File faultyPaths = new File(workingDirectory, DirectoryScanningTimerTask.FAULTY_PATH_FILENAME);
        final File someFile = new File(workingDirectory, "some_file");
        someFile.createNewFile();
        someFile.deleteOnExit();
        assert someFile.exists();
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(workingDirectory, ACCEPT_ALL_FILTER, mockPathHandler);
        CollectionIO.writeIterable(faultyPaths, Collections.singleton(someFile));
        scanner.run();
        assertEquals(0, mockPathHandler.handledPaths.size());
    }

    @Test
    public void testFaultyPaths() throws IOException
    {
        final File faultyPaths = new File(workingDirectory, DirectoryScanningTimerTask.FAULTY_PATH_FILENAME);
        final File someFile = new File(workingDirectory, "some_file");
        final MockPathHandler myPathHandler = new MockPathHandler()
            {
                boolean firstTime = true;

                @Override
                public boolean handle(File path)
                {
                    if (firstTime)
                    {
                        firstTime = false;
                        return false;
                    }
                    return super.handle(path);
                }
            };
        someFile.createNewFile();
        assert someFile.exists();
        someFile.deleteOnExit();
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(workingDirectory, ACCEPT_ALL_FILTER, myPathHandler);

        // See whether faulty_paths settings works.
        scanner.run();
        assertEquals(0, mockPathHandler.handledPaths.size());
        List<String> faulty = CollectionIO.readList(faultyPaths);
        assertEquals(1, faulty.size());
        assertEquals(someFile.getPath(), faulty.get(0));
        // See whether fault_paths resetting works.
        assert faultyPaths.delete();
        myPathHandler.clear(); // Isn't necessary, just for expressing intention.
        // See whether faulty_paths settings works.
        scanner.run();
        assertEquals(1, myPathHandler.handledPaths.size());
        assertEquals(someFile, myPathHandler.handledPaths.get(0));
    }

    @Test
    public void testPathOrder() throws IOException
    {
        final File dir = new File(workingDirectory, "testPathOrder");
        final File f1 = new File(dir, "1");
        final File f2 = new File(dir, "2");
        final File f3 = new File(dir, "3");
        final File f4 = new File(dir, "4");
        final long now = System.currentTimeMillis();
        dir.mkdir();
        dir.deleteOnExit();
        f1.createNewFile();
        f1.deleteOnExit();
        f2.createNewFile();
        f2.deleteOnExit();
        f3.createNewFile();
        f3.deleteOnExit();
        f4.createNewFile();
        f4.deleteOnExit();
        // Order should be: 2, 4, 3, 1
        f2.setLastModified(now - 10000);
        f4.setLastModified(now - 5000);
        f3.setLastModified(now - 1000);
        f1.setLastModified(now);
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(dir, ACCEPT_ALL_FILTER, mockPathHandler);
        scanner.run();
        assertEquals(f2, mockPathHandler.handledPaths.get(0));
        assertEquals(f4, mockPathHandler.handledPaths.get(1));
        assertEquals(f3, mockPathHandler.handledPaths.get(2));
        assertEquals(f1, mockPathHandler.handledPaths.get(3));
        assertEquals(4, mockPathHandler.handledPaths.size());
    }

    @Test
    public void testMissingDirectory()
    {
        final File dir = new File(workingDirectory, "testMissingDirectory");
        dir.mkdir();
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY, "Failed to get listing of directory");
        // The directory needs to exist when the scanner is created, otherwise the self-test will fail.
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(dir, ACCEPT_ALL_FILTER, mockPathHandler);
        dir.delete();
        assert dir.exists() == false;
        scanner.run();
        appender.verifyLogHasHappened();
        LogMonitoringAppender.removeAppender(appender);
    }

    @Test
    public void testDirectoryIsFile() throws IOException
    {
        final File dir = new File(workingDirectory, "testMissingDirectory");
        dir.mkdir();
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY, "Failed to get listing of directory");
        // The directory needs to exist when the scanner is created, otherwise the self-test will fail.
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(dir, ACCEPT_ALL_FILTER, mockPathHandler);
        dir.delete();
        dir.createNewFile();
        dir.deleteOnExit();
        assert dir.isFile();
        scanner.run();
        appender.verifyLogHasHappened();
        dir.delete();
        LogMonitoringAppender.removeAppender(appender);
    }

    @Test
    public void testFailingFileFilter() throws IOException
    {
        final File dir = new File(workingDirectory, "testMissingDirectory");
        dir.mkdir();
        dir.deleteOnExit();
        final File file = new File(dir, "some.file");
        file.createNewFile();
        file.deleteOnExit();
        LogMonitoringAppender appender1 =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY, "Failed to get listing of directory");
        LogMonitoringAppender appender2 =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY, EXCEPTION_THROWING_FILE_FILTER_MESSAGE);
        // The directory needs to exist when the scanner is created, otherwise the self-test will fail.
        final DirectoryScanningTimerTask scanner =
                new DirectoryScanningTimerTask(dir, EXCEPTION_THROWING_FILE_FILTER, mockPathHandler);
        scanner.run();
        appender1.verifyLogHasHappened();
        appender2.verifyLogHasHappened();
        file.delete();
        dir.delete();
        LogMonitoringAppender.removeAppender(appender1);
        LogMonitoringAppender.removeAppender(appender2);
    }

}
