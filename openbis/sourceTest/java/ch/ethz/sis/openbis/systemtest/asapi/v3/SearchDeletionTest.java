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

import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.exceptions.NotFetchedException;

import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class SearchDeletionTest extends AbstractDeletionTest
{

    @Test
    public void testSearchDeletionsWithoutDeletedObjects()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        List<Deletion> beforeDeletions = v3api.searchDeletions(sessionToken, new DeletionSearchCriteria(), fetchOptions).getObjects();

        ExperimentPermId experimentId = createCisdExperiment();
        SamplePermId sampleId = createCisdSample(experimentId);

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");
        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertExperimentDoesNotExist(experimentId);
        assertSampleDoesNotExist(sampleId);

        List<Deletion> afterDeletions = v3api.searchDeletions(sessionToken, new DeletionSearchCriteria(), fetchOptions).getObjects();
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
    public void testSearchDeletionsWithDeletedObjects()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        fetchOptions.withDeletedObjects();
        List<Deletion> beforeDeletions = v3api.searchDeletions(sessionToken, new DeletionSearchCriteria(), fetchOptions).getObjects();

        ExperimentPermId experimentId = createCisdExperiment();
        SamplePermId sampleId = createCisdSample(experimentId);

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");

        assertExperimentExists(experimentId);
        assertSampleExists(sampleId);

        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertExperimentDoesNotExist(experimentId);
        assertSampleDoesNotExist(sampleId);

        List<Deletion> afterDeletions = v3api.searchDeletions(sessionToken, new DeletionSearchCriteria(), fetchOptions).getObjects();
        Assert.assertEquals(beforeDeletions.size() + 1, afterDeletions.size());
        Deletion latestDeletion = afterDeletions.get(afterDeletions.size() - 1);
        Assert.assertEquals(deletionId, latestDeletion.getId());
        Assert.assertEquals(deletionOptions.getReason(), latestDeletion.getReason());
        Assert.assertEquals(1, latestDeletion.getDeletedObjects().size());
        Assert.assertEquals(experimentId, latestDeletion.getDeletedObjects().get(0).getId());
    }

    @Test
    public void testSearchDeletionsWithUnauthorizedDeletion()
    {
        String spaceSessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        DeletionFetchOptions fetchOptions = new DeletionFetchOptions();
        List<Deletion> beforeDeletions = v3api.searchDeletions(spaceSessionToken, new DeletionSearchCriteria(), fetchOptions).getObjects();

        String adminSessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createCisdExperiment();
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");
        v3api.deleteExperiments(adminSessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertExperimentDoesNotExist(experimentId);

        List<Deletion> afterDeletions = v3api.searchDeletions(spaceSessionToken, new DeletionSearchCriteria(), fetchOptions).getObjects();
        Assert.assertEquals(beforeDeletions.size(), afterDeletions.size());
    }

}
