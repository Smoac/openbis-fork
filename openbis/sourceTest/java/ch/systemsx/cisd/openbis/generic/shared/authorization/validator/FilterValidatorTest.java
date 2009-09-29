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

package ch.systemsx.cisd.openbis.generic.shared.authorization.validator;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FilterPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.FilterTranslator;

/**
 * Test cases for corresponding {@link FilterValidator} class.
 * 
 * @author Piotr Buczek
 */
@Friend(toClasses = FilterValidator.class)
public final class FilterValidatorTest extends AuthorizationTestCase
{
    @Test
    public final void testWithPublicFilter()
    {
        final DatabaseInstancePE instance = createDatabaseInstance();
        final PersonPE person = createPerson("A", instance);
        final PersonPE registrator = createPerson("B", instance);
        final boolean isPublic = true;
        final FilterPE filter = createFilter(instance, registrator, isPublic);
        final FilterValidator validator = new FilterValidator();
        assertEquals(true, validator.isValid(person, FilterTranslator.translate(filter)));
    }

    @Test
    public final void testWithTheRightRegistrator()
    {
        // registrators are equal when they have the same userId AND db instance
        final DatabaseInstancePE instance = createDatabaseInstance();
        final PersonPE person = createPerson("A", instance);
        final PersonPE registrator = person;
        final boolean isPublic = false;
        final FilterPE filter = createFilter(instance, registrator, isPublic);
        final FilterValidator validator = new FilterValidator();
        assertEquals(true, validator.isValid(person, FilterTranslator.translate(filter)));
    }

    @Test
    public final void testWithTheWrongRegistrator()
    {
        // registrators that have the same userId BUT different db instance are not the same
        final DatabaseInstancePE instance = createDatabaseInstance();
        final DatabaseInstancePE anotherInstance = createAnotherDatabaseInstance();
        final PersonPE person = createPerson("A", anotherInstance);
        final PersonPE registrator = createPerson("A", instance);
        final boolean isPublic = false;
        final FilterPE filter = createFilter(instance, registrator, isPublic);
        final FilterValidator validator = new FilterValidator();
        assertEquals(false, validator.isValid(person, FilterTranslator.translate(filter)));
    }

    @Test
    public final void testWithTheRightInstanceAdmin()
    {
        final DatabaseInstancePE instance = createDatabaseInstance();
        final PersonPE person = createPerson("A", instance);
        assignRoles(person);
        final PersonPE registrator = createPerson("B", instance);
        final boolean isPublic = false;
        final FilterPE filter = createFilter(instance, registrator, isPublic);
        final FilterValidator validator = new FilterValidator();
        assertEquals(true, validator.isValid(person, FilterTranslator.translate(filter)));
    }

    @Test
    public final void testWithTheWrongInstanceAdmin()
    {
        final DatabaseInstancePE instance = createDatabaseInstance();
        final DatabaseInstancePE anotherInstance = createAnotherDatabaseInstance();
        final PersonPE person = createPerson("A", instance);
        assignRoles(person);
        final PersonPE registrator = createPerson("B", anotherInstance);
        final boolean isPublic = false;
        final FilterPE filter = createFilter(anotherInstance, registrator, isPublic);
        final FilterValidator validator = new FilterValidator();
        assertEquals(false, validator.isValid(person, FilterTranslator.translate(filter)));
    }

}
