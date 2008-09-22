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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifierTest;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * Test cases for corresponding {@link DatabaseInstanceIdentifierPredicate} class.
 * 
 * @author Christian Ribeaud
 */
public final class DatabaseInstanceIdentifierPredicateTest
{
    static final long INSTANCE_ID = 11L;

    static final long ANOTHER_INSTANCE_ID = 12L;

    static final String INSTANCE_CODE = "DB1";

    static final String ANOTHER_INSTANCE_CODE = "DB2";

    private Mockery context;

    private IAuthorizationDAOFactory daoFactory;

    private IDatabaseInstanceDAO databaseInstanceDAO;

    final static DatabaseInstancePE createDatabaseInstance()
    {
        return createDatabaseInstance(INSTANCE_CODE, INSTANCE_ID);
    }

    final static DatabaseInstancePE createAnotherDatabaseInstance()
    {
        return createDatabaseInstance(ANOTHER_INSTANCE_CODE, ANOTHER_INSTANCE_ID);
    }

    final static DatabaseInstancePE createDatabaseInstance(String code, Long id)
    {
        final DatabaseInstancePE databaseInstance = new DatabaseInstancePE();
        databaseInstance.setCode(code);
        databaseInstance.setUuid("global_" + code);
        databaseInstance.setId(id);
        return databaseInstance;
    }

    final static PersonPE createPerson()
    {
        final PersonPE personPE = new PersonPE();
        personPE.setUserId("megapixel");
        personPE.setDatabaseInstance(createDatabaseInstance());
        return personPE;
    }

    final static List<RoleWithIdentifier> createAllowedRoles(final boolean withInstanceRole)
    {
        final List<RoleWithIdentifier> list = new ArrayList<RoleWithIdentifier>();
        final RoleWithIdentifier groupRole =
                RoleWithIdentifierTest.createGroupRole(RoleCode.USER, new GroupIdentifier(
                        INSTANCE_CODE, GroupIdentifierPredicateTest.GROUP_CODE));
        list.add(groupRole);
        if (withInstanceRole)
        {
            final RoleWithIdentifier databaseInstanceRole =
                    RoleWithIdentifierTest.createInstanceRole(RoleCode.USER,
                            new DatabaseInstanceIdentifier(ANOTHER_INSTANCE_CODE));
            list.add(databaseInstanceRole);
        }
        return list;
    }

    private final void preparePredicateInit(final DatabaseInstancePE databaseInstance,
            final boolean accessHomeGroup)
    {
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getDatabaseInstancesDAO();
                    will(returnValue(databaseInstanceDAO));

                    one(databaseInstanceDAO).listDatabaseInstances();
                    if (databaseInstance == null)
                    {
                        will(returnValue(Collections.EMPTY_LIST));
                    } else
                    {
                        will(returnValue(Arrays.asList(databaseInstance)));
                    }
                    if (accessHomeGroup)
                    {
                        one(daoFactory).getHomeDatabaseInstance();
                        will(returnValue(createAnotherDatabaseInstance()));
                    }
                }
            });
    }

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        daoFactory = context.mock(IAuthorizationDAOFactory.class);
        databaseInstanceDAO = context.mock(IDatabaseInstanceDAO.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testDoEvaluationWithoutDAOFactory()
    {
        final DatabaseInstanceIdentifierPredicate predicate = createInstancePredicate();
        boolean fail = true;
        try
        {
            predicate.doEvaluation(createPerson(), createAllowedRoles(false),
                    DatabaseInstanceIdentifier.createHome());
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluation()
    {
        final DatabaseInstanceIdentifierPredicate predicate = createInstancePredicate();
        final DatabaseInstancePE databaseInstance = createAnotherDatabaseInstance();
        preparePredicateInit(databaseInstance, false);
        predicate.init(null);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createAllowedRoles(true),
                        new DatabaseInstanceIdentifier(ANOTHER_INSTANCE_CODE));
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testFailedEvaluation()
    {
        final DatabaseInstanceIdentifierPredicate predicate = createInstancePredicate();
        final DatabaseInstancePE databaseInstance = createAnotherDatabaseInstance();
        preparePredicateInit(databaseInstance, false);
        predicate.init(null);
        final PersonPE person = createPerson();
        final Status evaluation =
                predicate.doEvaluation(person, createAllowedRoles(false),
                        new DatabaseInstanceIdentifier(ANOTHER_INSTANCE_CODE));
        assertEquals(StatusFlag.ERROR, evaluation.getFlag());
        assertEquals(
                "User 'megapixel' does not have enough privileges to read from database instance 'DB2'.",
                evaluation.tryGetErrorMessage());
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluationWithHomeDatabaseInstance()
    {
        final DatabaseInstanceIdentifierPredicate predicate = createInstancePredicate();
        final DatabaseInstancePE databaseInstance = createAnotherDatabaseInstance();
        preparePredicateInit(databaseInstance, true);
        predicate.init(null);
        final PersonPE person = createPerson();
        final Status evaluation =
                predicate.doEvaluation(person, createAllowedRoles(true), DatabaseInstanceIdentifier
                        .createHome());
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = UserFailureException.class)
    public final void testExceptionBecauseInstanceDoesNotExist()
    {
        final DatabaseInstanceIdentifierPredicate predicate = createInstancePredicate();
        preparePredicateInit(null, false);
        predicate.init(null);
        predicate.doEvaluation(DatabaseInstanceIdentifierPredicateTest.createPerson(),
                DatabaseInstanceIdentifierPredicateTest.createAllowedRoles(false),
                new DatabaseInstanceIdentifier(
                        DatabaseInstanceIdentifierPredicateTest.INSTANCE_CODE));
        context.assertIsSatisfied();
    }

    @Test
    public final void testWithGroupIdentifier()
    {
        final DatabaseInstanceIdentifierPredicate predicate = createInstancePredicate();
        final DatabaseInstancePE databaseInstance = createDatabaseInstance();
        preparePredicateInit(databaseInstance, false);
        predicate.init(null);
        final PersonPE person = createPerson();
        final Status evaluation =
                predicate.doEvaluation(person, createAllowedRoles(false), new GroupIdentifier(
                        new DatabaseInstanceIdentifier(databaseInstance.getCode()),
                        GroupIdentifierPredicateTest.GROUP_CODE));
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    private DatabaseInstanceIdentifierPredicate createInstancePredicate()
    {
        return new DatabaseInstanceIdentifierPredicate(true);
    }
}
