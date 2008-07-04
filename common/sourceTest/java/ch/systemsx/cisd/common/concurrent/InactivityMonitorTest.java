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

package ch.systemsx.cisd.common.concurrent;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.InactivityMonitor.IActivitySensor;
import ch.systemsx.cisd.common.concurrent.InactivityMonitor.IInactivityObserver;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.StoringUncaughtExceptionHandler;

/**
 * Test cases for the inactivity monitor.
 * 
 * @author Bernd Rinn
 */
public class InactivityMonitorTest
{
    private final static long CHECK_INTERVAL_MILLIS = 10L;

    private final static long INACTIVITY_THRESHOLD_MILLIS = 20L;

    private static final long TIME_TO_WAIT_MILLIS = 4 * INACTIVITY_THRESHOLD_MILLIS;

    private final static long DELTA = 10L;

    private Mockery context;

    private IActivitySensor sensor;

    private IInactivityObserver observer;

    private InactivityMonitor monitorUnderTest;

    private StoringUncaughtExceptionHandler exceptionHandler;

    private final class NowMatcher extends BaseMatcher<Long>
    {
        final long delta;

        NowMatcher()
        {
            this.delta = DELTA;
        }

        public boolean matches(Object item)
        {
            final long actual = (Long) item;
            return System.currentTimeMillis() - actual < delta;
        }

        public void describeTo(Description description)
        {
            description.appendValue("now");
        }
    }

    private final class CloseEnoughMatcher extends BaseMatcher<Long>
    {
        final long value;

        final long delta;

        CloseEnoughMatcher(long value)
        {
            this.value = value;
            this.delta = DELTA;
        }

        public boolean matches(Object item)
        {
            final long actual = (Long) item;
            return Math.abs(value - actual) < delta;
        }

        public void describeTo(Description description)
        {
            description.appendValue("Close enough to " + value);
        }
    }

    private final class ReturnNowMinus extends CustomAction
    {
        final long lagTimeMillis;
        
        ReturnNowMinus(long lagTimeMillis)
        {
            super("returns now - " + lagTimeMillis);
            this.lagTimeMillis = lagTimeMillis;
        }
        
        public Object invoke(Invocation invocation) throws Throwable
        {
            return (System.currentTimeMillis() - lagTimeMillis);
        }

    }

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
        exceptionHandler = new StoringUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
    }

    @BeforeMethod
    public final void beforeMethod()
    {
        exceptionHandler.reset();
        context = new Mockery();
        sensor = context.mock(IActivitySensor.class);
        observer = context.mock(IInactivityObserver.class);
    }

    @AfterMethod
    public final void afterMethod() throws Throwable
    {
        if (monitorUnderTest != null)
        {
            monitorUnderTest.stop();
        }
        // To following lines of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        exceptionHandler.checkAndRethrowException();
        context.assertIsSatisfied();
    }

    @Test
    public void testHappyCase() throws Throwable
    {
        context.checking(new Expectations()
            {
                {
                    atLeast(1).of(sensor).getTimeOfLastActivityMoreRecentThan(
                            INACTIVITY_THRESHOLD_MILLIS);
                    will(new ReturnNowMinus(0L));
                }
            });
        monitorUnderTest =
                new InactivityMonitor(sensor, observer, CHECK_INTERVAL_MILLIS,
                        INACTIVITY_THRESHOLD_MILLIS, true);
        ConcurrencyUtilities.sleep(TIME_TO_WAIT_MILLIS);
        monitorUnderTest.stop();
        exceptionHandler.checkAndRethrowException();
        context.assertIsSatisfied();
    }

    @Test
    public void testInactivity() throws Throwable
    {
        final String descriptionOfInactivity = "DESCRIPTION";
        context.checking(new Expectations()
            {
                {
                    one(sensor).getTimeOfLastActivityMoreRecentThan(INACTIVITY_THRESHOLD_MILLIS);
                    will(new ReturnNowMinus(2 * INACTIVITY_THRESHOLD_MILLIS));
                    one(sensor).describeInactivity(with(new NowMatcher()));
                    will(returnValue(descriptionOfInactivity));
                    one(observer).update(
                            with(new CloseEnoughMatcher(INACTIVITY_THRESHOLD_MILLIS * 2)),
                            with(equal(descriptionOfInactivity)));
                }
            });
        monitorUnderTest =
                new InactivityMonitor(sensor, observer, CHECK_INTERVAL_MILLIS,
                        INACTIVITY_THRESHOLD_MILLIS, true);
        ConcurrencyUtilities.sleep(TIME_TO_WAIT_MILLIS);
        monitorUnderTest.stop();
        exceptionHandler.checkAndRethrowException();
        context.assertIsSatisfied();
    }

    @Test
    public void testInactivityMultipleTimes() throws Throwable
    {
        final String descriptionOfInactivity = "DESCRIPTION";
        context.checking(new Expectations()
            {
                {
                    atLeast(3).of(sensor).getTimeOfLastActivityMoreRecentThan(INACTIVITY_THRESHOLD_MILLIS);
                    will(new ReturnNowMinus(2 * INACTIVITY_THRESHOLD_MILLIS));
                    atLeast(3).of(sensor).describeInactivity(with(new NowMatcher()));
                    will(returnValue(descriptionOfInactivity));
                    atLeast(3).of(observer).update(
                            with(new CloseEnoughMatcher(INACTIVITY_THRESHOLD_MILLIS * 2)),
                            with(equal(descriptionOfInactivity)));
                }
            });
        monitorUnderTest =
                new InactivityMonitor(sensor, observer, CHECK_INTERVAL_MILLIS,
                        INACTIVITY_THRESHOLD_MILLIS, false);
        ConcurrencyUtilities.sleep(TIME_TO_WAIT_MILLIS);
        monitorUnderTest.stop();
        exceptionHandler.checkAndRethrowException();
        context.assertIsSatisfied();
    }
}
