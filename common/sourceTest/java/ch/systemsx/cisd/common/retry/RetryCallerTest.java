/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.retry;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.springframework.remoting.RemoteConnectFailureException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author pkupczyk
 */
public class RetryCallerTest
{

    private Mockery mockery;

    private Runnable runnable;

    private RetryCaller<Object, Throwable> caller;

    @BeforeMethod
    public void beforeMethod()
    {
        mockery = new Mockery();
        runnable = mockery.mock(Runnable.class);
        caller = new RetryCaller<Object, Throwable>()
            {
                @Override
                protected Object call() throws Throwable
                {
                    runnable.run();
                    return null;
                }
            };
        caller.setRetryWaitingTime(100);
        caller.setRetryWaitingTimeFactor(2);
        caller.setRetryMaxCounter(5);
    }

    @Test
    public void testCallWithoutErrorShouldBeExecutedOnce() throws Throwable
    {
        mockery.checking(new Expectations()
            {
                {
                    oneOf(runnable).run();
                }
            });

        caller.callWithRetry();
        mockery.assertIsSatisfied();
    }

    @Test
    public void testCallWithCommunicationErrorShouldBeRetried() throws Throwable
    {
        mockery.checking(new Expectations()
            {
                {
                    oneOf(runnable).run();
                    will(throwException(new RemoteConnectFailureException("", null)));
                    oneOf(runnable).run();
                }
            });

        caller.callWithRetry();
        mockery.assertIsSatisfied();
    }

    @Test
    void testCallWithManyCommunicationErrorsShouldBeRetriedWithIncreasingWaitingTime()
            throws Throwable
    {
        final long startTime = System.currentTimeMillis();
        final long waitingTime = caller.getRetryWaitingTime();

        mockery.checking(new Expectations()
            {
                {
                    for (int i = 0; i < caller.getRetryMaxCounter() - 1; i++)
                    {
                        final int ifinal = i;
                        oneOf(runnable).run();
                        will(new CustomAction("Check waiting time and throw exception")
                            {
                                public Object invoke(Invocation invocation) throws Throwable
                                {
                                    Assert.assertTrue(System.currentTimeMillis() >= startTime
                                            + (ifinal * waitingTime));
                                    throw new RemoteConnectFailureException("", null);
                                }
                            });
                    }
                    oneOf(runnable).run();
                }
            });

        caller.callWithRetry();
        mockery.assertIsSatisfied();
    }

    @Test(expectedExceptions =
        { RemoteConnectFailureException.class })
    void testCallWithTooManyCommunicationErrorsShouldFinallyThrowException() throws Throwable
    {
        mockery.checking(new Expectations()
            {
                {
                    for (int i = 0; i < caller.getRetryMaxCounter(); i++)
                    {
                        oneOf(runnable).run();
                        will(throwException(new RemoteConnectFailureException("", null)));
                    }
                }
            });

        caller.callWithRetry();
        mockery.assertIsSatisfied();
    }

    @Test(expectedExceptions =
        { RuntimeException.class })
    void testCallWithNonCommunicationErrorShouldNotBeRetried() throws Throwable
    {
        mockery.checking(new Expectations()
            {
                {
                    oneOf(runnable).run();
                    will(throwException(new RuntimeException("", null)));
                }
            });

        caller.callWithRetry();
        mockery.assertIsSatisfied();
    }

}
