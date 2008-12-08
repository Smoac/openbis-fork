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

import org.testng.annotations.Test;

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

    public static final String NOE = "NOE";

    public static final String TESTPROJ = "TESTPROJ";

    public static final String NEMO = "NEMO";

    public static final String DEFAULT = "DEFAULT";

    static final String[] PRELOADED_PROJECTS =
        { DEFAULT, NEMO, NOE, TESTPROJ };

    @Test
    public void testListProjects() throws Exception
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
    public void testListProjectsFromGroup() throws Exception
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
    public void testListProjectsFromAnotherGroup() throws Exception
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

}
