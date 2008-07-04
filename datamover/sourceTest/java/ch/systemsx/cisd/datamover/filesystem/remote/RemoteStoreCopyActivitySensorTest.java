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

package ch.systemsx.cisd.datamover.filesystem.remote;

import static org.testng.AssertJUnit.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.DateStatus;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;

/**
 * Test cases for the {@link RemoteStoreCopyActivitySensor}.
 * 
 * @author Bernd Rinn
 */
public class RemoteStoreCopyActivitySensorTest
{

    private static final String ITEM_NAME = "I am probed";

    private static final long THRESHOLD = 123L;

    private static final long MAX_DELTA = 5L;

    private Mockery context;

    private IFileStore destinationStore;

    private StoreItem copyItem;

    private RemoteStoreCopyActivitySensor sensorUnderTest;

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
    }

    @BeforeMethod
    public final void beforeMethod()
    {
        context = new Mockery();
        destinationStore = context.mock(IFileStore.class);
        copyItem = new StoreItem(ITEM_NAME);
        sensorUnderTest = new RemoteStoreCopyActivitySensor(destinationStore, copyItem);
    }

    @AfterMethod
    public final void afterMethod() throws Throwable
    {
        // To following lines of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testHappyCase()
    {
        final long lastChanged = 1111L;
        context.checking(new Expectations()
            {
                {
                    one(destinationStore).lastChangedRelative(copyItem, THRESHOLD);
                    will(returnValue(DateStatus.create(lastChanged)));
                }
            });
        final long delta =
                System.currentTimeMillis()
                        - sensorUnderTest.getTimeOfLastActivityMoreRecentThan(THRESHOLD);
        assertTrue("Delta is " + delta, delta < MAX_DELTA);
        final String msg = sensorUnderTest.describeInactivity(System.currentTimeMillis());
        assertTrue(
                msg,
                msg
                        .startsWith("No write activity on item 'I am probed' in store 'iFileStore' for 0:00:00.0"));
    }

    @Test
    public void testOneError()
    {
        final String errorMsg = "ERROR message";
        context.checking(new Expectations()
            {
                {
                    one(destinationStore).lastChangedRelative(copyItem, THRESHOLD);
                    will(returnValue(DateStatus.createError(errorMsg)));
                }
            });
        final long now = System.currentTimeMillis();
        final long delta = now - sensorUnderTest.getTimeOfLastActivityMoreRecentThan(THRESHOLD);
        assertTrue("Delta is " + delta, delta < MAX_DELTA);
        assertEquals("Error [ERROR message]: Unable to determine the time of write activity on "
                + "item 'I am probed' in store 'iFileStore'", sensorUnderTest
                .describeInactivity(now));
    }

    @Test
    public void testThreeErrors()
    {
        final String errorMsg = "ERROR message";
        context.checking(new Expectations()
            {
                {
                    exactly(3).of(destinationStore).lastChangedRelative(copyItem, THRESHOLD);
                    will(returnValue(DateStatus.createError(errorMsg)));
                }
            });
        ConcurrencyUtilities.sleep(10L);
        final long now = System.currentTimeMillis();
        final long lastActivity1 = sensorUnderTest.getTimeOfLastActivityMoreRecentThan(THRESHOLD);
        assertEquals(now, lastActivity1);
        ConcurrencyUtilities.sleep(10L);
        final long lastActivity2 = sensorUnderTest.getTimeOfLastActivityMoreRecentThan(THRESHOLD);
        assertEquals(now, lastActivity2);
        ConcurrencyUtilities.sleep(10L);
        final long lastActivity3 = sensorUnderTest.getTimeOfLastActivityMoreRecentThan(THRESHOLD);
        assertEquals(now, lastActivity3);
    }

    @Test
    public void testValueErrorValue()
    {
        final String errorMsg = "ERROR message";
        context.checking(new Expectations()
            {
                {
                    exactly(3).of(destinationStore).lastChangedRelative(copyItem, THRESHOLD);
                    will(onConsecutiveCalls(returnValue(DateStatus.create(17L)),
                            returnValue(DateStatus.createError(errorMsg)), returnValue(DateStatus
                                    .create(17L))));
                }
            });
        ConcurrencyUtilities.sleep(10L);
        final long now1 = System.currentTimeMillis();
        final long lastActivity1 = sensorUnderTest.getTimeOfLastActivityMoreRecentThan(THRESHOLD);
        assertEquals(now1, lastActivity1);
        ConcurrencyUtilities.sleep(10L);
        final long now2 = System.currentTimeMillis();
        final long lastActivity2 = sensorUnderTest.getTimeOfLastActivityMoreRecentThan(THRESHOLD);
        assertEquals(now2, lastActivity2);
        ConcurrencyUtilities.sleep(10L);
        final long lastActivity3 = sensorUnderTest.getTimeOfLastActivityMoreRecentThan(THRESHOLD);
        assertEquals(now1, lastActivity3);
    }

}
