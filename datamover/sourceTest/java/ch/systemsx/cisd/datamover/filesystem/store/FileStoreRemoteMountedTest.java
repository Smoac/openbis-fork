/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.filesystem.store;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.NumberStatus;
import ch.systemsx.cisd.datamover.filesystem.store.FileStoreRemoteMounted.LastChangeWrapper;

/**
 * @author Tomasz Pylak
 */
@Friend(toClasses = FileStoreRemoteMounted.LastChangeWrapper.class)
public class FileStoreRemoteMountedTest
{
    private Mockery context;

    private IFileStore localStoreMock;

    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
        this.context = new Mockery();
        this.localStoreMock = context.mock(IFileStore.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testLastChangeHappyCase() throws Throwable
    {
        final StoreItem item = new StoreItem("some-directory");
        final long age = 212;
        long timeout = 100;
        LastChangeWrapper lastChangeWrapper =
                new FileStoreRemoteMounted.LastChangeWrapper(localStoreMock, timeout);
        final PathLastChangedCheckerDelayed lastChangedSleepAction =
                new PathLastChangedCheckerDelayed(timeout / 2); // return before timeout
        context.checking(new Expectations()
            {
                {
                    one(localStoreMock).lastChangedRelative(item, age);
                    will(lastChangedSleepAction);
                }
            });

        NumberStatus result = lastChangeWrapper.lastChangedInternal(item, age, true);
        AssertJUnit.assertFalse(result.isError());
        assertFalse(lastChangedSleepAction.lastCheckInterrupted());
        context.assertIsSatisfied();
    }

    @Test(groups = "broken")
    public void testLastChangedBlockedAndTerminated() throws Throwable
    {
        final StoreItem item = new StoreItem("some-directory");
        final long age = 212;
        long timeout = 100;
        LastChangeWrapper lastChangeWrapper =
                new FileStoreRemoteMounted.LastChangeWrapper(localStoreMock, timeout);
        final PathLastChangedCheckerDelayed lastChangedSleepAction =
                new PathLastChangedCheckerDelayed(timeout * 2);
        context.checking(new Expectations()
            {
                {
                    one(localStoreMock).lastChanged(item, age);
                    will(lastChangedSleepAction);
                }
            });

        NumberStatus result = lastChangeWrapper.lastChangedInternal(item, age, false);
        assertTrue(result.isError());
        assertTrue(lastChangedSleepAction.lastCheckInterrupted());
        context.assertIsSatisfied();
    }

    private final static class PathLastChangedCheckerDelayed implements Action
    {
        private final long delayMillis;

        private volatile boolean interrupted;

        public PathLastChangedCheckerDelayed(long delayMillis)
        {
            this.interrupted = false;
            this.delayMillis = delayMillis;
        }

        public Object invoke(Invocation invocation) throws Throwable
        {
            try
            {
                Thread.sleep(delayMillis); // Wait predefined time.
            } catch (InterruptedException e)
            {
                this.interrupted = true;
                // That is what we expect if we are terminated.
                throw new CheckedExceptionTunnel(new InterruptedException(e.getMessage()));
            }
            this.interrupted = false;
            return NumberStatus.create(System.currentTimeMillis());
        }

        synchronized boolean lastCheckInterrupted()
        {
            return interrupted;
        }

        public void describeTo(Description description)
        {
            description.appendText(toString());
        }
    }
}
