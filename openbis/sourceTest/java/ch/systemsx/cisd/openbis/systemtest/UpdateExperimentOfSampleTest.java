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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.RolePermutator;

/**
 * @author anttil
 */
public class UpdateExperimentOfSampleTest extends BaseTest
{
    private Sample sample;

    private Experiment sourceExperiment;

    private Experiment destinationExperiment;

    private Space sourceSpace;

    private Space destinationSpace;

    @Test
    public void sampleIsAssociatedWithTheNewExperiment() throws Exception
    {
        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(serverSays(sample), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void dataSetsOfTheSampleAreAssociatedWithTheNewExperiment() throws Exception
    {
        ExternalData dataSet = create(aDataSet().inSample(sample));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(serverSays(dataSet), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void sampleIsAssociatedWithTheSpaceOfTheNewExperiment()
            throws Exception
    {
        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

    @Test
    public void updatingTheSampleToHaveNoExperimentRemovesTheExperimentAssociation()
            throws Exception
    {
        perform(anUpdateOf(sample).removingExperiment());

        assertThat(serverSays(sample).getExperiment(), is(nullValue()));
    }

    @Test
    public void updatingTheSampleToHaveNoExperimentKeepsTheSampleInTheSameSpace() throws Exception
    {
        perform(anUpdateOf(sample).removingExperiment());

        assertThat(serverSays(sample), is(inSpace(sourceSpace)));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void updatingTheSampleToHaveNoExperimentIsNotAllowedIfTheSampleHasDataSets()
            throws Exception
    {
        create(aDataSet().inSample(sample));

        perform(anUpdateOf(sample).removingExperiment());
    }

    @Test(dataProvider = "rolesAllowedToUpdateExperimentOfSample")
    public void updatingTheExperimentOfSampleIsAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToUpdateExperimentOfSample", expectedExceptions =
        { AuthorizationFailureException.class })
    public void updatingTheExperimentOfSampleIsNotAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment).as(user));
    }

    @BeforeClass
    void createFixture() throws Exception
    {
        sourceSpace = create(aSpace());
        destinationSpace = create(aSpace());

        Project sourceProject = create(aProject().inSpace(sourceSpace));
        Project destinationProject = create(aProject().inSpace(destinationSpace));

        sourceExperiment =
                create(anExperiment().inProject(sourceProject).withCode("sourceExperiment"));
        destinationExperiment =
                create(anExperiment().inProject(destinationProject)
                        .withCode("destinationExperiment"));

        sample = create(aSample().inExperiment(sourceExperiment));
    }

    @DataProvider(name = "rolesAllowedToUpdateExperimentOfSample")
    public static Object[][] rolesAllowedToAssignSampleToExperiment()
    {
        RolePermutator rp =
                new RolePermutator(2, RoleWithHierarchy.SPACE_POWER_USER);
        return rp.getAcceptedPermutations();
    }

    @DataProvider(name = "rolesNotAllowedToUpdateExperimentOfSample")
    public static Object[][] rolesNotAllowedToAssignSampleToExperiment()
    {
        RolePermutator rp =
                new RolePermutator(2, RoleWithHierarchy.SPACE_POWER_USER);
        return rp.getRejectedPermutations();
    }

}
