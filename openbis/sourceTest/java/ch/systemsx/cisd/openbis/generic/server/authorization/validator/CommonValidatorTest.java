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
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public abstract class CommonValidatorTest<O> extends AuthorizationTestCase
{

    protected abstract O createObject(SpacePE spacePE, ProjectPE projectPE);

    protected abstract boolean validateObject(PersonPE personPE, O object);

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNull(final IAuthorizationConfig config)
    {
        PersonPE person = createPersonWithRoleAssignments(createInstanceRoleAssignment(RoleCode.ADMIN));
        try
        {
            assertWithNull(config, validateObject(person, null), null);
        } catch (Throwable t)
        {
            assertWithNull(config, false, t);
        }
    }

    protected void assertWithNull(IAuthorizationConfig config, boolean result, Throwable t)
    {
        assertFalse(result);
        assertEquals(AssertionError.class, t.getClass());
        assertEquals("Unspecified value", t.getMessage());
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNoAllowedRoles(final IAuthorizationConfig config)
    {
        expectAuthorizationConfig(config);

        PersonPE person = createPerson();
        boolean result = validateObject(person, createObject(SPACE_PE, SPACE_PROJECT_PE));
        assertWithNoAllowedRoles(config, result);
    }

    protected void assertWithNoAllowedRoles(IAuthorizationConfig config, boolean result)
    {
        assertFalse(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithMultipleAllowedRoles(final IAuthorizationConfig config)
    {
        expectAuthorizationConfig(config);

        PersonPE person = createPersonWithRoleAssignments(createSpaceRoleAssignment(RoleCode.ADMIN, ANOTHER_SPACE_CODE),
                createSpaceRoleAssignment(RoleCode.ADMIN, SPACE_CODE));
        boolean result = validateObject(person, createObject(SPACE_PE, SPACE_PROJECT_PE));
        assertWithMultipleAllowedRoles(config, result);
    }

    protected void assertWithMultipleAllowedRoles(IAuthorizationConfig config, boolean result)
    {
        assertTrue(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithInstanceUser(final IAuthorizationConfig config)
    {
        expectAuthorizationConfig(config);

        PersonPE person = createPersonWithRoleAssignments(createInstanceRoleAssignment(RoleCode.ADMIN));
        boolean result = validateObject(person, createObject(SPACE_PE, SPACE_PROJECT_PE));
        assertWithInstanceUser(config, result);
    }

    protected void assertWithInstanceUser(IAuthorizationConfig config, boolean result)
    {
        assertTrue(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithMatchingSpaceAndMatchingProjectUser(final IAuthorizationConfig config)
    {
        expectAuthorizationConfig(config);

        PersonPE person = createPersonWithRoleAssignments(createSpaceRoleAssignment(RoleCode.ADMIN, SPACE_CODE),
                createProjectRoleAssignment(RoleCode.ADMIN, SPACE_CODE, SPACE_PROJECT_CODE));
        boolean result = validateObject(person, createObject(SPACE_PE, SPACE_PROJECT_PE));
        assertWithMatchingSpaceAndMatchingProjectUser(config, result);
    }

    protected void assertWithMatchingSpaceAndMatchingProjectUser(IAuthorizationConfig config, boolean result)
    {
        assertTrue(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithMatchingSpaceAndNonMatchingProjectUser(final IAuthorizationConfig config)
    {
        expectAuthorizationConfig(config);

        PersonPE person = createPersonWithRoleAssignments(createSpaceRoleAssignment(RoleCode.ADMIN, SPACE_CODE),
                createProjectRoleAssignment(RoleCode.ADMIN, ANOTHER_SPACE_CODE, ANOTHER_SPACE_PROJECT_CODE));
        boolean result = validateObject(person, createObject(SPACE_PE, SPACE_PROJECT_PE));
        assertWithMatchingSpaceAndNonMatchingProjectUser(config, result);
    }

    protected void assertWithMatchingSpaceAndNonMatchingProjectUser(IAuthorizationConfig config, boolean result)
    {
        assertTrue(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNonMatchingSpaceAndMatchingProjectUser(final IAuthorizationConfig config)
    {
        expectAuthorizationConfig(config);

        PersonPE person = createPersonWithRoleAssignments(createSpaceRoleAssignment(RoleCode.ADMIN, ANOTHER_SPACE_CODE),
                createProjectRoleAssignment(RoleCode.ADMIN, SPACE_CODE, SPACE_PROJECT_CODE));
        person.setUserId(PERSON_PE.getUserId());
        boolean result = validateObject(person, createObject(SPACE_PE, SPACE_PROJECT_PE));
        assertWithNonMatchingSpaceAndMatchingProjectUser(config, result);
    }

    protected void assertWithNonMatchingSpaceAndMatchingProjectUser(IAuthorizationConfig config, boolean result)
    {
        if (config.isProjectLevelEnabled())
        {
            assertTrue(result);
        } else
        {
            assertFalse(result);
        }
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNonMatchingSpaceAndNonMatchingProjectUser(final IAuthorizationConfig config)
    {
        expectAuthorizationConfig(config);

        PersonPE person = createPersonWithRoleAssignments(createSpaceRoleAssignment(RoleCode.ADMIN, ANOTHER_SPACE_CODE),
                createProjectRoleAssignment(RoleCode.ADMIN, ANOTHER_SPACE_CODE, ANOTHER_SPACE_PROJECT_CODE));
        person.setUserId(PERSON_PE.getUserId());
        boolean result = validateObject(person, createObject(SPACE_PE, SPACE_PROJECT_PE));
        assertWithNonMatchingSpaceAndNonMatchingProjectUser(config, result);
    }

    protected void assertWithNonMatchingSpaceAndNonMatchingProjectUser(IAuthorizationConfig config, boolean result)
    {
        assertFalse(result);
    }

}
