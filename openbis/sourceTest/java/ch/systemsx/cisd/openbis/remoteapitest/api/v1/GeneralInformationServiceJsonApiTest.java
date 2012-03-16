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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
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
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Vocabulary;
import ch.systemsx.cisd.openbis.remoteapitest.RemoteApiTestCase;
import ch.systemsx.cisd.openbis.remoteapitest.api.v1.GeneralInformationServiceJsonApiTest.ContainsDataSetMatcher.CheckMode;

/**
 * Verifies that an instance of {@link IGeneralInformationService} is published via JSON-RPC and
 * that it is correctly functioning with external clients.
 * 
 * @author Kaloyan Enimanev
 */
@Test(groups =
    { "remote api" })
public class GeneralInformationServiceJsonApiTest extends RemoteApiTestCase
{
    protected IGeneralInformationService generalInformationService;

    protected String sessionToken;

    protected IGeneralInformationService createService()
    {
        return TestJsonServiceFactory.createGeneralInfoService();
    }

    @BeforeMethod
    public void beforeMethod() throws MalformedURLException
    {
        generalInformationService = createService();
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
        assertTrue(result.size() > 0);
        for (DataSet dataSet : result)
        {
            assertEquals(
                    "No parent codes should have been found for data set " + dataSet.getCode(),
                    "[]", dataSet.getParentCodes().toString());
        }
    }

