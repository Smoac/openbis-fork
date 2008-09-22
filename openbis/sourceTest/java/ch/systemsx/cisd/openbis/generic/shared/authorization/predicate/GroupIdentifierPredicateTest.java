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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.ArrayList;
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
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.GroupIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * Test cases for corresponding {@link GroupIdentifierPredicate} class.
 * 
 * @author Christian Ribeaud
 */
public final class GroupIdentifierPredicateTest
{
    static final String GROUP_CODE = "G1";

    static final String ANOTHER_GROUP_CODE = "G2";

    static final Long GROUP_ID = 87L;

    static final Long ANOTHER_GROUP_ID = 88L;

    private Mockery context;

    private IAuthorizationDataProvider provider;

    static final List<GroupPE> createGroups()
    {
        final List<GroupPE> groups = new ArrayList<GroupPE>();
        groups.add(createGroup());
        groups.add(createAnotherGroup());
        return groups;
    }

    static final GroupPE createGroup(final String groupCode, final Long id,
            final DatabaseInstancePE databaseInstancePE)
    {
        final GroupPE group = new GroupPE();
        group.setCode(groupCode);
        group.setId(id);
        group.setDatabaseInstance(databaseInstancePE);
        return group;
    }

    static final GroupPE createAnotherGroup()
    {
        return createGroup(ANOTHER_GROUP_CODE, ANOTHER_GROUP_ID,
                DatabaseInstanceIdentifierPredicateTest.createAnotherDatabaseInstance());
    }

