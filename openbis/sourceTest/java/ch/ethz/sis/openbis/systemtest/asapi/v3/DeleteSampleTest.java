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

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.systemtest.asapi.v3.index.RemoveFromIndexState;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class DeleteSampleTest extends AbstractDeletionTest
{

    @Test
    public void testDeleteWithIndexCheck()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentPermId = createCisdExperiment();
        SamplePermId samplePermId = createCisdSample(experimentPermId);

        SampleDeletionOptions options = new SampleDeletionOptions();
        options.setReason("It is just a test");

        List<SamplePE> samples = daoFactory.getSampleDAO().listByPermID(Arrays.asList(samplePermId.getPermId()));
        assertEquals(samples.size(), 1);

        RemoveFromIndexState state = new RemoveFromIndexState(daoFactory);

        v3api.deleteSamples(sessionToken, Collections.singletonList(samplePermId), options);

        assertSamplesRemovedFromIndex(state, samples.get(0).getId());
    }

    @Test
    public void testDeleteSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentPermId = createCisdExperiment();
        SamplePermId samplePermId = createCisdSample(experimentPermId);

        SampleDeletionOptions options = new SampleDeletionOptions();
        options.setReason("It is just a test");

        assertExperimentExists(experimentPermId);
        assertSampleExists(samplePermId);

        IDeletionId deletionId = v3api.deleteSamples(sessionToken, Collections.singletonList(samplePermId), options);
        Assert.assertNotNull(deletionId);

        assertExperimentExists(experimentPermId);
        assertSampleDoesNotExist(samplePermId);
    }

    @Test
    public void testDeleteSampleWithDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SamplePermId samplePermId = new SamplePermId("200902091225616-1027");
        DataSetPermId dataSetPermId1 = new DataSetPermId("20081105092159333-3");
        DataSetPermId dataSetPermId2 = new DataSetPermId("20110805092359990-17");

        SampleDeletionOptions options = new SampleDeletionOptions();
        options.setReason("It is just a test");

        assertSampleExists(samplePermId);
        assertDataSetExists(dataSetPermId1);
        assertDataSetExists(dataSetPermId2);

        IDeletionId deletionId = v3api.deleteSamples(sessionToken, Collections.singletonList(samplePermId), options);
        Assert.assertNotNull(deletionId);

        assertSampleDoesNotExist(samplePermId);
        assertDataSetDoesNotExist(dataSetPermId1);
        assertDataSetDoesNotExist(dataSetPermId2);
    }

    @Test
    public void testDeleteSampleWithComponentsSamples()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SamplePermId samplePermId = new SamplePermId("200811050919915-8");
        SamplePermId componentPermId1 = new SamplePermId("200811050919915-9");
        SamplePermId componentPermId2 = new SamplePermId("200811050919915-10");

        SampleDeletionOptions options = new SampleDeletionOptions();
        options.setReason("It is just a test");

        assertSampleExists(samplePermId);
        assertSampleExists(componentPermId1);
        assertSampleExists(componentPermId2);

        IDeletionId deletionId = v3api.deleteSamples(sessionToken, Collections.singletonList(samplePermId), options);
        Assert.assertNotNull(deletionId);

        assertSampleDoesNotExist(samplePermId);
        assertSampleDoesNotExist(componentPermId1);
        assertSampleDoesNotExist(componentPermId2);
    }

    @Test
    public void testDeleteSampleWithUnauthorizedSample()
    {
        final SamplePermId permId = createCisdSample(null);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

                    SampleDeletionOptions options = new SampleDeletionOptions();
                    options.setReason("It is just a test");

                    v3api.deleteSamples(sessionToken, Collections.singletonList(permId), options);
                }
            }, permId);
    }

}
