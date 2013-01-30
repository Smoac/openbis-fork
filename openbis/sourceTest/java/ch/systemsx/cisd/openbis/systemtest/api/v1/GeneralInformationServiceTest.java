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
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
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
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.MetaprojectAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.PropertyTypeGroup;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SampleFetchOption;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.CompareMode;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseTimeAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.metaproject.MetaprojectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Metaproject;
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
        assertEquals(3, spaces.size());
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
                @Override
                public int compare(Project p1, Project p2)
                {
                    return p1.getIdentifier().compareTo(p2.getIdentifier());
                }
            });
        assertEquals(
                "[/CISD/DEFAULT, /CISD/NEMO, /CISD/NOE, /TEST-SPACE/TEST-PROJECT, /TESTGROUP/TESTPROJ]",
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
        assertEquals(1225873363584L, samples.get(0).getRegistrationDetails().getRegistrationDate()
                .getTime());
        assertEquals(1237369819475L, samples.get(0).getRegistrationDetails().getModificationDate()
                .getTime());

        loginAsObserver();
        samples = generalInformationService.searchForSamples(sessionToken, searchCriteria);

        assertEntities("[]", samples);
    }

    @Test
    public void testSearchForSamplesOnBehalfOfUser()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "*TEST*"));

        // executed by test
        List<Sample> testResult =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities(
                "[/CISD/CP-TEST-1, /CISD/CP-TEST-2, /CISD/CP-TEST-3, /CISD/DYNA-TEST-1, /TEST-SPACE/EV-TEST, /TEST-SPACE/FV-TEST]",
                testResult);

        // executed by test on behalf of test_space
        List<Sample> onBehalfOfTestSpaceResult =
                generalInformationService.searchForSamplesOnBehalfOfUser(sessionToken,
                        searchCriteria, EnumSet.of(SampleFetchOption.BASIC), "test_space");
        assertEntities("[/TEST-SPACE/EV-TEST, /TEST-SPACE/FV-TEST]", onBehalfOfTestSpaceResult);

        // executed by test_space
        generalInformationService.logout(sessionToken);
        sessionToken =
                generalInformationService.tryToAuthenticateForAllServices("test_space", "password");

        List<Sample> testSpaceResult =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/EV-TEST, /TEST-SPACE/FV-TEST]", testSpaceResult);
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testSearchForSamplesOnBehalfOfUserExecutedByNotInstanceUser()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "SOME_CODE"));

        generalInformationService.logout(sessionToken);
        sessionToken =
                generalInformationService.tryToAuthenticateForAllServices("test_role", "password");
        generalInformationService.searchForSamplesOnBehalfOfUser(sessionToken, searchCriteria,
                EnumSet.of(SampleFetchOption.BASIC), "admin");
    }

    @Test
    public void testSearchForSamplesOnBehalfOfUserByMetaprojectName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS"));

        // executed by test
        List<Sample> testResult =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/EV-TEST]", testResult);

        // executed by test on behalf of test_space
        List<Sample> onBehalfOfTestSpaceResult =
                generalInformationService.searchForSamplesOnBehalfOfUser(sessionToken,
                        searchCriteria, EnumSet.of(SampleFetchOption.BASIC), "test_space");
        assertEntities("[/TEST-SPACE/EV-TEST, /TEST-SPACE/FV-TEST]", onBehalfOfTestSpaceResult);

        // executed by test_space
        generalInformationService.logout(sessionToken);
        sessionToken =
                generalInformationService.tryToAuthenticateForAllServices("test_space", "password");

        List<Sample> testSpaceResult =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/EV-TEST, /TEST-SPACE/FV-TEST]", testSpaceResult);
    }

    @Test
    public void testSearchForSamplesByPermId()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.PERM_ID, "201206191219327-1054"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/FV-TEST]", samples);
    }

    @Test
    public void testSearchForSamplesByPermIdByAnyField()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("201206191219327-1054"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/FV-TEST]", samples);
    }

    @Test
    public void testSearchForSamplesByMetaprojectName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/EV-TEST]", samples);
    }

    @Test
    public void testSearchForSamplesByMetaprojectNameWithDifferentCase()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TeSt_MeTaPrOjEcTs"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/EV-TEST]", samples);
    }

    @Test
    public void testSearchForSamplesByMetaprojectNameWithWildcards()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "*TeSt_MeTa*"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/3V-125, /TEST-SPACE/EV-TEST]", samples);
    }

    @Test
    public void testSearchForSamplesByMetaprojectNameOwnedBySomebodyElse()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS_2"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[]", samples);
    }

    @Test
    public void testSearchForSamplesByMetaprojectIdentifier()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test/TEST_METAPROJECTS"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/EV-TEST]", samples);
    }

    public void testSearchForSamplesByMetaprojectIdentifierWithDifferentCase()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test/TeSt_MeTaPrOjEcTs"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/TEST-SPACE/EV-TEST]", samples);
    }

    public void testSearchForSamplesByMetaprojectIdentifierWithWildcards()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test/*TeSt_MeTa*"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/3V-125, /TEST-SPACE/EV-TEST]", samples);
    }

    @Test
    public void testSearchForSamplesByMetaprojectIdentifierOwnedBySomebodyElse()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test_role/TEST_METAPROJECTS_2"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[]", samples);
    }

    @Test
    public void testSearchForSamplesWithChildren()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "DILUTION_PLATE"));

        EnumSet<SampleFetchOption> fetchOptions =
                EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.CHILDREN);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/3V-125, /CISD/3V-126, /CISD/DP1-A, /CISD/DP1-B, /CISD/DP2-A, /DP]",
                samples);
        assertEquals(fetchOptions, samples.get(0).getRetrievedFetchOptions());
        assertEquals("3V-125", samples.get(0).getCode());
        assertEquals("CISD", samples.get(0).getSpaceCode());
        assertEquals(979L, samples.get(0).getId().longValue());
        assertEquals("200811050945092-976", samples.get(0).getPermId());
        assertEquals("DILUTION_PLATE", samples.get(0).getSampleTypeCode());
        assertEquals(2L, samples.get(0).getSampleTypeId().longValue());
        assertEquals("test", samples.get(0).getRegistrationDetails().getUserId());
        assertEquals("franz-josef.elmer@systemsx.ch", samples.get(0).getRegistrationDetails()
                .getUserEmail());
        assertEquals("{OFFSET=49}", samples.get(0).getProperties().toString());
        List<Sample> children = samples.get(0).getChildren();
        assertEntities("[/CISD/3VCP5, /CISD/3VCP6, /CISD/3VCP7, /CISD/3VCP8]", children);
        assertEquals("3VCP5", children.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES), children.get(0)
                .getRetrievedFetchOptions());
        assertEquals("{}", children.get(0).getProperties().toString());

        loginAsObserver();
        samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[]", samples);
    }

    @Test
    public void testSearchForSamplesWithMetaprojects()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "DILUTION_PLATE"));

        EnumSet<SampleFetchOption> fetchOptions =
                EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.METAPROJECTS);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/3V-125, /CISD/3V-126, /CISD/DP1-A, /CISD/DP1-B, /CISD/DP2-A, /DP]",
                samples);
        assertEquals(fetchOptions, samples.get(0).getRetrievedFetchOptions());
        assertEquals("3V-125", samples.get(0).getCode());
        assertEquals("CISD", samples.get(0).getSpaceCode());
        assertEquals(979L, samples.get(0).getId().longValue());
        assertEquals("200811050945092-976", samples.get(0).getPermId());
        assertEquals("DILUTION_PLATE", samples.get(0).getSampleTypeCode());
        assertEquals(2L, samples.get(0).getSampleTypeId().longValue());
        assertEquals("test", samples.get(0).getRegistrationDetails().getUserId());
        assertEquals("franz-josef.elmer@systemsx.ch", samples.get(0).getRegistrationDetails()
                .getUserEmail());
        assertEquals("{OFFSET=49}", samples.get(0).getProperties().toString());
        assertEquals(1, samples.get(0).getMetaprojects().size());

        Metaproject metaproject = samples.get(0).getMetaprojects().get(0);
        assertEquals(3l, metaproject.getId().longValue());
        assertEquals("ANOTHER_TEST_METAPROJECTS", metaproject.getName());
        assertEquals("Another example metaproject", metaproject.getDescription());
        assertTrue(metaproject.isPrivate());
        assertNotNull(metaproject.getCreationDate());

        loginAsObserver();
        samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[]", samples);
    }

    @Test
    public void testSearchForSamplesWithParents()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "DILUTION_PLATE"));

        EnumSet<SampleFetchOption> fetchOptions =
                EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.PARENTS);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/3V-125, /CISD/3V-126, /CISD/DP1-A, /CISD/DP1-B, /CISD/DP2-A, /DP]",
                samples);
        assertEquals(fetchOptions, samples.get(0).getRetrievedFetchOptions());
        List<Sample> parents = samples.get(0).getParents();
        assertEntities("[/CISD/MP002-1]", parents);
        assertEquals("MP002-1", parents.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES), parents.get(0)
                .getRetrievedFetchOptions());
        assertEquals("{PLATE_GEOMETRY=384_WELLS_16X24}", parents.get(0).getProperties().toString());
    }

    @Test
    public void testSearchForSamplesWithParentsButNoProperties()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "DILUTION_PLATE"));

        EnumSet<SampleFetchOption> fetchOptions = EnumSet.of(SampleFetchOption.PARENTS);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/3V-125, /CISD/3V-126, /CISD/DP1-A, /CISD/DP1-B, /CISD/DP2-A, /DP]",
                samples);
        assertEquals(EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.PARENTS), samples.get(0)
                .getRetrievedFetchOptions());
        assertEquals(null, samples.get(0).getExperimentIdentifierOrNull());
        try
        {
            samples.get(0).getProperties();
            fail("Get properties should have thrown an illegal argument exception");
        } catch (IllegalArgumentException e)
        {
        }
        List<Sample> parents = samples.get(0).getParents();
        assertEntities("[/CISD/MP002-1]", parents);
        assertEquals("MP002-1", parents.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC), parents.get(0).getRetrievedFetchOptions());
        try
        {
            parents.get(0).getProperties();
            fail("Get properties should have thrown an illegal argument exception");
        } catch (IllegalArgumentException e)
        {
        }
    }

    @Test
    public void testSearchForSamplesWithParentsAndDescendentsButNoProperties()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "DILUTION_PLATE"));

        EnumSet<SampleFetchOption> fetchOptions =
                EnumSet.of(SampleFetchOption.PARENTS, SampleFetchOption.DESCENDANTS);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/3V-125, /CISD/3V-126, /CISD/DP1-A, /CISD/DP1-B, /CISD/DP2-A, /DP]",
                samples);
        assertEquals(EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.PARENTS,
                SampleFetchOption.CHILDREN), samples.get(0).getRetrievedFetchOptions());
        assertEquals(null, samples.get(0).getExperimentIdentifierOrNull());
        try
        {
            samples.get(0).getProperties();
            fail("Get properties should have thrown an illegal argument exception");
        } catch (IllegalArgumentException e)
        {
        }
        List<Sample> parents = samples.get(0).getParents();
        assertEntities("[/CISD/MP002-1]", parents);
        assertEquals("MP002-1", parents.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC), parents.get(0).getRetrievedFetchOptions());
        try
        {
            parents.get(0).getProperties();
            fail("Get properties should have thrown an illegal argument exception");
        } catch (IllegalArgumentException e)
        {

        }
        List<Sample> children = samples.get(0).getChildren();
        assertEntities("[/CISD/3VCP5, /CISD/3VCP6, /CISD/3VCP7, /CISD/3VCP8]", children);
        assertEquals("3VCP5", children.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.CHILDREN),
                children.get(0).getRetrievedFetchOptions());
        try
        {
            children.get(0).getProperties();
            fail("Get properties should have thrown an illegal argument exception");
        } catch (IllegalArgumentException e)
        {
        }
        assertEntities("[]", children.get(0).getChildren());
    }

    @Test
    public void testSearchForSamplesWhichAreParentChildRelatedToTestRecursiveLoop()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "CELL_PLATE"));

        EnumSet<SampleFetchOption> fetchOptions =
                EnumSet.of(SampleFetchOption.PARENTS, SampleFetchOption.CHILDREN);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/3VCP5, /CISD/3VCP6, /CISD/3VCP7, /CISD/3VCP8, /CISD/CP-TEST-1, "
                + "/CISD/CP-TEST-2, /CISD/CP-TEST-3, /CISD/CP1-A1, /CISD/CP1-A2, /CISD/CP1-B1, "
                + "/CISD/CP2-A1, /CISD/PLATE_WELLSEARCH, /TEST-SPACE/FV-TEST]", samples);

        Sample sample0 = samples.get(0);
        assertEquals("3VCP5", sample0.getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.PARENTS,
                SampleFetchOption.CHILDREN), sample0.getRetrievedFetchOptions());
        assertEntities("[]", sample0.getChildren());
        assertEquals("/CISD/NEMO/EXP10", sample0.getExperimentIdentifierOrNull());
        List<Sample> parents = sample0.getParents();
        assertEntities("[/CISD/3V-125]", parents);
        assertEquals("3V-125", parents.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC), parents.get(0).getRetrievedFetchOptions());

        Sample sample8 = samples.get(8);
        assertEquals("CP1-A2", sample8.getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.PARENTS,
                SampleFetchOption.CHILDREN), sample8.getRetrievedFetchOptions());
        assertEntities("[/CISD/DP1-A]", sample8.getParents());
        List<Sample> children = sample8.getChildren();
        assertEntities("[/CISD/RP1-A2X]", children);
        assertEquals("RP1-A2X", children.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC), children.get(0)
                .getRetrievedFetchOptions());

        Sample sample4 = samples.get(4);
        assertEquals("CP-TEST-1", sample4.getCode());
        parents = sample4.getParents();
        assertEntities("[/CISD/CP-TEST-2]", parents);
        assertEquals("CP-TEST-2", parents.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.PARENTS,
                SampleFetchOption.CHILDREN), parents.get(0).getRetrievedFetchOptions());
        assertEntities("[/CISD/CP-TEST-1]", parents.get(0).getChildren());
        assertEntities("[]", parents.get(0).getParents());

        Sample sample5 = samples.get(5);
        assertEquals("CP-TEST-2", sample5.getCode());
        children = sample5.getChildren();
        assertEntities("[/CISD/CP-TEST-1]", children);
        assertEquals("CP-TEST-1", children.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.BASIC, SampleFetchOption.PARENTS,
                SampleFetchOption.CHILDREN), children.get(0).getRetrievedFetchOptions());
        assertEntities("[/CISD/CP-TEST-2]", children.get(0).getParents());
        assertEntities("[]", children.get(0).getChildren());
    }

    @Test
    public void testSearchForSamplesWithAncestors()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "3VCP*"));

        EnumSet<SampleFetchOption> fetchOptions =
                EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.ANCESTORS);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/3VCP5, /CISD/3VCP6, /CISD/3VCP7, /CISD/3VCP8]", samples);
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.PARENTS), samples
                .get(0).getRetrievedFetchOptions());
        assertEquals("/CISD/NEMO/EXP10", samples.get(0).getExperimentIdentifierOrNull());
        List<Sample> parents = samples.get(0).getParents();
        assertEntities("[/CISD/3V-125]", parents);
        assertEquals("3V-125", parents.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.PARENTS), parents
                .get(0).getRetrievedFetchOptions());
        List<Sample> grandParents = parents.get(0).getParents();
        assertEntities("[/CISD/MP002-1]", grandParents);
        assertEquals("MP002-1", grandParents.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.PARENTS),
                grandParents.get(0).getRetrievedFetchOptions());
        assertEquals("{PLATE_GEOMETRY=384_WELLS_16X24}", grandParents.get(0).getProperties()
                .toString());
        assertEntities("[]", grandParents.get(0).getParents());
    }

    @Test
    public void testSearchForSamplesWithDescendants()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "MP002-1 "));

        EnumSet<SampleFetchOption> fetchOptions =
                EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.DESCENDANTS);
        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria,
                        fetchOptions);

        assertEntities("[/CISD/MP002-1]", samples);
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.CHILDREN), samples
                .get(0).getRetrievedFetchOptions());
        List<Sample> children = samples.get(0).getChildren();
        assertEntities("[/CISD/3V-125, /CISD/3V-126]", children);
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.CHILDREN), children
                .get(0).getRetrievedFetchOptions());
        List<Sample> grandChildren = children.get(0).getChildren();
        assertEntities("[/CISD/3VCP5, /CISD/3VCP6, /CISD/3VCP7, /CISD/3VCP8]", grandChildren);
        assertEquals("3VCP5", grandChildren.get(0).getCode());
        assertEquals(EnumSet.of(SampleFetchOption.PROPERTIES, SampleFetchOption.CHILDREN),
                grandChildren.get(0).getRetrievedFetchOptions());
        assertEquals("{}", grandChildren.get(0).getProperties().toString());
        assertEntities("[]", grandChildren.get(0).getChildren());
    }

    @Test
    public void testSearchForSamplesByAnyFieldMatchingProperty()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("\"very advanced stuff\""));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/CP-TEST-1]", samples);
    }

    @Test
    public void testSearchForSamplesByAnyFieldWithSubCriteria()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("stuff"));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/CP-TEST-1, /CISD/CP-TEST-2, /CISD/CP-TEST-3]", samples);

        SearchCriteria experimentCriteria = new SearchCriteria();
        experimentCriteria.addMatchClause(MatchClause.createAnyFieldMatch("EXP-TEST-1"));
        searchCriteria.addSubCriteria(SearchSubCriteria
                .createExperimentCriteria(experimentCriteria));

        samples = generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/CP-TEST-1]", samples);
    }

    @Test
    public void testSearchForSamplesByAnyFieldMatchingAttribute()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("\"CP-TEST-2\""));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/CP-TEST-2]", samples);
    }

    @Test
    public void testSearchForSamplesByAnyProperty()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria
                .addMatchClause(MatchClause.createAnyPropertyMatch("\"very advanced stuff\""));

        List<Sample> samples =
                generalInformationService.searchForSamples(sessionToken, searchCriteria);
        assertEntities("[/CISD/CP-TEST-1]", samples);
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
        updates.setVersion(dataSetInfo.getVersion());
        updates.setDatasetId(new TechId(dataSetInfo.getId()));
        updates.setProperties(dataSetInfo.getProperties());
        updates.setExperimentIdentifierOrNull(ExperimentIdentifierFactory.parse(dataSetInfo
                .getExperiment().getIdentifier()));
        updates.setSampleIdentifierOrNull(SampleIdentifierFactory.parse(sample.getIdentifier()));
        commonServer.updateDataSet(sessionToken, updates);
        List<DataSet> dataSets =
                generalInformationService.listDataSetsForSample(sessionToken, sample, true);

        sortDataSets(dataSets);
        assertDataSets("[20081105092159111-1, 20110509092359990-10]", dataSets);
        List<DataSet> containedDataSets = dataSets.get(1).getContainedDataSets();
        sortDataSets(containedDataSets);
        assertEquals("[DataSet[20110509092359990-11,<null>,<null>,HCS_IMAGE,{}], "
                + "DataSet[20110509092359990-12,<null>,<null>,HCS_IMAGE,{}]]",
                containedDataSets.toString());

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

        assertDataSets(
                "[20081105092259000-18, 20081105092259000-19, 20081105092259000-20, 20081105092259000-21, 20081105092259000-8, 20081105092259000-9, "
                        + "20081105092259900-0, 20081105092259900-1, 20081105092359990-2, "
                        + "20110509092359990-10, 20110509092359990-11, 20110509092359990-12, COMPONENT_1A, COMPONENT_1B, COMPONENT_2A, CONTAINER_1, CONTAINER_2, ROOT_CONTAINER]",
                dataSets);
        List<String> parentCodes = new ArrayList<String>(dataSets.get(5).getParentCodes());
        Collections.sort(parentCodes);
        assertEquals("[20081105092159111-1, 20081105092159222-2, 20081105092159333-3]",
                parentCodes.toString());
        List<String> childrenCodes = new ArrayList<String>(dataSets.get(5).getChildrenCodes());
        Collections.sort(childrenCodes);
        assertEquals("[20081105092259900-0, 20081105092259900-1]", childrenCodes.toString());
        DataSet dataSet = dataSets.get(9);
        assertTrue(dataSet.isContainerDataSet());
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
                "[DataSet[CONTAINER_2,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{}], DataSet[20110509092359990-10,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{}], DataSet[20081105092259000-19,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{}], DataSet[ROOT_CONTAINER,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{}], DataSet[CONTAINER_1,/CISD/DEFAULT/EXP-REUSE,<null>,CONTAINER_TYPE,{}]]",
                dataSets.toString());
        List<DataSet> containedDataSets = dataSets.get(1).getContainedDataSets();
        assertEquals("[DataSet[20110509092359990-11,<null>,<null>,HCS_IMAGE,{}], "
                + "DataSet[20110509092359990-12,<null>,<null>,HCS_IMAGE,{}]]",
                containedDataSets.toString());
        assertEquals(1304929379313L, containedDataSets.get(0).getRegistrationDate().getTime());

        loginAsObserver();
        dataSets = generalInformationService.searchForDataSets(sessionToken, searchCriteria);

        assertEquals("[]", dataSets.toString());
    }

    @Test
    public void testSearchForDataSetsOnBehalfOfUser()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "*9000-2*"));

        // executed by test
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertCollection(
                "[20081105092259000-20, 20081105092259000-21, 20120619092259000-22, 20120628092259000-23, 20120628092259000-24, 20120628092259000-25]",
                dataSets, new DataSetToCode());

        // executed by test on behalf of test_space
        List<DataSet> onBehalfOfTestSpaceResult =
                generalInformationService.searchForDataSetsOnBehalfOfUser(sessionToken,
                        searchCriteria, "test_space");
        assertCollection(
                "[20120619092259000-22, 20120628092259000-23, 20120628092259000-24, 20120628092259000-25]",
                onBehalfOfTestSpaceResult, new DataSetToCode());

        // executed by test_space
        generalInformationService.logout(sessionToken);
        sessionToken =
                generalInformationService.tryToAuthenticateForAllServices("test_space", "password");

        List<DataSet> testSpaceResult =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertCollection(
                "[20120619092259000-22, 20120628092259000-23, 20120628092259000-24, 20120628092259000-25]",
                testSpaceResult, new DataSetToCode());
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testSearchForDataSetsOnBehalfOfUserExecutedByNotInstanceUser()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.CODE,
                "SOME_CODE"));

        generalInformationService.logout(sessionToken);
        sessionToken =
                generalInformationService.tryToAuthenticateForAllServices("test_role", "password");
        generalInformationService.searchForDataSetsOnBehalfOfUser(sessionToken, searchCriteria,
                "admin");
    }

    @Test
    public void testSearchForDataSetsOnBehalfOfUserByMetaprojectName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS"));

        // executed by test
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertCollection("[20120619092259000-22]", dataSets, new DataSetToCode());

        // executed by test on behalf of test_space
        List<DataSet> onBehalfOfTestSpaceResult =
                generalInformationService.searchForDataSetsOnBehalfOfUser(sessionToken,
                        searchCriteria, "test_space");
        assertCollection("[20120619092259000-22, 20120628092259000-23]", onBehalfOfTestSpaceResult,
                new DataSetToCode());

        // executed by test_space
        generalInformationService.logout(sessionToken);
        sessionToken =
                generalInformationService.tryToAuthenticateForAllServices("test_space", "password");

        List<DataSet> testSpaceResult =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertCollection("[20120619092259000-22, 20120628092259000-23]", testSpaceResult,
                new DataSetToCode());
    }

    @Test
    public void testSearchForDataSetsByPermId()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.PERM_ID, "20081105092259000-21"));

        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals("[DataSet[20081105092259000-21,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{}]]",
                dataSets.toString());
    }

    @Test
    public void testSearchForDataSetsByMetaprojectName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS"));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(
                "[DataSet[20120619092259000-22,/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST,<null>,HCS_IMAGE,{}]]",
                dataSets.toString());
    }

    @Test
    public void testSearchForDataSetsByMetaprojectNameOwnedBySomebodyElse()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS_2"));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals("[]", dataSets.toString());
    }

    @Test
    public void testSearchForDataSetsByMetaprojectIdentifier()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test/TEST_METAPROJECTS"));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(
                "[DataSet[20120619092259000-22,/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST,<null>,HCS_IMAGE,{}]]",
                dataSets.toString());
    }

    @Test
    public void testSearchForDataSetsByMetaprojectIdentifierOwnedBySomebodyElse()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test/TEST_METAPROJECTS_2"));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals("[]", dataSets.toString());
    }

    @Test
    public void testSearchForDataSetsByAnyFieldMatchingProperty()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("\"non-virtual comment\""));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(
                "[DataSet[20110509092359990-11,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=non-virtual comment}], DataSet[20110509092359990-12,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=non-virtual comment}]]",
                dataSets.toString());
    }

    @Test
    public void testSearchForDataSetsByAnyFieldMatchingAttribute()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("TEST_METAPROJECTS"));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(
                "[DataSet[20120619092259000-22,/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST,<null>,HCS_IMAGE,{}]]",
                dataSets.toString());
    }

    @Test
    public void testSearchForDataSetsByAnyProperty()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria
                .addMatchClause(MatchClause.createAnyPropertyMatch("\"non-virtual comment\""));
        List<DataSet> dataSets =
                generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        assertEquals(
                "[DataSet[20110509092359990-11,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=non-virtual comment}], DataSet[20110509092359990-12,/CISD/DEFAULT/EXP-REUSE,<null>,HCS_IMAGE,{COMMENT=non-virtual comment}]]",
                dataSets.toString());
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
                @Override
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
                @Override
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
        assertEquals("LINK_TYPE", types.get(3).getCode());
        assertEquals("[PropertyTypeGroup[<null>,[]]]", types.get(0).getPropertyTypeGroups()
                .toString());
        assertEquals("UNKNOWN", types.get(4).getCode());
        assertEquals("[PropertyTypeGroup[<null>,[]]]", types.get(4).getPropertyTypeGroups()
                .toString());
        assertEquals(8, types.size());
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

    private interface IToStringDelegate<T>
    {
        String toString(T t);
    }

    private <T> void assertCollection(String expectedEntities, List<T> entities,
            IToStringDelegate<T> toStringMethod)
    {
        List<String> identifiers = new ArrayList<String>();
        for (T entity : entities)
        {
            identifiers.add(toStringMethod.toString(entity));
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

    @Test
    public void testSearchForMaterialsReferencing() throws java.text.ParseException
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "SELF_REF"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[SRM_1, SRM_1A]", materials, new IToStringDelegate<Material>()
            {
                @Override
                public String toString(Material t)
                {
                    return t.getMaterialCode();
                }
            });

        Material srm1 = materials.get(0);
        Material srm1a = materials.get(1);

        assertSelfReferencedMaterials(srm1, srm1a);
    }

    @Test
    public void testSearchMaterialsByCode() throws java.text.ParseException
    {
        List<MaterialIdentifier> materialIdentifiers = new LinkedList<MaterialIdentifier>();

        MaterialTypeIdentifier mt = new MaterialTypeIdentifier("SELF_REF");
        materialIdentifiers.add(new MaterialIdentifier(mt, "SRM_1"));
        materialIdentifiers.add(new MaterialIdentifier(mt, "SRM_1A"));

        List<Material> materials =
                generalInformationService.getMaterialByCodes(sessionToken, materialIdentifiers);

        assertCollection("[SRM_1, SRM_1A]", materials, new IToStringDelegate<Material>()
            {
                @Override
                public String toString(Material t)
                {
                    return t.getMaterialCode();
                }
            });

        Material srm1 = materials.get(0);
        Material srm1a = materials.get(1);

        assertSelfReferencedMaterials(srm1, srm1a);
    }

    private void assertSelfReferencedMaterials(Material srm1, Material srm1a) throws ParseException
    {
        assertEquals("SRM_1", srm1.getMaterialCode());
        assertEquals("SRM_1A", srm1a.getMaterialCode());

        assertEquals("Material with attached material", srm1.getProperties().get("DESCRIPTION"));
        assertEquals("Material wich is attached material", srm1a.getProperties().get("DESCRIPTION"));

        assertEquals("Referenced material should be srm_1a", srm1a.getMaterialCode(), srm1
                .getMaterialProperties().get("ANY_MATERIAL").getMaterialCode());

        assertEquals("Referenced material should be present in properties as code", String.format(
                "%s (%s)", srm1a.getMaterialCode(), srm1a.getMaterialTypeIdentifier()
                        .getMaterialTypeCode()), srm1.getProperties().get("ANY_MATERIAL"));

        Date date2012 = new SimpleDateFormat("yyyy-MM-dd").parse("2012-01-02");
        Date date2013 = new SimpleDateFormat("yyyy-MM-dd").parse("2013-01-02");

        assertEquals("The date should be after 2012", true, srm1.getRegistrationDetails()
                .getRegistrationDate().after(date2012));
        assertEquals("The date should be before 2013", true, srm1.getRegistrationDetails()
                .getRegistrationDate().before(date2013));
    }

    @Test
    public void testSearchForMaterials()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                "BACTERIUM"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[BACTERIUM-X, BACTERIUM-Y, BACTERIUM1, BACTERIUM2]", materials,
                new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByMetaprojectName()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[AD3]", materials, new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByMetaprojectNameOwnedBySomebodyElse()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "TEST_METAPROJECTS_2"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[]", materials, new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByMetaprojectIdentifier()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test/TEST_METAPROJECTS"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[AD3]", materials, new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByMetaprojectIdentifierOwnedBySomebodyElse()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.METAPROJECT, "/test_role/TEST_METAPROJECTS_2"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[]", materials, new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByAnyFieldMatchingProperty()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("\"Influenza A virus\""));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[FLU]", materials, new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByAnyFieldMatchingAttribute()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyFieldMatch("VIRUS1"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[VIRUS1]", materials, new MaterialToCode());
    }

    @Test
    public void testSearchForMaterialsByAnyProperty()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAnyPropertyMatch("\"Influenza A virus\""));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);

        assertCollection("[FLU]", materials, new MaterialToCode());
    }

    @Test
    public void testSearchMaterialsByModificationDate()
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createTimeAttributeMatch(
                MatchClauseTimeAttribute.MODIFICATION_DATE, CompareMode.LESS_THAN_OR_EQUAL,
                "2009-03-19", "+1"));

        List<Material> materials =
                generalInformationService.searchForMaterials(sessionToken, searchCriteria);
        assertEquals(3732, materials.size());

        searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createTimeAttributeMatch(
                MatchClauseTimeAttribute.MODIFICATION_DATE, CompareMode.GREATER_THAN_OR_EQUAL,
                "2009-03-19", "+1"));

        materials = generalInformationService.searchForMaterials(sessionToken, searchCriteria);
        assertEquals(2, materials.size());
    }

    @Test
    public void testListMetaprojects()
    {
        List<Metaproject> metaprojects = generalInformationService.listMetaprojects(sessionToken);

        assertEquals(2, metaprojects.size());
    }

    @Test(expectedExceptions = AuthorizationFailureException.class)
    public void testGetMetaprojectOwnedBySomebodyElse()
    {
        generalInformationService.getMetaproject(sessionToken, new MetaprojectIdentifierId(
                "/test_role/TEST_METAPROJECTS"));
    }

    @Test
    public void testGetMetaprojects()
    {
        MetaprojectAssignments metaprojectAssignments =
                generalInformationService.getMetaproject(sessionToken, new MetaprojectIdentifierId(
                        "/test/TEST_METAPROJECTS"));

        assertEquals(1, metaprojectAssignments.getDataSets().size());
        assertEquals(1, metaprojectAssignments.getMaterials().size());
        assertEquals(1, metaprojectAssignments.getSamples().size());
        assertEquals(2, metaprojectAssignments.getExperiments().size());

        generalInformationService.logout(sessionToken);
        sessionToken = generalInformationService.tryToAuthenticateForAllServices("test_role", "a");

        metaprojectAssignments =
                generalInformationService.getMetaproject(sessionToken, new MetaprojectIdentifierId(
                        "/test_role/TEST_METAPROJECTS"));

        assertEquals(1, metaprojectAssignments.getMaterials().size());
        assertEquals(1, metaprojectAssignments.getDataSets().size());
        assertTrue(metaprojectAssignments.getDataSets().get(0).isStub());
        assertTrue(metaprojectAssignments.getDataSets().get(0).toString().contains("STUB"));
        assertEquals(1, metaprojectAssignments.getSamples().size());
        assertTrue(metaprojectAssignments.getSamples().get(0).isStub());
        assertTrue(metaprojectAssignments.getSamples().get(0).toString().contains("STUB"));
        assertEquals(1, metaprojectAssignments.getExperiments().size());
        assertTrue(metaprojectAssignments.getExperiments().get(0).isStub());
        assertTrue(metaprojectAssignments.getExperiments().get(0).toString().contains("STUB"));
    }

    @Test
    public void testGetMetaprojectReturnsMetaprojectForNameWithDifferentCase()
    {
        MetaprojectAssignments metaprojectAssignments =
                generalInformationService.getMetaproject(sessionToken, new MetaprojectIdentifierId(
                        "/test/TEST_metaprojects"));
        assertEquals("/test/TEST_METAPROJECTS", metaprojectAssignments.getMetaproject()
                .getIdentifier());
        assertEquals("TEST_METAPROJECTS", metaprojectAssignments.getMetaproject().getName());
    }

    private void sortDataSets(List<DataSet> dataSets)
    {
        Collections.sort(dataSets, new Comparator<DataSet>()
            {
                @Override
                public int compare(DataSet d1, DataSet d2)
                {
                    return d1.getCode().compareTo(d2.getCode());
                }
            });
    }

    private static class MaterialToCode implements IToStringDelegate<Material>
    {
        @Override
        public String toString(Material t)
        {
            return t.getMaterialCode();
        }
    }

    private static class DataSetToCode implements IToStringDelegate<DataSet>
    {
        @Override
        public String toString(DataSet t)
        {
            return t.getCode();
        }
    }

}
