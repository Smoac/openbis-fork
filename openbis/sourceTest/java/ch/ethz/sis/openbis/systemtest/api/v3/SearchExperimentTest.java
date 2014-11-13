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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagCode;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ExperimentSearchCriterion;

/**
 * @author pkupczyk
 */
public class SearchExperimentTest extends AbstractExperimentTest
{

    @Test
    public void testSearchWithIdSetToIdentifier()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withId().thatEquals(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP1");
    }

    @Test
    public void testSearchWithIdSetToPermId()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withId().thatEquals(new ExperimentPermId("200811050951882-1028"));
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP1");
    }

    @Test
    public void testSearchWithPermId()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withPermId().thatEquals("200811050951882-1028");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP1");
    }

    @Test
    public void testSearchWithCode()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withCode().thatStartsWith("EXP1");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP1", "/CISD/NEMO/EXP10", "/CISD/NEMO/EXP11");
    }

    @Test
    public void testSearchWithTypeWithIdSetToPermId()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withType().withId().thatEquals(new EntityTypePermId("COMPOUND_HCS"));
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithTypeWithCode()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withType().withCode().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithTypeWithPermId()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withType().withPermId().thatEquals("COMPOUND_HCS");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithProjectWithIdSetToIdentifier()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withProject().withId().thatEquals(new ProjectIdentifier("/TEST-SPACE/NOE"));
        testSearch(TEST_USER, criterion, "/TEST-SPACE/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithIdSetToPermId()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withProject().withId().thatEquals(new ProjectPermId("20120814110011738-106"));
        testSearch(TEST_USER, criterion, "/TEST-SPACE/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithPermId()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withProject().withPermId().thatEquals("20120814110011738-106");
        testSearch(TEST_USER, criterion, "/TEST-SPACE/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithCode()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withProject().withCode().thatEquals("NOE");
        testSearch(TEST_USER, criterion, "/CISD/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithSpaceWithIdSetToPermId()
    {
        String[] expected = new String[] { "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", "/TEST-SPACE/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE" };

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withProject().withSpace().withId().thatEquals(new SpacePermId("TEST-SPACE"));
        testSearch(TEST_USER, criterion, expected);

        criterion = new ExperimentSearchCriterion();
        criterion.withProject().withSpace().withId().thatEquals(new SpacePermId("/TEST-SPACE"));
        testSearch(TEST_USER, criterion, expected);
    }

    @Test
    public void testSearchWithProjectWithSpaceWithCode()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withProject().withSpace().withCode().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criterion, "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", "/TEST-SPACE/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithProjectWithSpaceWithPermId()
    {
        String[] expected = new String[] { "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST", "/TEST-SPACE/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE" };

        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withProject().withSpace().withPermId().thatEquals("TEST-SPACE");
        testSearch(TEST_USER, criterion, expected);

        criterion = new ExperimentSearchCriterion();
        criterion.withProject().withSpace().withPermId().thatEquals("/TEST-SPACE");
        testSearch(TEST_USER, criterion, expected);
    }

    @Test
    public void testSearchWithPropertyThatEquals()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEquals("desc1");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-1");

        criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEquals("desc");
        testSearch(TEST_USER, criterion, 0);

        criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEquals("esc");
        testSearch(TEST_USER, criterion, 0);

        criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEquals("esc1");
        testSearch(TEST_USER, criterion, 0);
    }

    @Test
    public void testSearchWithPropertyThatStartsWith()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatStartsWith("desc1");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-1");

        criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatStartsWith("desc");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");

        criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatStartsWith("esc");
        testSearch(TEST_USER, criterion, 0);

        criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatStartsWith("esc1");
        testSearch(TEST_USER, criterion, 0);
    }

    @Test
    public void testSearchWithPropertyThatEndsWith()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEndsWith("desc1");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-1");

        criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEndsWith("desc");
        testSearch(TEST_USER, criterion, 0);

        criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEndsWith("esc");
        testSearch(TEST_USER, criterion, 0);

        criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatEndsWith("esc1");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-1");
    }

    @Test
    public void testSearchWithPropertyThatContains()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatContains("desc1");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-1");

        criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatContains("desc");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");

        criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatContains("esc");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-1", "/CISD/NOE/EXP-TEST-2");

        criterion = new ExperimentSearchCriterion();
        criterion.withProperty("DESCRIPTION").thatContains("esc1");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-1");
    }

    @Test
    public void testSearchWithAnyPropertyThatEquals()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withAnyProperty().thatEquals("FEMALE");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-2");

        criterion = new ExperimentSearchCriterion();
        criterion.withAnyProperty().thatEquals("FEMAL");
        testSearch(TEST_USER, criterion, 0);
    }

    @Test
    public void testSearchWithAnyPropertyThatEqualsWithWildcards()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withAnyProperty().thatEquals("*EMAL*");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-2");
    }

    @Test
    public void testSearchWithAnyPropertyThatStartsWith()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withAnyProperty().thatStartsWith("FEMAL");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-2");

        criterion = new ExperimentSearchCriterion();
        criterion.withAnyProperty().thatStartsWith("EMAL");
        testSearch(TEST_USER, criterion, 0);
    }

    @Test
    public void testSearchWithAnyPropertyThatEndsWith()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withAnyProperty().thatEndsWith("EMALE");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-2");

        criterion = new ExperimentSearchCriterion();
        criterion.withAnyProperty().thatEndsWith("EMAL");
        testSearch(TEST_USER, criterion, 0);
    }

    @Test
    public void testSearchWithAnyPropertyThatContains()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withAnyProperty().thatContains("EMAL");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-2");

        criterion = new ExperimentSearchCriterion();
        criterion.withAnyProperty().thatContains("FMAL");
        testSearch(TEST_USER, criterion, 0);
    }

    @Test
    public void testSearchWithAnyFieldMatchingProperty()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withAnyField().thatEquals("FEMALE");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-2");
    }

    @Test
    public void testSearchWithAnyFieldMatchingAttribute()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withAnyField().thatEquals("EXP-TEST-2");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-2", "/CISD/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2");
    }

    @Test
    public void testSearchWithTagWithIdSetToPermId()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withTag().withId().thatEquals(new TagPermId("/test/TEST_METAPROJECTS"));
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithTagWithIdSetToCodeId()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withTag().withId().thatEquals(new TagCode("TEST_METAPROJECTS"));
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithTagWithCode()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withTag().withCode().thatEquals("TEST_METAPROJECTS");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithTagWithPermId()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withTag().withPermId().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithTagWithPermIdUnauthorized()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withTag().withPermId().thatEquals("/test/TEST_METAPROJECTS");
        testSearch(TEST_SPACE_USER, criterion, 0);
    }

    @Test
    public void testSearchWithRegistrationDateThatEquals()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withRegistrationDate().thatEquals("2009-02-09");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP-TEST-1", "/CISD/NEMO/EXP-TEST-2", "/CISD/NOE/EXP-TEST-2", "/TEST-SPACE/NOE/EXP-TEST-2",
                "/TEST-SPACE/NOE/EXPERIMENT-TO-DELETE");
    }

    @Test
    public void testSearchWithRegistrationDateThatIsLaterThan()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withRegistrationDate().thatIsLaterThanOrEqualTo("2009-02-09");
        testSearch(TEST_USER, criterion, 5);
    }

    @Test
    public void testSearchWithRegistrationDateThatIsEarlierThan()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withRegistrationDate().thatIsEarlierThanOrEqualTo("2008-11-05");
        testSearch(TEST_USER, criterion, 7);
    }

    @Test
    public void testSearchWithModificationDateThatEquals()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withModificationDate().thatEquals("2009-03-18");
        testSearch(TEST_USER, criterion, 12);
    }

    @Test
    public void testSearchWithAndOperator()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withAndOperator();
        criterion.withCode().thatContains("TEST");
        criterion.withCode().thatContains("SPACE");
        testSearch(TEST_USER, criterion, "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testSearchWithOrOperator()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withOrOperator();
        criterion.withPermId().thatEquals("200811050952663-1029");
        criterion.withPermId().thatEquals("200811050952663-1030");
        testSearch(TEST_USER, criterion, "/CISD/NEMO/EXP10", "/CISD/NEMO/EXP11");
    }

    @Test
    public void testSearchWithSpaceUnauthorized()
    {
        ExperimentSearchCriterion criterion = new ExperimentSearchCriterion();
        criterion.withPermId().thatEquals("200811050951882-1028");
        testSearch(TEST_USER, criterion, 1);

        criterion = new ExperimentSearchCriterion();
        criterion.withPermId().thatEquals("200811050951882-1028");
        testSearch(TEST_SPACE_USER, criterion, 0);
    }

    private void testSearch(String user, ExperimentSearchCriterion criterion, String... expectedIdentifiers)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        List<Experiment> experiments =
                v3api.searchExperiments(sessionToken, criterion, new ExperimentFetchOptions());

        assertIdentifiers(experiments, expectedIdentifiers);
        v3api.logout(sessionToken);
    }

    private void testSearch(String user, ExperimentSearchCriterion criterion, int expectedCount)
    {
        String sessionToken = v3api.login(user, PASSWORD);

        List<Experiment> experiments =
                v3api.searchExperiments(sessionToken, criterion, new ExperimentFetchOptions());

        assertEquals(experiments.size(), expectedCount);
        v3api.logout(sessionToken);
    }

}
