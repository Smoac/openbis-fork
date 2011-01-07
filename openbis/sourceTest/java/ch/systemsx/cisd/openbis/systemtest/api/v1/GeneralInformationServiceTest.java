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

package ch.systemsx.cisd.openbis.systemtest.api.v1;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.systemtest.SystemTestCase;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class GeneralInformationServiceTest extends SystemTestCase
{
    @Autowired
    private IGeneralInformationService generalInformationService;

    private String sessionToken;

    @BeforeMethod
    public void beforeMethod()
    {
        sessionToken = generalInformationService.tryToAuthenticateForAllServices("test", "a");
    }

    @AfterMethod
    public void afterMethod()
    {
        generalInformationService.logout(sessionToken);
    }

    @Test
    public void testListNamedRoleSets()
    {
        Map<String, Set<Role>> namedRoleSets =
                generalInformationService.listNamedRoleSets(sessionToken);

        assertEquals("[ADMIN(instance), ADMIN(space)]", namedRoleSets.get("SPACE_ADMIN").toString());
    }

    @Test
    public void testListSpacesWithProjectsAndRoleAssignments()
    {
        List<SpaceWithProjectsAndRoleAssignments> spaces =
                generalInformationService.listSpacesWithProjectsAndRoleAssignments(sessionToken,
                        null);

        Collections.sort(spaces, new Comparator<SpaceWithProjectsAndRoleAssignments>()
            {
                public int compare(SpaceWithProjectsAndRoleAssignments s1,
                        SpaceWithProjectsAndRoleAssignments s2)
                {
                    return s1.getCode().compareTo(s2.getCode());
                }
            });
        checkSpace("CISD", "[/CISD/DEFAULT, /CISD/NEMO, /CISD/NOE]",
                "[ADMIN(instance), ADMIN(space), ETL_SERVER(instance), ETL_SERVER(space)]",
                spaces.get(0));
        checkSpace("TESTGROUP", "[/TESTGROUP/TESTPROJ]",
                "[ADMIN(instance), ADMIN(space), ETL_SERVER(instance)]", spaces.get(1));
        assertEquals(2, spaces.size());
    }

    @Test
    public void testSearchForSamples()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*"));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(true, result.size() > 0);
        String expectedSampleIdentifier = "CISD:/CISD/CL1";
        for (Sample sample : result)
        {
            if (expectedSampleIdentifier.equals(sample.getIdentifier()))
            {
                return;
            }
        }
        fail("result didn't contain sample " + expectedSampleIdentifier);
    }

    @Test
    public void testSearchForSamplesByProperty()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createPropertyMatch("ORGANISM", "HUMAN"));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(true, result.size() > 0);
    }

    @Test
    public void testListDataSetsForAllSamples()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*"));
        List<Sample> samples = generalInformationService.searchForSamples(sessionToken, sc);
        List<DataSet> result = generalInformationService.listDataSets(sessionToken, samples);
        assertEquals(true, result.size() > 0);
    }

    @Test
    public void testListDataSetsForSample()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createPropertyMatch("ORGANISM", "HUMAN"));
        List<Sample> samples = generalInformationService.searchForSamples(sessionToken, sc);
        List<DataSet> result =
                generalInformationService.listDataSetsForSample(sessionToken, samples.get(0), true);
        assertEquals(true, result.size() > 0);
    }

    @Test
    public void testListDataSetsForEmptySampleList()
    {
        List<Sample> samples = new ArrayList<Sample>();
        List<DataSet> result = generalInformationService.listDataSets(sessionToken, samples);
        assertEquals(true, result.size() == 0);
    }

    private static final String DEFAULT_PLATE_GEOMETRY_VALUE = "384_WELLS_16X24";

    @Test
    public void testSearchForDataSetsByProperty()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createPropertyMatch("$PLATE_GEOMETRY",
                DEFAULT_PLATE_GEOMETRY_VALUE));
        List<Sample> samples = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(true, samples.size() > 0);
        List<DataSet> result = generalInformationService.listDataSets(sessionToken, samples);
        assertEquals(true, result.size() == 0);
    }

    private void checkSpace(String expectedCode, String expectedProjects, String expectedRoles,
            SpaceWithProjectsAndRoleAssignments space)
    {
        assertEquals(expectedCode, space.getCode());
        List<Project> projects = space.getProjects();
        Collections.sort(projects, new Comparator<Project>()
            {
                public int compare(Project p1, Project p2)
                {
                    return p1.getCode().compareTo(p2.getCode());
                }
            });
        assertEquals(expectedProjects, projects.toString());
        List<Role> roles = new ArrayList<Role>(space.getRoles("test"));
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
    public void testListExperiments()
    {
        List<SpaceWithProjectsAndRoleAssignments> spaces =
                generalInformationService.listSpacesWithProjectsAndRoleAssignments(sessionToken,
                        null);
        ArrayList<Project> projects = new ArrayList<Project>();
        for (SpaceWithProjectsAndRoleAssignments space : spaces)
        {
            projects.addAll(space.getProjects());
        }
        List<Experiment> result =
                generalInformationService.listExperiments(sessionToken, projects, "SIRNA_HCS");
        assertEquals(true, result.size() > 0);
        Experiment resultExperiment = result.get(0);
        boolean identifierIsOk = "/CISD/DEFAULT/EXP-REUSE".equals(resultExperiment.getIdentifier());
        identifierIsOk |= "/CISD/NEMO/EXP-TEST-2".equals(resultExperiment.getIdentifier());
        assertEquals("Experiment should be: " + resultExperiment.getIdentifier(), true,
                identifierIsOk);
    }
}
