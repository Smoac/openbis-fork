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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
public class ProjectPermIdPredicateTest extends AuthorizationTestCase
{

    private static final PermId SPACE_PROJECT_PERM_ID = new PermId("projectPermId");

    private static final ProjectPE SPACE_PROJECT = new ProjectPE();

    private static final ProjectPE ANOTHER_SPACE_PROJECT = new ProjectPE();

    static
    {
        SPACE_PROJECT.setSpace(SPACE);
        ANOTHER_SPACE_PROJECT.setSpace(ANOTHER_SPACE);
    }

    @Test
    public void testWithNonexistentProjectForInstanceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(SPACE_PROJECT_PERM_ID);
                    will(returnValue(null));
                }
            });

        assertOK(evaluate(SPACE_PROJECT_PERM_ID, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithNonexistentProjectForSpaceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(SPACE_PROJECT_PERM_ID);
                    will(returnValue(null));
                }
            });

        assertOK(evaluate(SPACE_PROJECT_PERM_ID, createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithNoAllowedRoles()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(SPACE_PROJECT_PERM_ID);
                    will(returnValue(SPACE_PROJECT));
                }
            });

        assertError(evaluate(SPACE_PROJECT_PERM_ID));
    }

    @Test
    public void testWithMultipleAllowedRoles()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(SPACE_PROJECT_PERM_ID);
                    will(returnValue(SPACE_PROJECT));
                }
            });

        assertOK(evaluate(SPACE_PROJECT_PERM_ID, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE), createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithInstanceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(SPACE_PROJECT_PERM_ID);
                    will(returnValue(SPACE_PROJECT));
                }
            });

        assertOK(evaluate(SPACE_PROJECT_PERM_ID, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithMatchingSpaceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(SPACE_PROJECT_PERM_ID);
                    will(returnValue(SPACE_PROJECT));
                }
            });

        assertOK(evaluate(SPACE_PROJECT_PERM_ID, createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithNonMatchingSpaceUser()
    {
        prepareProvider(ALL_SPACES);

        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(SPACE_PROJECT_PERM_ID);
                    will(returnValue(SPACE_PROJECT));
                }
            });

        assertError(evaluate(SPACE_PROJECT_PERM_ID, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE)));
    }

    private Status evaluate(PermId permId, RoleWithIdentifier... roles)
    {
        ProjectPermIdPredicate predicate = new ProjectPermIdPredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON, Arrays.asList(roles), permId);
    }

}
