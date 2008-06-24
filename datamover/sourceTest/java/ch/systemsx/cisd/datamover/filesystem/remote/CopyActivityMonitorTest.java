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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;
import ch.systemsx.cisd.common.test.StoringUncaughtExceptionHandler;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.BooleanStatus;
import ch.systemsx.cisd.datamover.filesystem.remote.CopyActivityMonitor.IFileStoreMonitor;
import ch.systemsx.cisd.datamover.intf.ITimingParameters;

/**
 * Test cases for the {@link CopyActivityMonitor} class.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
    { CopyActivityMonitor.class, CopyActivityMonitor.IFileStoreMonitor.class })
public class CopyActivityMonitorTest
{

    private static final File unitTestRootDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory =
            new File(unitTestRootDirectory, "CopyActivityMonitorTest");

    private static final int INACTIVITY_PERIOD_MILLIS = 50;

    private final StoringUncaughtExceptionHandler exceptionHandler =
            new StoringUncaughtExceptionHandler();

    // ////////////////////////////////////////
    // Some mock and dummy implementations.
    //

    private final static class DummyTerminable implements ITerminable
    {
        public boolean terminate()
        {
            throw new AssertionError("call not expected");
        }
    }

    private final static class MockTerminable implements ITerminable
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

    private static interface ILastChangedChecker
    {
        public long lastChangedRelative(StoreItem item, long stopWhenFindYoungerRelative);
    }

    private final static class HappyPathLastChangedChecker implements ILastChangedChecker
    {
        private final long stopWhenFindYoungerRelativeExpected;

        public HappyPathLastChangedChecker(long stopWhenFindYoungerRelativeExpected)
        {
            this.stopWhenFindYoungerRelativeExpected = stopWhenFindYoungerRelativeExpected;
        }

        public long lastChangedRelative(StoreItem item, long stopWhenFindYoungerRelative)
        {
            if (stopWhenFindYoungerRelative != 0)
            {
                assertEquals(stopWhenFindYoungerRelativeExpected, stopWhenFindYoungerRelative);
            }
            return System.currentTimeMillis() - INACTIVITY_PERIOD_MILLIS / 2;
        }
    }

    private final static class MyTimingParameters implements ITimingParameters
    {

        private final long inactivityPeriodMillis;

        private final int maximalNumberOfRetries;

        MyTimingParameters(int maximalNumberOfRetries)
        {
            this(maximalNumberOfRetries, INACTIVITY_PERIOD_MILLIS);
        }

        MyTimingParameters(int maximalNumberOfRetries, long inactivityPeriodMillis)
        {
            this.inactivityPeriodMillis = inactivityPeriodMillis;
            this.maximalNumberOfRetries = maximalNumberOfRetries;
        }

        public long getCheckIntervalMillis()
        {
            return inactivityPeriodMillis / 10;
        }

        public long getQuietPeriodMillis()
        {
            return inactivityPeriodMillis / 10;
        }

        public long getInactivityPeriodMillis()
        {
            return inactivityPeriodMillis;
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

    abstract private static class AlwaysExistsStoreMonitor implements IFileStoreMonitor
    {
        private final StoreItem expectedItem;

        public AlwaysExistsStoreMonitor(StoreItem item)
        {
            this.expectedItem = item;
        }

        public BooleanStatus exists(StoreItem item)
        {
            assertEquals(this.expectedItem, item);
            return BooleanStatus.createTrue();
        }
    }

    private IFileStoreMonitor asFileStore(final File directory, final ILastChangedChecker checker)
    {
        return new IFileStoreMonitor()
            {
                public BooleanStatus exists(StoreItem item)
                {
                    return BooleanStatus.createTrue();
                }

                public long lastChanged(StoreItem item, long stopWhenFindYoungerRelative)
                {
                    return checker.lastChangedRelative(item, stopWhenFindYoungerRelative);
                }

                @Override
                public String toString()
                {
                    return "[test store] " + directory.getPath();
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
        final ITerminable dummyTerminable = new DummyTerminable();
        final long inactivityPeriodMillis = 5000L;
        final ITimingParameters parameters = new MyTimingParameters(0, inactivityPeriodMillis);
        final ILastChangedChecker checker =
                new HappyPathLastChangedChecker(inactivityPeriodMillis - 1000L);
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(asFileStore(workingDirectory, checker), dummyTerminable,
                        parameters);
        StoreItem item = createDirectoryInside(workingDirectory);
        monitor.start(item);
        Thread.sleep(INACTIVITY_PERIOD_MILLIS * 15);
        monitor.stop();
    }

    @Test(groups =
        { "slow" })
    public void testCopyStalled() throws Throwable
    {
        final ILastChangedChecker checker = new PathLastChangedCheckerStalled();
        final MockTerminable copyProcess = new MockTerminable();
        final ITimingParameters parameters = new MyTimingParameters(0);
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(asFileStore(workingDirectory, checker), copyProcess,
                        parameters);
        StoreItem item = createDirectoryInside(workingDirectory);
        monitor.start(item);
        Thread.sleep(INACTIVITY_PERIOD_MILLIS * 15);
        monitor.stop();
        assert copyProcess.isTerminated();
    }

    private final static class SimulateShortInterruptionChangedChecker implements
            ILastChangedChecker
    {
        private int numberOfTimesCalled = 0;

        public long lastChangedRelative(StoreItem item, long stopWhenFindYounger)
        {
            ++numberOfTimesCalled;
            if (numberOfTimesCalled == 2)
            {
                // Here we simulate the rare case where one file has been finished but the next file
                // hasn't yet been
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
     * This test case catches a case that I first hadn't thought of: since we use <code>rsync</code>
     * in a mode where at the end of copying a file they set the "last modified" time back to the
     * one of the source file, there is a short time interval after finishing copying one file and
     * starting copying the next file where the copy monitor could be tempted to trigger false
     * alarm: the just finished file will have already the "last modified" time of the source file
     * (which is when the data produce finished writing the source file). In fact everything is fine
     * but still the copy process will be canceled.
     */
    @Test(groups =
        { "slow" })
    public void testCopySeemsStalledButActuallyIsFine() throws Throwable
    {
        final ILastChangedChecker checker = new SimulateShortInterruptionChangedChecker();
        final MockTerminable copyProcess = new MockTerminable();
        final ITimingParameters parameters = new MyTimingParameters(0);
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(asFileStore(workingDirectory, checker), copyProcess,
                        parameters);
        StoreItem item = createDirectoryInside(workingDirectory);
        monitor.start(item);
        Thread.sleep(INACTIVITY_PERIOD_MILLIS * 15);
        monitor.stop();
        assert copyProcess.isTerminated() == false;
    }

    private final static class PathLastChangedCheckerStalled implements ILastChangedChecker
    {
        private final static long MAX_TIME =
                System.currentTimeMillis() + INACTIVITY_PERIOD_MILLIS * 2;

        public long lastChangedRelative(StoreItem item, long stopWhenFindYoungerRelative)
        {
            long now = System.currentTimeMillis();
            return now < MAX_TIME ? now : MAX_TIME;
        }
    }

    @Test(groups =
        { "slow" })
    public void testActivityMonitorTimedOut() throws Throwable
    {
        final PathLastChangedCheckerDelayed checker =
                new PathLastChangedCheckerDelayed(INACTIVITY_PERIOD_MILLIS);
        final MockTerminable copyProcess = new MockTerminable();
        final ITimingParameters parameters = new MyTimingParameters(0);
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(asFileStore(workingDirectory, checker), copyProcess,
                        parameters);
        final StoreItem item = createDirectoryInside(workingDirectory);
        final LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, String.format(
                        "Could not determine \"last changed time\" of %s: time out.", item));
        monitor.start(item);
        Thread.sleep(INACTIVITY_PERIOD_MILLIS * 15);
        monitor.stop();
        LogMonitoringAppender.removeAppender(appender);
        assertTrue(checker.lastCheckInterrupted());
        assertTrue(copyProcess.isTerminated());
        appender.verifyLogHasHappened();
    }

    @Test(groups =
        { "slow" })
    public void testActivityMonitorOnceTimedOutTheOK() throws Throwable
    {
        final PathLastChangedCheckerDelayed checker =
                new PathLastChangedCheckerDelayed(INACTIVITY_PERIOD_MILLIS, 0L);
        final MockTerminable copyProcess = new MockTerminable();
        final ITimingParameters parameters = new MyTimingParameters(0);
        final CopyActivityMonitor monitor =
                new CopyActivityMonitor(asFileStore(workingDirectory, checker), copyProcess,
                        parameters);
        final StoreItem item = createDirectoryInside(workingDirectory);
        monitor.start(item);
        Thread.sleep(INACTIVITY_PERIOD_MILLIS * 15);
        monitor.stop();
        assertFalse(checker.lastCheckInterrupted());
        assertFalse(copyProcess.isTerminated());
    }

    private StoreItem createDirectoryInside(File parentDir)
    {
        StoreItem item = new StoreItem("some-directory");
        final File directory = new File(parentDir, item.getName());
        directory.mkdir();
        assert directory.isDirectory() : "directory could not be created: " + directory;
        directory.deleteOnExit();
        return item;
    }

    private final static class PathLastChangedCheckerDelayed implements ILastChangedChecker
    {
        private final long[] delayMillis;

        private int callNumber;

        private volatile boolean interrupted;

        private int interruptionCount;

        public PathLastChangedCheckerDelayed(long... delayMillis)
        {
            assert delayMillis.length > 0;

            this.interrupted = false;
            this.interruptionCount = 0;
            this.delayMillis = delayMillis;
        }

        private long timeToSleepMillis()
        {
            try
            {
                return delayMillis[callNumber];
            } finally
            {
                if (callNumber < delayMillis.length - 1)
                {
                    ++callNumber;
                }
            }
        }

        public long lastChangedRelative(StoreItem item, long stopWhenFindYoungerRelative)
        {
            try
            {
                Thread.sleep(timeToSleepMillis()); // Wait predefined time.
            } catch (InterruptedException e)
            {
                this.interrupted = true;
                ++this.interruptionCount;
                // That is what we expect if we are terminated.
                throw new CheckedExceptionTunnel(new InterruptedException(e.getMessage()));
            }
            this.interrupted = false;
            return System.currentTimeMillis();
        }

        synchronized boolean lastCheckInterrupted()
        {
            return interrupted;
        }

        int getInterruptionCount()
        {
            return interruptionCount;
        }

    }

    @Test(groups =
        { "slow" })
    // check if copy is terminated if destination file is never visible
    public void testActivityFileNeverExistsFail() throws Throwable
    {
        final StoreItem dummyItem = createDummyItem();
        final IFileStoreMonitor store = new IFileStoreMonitor()
            {
                public BooleanStatus exists(StoreItem item)
                {
                    assertEquals(dummyItem, item);
                    return BooleanStatus.createFalse();
                }

                public long lastChanged(StoreItem item, long stopWhenYoungerThan)
                {
                    throw new UnsupportedOperationException(); // should be never called
                }

            };
        checkCopyTerminated(store, dummyItem);
    }

    @Test(groups =
        { "slow" })
    // check if copy is terminated if lastChange always fails
    public void testActivityLastChangeUnavailableFail() throws Throwable
    {
        final StoreItem dummyItem = createDummyItem();
        final IFileStoreMonitor store = new AlwaysExistsStoreMonitor(dummyItem)
            {
                public long lastChanged(StoreItem item, long stopWhenYoungerThan)
                {
                    assertEquals(dummyItem, item);
                    return 0; // signalizes error
                }
            };
        checkCopyTerminated(store, dummyItem);
    }

    @Test(groups =
        { "slow" })
    // check if copy is terminated if lastChange fails on even calls and returns the unchanged value
    // on odd calls
    public void testActivityLastChangeUnavailableOftenFail() throws Throwable
    {
        final StoreItem dummyItem = createDummyItem();
        final IFileStoreMonitor store = new AlwaysExistsStoreMonitor(dummyItem)
            {
                private boolean oddCall = true;

                public long lastChanged(StoreItem item, long stopWhenYoungerThan)
                {
                    assertEquals(dummyItem, item);
                    oddCall = !oddCall;
                    return oddCall ? 10 : 0; // error or unchanged value
                }
            };
        checkCopyTerminated(store, dummyItem);
    }

    @Test(groups =
        { "slow" })
    // happy case - check if copy is not terminated if lastChange returns changing values
    public void testActivityChangingCopyCompletes() throws Throwable
    {
        final StoreItem dummyItem = createDummyItem();
        final IFileStoreMonitor store = new AlwaysExistsStoreMonitor(dummyItem)
            {
                private int counter = 1;

                public long lastChanged(StoreItem item, long stopWhenYoungerThan)
                {
                    return counter++;
                }
            };
        checkCopyTerminationStatus(store, dummyItem, false);
    }

    private void checkCopyTerminated(final IFileStoreMonitor store, StoreItem dummyItem)
            throws InterruptedException
    {
        checkCopyTerminationStatus(store, dummyItem, true);
    }

    private void checkCopyTerminationStatus(final IFileStoreMonitor store, StoreItem dummyItem,
            boolean expectedIsTerminated) throws InterruptedException
    {
        final MockTerminable copyProcess = new MockTerminable();
        final ITimingParameters parameters = new MyTimingParameters(0);
        final CopyActivityMonitor monitor = new CopyActivityMonitor(store, copyProcess, parameters);
        monitor.start(dummyItem);
        Thread.sleep(INACTIVITY_PERIOD_MILLIS * 15);
        monitor.stop();
        assertEquals(expectedIsTerminated, copyProcess.isTerminated());
    }

    private static StoreItem createDummyItem()
    {
        return new StoreItem("dummy-item");
    }

}
