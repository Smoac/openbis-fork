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
import java.util.HashSet;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.SpaceOwnerKind;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdCollectionPredicate.ProjectTechIdCollectionPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
public class ProjectTechIdCollectionPredicateTest extends AuthorizationTestCase
{

    private static TechId PROJECT_ID = new TechId(123L);

    private static TechId PROJECT_ID_2 = new TechId(234L);

    @Test
    public void testWithNonexistentProjectForInstanceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID));
                    will(returnValue(Collections.emptySet()));
                }
            });

        assertOK(evaluate(Arrays.asList(PROJECT_ID), createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithNonexistentProjectForSpaceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID));
                    will(returnValue(Collections.emptySet()));
                }
            });

        assertOK(evaluate(Arrays.asList(PROJECT_ID), createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithNoAllowedRoles()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID));
                    will(returnValue(Collections.singleton(SPACE)));
                }
            });

        assertError(evaluate(Arrays.asList(PROJECT_ID)));
    }

    @Test
    public void testWithMultipleAllowedRoles()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID));
                    will(returnValue(Collections.singleton(SPACE)));
                }
            });

        assertOK(evaluate(Arrays.asList(PROJECT_ID), createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE), createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithInstanceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID));
                    will(returnValue(Collections.singleton(SPACE)));
                }
            });

        assertOK(evaluate(Arrays.asList(PROJECT_ID), createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithMatchingSpaceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID));
                    will(returnValue(Collections.singleton(SPACE)));
                }
            });

        assertOK(evaluate(Arrays.asList(PROJECT_ID), createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithNonMatchingSpaceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID));
                    will(returnValue(Collections.singleton(SPACE)));
                }
            });

        assertError(evaluate(Arrays.asList(PROJECT_ID), createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE)));
    }

    @Test
    public void testWithMultipleTechIdsWithAccessToOnlyOne()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID, PROJECT_ID_2));
                    will(returnValue(new HashSet<SpacePE>(Arrays.asList(SPACE, ANOTHER_SPACE))));
                }
            });

        assertError(evaluate(Arrays.asList(PROJECT_ID, PROJECT_ID_2), createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithMultipleTechIdsWithAccessToAll()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).getDistinctSpacesByEntityIds(SpaceOwnerKind.PROJECT, Arrays.asList(PROJECT_ID, PROJECT_ID_2));
                    will(returnValue(new HashSet<SpacePE>(Arrays.asList(SPACE, ANOTHER_SPACE))));
                }
            });

        assertOK(evaluate(Arrays.asList(PROJECT_ID, PROJECT_ID_2), createSpaceRole(RoleCode.ADMIN, SPACE),
                createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE)));
    }

    private Status evaluate(List<TechId> techIds, RoleWithIdentifier... roles)
    {
        ProjectTechIdCollectionPredicate predicate = new ProjectTechIdCollectionPredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON, Arrays.asList(roles), techIds);
    }

}
