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

import static ch.systemsx.cisd.openbis.systemtest.base.auth.RuleBuilder.and;
import static ch.systemsx.cisd.openbis.systemtest.base.auth.RuleBuilder.not;
import static ch.systemsx.cisd.openbis.systemtest.base.auth.RuleBuilder.or;
import static ch.systemsx.cisd.openbis.systemtest.base.auth.RuleBuilder.rule;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.auth.AuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.auth.GuardedDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.RolePermutator;

/**
 * @author anttil
 */
public class AssignDataSetToExperimentTest extends BaseTest
{
    private Experiment sourceExperiment;

    private Experiment destinationExperiment;

    private Space sourceSpace;

    private Space destinationSpace;

    @Test
    public void dataSetWithoutSampleCanBeUpdatedToAnotherExperiment() throws Exception
    {
        ExternalData dataset = create(aDataSet().inExperiment(sourceExperiment));

        perform(anUpdateOf(dataset).toExperiment(destinationExperiment));

        assertThat(serverSays(dataset), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void dataSetWithSampleCanBeUpdatedToAnotherExperiment() throws Exception
    {
        Sample sample = create(aSample().inExperiment(sourceExperiment));
        ExternalData dataset = create(aDataSet().inSample(sample));

        perform(anUpdateOf(dataset).toExperiment(destinationExperiment));

        assertThat(serverSays(dataset), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void sampleAssignmentOfDataSetIsRemovedWhenDataSetIsAssignedToNewExperiment()
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(sourceExperiment));
        ExternalData dataset = create(aDataSet().inSample(sample));

        perform(anUpdateOf(dataset).toExperiment(destinationExperiment));

        assertThat(serverSays(dataset).getSample(), is(nullValue()));
    }

    @Test
    public void childDataSetCanBeAssignedToNewExperiment() throws Exception
    {
        ExternalData parent = create(aDataSet().inExperiment(sourceExperiment));
        ExternalData child = create(aDataSet().inExperiment(sourceExperiment).withParent(parent));

        perform(anUpdateOf(child).toExperiment(destinationExperiment));

        assertThat(serverSays(child), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void experimentAssignmentOfParentDataSetIsNotChangedWhenChildDataSetIsAssignedToNewExperiment()
            throws Exception
    {
        ExternalData parent = create(aDataSet().inExperiment(sourceExperiment));
        ExternalData child = create(aDataSet().inExperiment(sourceExperiment).withParent(parent));

        perform(anUpdateOf(child).toExperiment(destinationExperiment));

        assertThat(serverSays(parent), is(inExperiment(sourceExperiment)));
    }

    @Test
    public void parentDataSetCanBeAssignedToNewExperiment() throws Exception
    {
        ExternalData parent = create(aDataSet().inExperiment(sourceExperiment));
        create(aDataSet().inExperiment(sourceExperiment).withParent(parent));

        perform(anUpdateOf(parent).toExperiment(destinationExperiment));

        assertThat(serverSays(parent), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void experimentAssignmentOfChildDataSetIsNotChangedWhenParentDatasetIsAssignedToNewExperiment()
            throws Exception
    {
        ExternalData parent = create(aDataSet().inExperiment(sourceExperiment));
        ExternalData child = create(aDataSet().inExperiment(sourceExperiment).withParent(parent));

        perform(anUpdateOf(parent).toExperiment(destinationExperiment));

        assertThat(serverSays(child), is(inExperiment(sourceExperiment)));
    }

    @Test
    public void componentDataSetCanBeAssignedToNewSample() throws Exception
    {
        ExternalData component = create(aDataSet().inExperiment(sourceExperiment));
        create(aDataSet().inExperiment(sourceExperiment).withComponent(component));

        perform(anUpdateOf(component).toExperiment(destinationExperiment));

        assertThat(serverSays(component), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void experimentAssignmentOfContainerDataSetIsNotChangedWhenComponentDataSetIsAssignedToNewExperiment()
            throws Exception
    {
        ExternalData component = create(aDataSet().inExperiment(sourceExperiment));
        ExternalData container =
                create(aDataSet().inExperiment(sourceExperiment).withComponent(component));

        perform(anUpdateOf(component).toExperiment(destinationExperiment));

        assertThat(serverSays(container), is(inExperiment(sourceExperiment)));
    }

    @Test
    public void containerDataSetCanBeAssignedToNewSample() throws Exception
    {
        ExternalData component = create(aDataSet().inExperiment(sourceExperiment));
        ExternalData container =
                create(aDataSet().inExperiment(sourceExperiment).withComponent(component));

        perform(anUpdateOf(container).toExperiment(destinationExperiment));

        assertThat(serverSays(container), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void experimentAssignmentOfComponentDataSetIsNotChangedWhenContainerDataSetIsAssignedToNewExperiment()
            throws Exception
    {
        ExternalData component = create(aDataSet().inExperiment(sourceExperiment));
        ExternalData container =
                create(aDataSet().inExperiment(sourceExperiment).withComponent(component));

        perform(anUpdateOf(container).toExperiment(destinationExperiment));

        assertThat(serverSays(component), is(inExperiment(sourceExperiment)));
    }

    @Test(dataProvider = "rolesAllowedToAssignDataSetToExperiment")
    public void assigningDataSetToExperimentIsAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        ExternalData dataset = create(aDataSet().inExperiment(sourceExperiment));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(dataset).toExperiment(destinationExperiment).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignDataSetToExperiment", expectedExceptions =
        { AuthorizationFailureException.class })
    public void assigningDataSetToExperimentIsNotAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        ExternalData dataset = create(aDataSet().inExperiment(sourceExperiment));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(dataset).toExperiment(destinationExperiment).as(user));
    }

    @BeforeClass
    protected void createFixture() throws Exception
    {
        sourceSpace = create(aSpace());
        Project sourceProject = create(aProject().inSpace(sourceSpace));
        sourceExperiment = create(anExperiment().inProject(sourceProject));

        destinationSpace = create(aSpace());
        Project destinationProject = create(aProject().inSpace(destinationSpace));
        destinationExperiment = create(anExperiment().inProject(destinationProject));
    }

    @BeforeClass
    void createAuthorizationRules()
    {
        space1 = new GuardedDomain("space1", RoleLevel.SPACE);
        space2 = new GuardedDomain("space2", RoleLevel.SPACE);
        instance = new GuardedDomain("instance", RoleLevel.INSTANCE);

        assignDataSetToExperimentRule =
                or(
                        and(
                                rule(space1, RoleWithHierarchy.SPACE_POWER_USER),
                                or(
                                        rule(space2, RoleWithHierarchy.SPACE_POWER_USER),
                                        rule(space2, RoleWithHierarchy.SPACE_ETL_SERVER),
                                        rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)
                                )
                        ),
                        rule(instance, RoleWithHierarchy.INSTANCE_ADMIN)
                );
    }

    public GuardedDomain space1;

    public GuardedDomain space2;

    public GuardedDomain instance;

    public AuthorizationRule assignDataSetToExperimentRule;

    @DataProvider
    Object[][] rolesAllowedToAssignDataSetToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(assignDataSetToExperimentRule, space1,
                space2,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignDataSetToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(assignDataSetToExperimentRule), space1,
                space2, instance);
    }
}
