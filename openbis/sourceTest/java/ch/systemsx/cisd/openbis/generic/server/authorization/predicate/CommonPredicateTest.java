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

import java.util.Arrays;
import java.util.List;

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

    protected abstract Status evaluateObjects(List<O> objects, RoleWithIdentifier... roles);

    protected void expectWithAll(IAuthorizationConfig config, List<O> objects)
    {

    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNull(IAuthorizationConfig config)
    {
        try
        {
            List<O> objects = Arrays.asList((O) null);
            expectWithAll(config, objects);
            Status result = evaluateObjects(objects);
            assertWithNull(config, result, null);
        } catch (Throwable t)
        {
            assertWithNull(config, null, t);
        }
    }

    protected void assertWithNull(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertNull(result);
        assertException(t, UserFailureException.class, "Unspecified value");
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNonexistentObjectForInstanceUser(IAuthorizationConfig config)
    {
        O object = createObject(NON_EXISTENT_SPACE_PE, NON_EXISTENT_SPACE_PROJECT_PE);
        List<O> objects = Arrays.asList(object);
        expectWithAll(config, objects);
        Status result = evaluateObjects(objects, createInstanceRole(RoleCode.ADMIN));
        assertWithNonexistentObjectForInstanceUser(config, result);
    }

    protected void assertWithNonexistentObjectForInstanceUser(IAuthorizationConfig config, Status result)
    {
        assertError(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNonexistentObjectForSpaceUser(IAuthorizationConfig config)
    {
        O object = createObject(NON_EXISTENT_SPACE_PE, NON_EXISTENT_SPACE_PROJECT_PE);
        List<O> objects = Arrays.asList(object);
        expectWithAll(config, objects);
        Status result = evaluateObjects(objects, createSpaceRole(RoleCode.ADMIN, SPACE_PE));
        assertWithNonexistentObjectForSpaceUser(config, result);
    }

    protected void assertWithNonexistentObjectForSpaceUser(IAuthorizationConfig config, Status result)
    {
        assertError(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNonexistentObjectForProjectUser(IAuthorizationConfig config)
    {
        O object = createObject(NON_EXISTENT_SPACE_PE, NON_EXISTENT_SPACE_PROJECT_PE);
        List<O> objects = Arrays.asList(object);
        expectWithAll(config, objects);
        Status result = evaluateObjects(objects, createProjectRole(RoleCode.ADMIN, SPACE_PROJECT_PE));
        assertWithNonexistentObjectForProjectUser(config, result);
    }

    protected void assertWithNonexistentObjectForProjectUser(IAuthorizationConfig config, Status result)
    {
        assertError(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNoAllowedRoles(IAuthorizationConfig config)
    {
        O object = createObject(SPACE_PE, SPACE_PROJECT_PE);
        List<O> objects = Arrays.asList(object);
        expectWithAll(config, objects);
        Status result = evaluateObjects(objects);
        assertWithNoAllowedRoles(config, result);
    }

    protected void assertWithNoAllowedRoles(IAuthorizationConfig config, Status result)
    {
        assertError(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithMultipleAllowedRoles(IAuthorizationConfig config)
    {
        O object = createObject(SPACE_PE, SPACE_PROJECT_PE);
        List<O> objects = Arrays.asList(object);
        expectWithAll(config, objects);
        Status result = evaluateObjects(objects, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE_PE), createSpaceRole(RoleCode.ADMIN, SPACE_PE));
        assertWithMultipleAllowedRoles(config, result);
    }

    protected void assertWithMultipleAllowedRoles(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithInstanceUser(IAuthorizationConfig config)
    {
        O object = createObject(SPACE_PE, SPACE_PROJECT_PE);
        List<O> objects = Arrays.asList(object);
        expectWithAll(config, objects);
        Status result = evaluateObjects(objects, createInstanceRole(RoleCode.ADMIN));
        assertWithInstanceUser(config, result);
    }

    protected void assertWithInstanceUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithMatchingSpaceAndMatchingProjectUser(IAuthorizationConfig config)
    {
        O object = createObject(SPACE_PE, SPACE_PROJECT_PE);
        List<O> objects = Arrays.asList(object);
        expectWithAll(config, objects);
        Status result = evaluateObjects(objects, createSpaceRole(RoleCode.ADMIN, SPACE_PE),
                createProjectRole(RoleCode.ADMIN, SPACE_PROJECT_PE));
        assertWithMatchingSpaceAndMatchingProjectUser(config, result);
    }

    protected void assertWithMatchingSpaceAndMatchingProjectUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithMatchingSpaceAndNonMatchingProjectUser(IAuthorizationConfig config)
    {
        O object = createObject(SPACE_PE, SPACE_PROJECT_PE);
        List<O> objects = Arrays.asList(object);
        expectWithAll(config, objects);
        Status result = evaluateObjects(objects, createSpaceRole(RoleCode.ADMIN, SPACE_PE),
                createProjectRole(RoleCode.ADMIN, ANOTHER_SPACE_PROJECT_PE));
        assertWithMatchingSpaceAndNonMatchingProjectUser(config, result);
    }

    protected void assertWithMatchingSpaceAndNonMatchingProjectUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNonMatchingSpaceAndMatchingProjectUser(IAuthorizationConfig config)
    {
        O object = createObject(SPACE_PE, SPACE_PROJECT_PE);
        List<O> objects = Arrays.asList(object);
        expectWithAll(config, objects);
        Status result = evaluateObjects(objects, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE_PE),
                createProjectRole(RoleCode.ADMIN, SPACE_PROJECT_PE));
        assertWithNonMatchingSpaceAndMatchingProjectUser(config, result);
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
        List<O> objects = Arrays.asList(object);
        expectWithAll(config, objects);
        Status result = evaluateObjects(objects, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE_PE),
                createProjectRole(RoleCode.ADMIN, ANOTHER_SPACE_PROJECT_PE));
        assertWithNonMatchingSpaceAndNonMatchingProjectUser(config, result);
    }

    protected void assertWithNonMatchingSpaceAndNonMatchingProjectUser(IAuthorizationConfig config, Status result)
    {
        assertError(result);
    }

    protected static void assertException(Throwable actualException, Class<?> expectedClass, String expectedMessage)
    {
        if (false == actualException.getClass().equals(expectedClass))
        {
            actualException.printStackTrace(System.err);
            fail();
        } else
        {
            actualException.printStackTrace(System.out);
        }
        assertEquals(expectedMessage, actualException.getMessage());
    }

}
