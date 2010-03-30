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

package ch.systemsx.cisd.openbis.dss.rpc.client;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.rpc.client.DssComponent;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class DssComponentTest extends AssertJUnit
{
    private Mockery context;

    private IETLLIMSService service;

    private DssComponent dssComponent;

    private static final String DUMMY_SESSSION_TOKEN = "DummySessionToken";

    public DssComponentTest()
    {

    }

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        service = context.mock(IETLLIMSService.class);
        dssComponent = new DssComponent(service);
    }

    @Test
    public void testLogin()
    {
        final SessionContextDTO session = getDummySession();

        context.checking(new Expectations()
            {
                {
                    one(service).tryToAuthenticate("foo", "bar");
                    will(returnValue(session));
                }
            });

        dssComponent.login("foo", "bar");
    }

    @Test
    public void testGetDataSet()
    {
        final SessionContextDTO session = getDummySession();

        context.checking(new Expectations()
            {
                {
                    one(service).tryToAuthenticate("foo", "bar");
                    will(returnValue(session));
                    one(service).tryGetDataSet(DUMMY_SESSSION_TOKEN, "DummyDataSetCode");
                    will(returnValue(new ExternalData()));
                }
            });

        dssComponent.login("foo", "bar");
        dssComponent.getDataSet("DummyDataSetCode");
    }

    private SessionContextDTO getDummySession()
    {
        final SessionContextDTO session = new SessionContextDTO();
        session.setSessionToken(DUMMY_SESSSION_TOKEN);
        return session;
    }
}
