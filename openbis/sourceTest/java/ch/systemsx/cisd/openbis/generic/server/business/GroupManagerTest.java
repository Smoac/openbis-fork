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

package ch.systemsx.cisd.openbis.generic.server.business;

import static ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool.EXAMPLE_SESSION;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupTable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * Test cases for corresponding {@link GroupManager} class.
 * 
 * @author Christian Ribeaud
 */
public final class GroupManagerTest
{
    private Mockery context;

    private IAuthorizationDAOFactory daoFactory;

    private IGenericBusinessObjectFactory businessObjectFactory;

    private IGroupDAO groupDAO;

    private IDatabaseInstanceDAO databaseInstanceDAO;

    private BufferedAppender logRecorder;

    private IGroupBO groupBO;

    private IGroupTable groupTable;

    private final IGroupManager createGroupManager()
    {
        ManagerTestTool.prepareGroupDAO(context, daoFactory, groupDAO);
        return new GroupManager(daoFactory, businessObjectFactory);
    }

    @BeforeMethod
    public final void beforeMethod()
    {
        context = new Mockery();
        daoFactory = context.mock(IAuthorizationDAOFactory.class);
        groupBO = context.mock(IGroupBO.class);
        groupTable = context.mock(IGroupTable.class);
        groupDAO = context.mock(IGroupDAO.class);
        databaseInstanceDAO = context.mock(IDatabaseInstanceDAO.class);
        businessObjectFactory = context.mock(IGenericBusinessObjectFactory.class);
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public final void afterMethod()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testRegisterGroup()
    {
        final GroupIdentifier groupIdentifier =
                new GroupIdentifier(DatabaseInstanceIdentifier.HOME, "MY_CODE");
        context.checking(new Expectations()
            {
                {
                    one(businessObjectFactory).createGroupBO(EXAMPLE_SESSION);
                    will(returnValue(groupBO));

                    one(groupBO).define(groupIdentifier, null, null);
                    one(groupBO).save();
                }
            });
        final IGroupManager groupManager = createGroupManager();
        boolean fail = true;
        try
        {
            groupManager.registerGroup(null, null, null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        try
        {
            groupManager.registerGroup(ManagerTestTool.EXAMPLE_SESSION, null, null, null);
            fail("Group code can not be null");
        } catch (final UserFailureException e)
        {
            // Nothing to do here.
        }
        groupManager.registerGroup(ManagerTestTool.EXAMPLE_SESSION, groupIdentifier, null, null);
        context.assertIsSatisfied();
    }

    @Test
    public final void testListGroupsWithNullDatabaseInstance()
    {
        final IGroupManager groupManager = createGroupManager();
        final DatabaseInstanceIdentifier databaseInstanceIdentifier =
                DatabaseInstanceIdentifier.createHome();
        boolean fail = true;
        try
        {
            groupManager.listGroups(null, null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);

        final List<GroupPE> groupsLoaded = new ArrayList<GroupPE>();
        groupsLoaded.add(ManagerTestTool.EXAMPLE_GROUP);
        context.checking(new Expectations()
            {
                {
                    one(businessObjectFactory).createGroupTable(EXAMPLE_SESSION);
                    will(returnValue(groupTable));

                    one(groupTable).load(databaseInstanceIdentifier);
                    one(groupTable).getGroups();
                    will(returnValue(groupsLoaded));
                }
            });
        final List<GroupPE> groups =
                groupManager
                        .listGroups(ManagerTestTool.EXAMPLE_SESSION, databaseInstanceIdentifier);
        assertEquals(1, groups.size());
        assertEquals(groups.get(0), ManagerTestTool.EXAMPLE_GROUP);
        context.assertIsSatisfied();
    }

    @Test
    public final void testListGroups()
    {
        final IGroupManager groupManager = createGroupManager();
        final DatabaseInstanceIdentifier identifier = new DatabaseInstanceIdentifier("DB1");
        context.checking(new Expectations()
            {
                {
                    one(businessObjectFactory).createGroupTable(EXAMPLE_SESSION);
                    will(returnValue(groupTable));

                    one(groupTable).load(identifier);

                    one(groupTable).getGroups();
                }
            });
        groupManager.listGroups(ManagerTestTool.EXAMPLE_SESSION, identifier);
        context.assertIsSatisfied();
    }

    @Test
    public final void testListDatabaseInstances()
    {
        final IGroupManager groupManager = createGroupManager();
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getDatabaseInstancesDAO();
                    will(returnValue(databaseInstanceDAO));

                    one(databaseInstanceDAO).listDatabaseInstances();
                }
            });
        boolean fail = true;
        try
        {
            groupManager.listDatabaseInstances(null);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        groupManager.listDatabaseInstances(ManagerTestTool.EXAMPLE_SESSION);
        context.assertIsSatisfied();
    }

}
