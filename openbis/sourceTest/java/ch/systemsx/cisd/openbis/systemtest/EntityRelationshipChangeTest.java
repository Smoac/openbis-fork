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
    public void createFixture() throws Exception
    {
        space = create(aSpace());
        sourceSpace = create(aSpace());
        destinationSpace = create(aSpace());
        session = create(aSession().withInstanceRole(RoleCode.ADMIN));
    }

    @Test
    public void dataSetCanBeUpdatedToAnotherExperiment() throws Exception
    {
        Project project = create(aProject().inSpace(space));
        Experiment sourceExperiment = create(anExperiment().inProject(project));
        Experiment destinationExperiment = create(anExperiment().inProject(project));
        ExternalData dataset = create(aDataSet().inExperiment(sourceExperiment));
        DataSetUpdatesDTO updates =
                create(anUpdateOf(dataset).withExperiment(destinationExperiment));

        commonServer.updateDataSet(session, updates);

        assertThat(serverSays(dataset), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void registeringAnExperimentWithExistingSpaceLevelSampleAssociatesTheSampleWithTheExperiment()
            throws Exception
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
    public void experimentCanBeUpdatedToContainSpaceSamples() throws Exception
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
            throws Exception
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
    public void experimentsCanBeUpdatedToAnotherProject() throws Exception
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
            throws Exception
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
    public void dataSetCanBeUpdatedToAnotherSample() throws Exception
    {
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sourceSample = create(aSample().inExperiment(experiment));
        Sample destinationSample = create(aSample().inExperiment(experiment));
        ExternalData dataset = create(aDataSet().inSample(sourceSample));
        DataSetUpdatesDTO updates = create(anUpdateOf(dataset).withSample(destinationSample));

        commonServer.updateDataSet(session, updates);

        assertThat(serverSays(dataset), is(inSample(destinationSample)));
    }

    @Test
    public void updatingProjectToAnotherSpaceChangesSpaceOfAllSamplesOfAllExperimentsInThatProject()
            throws Exception
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
    public void spaceLevelSampleCanBeUpdatedToAnotherSpace() throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));
        SampleUpdatesDTO updates =
                create(anUpdateOf(sample).inSpace(destinationSpace));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceLevelSampleCanBeUpdatedToSharedSample() throws Exception
    {
        Sample sample = create(aSample().inSpace(space));
        SampleUpdatesDTO updates = create(anUpdateOf(sample).withoutSpace());

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample).getSpace(), is(nullValue()));
    }

    @Test
    public void sharedSampleCanBeUpdatedToSpaceLevelSample() throws Exception
    {
        Sample sample = create(aSample());
        SampleUpdatesDTO updates = create(anUpdateOf(sample).inSpace(space));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(sample), is(inSpace(space)));
    }

    @Test
    public void addParentToSample() throws Exception
    {
        Sample parentToBe = create(aSample().inSpace(space));
        Sample childToBe = create(aSample().inSpace(space));
        SampleUpdatesDTO updates = create(anUpdateOf(childToBe).withParent(parentToBe));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(childToBe).getParents(), containsExactly(parentToBe));
    }

    @Test
    public void changeParentOfSample() throws Exception
    {
        Sample currentParent = create(aSample().inSpace(space));
        Sample child = create(aSample().inSpace(space).withParent(currentParent));
        Sample newParent = create(aSample().inSpace(space));
        SampleUpdatesDTO updates = create(anUpdateOf(child).withParent(newParent));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(child).getParents(), containsExactly(newParent));
    }

    @Test
    public void removeParentOfSample() throws Exception
    {
        Sample parent1 = create(aSample().inSpace(space));
        Sample parent2 = create(aSample().inSpace(space));

        Sample child = create(aSample().inSpace(space).withParents(parent1, parent2));
        SampleUpdatesDTO updates = create(anUpdateOf(child).withParent(parent1));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(child).getParents(), containsExactly(parent1));
    }

    @Test
    public void duplicateParents() throws Exception
    {
        Sample parent1 = create(aSample().inSpace(space));
        Sample parent2 = create(aSample().inSpace(space));
        Sample child = create(aSample().inSpace(space));
        SampleUpdatesDTO updates = create(anUpdateOf(child).withParents(parent1, parent2, parent1));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(child).getParents(), containsExactly(parent1, parent2));
    }

    @Test
    public void addSampleToContainer() throws Exception
    {
        Sample container = create(aSample().inSpace(space));
        Sample componentCandidate = create(aSample().inSpace(space));
        SampleUpdatesDTO updates = create(anUpdateOf(componentCandidate).withContainer(container));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(componentCandidate).getContainer(), is(container));
    }

    @Test
    public void removeSampleFromContainer() throws Exception
    {
        Sample container = create(aSample().inSpace(space));
        Sample component = create(aSample().inSpace(space).inContainer(container));
        SampleUpdatesDTO updates = create(anUpdateOf(component));

        commonServer.updateSample(session, updates);

        assertThat(serverSays(component).getContainer(), is(nullValue()));
    }

    @Test
    public void sampleUpdateWithNewContainerWillChangeContanerOfSample() throws Exception
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
    public void sampleCannotBeUpdatedToBeComponentOfComponentSample() throws Exception
    {
        Sample container = create(aSample().inSpace(space));
        Sample component = create(aSample().inSpace(space).inContainer(container));
        Sample subComponent = create(aSample().inSpace(space));
        SampleUpdatesDTO updates = create(anUpdateOf(subComponent).withContainer(component));

        commonServer.updateSample(session, updates);
    }

    @Test
    public void addParentToDataSet() throws Exception
    {
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));
        ExternalData parentToBe = create(aDataSet().inSample(sample));
        ExternalData childToBe = create(aDataSet().inSample(sample));
        DataSetUpdatesDTO updates = create(anUpdateOf(childToBe).withParent(parentToBe));

        commonServer.updateDataSet(session, updates);

        assertThat(serverSays(childToBe).getParents(), containsExactly(parentToBe));
        assertThat(serverSays(parentToBe).getChildren(), containsExactly(childToBe));
    }

    @Test
    public void changeParentOfDataSet() throws Exception
    {
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));
        ExternalData currentParent = create(aDataSet().inSample(sample));
        ExternalData newParent = create(aDataSet().inSample(sample));
        ExternalData child = create(aDataSet().inSample(sample).withParent(currentParent));
        DataSetUpdatesDTO updates = create(anUpdateOf(child).withParent(newParent));

        assertThat(serverSays(currentParent).getChildren(), containsExactly(child));

        commonServer.updateDataSet(session, updates);

        assertThat(serverSays(currentParent).getChildren().size(), is(0));
        assertThat(serverSays(child).getParents(), containsExactly(newParent));
        assertThat(serverSays(newParent).getChildren(), containsExactly(child));
    }

    @Test
    public void removeParentOfDataSet() throws Exception
    {
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));
        ExternalData parent = create(aDataSet().inSample(sample));
        ExternalData child = create(aDataSet().inSample(sample).withParent(parent));
        DataSetUpdatesDTO updates = create(anUpdateOf(child));

        commonServer.updateDataSet(session, updates);

        assertThat(serverSays(child).getParents().size(), is(0));
        assertThat(serverSays(parent).getChildren().size(), is(0));
    }

    @Test
    public void addDataSetToContainerThroughUpdatingContainer() throws Exception
    {
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));
        ExternalData componentCandidate = create(aDataSet().inSample(sample));
        ExternalData container = create(aDataSet().inSample(sample).asContainer());
        DataSetUpdatesDTO updates =
                create(anUpdateOf(container).withComponents(componentCandidate));

        commonServer.updateDataSet(session, updates);

        assertThat(serverSays(componentCandidate).tryGetContainer().getCode(),
                is(container.getCode()));
    }

    @Test
    public void addDataSetToContainerThroughUpdatingComponent() throws Exception
    {
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));
        ExternalData componentCandidate = create(aDataSet().inSample(sample));
        ExternalData container = create(aDataSet().inSample(sample).asContainer());
        DataSetUpdatesDTO updates =
                create(anUpdateOf(componentCandidate).withContainer(container));

        commonServer.updateDataSet(session, updates);

        assertThat(serverSays(componentCandidate).tryGetContainer().getCode(),
                is(container.getCode()));
    }

    @Test
    public void removeDataSetFromContainer() throws Exception
    {
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));
        ExternalData component = create(aDataSet().inSample(sample));
        ExternalData container =
                create(aDataSet().inSample(sample).asContainer().withComponent(component));
        DataSetUpdatesDTO updates = create(anUpdateOf(container).withComponents());

        commonServer.updateDataSet(session, updates);

        assertThat(serverSays(component).tryGetContainer(),
                is(nullValue()));

    }

    @Test
    public void dataSetUpdateWithNewContainerWillChangeContainerOfDataSet() throws Exception
    {
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));
        ExternalData component = create(aDataSet().inSample(sample));
        create(aDataSet().inSample(sample).asContainer().withComponent(component));
        ExternalData newContainer = create(aDataSet().inSample(sample).asContainer());
        DataSetUpdatesDTO updates = create(anUpdateOf(newContainer).withComponents(component));

        commonServer.updateDataSet(session, updates);

        assertThat(serverSays(component).tryGetContainer().getCode(),
                is(newContainer.getCode()));

    }
}
