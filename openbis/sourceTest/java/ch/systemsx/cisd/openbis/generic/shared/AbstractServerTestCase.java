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

package ch.systemsx.cisd.openbis.generic.shared;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExperimentTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * An <i>abstract</i> test infrastructure for {@link IServer} implementations.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractServerTestCase extends AssertJUnit
{
    protected static final Principal PRINCIPAL = new Principal(CommonTestUtils.USER_ID, "john", "doe", "j@d");

    protected static final String SESSION_TOKEN = "session-token";

    protected Mockery context;

    protected IDAOFactory daoFactory;

    protected IAuthenticationService authenticationService;

    protected ISessionManager<Session> sessionManager;

    protected IDatabaseInstanceDAO databaseInstanceDAO;

    protected IPersonDAO personDAO;

    protected IRoleAssignmentDAO roleAssignmentDAO;

    protected DatabaseInstancePE homeDatabaseInstance;

    protected IGroupDAO groupDAO;

    protected IExternalDataDAO externalDataDAO;

    protected ISampleDAO sampleDAO;

    protected IGroupBO groupBO;

    protected ISampleBO sampleBO;

    protected IExternalDataTable externalDataTable;

    protected IExperimentTable experimentTable;

    protected IEntityTypeDAO experimentTypeDAO;

    protected IProjectDAO projectDAO;

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp()
    {
        context = new Mockery();
        authenticationService = context.mock(IAuthenticationService.class);
        sessionManager = context.mock(ISessionManager.class);

        daoFactory = context.mock(IDAOFactory.class);
        databaseInstanceDAO = context.mock(IDatabaseInstanceDAO.class);
        personDAO = context.mock(IPersonDAO.class);
        groupDAO = context.mock(IGroupDAO.class);
        sampleDAO = context.mock(ISampleDAO.class);
        roleAssignmentDAO = context.mock(IRoleAssignmentDAO.class);
        externalDataDAO = context.mock(IExternalDataDAO.class);

        groupBO = context.mock(IGroupBO.class);
        sampleBO = context.mock(ISampleBO.class);
        externalDataTable = context.mock(IExternalDataTable.class);

        experimentTable = context.mock(IExperimentTable.class);
        experimentTypeDAO = context.mock(IEntityTypeDAO.class);

        projectDAO = context.mock(IProjectDAO.class);

        homeDatabaseInstance =
                CommonTestUtils.createDatabaseInstance(CommonTestUtils.HOME_DATABASE_INSTANCE_CODE);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(homeDatabaseInstance));
                    allowing(daoFactory).getDatabaseInstanceDAO();
                    will(returnValue(databaseInstanceDAO));
                    allowing(daoFactory).getPersonDAO();
                    will(returnValue(personDAO));
                    allowing(daoFactory).getGroupDAO();
                    will(returnValue(groupDAO));
                    allowing(daoFactory).getRoleAssignmentDAO();
                    will(returnValue(roleAssignmentDAO));
                }
            });
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    protected Session createExampleSession()
    {
        return new Session(CommonTestUtils.USER_ID, SESSION_TOKEN, PRINCIPAL, "remote-host", 1);
    }

    protected Session prepareGetSession()
    {
        final Session session = createExampleSession();
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));
                }
            });
        return session;
    }
}
