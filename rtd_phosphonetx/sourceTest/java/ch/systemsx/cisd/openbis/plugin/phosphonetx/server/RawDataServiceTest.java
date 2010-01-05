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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataServiceInternal;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class RawDataServiceTest extends AbstractServerTestCase
{
    private IRawDataServiceInternal internalService;
    private IRawDataService service;
    private SessionContextDTO session2;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        internalService = context.mock(IRawDataServiceInternal.class);
        service = new RawDataService(sessionManager, daoFactory, internalService);
        session2 = new SessionContextDTO();
        session2.setSessionToken(SESSION_TOKEN + "2");
    }
    
    @Test
    public void testListRawDataSamplesForUnknownUser()
    {
        prepareGetSession();
        prepareLoginLogout(null);

        try
        {
            service.listRawDataSamples(SESSION_TOKEN, "abc");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Unknown user ID: abc", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListRawDataSamples()
    {
        prepareGetSession();
        prepareLoginLogout(session2);
        final Sample sample = new Sample();
        context.checking(new Expectations()
            {
                {
                    one(internalService).listRawDataSamples(session2.getSessionToken());
                    will(returnValue(Arrays.asList(sample)));
                }
            });

        List<Sample> samples = service.listRawDataSamples(SESSION_TOKEN, "abc");

        assertSame(sample, samples.get(0));
        assertEquals(1, samples.size());
        context.assertIsSatisfied();
    }

    @Test
    public void testCopyRawDataForUnknownUser()
    {
        prepareGetSession();
        prepareLoginLogout(null);

        try
        {
            service.copyRawData(SESSION_TOKEN, "abc", new long[0]);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Unknown user ID: abc", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testCopyRawData()
    {
        prepareGetSession();
        prepareLoginLogout(session2);
        final long[] ids = new long[] {42};
        context.checking(new Expectations()
            {
                {
                    one(internalService).copyRawData(session2.getSessionToken(), ids);
                }
            });

        service.copyRawData(SESSION_TOKEN, "abc", ids);

        context.assertIsSatisfied();
    }
    
    private void prepareLoginLogout(final SessionContextDTO session)
    {
        context.checking(new Expectations()
            {
                {
                    one(internalService).tryToAuthenticate("abc", "dummy-password");
                    will(returnValue(session));
                    
                    if (session != null)
                    {
                        one(internalService).logout(session.getSessionToken());
                    }
                }
            });
    }

}
