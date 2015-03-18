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

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.systemsx.cisd.openbis.systemtest.AbstractDataSetAssignmentTestCase;

/**
 * Data set assignment tests for API V3.
 * 
 * @author Franz-Josef Elmer
 */
@Test(groups = { "system-cleandb", "broken" })
public class AssignDataSetToExperimentAndSampleTest extends AbstractDataSetAssignmentTestCase
{

    @Autowired
    protected IApplicationServerApi v3api;

    @Override
    protected void reassignToExperiment(String dataSetCode, String experimentIdentifierOrNull, String userSessionToken)
    {
        reassignDataSet(dataSetCode, experimentIdentifierOrNull, null, userSessionToken);
    }

    @Override
    protected void reassignToSample(String dataSetCode, String samplePermIdOrNull, String userSessionToken)
    {
        reassignDataSet(dataSetCode, null, samplePermIdOrNull, userSessionToken);
    }
    
    private void reassignDataSet(String dataSetCode, String experimentIdentifierOrNull, String samplePermIdOrNull, 
            String userSessionToken)
    {
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(new DataSetPermId(dataSetCode));
        if (experimentIdentifierOrNull != null)
        {
            dataSetUpdate.setExperimentId(new ExperimentIdentifier(experimentIdentifierOrNull));
        }
        if (samplePermIdOrNull != null)
        {
            dataSetUpdate.setSampleId(new SamplePermId(samplePermIdOrNull));
        }
        v3api.updateDataSets(userSessionToken, Arrays.asList(dataSetUpdate));
        
    }

}
