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

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Test cases for corresponding {@link DatabaseInstanceIdentifierPredicate} class.
 * 
 * @author Christian Ribeaud
 */
public final class DatabaseInstanceIdentifierPredicateTest extends AuthorizationTestCase
{

    @Test
    public final void testDoEvaluationWithoutDAOFactory()
    {
        final DatabaseInstanceIdentifierPredicate predicate = createInstancePredicate(true);
        boolean fail = true;
        try
        {
            predicate.doEvaluation(createPerson(), createRoles(false),
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
        final DatabaseInstanceIdentifierPredicate predicate = createInstancePredicate(true);
        predicate.init(provider);
        final Status evaluation =
                predicate.doEvaluation(createPerson(), createRoles(true),
                        new DatabaseInstanceIdentifier(ANOTHER_INSTANCE_CODE));
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testEveryoneCanReadDatabaseEntities()
    {
        final DatabaseInstanceIdentifierPredicate predicate = createInstancePredicate(true);
        predicate.init(provider);
        final PersonPE person = createPerson();
        final Status evaluation =
                predicate.doEvaluation(person, createRoles(false),
                        new DatabaseInstanceIdentifier(ANOTHER_INSTANCE_CODE));
        assertEquals(StatusFlag.OK, evaluation.getFlag());
        context.assertIsSatisfied();
    }

    @Test
    public final void testSuccessfulEvaluationWithHomeDatabaseInstance()
    {
        final DatabaseInstanceIdentifierPredicate predicate = createInstancePredicate(true);
        predicate.init(provider);
        final PersonPE person = createPerson();
        final Status evaluation =
                predicate.doEvaluation(person, createRoles(true), DatabaseInstanceIdentifier
                        .createHome());
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    @Test
    public final void testWithGroupIdentifier()
    {
        final DatabaseInstanceIdentifierPredicate predicate = createInstancePredicate(true);
        predicate.init(provider);
        final PersonPE person = createPerson();
        final Status evaluation =
                predicate.doEvaluation(person, createRoles(false), new SpaceIdentifier(
                        SPACE_CODE));
        assertEquals(Status.OK, evaluation);
        context.assertIsSatisfied();
    }

    private DatabaseInstanceIdentifierPredicate createInstancePredicate(boolean isReadOnly)
    {
        return new DatabaseInstanceIdentifierPredicate(isReadOnly);
    }
}
