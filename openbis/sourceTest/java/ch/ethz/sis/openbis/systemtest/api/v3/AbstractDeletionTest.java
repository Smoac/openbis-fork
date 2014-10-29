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

import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.deletion.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;

/**
 * @author pkupczyk
 */
public class AbstractDeletionTest extends AbstractTest
{

    protected ExperimentPermId createExperimentToDelete()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentCreation creation = new ExperimentCreation();
        creation.setCode("EXPERIMENT_TO_DELETE");
        creation.setTypeId(new EntityTypePermId("SIRNA_HCS"));
        creation.setProjectId(new ProjectIdentifier("/CISD/DEFAULT"));

        List<ExperimentPermId> permIds = v3api.createExperiments(sessionToken, Collections.singletonList(creation));
        List<Experiment> experiments = v3api.listExperiments(sessionToken, permIds, new ExperimentFetchOptions());

        Assert.assertEquals(1, experiments.size());
        Assert.assertEquals("EXPERIMENT_TO_DELETE", experiments.get(0).getCode());

        return permIds.get(0);
    }

    protected SamplePermId createSampleToDelete(IExperimentId experimentId)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_TO_DELETE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setExperimentId(experimentId);

        List<SamplePermId> permIds = v3api.createSamples(sessionToken, Collections.singletonList(creation));
        List<Sample> samples = v3api.listSamples(sessionToken, permIds, new SampleFetchOptions());

        Assert.assertEquals(1, samples.size());
        Assert.assertEquals("SAMPLE_TO_DELETE", samples.get(0).getCode());

        return permIds.get(0);
    }

    protected void assertExperimentExists(IExperimentId experimentId)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<Experiment> result = v3api.listExperiments(sessionToken, Collections.singletonList(experimentId), new ExperimentFetchOptions());
        Assert.assertEquals(1, result.size());
    }

    protected void assertExperimentDoesNotExist(IExperimentId experimentId)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<Experiment> result = v3api.listExperiments(sessionToken, Collections.singletonList(experimentId), new ExperimentFetchOptions());
        Assert.assertEquals(0, result.size());
    }

    protected void assertSampleExists(ISampleId sampleId)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<Sample> result = v3api.listSamples(sessionToken, Collections.singletonList(sampleId), new SampleFetchOptions());
        Assert.assertEquals(1, result.size());
    }

    protected void assertSampleDoesNotExist(ISampleId sampleId)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<Sample> result = v3api.listSamples(sessionToken, Collections.singletonList(sampleId), new SampleFetchOptions());
        Assert.assertEquals(0, result.size());
    }

    protected void assertDeletionExists(IDeletionId deletionId)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<Deletion> result = v3api.listDeletions(sessionToken, new DeletionFetchOptions());

        for (Deletion item : result)
        {
            if (item.getId().equals(deletionId))
            {
                return;
            }
        }

        Assert.fail("Deletion " + deletionId + " does not exist");
    }

    protected void assertDeletionDoesNotExist(IDeletionId deletionId)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<Deletion> result = v3api.listDeletions(sessionToken, new DeletionFetchOptions());

        for (Deletion item : result)
        {
            if (item.getId().equals(deletionId))
            {
                Assert.fail("Deletion " + deletionId + " exists");
            }
        }
    }

}
