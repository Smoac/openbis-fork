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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Test cases for corresponding {@link CommonServer} class.
 * 
 * @author Franz-Josef Elmer
 */
public final class CommonServerTest extends AbstractServerTestCase
{

    private final ICommonServer createServer()
    {
        return new CommonServer(authenticationService, sessionManager, daoFactory, commonBusinessObjectFactory);
    }

    private final static PersonPE createSystemUser()
    {
        final PersonPE systemPerson = new PersonPE();
        systemPerson.setUserId(PersonPE.SYSTEM_USER_ID);
        return systemPerson;
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
        final PersonPE systemPerson = createSystemUser();
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
                    one(personDAO).updatePerson(person);
                }
            });

        final Session s = createServer().tryToAuthenticate(user, password);

        assertEquals(person, s.tryGetPerson());
        assertEquals(roleAssignment, s.tryGetPerson().getRoleAssignments().iterator().next());

        context.assertIsSatisfied();
    }

    @Test
    public void testFirstTryToAuthenticateButNotFirstUser()
    {
        final String user = "user";
        final String password = "password";
        final Session session = createExampleSession();
        final PersonPE systemPerson = createSystemUser();
        final PersonPE person = createPersonFromPrincipal(PRINCIPAL);
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(SESSION_TOKEN));

                    one(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));

                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(systemPerson, person)));

                    one(personDAO).tryFindPersonByUserId(user); // first login
                    will(returnValue(null));

                    one(personDAO).createPerson(person);
                }
            });

        final Session s = createServer().tryToAuthenticate(user, password);

        assertEquals(person, s.tryGetPerson());

        context.assertIsSatisfied();
    }

    @Test
    public void testTryToAuthenticate()
    {
        final String user = "user";
        final String password = "password";
        final Session session = createExampleSession();
        final PersonPE systemPerson = createSystemUser();
        final PersonPE person = createPersonFromPrincipal(PRINCIPAL);
        context.checking(new Expectations()
            {
                {
                    one(sessionManager).tryToOpenSession(user, password);
                    will(returnValue(SESSION_TOKEN));

                    one(sessionManager).getSession(SESSION_TOKEN);
                    will(returnValue(session));

                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(systemPerson, person)));

                    one(personDAO).tryFindPersonByUserId(user);
                    will(returnValue(person));
                }
            });
        assertEquals(null, session.tryGetPerson());

        final Session s = createServer().tryToAuthenticate(user, password);

        assertSame(session, s);
        assertEquals(person, s.tryGetPerson());

        context.assertIsSatisfied();
    }

    @Test
    public void testListGroups()
    {
        final PersonPE person = createPersonFromPrincipal(PRINCIPAL);
        final DatabaseInstanceIdentifier identifier = DatabaseInstanceIdentifier.createHome();
        final GroupPE g1 = createGroup("g1", homeDatabaseInstance);
        final GroupPE g2 = createGroup("g2", homeDatabaseInstance);
        final Session session = prepareGetSession();
        session.setPerson(person);
        person.setHomeGroup(g1);
        g1.setId(42L);
        context.checking(new Expectations()
            {
                {
                    one(groupDAO).listGroups(homeDatabaseInstance);
                    will(returnValue(Arrays.asList(g1, g2)));
                }
            });

        final List<GroupPE> groups = createServer().listGroups(SESSION_TOKEN, identifier);

        assertSame(g1, groups.get(0));
        assertSame(g2, groups.get(1));
        assertEquals(2, groups.size());
        assertEquals(true, g1.isHome().booleanValue());
        assertEquals(false, g2.isHome().booleanValue());

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterGroup()
    {
        final Session session = prepareGetSession();
        final String groupCode = "group";
        final String description = "description";
        final String leader = "leader";
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createGroupBO(session);
                    will(returnValue(groupBO));

                    one(groupBO).define(groupCode, description, leader);
                    one(groupBO).save();
                }
            });

        createServer().registerGroup(SESSION_TOKEN, groupCode, description, leader);

        context.assertIsSatisfied();
    }

    @Test
    public void testListPersons()
    {
        final PersonPE person = createPersonFromPrincipal(PRINCIPAL);
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(person)));
                }
            });

        final List<PersonPE> persons = createServer().listPersons(SESSION_TOKEN);

        assertSame(person, persons.get(0));
        assertEquals(1, persons.size());

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

                    final String applicationToken = "application-token";
                    one(authenticationService).authenticateApplication();
                    will(returnValue(applicationToken));

                    one(authenticationService).getPrincipal(applicationToken, USER_ID);
                    will(returnValue(PRINCIPAL));

                    final PersonPE person = createPersonFromPrincipal(PRINCIPAL);
                    one(personDAO).createPerson(person);
                }
            });

        createServer().registerPerson(SESSION_TOKEN, USER_ID);

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterExistingPerson()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).tryFindPersonByUserId(USER_ID);
                    will(returnValue(createPersonFromPrincipal(PRINCIPAL)));
                }
            });

        try
        {
            createServer().registerPerson(SESSION_TOKEN, USER_ID);
            fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Person '" + USER_ID + "' already exists.", e.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testRegisterUnknownPerson()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(personDAO).tryFindPersonByUserId(USER_ID);
                    will(returnValue(null));

                    final String applicationToken = "application-token";
                    one(authenticationService).authenticateApplication();
                    will(returnValue(applicationToken));

                    one(authenticationService).getPrincipal(applicationToken, USER_ID);
                    will(throwException(new IllegalArgumentException()));
                }
            });

        try
        {
            createServer().registerPerson(SESSION_TOKEN, USER_ID);
            fail("UserFailureException expected");
        } catch (final UserFailureException e)
        {
            assertEquals("Person '" + USER_ID + "' unknown by the authentication service.", e
                    .getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testListRoles()
    {
        prepareGetSession();
        final RoleAssignmentPE role = new RoleAssignmentPE();
        context.checking(new Expectations()
            {
                {
                    one(roleAssignmentDAO).listRoleAssignments();
                    will(returnValue(Arrays.asList(role)));
                }
            });

        final List<RoleAssignmentPE> roles = createServer().listRoles(SESSION_TOKEN);

        assertSame(role, roles.get(0));
        assertEquals(1, roles.size());

        context.assertIsSatisfied();
    }

    @Test
    public final void testListExternalData()
    {
        final Session session = prepareGetSession();
        final SampleIdentifier sampleIdentifier = createSampleIdentifier();
        context.checking(new Expectations()
            {
                {
                    one(commonBusinessObjectFactory).createExternalDataTable(session);
                    will(returnValue(externalDataTable));

                    one(externalDataTable).loadBySampleIdentifier(sampleIdentifier);

                    one(externalDataTable).getExternalData();
                    will(returnValue(new ArrayList<DataPE>()));
                }
            });
        createServer().listExternalData(SESSION_TOKEN, sampleIdentifier);
        context.assertIsSatisfied();
    }

}
