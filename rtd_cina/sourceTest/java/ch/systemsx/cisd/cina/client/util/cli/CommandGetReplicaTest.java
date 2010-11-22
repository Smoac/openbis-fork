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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cina.client.util.v1.ICinaUtilities;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ICommand;
import ch.systemsx.cisd.openbis.dss.client.api.cli.ResultCode;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CommandGetReplicaTest extends AssertJUnit
{
    private final class MockCommandGetReplica extends CommandGetReplica
    {
        @Override
        protected ICinaUtilities login()
        {
            facade =
                    ch.systemsx.cisd.cina.client.util.v1.impl.CinaUtilitiesFacadeTest.createFacade(
                            service, openbisService, USER_ID, PASSWORD);
            return facade;
        }
    }

    private final static String USER_ID = "userid";

    private final static String PASSWORD = "password";

    private final static String SESSION_TOKEN = "sessionToken";

    private Mockery context;

    private ICinaUtilities facade;

    private IGeneralInformationService service;

    private IETLLIMSService openbisService;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        service = context.mock(IGeneralInformationService.class);
        openbisService = context.mock(IETLLIMSService.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    private void setupAuthenticationExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryToAuthenticateForAllServices(USER_ID, PASSWORD);
                    will(returnValue(SESSION_TOKEN));
                    one(service).logout(SESSION_TOKEN);
                }
            });

    }

    @Test
    public void testCodePath()
    {
        setupAuthenticationExpectations();
        context.checking(new Expectations()
            {
                {
                    // final ArrayList<Project> projects = new ArrayList<Project>();
                    // Project project = new Project("PROJECT-1", "SPACE-1");
                    // projects.add(project);
                    //
                    // final ArrayList<Experiment> experiments = new ArrayList<Experiment>();
                    //
                    // final ArrayList<SpaceWithProjectsAndRoleAssignments> spaces =
                    // new ArrayList<SpaceWithProjectsAndRoleAssignments>();
                    // SpaceWithProjectsAndRoleAssignments space =
                    // new SpaceWithProjectsAndRoleAssignments("SPACE-1");
                    // space.add(project);
                    // space.add("user", new Role("ADMIN", true));
                    // spaces.add(space);

                    // one(service).getMinorVersion();
                    // will(returnValue(2));

                    // one(service).listSpacesWithProjectsAndRoleAssignments(SESSION_TOKEN, null);
                    // will(returnValue(spaces));
                    //
                    // one(service).listExperiments(SESSION_TOKEN, projects, "EXP-TYPE");
                    // will(returnValue(experiments));

                }
            });

        ICommand command = new MockCommandGetReplica();

        ResultCode exitCode = command.execute(new String[]
            { "-s", "url", "-u", USER_ID, "-p", PASSWORD, "REPLICA-ID" });

        assertEquals(ResultCode.OK, exitCode);
        context.assertIsSatisfied();
    }

    @Test
    public void testOutputFolder()
    {
        setupAuthenticationExpectations();

        ICommand command = new MockCommandGetReplica();

        ResultCode exitCode = command.execute(new String[]
            { "-s", "url", "-u", USER_ID, "-p", PASSWORD, "-o", "Foo.bundle/", "REPLICA-ID" });

        assertEquals(ResultCode.OK, exitCode);
        context.assertIsSatisfied();
    }

    @Test
    public void testMultipleReplicas()
    {
        setupAuthenticationExpectations();

        ICommand command = new MockCommandGetReplica();

        ResultCode exitCode = command.execute(new String[]
            { "-s", "url", "-u", USER_ID, "-p", PASSWORD, "REPLICA-ID1", "REPLICA-ID2" });

        assertEquals(ResultCode.OK, exitCode);
        context.assertIsSatisfied();
    }
}
