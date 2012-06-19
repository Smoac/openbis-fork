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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;

/**
 * The tests in this class exercise all the methods of RelationshipService through CommonServer
 * 
 * @author anttil
 */
@Test(groups = "system test")
public class EntityRelationshipChangeTest extends BaseTest
{

    @Test
    public void dataSetCanBeUpdatedToAnotherExperiment()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment sourceExperiment = create(anExperiment().inProject(project));
        Experiment destinationExperiment = create(anExperiment().inProject(project));
        DataSet dataset = create(aDataSet().inExperiment(sourceExperiment));
        String session = create(aSession().withInstanceRole(RoleCode.ADMIN));
        DataSetUpdatesDTO updates =
                create(anUpdateOf(dataset).withExperiment(destinationExperiment));

        commonServer.updateDataSet(session, updates);

        assertThat(serverSays(dataset), is(inExperiment(destinationExperiment)));
    }

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
    public void updatingSampleToAnotherExperimentUpdatesTheExperimentOfAllDatasetsInSample()
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
                create(anUpdateOf(serverSays(sample)).inExperiment(destinationExperiment));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample), is(inExperiment(destinationExperiment)));
        assertThat(serverSays(data), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void updatingSampleToHaveNoExperimentIsAllowedIfSampleDoesNotHaveDataSets()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));

        String session = create(aSession().withInstanceRole(RoleCode.ADMIN));

        SampleUpdatesDTO updates =
                create(anUpdateOf(sample).withoutExperiment());

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample).getExperiment(), is(nullValue()));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void updatingSampleToHaveNoExperimentIsNotAllowedIfSampleHasDataSets()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));
        create(aDataSet().inSample(sample));

        String session = create(aSession().withInstanceRole(RoleCode.ADMIN));

        SampleUpdatesDTO updates =
                create(anUpdateOf(serverSays(sample)).withoutExperiment());

        commonServer.updateSample(session, updates);
    }

    @Test
    public void experimentCanBeUpdatedToContainSpaceSamples()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inSpace(space));

        String session = create(aSession().withInstanceRole(RoleCode.ADMIN));

        ExperimentUpdatesDTO updates =
                create(anUpdateOf(experiment).withSamples(sample));

        commonServer.updateExperiment(session, updates);

        assertThat(serverSays(sample), is(inExperiment(experiment)));
    }

    @Test
    public void sampleBecomesSpaceSampleIfExperimentUpdateRemovesItFromExperiment()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));
        Sample anotherSample = create(aSample().inExperiment(experiment));

        String session = create(aSession().withInstanceRole(RoleCode.ADMIN));

        ExperimentUpdatesDTO updates =
                create(anUpdateOf(experiment).withSamples(sample));

        commonServer.updateExperiment(session, updates);

        assertThat(serverSays(sample), is(inExperiment(experiment)));
        assertThat(serverSays(anotherSample).getExperiment(), is(nullValue()));

    }

    @Test
    public void experimentsCanBeUpdatedToAnotherProject()
    {
        Space space = create(aSpace());

        Project sourceProject = create(aProject().inSpace(space));
        Project destinationProject = create(aProject().inSpace(space));

        Experiment experiment = create(anExperiment().inProject(sourceProject));

        ExperimentUpdatesDTO updates =
                create(anUpdateOf(experiment).withProject(destinationProject));

        String session =
                create(aSession().withInstanceRole(RoleCode.ADMIN));

        commonServer.updateExperiment(session, updates);
    }

    @Test
    public void updatingExperimentToProjectInAnotherSpaceChangesTheSpaceOfSamplesInThatExperiment()
    {
        Space sourceSpace = create(aSpace());
        Space destinationSpace = create(aSpace());

        Project sourceProject = create(aProject().withCode("source").inSpace(sourceSpace));
        Project destinationProject =
                create(aProject().withCode("destination").inSpace(destinationSpace));

        Experiment experiment = create(anExperiment().inProject(sourceProject));

        Sample sample = create(aSample().inExperiment(experiment));

        ExperimentUpdatesDTO updates =
                create(anUpdateOf(experiment).withProject(destinationProject));

        String session =
                create(aSession().withSpaceRole(RoleCode.ADMIN, sourceSpace).withSpaceRole(
                        RoleCode.ADMIN, destinationSpace));

        commonServer.updateExperiment(session, updates);

        assertThat(serverSays(experiment), is(inProject(destinationProject)));
        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

    @Test
    public void dataSetCanBeUpdatedToAnotherSample()
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));

        Sample sourceSample = create(aSample().inExperiment(experiment));
        Sample destinationSample = create(aSample().inExperiment(experiment));

        DataSet dataset = create(aDataSet().inSample(sourceSample));
        String session = create(aSession().withInstanceRole(RoleCode.ADMIN));

        DataSetUpdatesDTO updates =
                create(anUpdateOf(dataset).withSample(destinationSample));

        commonServer.updateDataSet(session, updates);

        assertThat(serverSays(dataset), is(inSample(destinationSample)));

    }

    @Test
    public void updatingProjectToAnotherSpaceChangesSpaceOfAllSamplesOfAllExperimentsInThatProject()
    {
        Space sourceSpace = create(aSpace());
        Space destinationSpace = create(aSpace());
        Project project = create(aProject().inSpace(sourceSpace));

        Experiment experiment1 = create(anExperiment().inProject(project));
        Experiment experiment2 = create(anExperiment().inProject(project));

        Sample sample1 = create(aSample().inExperiment(experiment1));
        Sample sample2 = create(aSample().inExperiment(experiment2));

        ProjectUpdatesDTO updates = create(anUpdateOf(project).withSpace(destinationSpace));

        String session =
                create(aSession().withSpaceRole(RoleCode.ADMIN, sourceSpace).withSpaceRole(
                        RoleCode.ADMIN, destinationSpace));

        commonServer.updateProject(session, updates);

        assertThat(serverSays(project), is(inSpace(destinationSpace)));
        assertThat(serverSays(sample1), is(inSpace(destinationSpace)));
        assertThat(serverSays(sample2), is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceLevelSampleCanBeUpdatedToAnotherSpace()
    {
        Space sourceSpace = create(aSpace());
        Space destinationSpace = create(aSpace());
        Sample sample = create(aSample().inSpace(sourceSpace));

        SampleUpdatesDTO updates =
                create(anUpdateOf(sample).inSpace(destinationSpace));

        String session =
                create(aSession().withInstanceRole(RoleCode.ADMIN));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample), is(inSpace(destinationSpace)));

    }

    @Test
    public void spaceLevelSampleCanBeUpdatedToSharedSample()
    {
        Space space = create(aSpace());
        Sample sample = create(aSample().inSpace(space));

        SampleUpdatesDTO updates = create(anUpdateOf(sample).withoutSpace());

        String session =
                create(aSession().withInstanceRole(RoleCode.ADMIN));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample).getSpace(), is(nullValue()));
    }

    @Test
    public void sharedSampleCanBeUpdatedToSpaceLevelSample()
    {
        Sample sample = create(aSample());
        Space space = create(aSpace());
        SampleUpdatesDTO updates = create(anUpdateOf(sample).inSpace(space));

        String session =
                create(aSession().withInstanceRole(RoleCode.ADMIN));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample), is(inSpace(space)));
    }
}
