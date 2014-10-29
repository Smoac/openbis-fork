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

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.experiment.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.deletion.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.NotFetchedException;

/**
 * @author pkupczyk
 */
public class ListDeletionTest extends AbstractDeletionTest
{

    @Test
    public void testListDeletionsWithoutDeletedObjects()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        List<Deletion> beforeDeletions = v3api.listDeletions(sessionToken, fetchOptions);

        ExperimentPermId experimentId = createExperimentToDelete();
        SamplePermId sampleId = createSampleToDelete(experimentId);

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");
        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertExperimentDoesNotExist(experimentId);
        assertSampleDoesNotExist(sampleId);

        List<Deletion> afterDeletions = v3api.listDeletions(sessionToken, fetchOptions);
        Assert.assertEquals(beforeDeletions.size() + 1, afterDeletions.size());
        Deletion latestDeletion = afterDeletions.get(afterDeletions.size() - 1);
        Assert.assertEquals(deletionId, latestDeletion.getId());
        Assert.assertEquals(deletionOptions.getReason(), latestDeletion.getReason());
        try
        {
            latestDeletion.getDeletedObjects();
            Assert.fail();
        } catch (NotFetchedException e)
        {
            // that's expected
        }
    }

    @Test
    public void testListDeletionsWithDeletedObjects()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        fetchOptions.fetchDeletedObjects();
        List<Deletion> beforeDeletions = v3api.listDeletions(sessionToken, fetchOptions);

        ExperimentPermId experimentId = createExperimentToDelete();
        SamplePermId sampleId = createSampleToDelete(experimentId);

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");
        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertExperimentDoesNotExist(experimentId);
        assertSampleDoesNotExist(sampleId);

        List<Deletion> afterDeletions = v3api.listDeletions(sessionToken, fetchOptions);
        Assert.assertEquals(beforeDeletions.size() + 1, afterDeletions.size());
        Deletion latestDeletion = afterDeletions.get(afterDeletions.size() - 1);
        Assert.assertEquals(deletionId, latestDeletion.getId());
        Assert.assertEquals(deletionOptions.getReason(), latestDeletion.getReason());
        Assert.assertEquals(1, latestDeletion.getDeletedObjects().size());
        Assert.assertEquals(experimentId, latestDeletion.getDeletedObjects().get(0).getId());
    }

    @Test
    public void testListDeletionsWithUnauthorizedDeletion()
    {
        String spaceSessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        List<Deletion> beforeDeletions = v3api.listDeletions(spaceSessionToken, fetchOptions);

        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentToDelete();
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");
        v3api.deleteExperiments(adminSessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertExperimentDoesNotExist(experimentId);

        List<Deletion> afterDeletions = v3api.listDeletions(spaceSessionToken, fetchOptions);
        Assert.assertEquals(beforeDeletions.size(), afterDeletions.size());
    }

}
