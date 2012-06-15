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

package ch.systemsx.cisd.openbis.systemtest.relationshipservice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;

/**
 * @author anttil
 */
public class ChangeExperimentOfASample extends RelationshipServiceTest
{
    @Test
    public void registeringAnExperimentWithExistingSpaceLevelSampleAssociatesTheSampleWithTheExperiment()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Sample sample = create(aSample().inSpace(space));

        String session = create(aSession().withInstanceRole(RoleCode.ADMIN));

        Experiment experiment =
                create(anExperiment().asUser(session).withCode("the_experiment").inProject(project)
                        .withSamples(sample));

        assertThat(serverSays(sample), is(inSpace(space)));
        assertThat(serverSays(sample), is(inExperiment(experiment)));
    }

    @Test
    public void updateSampleWithNewExperiment()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment sourceExperiment =
                create(anExperiment().inProject(project).withCode("source_experiment"));
        Experiment destinationExperiment =
                create(anExperiment().inProject(project).withCode("destination_experiment"));

        Sample sample = create(aSample().inExperiment(sourceExperiment));
        ExternalData data = create(aDataSet().inSample(sample));

        String session = create(aSession().withInstanceRole(RoleCode.ADMIN));

        SampleUpdatesDTO updates =
                create(aSampleUpdate(serverSays(sample)).inExperiment(destinationExperiment));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample), is(inExperiment(destinationExperiment)));
        assertThat(serverSays(data), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void updateSampleWithExperimentRemoved()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));

        String session = create(aSession().withInstanceRole(RoleCode.ADMIN));

        SampleUpdatesDTO updates =
                create(aSampleUpdate(sample).withoutExperiment());

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample).getExperiment(), is(nullValue()));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void updateSampleWithExperimentRemovedFailsIfThereAreDataSets()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));
        create(aDataSet().inSample(sample));

        String session = create(aSession().withInstanceRole(RoleCode.ADMIN));

        SampleUpdatesDTO updates =
                create(aSampleUpdate(serverSays(sample)).withoutExperiment());

        commonServer.updateSample(session, updates);
    }

    @Test
    public void updateExperimentWithNewSamples()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inSpace(space));

        String session = create(aSession().withInstanceRole(RoleCode.ADMIN));

        ExperimentUpdatesDTO updates =
                create(anExperimentUpdate(experiment).withSamples(sample));

        commonServer.updateExperiment(session, updates);

        assertThat(serverSays(sample), is(inExperiment(experiment)));
    }

    @Test
    public void updateExperimentWithSamplesRemoved()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));
        Sample anotherSample = create(aSample().inExperiment(experiment));

        String session = create(aSession().withInstanceRole(RoleCode.ADMIN));

        ExperimentUpdatesDTO updates =
                create(anExperimentUpdate(experiment).withSamples(sample));

        commonServer.updateExperiment(session, updates);

        assertThat(serverSays(sample), is(inExperiment(experiment)));
        assertThat(serverSays(anotherSample).getExperiment(), is(nullValue()));

    }

}
