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

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = GenericServer.class)
public class GenericServerTest
{
    private static final String HOME_DATABASE_INSTANCE_CODE = "HOME_DATABASE";

    private static final String USER_ID = "test";

    private static final Principal PRINCIPAL = new Principal(USER_ID, "john", "doe", "j@d");

    private static final String SESSION_TOKEN = "session-token";

    private Mockery context;

    private IDAOFactory daoFactory;

    private IAuthenticationService authenticationService;

    private ISessionManager<Session> sessionManager;

    private IGenericBusinessObjectFactory boFactory;

    private IDatabaseInstanceDAO databaseInstanceDAO;

    private IPersonDAO personDAO;

    private IRoleAssignmentDAO roleAssignmentDAO;

    private DatabaseInstancePE homeDatabaseInstance;

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
        roleAssignmentDAO = context.mock(IRoleAssignmentDAO.class);
        boFactory = context.mock(IGenericBusinessObjectFactory.class);
        
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
    
    private PersonPE createPersonFromPrincipal(Principal principal)
    {
        final PersonPE person = new PersonPE();
        person.setUserId(principal.getUserId());
        person.setFirstName(principal.getFirstName());
        person.setLastName(principal.getLastName());
        person.setEmail(principal.getEmail());
        return person;
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
    
    @Test
    public void testLogout()
    {
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).closeSession(SESSION_TOKEN);
                }
            });
        createServer().logout(SESSION_TOKEN);
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testTryToAuthenticateWhichFailed()
    {
        final String user = "user";
        final String password = "password";
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(null));
                }
            });
        
        assertEquals(null, createServer().tryToAuthenticate(user, password));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testFirstTryToAuthenticate()
    {
        final String user = "user";
        final String password = "password";
        final Session session = createExampleSession();
        final PersonPE systemPerson = new PersonPE();
        final PersonPE person = createPersonFromPrincipal(PRINCIPAL);
        final RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        roleAssignment.setPerson(person);
        roleAssignment.setDatabaseInstance(homeDatabaseInstance);
        roleAssignment.setRegistrator(systemPerson);
        roleAssignment.setRole(RoleCode.ADMIN);
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(SESSION_TOKEN));
                    
                    one(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));
                    
                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(systemPerson))); // only 'system' in database
                    
                    one(personDAO).tryFindPersonByUserId(user); // first login
                    will(returnValue(null)); 
                    
                    one(personDAO).createPerson(person);
                    
                    one(roleAssignmentDAO).createRoleAssignment(roleAssignment);
                    
                }
            });
        
        final Session s = createServer().tryToAuthenticate(user, password);
        
        assertEquals(person, s.tryGetPerson());
        assertEquals(roleAssignment, s.tryGetPerson().getRoleAssignments().get(0));
        
        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterPerson()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).tryFindPersonByUserId(USER_ID);
                    will(returnValue(null));
                    
                    String applicationToken = "application-token";
                    one(authenticationService).authenticateApplication();
                    will(returnValue(applicationToken));
                    
                    one(authenticationService).getPrincipal(applicationToken, USER_ID);
                    will(returnValue(PRINCIPAL));
                    
                    PersonPE person = createPersonFromPrincipal(PRINCIPAL);
                    one(personDAO).createPerson(person);
                }
            });
        
        createServer().registerPerson(SESSION_TOKEN, USER_ID);
    }
}