    static final GroupPE createGroup()
    {
        return createGroup(GROUP_CODE, GROUP_ID, DatabaseInstanceIdentifierPredicateTest
                .createDatabaseInstance());
    }

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        provider = context.mock(IAuthorizationDataProvider.class);
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
        final GroupIdentifierPredicate predicate = new GroupIdentifierPredicate();
        boolean fail = true;
        try
        {
            predicate.doEvaluation(DatabaseInstanceIdentifierPredicateTest.createPerson(),
                    DatabaseInstanceIdentifierPredicateTest.createAllowedRoles(false),
                    GroupIdentifier.createHome());
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = UserFailureException.class)
    public final void testExceptionBecauseInstanceDoesNotExist()
    {
        final GroupIdentifierPredicate predicate = new GroupIdentifierPredicate();
        context.checking(new Expectations()
            {
                {
                    one(provider).tryFindDatabaseInstanceByCode(
                            DatabaseInstanceIdentifierPredicateTest.INSTANCE_CODE);
                    will(returnValue(null));

                    one(provider).listGroups();
                    will(returnValue(Collections.EMPTY_LIST));
                }
            });
        predicate.init(provider);
        predicate.doEvaluation(DatabaseInstanceIdentifierPredicateTest.createPerson(),
                DatabaseInstanceIdentifierPredicateTest.createAllowedRoles(false),
                new GroupIdentifier(DatabaseInstanceIdentifierPredicateTest.INSTANCE_CODE,
                        GROUP_CODE));
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = UserFailureException.class)
    public final void testExceptionBecauseGroupDoesNotExist()
    {
        final GroupIdentifierPredicate predicate = new GroupIdentifierPredicate();
        context.checking(new Expectations()
            {
                {
                    one(provider).tryFindDatabaseInstanceByCode(
                            DatabaseInstanceIdentifierPredicateTest.INSTANCE_CODE);
                    will(returnValue(DatabaseInstanceIdentifierPredicateTest
                            .createDatabaseInstance()));

                    one(provider).listGroups();
                    will(returnValue(Collections.EMPTY_LIST));
                }
            });
        predicate.init(provider);
        predicate.doEvaluation(DatabaseInstanceIdentifierPredicateTest.createPerson(),
                DatabaseInstanceIdentifierPredicateTest.createAllowedRoles(false),
                new GroupIdentifier(DatabaseInstanceIdentifierPredicateTest.INSTANCE_CODE,
                        GROUP_CODE));
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluation()
    {
        final GroupIdentifierPredicate predicate = new GroupIdentifierPredicate();
        context.checking(new Expectations()
            {
                {
                    one(provider).tryFindDatabaseInstanceByCode(
                            DatabaseInstanceIdentifierPredicateTest.INSTANCE_CODE);
                    will(returnValue(DatabaseInstanceIdentifierPredicateTest
                            .createDatabaseInstance()));

                    one(provider).listGroups();
                    will(returnValue(createGroups()));
                }
            });
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(DatabaseInstanceIdentifierPredicateTest.createPerson(),
                        DatabaseInstanceIdentifierPredicateTest.createAllowedRoles(false),
                        new GroupIdentifier(DatabaseInstanceIdentifierPredicateTest.INSTANCE_CODE,
                                GROUP_CODE));
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluationWithHomeGroup()
    {
        final GroupIdentifierPredicate predicate = new GroupIdentifierPredicate();
        context.checking(new Expectations()
            {
                {
                    one(provider).tryFindDatabaseInstanceByCode(
                            DatabaseInstanceIdentifierPredicateTest.INSTANCE_CODE);
                    will(returnValue(DatabaseInstanceIdentifierPredicateTest
                            .createDatabaseInstance()));

                    one(provider).listGroups();
                    will(returnValue(createGroups()));
                }
            });
        predicate.init(provider);
        final PersonPE person = DatabaseInstanceIdentifierPredicateTest.createPerson();
        final GroupPE homeGroup = createGroup();
        person.setHomeGroup(homeGroup);
        final GroupIdentifier groupIdentifier =
                new GroupIdentifier(DatabaseInstanceIdentifierPredicateTest.INSTANCE_CODE, null);
        final Status evaluation =
                predicate.doEvaluation(person, DatabaseInstanceIdentifierPredicateTest
                        .createAllowedRoles(false), groupIdentifier);
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testFailedEvaluation()
    {
        final GroupIdentifierPredicate predicate = new GroupIdentifierPredicate();
        context.checking(new Expectations()
            {
                {
                    one(provider).tryFindDatabaseInstanceByCode(
                            DatabaseInstanceIdentifierPredicateTest.ANOTHER_INSTANCE_CODE);
                    will(returnValue(DatabaseInstanceIdentifierPredicateTest
                            .createAnotherDatabaseInstance()));

                    one(provider).listGroups();
                    will(returnValue(createGroups()));
                }
            });
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(DatabaseInstanceIdentifierPredicateTest.createPerson(),
                        DatabaseInstanceIdentifierPredicateTest.createAllowedRoles(false),
                        new GroupIdentifier(
                                DatabaseInstanceIdentifierPredicateTest.ANOTHER_INSTANCE_CODE,
                                ANOTHER_GROUP_CODE));
        assertEquals(StatusFlag.ERROR, evaluation.getFlag());
        assertEquals(
                "User 'megapixel' does not have enough privileges to access data in the group 'DB2:/G2'.",
                evaluation.tryGetErrorMessage());
        context.assertIsSatisfied();
    }

    @Test
    public final void testAccessAnotherGroup()
    {
        final DatabaseInstancePE homeDatabaseInstance =
                DatabaseInstanceIdentifierPredicateTest.createDatabaseInstance();
        final GroupIdentifierPredicate predicate = new GroupIdentifierPredicate();
        final List<GroupPE> groups = createGroups();
        groups.add(createGroup(ANOTHER_GROUP_CODE, 34L, homeDatabaseInstance));
        context.checking(new Expectations()
            {
                {
                    one(provider).tryFindDatabaseInstanceByCode(
                            DatabaseInstanceIdentifierPredicateTest.INSTANCE_CODE);
                    will(returnValue(DatabaseInstanceIdentifierPredicateTest
                            .createDatabaseInstance()));

                    one(provider).listGroups();
                    will(returnValue(groups));
                }
            });
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(DatabaseInstanceIdentifierPredicateTest.createPerson(),
                        DatabaseInstanceIdentifierPredicateTest.createAllowedRoles(false),
                        new GroupIdentifier(DatabaseInstanceIdentifierPredicateTest.INSTANCE_CODE,
                                ANOTHER_GROUP_CODE));
        assertEquals(StatusFlag.ERROR, evaluation.getFlag());
        assertEquals(
                "User 'megapixel' does not have enough privileges to access data in the group 'DB1:/G2'.",
                evaluation.tryGetErrorMessage());
        context.assertIsSatisfied();
    }
}
