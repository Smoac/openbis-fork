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

package ch.systemsx.cisd.cina.client.util.cli;

import java.util.ArrayList;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cina.client.util.cli.CommandSampleLister;
import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.cina.shared.constants.CinaConstants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ICommand;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleListerCommandTest extends AssertJUnit
{
    private final class MockCommandSampleLister extends CommandSampleLister
    {

        @Override
        protected ICinaUtilities login()
        {
            return facade;
        }

    }

    private final static String USER_ID = "userid";

    private final static String PASSWORD = "password";

    private final static String SESSION_TOKEN = "sessionToken";

    private Mockery context;

    private ICinaUtilities facade;

    private IGeneralInformationService service;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        service = context.mock(IGeneralInformationService.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testCodePath()
    {
        context.checking(new Expectations()
            {
                {
                    final SearchCriteria searchCriteria = new SearchCriteria();
                    searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                            MatchClauseAttribute.TYPE, CinaConstants.REPLICA_SAMPLE_TYPE_CODE));

                    final ArrayList<Sample> samples = new ArrayList<Sample>();

                    one(service).tryToAuthenticateForAllServices(USER_ID, PASSWORD);
                    will(returnValue(SESSION_TOKEN));

                    one(service).getMinorVersion();
                    will(returnValue(1));

                    one(service).searchForSamples(SESSION_TOKEN, searchCriteria);
                    will(returnValue(samples));

                    one(service).logout(SESSION_TOKEN);
                }
            });
        facade =
                ch.systemsx.cisd.cina.client.util.v1.impl.CinaUtilitiesFacadeTest.createFacade(
                        service, USER_ID, PASSWORD);
        ICommand command = new MockCommandSampleLister();

        int exitCode = command.execute(new String[]
            { "-s", "url", "-u", USER_ID, "-p", PASSWORD });

        assertEquals(0, exitCode);
        context.assertIsSatisfied();
    }

    @Test
    public void testOldVersion()
    {
        context.checking(new Expectations()
            {
                {
                    final SearchCriteria searchCriteria = new SearchCriteria();
                    searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                            MatchClauseAttribute.TYPE, CinaConstants.REPLICA_SAMPLE_TYPE_CODE));

                    one(service).tryToAuthenticateForAllServices(USER_ID, PASSWORD);
                    will(returnValue(SESSION_TOKEN));

                    // The service used wasn't available in version 0
                    one(service).getMinorVersion();
                    will(returnValue(0));

                    one(service).logout(SESSION_TOKEN);
                }
            });
        facade =
                ch.systemsx.cisd.cina.client.util.v1.impl.CinaUtilitiesFacadeTest.createFacade(
                        service, USER_ID, PASSWORD);
        ICommand command = new MockCommandSampleLister();

        try
        {
            command.execute(new String[]
                { "-s", "url", "-u", USER_ID, "-p", PASSWORD });
            fail("Command should throw an exception when run against an older version of the interface.");
        } catch (EnvironmentFailureException e)
        {
            assertEquals("Server does not support this feature.", e.getMessage());
        }
    }
}
