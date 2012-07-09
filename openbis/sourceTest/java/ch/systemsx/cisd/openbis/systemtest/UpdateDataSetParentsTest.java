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
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;

/**
 * @author anttil
 */
public class UpdateDataSetParentsTest extends BaseTest
{

    Sample sample;

    @Test
    public void dataSetCanBeUpdatedToHaveAnotherDataSetAsItsParent() throws Exception
    {
        ExternalData parentToBe = create(aDataSet().inSample(sample));
        ExternalData childToBe = create(aDataSet().inSample(sample));

        perform(anUpdateOf(childToBe).withParent(parentToBe));

        assertThat(serverSays(childToBe).getParents(), containsExactly(parentToBe));
        assertThat(serverSays(parentToBe).getChildren(), containsExactly(childToBe));
    }

    @Test
    public void dataSetCanBeUpdatedToHaveDifferentParent() throws Exception
    {
        ExternalData parent = create(aDataSet().inSample(sample));
        ExternalData child = create(aDataSet().inSample(sample).withParent(parent));
        ExternalData newParent = create(aDataSet().inSample(sample));

        perform(anUpdateOf(child).withParent(newParent));

        assertThat(serverSays(child).getParents(), containsExactly(newParent));
        assertThat(serverSays(parent).getChildren().size(), is(0));
        assertThat(serverSays(newParent).getChildren(), containsExactly(child));
    }

    @Test
    public void parentOfDataSetCanBeRemoved() throws Exception
    {
        ExternalData parent1 = create(aDataSet().inSample(sample));
        ExternalData parent2 = create(aDataSet().inSample(sample));
        ExternalData child = create(aDataSet().inSample(sample).withParents(parent1, parent2));

        perform(anUpdateOf(child).withParent(parent1));

        assertThat(serverSays(child).getParents(), containsExactly(parent1));
        assertThat(serverSays(parent1).getChildren(), containsExactly(child));
        assertThat(serverSays(parent2).getChildren().size(), is(0));
    }

    @Test
    public void allParentsOfDataSetCanBeRemoved() throws Exception
    {
        ExternalData parent1 = create(aDataSet().inSample(sample));
        ExternalData parent2 = create(aDataSet().inSample(sample));
        ExternalData child = create(aDataSet().inSample(sample).withParents(parent1, parent2));

        perform(anUpdateOf(child).withParents());

        assertThat(serverSays(child).getParents().size(), is(0));
        assertThat(serverSays(parent1).getChildren().size(), is(0));
        assertThat(serverSays(parent2).getChildren().size(), is(0));
    }

    @Test
    public void duplicateParentDefinitionsAreSilentlyDismissed() throws Exception
    {
        ExternalData parent1 = create(aDataSet().inSample(sample));
        ExternalData parent2 = create(aDataSet().inSample(sample));
        ExternalData child = create(aDataSet().inSample(sample));

        perform(anUpdateOf(child).withParents(parent1, parent2, parent1, parent2, parent2));

        assertThat(serverSays(child).getParents(), containsExactly(parent1, parent2));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void dataSetCannotBeItsOwnParent() throws Exception
    {
        ExternalData data = create(aDataSet().inSample(sample));

        perform(anUpdateOf(data).withParent(data));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void dataSetCannotBeItsOwnGrandParent() throws Exception
    {
        ExternalData parent = create(aDataSet().inSample(sample));
        ExternalData child = create(aDataSet().inSample(sample).withParent(parent));

        perform(anUpdateOf(parent).withParent(child));
    }

    @Test
    public void parentCanBeInDifferentSpaceThanChild() throws Exception
    {
        Space parentSpace = create(aSpace());
        Project parentProject = create(aProject().inSpace(parentSpace));
        Experiment parentExperiment = create(anExperiment().inProject(parentProject));
        Sample parentSample = create(aSample().inExperiment(parentExperiment));
        ExternalData parent = create(aDataSet().inSample(parentSample));
        ExternalData child = create(aDataSet().inSample(sample));

        perform(anUpdateOf(child).withParent(parent));

        assertThat(serverSays(child).getParents(), containsExactly(parent));
    }

    @BeforeClass
    protected void createFixture() throws Exception
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        sample = create(aSample().inExperiment(experiment));

    }
}
