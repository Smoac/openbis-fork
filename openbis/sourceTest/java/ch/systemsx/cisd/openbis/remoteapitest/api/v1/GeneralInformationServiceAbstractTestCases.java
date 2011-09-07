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

package ch.systemsx.cisd.openbis.remoteapitest.api.v1;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.utilities.ToStringComparator;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;

/**
 * This class contains a set of generic tests cases for {@link IGeneralInformationService}. The test
 * cases require an instance of {@link IGeneralInformationService} to be injected from an external
 * source and thus cannot run on their own.
 * 
 * @author Franz-Josef Elmer
 * @author Kaloyan Enimanev
 */
// exclude from running directly as TestNG test.
@Test(groups =
    { "abstract", "broken" })
public class GeneralInformationServiceAbstractTestCases extends AssertJUnit
{
    /**
     * Helps delaying the initialization of the {@link IGeneralInformationService} field until the
     * necessary infrastructure has been bootstrapped.
     */
    public static interface IGeneralInformationServiceFactory
    {
        IGeneralInformationService createService();
    }

    private final IGeneralInformationServiceFactory serviceHolder;

    private IGeneralInformationService generalInformationService;

    private String sessionToken;

    public GeneralInformationServiceAbstractTestCases(
            IGeneralInformationServiceFactory serviceHolder)
    {
        this.serviceHolder = serviceHolder;
    }

    @BeforeMethod
    public void beforeMethod() throws MalformedURLException
    {
        generalInformationService = serviceHolder.createService();
        sessionToken = generalInformationService.tryToAuthenticateForAllServices("test", "a");
    }

    @AfterMethod
    public void afterMethod() throws MalformedURLException
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
    public void testListProjects()
    {
        List<Project> result = generalInformationService.listProjects(sessionToken);
        assertEquals(true, result.size() > 0);
        String expectedSampleIdentifier = "/TESTGROUP/TESTPROJ";
        for (Project project : result)
        {
            if (expectedSampleIdentifier.equals(project.toString()))
            {
                return;
            }
        }
        fail("result didn't contain project " + expectedSampleIdentifier);
    }

