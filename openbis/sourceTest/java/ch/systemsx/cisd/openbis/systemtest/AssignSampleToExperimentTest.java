/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * @author Franz-Josef Elmer
 */
@Test(groups = { "system-cleandb" })
public class AssignSampleToExperimentTest extends AbstractAssignmentSampleToExperimentTestCase
{
    @Override
    protected void reassignSamplesToExperiment(String experimentIdentifier, List<String> samplePermIds, 
            String userSessionToken)
    {
        Experiment experiment = etlService.tryGetExperiment(systemSessionToken, 
                ExperimentIdentifierFactory.parse(experimentIdentifier));
        perform(anUpdateOf(experiment).withSamples(loadSamples(samplePermIds)).as(userSessionToken));
        
    }
    
    @Override
    protected void reassignSampleToExperiment(String samplePermId, String experimentIdentifierOrNull, 
            String userSessionToken)
    {
        SampleIdentifier sampleIdentifier = etlService.tryGetSampleIdentifier(systemSessionToken, samplePermId);
        Sample sample = etlService.tryGetSampleWithExperiment(systemSessionToken, sampleIdentifier);
        if (experimentIdentifierOrNull != null)
        {
            Experiment experiment = etlService.tryGetExperiment(systemSessionToken, 
                    ExperimentIdentifierFactory.parse(experimentIdentifierOrNull));
            perform(anUpdateOf(sample).toExperiment(experiment).as(userSessionToken));
        } else
        {
            perform(anUpdateOf(sample).removingExperiment().as(userSessionToken));
        }
    }
    
    @Override
    protected String registerExperimentWithSamples(String projectIdentifier, List<String> samplePermIds,
            String userSessionToken)
    {
        Sample[] samples = loadSamples(samplePermIds);
        Project project = commonServer.getProjectInfo(systemSessionToken, ProjectIdentifierFactory.parse(projectIdentifier));
        Experiment experiment = create(anExperiment().inProject(project).withSamples(samples).as(userSessionToken));
        return experiment.getIdentifier();
    }
    
    private Sample[] loadSamples(List<String> samplePermIds)
    {
        List<Sample> samples = new ArrayList<Sample>();
        for (String permId : samplePermIds)
        {
            SampleIdentifier sampleIdentifier = etlService.tryGetSampleIdentifier(systemSessionToken, permId);
            if (sampleIdentifier == null)
            {
                throw new IllegalArgumentException("Unknown sample with perm id: " + permId);
            }
            Sample sample = etlService.tryGetSampleWithExperiment(systemSessionToken, sampleIdentifier);
            if (sample == null)
            {
                throw new IllegalArgumentException("Unknown sample with identifier: " + sampleIdentifier);
            }
            samples.add(sample);
        }
        return samples.toArray(new Sample[0]);
    }

}
