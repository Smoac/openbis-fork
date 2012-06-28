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

import org.testng.annotations.BeforeClass;
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
public class EntityRelationshipChangeTest extends BaseTest
{

    private String session;

    private Space space;

    private Space sourceSpace;

    private Space destinationSpace;

    @BeforeClass
    public void createFixture()
    {
        space = create(aSpace());
        sourceSpace = create(aSpace());
        destinationSpace = create(aSpace());
        session = create(aSession().withInstanceRole(RoleCode.ADMIN));
    }

    @Test
    public void dataSetCanBeUpdatedToAnotherExperiment()
    {
        Project project = create(aProject().inSpace(space));
        Experiment sourceExperiment = create(anExperiment().inProject(project));
        Experiment destinationExperiment = create(anExperiment().inProject(project));
        DataSet dataset = create(aDataSet().inExperiment(sourceExperiment));
        DataSetUpdatesDTO updates =
                create(anUpdateOf(dataset).withExperiment(destinationExperiment));

        commonServer.updateDataSet(session, updates);

        assertThat(serverSays(dataset), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void registeringAnExperimentWithExistingSpaceLevelSampleAssociatesTheSampleWithTheExperiment()
    {
        Project project = create(aProject().inSpace(space));
        Sample sample = create(aSample().inSpace(space));

        Experiment experiment =
                create(anExperiment().asUser(session).withCode("the_experiment").inProject(project)
                        .withSamples(sample));

        assertThat(serverSays(sample), is(inSpace(space)));
        assertThat(serverSays(sample), is(inExperiment(experiment)));
    }

    @Test
    public void updatingSampleToAnotherExperimentUpdatesTheExperimentOfAllDatasetsInSample()
    {
        Project project = create(aProject().inSpace(space));
        Experiment sourceExperiment =
                create(anExperiment().inProject(project).withCode("source_experiment"));
        Experiment destinationExperiment =
                create(anExperiment().inProject(project).withCode("destination_experiment"));
        Sample sample = create(aSample().inExperiment(sourceExperiment));
        ExternalData data = create(aDataSet().inSample(sample));
        SampleUpdatesDTO updates =
                create(anUpdateOf(serverSays(sample)).inExperiment(destinationExperiment));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample), is(inExperiment(destinationExperiment)));
        assertThat(serverSays(data), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void updatingSampleToHaveNoExperimentIsAllowedIfSampleDoesNotHaveDataSets()
    {
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));
        SampleUpdatesDTO updates = create(anUpdateOf(sample).withoutExperiment());

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample).getExperiment(), is(nullValue()));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void updatingSampleToHaveNoExperimentIsNotAllowedIfSampleHasDataSets()
    {
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));
        create(aDataSet().inSample(sample));
        SampleUpdatesDTO updates = create(anUpdateOf(serverSays(sample)).withoutExperiment());

        commonServer.updateSample(session, updates);
    }

    @Test
    public void experimentCanBeUpdatedToContainSpaceSamples()
    {
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inSpace(space));
        ExperimentUpdatesDTO updates = create(anUpdateOf(experiment).withSamples(sample));

        commonServer.updateExperiment(session, updates);

        assertThat(serverSays(sample), is(inExperiment(experiment)));
    }

    @Test
    public void sampleBecomesSpaceSampleIfExperimentUpdateRemovesItFromExperiment()
    {
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));
        Sample anotherSample = create(aSample().inExperiment(experiment));
        ExperimentUpdatesDTO updates = create(anUpdateOf(experiment).withSamples(sample));

        commonServer.updateExperiment(session, updates);

        assertThat(serverSays(sample), is(inExperiment(experiment)));
        assertThat(serverSays(anotherSample).getExperiment(), is(nullValue()));
    }

    @Test
    public void experimentsCanBeUpdatedToAnotherProject()
    {
        Project sourceProject = create(aProject().inSpace(space));
        Project destinationProject = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(sourceProject));
        ExperimentUpdatesDTO updates =
                create(anUpdateOf(experiment).withProject(destinationProject));

        commonServer.updateExperiment(session, updates);

        assertThat(serverSays(experiment), is(inProject(destinationProject)));
    }

    @Test
    public void updatingExperimentToProjectInAnotherSpaceChangesTheSpaceOfSamplesInThatExperiment()
    {
        Project sourceProject = create(aProject().withCode("source").inSpace(sourceSpace));
        Project destinationProject =
                create(aProject().withCode("destination").inSpace(destinationSpace));
        Experiment experiment = create(anExperiment().inProject(sourceProject));
        Sample sample = create(aSample().inExperiment(experiment));
        ExperimentUpdatesDTO updates =
                create(anUpdateOf(experiment).withProject(destinationProject));

        commonServer.updateExperiment(session, updates);

        assertThat(serverSays(experiment), is(inProject(destinationProject)));
        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

    @Test
    public void dataSetCanBeUpdatedToAnotherSample()
    {
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sourceSample = create(aSample().inExperiment(experiment));
        Sample destinationSample = create(aSample().inExperiment(experiment));
        DataSet dataset = create(aDataSet().inSample(sourceSample));
        DataSetUpdatesDTO updates = create(anUpdateOf(dataset).withSample(destinationSample));

        commonServer.updateDataSet(session, updates);

        assertThat(serverSays(dataset), is(inSample(destinationSample)));
    }

    @Test
    public void updatingProjectToAnotherSpaceChangesSpaceOfAllSamplesOfAllExperimentsInThatProject()
    {
        Project project = create(aProject().inSpace(sourceSpace));
        Experiment experiment1 = create(anExperiment().inProject(project));
        Experiment experiment2 = create(anExperiment().inProject(project));
        Sample sample1 = create(aSample().inExperiment(experiment1));
        Sample sample2 = create(aSample().inExperiment(experiment2));
        ProjectUpdatesDTO updates = create(anUpdateOf(project).withSpace(destinationSpace));

        commonServer.updateProject(session, updates);

        assertThat(serverSays(project), is(inSpace(destinationSpace)));
        assertThat(serverSays(sample1), is(inSpace(destinationSpace)));
        assertThat(serverSays(sample2), is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceLevelSampleCanBeUpdatedToAnotherSpace()
    {
        Sample sample = create(aSample().inSpace(sourceSpace));
        SampleUpdatesDTO updates =
                create(anUpdateOf(sample).inSpace(destinationSpace));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceLevelSampleCanBeUpdatedToSharedSample()
    {
        Sample sample = create(aSample().inSpace(space));
        SampleUpdatesDTO updates = create(anUpdateOf(sample).withoutSpace());

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample).getSpace(), is(nullValue()));
    }

    @Test
    public void sharedSampleCanBeUpdatedToSpaceLevelSample()
    {
        Sample sample = create(aSample());
        SampleUpdatesDTO updates = create(anUpdateOf(sample).inSpace(space));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample), is(inSpace(space)));
    }

    @Test
    public void addParent()
    {
        Sample parentToBe = create(aSample().inSpace(space));
        Sample childToBe = create(aSample().inSpace(space));
        SampleUpdatesDTO updates = create(anUpdateOf(childToBe).withParent(parentToBe));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(childToBe).getParents(), containsExactly(parentToBe));
    }

    @Test
    public void changeParentOfSample()
    {
        Sample currentParent = create(aSample().inSpace(space));
        Sample child = create(aSample().inSpace(space).withParent(currentParent));
        Sample newParent = create(aSample().inSpace(space));
        SampleUpdatesDTO updates = create(anUpdateOf(child).withParent(newParent));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(child).getParents(), containsExactly(newParent));
    }

    @Test
    public void removeParentOfSample()
    {
        Sample parent1 = create(aSample().inSpace(space));
        Sample parent2 = create(aSample().inSpace(space));

        Sample child = create(aSample().inSpace(space).withParents(parent1, parent2));
        SampleUpdatesDTO updates = create(anUpdateOf(child).withParent(parent1));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(child).getParents(), containsExactly(parent1));
    }

    @Test
    public void duplicateParents()
    {
        Sample parent1 = create(aSample().inSpace(space));
        Sample parent2 = create(aSample().inSpace(space));
        Sample child = create(aSample().inSpace(space));
        SampleUpdatesDTO updates = create(anUpdateOf(child).withParents(parent1, parent2, parent1));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(child).getParents(), containsExactly(parent1, parent2));
    }

    @Test
    public void addSampleToContainer()
    {
        Sample container = create(aSample().inSpace(space));
        Sample componentCandidate = create(aSample().inSpace(space));
        SampleUpdatesDTO updates = create(anUpdateOf(componentCandidate).withContainer(container));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(componentCandidate).getContainer(), is(container));
    }

    @Test
    public void removeSampleFromContainer()
    {
        Sample container = create(aSample().inSpace(space));
        Sample component = create(aSample().inSpace(space).inContainer(container));
        SampleUpdatesDTO updates = create(anUpdateOf(component));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(component).getContainer(), is(nullValue()));
    }

    @Test
    public void sampleUpdateWithNewContainerWillChangeContanerOfSample()
    {
        Sample container = create(aSample().inSpace(space));
        Sample component = create(aSample().inSpace(space).inContainer(container));
        Sample newContainer = create(aSample().inSpace(space));
        SampleUpdatesDTO updates = create(anUpdateOf(component).withContainer(newContainer));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(component).getContainer(), is(newContainer));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void sampleCannotBeUpdatedToBeChildOfComponentSample()
    {
        Sample container = create(aSample().inSpace(space));
        Sample component = create(aSample().inSpace(space).inContainer(container));
        Sample subComponent = create(aSample().inSpace(space));
        SampleUpdatesDTO updates = create(anUpdateOf(subComponent).withContainer(component));

        commonServer.updateSample(session, updates);
    }
}