    @Test
    public void testSearchForSamples()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*"));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(true, result.size() > 0);
        String expectedSampleIdentifier = "/CISD/CL1";
        for (Sample sample : result)
        {
            if (expectedSampleIdentifier.equals(sample.getIdentifier()))
            {
                return;
            }
        }
        fail("result didn't contain sample " + expectedSampleIdentifier);
    }

    @Test(dependsOnMethods = "testSearchForSamples")
    public void testSearchForSamplesWithNoCriterion()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*"));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);

        // if no criterion was specified all samples should be returned as well
        SearchCriteria scWithNoCriterion = new SearchCriteria();
        List<Sample> resultWithNoCriterion =
                generalInformationService.searchForSamples(sessionToken, scWithNoCriterion);
        assertEquals(result, resultWithNoCriterion);
    }

    @Test
    public void testSearchForSamplesByProperty()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createPropertyMatch("ORGANISM", "HUMAN"));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchForSamplesByExperimentCode()
    {
        // Search for Samples with only experiment's code limiting the results
        SearchCriteria sc = new SearchCriteria();
        SearchCriteria ec = new SearchCriteria();
        ec.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "EXP-TEST-1"));
        sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(ec));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(1, result.size());
        assertEquals("/CISD/NEMO/EXP-TEST-1", result.get(0).getExperimentIdentifierOrNull());
    }

    @Test
    public void testSearchForSamplesByExperimentProperty()
    {
        // Search for Samples with only experiment's property limiting the results
        SearchCriteria sc = new SearchCriteria();
        SearchCriteria ec = new SearchCriteria();
        ec.addMatchClause(MatchClause.createPropertyMatch("DESCRIPTION", "A simple experiment"));
        sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(ec));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(2, result.size());
    }

    @Test
    public void testSearchForSamplesByParentCode()
    {
        // Search for Samples with only parent's code limiting the results
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*"));
        SearchCriteria pc = new SearchCriteria();
        pc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "MP002-1"));
        sc.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(pc));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(2, result.size());
    }

    @Test
    public void testSearchForSamplesByChildCode()
    {
        // Search for Samples with only child's code limiting the results
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*"));
        SearchCriteria cc = new SearchCriteria();
        cc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "3VCP*"));
        sc.addSubCriteria(SearchSubCriteria.createSampleChildCriteria(cc));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(2, result.size());
    }

    @Test
    public void testSearchForSamplesByContainerCode()
    {
        // Search for Samples with only container's code limiting the results
        SearchCriteria sc = new SearchCriteria();
        SearchCriteria cc = new SearchCriteria();
        cc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "CL1"));
        sc.addSubCriteria(SearchSubCriteria.createSampleContainerCriteria(cc));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(2, result.size());
        for (Sample s : result)
        {
            assertTrue(s.getCode() + "doesn't start with 'CL1:'", s.getCode().startsWith("CL1:"));
        }
    }

    @Test
    public void testSearchForSamplesByExperimentAndParentCode()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(true, result.size() > 600);
        // Add experiment criteria limiting results to 7
        SearchCriteria ec = new SearchCriteria();
        ec.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "EXP-REUSE"));
        sc.addSubCriteria(SearchSubCriteria.createExperimentCriteria(ec));
        List<Sample> result2 = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(7, result2.size());
        // Add parent criteria limiting results to only 2
        SearchCriteria pc = new SearchCriteria();
        pc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "DP1-A"));
        sc.addSubCriteria(SearchSubCriteria.createSampleParentCriteria(pc));
        List<Sample> result3 = generalInformationService.searchForSamples(sessionToken, sc);
        assertEquals(2, result3.size());
    }

    @Test
    public void testListDataSetsForAllSamples()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        List<Sample> samples = generalInformationService.searchForSamples(sessionToken, sc);
        List<DataSet> result = generalInformationService.listDataSets(sessionToken, samples);
        assertEquals(true, result.size() > 0);
    }

    @Test
    public void testListDataSetsWithParentsForAllSamples()
    {
        // Search for Samples first
        SearchCriteria sc = new SearchCriteria();
        List<Sample> samples = generalInformationService.searchForSamples(sessionToken, sc);
        List<DataSet> result =
                generalInformationService.listDataSets(sessionToken, samples,
                        EnumSet.of(Connections.PARENTS));
        assertEquals(true, result.size() > 0);
        // See if some sample has parents
        boolean parentCodesFound = false;
        for (DataSet dataSet : result)
        {
            if (false == dataSet.getParentCodes().isEmpty())
            {
                parentCodesFound = true;
                break;
            }
        }

        assertTrue("No parent codes should have been found", (false == parentCodesFound));
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

    @Test
    public void testListExperimentsByIdentifier()
    {
        List<SpaceWithProjectsAndRoleAssignments> spaces =
                generalInformationService.listSpacesWithProjectsAndRoleAssignments(sessionToken,
                        null);
        ArrayList<Project> projects = new ArrayList<Project>();
        for (SpaceWithProjectsAndRoleAssignments space : spaces)
        {
            projects.addAll(space.getProjects());
        }
        List<Experiment> experimentsByProject =
                generalInformationService.listExperiments(sessionToken, projects, "SIRNA_HCS");
        assertEquals(true, experimentsByProject.size() > 0);

        List<String> experimentIdentifiers = new ArrayList<String>();
        for (Experiment exp : experimentsByProject)
        {
            experimentIdentifiers.add(exp.getIdentifier());
        }

        List<Experiment> results =
                generalInformationService.listExperiments(sessionToken, experimentIdentifiers);
        Comparator<Experiment> experimentCompare = new Comparator<Experiment>()
            {

                public int compare(Experiment o1, Experiment o2)
                {
                    return o1.getIdentifier().compareTo(o2.getIdentifier());
                }
            };
        Collections.sort(experimentsByProject, experimentCompare);
        Collections.sort(results, experimentCompare);
        assertEquals(experimentsByProject, results);
    }

    @Test
    public void testListDataSetTypes()
    {
        List<DataSetType> dataSetTypes = generalInformationService.listDataSetTypes(sessionToken);
        assertEquals(4, dataSetTypes.size());

        Collections.sort(dataSetTypes, new ToStringComparator());
        DataSetType dataSetType = dataSetTypes.get(1);
        assertEquals("HCS_IMAGE", dataSetType.getCode());

        List<PropertyTypeGroup> propertyTypeGroups = dataSetType.getPropertyTypeGroups();
        assertEquals(1, propertyTypeGroups.size());

        PropertyTypeGroup propertyTypeGroup = propertyTypeGroups.get(0);
        assertEquals(null, propertyTypeGroup.getName());

        List<PropertyType> propertyTypes = propertyTypeGroup.getPropertyTypes();
        assertEquals(4, propertyTypes.size());

        PropertyType propertyType;
        propertyType = propertyTypes.get(0);
        assertEquals("COMMENT", propertyType.getCode());
        assertEquals("Comment", propertyType.getLabel());
        assertEquals("Any other comments", propertyType.getDescription());

        propertyType = propertyTypes.get(1);
        assertEquals("ANY_MATERIAL", propertyType.getCode());
        assertEquals("any_material", propertyType.getLabel());
        assertEquals("any_material", propertyType.getDescription());
    }

    @Test
    public void testSearchForDataSetsByCode()
    {
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE, "*"));
        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, sc);
        assertTrue(result.size() > 0);
        String expectedDataSetCode = "20081105092159188-3";
        for (DataSet dataSet : result)
        {
            if (expectedDataSetCode.equals(dataSet.getCode()))
            {
                return;
            }
        }
        fail("result didn't contain data set" + expectedDataSetCode);
    }

    @Test
    public void testSearchForDataSetsByExperiments()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        SearchCriteria expCriteria = new SearchCriteria();
        expCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "EXP-TEST-1"));
        searchCriteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(expCriteria));
        List<DataSet> result =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(1, result.size());

        DataSet dataSet = result.get(0);
        assertEquals("20081105092159111-1", dataSet.getCode());
        assertEquals("/CISD/NEMO/EXP-TEST-1", dataSet.getExperimentIdentifier());

    }

    @Test
    public void testSearchForDataSetsBySamples()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        SearchCriteria sampleCriteria = new SearchCriteria();
        sampleCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "CP-TEST-1"));
        searchCriteria.addSubCriteria(SearchSubCriteria.createSampleCriteria(sampleCriteria));
        List<DataSet> result =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(1, result.size());

        DataSet dataSet = result.get(0);
        assertEquals("20081105092159111-1", dataSet.getCode());
        assertEquals("/CISD/NEMO/EXP-TEST-1", dataSet.getExperimentIdentifier());
        assertEquals("/CISD/CP-TEST-1", dataSet.getSampleIdentifierOrNull());
    }

    @Test
    public void testSearchForDataSetsByParent()
    {
        SearchCriteria searchCriteria = new SearchCriteria();

        SearchCriteria parentCriteria = new SearchCriteria();
        parentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "20081105092159333-3"));
        searchCriteria
                .addSubCriteria(SearchSubCriteria.createDataSetParentCriteria(parentCriteria));

        List<DataSet> result =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);

        assertEquals(2, result.size());
        assertEquals(
                "[DataSet[20081105092259000-8,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=no comment}], "
                        + "DataSet[20081105092259000-9,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=no comment}]]",
                result.toString());
    }

    @Test
    public void testSearchForDataSetsByChild()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        SearchCriteria childCriteria = new SearchCriteria();
        childCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "20081105092259000-9"));
        searchCriteria.addSubCriteria(SearchSubCriteria.createDataSetChildCriteria(childCriteria));

        List<DataSet> result =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(3, result.size());
        assertEquals(
                "[DataSet[20081105092159111-1,/CISD/NEMO/EXP-TEST-1,/CISD/CP-TEST-1,HCS_IMAGE,{ANY_MATERIAL=null, BACTERIUM=null, COMMENT=no comment, GENDER=null}], "
                        + "DataSet[20081105092159222-2,/CISD/NOE/EXP-TEST-2,/CISD/CP-TEST-2,HCS_IMAGE,{COMMENT=no comment}], "
                        + "DataSet[20081105092159333-3,/CISD/NEMO/EXP-TEST-2,/CISD/CP-TEST-3,HCS_IMAGE,{COMMENT=no comment}]]",
                result.toString());
    }

    @Test
    public void testSearchForDataSetsByParentAndChild()
    {
        SearchCriteria searchCriteria = new SearchCriteria();

        SearchCriteria parentCriteria = new SearchCriteria();
        parentCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "20081105092159333-3"));
        searchCriteria
                .addSubCriteria(SearchSubCriteria.createDataSetParentCriteria(parentCriteria));

        SearchCriteria childCriteria = new SearchCriteria();
        childCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "20081105092259900-1"));
        searchCriteria.addSubCriteria(SearchSubCriteria.createDataSetChildCriteria(childCriteria));

        List<DataSet> result =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(1, result.size());
        assertEquals(
                "[DataSet[20081105092259000-9,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=no comment}]]",
                result.toString());
    }

    @Test
    public void testSearchForDataSetsByContainer()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        SearchCriteria containerCriteria = new SearchCriteria();

        containerCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.CODE, "20110509092359990-10"));
        searchCriteria.addSubCriteria(SearchSubCriteria
                .createDataSetContainerCriteria(containerCriteria));

        List<DataSet> result =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(2, result.size());
        assertEquals(
                "[DataSet[20110509092359990-11,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=non-virtual comment}], "
                        + "DataSet[20110509092359990-12,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=non-virtual comment}]]",
                result.toString());
    }

}
