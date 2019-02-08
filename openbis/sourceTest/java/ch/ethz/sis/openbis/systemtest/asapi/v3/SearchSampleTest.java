/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.CacheMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagCode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.systemtest.authorization.ProjectAuthorizationUser;

/**
 * @author pkupczyk
 */
public class SearchSampleTest extends AbstractSampleTest
{

    @Test
    public void testSearchWhichReturnsSharedSamplesForSpaceUser()
    {
        SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
        sampleSearchCriteria.withCode().thatEndsWith("P");
        testSearch(TEST_SPACE_USER, sampleSearchCriteria, "/DP", "/MP");
    }

    @Test
    public void testSearchWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-1"));
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withId().thatEquals(new SamplePermId("200902091219327-1025"));
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithMultipleIds()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-1"));
        criteria.withId().thatEquals(new SamplePermId("200902091250077-1026"));
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2");
    }

    @Test
    public void testSearchWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withPermId().thatEquals("200902091219327-1025");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithIdentifierThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withIdentifier().thatEquals("/CISD/CP-TEST-1");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");

        SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withIdentifier().thatEquals("/CISD/CP-TEST-*");
        testSearch(TEST_USER, criteria2, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3");

        SampleSearchCriteria criteria3 = new SampleSearchCriteria();
        criteria3.withIdentifier().thatEquals("/CISD/CP-*-1");
        testSearch(TEST_USER, criteria3, "/CISD/CP-TEST-1");

        SampleSearchCriteria criteria4 = new SampleSearchCriteria();
        criteria4.withIdentifier().thatEquals("/*/CP-TEST-*");
        testSearch(TEST_USER, criteria4, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3", "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithIdentifierThatStartsWith()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withIdentifier().thatStartsWith("/CISD/CP-TEST");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3");

        SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withIdentifier().thatStartsWith("/CISD/*-TEST");
        testSearch(TEST_USER, criteria2, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithIdentifierThatEndsWith()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withIdentifier().thatEndsWith("-TEST-1");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/DYNA-TEST-1");

        SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withIdentifier().thatEndsWith("-TEST-*");
        testSearch(TEST_USER, criteria2, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3", "/TEST-SPACE/CP-TEST-4", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithIdentifierThatContains()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withIdentifier().thatContains("CP-TEST");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3", "/TEST-SPACE/CP-TEST-4");

        SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withIdentifier().thatContains("CISD*-TEST");
        testSearch(TEST_USER, criteria2, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEquals("RP1-A2X");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X");
    }

    @Test
    public void testSearchWithCodeThatEqualsWithStarWildcard()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEquals("RP1-*X");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP1-B1X");
    }

    @Test
    public void testSearchWithCodeThatEqualsWithQuestionMarkWildcard()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEquals("CP???1");
        testSearch(TEST_USER, criteria, "/CISD/CP1-A1", "/CISD/CP1-B1", "/CISD/CP2-A1");
    }

    @Test
    public void testSearchWithCodeThatStartsWithStarWildcard()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatStartsWith("PLATE_WELLSEARCH:W*L-");
        testSearch(TEST_USER, criteria, "/CISD/PLATE_WELLSEARCH:WELL-A01", "/CISD/PLATE_WELLSEARCH:WELL-A02");
    }

    @Test
    public void testSearchWithCodeThatStartsWithQuestionMarkWildcard()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatStartsWith("CP?-");
        testSearch(TEST_USER, criteria, "/CISD/CP1-A1", "/CISD/CP1-A2", "/CISD/CP1-B1", "/CISD/CP2-A1");
    }

    @Test
    public void testSearchWithCodeThatEndsWithStarWildcard()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEndsWith("NOR*L");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/EV-PARENT-NORMAL");
    }

    @Test
    public void testSearchWithCodeThatEndsWithQuestionMarkWildcard()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEndsWith("-??2");
        testSearch(TEST_USER, criteria, "/CISD/PLATE_WELLSEARCH:WELL-A02");
    }

    @Test
    public void testSearchForAll()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        testSearch(TEST_USER, criteria, 701);
    }

    @Test
    public void testSearchForAllSpaceSamples()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withCode();
        testSearch(TEST_USER, criteria, 379);
    }

    @Test
    public void testSearchForAllSharedSamples()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria().withoutSpace();
        testSearch(TEST_USER, criteria, 322);
    }

    @Test
    public void testSearchWithSpaceWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withId().thatEquals(new SpacePermId("TEST-SPACE"));
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithSpaceWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withCode().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithSpaceWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withSpace().withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);

        criteria = new SampleSearchCriteria();
        criteria.withSpace().withPermId().thatEquals("/TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithSpaceUnauthorized()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withPermId().thatEquals("200902091219327-1025");
        testSearch(TEST_USER, criteria, 1);

        criteria = new SampleSearchCriteria();
        criteria.withPermId().thatEquals("200902091219327-1025");
        testSearch(TEST_SPACE_USER, criteria, 0);
    }

    @Test
    public void testSearchWithCodeInContainer()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatEquals("PLATE_WELLSEARCH:WELL-A01");
        testSearch(TEST_USER, criteria, "/CISD/PLATE_WELLSEARCH:WELL-A01");
    }

    @Test
    public void testSearchWithTypeIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withId().thatEquals(new EntityTypePermId("REINFECT_PLATE"));
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP1-B1X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithTypeWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withCode().thatEquals("REINFECT_PLATE");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP1-B1X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithTypeWithCodeWithWildcard()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withCode().thatEquals("REINFECT_PLAT*");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP1-B1X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithTypeWithSemanticAnnotationsWithIdThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withSemanticAnnotations().withId().thatEquals(new SemanticAnnotationPermId("ST_DILUTION_PLATE"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = search(sessionToken, criteria, fo);

        assertEquals(samples.size(), 6, samples.toString());
        for (Sample sample : samples)
        {
            assertEquals(sample.getType().getCode(), "DILUTION_PLATE");
        }
    }

    @Test
    public void testSearchWithTypeWithSemanticAnnotationsWithIdOrIdAndWithSampleCodeThatContains()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("P2");

        SemanticAnnotationSearchCriteria annotationCriteria = criteria.withType().withSemanticAnnotations();
        annotationCriteria.withOrOperator();
        annotationCriteria.withId().thatEquals(new SemanticAnnotationPermId("ST_MASTER_PLATE"));
        annotationCriteria.withId().thatEquals(new SemanticAnnotationPermId("ST_DILUTION_PLATE"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = search(sessionToken, criteria, fo);

        assertEquals(samples.size(), 2, samples.toString());
        for (Sample sample : samples)
        {
            AssertionUtil.assertContains("P2", sample.getCode());
            AssertionUtil.assertCollectionContains(Arrays.asList("MASTER_PLATE", "DILUTION_PLATE"), sample.getType().getCode());
        }
    }

    @Test
    public void testSearchWithTypeWithPropertyAssignmentsWithSemanticAnnotationsWithIdThatEqualsWhereSemanticAnnotationIsDefinedAtPropertyAssignment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withPropertyAssignments().withSemanticAnnotations().withId()
                .thatEquals(new SemanticAnnotationPermId("ST_CELL_PLATE_PT_ORGANISM"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = search(sessionToken, criteria, fo);

        assertEquals(samples.size(), 14, samples.toString());
        for (Sample sample : samples)
        {
            assertEquals(sample.getType().getCode(), "CELL_PLATE");
        }
    }

    @Test
    public void testSearchWithTypeWithPropertyAssignmentsWithSemanticAnnotationsWithIdThatEqualsWhereSemanticAnnotationIsDefinedAtPropertyTypeLevelOnly()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withPropertyAssignments().withSemanticAnnotations().withId()
                .thatEquals(new SemanticAnnotationPermId("PT_ORGANISM"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = search(sessionToken, criteria, fo);

        assertEquals(samples.size(), 2, samples.toString());
        for (Sample sample : samples)
        {
            AssertionUtil.assertCollectionContains(Arrays.asList("DELETION_TEST", "NORMAL"), sample.getType().getCode());
        }
    }

    @Test
    public void testSearchWithTypeWithPropertyAssignmentsWithPropertyTypeWithSemanticAnnotationsWithIdThatEquals()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withPropertyAssignments().withPropertyType().withSemanticAnnotations().withId()
                .thatEquals(new SemanticAnnotationPermId("PT_DESCRIPTION"));

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        List<Sample> samples = search(sessionToken, criteria, fo);

        assertEquals(samples.size(), 11, samples.toString());
        for (Sample sample : samples)
        {
            AssertionUtil.assertCollectionContains(Arrays.asList("MASTER_PLATE", "CONTROL_LAYOUT", "DELETION_TEST"), sample.getType().getCode());
        }
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withType().withPermId().thatEquals("REINFECT_PLATE");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP1-B1X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithExperiment()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("A01");
        criteria.withExperiment();
        testSearch(TEST_USER, criteria, "/CISD/PLATE_WELLSEARCH:WELL-A01");
    }

    @Test
    public void testSearchWithoutExperiment()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("A01");
        criteria.withoutExperiment();
        testSearch(TEST_USER, criteria, "/CISD/CL1:A01", "/CISD/MP2-NO-CL:A01", "/CISD/CL-3V:A01", "/CISD/MP1-MIXED:A01");
    }

    @Test
    public void testSearchWithExperimentWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withId().thatEquals(new ExperimentIdentifier("/CISD/NEMO/EXP10"));
        testSearch(TEST_USER, criteria, "/CISD/3VCP5");
    }

    @Test
    public void testSearchWithExperimentWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withId().thatEquals(new ExperimentPermId("200811050952663-1029"));
        testSearch(TEST_USER, criteria, "/CISD/3VCP5");
    }

    @Test
    public void testSearchWithExperimentWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withPermId().thatEquals("200811050952663-1029");
        testSearch(TEST_USER, criteria, "/CISD/3VCP5");
    }

    @Test
    public void testSearchWithExperimentWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withCode().thatEquals("EXP-TEST-1");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithTypeIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withType().withId().thatEquals(new EntityTypePermId("COMPOUND_HCS"));
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithTypeWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withType().withCode().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithTypeWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withType().withPermId().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/NOE"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withId().thatEquals(new ProjectPermId("20120814110011738-106"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withCode().thatEquals("NOE");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-2", "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withId().thatEquals(new ProjectPermId("20120814110011738-106"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithSpaceWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withId().thatEquals(new SpacePermId("TEST-SPACE"));
        testSearch(TEST_USER, criteria, 8);

        criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withId().thatEquals(new SpacePermId("/TEST-SPACE"));
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithExperimentWithProjectWithSpaceWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withCode().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithExperimentWithProjectWithSpaceWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);

        criteria = new SampleSearchCriteria();
        criteria.withExperiment().withProject().withSpace().withPermId().thatEquals("/TEST-SPACE");
        testSearch(TEST_USER, criteria, 8);
    }

    @Test
    public void testSearchWithParentWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents().withId().thatEquals(new SampleIdentifier("/CISD/MP002-1"));
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithParentWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents().withId().thatEquals(new SamplePermId("200811050917877-331"));
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithParentWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents().withPermId().thatEquals("200811050917877-331");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithParentWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withParents().withCode().thatEquals("MP002-1");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithChildrenWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withChildren().withId().thatEquals(new SampleIdentifier("/CISD/3VCP6"));
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithChildrenWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withChildren().withId().thatEquals(new SamplePermId("200811050946559-980"));
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithChildrenWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withChildren().withPermId().thatEquals("200811050946559-980");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithChildrenWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withChildren().withCode().thatEquals("3VCP6");
        testSearch(TEST_USER, criteria, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithContainer()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("WELL");
        criteria.withContainer();
        testSearch(TEST_USER, criteria, "/CISD/PLATE_WELLSEARCH:WELL-A01", "/CISD/PLATE_WELLSEARCH:WELL-A02");
    }

    @Test
    public void testSearchWithoutContainer()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("A1");
        criteria.withoutContainer();
        testSearch(TEST_USER, criteria, "/CISD/CP1-A1", "/CISD/CP2-A1", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithContainerWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer().withId().thatEquals(new SamplePermId("200811050924274-994"));
        testSearch(TEST_USER, criteria, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithContainerWithIdSetToIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer().withId().thatEquals(new SampleIdentifier("/CISD/B1B3"));
        testSearch(TEST_USER, criteria, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithContainerWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer().withPermId().thatEquals("200811050924274-994");
        testSearch(TEST_USER, criteria, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithContainerWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withContainer().withCode().thatEquals("B1B3");
        testSearch(TEST_USER, criteria, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithTagWithIdSetToPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithIdSetToCodeId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withId().thatEquals(new TagCode("TEST_METAPROJECTS"));
        testSearch(TEST_USER, criteria, "/TEST-SPACE/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withCode().thatEquals("TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithPermId()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withPermId().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_USER, criteria, "/TEST-SPACE/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithPermIdUnauthorized()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withTag().withCode().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_SPACE_USER, criteria, 0);
    }

    @Test
    public void testSearchWithRegistratorWithUserIdThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrator().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithRegistratorWithFirstNameThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrator().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithRegistratorWithLastNameThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrator().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithRegistratorWithEmailThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrator().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria, "/CISD/RP1-A2X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithModifierWithUserIdThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModifier().withUserId().thatEquals("etlserver");
        testSearch(TEST_USER, criteria, "/CISD/RP1-B1X");
    }

    @Test
    public void testSearchWithModifierWithFirstNameThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModifier().withFirstName().thatEquals("John 2");
        testSearch(TEST_USER, criteria, "/CISD/RP1-B1X");
    }

    @Test
    public void testSearchWithModifierWithLastNameThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModifier().withLastName().thatEquals("ETL Server");
        testSearch(TEST_USER, criteria, "/CISD/RP1-B1X");
    }

    @Test
    public void testSearchWithModifierWithEmailThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModifier().withEmail().thatEquals("etlserver@systemsx.ch");
        testSearch(TEST_USER, criteria, "/CISD/RP1-B1X");
    }

    @Test
    public void testSearchWithRegistrationDateThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withRegistrationDate().thatEquals("2009-02-09");
        testSearch(TEST_USER, criteria, 15);
    }

    @Test
    public void testSearchWithModificationDateThatEquals()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withModificationDate().thatEquals("2009-08-18");
        testSearch(TEST_USER, criteria, 14);
    }

    @Test
    public void testSearchWithAnyFieldMatchingProperty()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyField().thatEquals("\"very advanced stuff\"");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithAnyFieldMatchingAttribute()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyField().thatEquals("\"CP-TEST-2\"");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-2");
    }

    @Test
    public void testSearchWithAnyFieldMatchingIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyField().thatEquals("/CISD/CP-TEST-*");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3");

        SampleSearchCriteria criteria2 = new SampleSearchCriteria();
        criteria2.withAnyField().thatStartsWith("/CISD/DYNA");
        testSearch(TEST_USER, criteria2, "/CISD/DYNA-TEST-1");

        SampleSearchCriteria criteria3 = new SampleSearchCriteria();
        criteria3.withAnyField().thatEndsWith("-1");
        testSearch(TEST_USER, criteria3, "/CISD/CP-TEST-1", "/CISD/DYNA-TEST-1", "/CISD/MP002-1");
    }

    @Test
    public void testSearchWithAnyProperty()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAnyProperty().thatEquals("\"very advanced\"");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withAndOperator();
        criteria.withCode().thatStartsWith("CP");
        criteria.withCode().thatEndsWith("-1");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091250077-1026");
        testSearch(TEST_USER, criteria, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2");
    }

    @Test
    public void testSearchWithCachingNoCache()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091250077-1026");

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.cacheMode(CacheMode.NO_CACHE);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        v3api.logout(sessionToken);

        assertEquals(samples1.get(0).getPermId(), samples2.get(0).getPermId());
        assertEquals(samples1.get(1).getPermId(), samples2.get(1).getPermId());

        assertNotSame(samples1.get(0), samples2.get(0));
        assertNotSame(samples1.get(1), samples2.get(1));
    }

    @Test
    public void testSearchWithCachingCache()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091250077-1026");

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.cacheMode(CacheMode.CACHE);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        v3api.logout(sessionToken);

        assertEquals(samples1.get(0).getPermId(), samples2.get(0).getPermId());
        assertEquals(samples1.get(1).getPermId(), samples2.get(1).getPermId());

        assertSame(samples1.get(0), samples2.get(0));
        assertSame(samples1.get(1), samples2.get(1));
    }

    @Test
    public void testSearchWithCachingReloadAndCache()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091250077-1026");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fo = new SampleFetchOptions();

        fo.cacheMode(CacheMode.CACHE);
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        List<Sample> samples2 = search(sessionToken, criteria, fo);

        fo.cacheMode(CacheMode.RELOAD_AND_CACHE);
        List<Sample> samples3 = search(sessionToken, criteria, fo);

        fo.cacheMode(CacheMode.CACHE);
        List<Sample> samples4 = search(sessionToken, criteria, fo);

        v3api.logout(sessionToken);

        assertEquals(samples1.get(0).getPermId(), samples2.get(0).getPermId());
        assertEquals(samples1.get(1).getPermId(), samples2.get(1).getPermId());
        assertSame(samples1.get(0), samples2.get(0));
        assertSame(samples1.get(1), samples2.get(1));

        assertEquals(samples3.get(0).getPermId(), samples2.get(0).getPermId());
        assertEquals(samples3.get(1).getPermId(), samples2.get(1).getPermId());
        assertNotSame(samples3.get(0), samples2.get(0));
        assertNotSame(samples3.get(1), samples2.get(1));

        assertEquals(samples4.get(0).getPermId(), samples3.get(0).getPermId());
        assertEquals(samples4.get(1).getPermId(), samples3.get(1).getPermId());
        assertSame(samples4.get(0), samples3.get(0));
        assertSame(samples4.get(1), samples3.get(1));
    }

    @Test
    public void testSearchWithSortingByCode()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-1"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-2"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-3"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().code().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3");

        fo.sortBy().code().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/CP-TEST-3", "/CISD/CP-TEST-2", "/CISD/CP-TEST-1");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByCodeScore()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withCode().thatContains("CP-TEST");
        criteria.withCode().thatContains("TEST-1");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().fetchedFieldsScore().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertTrue(samples1.get(0).getCode().equals("CP-TEST-1"));

        fo.sortBy().fetchedFieldsScore().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertTrue(samples2.get(samples1.size() - 1).getCode().equals("CP-TEST-1"));

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByIdentifier()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SampleIdentifier("/TEST-SPACE/CP-TEST-4"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-1"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/3V-125"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().identifier().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/3V-125", "/CISD/CP-TEST-1", "/TEST-SPACE/CP-TEST-4");

        fo.sortBy().identifier().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/TEST-SPACE/CP-TEST-4", "/CISD/CP-TEST-1", "/CISD/3V-125");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByType()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withId().thatEquals(new SampleIdentifier("/TEST-SPACE/CP-TEST-4"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-1"));
        criteria.withId().thatEquals(new SampleIdentifier("/CISD/3V-125"));

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withType();

        fo.sortBy().type().asc();
        fo.sortBy().code().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/CP-TEST-1", "/TEST-SPACE/CP-TEST-4", "/CISD/3V-125");

        fo.sortBy().type().desc();
        fo.sortBy().code().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/3V-125", "/TEST-SPACE/CP-TEST-4", "/CISD/CP-TEST-1");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByPropertyWithTextValues()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withProperties();

        fo.sortBy().property("COMMENT").asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertEquals(samples1.get(0).getProperty("COMMENT"), "extremely simple stuff");
        assertEquals(samples1.get(1).getProperty("COMMENT"), "stuff like others");
        assertEquals(samples1.get(2).getProperty("COMMENT"), "very advanced stuff");

        fo.sortBy().property("COMMENT").desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertEquals(samples2.get(0).getProperty("COMMENT"), "very advanced stuff");
        assertEquals(samples2.get(1).getProperty("COMMENT"), "stuff like others");
        assertEquals(samples2.get(2).getProperty("COMMENT"), "extremely simple stuff");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByPropertyWithIntegerValues()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");
        criteria.withPermId().thatEquals("200811050946559-981");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withProperties();

        fo.sortBy().property("SIZE").asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertEquals(samples1.get(0).getProperty("SIZE"), "123");
        assertEquals(samples1.get(1).getProperty("SIZE"), "321");
        assertEquals(samples1.get(2).getProperty("SIZE"), "666");
        assertEquals(samples1.get(3).getProperty("SIZE"), "4711");

        fo.sortBy().property("SIZE").desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertEquals(samples2.get(0).getProperty("SIZE"), "4711");
        assertEquals(samples2.get(1).getProperty("SIZE"), "666");
        assertEquals(samples2.get(2).getProperty("SIZE"), "321");
        assertEquals(samples2.get(3).getProperty("SIZE"), "123");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByRegistrationDate()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050924274-995");
        criteria.withPermId().thatEquals("200811050927630-1004");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().registrationDate().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/B1B3:B01", "/CISD/MP1-MIXED:A01");

        fo.sortBy().registrationDate().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/MP1-MIXED:A01", "/CISD/B1B3:B01");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByModificationDate()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1053");
        criteria.withPermId().thatEquals("200811050928301-1012");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().modificationDate().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/MP2-NO-CL:B02", "/CISD/DYNA-TEST-1");

        fo.sortBy().modificationDate().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/DYNA-TEST-1", "/CISD/MP2-NO-CL:B02");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingByMultipleFields()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050919915-9");
        criteria.withPermId().thatEquals("200811050944030-974");
        criteria.withPermId().thatEquals("200811050924274-995");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().code().asc();
        fo.sortBy().registrationDate().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/CL1:A01", "/CISD/CL-3V:A01", "/CISD/B1B3:B01");

        fo.sortBy().code().asc();
        fo.sortBy().registrationDate().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/CL-3V:A01", "/CISD/CL1:A01", "/CISD/B1B3:B01");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingTopLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();

        fo.sortBy().code().asc();
        List<Sample> samples1 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples1, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3");

        fo.sortBy().code().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples2, "/CISD/CP-TEST-3", "/CISD/CP-TEST-2", "/CISD/CP-TEST-1");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithSortingSubLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050945092-976");
        criteria.withPermId().thatEquals("200811050927630-1003");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.sortBy().code().asc();
        fo.withChildren().sortBy().code().asc();

        List<Sample> samples1 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples1, "/CISD/3V-125", "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples1.get(0).getChildren(), "/CISD/3VCP5", "/CISD/3VCP6", "/CISD/3VCP7", "/CISD/3VCP8");
        assertSampleIdentifiersInOrder(samples1.get(1).getChildren(), "/CISD/DP1-A", "/CISD/DP1-B");

        fo.withChildren().sortBy().code().desc();
        List<Sample> samples2 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples2, "/CISD/3V-125", "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples2.get(0).getChildren(), "/CISD/3VCP8", "/CISD/3VCP7", "/CISD/3VCP6", "/CISD/3VCP5");
        assertSampleIdentifiersInOrder(samples2.get(1).getChildren(), "/CISD/DP1-B", "/CISD/DP1-A");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPagingTopLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200902091219327-1025");
        criteria.withPermId().thatEquals("200902091225616-1027");
        criteria.withPermId().thatEquals("200902091250077-1026");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.sortBy().code().asc();

        fo.from(0).count(1);
        List<Sample> samples1 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples1, "/CISD/CP-TEST-1");

        fo.from(1).count(1);
        List<Sample> samples2 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples2, "/CISD/CP-TEST-2");

        fo.from(2).count(1);
        List<Sample> samples3 = search(sessionToken, criteria, fo);
        assertSampleIdentifiersInOrder(samples3, "/CISD/CP-TEST-3");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPagingSubLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050945092-976");
        criteria.withPermId().thatEquals("200811050927630-1003");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.sortBy().code().asc();
        fo.withChildren().sortBy().code().asc();

        fo.withChildren().from(0).count(1);
        List<Sample> samples1 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples1, "/CISD/3V-125", "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples1.get(0).getChildren(), "/CISD/3VCP5");
        assertSampleIdentifiersInOrder(samples1.get(1).getChildren(), "/CISD/DP1-A");

        fo.withChildren().from(1).count(1);
        List<Sample> samples2 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples2, "/CISD/3V-125", "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples2.get(0).getChildren(), "/CISD/3VCP6");
        assertSampleIdentifiersInOrder(samples2.get(1).getChildren(), "/CISD/DP1-B");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchWithPagingTopAndSubLevel()
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withOrOperator();
        criteria.withPermId().thatEquals("200811050945092-976");
        criteria.withPermId().thatEquals("200811050927630-1003");

        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.sortBy().code().asc();
        fo.withChildren().sortBy().code().asc();

        fo.from(0).count(1);
        fo.withChildren().from(0).count(1);
        List<Sample> samples1 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples1, "/CISD/3V-125");
        assertSampleIdentifiersInOrder(samples1.get(0).getChildren(), "/CISD/3VCP5");

        fo.from(1).count(1);
        fo.withChildren().from(1).count(1);
        List<Sample> samples2 = search(sessionToken, criteria, fo);

        assertSampleIdentifiersInOrder(samples2, "/CISD/MP1-MIXED");
        assertSampleIdentifiersInOrder(samples2.get(0).getChildren(), "/CISD/DP1-B");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchNumeric()
    {
        // SIZE: 4711 CODE: 3VCP7
        // SIZE: 123 CODE: CP-TEST-1
        // SIZE: 321 CODE: CP-TEST-2
        // SIZE: 666 CODE: CP-TEST-3

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions sortByCodeFO = new SampleFetchOptions();
        sortByCodeFO.sortBy().code().asc();
        sortByCodeFO.withProperties();

        // Greater or Equals - Giving integer as real
        SampleSearchCriteria criteriaGOE = new SampleSearchCriteria();
        criteriaGOE.withNumberProperty("SIZE").thatIsGreaterThanOrEqualTo(321.0);
        List<Sample> samplesGOE = search(sessionToken, criteriaGOE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesGOE, "/CISD/3VCP7", "/CISD/CP-TEST-2", "/CISD/CP-TEST-3");

        // Greater - Giving integer as real
        SampleSearchCriteria criteriaG = new SampleSearchCriteria();
        criteriaG.withNumberProperty("SIZE").thatIsGreaterThan(321.0);
        List<Sample> samplesG = search(sessionToken, criteriaG, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesG, "/CISD/3VCP7", "/CISD/CP-TEST-3");

        // Equals As Text - Real
        SampleSearchCriteria criteriaETxt2 = new SampleSearchCriteria();
        criteriaETxt2.withProperty("SIZE").thatEquals("666.0");
        List<Sample> samplesETxt2 = search(sessionToken, criteriaETxt2, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesETxt2, "/CISD/CP-TEST-3");

        // Equals As Text - Integer
        SampleSearchCriteria criteriaETxt = new SampleSearchCriteria();
        criteriaETxt.withProperty("SIZE").thatEquals("666");
        List<Sample> samplesETxt = search(sessionToken, criteriaETxt, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesETxt, "/CISD/CP-TEST-3");

        // Equals
        SampleSearchCriteria criteriaE = new SampleSearchCriteria();
        criteriaE.withNumberProperty("SIZE").thatEquals(666);
        List<Sample> samplesE = search(sessionToken, criteriaE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesE, "/CISD/CP-TEST-3");

        // Less
        SampleSearchCriteria criteriaL = new SampleSearchCriteria();
        criteriaL.withNumberProperty("SIZE").thatIsLessThan(666);
        List<Sample> samplesL = search(sessionToken, criteriaL, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesL, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2");

        // Less or Equals
        SampleSearchCriteria criteriaLOE = new SampleSearchCriteria();
        criteriaLOE.withNumberProperty("SIZE").thatIsLessThanOrEqualTo(321);
        List<Sample> samplesLOE = search(sessionToken, criteriaLOE, sortByCodeFO);
        assertSampleIdentifiersInOrder(samplesLOE, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2");

        v3api.logout(sessionToken);
    }

    @Test
    public void testSearchListableOnlyShouldNotFindUnlistable()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fo = new SampleFetchOptions();
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatStartsWith("EV");
        List<Sample> samples = search(sessionToken, criteria, fo);
        List<String> identifiers = extractIndentifiers(samples);
        Collections.sort(identifiers);
        assertEquals(identifiers.toString(), "[/TEST-SPACE/EV-INVALID, /TEST-SPACE/EV-NOT_INVALID, "
                + "/TEST-SPACE/EV-PARENT, /TEST-SPACE/EV-PARENT-NORMAL, /TEST-SPACE/EV-TEST]");
        criteria.withType().withListable().thatEquals(true);

        samples = search(sessionToken, criteria, fo);

        identifiers = extractIndentifiers(samples);
        Collections.sort(identifiers);
        assertEquals(identifiers.toString(), "[/TEST-SPACE/EV-INVALID, /TEST-SPACE/EV-PARENT, "
                + "/TEST-SPACE/EV-PARENT-NORMAL, /TEST-SPACE/EV-TEST]");
    }

    @Test
    public void testSearchUnlistableOnlyShouldFindListable()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SampleFetchOptions fo = new SampleFetchOptions();
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatStartsWith("EV");
        List<Sample> samples = search(sessionToken, criteria, fo);
        List<String> identifiers = extractIndentifiers(samples);
        Collections.sort(identifiers);
        assertEquals(identifiers.toString(), "[/TEST-SPACE/EV-INVALID, /TEST-SPACE/EV-NOT_INVALID, "
                + "/TEST-SPACE/EV-PARENT, /TEST-SPACE/EV-PARENT-NORMAL, /TEST-SPACE/EV-TEST]");
        criteria.withType().withListable().thatEquals(false);

        samples = search(sessionToken, criteria, fo);

        identifiers = extractIndentifiers(samples);
        Collections.sort(identifiers);
        assertEquals(identifiers.toString(), "[/TEST-SPACE/EV-NOT_INVALID]");
    }

    @Test(dataProviderClass = ProjectAuthorizationUser.class, dataProvider = ProjectAuthorizationUser.PROVIDER_WITH_ETL)
    public void testSearchWithProjectAuthorization(ProjectAuthorizationUser user)
    {
        SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withId().thatEquals(new SampleIdentifier("/TEST-SPACE/EV-TEST"));

        if (user.isDisabledProjectUser())
        {
            assertAuthorizationFailureException(new IDelegatedAction()
                {
                    @Override
                    public void execute()
                    {
                        testSearch(user.getUserId(), criteria);
                    }
                });
        } else if (user.isInstanceUserOrTestSpaceUserOrEnabledTestProjectUser())
        {
            testSearch(user.getUserId(), criteria, "/TEST-SPACE/EV-TEST");
        } else
        {
            testSearch(user.getUserId(), criteria);
        }
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleSearchCriteria c = new SampleSearchCriteria();
        c.withCode().thatEquals("RP1-A2X");

        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withSpace();
        fo.withProject();

        v3api.searchSamples(sessionToken, c, fo);

        assertAccessLog(
                "search-samples  SEARCH_CRITERIA:\n'SAMPLE\n    with attribute 'code' equal to 'RP1-A2X'\n'\nFETCH_OPTIONS:\n'Sample\n    with Project\n    with Space\n'");
    }

    private void testSearch(String user, SampleSearchCriteria criteria, String... expectedIdentifiers)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        List<Sample> samples = search(sessionToken, criteria, new SampleFetchOptions());
        assertSampleIdentifiers(samples, expectedIdentifiers);
        v3api.logout(sessionToken);
    }

    private void testSearch(String user, SampleSearchCriteria criteria, int expectedCount)
    {
        String sessionToken = v3api.login(user, PASSWORD);
        List<Sample> samples = search(sessionToken, criteria, new SampleFetchOptions());
        assertEquals(samples.size(), expectedCount);
        v3api.logout(sessionToken);
    }

    private List<Sample> search(String sessionToken, SampleSearchCriteria criteria, SampleFetchOptions fetchOptions)
    {
        SearchResult<Sample> searchResult =
                v3api.searchSamples(sessionToken, criteria, fetchOptions);
        return searchResult.getObjects();
    }

    private List<String> extractIndentifiers(List<Sample> samples)
    {
        List<String> identifiers = new ArrayList<>();
        for (Sample sample : samples)
        {
            identifiers.add(sample.getIdentifier().getIdentifier());
        }
        return identifiers;
    }

}
