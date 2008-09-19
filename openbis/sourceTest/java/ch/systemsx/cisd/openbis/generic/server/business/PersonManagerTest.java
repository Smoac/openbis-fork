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

package ch.systemsx.cisd.openbis.generic.server.business;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPersonBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * Test cases for corresponding {@link PersonManager} class.
 * 
 * @author Christian Ribeaud
 */
public final class PersonManagerTest
{
    private Mockery context;

    private IAuthorizationDAOFactory daoFactory;

    private IPersonDAO personDAO;

    private IGenericBusinessObjectFactory businessObjectFactory;

    private IGroupDAO groupDAO;

    private IPersonBO personBO;

    private IGroupBO groupBO;

    private IRoleAssignmentTable roleAssignmentTable;

    private BufferedAppender logRecorder;

    private final IPersonManager createPersonManager()
    {
        ManagerTestTool.prepareGroupDAO(context, daoFactory, groupDAO);
        return new PersonManager(daoFactory, businessObjectFactory);
    }

    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        daoFactory = context.mock(IAuthorizationDAOFactory.class);
        personDAO = context.mock(IPersonDAO.class);
        personBO = context.mock(IPersonBO.class);
        groupBO = context.mock(IGroupBO.class);
        roleAssignmentTable = context.mock(IRoleAssignmentTable.class);
        groupDAO = context.mock(IGroupDAO.class);
        businessObjectFactory = context.mock(IGenericBusinessObjectFactory.class);
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public final void tearDown()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testListPersons()
    {
        final PersonPE person = ManagerTestTool.EXAMPLE_PERSON;
        final List<PersonPE> personDTOs = new ArrayList<PersonPE>();
        personDTOs.add(person);
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getPersonDAO();
                    will(returnValue(personDAO));
                    one(personDAO).listPersons();
                    will(returnValue(personDTOs));
                }
            });
        final IPersonManager personManager = createPersonManager();
        final List<PersonPE> persons = personManager.listPersons(EXAMPLE_SESSION);
        assertEquals(1, persons.size());
        assertEquals(persons.get(0), person);
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterPersonIfNecessary()
    {
        context.checking(new Expectations()
            {
                {
                    one(businessObjectFactory).createPersonBO(EXAMPLE_SESSION);
                    will(returnValue(personBO));

                    one(personBO).enrichSessionWithPerson();

                    one(daoFactory).getPersonDAO();
                    will(returnValue(personDAO));

                    one(personDAO).listPersons();
                    will(returnValue(Arrays.asList(new PersonPE[]
                        { new PersonPE(), new PersonPE() })));
                }
            });
        final IPersonManager personManager = createPersonManager();
        boolean exceptionThrown = false;
        try
        {
            personManager.registerPersonIfNecessary(null);
        } catch (final AssertionError e)
        {
            exceptionThrown = true;
        }
        assertTrue("Null session is not allowed.", exceptionThrown);
        personManager.registerPersonIfNecessary(ManagerTestTool.EXAMPLE_SESSION);
        context.assertIsSatisfied();
    }

    @Test
    public final void testAssignHomeGroup()
    {
        final String userId = "U1";
        final String groupCode = "G1";
        final GroupIdentifier groupIdentifier =
                new GroupIdentifier(DatabaseInstanceIdentifier.createHome(), groupCode);
        final GroupPE groupPE = new GroupPE();
        groupPE.setCode(groupCode);
        context.checking(new Expectations()
            {
                {
                    one(businessObjectFactory).createPersonBO(EXAMPLE_SESSION);
                    will(returnValue(personBO));

                    one(businessObjectFactory).createGroupBO(EXAMPLE_SESSION);
                    will(returnValue(groupBO));

                    one(groupBO).load(groupIdentifier);
                    one(groupBO).getGroup();
                    will(returnValue(groupPE));

                    one(personBO).load(userId);
                    one(personBO).setHomeGroup(groupPE);
                }
            });
        final IPersonManager personManager = createPersonManager();
        personManager.assignHomeGroup(EXAMPLE_SESSION, userId, groupIdentifier);
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterRoleAssignments()
    {
        final NewRoleAssignment newRoleAssignment = new NewRoleAssignment();
        final GroupIdentifier groupIdentifier = new GroupIdentifier("DB1", "GROUP");
        newRoleAssignment.setGroupIdentifier(groupIdentifier);
        newRoleAssignment.setRole(RoleCode.ADMIN);
        final String userName = EXAMPLE_SESSION.getUserName();
        newRoleAssignment.setUserId(userName);
        context.checking(new Expectations()
            {
                {
                    one(businessObjectFactory).createRoleAssignmentTable(EXAMPLE_SESSION);
                    will(returnValue(roleAssignmentTable));

                    one(roleAssignmentTable).add(newRoleAssignment);

                    one(roleAssignmentTable).save();
                }
            });
        final IPersonManager personManager = createPersonManager();
        personManager.registerRoleAssignments(EXAMPLE_SESSION, new NewRoleAssignment[]
            { newRoleAssignment });
        context.assertIsSatisfied();
    }

}