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

package ch.systemsx.cisd.openbis.generic.server;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = GenericServer.class)
public abstract class GenericServerTestCase extends AssertJUnit
{
    protected static final String HOME_DATABASE_INSTANCE_CODE = "HOME_DATABASE";

    protected static final String USER_ID = "test";

    protected static final Principal PRINCIPAL = new Principal(USER_ID, "john", "doe", "j@d");

    protected static final String SESSION_TOKEN = "session-token";

    protected Mockery context;

    protected IDAOFactory daoFactory;

    protected IAuthenticationService authenticationService;

    protected ISessionManager<Session> sessionManager;

    protected IGenericBusinessObjectFactory boFactory;

    protected IDatabaseInstanceDAO databaseInstanceDAO;

    protected IPersonDAO personDAO;

    protected IRoleAssignmentDAO roleAssignmentDAO;

    protected DatabaseInstancePE homeDatabaseInstance;

    protected IGroupDAO groupDAO;

    protected IGroupBO groupBO;

    @BeforeMethod
    @SuppressWarnings("unchecked")
    public final void setUp()
    {
        context = new Mockery();
        authenticationService = context.mock(IAuthenticationService.class);
        sessionManager = context.mock(ISessionManager.class);
        
        daoFactory = context.mock(IDAOFactory.class);
        databaseInstanceDAO = context.mock(IDatabaseInstanceDAO.class);
        personDAO = context.mock(IPersonDAO.class);
        groupDAO = context.mock(IGroupDAO.class);
        roleAssignmentDAO = context.mock(IRoleAssignmentDAO.class);
        
        boFactory = context.mock(IGenericBusinessObjectFactory.class);
        groupBO = context.mock(IGroupBO.class);
        
        homeDatabaseInstance = createDatabaseInstance(HOME_DATABASE_INSTANCE_CODE);
        context.checking(new Expectations()
            {
                {
                    allowing(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(homeDatabaseInstance));
                    allowing(daoFactory).getDatabaseInstancesDAO();
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
    public final void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    protected IGenericServer createServer()
    {
        return new GenericServer(authenticationService, sessionManager, daoFactory, boFactory);
    }
    
    protected Session createExampleSession()
    {
        return new Session(USER_ID, SESSION_TOKEN, PRINCIPAL, "remote-host", 1);
    }

    protected DatabaseInstancePE createDatabaseInstance(String code)
    {
        final DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode(code);
        return databaseInstance;
    }
    
    protected PersonPE createPersonFromPrincipal(Principal principal)
    {
        final PersonPE person = new PersonPE();
        person.setUserId(principal.getUserId());
        person.setFirstName(principal.getFirstName());
        person.setLastName(principal.getLastName());
        person.setEmail(principal.getEmail());
        return person;
    }
    
    protected GroupPE createGroup(String groupCode, DatabaseInstancePE databaseInstance)
    {
        GroupPE group = new GroupPE();
        group.setCode(groupCode);
        group.setDatabaseInstance(databaseInstance);
        return group;
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
