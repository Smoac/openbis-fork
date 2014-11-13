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

package ch.ethz.sis.openbis.systemtest.api.v3;

import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagCode;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriterion;

/**
 * @author pkupczyk
 */
public class SearchSampleTest extends AbstractSampleTest
{

    @Test
    public void testSearchWithIdSetToIdentifier()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-1"));
        testSearch(TEST_USER, criterion, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withId().thatEquals(new SamplePermId("200902091219327-1025"));
        testSearch(TEST_USER, criterion, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithMultipleIds()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withOrOperator();
        criterion.withId().thatEquals(new SampleIdentifier("/CISD/CP-TEST-1"));
        criterion.withId().thatEquals(new SamplePermId("200902091250077-1026"));
        testSearch(TEST_USER, criterion, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2");
    }

    @Test
    public void testSearchWithPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withPermId().thatEquals("200902091219327-1025");
        testSearch(TEST_USER, criterion, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithCode()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withCode().thatEquals("RP1-A2X");
        testSearch(TEST_USER, criterion, "/CISD/RP1-A2X");
    }

    @Test
    public void testSearchWithSpaceWithIdSetToPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withSpace().withId().thatEquals(new SpacePermId("TEST-SPACE"));
        testSearch(TEST_USER, criterion, 8);
    }

    @Test
    public void testSearchWithSpaceWithCode()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withSpace().withCode().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criterion, 8);
    }

    @Test
    public void testSearchWithSpaceWithPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withSpace().withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criterion, 8);

        criterion = new SampleSearchCriterion();
        criterion.withSpace().withPermId().thatEquals("/TEST-SPACE");
        testSearch(TEST_USER, criterion, 8);
    }

    @Test
    public void testSearchWithSpaceUnauthorized()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withPermId().thatEquals("200902091219327-1025");
        testSearch(TEST_USER, criterion, 1);

        criterion = new SampleSearchCriterion();
        criterion.withPermId().thatEquals("200902091219327-1025");
        testSearch(TEST_SPACE_USER, criterion, 0);
    }

    @Test
    public void testSearchWithCodeInContainer()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withCode().thatEquals("PLATE_WELLSEARCH:WELL-A01");
        testSearch(TEST_USER, criterion, "/CISD/PLATE_WELLSEARCH:WELL-A01");
    }

    @Test
    public void testSearchWithTypeIdSetToPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withType().withId().thatEquals(new EntityTypePermId("REINFECT_PLATE"));
        testSearch(TEST_USER, criterion, "/CISD/RP1-A2X", "/CISD/RP1-B1X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithTypeWithCode()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withType().withCode().thatEquals("REINFECT_PLATE");
        testSearch(TEST_USER, criterion, "/CISD/RP1-A2X", "/CISD/RP1-B1X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withType().withPermId().thatEquals("REINFECT_PLATE");
        testSearch(TEST_USER, criterion, "/CISD/RP1-A2X", "/CISD/RP1-B1X", "/CISD/RP2-A1X");
    }

    @Test
    public void testSearchWithExperimentWithIdSetToIdentifier()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withExperiment().withId().thatEquals(new ExperimentIdentifier("/CISD/NEMO/EXP10"));
        testSearch(TEST_USER, criterion, "/CISD/3VCP5");
    }

    @Test
    public void testSearchWithExperimentWithIdSetToPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withExperiment().withId().thatEquals(new ExperimentPermId("200811050952663-1029"));
        testSearch(TEST_USER, criterion, "/CISD/3VCP5");
    }

    @Test
    public void testSearchWithExperimentWithPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withExperiment().withPermId().thatEquals("200811050952663-1029");
        testSearch(TEST_USER, criterion, "/CISD/3VCP5");
    }

    @Test
    public void testSearchWithExperimentWithCode()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withExperiment().withCode().thatEquals("EXP-TEST-1");
        testSearch(TEST_USER, criterion, "/CISD/CP-TEST-1", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithTypeIdSetToPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withExperiment().withType().withId().thatEquals(new EntityTypePermId("COMPOUND_HCS"));
        testSearch(TEST_USER, criterion, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithTypeWithCode()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withExperiment().withType().withCode().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criterion, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithTypeWithPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withExperiment().withType().withPermId().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criterion, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2", "/CISD/DYNA-TEST-1");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithIdSetToIdentifier()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withExperiment().withProject().withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/NOE"));
        testSearch(TEST_USER, criterion, "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithIdSetToPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withExperiment().withProject().withId().thatEquals(new ProjectPermId("20120814110011738-106"));
        testSearch(TEST_USER, criterion, "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithCode()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withExperiment().withProject().withCode().thatEquals("NOE");
        testSearch(TEST_USER, criterion, "/CISD/CP-TEST-2", "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withExperiment().withProject().withId().thatEquals(new ProjectPermId("20120814110011738-106"));
        testSearch(TEST_USER, criterion, "/TEST-SPACE/CP-TEST-4");
    }

    @Test
    public void testSearchWithExperimentWithProjectWithSpaceWithIdSetToPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withExperiment().withProject().withSpace().withId().thatEquals(new SpacePermId("TEST-SPACE"));
        testSearch(TEST_USER, criterion, 8);

        criterion = new SampleSearchCriterion();
        criterion.withExperiment().withProject().withSpace().withId().thatEquals(new SpacePermId("/TEST-SPACE"));
        testSearch(TEST_USER, criterion, 8);
    }

    @Test
    public void testSearchWithExperimentWithProjectWithSpaceWithCode()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withExperiment().withProject().withSpace().withCode().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criterion, 8);
    }

    @Test
    public void testSearchWithExperimentWithProjectWithSpaceWithPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withExperiment().withProject().withSpace().withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criterion, 8);

        criterion = new SampleSearchCriterion();
        criterion.withExperiment().withProject().withSpace().withPermId().thatEquals("/TEST-SPACE");
        testSearch(TEST_USER, criterion, 8);
    }

    @Test
    public void testSearchWithParentWithIdSetToIdentifier()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withParents().withId().thatEquals(new SampleIdentifier("/CISD/MP002-1"));
        testSearch(TEST_USER, criterion, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithParentWithIdSetToPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withParents().withId().thatEquals(new SamplePermId("200811050917877-331"));
        testSearch(TEST_USER, criterion, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithParentWithPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withParents().withPermId().thatEquals("200811050917877-331");
        testSearch(TEST_USER, criterion, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithParentWithCode()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withParents().withCode().thatEquals("MP002-1");
        testSearch(TEST_USER, criterion, "/CISD/3V-125", "/CISD/3V-126");
    }

    @Test
    public void testSearchWithChildrenWithIdSetToIdentifier()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withChildren().withId().thatEquals(new SampleIdentifier("/CISD/3VCP6"));
        testSearch(TEST_USER, criterion, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithChildrenWithIdSetToPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withChildren().withId().thatEquals(new SamplePermId("200811050946559-980"));
        testSearch(TEST_USER, criterion, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithChildrenWithPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withChildren().withPermId().thatEquals("200811050946559-980");
        testSearch(TEST_USER, criterion, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithChildrenWithCode()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withChildren().withCode().thatEquals("3VCP6");
        testSearch(TEST_USER, criterion, "/CISD/3V-125", "/CISD/CL-3V:A02");
    }

    @Test
    public void testSearchWithContainerWithIdSetToPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withContainer().withId().thatEquals(new SamplePermId("200811050924274-994"));
        testSearch(TEST_USER, criterion, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithContainerWithIdSetToIdentifier()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withContainer().withId().thatEquals(new SampleIdentifier("/CISD/B1B3"));
        testSearch(TEST_USER, criterion, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithContainerWithPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withContainer().withPermId().thatEquals("200811050924274-994");
        testSearch(TEST_USER, criterion, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithContainerWithCode()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withContainer().withCode().thatEquals("B1B3");
        testSearch(TEST_USER, criterion, "/CISD/B1B3:B01", "/CISD/B1B3:B03");
    }

    @Test
    public void testSearchWithTagWithIdSetToPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criterion, "/TEST-SPACE/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithIdSetToCodeId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withTag().withId().thatEquals(new TagCode("TEST_METAPROJECTS"));
        testSearch(TEST_USER, criterion, "/TEST-SPACE/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithCode()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withTag().withCode().thatEquals("TEST_METAPROJECTS");
        testSearch(TEST_USER, criterion, "/TEST-SPACE/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithPermId()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withTag().withPermId().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_USER, criterion, "/TEST-SPACE/EV-TEST");
    }

    @Test
    public void testSearchWithTagWithPermIdUnauthorized()
    {
        final SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withTag().withCode().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_SPACE_USER, criterion, 0);
    }

    @Test
    public void testSearchWithRegistrationDateThatEquals()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withRegistrationDate().thatEquals("2009-02-09");
        testSearch(TEST_USER, criterion, 15);
    }

    @Test
    public void testSearchWithModificationDateThatEquals()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withModificationDate().thatEquals("2009-08-18");
        testSearch(TEST_USER, criterion, 14);
    }

    @Test
    public void testSearchWithAnyFieldMatchingProperty()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withAnyField().thatEquals("\"very advanced stuff\"");
        testSearch(TEST_USER, criterion, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithAnyFieldMatchingAttribute()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withAnyField().thatEquals("\"CP-TEST-2\"");
        testSearch(TEST_USER, criterion, "/CISD/CP-TEST-2");
    }

    @Test
    public void testSearchWithAnyProperty()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withAnyProperty().thatEquals("\"very advanced\"");
        testSearch(TEST_USER, criterion, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithAndOperator()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withAndOperator();
        criterion.withCode().thatStartsWith("CP");
        criterion.withCode().thatEndsWith("-1");
        testSearch(TEST_USER, criterion, "/CISD/CP-TEST-1");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        SampleSearchCriterion criterion = new SampleSearchCriterion();
        criterion.withOrOperator();
        criterion.withPermId().thatEquals("200902091219327-1025");
        criterion.withPermId().thatEquals("200902091250077-1026");
        testSearch(TEST_USER, criterion, "/CISD/CP-TEST-1", "/CISD/CP-TEST-2");
    }

    private void testSearch(String user, SampleSearchCriterion criterion, String... expectedIdentifiers)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        List<Sample> samples =
                v3api.searchSamples(sessionToken, criterion, new SampleFetchOptions());

        assertIdentifiers(samples, expectedIdentifiers);
        v3api.logout(sessionToken);
    }

    private void testSearch(String user, SampleSearchCriterion criterion, int expectedCount)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        List<Sample> samples =
                v3api.searchSamples(sessionToken, criterion, new SampleFetchOptions());

        assertEquals(samples.size(), expectedCount);
        v3api.logout(sessionToken);
    }

}
