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

package ch.systemsx.cisd.openbis.systemtest.api.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagNameId;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author pkupczyk
 */
public class ExperimentUpdateTest extends AbstractExperimentTest
{

    @Test
    public void testUpdateExperimentSetProject()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(ids.get(0));
        update.setProjectId(new ProjectIdentifier("/CISD/NOE"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        List<Experiment> experiments = v3api.listExperiments(sessionToken, ids, fetchOptions);

        AssertionUtil.assertCollectionSize(experiments, 1);

        Experiment experiment = experiments.get(0);
        assertEquals(experiment.getIdentifier().getIdentifier(), "/CISD/NOE/TEST_EXPERIMENT");
    }

    @Test
    public void testUpdateExperimentSetProperties()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "description 1");

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(ids.get(0));
        update.setProperty("DESCRIPTION", "description 2");

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchProperties();
        List<Experiment> experiments = v3api.listExperiments(sessionToken, ids, fetchOptions);

        AssertionUtil.assertCollectionSize(experiments, 1);

        Experiment experiment = experiments.get(0);
        assertEquals(1, experiment.getProperties().size());
        assertEquals("description 2", experiment.getProperties().get("DESCRIPTION"));
    }

    @Test
    public void testUpdateExperimentSetTags()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("TEST_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        creation.setProperty("DESCRIPTION", "a description");
        creation.setTagIds(Arrays.asList(new TagNameId("TEST_TAG_1")));

        List<ExperimentPermId> ids = v3api.createExperiments(sessionToken, Arrays.asList(creation));

        ExperimentUpdate update = new ExperimentUpdate();
        update.setExperimentId(ids.get(0));
        update.getTagIds().set(new TagNameId("TEST_TAG_2"));

        v3api.updateExperiments(sessionToken, Arrays.asList(update));

        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchTags();
        List<Experiment> experiments = v3api.listExperiments(sessionToken, ids, fetchOptions);

        AssertionUtil.assertCollectionSize(experiments, 1);

        Experiment experiment = experiments.get(0);
        assertEquals(1, experiment.getTags().size());
        assertEquals("TEST_TAG_2", experiment.getTags().iterator().next().getName());
    }

}
