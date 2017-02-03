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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.test.context.transaction.TestTransaction;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.systemtest.asapi.v3.index.RemoveFromIndexState;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import junit.framework.Assert;

/**
 * @author pkupczyk
 */
public class DeleteExperimentTest extends AbstractDeletionTest
{

    @Test
    public void testDeleteEmptyList()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentDeletionOptions options = new ExperimentDeletionOptions();
        options.setReason("It is just a test");

        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, new ArrayList<ExperimentPermId>(), options);
        Assert.assertNull(deletionId);
    }

    @Test
    public void testDeleteWithIndexCheck() throws Exception
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId permId = createCisdExperiment(new ProjectIdentifier("/CISD/DEFAULT"), java.util.UUID.randomUUID().toString().toUpperCase());
        TestTransaction.flagForCommit();
        TestTransaction.end();

        ExperimentDeletionOptions options = new ExperimentDeletionOptions();
        options.setReason("It is just a test");

        TestTransaction.start();
        List<ExperimentPE> experiments = daoFactory.getExperimentDAO().listByPermID(Arrays.asList(permId.getPermId()));
        assertEquals(experiments.size(), 1);

        RemoveFromIndexState state = new RemoveFromIndexState();

        v3api.deleteExperiments(sessionToken, Collections.singletonList(permId), options);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        Thread.sleep(2000);

        assertExperimentsRemovedFromIndex(state, experiments.get(0).getId());
    }

    @Test
    public void testDeleteExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId permId = createCisdExperiment();

        ExperimentDeletionOptions options = new ExperimentDeletionOptions();
        options.setReason("It is just a test");

        assertExperimentExists(permId);

        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(permId), options);
        Assert.assertNotNull(deletionId);

        assertExperimentDoesNotExist(permId);
    }

    @Test
    public void testDeleteExperimentWithSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentPermId = createCisdExperiment();
        SamplePermId samplePermId = createCisdSample(experimentPermId);

        ExperimentDeletionOptions options = new ExperimentDeletionOptions();
        options.setReason("It is just a test");

        assertExperimentExists(experimentPermId);
        assertSampleExists(samplePermId);

        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentPermId), options);
        Assert.assertNotNull(deletionId);

        assertExperimentDoesNotExist(experimentPermId);
        assertSampleDoesNotExist(samplePermId);
    }

    @Test
    public void testDeleteExperimentWithDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentPermId = new ExperimentPermId("200902091255058-1035");
        DataSetPermId dataSetPermId1 = new DataSetPermId("20081105092159333-3");
        DataSetPermId dataSetPermId2 = new DataSetPermId("20110805092359990-17");

        ExperimentDeletionOptions options = new ExperimentDeletionOptions();
        options.setReason("It is just a test");

        assertExperimentExists(experimentPermId);
        assertDataSetExists(dataSetPermId1);
        assertDataSetExists(dataSetPermId2);

        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentPermId), options);
        Assert.assertNotNull(deletionId);

        assertExperimentDoesNotExist(experimentPermId);
        assertDataSetDoesNotExist(dataSetPermId1);
        assertDataSetDoesNotExist(dataSetPermId2);
    }

    @Test
    public void testDeleteExperimentWithUnauthorizedExperiment()
    {
        final ExperimentPermId permId = createCisdExperiment();

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

                    ExperimentDeletionOptions options = new ExperimentDeletionOptions();
                    options.setReason("It is just a test");

                    v3api.deleteExperiments(sessionToken, Collections.singletonList(permId), options);
                }
            }, permId);
    }

    @Test
    public void testExperimentWithPowerUserInAnotherSpace()
    {
        final ExperimentPermId permId = new ExperimentPermId("200902091255058-1037");

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);

                    ExperimentDeletionOptions options = new ExperimentDeletionOptions();
                    options.setReason("It is just a test");

                    v3api.deleteExperiments(sessionToken, Collections.singletonList(permId), options);
                }
            }, permId);
    }

}
