/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.api.v1.client.impl;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.api.v1.client.impl.OpenBisServiceFactory.ILimsServiceStubFactory;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class OpenBisServiceFactoryTest extends AssertJUnit
{
    private Mockery context;

    private IETLLIMSService openBisService;

    private ILimsServiceStubFactory stubFactory;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        openBisService = context.mock(IETLLIMSService.class);
        stubFactory = context.mock(ILimsServiceStubFactory.class);
    }

    @Test
    public void testBasicUse()
    {
        context.checking(new Expectations()
            {
                {
                    one(stubFactory).createServiceStub(
                            "http://localhost:8888/openbis/openbis/rmi-etl");
                    will(returnValue(openBisService));
                    one(openBisService).getVersion();
                }
            });
        OpenBisServiceFactory factory =
                new OpenBisServiceFactory("http://localhost:8888", stubFactory);
        factory.createService();
        context.assertIsSatisfied();
    }

    @Test
    public void testAlternateLocation()
    {
        context.checking(new Expectations()
            {
                {
                    one(stubFactory).createServiceStub(
                            "http://localhost:8888/openbis/openbis/rmi-etl");
                    will(returnValue(openBisService));
                    one(openBisService).getVersion();
                    will(throwException(new Exception()));
                    one(stubFactory).createServiceStub("http://localhost:8888/openbis/rmi-etl");
                    will(returnValue(openBisService));
                    one(openBisService).getVersion();
                }
            });
        OpenBisServiceFactory factory =
                new OpenBisServiceFactory("http://localhost:8888", stubFactory);
        factory.createService();
        context.assertIsSatisfied();
    }

    @Test
    public void testLocationAlreadySpecified()
    {
        context.checking(new Expectations()
            {
                {
                    one(stubFactory).createServiceStub(
                            "http://localhost:8888/openbis/openbis/rmi-etl");
                    will(returnValue(openBisService));
                    one(openBisService).getVersion();
                }
            });
        OpenBisServiceFactory factory =
                new OpenBisServiceFactory("http://localhost:8888/openbis/openbis", stubFactory);
        factory.createService();
        context.assertIsSatisfied();
    }
}
