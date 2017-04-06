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
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.server.authorization.SpaceOwnerKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;

/**
 * @author pkupczyk
 */
public class ProjectUpdatesPredicateTest extends AuthorizationTestCase
{

    private static final ProjectPE SPACE_PROJECT = new ProjectPE();

    private static final ProjectPE ANOTHER_SPACE_PROJECT = new ProjectPE();

    private static final ProjectUpdatesDTO SPACE_PROJECT_WITH_TECH_ID = new ProjectUpdatesDTO();

    private static final ProjectUpdatesDTO ANOTHER_SPACE_PROJECT_WITH_TECH_ID = new ProjectUpdatesDTO();

    private static final ProjectUpdatesDTO SPACE_PROJECT_WITH_PERM_ID = new ProjectUpdatesDTO();

    private static final ProjectUpdatesDTO ANOTHER_SPACE_PROJECT_WITH_PERM_ID = new ProjectUpdatesDTO();

    private static final ProjectUpdatesDTO SPACE_PROJECT_WITH_IDENTIFIER = new ProjectUpdatesDTO();

    private static final ProjectUpdatesDTO ANOTHER_SPACE_PROJECT_WITH_IDENTIFIER = new ProjectUpdatesDTO();

    static
    {
        SPACE_PROJECT.setSpace(SPACE);
        ANOTHER_SPACE_PROJECT.setSpace(ANOTHER_SPACE);

        SPACE_PROJECT_WITH_TECH_ID.setTechId(new TechId(123L));
        ANOTHER_SPACE_PROJECT_WITH_TECH_ID.setTechId(new TechId(234L));

        SPACE_PROJECT_WITH_PERM_ID.setPermId("spaceProjectPermId");
        ANOTHER_SPACE_PROJECT_WITH_PERM_ID.setPermId("anotherSpaceProjectPermId");

        SPACE_PROJECT_WITH_IDENTIFIER.setIdentifier("/" + SPACE_CODE + "/PROJECT");
        ANOTHER_SPACE_PROJECT_WITH_IDENTIFIER.setIdentifier("/" + ANOTHER_SPACE_CODE + "/PROJECT");
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "No project updates specified.")
    public void testWithNonexistentProjectForInstanceUser()
    {
        prepareProvider(ALL_SPACES);
        assertError(evaluate(null, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "No project updates specified.")
    public void testWithNonexistentProjectForSpaceUser()
    {
        prepareProvider(ALL_SPACES);
        assertError(evaluate(null, createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithNoAllowedRolesWithIdentifier()
    {
        prepareProvider(ALL_SPACES);
        assertError(evaluate(SPACE_PROJECT_WITH_IDENTIFIER));
    }

    @Test
    public void testWithNoAllowedRolesWithPermId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(new PermId(SPACE_PROJECT_WITH_PERM_ID.getPermId()));
                    will(returnValue(SPACE_PROJECT));
                }
            });

        prepareProvider(ALL_SPACES);
        assertError(evaluate(SPACE_PROJECT_WITH_PERM_ID));
    }

    @Test
    public void testWithNoAllowedRolesWithTechId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, SPACE_PROJECT_WITH_TECH_ID.getTechId());
                    will(returnValue(SPACE));
                }
            });

        prepareProvider(ALL_SPACES);
        assertError(evaluate(SPACE_PROJECT_WITH_TECH_ID));
    }

    @Test
    public void testWithMultipleAllowedRolesWithIdentifier()
    {
        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_WITH_IDENTIFIER, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE), createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithMultipleAllowedRolesWithPermId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(new PermId(SPACE_PROJECT_WITH_PERM_ID.getPermId()));
                    will(returnValue(SPACE_PROJECT));
                }
            });

        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_WITH_PERM_ID, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE), createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithMultipleAllowedRolesWithTechId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, SPACE_PROJECT_WITH_TECH_ID.getTechId());
                    will(returnValue(SPACE));
                }
            });

        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_WITH_TECH_ID, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE), createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithInstanceUserWithIdentifier()
    {
        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_WITH_IDENTIFIER, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithInstanceUserWithPermId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(new PermId(SPACE_PROJECT_WITH_PERM_ID.getPermId()));
                    will(returnValue(SPACE_PROJECT));
                }
            });

        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_WITH_PERM_ID, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithInstanceUserWithTechId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, SPACE_PROJECT_WITH_TECH_ID.getTechId());
                    will(returnValue(SPACE));
                }
            });

        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_WITH_TECH_ID, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithMatchingSpaceUserWithIdentifier()
    {
        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_WITH_IDENTIFIER, createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithMatchingSpaceUserWithPermId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(new PermId(SPACE_PROJECT_WITH_PERM_ID.getPermId()));
                    will(returnValue(SPACE_PROJECT));
                }
            });

        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_WITH_PERM_ID, createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithMatchingSpaceUserWithTechId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, SPACE_PROJECT_WITH_TECH_ID.getTechId());
                    will(returnValue(SPACE));
                }
            });

        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_WITH_TECH_ID, createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithNonMatchingSpaceUserWithIdentifier()
    {
        prepareProvider(ALL_SPACES);
        assertError(evaluate(SPACE_PROJECT_WITH_IDENTIFIER, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE)));
    }

    @Test
    public void testWithNonMatchingSpaceUserWithPermId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(new PermId(SPACE_PROJECT_WITH_PERM_ID.getPermId()));
                    will(returnValue(SPACE_PROJECT));
                }
            });

        prepareProvider(ALL_SPACES);
        assertError(evaluate(SPACE_PROJECT_WITH_PERM_ID, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE)));
    }

    @Test
    public void testWithNonMatchingSpaceUserWithTechId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, SPACE_PROJECT_WITH_TECH_ID.getTechId());
                    will(returnValue(SPACE));
                }
            });

        prepareProvider(ALL_SPACES);
        assertError(evaluate(SPACE_PROJECT_WITH_TECH_ID, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE)));
    }

    private Status evaluate(ProjectUpdatesDTO updates, RoleWithIdentifier... roles)
    {
        ProjectUpdatesPredicate predicate = new ProjectUpdatesPredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON, Arrays.asList(roles), updates);
    }

}
