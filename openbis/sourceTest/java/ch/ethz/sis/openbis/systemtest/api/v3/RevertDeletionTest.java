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

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.experiment.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.DeletionTechId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class RevertDeletionTest extends AbstractDeletionTest
{

    @Test
    public void testRevertDeletionOfExperimentWithSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentToDelete();
        SamplePermId sampleId = createSampleToDelete(experimentId);

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");
        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertDeletionExists(deletionId);
        assertExperimentDoesNotExist(experimentId);
        assertSampleDoesNotExist(sampleId);

        v3api.revertDeletions(sessionToken, Collections.singletonList(deletionId));

        assertDeletionDoesNotExist(deletionId);
        assertExperimentExists(experimentId);
        assertSampleExists(sampleId);
    }

    @Test
    public void testRevertDeletionWithNonexistentDeletion()
    {
        final IDeletionId deletionId = new DeletionTechId(-1L);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken = v3api.login(TEST_USER, PASSWORD);
                    v3api.revertDeletions(sessionToken, Collections.singletonList(deletionId));
                }
            }, deletionId);
    }

    @Test
    public void testRevertDeletionWithUnauthorizedDeletion()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId experimentId = createExperimentToDelete();

        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("It is just a test");
        final IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(experimentId), deletionOptions);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    String sessionToken2 = v3api.login(TEST_SPACE_USER, PASSWORD);
                    v3api.revertDeletions(sessionToken2, Collections.singletonList(deletionId));
                }
            }, deletionId);
    }

}
