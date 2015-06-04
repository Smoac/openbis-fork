/*
 * Copyright 2015 ETH Zuerich, SIS
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.systemsx.cisd.openbis.systemtest.AbstractAssignmentSampleToExperimentTestCase;

/**
 * Sample to experiment assignment tests for API V3.
 *
 * @author Franz-Josef Elmer
 */
@Test(groups = { "system-cleandb"}, enabled = false)
public class AssignSampleToExperimentTest //extends AbstractAssignmentSampleToExperimentTestCase
{
    private static final String CONTEXT_DESCRIPTION = " (Context: [])";
    
    @Autowired
    protected IApplicationServerApi v3api;
/*
    @Override
    protected void reassignSamplesToExperiment(String experimentIdentifier, List<String> samplePermIds, 
            String userSessionToken)
    {
        ExperimentIdentifier experimentId 
            = experimentIdentifier == null ? null : new ExperimentIdentifier(experimentIdentifier);
        List<SampleUpdate> sampleUpdates = new ArrayList<SampleUpdate>();
        for (String samplePermId : samplePermIds)
        {
            SampleUpdate sampleUpdate = new SampleUpdate();
            sampleUpdate.setSampleId(new SamplePermId(samplePermId));
            sampleUpdate.setExperimentId(experimentId);
            sampleUpdates.add(sampleUpdate);
        }
        v3api.updateSamples(userSessionToken, sampleUpdates);
    }

    @Override
    protected void reassignSampleToExperiment(String samplePermId, String experimentIdentifierOrNull, 
            String userSessionToken)
    {
        reassignSamplesToExperiment(experimentIdentifierOrNull, Collections.singletonList(samplePermId), 
                userSessionToken);
    }
*/
}
