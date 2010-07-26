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

import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

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
        SampleOwnerIdentifier identifier = new SampleOwnerIdentifier(ANOTHER_INSTANCE_IDENTIFIER);
        prepareProvider(ANOTHER_INSTANCE_CODE, createAnotherDatabaseInstance(), createGroups());
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
        SampleOwnerIdentifier identifier = new SampleOwnerIdentifier(INSTANCE_IDENTIFIER);
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createGroups());
        predicate.init(provider);

        Status status = predicate.evaluate(person, roles, identifier);

        assertEquals(true, status.isError());
        assertEquals("User 'megapixel' does not have enough privileges to modify "
                + "database instance 'DB1'.", status.tryGetErrorMessage());
        context.assertIsSatisfied();
    }

    @Test
    public void testAllowedDatabaseInstance()
    {
        SampleOwnerIdentifierPredicate predicate = new SampleOwnerIdentifierPredicate();
        PersonPE person = createPerson();
        List<RoleWithIdentifier> roles = createRoles(false);
        SampleOwnerIdentifier identifier = new SampleOwnerIdentifier(INSTANCE_IDENTIFIER);
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createGroups());
        predicate.init(provider);

        Status status = predicate.evaluate(person, roles, identifier);

        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }

    @Test
    public void testNotAllowedDatabaseInstance()
    {
        SampleOwnerIdentifierPredicate predicate = new SampleOwnerIdentifierPredicate();
        PersonPE person = createPerson();
        List<RoleWithIdentifier> roles = createRoles(false);
        SampleOwnerIdentifier identifier = new SampleOwnerIdentifier(ANOTHER_INSTANCE_IDENTIFIER);
        prepareProvider(ANOTHER_INSTANCE_CODE, createAnotherDatabaseInstance(), createGroups());
        predicate.init(provider);

        Status status = predicate.evaluate(person, roles, identifier);

        assertEquals(true, status.isError());
        assertEquals("User 'megapixel' does not have enough privileges to read "
                + "from database instance 'DB2'.", status.tryGetErrorMessage());
        context.assertIsSatisfied();
    }

    @Test
    public void testAllowedGroup()
    {
        SampleOwnerIdentifierPredicate predicate = new SampleOwnerIdentifierPredicate();
        PersonPE person = createPerson();
        List<RoleWithIdentifier> roles = createRoles(false);
        SampleOwnerIdentifier identifier =
                new SampleOwnerIdentifier(new GroupIdentifier(INSTANCE_IDENTIFIER, SPACE_CODE));
        prepareProvider(INSTANCE_CODE, createDatabaseInstance(), createGroups());
        predicate.init(provider);

        Status status = predicate.evaluate(person, roles, identifier);

        assertEquals(false, status.isError());
        context.assertIsSatisfied();
    }

    @Test
    public void testNotAllowedGroup()
    {
        SampleOwnerIdentifierPredicate predicate = new SampleOwnerIdentifierPredicate();
        PersonPE person = createPerson();
        List<RoleWithIdentifier> roles = createRoles(false);
        SampleOwnerIdentifier identifier =
                new SampleOwnerIdentifier(new GroupIdentifier(ANOTHER_INSTANCE_CODE,
                        ANOTHER_GROUP_CODE));
        prepareProvider(ANOTHER_INSTANCE_CODE, createAnotherDatabaseInstance(), createGroups());
        predicate.init(provider);

        Status status = predicate.evaluate(person, roles, identifier);

        assertEquals(true, status.isError());
        assertEquals("User 'megapixel' does not have enough privileges.", status
                .tryGetErrorMessage());
        context.assertIsSatisfied();
    }
}
