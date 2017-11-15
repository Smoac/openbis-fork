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
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class GetRoleAssignmentsTest extends AbstractTest
{

    @Test
    public void testOnlyBasic()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentTechId id1 = new RoleAssignmentTechId(1L);
        RoleAssignmentTechId id2 = new RoleAssignmentTechId(2L);
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();

        // When
        Map<IRoleAssignmentId, RoleAssignment> map = v3api.getRoleAssignments(sessionToken, Arrays.asList(id1, id2), fetchOptions);
        
        // Then
        assertEquals(map.get(id1).getRole(), Role.ADMIN);
        assertEquals(map.get(id1).getRoleLevel(), RoleLevel.INSTANCE);
        assertEquals(map.get(id2).getRole(), Role.ADMIN);
        assertEquals(map.get(id2).getRoleLevel(), RoleLevel.SPACE);
    }
    
    @Test
    public void testWithSpaceAndProject()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentTechId id1 = new RoleAssignmentTechId(19L);
        RoleAssignmentTechId id2 = new RoleAssignmentTechId(6L);
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withProject().withSpace();
        fetchOptions.withSpace();

        // When
        Map<IRoleAssignmentId, RoleAssignment> map = v3api.getRoleAssignments(sessionToken, Arrays.asList(id1, id2), fetchOptions);
        
        // Then
        assertEquals(map.get(id1).getRole(), Role.ADMIN);
        assertEquals(map.get(id1).getRoleLevel(), RoleLevel.PROJECT);
        assertEquals(map.get(id1).getProject().getIdentifier().getIdentifier(), "/TEST-SPACE/TEST-PROJECT");
        assertEquals(map.get(id1).getProject().getCode(), "TEST-PROJECT");
        assertEquals(map.get(id1).getProject().getSpace().getCode(), "TEST-SPACE");
        assertEquals(map.get(id2).getRole(), Role.OBSERVER);
        assertEquals(map.get(id2).getRoleLevel(), RoleLevel.SPACE);
        assertEquals(map.get(id2).getSpace().getCode(), "TESTGROUP");
    }
}
