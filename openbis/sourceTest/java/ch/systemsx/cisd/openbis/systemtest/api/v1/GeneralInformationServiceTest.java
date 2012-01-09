/*
 * Copyright 2011 ETH Zuerich, CISD
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.EntityRegistrationDetails.EntityRegistrationDetailsInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment.ExperimentInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
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
    public void testListSpaces()
    {
        List<SpaceWithProjectsAndRoleAssignments> spaces =
                generalInformationService.listSpacesWithProjectsAndRoleAssignments(sessionToken,
                        null);
        assertEquals(2, spaces.size());
        loginAsObserver();

        spaces =
                generalInformationService.listSpacesWithProjectsAndRoleAssignments(sessionToken,
                        null);

        assertEquals("TESTGROUP", spaces.get(0).getCode());
        assertEquals(1, spaces.size());
    }

    @Test
    public void testListProjects()
    {
        List<Project> projects = generalInformationService.listProjects(sessionToken);
        Collections.sort(projects, new Comparator<Project>()
            {
                public int compare(Project p1, Project p2)
                {
                    return p1.getIdentifier().compareTo(p2.getIdentifier());
                }
            });
        assertEquals("[/CISD/DEFAULT, /CISD/NEMO, /CISD/NOE, /TESTGROUP/TESTPROJ]",
                projects.toString());

        loginAsObserver();

        projects = generalInformationService.listProjects(sessionToken);
        assertEquals("[/TESTGROUP/TESTPROJ]", projects.toString());
    }

    @Test
    public void testSearchForSamples()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "MASTER_PLATE"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/EMPTY-MP, /CISD/MP002-1, /CISD/MP1-MIXED, /CISD/MP2-NO-CL, /MP]",
                samples);

        loginAsObserver();
        samples = generalInformationService.searchForSamples(sessionToken, searchCriteria);

        assertEntities("[]", samples);
    }

    @Test
    public void testListSamplesForExperiment()
    {
        List<Sample> samples =
                generalInformationService
                        .listSamplesForExperiment(sessionToken, "/CISD/NEMO/EXP10");
        assertEntities("[/CISD/3VCP5]", samples);

        loginAsObserver();
        try
        {
            generalInformationService.listSamplesForExperiment(sessionToken, "/CISD/NEMO/EXP1");
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testListDataSets()
    {
        SampleInitializer s1 = new SampleInitializer();
        s1.setId(1042L);
        s1.setCode("CP-TEST-1");
        s1.setIdentifier("/CISD/CP-TEST-1");
        s1.setPermId("200902091219327-1025");
        s1.setSampleTypeCode("CELL_PLATE");
        s1.setSampleTypeId(3L);
        s1.setRegistrationDetails(new EntityRegistrationDetails(
                new EntityRegistrationDetailsInitializer()));
        List<DataSet> dataSets =
                generalInformationService.listDataSets(sessionToken, Arrays.asList(new Sample(s1)));

        assertDataSets("[20081105092159111-1]", dataSets);

        loginAsObserver();
        try
        {
            generalInformationService.listDataSets(sessionToken, Arrays.asList(new Sample(s1)));
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testListDataSetsForSampleOnlyDirectlyConnected()
    {
        SampleInitializer s1 = new SampleInitializer();
        s1.setId(1042L);
        s1.setCode("CP-TEST-1");
        s1.setIdentifier("/CISD/CP-TEST-1");
        s1.setPermId("200902091219327-1025");
        s1.setSampleTypeCode("CELL_PLATE");
        s1.setSampleTypeId(3L);
        s1.setRegistrationDetails(new EntityRegistrationDetails(
                new EntityRegistrationDetailsInitializer()));
        Sample sample = new Sample(s1);
        ExternalData dataSetInfo = genericServer.getDataSetInfo(sessionToken, new TechId(13));
        DataSetUpdatesDTO updates = new DataSetUpdatesDTO();
        updates.setVersion(dataSetInfo.getModificationDate());
        updates.setDatasetId(new TechId(dataSetInfo.getId()));
        updates.setProperties(dataSetInfo.getProperties());
        updates.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(dataSetInfo
                .getExperiment().getIdentifier()));
        updates.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sample.getIdentifier()));
        commonServer.updateDataSet(sessionToken, updates);
        List<DataSet> dataSets =
                generalInformationService.listDataSetsForSample(sessionToken, sample, true);

        assertDataSets("[20081105092159111-1, 20110509092359990-10]", dataSets);
        assertEquals("[DataSet[20110509092359990-11,<null>,<null>,HCS_IMAGE,{}], "
                + "DataSet[20110509092359990-12,<null>,<null>,HCS_IMAGE,{}]]", dataSets.get(1)
                .getContainedDataSets().toString());

        loginAsObserver();
        try
        {
            generalInformationService.listDataSetsForSample(sessionToken, sample, false);
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testListDataSetsForSampleAlsoIndirectlyConnected()
    {
        SampleInitializer s1 = new SampleInitializer();
        s1.setId(1042L);
        s1.setCode("CP-TEST-1");
        s1.setIdentifier("/CISD/CP-TEST-1");
        s1.setPermId("200902091219327-1025");
        s1.setSampleTypeCode("CELL_PLATE");
        s1.setSampleTypeId(3L);
        s1.setRegistrationDetails(new EntityRegistrationDetails(
                new EntityRegistrationDetailsInitializer()));
        List<DataSet> dataSets =
                generalInformationService
                        .listDataSetsForSample(sessionToken, new Sample(s1), false);

        assertDataSets(
                "[20081105092159111-1, 20081105092259000-9, 20081105092259900-0, 20081105092259900-1, 20081105092359990-2]",
                dataSets);
        assertEquals(
                "DataSet[20081105092259000-9,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=no comment}]",
                dataSets.get(1).toString());

        loginAsObserver();
        try
        {
            generalInformationService.listDataSetsForSample(sessionToken, new Sample(s1), false);
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testListDataSetsWithConnections()
    {
        SampleInitializer s1 = new SampleInitializer();
        s1.setId(1043L);
        s1.setCode("CP-TEST-2");
        s1.setIdentifier("/CISD/CP-TEST-2");
        s1.setPermId("200902091250077-1026");
        s1.setSampleTypeCode("CELL_PLATE");
        s1.setSampleTypeId(3L);
        s1.setRegistrationDetails(new EntityRegistrationDetails(
                new EntityRegistrationDetailsInitializer()));
        List<DataSet> dataSets =
                generalInformationService.listDataSets(sessionToken, Arrays.asList(new Sample(s1)),
                        EnumSet.allOf(Connections.class));

        assertDataSets("[20081105092159222-2]", dataSets);
        assertEquals("[]", dataSets.get(0).getParentCodes().toString());

        loginAsObserver();
        try
        {
            generalInformationService.listDataSets(sessionToken, Arrays.asList(new Sample(s1)),
                    EnumSet.of(Connections.CHILDREN));
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testListDataSetsForExperiments()
    {
        ExperimentInitializer e1 = new ExperimentInitializer();
        e1.setId(8L);
        e1.setCode("EXP-REUSE");
        e1.setIdentifier("/CISD/DEFAULT/EXP-REUSE");
        e1.setExperimentTypeCode("SIRNA_HCS");
        e1.setPermId("200811050940555-1032");
        e1.setRegistrationDetails(new EntityRegistrationDetails(
                new EntityRegistrationDetailsInitializer()));
        List<DataSet> dataSets =
                generalInformationService.listDataSetsForExperiments(sessionToken,
                        Arrays.asList(new Experiment(e1)), EnumSet.allOf(Connections.class));

        assertDataSets("[20081105092259000-18, 20081105092259000-8, 20081105092259000-9, "
                + "20081105092259900-0, 20081105092259900-1, 20081105092359990-2, "
                + "20110509092359990-10, 20110509092359990-11, 20110509092359990-12]", dataSets);
        List<String> parentCodes = new ArrayList<String>(dataSets.get(2).getParentCodes());
        Collections.sort(parentCodes);
        assertEquals("[20081105092159111-1, 20081105092159222-2, 20081105092159333-3]",
                parentCodes.toString());
        List<String> childrenCodes = new ArrayList<String>(dataSets.get(2).getChildrenCodes());
        Collections.sort(childrenCodes);
        assertEquals("[]", childrenCodes.toString());
        DataSet dataSet = dataSets.get(6);
        assertEquals(true, dataSet.isContainerDataSet());
        assertEquals("[DataSet[20110509092359990-11,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,"
                + "{COMMENT=non-virtual comment},[]], "
                + "DataSet[20110509092359990-12,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,"
                + "{COMMENT=non-virtual comment},[]]]", dataSet.getContainedDataSets().toString());
        assertEquals(
                "DataSet[20110509092359990-10,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{},[]]",
                dataSet.toString());

        loginAsObserver();
        try
        {
            generalInformationService.listDataSetsForExperiments(sessionToken,
                    Arrays.asList(new Experiment(e1)), EnumSet.allOf(Connections.class));
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testGetDataSetMetaData()
    {
        List<DataSet> dataSets =
                generalInformationService.getDataSetMetaData(sessionToken,
                        Arrays.asList("20081105092159222-2", "20110509092359990-10"));
        assertEquals(
                "[DataSet[20081105092159222-2,/CISD/NOE/EXP-TEST-2,/CISD/CP-TEST-2,HCS_IMAGE,{COMMENT=no comment},[]], "
                        + "DataSet[20110509092359990-10,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{},[]]]",
                dataSets.toString());
        assertEquals("[]", dataSets.get(1).getContainedDataSets().toString());

        loginAsObserver();
        dataSets =
                generalInformationService.getDataSetMetaData(sessionToken,
                        Arrays.asList("20081105092159222-2"));

        assertEquals("[]", dataSets.toString());
    }

    @Test
    public void testSearchForDataSets()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "CONTAINER_TYPE"));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(
                "[DataSet[20110509092359990-10,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{}]]",
                dataSets.toString());
        List<DataSet> containedDataSets = dataSets.get(0).getContainedDataSets();
        assertEquals("[DataSet[20110509092359990-11,<null>,<null>,HCS_IMAGE,{}], "
                + "DataSet[20110509092359990-12,<null>,<null>,HCS_IMAGE,{}]]",
                containedDataSets.toString());
        assertEquals(1304929379313L, containedDataSets.get(0).getRegistrationDate().getTime());

        loginAsObserver();
        dataSets = generalInformationService.searchForDataSets(sessionToken, searchCriteria);

        assertEquals("[]", dataSets.toString());
    }

    @Test
    public void testGetDefaultPutDataStoreBaseURL()
    {
        String url = generalInformationService.getDefaultPutDataStoreBaseURL(sessionToken);

        assertEquals("", url);
    }

    @Test
    public void testTryGetDataStoreBaseURL()
    {
        String url =
                generalInformationService.tryGetDataStoreBaseURL(sessionToken,
                        "20081105092159111-1");

        assertEquals("", url);
    }

    @Test
    public void testListDataSetTypes()
    {
        List<DataSetType> types = generalInformationService.listDataSetTypes(sessionToken);
        Collections.sort(types, new Comparator<DataSetType>()
            {
                public int compare(DataSetType t1, DataSetType t2)
                {
                    return t1.getCode().compareTo(t2.getCode());
                }
            });
        assertEquals("CONTAINER_TYPE", types.get(0).getCode());
        assertEquals("[PropertyTypeGroup[<null>,[]]]", types.get(0).getPropertyTypeGroups()
                .toString());
        assertEquals("HCS_IMAGE", types.get(1).getCode());
        List<PropertyTypeGroup> groups = types.get(1).getPropertyTypeGroups();
        List<PropertyType> propertyTypes = groups.get(0).getPropertyTypes();
        Collections.sort(propertyTypes, new Comparator<PropertyType>()
            {
                public int compare(PropertyType t1, PropertyType t2)
                {
                    return t1.getCode().compareTo(t2.getCode());
                }
            });
        List<String> propertyTypeCodes = new ArrayList<String>();
        for (PropertyType propertyType : propertyTypes)
        {
            propertyTypeCodes.add(propertyType.getCode());
        }
        assertEquals("[ANY_MATERIAL, BACTERIUM, COMMENT, GENDER]", propertyTypeCodes.toString());
        assertEquals("PropertyType[MATERIAL,ANY_MATERIAL,any_material,any_material,optional]",
                propertyTypes.get(0).toString());
        assertEquals(1, groups.size());
        assertEquals("HCS_IMAGE_ANALYSIS_DATA", types.get(2).getCode());
        assertEquals("[PropertyTypeGroup[<null>,[]]]", types.get(2).getPropertyTypeGroups()
                .toString());
        assertEquals("UNKNOWN", types.get(3).getCode());
        assertEquals("[PropertyTypeGroup[<null>,[]]]", types.get(3).getPropertyTypeGroups()
                .toString());
        assertEquals(4, types.size());
    }

    @Test
    public void testListExperimentsByProjects()
    {
        List<Project> projects = Arrays.asList(new Project("CISD", "NEMO"));
        List<Experiment> experiments =
                generalInformationService.listExperiments(sessionToken, projects, "SIRNA_HCS");

        assertEntities(
                "[/CISD/NEMO/EXP-TEST-2, /CISD/NEMO/EXP1, /CISD/NEMO/EXP10, /CISD/NEMO/EXP11]",
                experiments);

        loginAsObserver();
        try
        {
            generalInformationService.listExperiments(sessionToken, projects, "SIRNA_HCS");
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testListExperimentsHavingSamples()
    {
        List<Project> projects = Arrays.asList(new Project("CISD", "NOE"));
        List<Experiment> experiments =
                generalInformationService.listExperimentsHavingSamples(sessionToken, projects,
                        "COMPOUND_HCS");

        assertEntities("[/CISD/NOE/EXP-TEST-2]", experiments);
        assertEquals("Experiment[/CISD/NOE/EXP-TEST-2,COMPOUND_HCS,{DESCRIPTION=desc2}]",
                experiments.get(0).toString());

        loginAsObserver();
        try
        {
            generalInformationService.listExperimentsHavingSamples(sessionToken, projects,
                    "COMPOUND_HCS");
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testListExperimentsHavingDataSetsSamples()
    {
        List<Project> projects = Arrays.asList(new Project("CISD", "NOE"));
        List<Experiment> experiments =
                generalInformationService.listExperimentsHavingDataSets(sessionToken, projects,
                        "COMPOUND_HCS");

        assertEntities("[/CISD/NOE/EXP-TEST-2]", experiments);
        assertEquals("Experiment[/CISD/NOE/EXP-TEST-2,COMPOUND_HCS,{DESCRIPTION=desc2}]",
                experiments.get(0).toString());

        loginAsObserver();
        try
        {
            generalInformationService.listExperimentsHavingDataSets(sessionToken, projects,
                    "COMPOUND_HCS");
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    @Test
    public void testListExperimentsByIdentifiers()
    {
        List<Experiment> experiments =
                generalInformationService.listExperiments(sessionToken,
                        Arrays.asList("/CISD/NOE/EXP-TEST-2"));

        assertEquals("[Experiment[/CISD/NOE/EXP-TEST-2,COMPOUND_HCS,{DESCRIPTION=desc2}]]",
                experiments.toString());

        loginAsObserver();
        try
        {
            generalInformationService.listExperiments(sessionToken,
                    Arrays.asList("/CISD/NEMO/EXP1"));
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals(
                    "Authorization failure: ERROR: \"User 'observer' does not have enough privileges.\".",
                    ex.getMessage());
        }
    }

    private void loginAsObserver()
    {
        generalInformationService.logout(sessionToken);
        sessionToken = generalInformationService.tryToAuthenticateForAllServices("observer", "a");
    }

    private void assertEntities(String expectedEntities, List<? extends IIdentifierHolder> entities)
    {
        List<String> identifiers = new ArrayList<String>();
        for (IIdentifierHolder entity : entities)
        {
            identifiers.add(entity.getIdentifier());
        }
        Collections.sort(identifiers);
        assertEquals(expectedEntities, identifiers.toString());
    }

    private void assertDataSets(String expectedDataSets, List<DataSet> dataSets)
    {
        List<String> codes = new ArrayList<String>();
        for (DataSet dataSet : dataSets)
        {
            codes.add(dataSet.getCode());
        }
        Collections.sort(codes);
        assertEquals(expectedDataSets, codes.toString());
    }
}
