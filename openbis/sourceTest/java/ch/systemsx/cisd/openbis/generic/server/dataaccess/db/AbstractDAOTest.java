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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import ch.systemsx.cisd.openbis.generic.shared.util.UuidUtil;

/**
 * Abstract test case for database related unit testing.
 * 
 * @author Christian Ribeaud
 */
@ContextConfiguration(locations = "classpath:applicationContext.xml")
// In 'commonContext.xml', our transaction manager is called 'transaction-manager' (by default
// Spring looks for 'transactionManager').
@TransactionConfiguration(transactionManager = "transaction-manager")
@Friend(toClasses =
    { AbstractDAO.class })
public abstract class AbstractDAOTest extends AbstractTransactionalTestNGSpringContextTests
{
    static
    {
        LogInitializer.init();
        System.setProperty("database.create-from-scratch", "true");
        System.setProperty("database.kind", "test");
        System.setProperty("script-folder", "sourceTest");
        System.setProperty("mass-upload-folder", "sourceTest/sql/postgresql");
    }

    protected static final Long ANOTHER_DATABASE_INSTANCE_ID = new Long(3);

    protected IDAOFactory daoFactory;

    protected SessionFactory sessionFactory;

    private Long origDatabaseInstanceId;

    private Object currentDAO;

    @BeforeMethod(alwaysRun = true)
    public final void setUp()
    {
        createAnotherDatabaseInstanceId();
    }

    @AfterMethod(alwaysRun = true)
    public final void tearDown()
    {
        if (currentDAO != null)
        {
            resetDatabaseInstanceId(currentDAO);
        }
    }

    /**
     * Sets <code>daoFactory</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setDaoFactory(final IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    /**
     * Sets <code>hibernate session factory</code>.
     * <p>
     * Will be automatically dependency injected by type.
     * </p>
     */
    @Autowired
    public final void setHibernateSessionFactory(final SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Changes the database instance id of given {@link AbstractDAO} to a new value.
     */
    final void changeDatabaseInstanceId(final Object dao)
    {
        assertNull(origDatabaseInstanceId);
        assertTrue(dao instanceof AbstractDAO);
        final DatabaseInstancePE databaseInstance = getDatabaseInstanceReference(dao);
        origDatabaseInstanceId = databaseInstance.getId();
        databaseInstance.setId(ANOTHER_DATABASE_INSTANCE_ID);
        currentDAO = dao;
    }

    private DatabaseInstancePE getDatabaseInstanceReference(final Object dao)
    {
        final AbstractDAO abstractDAO = (AbstractDAO) dao;
        final DatabaseInstancePE databaseInstance = abstractDAO.getDatabaseInstance();
        assertNotNull(databaseInstance);
        return databaseInstance;
    }

    /**
     * Resets the database instance id of given {@link AbstractDAO} to its original value.
     */
    final void resetDatabaseInstanceId(final Object dao)
    {
        assertTrue(dao instanceof AbstractDAO);
        assertNotNull(origDatabaseInstanceId);
        final DatabaseInstancePE databaseInstance = getDatabaseInstanceReference(dao);
        databaseInstance.setId(origDatabaseInstanceId);
        origDatabaseInstanceId = null;
        currentDAO = null;
    }

    /**
     * Creates <code>ANOTHER_DATABASE_INSTANCE_ID</code> in the database if needed.
     */
    private final Long createAnotherDatabaseInstanceId()
    {
        try
        {
            return simpleJdbcTemplate.queryForLong(String.format("select id from %s where id = ?",
                    TableNames.DATABASE_INSTANCES_TABLE), ANOTHER_DATABASE_INSTANCE_ID);
        } catch (final DataAccessException ex)
        {
            simpleJdbcTemplate.update(String.format(
                    "insert into %s (id, code, uuid) values (?, ?, ?)",
                    TableNames.DATABASE_INSTANCES_TABLE), ANOTHER_DATABASE_INSTANCE_ID,
                    "MY_INSTANCE", UuidUtil.generateUUID());
            return ANOTHER_DATABASE_INSTANCE_ID;
        }
    }

    protected PersonPE getSystemPerson()
    {
        PersonPE person = daoFactory.getPersonDAO().tryFindPersonByUserId("system");
        assertNotNull("Person 'system' does not exists.", person);
        return person;
    }

    protected DatabaseInstancePE createDatabaseInstance(String databaseInstanceCode)
    {
        DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode(databaseInstanceCode);
        daoFactory.getDatabaseInstanceDAO().createDatabaseInstance(databaseInstance);
        return databaseInstance;
    }

    protected GroupPE createGroup(String groupCode)
    {
        DatabaseInstancePE databaseInstance = daoFactory.getHomeDatabaseInstance();
        return createGroup(groupCode, databaseInstance);
    }

    protected GroupPE createGroup(String groupCode, DatabaseInstancePE databaseInstance)
    {
        GroupPE group = new GroupPE();
        group.setCode(groupCode);
        group.setDatabaseInstance(databaseInstance);
        group.setRegistrator(getSystemPerson());
        daoFactory.getGroupDAO().createGroup(group);
        return group;
    }
}
