/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.delete.RoleAssignmentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class DeleteRoleAssignmentTest extends AbstractTest
{
    @Test
    public void testDelete()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode("TEST-" + System.currentTimeMillis());
        SpacePermId spacePermId = v3api.createSpaces(sessionToken, Arrays.asList(spaceCreation)).get(0);
        RoleAssignmentCreation creation = new RoleAssignmentCreation();
        creation.setRole(Role.POWER_USER);
        creation.setUserId(new PersonPermId(TEST_NO_HOME_SPACE));
        creation.setSpaceId(spacePermId);
        List<RoleAssignmentTechId> assignments = v3api.createRoleAssignments(sessionToken, Arrays.asList(creation));
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withSpace();
        RoleAssignment assignment = v3api.getRoleAssignments(sessionToken, assignments, fetchOptions).get(assignments.get(0));
        assertEquals(assignment.getSpace().getCode(), spaceCreation.getCode());

        // When
        RoleAssignmentDeletionOptions deletionOptions = new RoleAssignmentDeletionOptions();
        deletionOptions.setReason("test");
        v3api.deleteRoleAssignments(sessionToken, assignments, deletionOptions);
        
        // Then
        Map<IRoleAssignmentId, RoleAssignment> map = v3api.getRoleAssignments(sessionToken, assignments, fetchOptions);
        assertEquals(map.toString(), "{}");
        v3api.logout(sessionToken);
    }
}
