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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test cases for the {@link ProcessRunner}.
 * 
 * @author Christian Ribeaud
 */
public class ProcessRunnerTest
{

    private Mockery context;

    private IProcess process;

    @BeforeMethod
    public final void beforeMethod()
    {
        context = new Mockery();
        process = context.mock(IProcess.class);
    }

    @AfterMethod
    public final void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testRunSuccessfulProcess()
    {
        context.checking(new Expectations()
            {
                {
                    one(process).run();

                    one(process).succeeded();
                    will(returnValue(true));

                    one(process).getMillisToSleepOnFailure();
                    will(returnValue(1L));

                    one(process).getMaxRetryOnFailure();
                    will(returnValue(2));
                }
            });
        new ProcessRunner(process);
        context.assertIsSatisfied();
    }

    @Test
    public final void testRunUnsuccessfulProcess()
    {
        final int tries = 3;
        context.checking(new Expectations()
            {
                {
                    exactly(tries).of(process).run();

                    exactly(tries - 1).of(process).succeeded();
                    will(returnValue(false));

                    one(process).getMillisToSleepOnFailure();
                    will(returnValue(1L));

                    one(process).getMaxRetryOnFailure();
                    will(returnValue(tries));
                }
            });
        new ProcessRunner(process);
        context.assertIsSatisfied();
    }
}
