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

package ch.systemsx.cisd.datamover.filesystem.remote;

import java.io.File;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.logging.LogMonitoringAppender;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.StoringUncaughtExceptionHandler;
import ch.systemsx.cisd.datamover.common.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.FileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IStoreCopier;
import ch.systemsx.cisd.datamover.filesystem.store.FileStoreLocal;
import ch.systemsx.cisd.datamover.intf.ITimingParameters;
import ch.systemsx.cisd.datamover.testhelper.FileOperationsUtil;

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

    private static interface LastChangedChecker
    {
        public long lastChanged(StoreItem item);
    }

    private final class HappyPathLastChangedChecker implements LastChangedChecker
    {
        public long lastChanged(StoreItem item)
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

    private FileStore asFileStore(File directory, final LastChangedChecker checker)
    {
        IFileSysOperationsFactory factory = FileOperationsUtil.createTestFatory();
        return asFileStore(directory, checker, factory);
    }

    private FileStore asFileStore(File directory, final LastChangedChecker checker, IFileSysOperationsFactory factory)
    {
        final FileStoreLocal localImpl = new FileStoreLocal(directory, "input-test", factory);
        return new FileStore(directory, null, false, "input-test", factory)
            {
                @Override
                public Status delete(StoreItem item)
                {
                    return localImpl.delete(item);
                }

                @Override
                public boolean exists(StoreItem item)
                {
                    return localImpl.exists(item);
                }

                @Override
                public long lastChanged(StoreItem item)
                {
                    return checker.lastChanged(item);
                }

                @Override
                public String tryCheckDirectoryFullyAccessible()
                {
                    return localImpl.tryCheckDirectoryFullyAccessible();
                }

                @Override
                public ExtendedFileStore tryAsExtended()
                {
                    return localImpl.tryAsExtended();
                }

                @Override
                public IStoreCopier getCopier(FileStore destinationDirectory)
                {
                    return localImpl.getCopier(destinationDirectory);
                }
            };
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
        final LastChangedChecker checker = new HappyPathLastChangedChecker();
        final ITerminable dummyTerminable = new DummyTerminable();
        final ITimingParameters parameters = new MyTimingParameters(0);
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(asFileStore(workingDirectory, checker), dummyTerminable, parameters);
        StoreItem item = createDirectoryInside(workingDirectory);
        monitor.start(item);
        Thread.sleep(INACTIVITY_PERIOD_MILLIS * 15);
        monitor.stop();
    }

    @Test(groups =
        { "slow" })
    public void testCopyStalled() throws Throwable
    {
        final LastChangedChecker checker = new PathLastChangedCheckerStalled();
        final MockTerminable copyProcess = new MockTerminable();
        final ITimingParameters parameters = new MyTimingParameters(0);
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(asFileStore(workingDirectory, checker), copyProcess, parameters);
        StoreItem item = createDirectoryInside(workingDirectory);
        monitor.start(item);
        Thread.sleep(INACTIVITY_PERIOD_MILLIS * 15);
        monitor.stop();
        assert copyProcess.isTerminated();
    }

    private final class SimulateShortInterruptionChangedChecker implements LastChangedChecker
    {
        private int numberOfTimesCalled = 0;

        public long lastChanged(StoreItem item)
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
     * This test case catches a case that I first hadn't thought of: since we use <code>rsync</code> in a mode where
     * at the end of copying a file they set the "last modified" time back to the one of the source file, there is a
     * short time interval after finishing copying one file anst starting copying the next file where the copy monitor
     * could be tempted to trigger false alarm: the just finished file will have already the "last modified" time of the
     * source file (which is when the data produce finished writing the source file). In fact everything is fine but
     * still the copy process will be cancelled.
     */
    @Test(groups =
        { "slow" })
    public void testCopySeemsStalledButActuallyIsFine() throws Throwable
    {
        final LastChangedChecker checker = new SimulateShortInterruptionChangedChecker();
        final MockTerminable copyProcess = new MockTerminable();
        final ITimingParameters parameters = new MyTimingParameters(0);
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(asFileStore(workingDirectory, checker), copyProcess, parameters);
        StoreItem item = createDirectoryInside(workingDirectory);
        monitor.start(item);
        Thread.sleep(INACTIVITY_PERIOD_MILLIS * 15);
        monitor.stop();
        assert copyProcess.isTerminated() == false;
    }

    private final class PathLastChangedCheckerStalled implements LastChangedChecker
    {
        public long lastChanged(StoreItem item)
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
        final LastChangedChecker checker = new PathLastChangedCheckerStuck();
        final MockTerminable copyProcess = new MockTerminable();
        final ITimingParameters parameters = new MyTimingParameters(0);
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(asFileStore(workingDirectory, checker), copyProcess, parameters);
        StoreItem item = createDirectoryInside(workingDirectory);
        monitor.start(item);
        Thread.sleep(INACTIVITY_PERIOD_MILLIS * 15);
        monitor.stop();
        LogMonitoringAppender.removeAppender(appender);
        assert copyProcess.isTerminated();
        appender.verifyLogHasHappened();
    }

    private StoreItem createDirectoryInside(File parentDir)
    {
        StoreItem item = new StoreItem("some-directory");
        final File directory = new File(parentDir, item.getName());
        directory.mkdir();
        directory.deleteOnExit();
        return item;
    }

    private final class PathLastChangedCheckerStuck implements LastChangedChecker
    {
        private boolean interrupted = false;

        public long lastChanged(StoreItem item)
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
