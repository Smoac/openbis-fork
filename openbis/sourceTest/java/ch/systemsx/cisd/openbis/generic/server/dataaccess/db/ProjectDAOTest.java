/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collections;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;

/**
 * Test cases for {@link ProjectDAO}.
 * 
 * @author Izabela Adamczyk
 */
@Test(groups =
    { "db", "project" })
public class ProjectDAOTest extends AbstractDAOTest
{

    private static final String DESCRIPTION_NEW_PROJECT = "New project.";

    private static final String NONEXISTENT = "nonexistent";

    public static final String NOE = "NOE";

    public static final String TESTPROJ = "TESTPROJ";

    public static final String NEMO = "NEMO";

    public static final String DEFAULT = "DEFAULT";

    static final String[] PRELOADED_PROJECTS =
        { DEFAULT, NEMO, NOE, TESTPROJ };

    @Test
    public void testListProjects()
    {
        final List<ProjectPE> projects = daoFactory.getProjectDAO().listProjects();
        Collections.sort(projects);
        for (int i = 0; i < projects.size(); i++)
        {
            assertEquals(PRELOADED_PROJECTS[i], projects.get(i).getCode());
        }
        assertEquals(4, projects.size());
    }

    @Test(dependsOnMethods = "testListProjects")
    public void testListProjectsFromGroup()
    {
        final List<ProjectPE> allProjects = daoFactory.getProjectDAO().listProjects();
        Collections.sort(allProjects);
        final ProjectPE defaultProject = allProjects.get(0);
        assertEquals(DEFAULT, defaultProject.getCode());
        final List<ProjectPE> groupProjects =
                daoFactory.getProjectDAO().listProjects(defaultProject.getGroup());
        assertEquals(3, groupProjects.size());
        Collections.sort(groupProjects);
        assertEquals(DEFAULT, groupProjects.get(0).getCode());
        assertEquals(NEMO, groupProjects.get(1).getCode());
        assertEquals(NOE, groupProjects.get(2).getCode());
    }

    @Test(dependsOnMethods = "testListProjects")
    public void testListProjectsFromAnotherGroup()
    {
        final List<ProjectPE> allProjects = daoFactory.getProjectDAO().listProjects();
        Collections.sort(allProjects);
        final ProjectPE testProject = allProjects.get(3);
        assertEquals(testProject.getCode(), TESTPROJ);
        final List<ProjectPE> groupProjects =
                daoFactory.getProjectDAO().listProjects(testProject.getGroup());
        assertEquals(1, groupProjects.size());
        Collections.sort(groupProjects);
        assertEquals(groupProjects.get(0).getCode(), TESTPROJ);
    }

    @Test
    public void testTryFindProject() throws Exception
    {
        final List<ProjectPE> allProjects = daoFactory.getProjectDAO().listProjects();
        Collections.sort(allProjects);
        final ProjectPE templateProject = allProjects.get(3);
        assertEquals(templateProject.getCode(), TESTPROJ);

        ProjectPE found =
                daoFactory.getProjectDAO().tryFindProject(
                        templateProject.getGroup().getDatabaseInstance().getCode(),
                        templateProject.getGroup().getCode(), templateProject.getCode());
        assertEquals(TESTPROJ, found.getCode());
        assertEquals(templateProject.getGroup().getCode(), found.getGroup().getCode());
        assertEquals(templateProject.getGroup().getDatabaseInstance().getCode(), found.getGroup()
                .getDatabaseInstance().getCode());
    }

    @Test
    public void testTryFindProjectNonexistent() throws Exception
    {
        final List<ProjectPE> allProjects = daoFactory.getProjectDAO().listProjects();
        Collections.sort(allProjects);
        final ProjectPE templateProject = allProjects.get(3);
        assertEquals(templateProject.getCode(), TESTPROJ);

        AssertJUnit.assertNull(daoFactory.getProjectDAO().tryFindProject(
                templateProject.getGroup().getDatabaseInstance().getCode(),
                templateProject.getGroup().getCode(), NONEXISTENT));

        AssertJUnit.assertNull(daoFactory.getProjectDAO().tryFindProject(
                templateProject.getGroup().getDatabaseInstance().getCode(), NONEXISTENT,
                templateProject.getCode()));

        AssertJUnit.assertNull(daoFactory.getProjectDAO().tryFindProject(NONEXISTENT,
                templateProject.getGroup().getCode(), templateProject.getCode()));
    }

    @Test
    public void testCreateProject() throws Exception
    {
        final List<ProjectPE> allProjects = daoFactory.getProjectDAO().listProjects();
        Collections.sort(allProjects);
        final ProjectPE templateProject = allProjects.get(3);
        assertEquals(templateProject.getCode(), TESTPROJ);
        AssertJUnit.assertNull(daoFactory.getProjectDAO().tryFindProject(
                templateProject.getGroup().getDatabaseInstance().getCode(),
                templateProject.getGroup().getCode(), NONEXISTENT));
        final ProjectPE newProject =
                prepareProject(templateProject.getGroup(), NONEXISTENT, DESCRIPTION_NEW_PROJECT,
                        getSystemPerson());
        daoFactory.getProjectDAO().createProject(newProject);
        final ProjectPE registeredProject =
                daoFactory.getProjectDAO().tryFindProject(
                        templateProject.getGroup().getDatabaseInstance().getCode(),
                        templateProject.getGroup().getCode(), NONEXISTENT);
        AssertJUnit.assertNotNull(registeredProject);
        assertEquals(registeredProject.getDescription(), DESCRIPTION_NEW_PROJECT);
        assertEquals(registeredProject.getGroup(), templateProject.getGroup());
        assertEquals(registeredProject.getProjectLeader(), getSystemPerson());
    }

    private ProjectPE prepareProject(GroupPE group, String code, String description, PersonPE leader)
    {
        final ProjectPE result = new ProjectPE();
        result.setCode(code);
        result.setDescription(description);
        result.setGroup(group);
        result.setProjectLeader(leader);
        final PersonPE systemPerson = getSystemPerson();
        result.setRegistrator(systemPerson);
        return result;
    }

}
