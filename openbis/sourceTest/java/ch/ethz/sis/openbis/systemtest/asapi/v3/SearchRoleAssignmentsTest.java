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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.search.RoleAssignmentSearchCriteria;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SearchRoleAssignmentsTest extends AbstractTest
{
    @Test
    public void testSearchForAll()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentSearchCriteria searchCriteria = new RoleAssignmentSearchCriteria();
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withUser();
        fetchOptions.withAuthorizationGroup();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        
        // When
        List<RoleAssignment> assignments = v3api.searchRoleAssignments(sessionToken, searchCriteria, fetchOptions).getObjects();
        
        // Then
        assertRoleAssignments(assignments, "ADMIN INSTANCE for user instance_admin\n"
                + "ADMIN INSTANCE for user test\n"
                + "ADMIN PROJECT[/TEST-SPACE/PROJECT-TO-DELETE] for user test_project_pa_off\n"
                + "ADMIN PROJECT[/TEST-SPACE/PROJECT-TO-DELETE] for user test_project_pa_on\n"
                + "ADMIN PROJECT[/TEST-SPACE/TEST-PROJECT] for user test_project_pa_off\n"
                + "ADMIN PROJECT[/TEST-SPACE/TEST-PROJECT] for user test_project_pa_on\n"
                + "ADMIN SPACE[CISD] for user test\n"
                + "ADMIN SPACE[CISD] for user test_v3\n"
                + "ADMIN SPACE[TEST-SPACE] for user test_space\n"
                + "ADMIN SPACE[TEST-SPACE] for user test_space_pa_off\n"
                + "ADMIN SPACE[TEST-SPACE] for user test_space_pa_on\n"
                + "ADMIN SPACE[TESTGROUP] for group AGROUP\n"
                + "ADMIN SPACE[TESTGROUP] for user admin\n"
                + "ADMIN SPACE[TESTGROUP] for user homeless\n"
                + "ADMIN SPACE[TESTGROUP] for user observer_cisd\n"
                + "ADMIN SPACE[TESTGROUP] for user test\n"
                + "ETL_SERVER INSTANCE for user etlserver\n"
                + "ETL_SERVER INSTANCE for user test\n"
                + "ETL_SERVER SPACE[CISD] for user test\n"
                + "ETL_SERVER SPACE[TEST-SPACE] for user test_space_etl_server\n"
                + "ETL_SERVER SPACE[TESTGROUP] for user test_group_etl_server\n"
                + "OBSERVER INSTANCE for user instance_observer\n"
                + "OBSERVER SPACE[CISD] for user observer_cisd\n"
                + "OBSERVER SPACE[TEST-SPACE] for user test_v3\n"
                + "OBSERVER SPACE[TESTGROUP] for user observer\n"
                + "POWER_USER SPACE[CISD] for user homeless\n"
                + "POWER_USER SPACE[CISD] for user test_role\n"
                + "POWER_USER SPACE[TESTGROUP] for user poweruser\n"
                + "USER PROJECT[/CISD/DEFAULT] for group AGROUP\n");
    }
    
    @Test
    public void testSearchForAllProjects()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentSearchCriteria searchCriteria = new RoleAssignmentSearchCriteria();
        searchCriteria.withProject();
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withUser();
        fetchOptions.withAuthorizationGroup();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        
        // When
        List<RoleAssignment> assignments = v3api.searchRoleAssignments(sessionToken, searchCriteria, fetchOptions).getObjects();
        
        // Then
        assertRoleAssignments(assignments, "ADMIN PROJECT[/TEST-SPACE/PROJECT-TO-DELETE] for user test_project_pa_off\n"
                + "ADMIN PROJECT[/TEST-SPACE/PROJECT-TO-DELETE] for user test_project_pa_on\n"
                + "ADMIN PROJECT[/TEST-SPACE/TEST-PROJECT] for user test_project_pa_off\n"
                + "ADMIN PROJECT[/TEST-SPACE/TEST-PROJECT] for user test_project_pa_on\n"
                + "USER PROJECT[/CISD/DEFAULT] for group AGROUP\n");
    }
    
    @Test
    public void testSearchForAllSpace()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentSearchCriteria searchCriteria = new RoleAssignmentSearchCriteria();
        searchCriteria.withSpace();
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withUser();
        fetchOptions.withAuthorizationGroup();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        
        // When
        List<RoleAssignment> assignments = v3api.searchRoleAssignments(sessionToken, searchCriteria, fetchOptions).getObjects();
        
        // Then
        assertRoleAssignments(assignments, "ADMIN SPACE[CISD] for user test\n"
                + "ADMIN SPACE[CISD] for user test_v3\n"
                + "ADMIN SPACE[TEST-SPACE] for user test_space\n"
                + "ADMIN SPACE[TEST-SPACE] for user test_space_pa_off\n"
                + "ADMIN SPACE[TEST-SPACE] for user test_space_pa_on\n"
                + "ADMIN SPACE[TESTGROUP] for group AGROUP\n"
                + "ADMIN SPACE[TESTGROUP] for user admin\n"
                + "ADMIN SPACE[TESTGROUP] for user homeless\n"
                + "ADMIN SPACE[TESTGROUP] for user observer_cisd\n"
                + "ADMIN SPACE[TESTGROUP] for user test\n"
                + "ETL_SERVER SPACE[CISD] for user test\n"
                + "ETL_SERVER SPACE[TEST-SPACE] for user test_space_etl_server\n"
                + "ETL_SERVER SPACE[TESTGROUP] for user test_group_etl_server\n"
                + "OBSERVER SPACE[CISD] for user observer_cisd\n"
                + "OBSERVER SPACE[TEST-SPACE] for user test_v3\n"
                + "OBSERVER SPACE[TESTGROUP] for user observer\n"
                + "POWER_USER SPACE[CISD] for user homeless\n"
                + "POWER_USER SPACE[CISD] for user test_role\n"
                + "POWER_USER SPACE[TESTGROUP] for user poweruser\n");
    }
    
    @Test
    public void testSearchForAllAuthorizationGroups()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentSearchCriteria searchCriteria = new RoleAssignmentSearchCriteria();
        searchCriteria.withAuthorizationGroup();
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withUser();
        fetchOptions.withAuthorizationGroup();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        
        // When
        List<RoleAssignment> assignments = v3api.searchRoleAssignments(sessionToken, searchCriteria, fetchOptions).getObjects();
        
        // Then
        assertRoleAssignments(assignments, "ADMIN SPACE[TESTGROUP] for group AGROUP\n"
                + "USER PROJECT[/CISD/DEFAULT] for group AGROUP\n");
    }
    
    @Test
    public void testSearchForAllUsers()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentSearchCriteria searchCriteria = new RoleAssignmentSearchCriteria();
        searchCriteria.withUser();
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withUser();
        fetchOptions.withAuthorizationGroup();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        
        // When
        List<RoleAssignment> assignments = v3api.searchRoleAssignments(sessionToken, searchCriteria, fetchOptions).getObjects();
        
        // Then
        assertRoleAssignments(assignments, "ADMIN INSTANCE for user instance_admin\n"
                + "ADMIN INSTANCE for user test\n"
                + "ADMIN PROJECT[/TEST-SPACE/PROJECT-TO-DELETE] for user test_project_pa_off\n"
                + "ADMIN PROJECT[/TEST-SPACE/PROJECT-TO-DELETE] for user test_project_pa_on\n"
                + "ADMIN PROJECT[/TEST-SPACE/TEST-PROJECT] for user test_project_pa_off\n"
                + "ADMIN PROJECT[/TEST-SPACE/TEST-PROJECT] for user test_project_pa_on\n"
                + "ADMIN SPACE[CISD] for user test\n"
                + "ADMIN SPACE[CISD] for user test_v3\n"
                + "ADMIN SPACE[TEST-SPACE] for user test_space\n"
                + "ADMIN SPACE[TEST-SPACE] for user test_space_pa_off\n"
                + "ADMIN SPACE[TEST-SPACE] for user test_space_pa_on\n"
                + "ADMIN SPACE[TESTGROUP] for user admin\n"
                + "ADMIN SPACE[TESTGROUP] for user homeless\n"
                + "ADMIN SPACE[TESTGROUP] for user observer_cisd\n"
                + "ADMIN SPACE[TESTGROUP] for user test\n"
                + "ETL_SERVER INSTANCE for user etlserver\n"
                + "ETL_SERVER INSTANCE for user test\n"
                + "ETL_SERVER SPACE[CISD] for user test\n"
                + "ETL_SERVER SPACE[TEST-SPACE] for user test_space_etl_server\n"
                + "ETL_SERVER SPACE[TESTGROUP] for user test_group_etl_server\n"
                + "OBSERVER INSTANCE for user instance_observer\n"
                + "OBSERVER SPACE[CISD] for user observer_cisd\n"
                + "OBSERVER SPACE[TEST-SPACE] for user test_v3\n"
                + "OBSERVER SPACE[TESTGROUP] for user observer\n"
                + "POWER_USER SPACE[CISD] for user homeless\n"
                + "POWER_USER SPACE[CISD] for user test_role\n"
                + "POWER_USER SPACE[TESTGROUP] for user poweruser\n");
    }
    
    @Test
    public void testSearchForAUser()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentSearchCriteria searchCriteria = new RoleAssignmentSearchCriteria();
        searchCriteria.withUser().withUserId().thatEquals("test");
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withUser();
        fetchOptions.withAuthorizationGroup();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        
        // When
        List<RoleAssignment> assignments = v3api.searchRoleAssignments(sessionToken, searchCriteria, fetchOptions).getObjects();
        
        // Then
        assertRoleAssignments(assignments, "ADMIN INSTANCE for user test\n"
                + "ADMIN SPACE[CISD] for user test\n"
                + "ADMIN SPACE[TESTGROUP] for user test\n"
                + "ETL_SERVER INSTANCE for user test\n"
                + "ETL_SERVER SPACE[CISD] for user test\n");
    }
    
    @Test
    public void testSearchForSomeUsers()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentSearchCriteria searchCriteria = new RoleAssignmentSearchCriteria();
        searchCriteria.withUser().withUserIds().thatIn(Arrays.asList("test", "homeless"));
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withUser();
        fetchOptions.withAuthorizationGroup();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        
        // When
        List<RoleAssignment> assignments = v3api.searchRoleAssignments(sessionToken, searchCriteria, fetchOptions).getObjects();
        
        // Then
        assertRoleAssignments(assignments, "ADMIN INSTANCE for user test\n"
                + "ADMIN SPACE[CISD] for user test\n"
                + "ADMIN SPACE[TESTGROUP] for user homeless\n"
                + "ADMIN SPACE[TESTGROUP] for user test\n"
                + "ETL_SERVER INSTANCE for user test\n"
                + "ETL_SERVER SPACE[CISD] for user test\n"
                + "POWER_USER SPACE[CISD] for user homeless\n");
    }
    
    @Test
    public void testSearchForAUserAndASpace()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentSearchCriteria searchCriteria = new RoleAssignmentSearchCriteria();
        searchCriteria.withUser().withUserId().thatEquals("test");
        searchCriteria.withAndOperator();
        searchCriteria.withSpace().withCode().thatStartsWith("C");
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withUser();
        fetchOptions.withAuthorizationGroup();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        
        // When
        List<RoleAssignment> assignments = v3api.searchRoleAssignments(sessionToken, searchCriteria, fetchOptions).getObjects();
        
        // Then
        assertRoleAssignments(assignments, "ADMIN SPACE[CISD] for user test\n"
                + "ETL_SERVER SPACE[CISD] for user test\n");
    }
    
    private void assertRoleAssignments(List<RoleAssignment> assignments, String expectedAssignmentsAsString)
    {
        List<String> assignmentsAsStrings = new ArrayList<>();
        for (RoleAssignment roleAssignment : assignments)
        {
            String asString = asString(roleAssignment);
            if (asString.contains("EO_") == false) // other tests create new role assignments
            {
                assignmentsAsStrings.add(asString);
            }
        }
        Collections.sort(assignmentsAsStrings);
        StringBuilder builder = new StringBuilder();
        for (String line : assignmentsAsStrings)
        {
            builder.append(line).append("\n");
        }
        assertEquals(builder.toString(), expectedAssignmentsAsString);
    }
    
    private String asString(RoleAssignment assignment)
    {
        StringBuilder builder = new StringBuilder();
        RoleLevel roleLevel = assignment.getRoleLevel();
        builder.append(assignment.getRole()).append(" ").append(roleLevel);
        switch (roleLevel)
        {
            case SPACE:
                builder.append("[").append(assignment.getSpace().getCode()).append("]");
                break;
            case PROJECT:
                builder.append("[").append(assignment.getProject().getIdentifier()).append("]");
                break;
            case INSTANCE:
                break;
        }
        builder.append(" for ");
        AuthorizationGroup authorizationGroup = assignment.getAuthorizationGroup();
        if (authorizationGroup != null)
        {
            builder.append("group ").append(authorizationGroup.getCode());
        } else
        {
            builder.append("user ").append(assignment.getUser().getUserId());
        }
        return builder.toString();
    }
}
