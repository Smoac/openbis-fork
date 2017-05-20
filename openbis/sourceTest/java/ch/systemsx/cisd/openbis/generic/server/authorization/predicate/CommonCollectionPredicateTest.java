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
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;

/**
 * @author pkupczyk
 */
public abstract class CommonCollectionPredicateTest<O> extends CommonPredicateTest<O>
{

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithNullCollection(IAuthorizationConfig config)
    {
        try
        {
            expectWithAll(config, null);
            Status result = evaluateObjects(null);
            assertWithNullCollection(config, result, null);
        } catch (Throwable t)
        {
            assertWithNullCollection(config, null, t);
        }
    }

    protected void assertWithNullCollection(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertNull(result);
        assertException(t, UserFailureException.class, "Unspecified value");
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectMatchesAtSpaceLevel(final IAuthorizationConfig config)
    {
        O objectInSpaceProject = createObject(SPACE_PE, SPACE_PROJECT_PE);
        O objectInAnotherSpaceAnotherProject = createObject(ANOTHER_SPACE_PE, ANOTHER_SPACE_ANOTHER_PROJECT_PE);

        List<O> objects = Arrays.asList(objectInSpaceProject, objectInAnotherSpaceAnotherProject);

        expectWithAll(config, objects);

        Status result = evaluateObjects(objects, createProjectRole(RoleCode.ADMIN, SPACE_PROJECT_PE),
                createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE_PE));

        assertWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectMatchesAtSpaceLevel(config, result, null);
    }

    protected void assertWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectMatchesAtSpaceLevel(IAuthorizationConfig config,
            Status result, Throwable t)
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
    public void testWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(final IAuthorizationConfig config)
    {
        O objectInSpaceProject = createObject(SPACE_PE, SPACE_PROJECT_PE);
        O objectInAnotherSpaceAnotherProject = createObject(ANOTHER_SPACE_PE, ANOTHER_SPACE_ANOTHER_PROJECT_PE);

        List<O> objects = Arrays.asList(objectInSpaceProject, objectInAnotherSpaceAnotherProject);

        expectWithAll(config, objects);

        Status result = evaluateObjects(objects, createProjectRole(RoleCode.ADMIN, SPACE_PROJECT_PE));

        assertError(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithTwoObjectsWhereOneObjectMatchesAtSpaceLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(final IAuthorizationConfig config)
    {
        O objectInSpaceProject = createObject(SPACE_PE, SPACE_PROJECT_PE);
        O objectInAnotherSpaceAnotherProject = createObject(ANOTHER_SPACE_PE, ANOTHER_SPACE_ANOTHER_PROJECT_PE);

        List<O> objects = Arrays.asList(objectInSpaceProject, objectInAnotherSpaceAnotherProject);

        expectWithAll(config, objects);

        Status result = evaluateObjects(objects, createSpaceRole(RoleCode.ADMIN, SPACE_PE));

        assertError(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithTwoObjectsWhereBothObjectsMatchAtProjectLevel(final IAuthorizationConfig config)
    {
        O objectInSpaceProject = createObject(SPACE_PE, SPACE_PROJECT_PE);
        O objectInAnotherSpaceAnotherProject = createObject(ANOTHER_SPACE_PE, ANOTHER_SPACE_ANOTHER_PROJECT_PE);

        List<O> objects = Arrays.asList(objectInSpaceProject, objectInAnotherSpaceAnotherProject);

        expectWithAll(config, objects);

        Status result = evaluateObjects(objects,
                createProjectRole(RoleCode.ADMIN, SPACE_PROJECT_PE),
                createProjectRole(RoleCode.ADMIN, ANOTHER_SPACE_ANOTHER_PROJECT_PE));

        if (config.isProjectLevelEnabled())
        {
            assertOK(result);
        } else
        {
            assertError(result);
        }
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithTwoObjectsWhereBothObjectsMatchAtSpaceLevel(final IAuthorizationConfig config)
    {
        O objectInSpaceProject = createObject(SPACE_PE, SPACE_PROJECT_PE);
        O objectInAnotherSpaceAnotherProject = createObject(ANOTHER_SPACE_PE, ANOTHER_SPACE_ANOTHER_PROJECT_PE);

        List<O> objects = Arrays.asList(objectInSpaceProject, objectInAnotherSpaceAnotherProject);

        expectWithAll(config, objects);

        Status result = evaluateObjects(objects,
                createSpaceRole(RoleCode.ADMIN, SPACE_PE),
                createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE_PE));

        assertOK(result);
    }

}