    @Test
    public void testListDataSetsWithParentsForExperiment()
    {
        List<String> experimentIdentifiers = Arrays.asList("/CISD/NEMO/EXP1");
        List<Experiment> experiments =
                generalInformationService.listExperiments(sessionToken, experimentIdentifiers);
        List<DataSet> result =
                generalInformationService.listDataSetsForExperiments(sessionToken, experiments,
                        EnumSet.of(Connections.PARENTS));
        assertTrue(result.size() > 0);
        for (DataSet dataSet : result)
        {
            assertEquals(
                    "No parent codes should have been found for data set " + dataSet.getCode(),
                    "[]", dataSet.getParentCodes().toString());
        }

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
            System.out.println(dataSet.getRegistrationDetails().toString());
        }
        for (DataSet dataSet : result)
        {
            if (expectedDataSetCode.equals(dataSet.getCode()))
            {
                return;
            }
        }
        fail("result didn't contain data set" + expectedDataSetCode);
    }

    /**
     * reads date in format "y-M-d"
     */
    private Date getDate(String date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("y-M-d");
        try
        {
            return sdf.parse(date);
        } catch (ParseException ex)
        {
            throw new IllegalArgumentException(date);
        }
    }

    @DataProvider(name = "testSearchForDataSetsWithRegistrationDate")
    public Object[][] dataForDataSetRegistrationDateTest()
    {
        // datasets from 2008-11-05
        String[] a =
                new String[]
                    { "20081105092159188-3", "20081105092259000-8", "20081105092259000-9",
                            "20081105092259900-0", "20081105092259900-1", "20081105092359990-2",
                            "20081105092259000-18", "20081105092259000-19", "20081105092259000-20",
                            "20081105092259000-21" };
        // 2009-02-09
        String[] b =
                new String[]
                    { "20110805092359990-17", "20081105092159111-1", "20081105092159222-2",
                            "20081105092159333-3" };
        // 2011-05-09
        String[] c = new String[]
            { "20110509092359990-10", "20110509092359990-11", "20110509092359990-12" };

        String[] ab = (String[]) ArrayUtils.addAll(a, b);
        String[] bc = (String[]) ArrayUtils.addAll(b, c);
        String[] abc = (String[]) ArrayUtils.addAll(ab, c);
        String[] ac = (String[]) ArrayUtils.addAll(a, c);

        return new Object[][]
            {
                        { "less than now", getDate("2012-01-01"), null, null, abc, null },
                        { "less than late+1", getDate("2011-05-10"), null, null, abc, null },
                        { "less than late", getDate("2011-05-09"), null, null, abc, null },
                        { "less than late-1", getDate("2011-05-08"), null, null, ab, c },
                        { "less than beetween middle / late", getDate("2011-01-01"), null, null,
                                ab, c },
                        { "less than middle+1", getDate("2009-02-10"), null, null, ab, c },
                        { "less than middle", getDate("2009-02-09"), null, null, ab, c },
                        { "less than middle-1", getDate("2009-02-08"), null, null, a, bc },
                        { "less than beetween middle / early", getDate("2009-01-01"), null, null,
                                a, bc },
                        { "less than early+1", getDate("2008-11-06"), null, null, a, bc },
                        { "less than early", getDate("2008-11-05"), null, null, a, bc },
                        { "less than early-1", getDate("2008-11-04"), null, null, null, abc },
                        { "less than in the past", getDate("2007-12-30"), null, null, null, abc },

                        { "more than now", null, getDate("2012-01-01"), null, null, abc },
                        { "more than late+1", null, getDate("2011-05-10"), null, null, abc },
                        { "more than late", null, getDate("2011-05-09"), null, c, ab },
                        { "more than late-1", null, getDate("2011-05-08"), null, c, ab },
                        { "more than beetween middle / late", null, getDate("2011-01-01"), null, c,
                                ab },
                        { "more than middle+1", null, getDate("2009-02-10"), null, c, ab },
                        { "more than middle", null, getDate("2009-02-09"), null, bc, a },
                        { "more than middle-1", null, getDate("2009-02-08"), null, bc, a },
                        { "more than beetween middle / early", null, getDate("2009-01-01"), null,
                                bc, a },
                        { "more than early+1", null, getDate("2008-11-06"), null, bc, a },
                        { "more than early", null, getDate("2008-11-05"), null, abc, null },
                        { "more than early-1", null, getDate("2008-11-04"), null, abc, null },
                        { "more than in the past", null, getDate("2007-12-30"), null, abc, null },

                        { "equal now", null, null, getDate("2012-01-01"), null, abc },
                        { "equal late+1", null, null, getDate("2011-05-10"), null, abc },
                        { "equal late", null, null, getDate("2011-05-09"), c, ab },
                        { "equal late-1", null, null, getDate("2011-05-08"), null, abc },
                        { "equal beetween middle / late", null, null, getDate("2011-01-01"), null,
                                abc },
                        { "equal middle+1", null, null, getDate("2009-02-10"), null, abc },
                        { "equal middle", null, null, getDate("2009-02-09"), b, ac },
                        { "equal middle-1", null, null, getDate("2009-02-08"), null, abc },
                        { "equal beetween middle / early", null, null, getDate("2009-01-01"), null,
                                abc },
                        { "equal early+1", null, null, getDate("2008-11-06"), null, abc },
                        { "equal early", null, null, getDate("2008-11-05"), a, bc },
                        { "equal early-1", null, null, getDate("2008-11-04"), null, abc },
                        { "equal in the past", null, null, getDate("2007-12-30"), null, abc },

                        { "later/earlier constraint for all data", getDate("2011-05-09"),
                                getDate("2008-11-05"), null, abc, null },
                        { "triple constraint for middle data", getDate("2011-05-09"),
                                getDate("2008-11-05"), getDate("2009-02-09"), b, ac },
                        { "invalid contraint constraint for middle data", getDate("2009-01-01"),
                                getDate("2009-01-01"), null, null, abc },
                        { "later/earlier constraint for the middle data", getDate("2011-05-08"),
                                getDate("2008-11-06"), getDate("2009-02-09"), b, ac }, };

    }

    @Test(dataProvider = "testSearchForDataSetsWithRegistrationDate")
    public void testSearchForDataSetsWithRegistrationDate(String description, Date lessThan,
            Date moreThan, Date equal, String[] expectedDatasets, String[] unexpectedDataSets)
    {

        if (lessThan == null && moreThan == null && equal == null)
        {
            throw new IllegalArgumentException("Specify at least one date criterium for this test.");
        }

        SearchCriteria sc = new SearchCriteria();

        if (lessThan != null)
        {

            sc.addMatchClause(MatchClause.createAttributeMatch(
                    MatchClauseAttribute.REGISTRATION_DATE,
                    SearchCriteria.CompareMode.LESS_THAN_OR_EQUAL, lessThan));
        }

        if (moreThan != null)
        {

            sc.addMatchClause(MatchClause.createAttributeMatch(
                    MatchClauseAttribute.REGISTRATION_DATE,
                    SearchCriteria.CompareMode.MORE_THAN_OR_EQUAL, moreThan));
        }

        if (equal != null)
        {

            sc.addMatchClause(MatchClause.createAttributeMatch(
                    MatchClauseAttribute.REGISTRATION_DATE, SearchCriteria.CompareMode.EQUALS,
                    equal));
        }

        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, sc);

        if (expectedDatasets != null)
        {
            assertThat(result, containsDataSets(expectedDatasets));
        }

        if (unexpectedDataSets != null)
        {
            assertThat(result, doesntContainDataSets(unexpectedDataSets));
        }
    }

    @Test
    public void testSearchForDataSetsWithMultipleSameEqualsClauses()
    {
        Date date1 = getDate("2009-02-09");
        Date date2 = getDate("2009-02-09");

        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATION_DATE,
                SearchCriteria.CompareMode.EQUALS, date1));
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATION_DATE,
                SearchCriteria.CompareMode.EQUALS, date2));
        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, sc);

        assertThat(
                result,
                containsDataSets("20081105092159111-1", "20081105092159222-2",
                        "20081105092159333-3", "20110805092359990-17"));
    }

    @Test
    public void testSearchForDataSetsWithMultipleDifferentEqualsClausesOnRegistrationDate()
    {
        Date date1 = getDate("2011-05-09");
        Date date2 = getDate("2009-02-09");

        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATION_DATE,
                SearchCriteria.CompareMode.EQUALS, date1));
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATION_DATE,
                SearchCriteria.CompareMode.EQUALS, date2));
        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, sc);

        assertThat(result.size(), is(0));
    }

    @Test
    public void testSearchForDataSetsWithModificationDateRange()
    {
        Date lower = getDate("2011-01-22");
        Date upper = getDate("2012-04-23");

        SearchCriteria sc = new SearchCriteria();

        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFICATION_DATE,
                SearchCriteria.CompareMode.MORE_THAN_OR_EQUAL, lower));
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFICATION_DATE,
                SearchCriteria.CompareMode.LESS_THAN_OR_EQUAL, upper));
        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, sc);

        assertThat(
                result,
                containsDataSets("20110509092359990-10", "20110509092359990-11",
                        "20110509092359990-12"));
    }

    @Test
    public void testSearchForDataSetsWithRegistrationDateRangeAndModificationDateRange()
    {

        Date lowerReg = getDate("2009-02-01");
        Date upperReg = getDate("2009-04-15");
        Date lowerMod = getDate("2009-03-22");
        Date upperMod = getDate("2009-04-23");

        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATION_DATE,
                SearchCriteria.CompareMode.MORE_THAN_OR_EQUAL, lowerReg));
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATION_DATE,
                SearchCriteria.CompareMode.LESS_THAN_OR_EQUAL, upperReg));

        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFICATION_DATE,
                SearchCriteria.CompareMode.MORE_THAN_OR_EQUAL, lowerMod));
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFICATION_DATE,
                SearchCriteria.CompareMode.LESS_THAN_OR_EQUAL, upperMod));
        List<DataSet> result = generalInformationService.searchForDataSets(sessionToken, sc);

        assertThat(
                result,
                containsDataSets("20081105092159111-1", "20081105092159222-2",
                        "20081105092159333-3", "20110805092359990-17"));
    }

    @Test
    public void testSearchSamplesWithOneDayRegistrationDateRange()
    {
        Date lower = getDate("2009-02-09");
        Date upper = getDate("2009-02-09");

        SearchCriteria sc = new SearchCriteria();

        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATION_DATE,
                SearchCriteria.CompareMode.MORE_THAN_OR_EQUAL, lower));
        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.REGISTRATION_DATE,
                SearchCriteria.CompareMode.LESS_THAN_OR_EQUAL, upper));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);

        assertThat(
                result,
                containsSamples("CP-TEST-1", "CP-TEST-2", "CP-TEST-3", "PLATE_WELLSEARCH",
                        "PLATE_WELLSEARCH:WELL-A01", "PLATE_WELLSEARCH:WELL-A02"));
    }

    @Test
    public void testSearchSamplesWithModificationDate()
    {
        Date date = getDate("2009-08-18");

        SearchCriteria sc = new SearchCriteria();

        sc.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.MODIFICATION_DATE,
                SearchCriteria.CompareMode.EQUALS, date));
        List<Sample> result = generalInformationService.searchForSamples(sessionToken, sc);

        assertThat(
                result,
                containsSamples("CP-TEST-1", "CP-TEST-2", "PLATE_WELLSEARCH",
                        "PLATE_WELLSEARCH:WELL-A01", "PLATE_WELLSEARCH:WELL-A02"));
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
                "[DataSet[20081105092159111-1,/CISD/NEMO/EXP-TEST-1,/CISD/CP-TEST-1,HCS_IMAGE,{ANY_MATERIAL=1000_C (SIRNA), BACTERIUM=BACTERIUM1 (BACTERIUM), COMMENT=no comment, GENDER=FEMALE}], "
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

    @Test
    public void testListVocabularyTerms()
    {
        List<Vocabulary> vocabularies = generalInformationService.listVocabularies(sessionToken);

        final Vocabulary gender = findVocabulary(vocabularies, "GENDER");
        assertEquals(
                "Vocabulary[GENDER,[VocabularyTerm[MALE,MALE], VocabularyTerm[FEMALE,FEMALE]]]",
                gender.toString());
        final Vocabulary human = findVocabulary(vocabularies, "HUMAN");
        assertEquals(
                "Vocabulary[HUMAN,[VocabularyTerm[MAN,MAN], VocabularyTerm[WOMAN,WOMAN], VocabularyTerm[CHILD,CHILD]]]",
                human.toString());

        Vocabulary organism = findVocabulary(vocabularies, "ORGANISM");
        assertNotNull(organism);
        assertEquals("VocabularyTerm[RAT,RAT]", organism.getTerms().get(0).toString());
    }

    @Test
    public void testDataSetVocabularyProperties()
    {
        List<DataSet> dataSets =
                generalInformationService.getDataSetMetaData(sessionToken,
                        Collections.singletonList("20081105092159111-1"));

        assertEquals(1, dataSets.size());
        assertEquals("FEMALE", dataSets.get(0).getProperties().get("GENDER"));

    }

    private Vocabulary findVocabulary(List<Vocabulary> vocabularies, String vocabularyCode)
    {
        for (Vocabulary vocabulary : vocabularies)
        {
            if (vocabulary.getCode().equals(vocabularyCode))
            {
                return vocabulary;
            }
        }
        return null;
    }

    public static Matcher<Collection<DataSet>> containsDataSets(String... codes)
    {
        return new ContainsDataSetMatcher(CheckMode.CHECK_CONTAINS, codes);
    }

    public static Matcher<Collection<DataSet>> doesntContainDataSets(String... codes)
    {
        return new ContainsDataSetMatcher(CheckMode.CHECK_DOESNT_CONTAIN, codes);
    }

    public static class ContainsDataSetMatcher extends TypeSafeMatcher<Collection<DataSet>>
    {
        public enum CheckMode
        {
            CHECK_CONTAINS, CHECK_DOESNT_CONTAIN
        }

        private final CheckMode checkMode;

        private final Collection<String> codes;

        private String fatalString;

        public ContainsDataSetMatcher(CheckMode checkMode, String... datasetCodes)
        {
            this.codes = Collections.unmodifiableCollection(Arrays.asList(datasetCodes));
            this.checkMode = checkMode;
        }

        public void describeTo(Description description)
        {
            description.appendText("a collection containing unwanted " + fatalString
                    + " as well as all datasets " + this.codes);
        }

        @Override
        public boolean matchesSafely(Collection<DataSet> actualDataSets)
        {

            Set<String> actualCodes = new HashSet<String>();
            for (DataSet set : actualDataSets)
            {
                actualCodes.add(set.getCode());
            }

            for (String code : this.codes)
            {
                boolean codeContained = actualCodes.contains(code);

                switch (checkMode)
                {
                    case CHECK_CONTAINS:
                        if (false == codeContained)
                        {
                            fatalString = code;
                            return false;
                        }
                        break;
                    case CHECK_DOESNT_CONTAIN:
                        if (codeContained)
                        {
                            fatalString = code;
                            return false;
                        }
                        break;
                }
            }
            return true;
        }
    }

    public static Matcher<Collection<Sample>> containsSamples(String... codes)
    {
        return new ContainsSampleMatcher(codes);
    }

    public static class ContainsSampleMatcher extends TypeSafeMatcher<Collection<Sample>>
    {

        private Collection<String> codes;

        public ContainsSampleMatcher(String... sampleCodes)
        {
            this.codes = Collections.unmodifiableCollection(Arrays.asList(sampleCodes));
        }

        public void describeTo(Description description)
        {
            description.appendText("a collection containing all samples " + this.codes);
        }

        @Override
        public boolean matchesSafely(Collection<Sample> actualSamples)
        {

            Set<String> actualCodes = new HashSet<String>();
            for (Sample sample : actualSamples)
            {
                actualCodes.add(sample.getCode());
            }

            for (String code : this.codes)
            {
                if (!actualCodes.contains(code))
                {
                    return false;
                }
            }
            return true;

        }
    }

}
