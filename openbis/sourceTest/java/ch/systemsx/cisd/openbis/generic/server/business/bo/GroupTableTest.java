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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;

/**
 * Test cases for corresponding {@link GroupTable} class.
 * 
 * @author Christian Ribeaud
 */
public final class GroupTableTest
{
    private BufferedAppender logRecorder;

    private Mockery context;

    private IAuthorizationDAOFactory daoFactory;

    private IGroupDAO groupDAO;

    private IDatabaseInstanceDAO databaseInstanceDAO;

    private final IGroupTable createGroupTable()
    {
        return new GroupTable(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
    }

    @BeforeMethod
    public final void beforeMethod()
    {
        context = new Mockery();
        daoFactory = context.mock(IAuthorizationDAOFactory.class);
        groupDAO = context.mock(IGroupDAO.class);
        databaseInstanceDAO = context.mock(IDatabaseInstanceDAO.class);
        logRecorder = new BufferedAppender("%m", Level.DEBUG);
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
    public final void testLoad()
    {
        final IGroupTable groupTable = createGroupTable();
        final DatabaseInstancePE databaseInstancePE = new DatabaseInstancePE();
        final long databaseInstanceId = 24L;
        databaseInstancePE.setId(databaseInstanceId);
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getGroupDAO();
                    will(returnValue(groupDAO));

                    one(daoFactory).getHomeDatabaseInstance();
                    will(returnValue(databaseInstancePE));

                    one(groupDAO).listGroups(databaseInstanceId);
                }
            });
        groupTable.load(DatabaseInstanceIdentifier.createHome());
        assertEquals(0, groupTable.getGroups().size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testLoadWithDatabaseInstance()
    {
        final IGroupTable groupTable = createGroupTable();
        final String dbCode = "DB2";
        final DatabaseInstanceIdentifier identifier = new DatabaseInstanceIdentifier(dbCode);
        final long databaseInstanceId = 24L;
        final DatabaseInstancePE databaseInstancePE = new DatabaseInstancePE();
        databaseInstancePE.setId(databaseInstanceId);
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getGroupDAO();
                    will(returnValue(groupDAO));

                    one(daoFactory).getDatabaseInstancesDAO();
                    will(returnValue(databaseInstanceDAO));

                    one(databaseInstanceDAO).tryFindDatabaseInstanceByCode(dbCode);
                    will(returnValue(databaseInstancePE));

                    one(groupDAO).listGroups(databaseInstanceId);
                }
            });
        groupTable.load(identifier);
        assertEquals(0, groupTable.getGroups().size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testRainyDay()
    {
        final IGroupTable groupTable = createGroupTable();
        boolean fail = true;
        try
        {
            groupTable.getGroups();
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }
}
