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

import org.jmock.Expectations;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.SpaceOwnerKind;
import ch.systemsx.cisd.openbis.generic.server.authorization.predicate.AbstractTechIdPredicate.ProjectTechIdPredicate;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;

/**
 * @author pkupczyk
 */
public class ProjectTechIdPredicateTest extends AuthorizationTestCase
{

    private static Long PROJECT_ID = 123L;

    @Test
    public void testWithNonexistentProjectForInstanceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, new TechId(PROJECT_ID));
                    will(returnValue(null));
                }
            });

        assertError(evaluate(PROJECT_ID, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithNonexistentProjectForSpaceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, new TechId(PROJECT_ID));
                    will(returnValue(null));
                }
            });

        assertError(evaluate(PROJECT_ID, createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithNoAllowedRoles()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, new TechId(PROJECT_ID));
                    will(returnValue(SPACE));
                }
            });

        assertError(evaluate(PROJECT_ID));
    }

    @Test
    public void testWithMultipleAllowedRoles()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, new TechId(PROJECT_ID));
                    will(returnValue(SPACE));
                }
            });

        assertOK(evaluate(PROJECT_ID, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE), createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithInstanceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, new TechId(PROJECT_ID));
                    will(returnValue(SPACE));
                }
            });

        assertOK(evaluate(PROJECT_ID, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithMatchingSpaceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, new TechId(PROJECT_ID));
                    will(returnValue(SPACE));
                }
            });

        assertOK(evaluate(PROJECT_ID, createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithNonMatchingSpaceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, new TechId(PROJECT_ID));
                    will(returnValue(SPACE));
                }
            });

        assertError(evaluate(PROJECT_ID, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE)));
    }

    private Status evaluate(long techId, RoleWithIdentifier... roles)
    {
        ProjectTechIdPredicate predicate = new ProjectTechIdPredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON, Arrays.asList(roles), new TechId(123L));
    }

}
