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

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.CommonTestUtils;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.authorization.EntityWithGroupKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Test cases for corresponding {@link AbstractTechIdPredicate} class implementation.
 * 
 * @see GroupIdentifierPredicateTest
 * @author Piotr Buczek
 */
public final class AbstractTechIdPredicateTest extends AuthorizationTestCase
{
    private static TechId TECH_ID = CommonTestUtils.TECH_ID;

    private static EntityWithGroupKind ENTITY_KIND = EntityWithGroupKind.EXPERIMENT;

    private AbstractTechIdPredicate createPredicate()
    {
        return AbstractTechIdPredicate.create(ENTITY_KIND);
    }

    @Test
    public final void testDoEvaluationWithoutDAOFactory()
    {
        final AbstractTechIdPredicate predicate = createPredicate();
        boolean fail = true;
        try
        {
            predicate.doEvaluation(createPerson(), createRoles(false), TECH_ID);
        } catch (final AssertionError e)
        {
            fail = false;
        }
        assertFalse(fail);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = UserFailureException.class)
    public final void testExceptionBecauseGroupDoesNotExist()
    {
        final AbstractTechIdPredicate predicate = createPredicate();
        prepareProvider(Collections.<GroupPE> emptyList(), createGroup(), ENTITY_KIND, TECH_ID);
        predicate.init(provider);
        predicate.doEvaluation(createPerson(), createRoles(false), TECH_ID);
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluation()
    {
        final AbstractTechIdPredicate predicate = createPredicate();
        prepareProvider(createGroups(), createGroup(), ENTITY_KIND, TECH_ID);
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), TECH_ID);
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluationWithHomeGroup()
    {
        final AbstractTechIdPredicate predicate = createPredicate();
        final PersonPE person = createPerson();
        person.setHomeGroup(createGroup());
        final DatabaseInstancePE homeDatabaseInstance = createDatabaseInstance();
        final GroupPE homeGroup = createGroup(null, homeDatabaseInstance);
        prepareProvider(createGroups(), homeGroup, ENTITY_KIND, TECH_ID);
        predicate.init(provider);
        final Status evaluation = predicate.doEvaluation(person, createRoles(false), TECH_ID);
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testFailedEvaluation()
    {
        final AbstractTechIdPredicate predicate = createPredicate();
        prepareProvider(createGroups(), createAnotherGroup(), ENTITY_KIND, TECH_ID);
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), TECH_ID);
        assertEquals(StatusFlag.ERROR, evaluation.getFlag());
        assertEquals(
                "User 'megapixel' does not have enough privileges to access data in the group 'DB2:/G2'.",
                evaluation.tryGetErrorMessage());
        context.assertIsSatisfied();
    }

    @Test
    public final void testAccessAnotherGroup()
    {
        final AbstractTechIdPredicate predicate = createPredicate();
        final DatabaseInstancePE homeDatabaseInstance = createDatabaseInstance();
        final List<GroupPE> groups = createGroups();
        final GroupPE anotherGroup = createGroup(ANOTHER_GROUP_CODE, homeDatabaseInstance);
        groups.add(anotherGroup);
        prepareProvider(groups, anotherGroup, ENTITY_KIND, TECH_ID);
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(false), TECH_ID);
        assertEquals(StatusFlag.ERROR, evaluation.getFlag());
        assertEquals(
                "User 'megapixel' does not have enough privileges to access data in the group 'DB1:/G2'.",
                evaluation.tryGetErrorMessage());
        context.assertIsSatisfied();
    }

}
