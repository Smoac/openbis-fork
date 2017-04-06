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

/**
 * @author pkupczyk
 */
public class ProjectAugmentedCodePredicateTest extends AuthorizationTestCase
{

    private static String SPACE_PROJECT = "/" + SPACE_CODE + "/PROJECT";

    private static String ANOTHER_SPACE_PROJECT = "/" + ANOTHER_SPACE_CODE + "/PROJECT";

    @Test
    public void testWithNonexistentSpaceForInstanceUser()
    {
        prepareProvider(Arrays.asList(SPACE));
        assertOK(evaluate(ANOTHER_SPACE_PROJECT, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithNonexistentSpaceForSpaceUser()
    {
        prepareProvider(Arrays.asList(SPACE));
        assertError(evaluate(ANOTHER_SPACE_PROJECT, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE)));
    }

    @Test
    public void testWithNoAllowedRoles()
    {
        prepareProvider(ALL_SPACES);
        assertError(evaluate(SPACE_PROJECT));
    }

    @Test
    public void testWithMultipleAllowedRoles()
    {
        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE), createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithInstanceUser()
    {
        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT, createInstanceRole(RoleCode.ADMIN)));
    }

    @Test
    public void testWithMatchingSpaceUser()
    {
        prepareProvider(ALL_SPACES);
        assertOK(evaluate(SPACE_PROJECT, createSpaceRole(RoleCode.ADMIN, SPACE)));
    }

    @Test
    public void testWithNonMatchingSpaceUser()
    {
        prepareProvider(ALL_SPACES);
        assertError(evaluate(SPACE_PROJECT, createSpaceRole(RoleCode.ADMIN, ANOTHER_SPACE)));
    }

    private Status evaluate(String projectIdentifier, RoleWithIdentifier... roles)
    {
        ProjectAugmentedCodePredicate predicate = new ProjectAugmentedCodePredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON, Arrays.asList(roles), projectIdentifier);
    }

}
