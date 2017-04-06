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

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationTestCase;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * @author pkupczyk
 */
public class ProjectPEPredicateTest extends AuthorizationTestCase
{

    private static final ProjectPE SPACE_PROJECT = new ProjectPE();

    private static final ProjectPE ANOTHER_SPACE_PROJECT = new ProjectPE();

    static
    {
        SPACE_PROJECT.setSpace(SPACE);
        ANOTHER_SPACE_PROJECT.setSpace(ANOTHER_SPACE);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testWithNonexistentProjectForInstanceUser()
    {
        evaluate(null, createInstanceRole(RoleCode.ADMIN));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testWithNonexistentProjectForSpaceUser()
    {
        evaluate(null, createSpaceRole(RoleCode.ADMIN, SPACE));
    }

    @Test
    public void testWithNoAllowedRoles()
    {
        assertError(evaluate(SPACE_PROJECT));
    }

    @Test
    public void testWithMultipleAllowedRoles()
    {
        assertOK(evaluate(SPACE_PROJECT, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE), createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithInstanceUser()
    {
        assertOK(evaluate(SPACE_PROJECT, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithMatchingSpaceUser()
    {
        assertOK(evaluate(SPACE_PROJECT, createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithNonMatchingSpaceUser()
    {
        assertError(evaluate(SPACE_PROJECT, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE)));
    }

    private Status evaluate(ProjectPE project, RoleWithIdentifier... roles)
    {
        ProjectPEPredicate predicate = new ProjectPEPredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON, Arrays.asList(roles), project);
    }

}
