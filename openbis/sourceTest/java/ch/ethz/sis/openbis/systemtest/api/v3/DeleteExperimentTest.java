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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.experiment.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.deletion.IDeletionId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author pkupczyk
 */
public class DeleteExperimentTest extends AbstractExperimentTest
{

    @Test
    public void testDeleteExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId permId = createExperimentToDelete();

        ExperimentDeletionOptions options = new ExperimentDeletionOptions();
        options.setReason("It is just a test");

        IDeletionId deletionId = v3api.deleteExperiments(sessionToken, Collections.singletonList(permId), options);
        Assert.assertNotNull(deletionId);

        List<Experiment> experiments = v3api.listExperiments(sessionToken, Collections.singletonList(permId), new ExperimentFetchOptions());
        Assert.assertEquals(0, experiments.size());
    }

    @Test
    public void testDeleteExperimentWithUnauthorizedExperiment()
    {
        final ExperimentPermId permId = createExperimentToDelete();

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

    private ExperimentPermId createExperimentToDelete()
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

}
