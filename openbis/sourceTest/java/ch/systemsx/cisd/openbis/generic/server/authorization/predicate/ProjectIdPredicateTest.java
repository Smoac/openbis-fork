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
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.IProjectId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.PermId;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
public class ProjectIdPredicateTest extends AuthorizationTestCase
{

    private static final ProjectPE SPACE_PROJECT = new ProjectPE();

    private static final ProjectPE ANOTHER_SPACE_PROJECT = new ProjectPE();

    private static final ProjectIdentifierId SPACE_PROJECT_IDENTIFIER = new ProjectIdentifierId("/" + SPACE_CODE + "/PROJECT");

    private static final ProjectPermIdId SPACE_PROJECT_PERM_ID = new ProjectPermIdId("spaceProjectPermId");

    private static final ProjectTechIdId SPACE_PROJECT_TECH_ID = new ProjectTechIdId(123L);

    static
    {
        SPACE_PROJECT.setSpace(SPACE);
        ANOTHER_SPACE_PROJECT.setSpace(ANOTHER_SPACE);
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "No project id specified.")
    public void testWithNonexistentProjectForInstanceUser()
    {
        prepareProvider(ALL_SPACES);
        assertError(evaluate(null, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "No project id specified.")
    public void testWithNonexistentProjectForSpaceUser()
    {
        prepareProvider(ALL_SPACES);
        assertError(evaluate(null, createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithNoAllowedRolesWithIdentifier()
    {
        prepareProvider(ALL_SPACES);
        assertError(evaluate(SPACE_PROJECT_IDENTIFIER));
    }

    @Test
    public void testWithNoAllowedRolesWithPermId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(new PermId(SPACE_PROJECT_PERM_ID.getPermId()));
                    will(returnValue(SPACE_PROJECT));
                }
            });

        prepareProvider(ALL_SPACES);
        assertError(evaluate(SPACE_PROJECT_PERM_ID));
    }

    @Test
    public void testWithNoAllowedRolesWithTechId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, new TechId(SPACE_PROJECT_TECH_ID.getTechId()));
                    will(returnValue(SPACE));
                }
            });

        prepareProvider(ALL_SPACES);
        assertError(evaluate(SPACE_PROJECT_TECH_ID));
    }

    @Test
    public void testWithMultipleAllowedRolesWithIdentifier()
    {
        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_IDENTIFIER, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE), createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithMultipleAllowedRolesWithPermId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(new PermId(SPACE_PROJECT_PERM_ID.getPermId()));
                    will(returnValue(SPACE_PROJECT));
                }
            });

        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_PERM_ID, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE), createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithMultipleAllowedRolesWithTechId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, new TechId(SPACE_PROJECT_TECH_ID.getTechId()));
                    will(returnValue(SPACE));
                }
            });

        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_TECH_ID, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE), createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithInstanceUserWithIdentifier()
    {
        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_IDENTIFIER, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithInstanceUserWithPermId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(new PermId(SPACE_PROJECT_PERM_ID.getPermId()));
                    will(returnValue(SPACE_PROJECT));
                }
            });

        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_PERM_ID, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithInstanceUserWithTechId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, new TechId(SPACE_PROJECT_TECH_ID.getTechId()));
                    will(returnValue(SPACE));
                }
            });

        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_TECH_ID, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithMatchingSpaceUserWithIdentifier()
    {
        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_IDENTIFIER, createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithMatchingSpaceUserWithPermId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(new PermId(SPACE_PROJECT_PERM_ID.getPermId()));
                    will(returnValue(SPACE_PROJECT));
                }
            });

        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_PERM_ID, createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithMatchingSpaceUserWithTechId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, new TechId(SPACE_PROJECT_TECH_ID.getTechId()));
                    will(returnValue(SPACE));
                }
            });

        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT_TECH_ID, createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithNonMatchingSpaceUserWithIdentifier()
    {
        prepareProvider(ALL_SPACES);
        assertError(evaluate(SPACE_PROJECT_IDENTIFIER, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE)));
    }

    @Test
    public void testWithNonMatchingSpaceUserWithPermId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetProjectByPermId(new PermId(SPACE_PROJECT_PERM_ID.getPermId()));
                    will(returnValue(SPACE_PROJECT));
                }
            });

        prepareProvider(ALL_SPACES);
        assertError(evaluate(SPACE_PROJECT_PERM_ID, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE)));
    }

    @Test
    public void testWithNonMatchingSpaceUserWithTechId()
    {
        context.checking(new Expectations()
            {
                {
                    allowing(provider).tryGetSpace(SpaceOwnerKind.PROJECT, new TechId(SPACE_PROJECT_TECH_ID.getTechId()));
                    will(returnValue(SPACE));
                }
            });

        prepareProvider(ALL_SPACES);
        assertError(evaluate(SPACE_PROJECT_TECH_ID, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE)));
    }

    private Status evaluate(IProjectId projectId, RoleWithIdentifier... roles)
    {
        ProjectIdPredicate predicate = new ProjectIdPredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON, Arrays.asList(roles), projectId);
    }

}
