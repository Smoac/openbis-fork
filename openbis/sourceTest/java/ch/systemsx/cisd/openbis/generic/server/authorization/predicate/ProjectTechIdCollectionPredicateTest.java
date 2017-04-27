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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.SpaceOwnerKind;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdCollectionPredicate.ProjectTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ProjectTechIdCollectionPredicateTest extends CommonPredicateTest<List<TechId>>
{

    private static TechId PROJECT_ID = new TechId(1231L);

    private static TechId PROJECT_ID_2 = new TechId(1232L);

    private static TechId NON_EXISTENT_PROJECT_ID = new TechId(234L);

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithTwoObjectsWhereOneObjectMatchesAtProjectLevelAndTheOtherObjectMatchesAtSpaceLevel(final IAuthorizationConfig config)
    {
        prepareProvider(ALL_SPACES_PE);
        expectAuthorizationConfig(config);

        context.checking(new Expectations()
            {
                {
                    if (config.isProjectLevelEnabled())
                    {
                        allowing(provider).tryGetProjectsByTechIds(Arrays.asList(PROJECT_ID, PROJECT_ID_2));

                        Map<TechId, ProjectPE> map = new HashMap<TechId, ProjectPE>();
                        map.put(PROJECT_ID, SPACE_PROJECT_PE);
                        map.put(PROJECT_ID_2, ANOTHER_SPACE_PROJECT_PE);
                        will(returnValue(map));

                        allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID));
                        will(returnValue(new HashSet<SpacePE>(Arrays.asList(SPACE_PE))));
                    } else
                    {
                        allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID, PROJECT_ID_2));
                        will(returnValue(new HashSet<SpacePE>(Arrays.asList(SPACE_PE, ANOTHER_SPACE_PE))));
                    }

                }
            });

        Status result = evaluateObject(Arrays.asList(PROJECT_ID, PROJECT_ID_2), createSpaceRole(RoleCode.ADMIN, SPACE_CODE),
                createProjectRole(RoleCode.ADMIN, ANOTHER_SPACE_CODE, ANOTHER_SPACE_PROJECT_CODE));

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
        prepareProvider(ALL_SPACES_PE);
        expectAuthorizationConfig(config);

        context.checking(new Expectations()
            {
                {
                    if (config.isProjectLevelEnabled())
                    {
                        allowing(provider).tryGetProjectsByTechIds(Arrays.asList(PROJECT_ID, PROJECT_ID_2));

                        Map<TechId, ProjectPE> map = new HashMap<TechId, ProjectPE>();
                        map.put(PROJECT_ID, SPACE_PROJECT_PE);
                        map.put(PROJECT_ID_2, ANOTHER_SPACE_PROJECT_PE);
                        will(returnValue(map));

                        allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID_2));
                        will(returnValue(new HashSet<SpacePE>(Arrays.asList(ANOTHER_SPACE_PE))));
                    } else
                    {
                        allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID, PROJECT_ID_2));
                        will(returnValue(new HashSet<SpacePE>(Arrays.asList(SPACE_PE, ANOTHER_SPACE_PE))));
                    }

                }
            });

        Status result = evaluateObject(Arrays.asList(PROJECT_ID, PROJECT_ID_2), createProjectRole(RoleCode.ADMIN, SPACE_CODE, SPACE_PROJECT_CODE));

        assertError(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithTwoObjectsWhereOneObjectMatchesAtSpaceLevelAndTheOtherObjectDoesNotMatchAtAnyLevel(final IAuthorizationConfig config)
    {
        prepareProvider(ALL_SPACES_PE);
        expectAuthorizationConfig(config);

        context.checking(new Expectations()
            {
                {
                    if (config.isProjectLevelEnabled())
                    {
                        allowing(provider).tryGetProjectsByTechIds(Arrays.asList(PROJECT_ID, PROJECT_ID_2));

                        Map<TechId, ProjectPE> map = new HashMap<TechId, ProjectPE>();
                        map.put(PROJECT_ID, SPACE_PROJECT_PE);
                        map.put(PROJECT_ID_2, ANOTHER_SPACE_PROJECT_PE);
                        will(returnValue(map));
                    }

                    allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID, PROJECT_ID_2));
                    will(returnValue(new HashSet<SpacePE>(Arrays.asList(SPACE_PE, ANOTHER_SPACE_PE))));
                }
            });

        Status result = evaluateObject(Arrays.asList(PROJECT_ID, PROJECT_ID_2), createSpaceRole(RoleCode.ADMIN, SPACE_CODE));

        assertError(result);
    }

    @Test(dataProvider = AUTHORIZATION_CONFIG_PROVIDER)
    public void testWithTwoObjectsWhereBothObjectsMatchAtProjectLevel(final IAuthorizationConfig config)
    {
        prepareProvider(ALL_SPACES_PE);
        expectAuthorizationConfig(config);

        context.checking(new Expectations()
            {
                {
                    if (config.isProjectLevelEnabled())
                    {
                        allowing(provider).tryGetProjectsByTechIds(Arrays.asList(PROJECT_ID, PROJECT_ID_2));

                        Map<TechId, ProjectPE> map = new HashMap<TechId, ProjectPE>();
                        map.put(PROJECT_ID, SPACE_PROJECT_PE);
                        map.put(PROJECT_ID_2, ANOTHER_SPACE_PROJECT_PE);
                        will(returnValue(map));
                    } else
                    {
                        allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID, PROJECT_ID_2));
                        will(returnValue(new HashSet<SpacePE>(Arrays.asList(SPACE_PE, ANOTHER_SPACE_PE))));
                    }
                }
            });

        Status result = evaluateObject(Arrays.asList(PROJECT_ID, PROJECT_ID_2), createProjectRole(RoleCode.ADMIN, SPACE_CODE, SPACE_PROJECT_CODE),
                createProjectRole(RoleCode.ADMIN, ANOTHER_SPACE_CODE, ANOTHER_SPACE_PROJECT_CODE));

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
        prepareProvider(ALL_SPACES_PE);
        expectAuthorizationConfig(config);

        context.checking(new Expectations()
            {
                {
                    if (config.isProjectLevelEnabled())
                    {
                        allowing(provider).tryGetProjectsByTechIds(Arrays.asList(PROJECT_ID, PROJECT_ID_2));

                        Map<TechId, ProjectPE> map = new HashMap<TechId, ProjectPE>();
                        map.put(PROJECT_ID, SPACE_PROJECT_PE);
                        map.put(PROJECT_ID_2, ANOTHER_SPACE_PROJECT_PE);
                        will(returnValue(map));
                    }

                    allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID, PROJECT_ID_2));
                    will(returnValue(new HashSet<SpacePE>(Arrays.asList(SPACE_PE, ANOTHER_SPACE_PE))));
                }
            });

        Status result = evaluateObject(Arrays.asList(PROJECT_ID, PROJECT_ID_2), createSpaceRole(RoleCode.ADMIN, SPACE_CODE),
                createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE_CODE));

        assertOK(result);
    }

    @Override
    protected void expectWithAll(IAuthorizationConfig config, final List<TechId> object)
    {
        prepareProvider(ALL_SPACES_PE);
        expectAuthorizationConfig(config);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectsByTechIds(object);

                    if (Arrays.asList(PROJECT_ID).equals(object))
                    {
                        will(returnValue(Collections.singletonMap(object.get(0), SPACE_PROJECT_PE)));
                    } else if (Arrays.asList(NON_EXISTENT_PROJECT_ID).equals(object))
                    {
                        will(returnValue(Collections.emptyMap()));
                    }

                    allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, object);

                    if (Arrays.asList(PROJECT_ID).equals(object))
                    {
                        will(returnValue(Collections.singleton(SPACE_PE)));
                    } else if (Arrays.asList(NON_EXISTENT_PROJECT_ID).equals(object))
                    {
                        will(returnValue(Collections.emptySet()));
                    }
                }
            });
    }

    @Override
    protected List<TechId> createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        if (SPACE_PE.equals(spacePE) && SPACE_PROJECT_PE.equals(projectPE))
        {
            return Arrays.asList(PROJECT_ID);
        } else if (NON_EXISTENT_SPACE_PE.equals(spacePE) && NON_EXISTENT_SPACE_PROJECT_PE.equals(projectPE))
        {
            return Arrays.asList(NON_EXISTENT_PROJECT_ID);
        } else
        {
            throw new RuntimeException();
        }
    }

    @Override
    protected Status evaluateObject(List<TechId> object, RoleWithIdentifier... roles)
    {
        ProjectTechIdCollectionPredicate predicate = new ProjectTechIdCollectionPredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON_PE, Arrays.asList(roles), object);
    }

    @Override
    protected void assertWithNull(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertNull(result);
        assertEquals(UserFailureException.class, t.getClass());
        assertEquals("No PROJECT technical id collection specified.", t.getMessage());
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

    @Override
    protected void assertWithNonexistentObjectForSpaceUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

    @Override
    protected void assertWithNonexistentObjectForProjectUser(IAuthorizationConfig config, Status result)
    {
        assertOK(result);
    }

}