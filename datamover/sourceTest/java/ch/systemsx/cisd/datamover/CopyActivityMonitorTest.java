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

package ch.systemsx.cisd.datamover;

import java.io.File;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.logging.LogMonitoringAppender;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.StoringUncaughtExceptionHandler;

/**
 * Test cases for the {@link CopyActivityMonitor} class.
 * 
 * @author Bernd Rinn
 */
public class CopyActivityMonitorTest
{

    private static final File unitTestRootDirectory = new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "CopyActivityMonitorTest");

    private static final int INACTIVITY_PERIOD_MILLIS = 50;

    private final StoringUncaughtExceptionHandler exceptionHandler = new StoringUncaughtExceptionHandler();

    // ////////////////////////////////////////
    // Some mock and dummy implementations.
    //

    private final class DummyTerminable implements ITerminable
    {
        public boolean terminate()
        {
            throw new AssertionError("call not expected");
        }
    }

    private final class MockTerminable implements ITerminable
    {
        private boolean terminated = false;

        public boolean terminate()
        {
            terminated = true;
            return true;
        }

        /**
         * @return <code>true</code> if {@link #terminate} has been called.
         */
        public boolean isTerminated()
        {
            return terminated;
        }
    }

    private final class MyFileSystemOperations implements IFileSystemOperations
    {
        /**
         * 
         */
        private final IPathLastChangedChecker checker;

        private MyFileSystemOperations(IPathLastChangedChecker checker)
        {
            this.checker = checker;
        }

        public IPathLastChangedChecker getChecker()
        {
            return checker;
        }

        public IPathCopier getCopier()
        {
            throw new AssertionError("call not expected");
        }

        public IPathRemover getRemover()
        {
            throw new AssertionError("call not expected");
        }
    }

    private final class HappyPathLastChangedChecker implements IPathLastChangedChecker
    {
        public long lastChanged(File path)
        {
            return System.currentTimeMillis() - INACTIVITY_PERIOD_MILLIS / 2;
        }
    }

    private final class MyTimingParameters implements ITimingParameters
    {

        private final int maximalNumberOfRetries;

        MyTimingParameters(int maximalNumberOfRetries)
        {
            this.maximalNumberOfRetries = maximalNumberOfRetries;
        }

        public long getCheckIntervalMillis()
        {
            return INACTIVITY_PERIOD_MILLIS / 10;
        }

        public long getQuietPeriodMillis()
        {
            return INACTIVITY_PERIOD_MILLIS / 10;
        }

        public long getInactivityPeriodMillis()
        {
            return INACTIVITY_PERIOD_MILLIS;
        }

        public long getIntervalToWaitAfterFailure()
        {
            return 0;
        }

        public int getMaximalNumberOfRetries()
        {
            return maximalNumberOfRetries;
        }
    }

    // ////////////////////////////////////////
    // Initialization methods.
    //

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
        workingDirectory.delete();
        workingDirectory.mkdirs();
        workingDirectory.deleteOnExit();
        exceptionHandler.reset();
    }

    @AfterMethod
    public void checkException()
    {
        exceptionHandler.checkAndRethrowException();
    }

    // ////////////////////////////////////////
    // Test cases.
    //

    @Test(groups =
        { "slow" })
    public void testHappyPath() throws Throwable
    {
        final IPathLastChangedChecker checker = new HappyPathLastChangedChecker();
        final IFileSystemOperations operations = new MyFileSystemOperations(checker);
        final ITerminable dummyTerminable = new DummyTerminable();
        final ITimingParameters parameters = new MyTimingParameters(0);
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(workingDirectory, operations, dummyTerminable, parameters);
        final File directory = new File(workingDirectory, "some-directory");
        directory.mkdir();
        directory.deleteOnExit();
        monitor.start(directory);
        Thread.sleep(INACTIVITY_PERIOD_MILLIS * 15);
        monitor.stop();
    }

    @Test(groups =
        { "slow" })
    public void testCopyStalled() throws Throwable
    {
        final IPathLastChangedChecker checker = new PathLastChangedCheckerStalled();
        final IFileSystemOperations operations = new MyFileSystemOperations(checker);
        final MockTerminable copyProcess = new MockTerminable();
        final ITimingParameters parameters = new MyTimingParameters(0);
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(workingDirectory, operations, copyProcess, parameters);
        final File file = new File(workingDirectory, "some-directory");
        file.mkdir();
        monitor.start(file);
        Thread.sleep(INACTIVITY_PERIOD_MILLIS * 15);
        monitor.stop();
        assert copyProcess.isTerminated();
    }

    private final class SimulateShortInterruptionChangedChecker implements IPathLastChangedChecker
    {
        private int numberOfTimesCalled = 0;

        public long lastChanged(File path)
        {
            ++numberOfTimesCalled;
            if (numberOfTimesCalled == 2)
            {
                // Here we simulate the rare case where one file has been finished but the next file hasn't yet been
                // started.
                return System.currentTimeMillis() - INACTIVITY_PERIOD_MILLIS * 2;
            } else
            {
                // Here we simulate normal activity.
                return System.currentTimeMillis() - INACTIVITY_PERIOD_MILLIS / 2;
            }
        }
    }

    /**
     * This test case catches a case that I first hadn't thought of: since we use <code>rsync</code> (or
     * <code>xcopy</code>) in a mode where at the end of copying a file they set the "last modified" time back to the
     * one of the source file, there is a short time interval after finishing copying one file anst starting copying the
     * next file where the copy monitor could be tempted to trigger false alarm: the just finished file will have
     * already the "last modified" time of the source file (which is when the data produce finished writing the source
     * file). In fact everything is fine but still the copy process will be cancelled.
     */
    @Test(groups =
        { "slow" })
    public void testCopySeemsStalledButActuallyIsFine() throws Throwable
    {
        final IPathLastChangedChecker checker = new SimulateShortInterruptionChangedChecker();
        final IFileSystemOperations operations = new MyFileSystemOperations(checker);
        final MockTerminable copyProcess = new MockTerminable();
        final ITimingParameters parameters = new MyTimingParameters(0);
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(workingDirectory, operations, copyProcess, parameters);
        final File file = new File(workingDirectory, "some-directory");
        file.mkdir();
        monitor.start(file);
        Thread.sleep(INACTIVITY_PERIOD_MILLIS * 15);
        monitor.stop();
        assert copyProcess.isTerminated() == false;
    }

    private final class PathLastChangedCheckerStalled implements IPathLastChangedChecker
    {
        public long lastChanged(File path)
        {
            return System.currentTimeMillis() - INACTIVITY_PERIOD_MILLIS * 2;
        }
    }

    @Test(groups =
        { "slow" })
    public void testActivityMonitorStuck() throws Throwable
    {
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, "Activity monitor got terminated");
        LogFactory.getLogger(LogCategory.OPERATION, CopyActivityMonitor.class).addAppender(appender);
        final PathLastChangedCheckerStuck checker = new PathLastChangedCheckerStuck();
        final IFileSystemOperations operations = new MyFileSystemOperations(checker);
        final MockTerminable copyProcess = new MockTerminable();
        final ITimingParameters parameters = new MyTimingParameters(0);
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(workingDirectory, operations, copyProcess, parameters);
        final File directory = new File(workingDirectory, "some-directory");
        directory.mkdir();
        directory.deleteOnExit();
        monitor.start(directory);
        Thread.sleep(INACTIVITY_PERIOD_MILLIS * 15);
        monitor.stop();
        LogMonitoringAppender.removeAppender(appender);
        assert copyProcess.isTerminated();
        appender.verifyLogHasHappened();
    }

    private final class PathLastChangedCheckerStuck implements IPathLastChangedChecker
    {
        private boolean interrupted = false;

        public long lastChanged(File path)
        {
            try
            {
                Thread.sleep(INACTIVITY_PERIOD_MILLIS); // Wait longer than the activity monitor is willing to wait for
                                                        // us.
            } catch (InterruptedException e)
            {
                // Can't happen since this method runs in a TimerThread which isn't interrupted.
                throw new CheckedExceptionTunnel(e);
            }
            return 0; // Return value doesn't matter because the TImerTask is already terminated.
        }

        /**
         * @return If the checker has been interrupted.
         */
        public boolean isInterrupted()
        {
            return interrupted;
        }
    }

}
