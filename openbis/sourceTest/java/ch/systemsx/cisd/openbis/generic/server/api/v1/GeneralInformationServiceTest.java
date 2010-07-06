/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.generic.shared.AbstractServerTestCase;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;

/**
 * @author Franz-Josef Elmer
 */
// PLEASE, if you add here a new test add also a system test to
// ch.systemsx.cisd.openbis.systemtest.api.v1.GeneralInformationService
@Friend(toClasses=RoleAssignmentPE.class)
public class GeneralInformationServiceTest extends AbstractServerTestCase
{
    private GeneralInformationService service;

    @Override
    @BeforeMethod
    public final void setUp()
    {
        super.setUp();
        service = new GeneralInformationService(sessionManager, daoFactory);
    }

    @Test
    public void testListNamedRoleSets()
    {
        prepareGetSession();

        Map<String, Set<Role>> namedRoleSets = service.listNamedRoleSets(SESSION_TOKEN);

        List<Entry<String, Set<Role>>> entries =
                new ArrayList<Entry<String, Set<Role>>>(namedRoleSets.entrySet());
        Collections.sort(entries, new Comparator<Entry<String, Set<Role>>>()
            {
                public int compare(Entry<String, Set<Role>> e1, Entry<String, Set<Role>> e2)
                {
                    return e1.getKey().compareTo(e2.getKey());
                }
            });
        assertNamedRoles("ETL_SERVER", "[ADMIN(instance), "
                + "ETL_SERVER(instance), ETL_SERVER(space)]", entries.get(0));
        assertNamedRoles("INSTANCE_ADMIN", "[ADMIN(instance)]", entries.get(1));
        assertNamedRoles("INSTANCE_ADMIN_OBSERVER", "[ADMIN(instance), OBSERVER(instance)]",
                entries.get(2));
        assertNamedRoles("NONE", "[]", entries.get(3));
        assertNamedRoles("OBSERVER", "[ADMIN(instance), ADMIN(space), OBSERVER(space), "
                + "POWER_USER(space), USER(space)]", entries.get(4));
        assertNamedRoles("POWER_USER", "[ADMIN(instance), ADMIN(space), POWER_USER(space)]",
                entries.get(5));
        assertNamedRoles("SPACE_ADMIN", "[ADMIN(instance), ADMIN(space)]", entries.get(6));
        assertNamedRoles("USER", "[ADMIN(instance), ADMIN(space), POWER_USER(space), USER(space)]",
                entries.get(7));
        assertEquals(8, entries.size());
        context.assertIsSatisfied();
    }

    private void assertNamedRoles(String expectedName, String expectedRoles,
            Entry<String, Set<Role>> entry)
    {
        assertEquals(expectedName, entry.getKey());
        List<Role> roles = new ArrayList<Role>(entry.getValue());
        Collections.sort(roles, new Comparator<Role>()
            {
                public int compare(Role r1, Role r2)
                {
                    return r1.toString().compareTo(r2.toString());
                }
            });
        assertEquals(expectedRoles, roles.toString());
    }

    @Test
    public void testListSpacesWithProjectsAndRoleAssignments()
    {
        prepareGetSession();
        context.checking(new Expectations()
            {
                {
                    one(roleAssignmentDAO).listRoleAssignments();
                    RoleAssignmentPE assignment1 = createUserAssignment("user1", null, RoleCode.ADMIN);
                    RoleAssignmentPE assignment2 = createUserAssignment("user2", "s2", RoleCode.OBSERVER);
                    RoleAssignmentPE assignment3 = createUserAssignment("user1", "s1", RoleCode.USER);
                    will(returnValue(Arrays.asList(assignment1, assignment2, assignment3)));
                    
                    one(groupDAO).listGroups(daoFactory.getHomeDatabaseInstance());
                    List<GroupPE> spaces = createSpaces("s1", "s2", "s3");
                    will(returnValue(spaces));
                    
                    one(projectDAO).listProjects(spaces.get(0));
                    ProjectPE a = new ProjectPE();
                    a.setCode("a");
                    a.setGroup(spaces.get(0));
                    ProjectPE b = new ProjectPE();
                    b.setCode("b");
                    b.setGroup(spaces.get(0));
                    will(returnValue(Arrays.asList(a, b)));
                    
                    one(projectDAO).listProjects(spaces.get(1));
                    will(returnValue(Arrays.asList()));
                    
                    one(projectDAO).listProjects(spaces.get(2));
                    ProjectPE c = new ProjectPE();
                    c.setCode("c");
                    c.setGroup(spaces.get(0));
                    will(returnValue(Arrays.asList(c)));
                }
            });

        List<SpaceWithProjectsAndRoleAssignments> spaces =
                service.listSpacesWithProjectsAndRoleAssignments(SESSION_TOKEN, null);
        
        assertSpaceAndProjects("s1", "[/s1/a, /s1/b]", spaces.get(0));
        assertRoles("[]", spaces.get(0).getRoles("unknown user"));
        assertRoles("[ADMIN(instance), USER(space)]", spaces.get(0).getRoles("user1"));
        assertRoles("[]", spaces.get(0).getRoles("user2"));
        
        assertSpaceAndProjects("s2", "[]", spaces.get(1));
        assertRoles("[ADMIN(instance)]", spaces.get(1).getRoles("user1"));
        assertRoles("[OBSERVER(space)]", spaces.get(1).getRoles("user2"));
        
        assertSpaceAndProjects("s3", "[/s3/c]", spaces.get(2));
        assertRoles("[ADMIN(instance)]", spaces.get(2).getRoles("user1"));
        assertRoles("[]", spaces.get(2).getRoles("user2"));
        
        assertEquals(3, spaces.size());
        context.assertIsSatisfied();
    }

    private void assertSpaceAndProjects(String expectedSpaceCode, String expectedProjects,
            SpaceWithProjectsAndRoleAssignments space)
    {
        assertEquals(expectedSpaceCode, space.getCode());
        List<Project> projects = space.getProjects();
        Collections.sort(projects, new Comparator<Project>()
            {
                public int compare(Project p1, Project p2)
                {
                    return p1.toString().compareTo(p2.toString());
                }
            });
        assertEquals(expectedProjects, projects.toString());
    }

    private void assertRoles(String expectedRoles, Set<Role> roles)
    {
        List<Role> list = new ArrayList<Role>(roles);
        Collections.sort(list, new Comparator<Role>()
            {
                public int compare(Role r1, Role r2)
                {
                    return r1.toString().compareTo(r2.toString());
                }
            });
        assertEquals(expectedRoles, list.toString());
    }

    private RoleAssignmentPE createUserAssignment(String userID, String spaceCodeOrNull,
            RoleCode roleCode)
    {
        RoleAssignmentPE assignment = new RoleAssignmentPE();
        if (spaceCodeOrNull != null)
        {
        assignment.setGroup(createGroup(spaceCodeOrNull));
        }
        assignment.setRole(roleCode);
        PersonPE person = new PersonPE();
        person.setUserId(userID);
        assignment.setPersonInternal(person);
        return assignment;
    }
    
    private List<GroupPE> createSpaces(String... codes)
    {
        List<GroupPE> list = new ArrayList<GroupPE>();
        for (String code : codes)
        {
            list.add(createGroup(code));
        }
        return list;
    }
    
}
