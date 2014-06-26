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

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class SampleOwnerIdentifierPredicateTest extends AuthorizationTestCase
{
    @Test
    public void testAllowedToModifyDatabase()
    {
        SampleOwnerIdentifierPredicate predicate = new SampleOwnerIdentifierPredicate(false);
        PersonPE person = createPerson();
        List<RoleWithIdentifier> roles = createRoles(true);
        SampleOwnerIdentifier identifier = new SampleOwnerIdentifier();
        prepareProvider(ANOTHER_INSTANCE_CODE, createAnotherDatabaseInstance(), createSpaces());
        predicate.init(provider);

        Status status = predicate.evaluate(person, roles, identifier);

        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }

    @Test
    public void testNotAllowedToModifyDatabase()
    {
        SampleOwnerIdentifierPredicate predicate = new SampleOwnerIdentifierPredicate(false);
        PersonPE person = createPerson();
        List<RoleWithIdentifier> roles = createRoles(false);
        SampleOwnerIdentifier identifier = new SampleOwnerIdentifier();
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createSpaces());
        predicate.init(provider);

        Status status = predicate.evaluate(person, roles, identifier);

        assertEquals(true, status.isError());
        assertEquals("User 'megapixel' does not have enough privileges to modify "
                + "instance level entities.", status.tryGetErrorMessage());
        context.assertIsSatisfied();
    }

    @Test
    public void testAllowedDatabaseInstance()
    {
        SampleOwnerIdentifierPredicate predicate = new SampleOwnerIdentifierPredicate();
        PersonPE person = createPerson();
        List<RoleWithIdentifier> roles = createRoles(false);
        SampleOwnerIdentifier identifier = new SampleOwnerIdentifier();
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createSpaces());
        predicate.init(provider);

        Status status = predicate.evaluate(person, roles, identifier);

        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }

    @Test
    public void testGenericAllowedDatabaseInstance()
    {
        SampleOwnerIdentifierPredicate predicate = new SampleOwnerIdentifierPredicate();
        PersonPE person = createPerson();
        List<RoleWithIdentifier> roles = createRoles(false);
        SampleOwnerIdentifier identifier = new SampleOwnerIdentifier();
        prepareProvider(ANOTHER_INSTANCE_CODE, createAnotherDatabaseInstance(), createSpaces());
        predicate.init(provider);

        Status status = predicate.evaluate(person, roles, identifier);

        assertEquals(Status.OK, status);
        context.assertIsSatisfied();
    }

    @Test
    public void testAllowedSpace()
    {
        SampleOwnerIdentifierPredicate predicate = new SampleOwnerIdentifierPredicate();
        PersonPE person = createPerson();
        List<RoleWithIdentifier> roles = createRoles(false);
        SampleOwnerIdentifier identifier =
                new SampleOwnerIdentifier(new SpaceIdentifier(INSTANCE_IDENTIFIER, SPACE_CODE));
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createSpaces());
        predicate.init(provider);

        Status status = predicate.evaluate(person, roles, identifier);

        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }

    @Test
    public void testNotAllowedSpace()
    {
        SampleOwnerIdentifierPredicate predicate = new SampleOwnerIdentifierPredicate();
        PersonPE person = createPerson();
        List<RoleWithIdentifier> roles = createRoles(false);
        SampleOwnerIdentifier identifier =
                new SampleOwnerIdentifier(new SpaceIdentifier(ANOTHER_INSTANCE_CODE,
                        ANOTHER_SPACE_CODE));
        prepareProvider(ANOTHER_INSTANCE_CODE, createAnotherDatabaseInstance(), createSpaces());
        predicate.init(provider);

        Status status = predicate.evaluate(person, roles, identifier);

        assertEquals(true, status.isError());
        assertEquals("User 'megapixel' does not have enough privileges.", status
                .tryGetErrorMessage());
        context.assertIsSatisfied();
    }
}
