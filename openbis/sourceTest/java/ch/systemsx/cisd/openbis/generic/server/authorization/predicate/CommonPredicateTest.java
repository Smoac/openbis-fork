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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public abstract class CommonPredicateTest<O> extends AuthorizationTestCase
{

    protected abstract O createObject(SpacePE spacePE, ProjectPE projectPE);

    protected abstract Status evaluateObject(O object, RoleWithIdentifier... roles);

    protected void expectWithAll(IAuthorizationConfig config, O object)
    {

    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNull(IAuthorizationConfig config)
    {
        try
        {
            expectWithAll(config, null);
            expectWithNull(config, null);
            Status result = evaluateObject(null);
            assertWithNull(config, result, null);
        } catch (Throwable t)
        {
            assertWithNull(config, null, t);
        }

    }

    protected void expectWithNull(IAuthorizationConfig config, O object)
    {
    }

    protected void assertWithNull(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertNull(result);
        assertEquals(UserFailureException.class, t.getClass());
        assertEquals("Unspecified value", t.getMessage());
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNonexistentObjectForInstanceUser(IAuthorizationConfig config)
    {
        O object = createObject(NON_EXISTENT_SPACE_PE, NON_EXISTENT_SPACE_PROJECT_PE);
        expectWithAll(config, object);
        expectWithNonexistentObjectForInstanceUser(config, object);
        Status result = evaluateObject(object, createInstanceRole(RoleCode.ADMIN));
        assertWithNonexistentObjectForInstanceUser(config, result);
    }

    protected void expectWithNonexistentObjectForInstanceUser(IAuthorizationConfig config, O object)
    {
    }

    protected void assertWithNonexistentObjectForInstanceUser(IAuthorizationConfig config, Status result)
    {
        assertError(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNonexistentObjectForSpaceUser(IAuthorizationConfig config)
    {
        O object = createObject(NON_EXISTENT_SPACE_PE, NON_EXISTENT_SPACE_PROJECT_PE);
        expectWithAll(config, object);
        expectWithNonexistentObjectForSpaceUser(config, object);
        Status result = evaluateObject(object, createSpaceRole(RoleCode.ADMIN, SPACE_CODE));
        assertWithNonexistentObjectForSpaceUser(config, result);
    }

    protected void expectWithNonexistentObjectForSpaceUser(IAuthorizationConfig config, O object)
    {
    }

    protected void assertWithNonexistentObjectForSpaceUser(IAuthorizationConfig config, Status result)
    {
        assertError(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNonexistentObjectForProjectUser(IAuthorizationConfig config)
    {
        O object = createObject(NON_EXISTENT_SPACE_PE, NON_EXISTENT_SPACE_PROJECT_PE);
        expectWithAll(config, object);
        expectWithNonexistentObjectForProjectUser(config, object);
        Status result = evaluateObject(object, createProjectRole(RoleCode.ADMIN, SPACE_CODE, SPACE_PROJECT_CODE));
        assertWithNonexistentObjectForProjectUser(config, result);
    }

    protected void expectWithNonexistentObjectForProjectUser(IAuthorizationConfig config, O object)
    {
    }

    protected void assertWithNonexistentObjectForProjectUser(IAuthorizationConfig config, Status result)
    {
        assertError(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNoAllowedRoles(IAuthorizationConfig config)
    {
        O object = createObject(SPACE_PE, SPACE_PROJECT_PE);
        expectWithAll(config, object);
        expectWithNoAllowedRoles(config, object);
        Status result = evaluateObject(object);
        assertWithNoAllowedRoles(config, result);
    }

    protected void expectWithNoAllowedRoles(IAuthorizationConfig config, O object)
    {
    }

    protected void assertWithNoAllowedRoles(IAuthorizationConfig config, Status result)
    {
        assertError(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithMultipleAllowedRoles(IAuthorizationConfig config)
    {
        O object = createObject(SPACE_PE, SPACE_PROJECT_PE);
        expectWithAll(config, object);
        expectWithMultipleAllowedRoles(config, object);
        Status result = evaluateObject(object, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE_CODE), createSpaceRole(RoleCode.ADMIN, SPACE_CODE));
        assertWithMultipleAllowedRoles(config, result);
    }

    protected void expectWithMultipleAllowedRoles(IAuthorizationConfig config, O object)
    {
    }

    protected void assertWithMultipleAllowedRoles(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithInstanceUser(IAuthorizationConfig config)
    {
        O object = createObject(SPACE_PE, SPACE_PROJECT_PE);
        expectWithAll(config, object);
        expectWithInstanceUser(config, object);
        Status result = evaluateObject(object, createInstanceRole(RoleCode.ADMIN));
        assertWithInstanceUser(config, result);
    }

    protected void expectWithInstanceUser(IAuthorizationConfig config, O object)
    {
    }

    protected void assertWithInstanceUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithMatchingSpaceAndMatchingProjectUser(IAuthorizationConfig config)
    {
        O object = createObject(SPACE_PE, SPACE_PROJECT_PE);
        expectWithAll(config, object);
        expectWithMatchingSpaceAndMatchingProjectUser(config, object);
        Status result = evaluateObject(object, createSpaceRole(RoleCode.ADMIN, SPACE_CODE),
                createProjectRole(RoleCode.ADMIN, SPACE_CODE, SPACE_PROJECT_CODE));
        assertWithMatchingSpaceAndMatchingProjectUser(config, result);
    }

    protected void expectWithMatchingSpaceAndMatchingProjectUser(IAuthorizationConfig config, O object)
    {
    }

    protected void assertWithMatchingSpaceAndMatchingProjectUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithMatchingSpaceAndNonMatchingProjectUser(IAuthorizationConfig config)
    {
        O object = createObject(SPACE_PE, SPACE_PROJECT_PE);
        expectWithAll(config, object);
        expectWithMatchingSpaceAndNonMatchingProjectUser(config, object);
        Status result = evaluateObject(object, createSpaceRole(RoleCode.ADMIN, SPACE_CODE),
                createProjectRole(RoleCode.ADMIN, ANOTHER_SPACE_CODE, ANOTHER_SPACE_PROJECT_CODE));
        assertWithMatchingSpaceAndNonMatchingProjectUser(config, result);
    }

    protected void expectWithMatchingSpaceAndNonMatchingProjectUser(IAuthorizationConfig config, O object)
    {
    }

    protected void assertWithMatchingSpaceAndNonMatchingProjectUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNonMatchingSpaceAndMatchingProjectUser(IAuthorizationConfig config)
    {
        O object = createObject(SPACE_PE, SPACE_PROJECT_PE);
        expectWithAll(config, object);
        expectWithNonMatchingSpaceAndMatchingProjectUser(config, object);
        Status result = evaluateObject(object, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE_CODE),
                createProjectRole(RoleCode.ADMIN, SPACE_CODE, SPACE_PROJECT_CODE));
        assertWithNonMatchingSpaceAndMatchingProjectUser(config, result);
    }

    protected void expectWithNonMatchingSpaceAndMatchingProjectUser(IAuthorizationConfig config, O object)
    {
    }

    protected void assertWithNonMatchingSpaceAndMatchingProjectUser(IAuthorizationConfig config, Status result)
    {
        if (config.isProjectLevelEnabled())
        {
            assertOK(result);
        } else
        {
            assertError(result);
        }
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNonMatchingSpaceAndNonMatchingProjectUser(IAuthorizationConfig config)
    {
        O object = createObject(SPACE_PE, SPACE_PROJECT_PE);
        expectWithAll(config, object);
        expectWithNonMatchingSpaceAndNonMatchingProjectUser(config, object);
        Status result = evaluateObject(object, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE_CODE),
                createProjectRole(RoleCode.ADMIN, ANOTHER_SPACE_CODE, ANOTHER_SPACE_PROJECT_CODE));
        assertWithNonMatchingSpaceAndNonMatchingProjectUser(config, result);
    }

    protected void expectWithNonMatchingSpaceAndNonMatchingProjectUser(IAuthorizationConfig config, O object)
    {
    }

    protected void assertWithNonMatchingSpaceAndNonMatchingProjectUser(IAuthorizationConfig config, Status result)
    {
        assertError(result);
    }

}
