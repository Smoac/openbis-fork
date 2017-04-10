/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.authorization.validator;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author pkupczyk
 */
public class ProjectByIdentiferValidatorTest extends AuthorizationTestCase
{

    private static final Project SPACE_PROJECT = new Project(SPACE_CODE, "PROJECT");

    @Test(expectedExceptions = AssertionError.class, expectedExceptionsMessageRegExp = "Unspecified value")
    public void testWithNull()
    {
        PersonPE person = createPersonWithRoleAssignments(createInstanceRoleAssignment(RoleCode.ADMIN));
        assertFalse(validate(person, null));
    }

    @Test
    public void testWithNoAllowedRoles()
    {
        PersonPE person = createPerson();
        assertFalse(validate(person, SPACE_PROJECT));
    }

    @Test
    public void testWithMultipleAllowedRoles()
    {
        PersonPE person = createPersonWithRoleAssignments(createSpaceRoleAssignment(RoleCode.ADMIN, ANOTHER_SPACE_CODE),
                createSpaceRoleAssignment(RoleCode.ADMIN, SPACE_CODE));
        assertTrue(validate(person, SPACE_PROJECT));
    }

    @Test
    public void testWithInstanceUser()
    {
        PersonPE person = createPersonWithRoleAssignments(createInstanceRoleAssignment(RoleCode.ADMIN));
        assertTrue(validate(person, SPACE_PROJECT));
    }

    @Test
    public void testWithMatchingSpaceUser()
    {
        PersonPE person = createPersonWithRoleAssignments(createSpaceRoleAssignment(RoleCode.ADMIN, SPACE_CODE));
        assertTrue(validate(person, SPACE_PROJECT));
    }

    @Test
    public void testWithNonMatchingSpaceUser()
    {
        PersonPE person = createPersonWithRoleAssignments(createSpaceRoleAssignment(RoleCode.ADMIN, ANOTHER_SPACE_CODE));
        assertFalse(validate(person, SPACE_PROJECT));
    }

    private boolean validate(PersonPE person, Project project)
    {
        ProjectByIdentiferValidator validator = new ProjectByIdentiferValidator();
        validator.init(provider);
        return validator.isValid(person, project);
    }

}
