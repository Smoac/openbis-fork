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

import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;

/**
 * Test cases for {@link SampleValidator}.
 * 
 * @author Izabela Adamczyk
 */
public class SampleValidatorTest extends AuthorizationTestCase
{
    private static final String BASE_URL = "baseUrl";

    @Test
    public void testIsValidWithTheRightGroup()
    {
        SampleValidator validator = new SampleValidator();
        PersonPE person = createPersonWithRoleAssignments();
        assertEquals(true, validator.isValid(person, SampleTranslator.translate(
                createSample(createAnotherGroup()), BASE_URL)));
    }

    @Test
    public void testIsValidSharedWithTheRightInstance()
    {
        SampleValidator validator = new SampleValidator();
        PersonPE person = createPersonWithRoleAssignments();
        assertEquals(true, validator.isValid(person, SampleTranslator.translate(
                createSample(createDatabaseInstance()), BASE_URL)));
    }

    @Test
    public void testIsValidSharedWithTheWrongInstance()
    {
        SampleValidator validator = new SampleValidator();
        PersonPE person = createPersonWithRoleAssignments();
        assertEquals(false, validator.isValid(person, SampleTranslator.translate(
                createSample(createDatabaseInstance("blabla")), BASE_URL)));
    }

    @Test
    public void testIsValidWithTheRightDatabaseInstance()
    {
        SampleValidator validator = new SampleValidator();
        PersonPE person = createPersonWithRoleAssignments();
        assertEquals(true, validator.isValid(person, SampleTranslator.translate(
                createSample(createGroup()), BASE_URL)));
    }

    @Test
    public void testIsValidWithTheWrongGroup()
    {
        SampleValidator validator = new SampleValidator();
        PersonPE person = createPersonWithRoleAssignments();
        GroupPE group = createGroup("blabla", createAnotherDatabaseInstance());
        assertEquals(false, validator.isValid(person, SampleTranslator.translate(
                createSample(group), BASE_URL)));
    }
}
