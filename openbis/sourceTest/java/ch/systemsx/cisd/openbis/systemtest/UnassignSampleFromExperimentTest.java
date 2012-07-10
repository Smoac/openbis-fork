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
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.auth.AuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.auth.GuardedDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.InstanceDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.RolePermutator;
import ch.systemsx.cisd.openbis.systemtest.base.auth.SpaceDomain;

/**
 * @author anttil
 */
public class UnassignSampleFromExperimentTest extends BaseTest
{
    Experiment experiment;

    Space space;

    @Test
    public void experimentAssignmentOfSampleIsRemoved()
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).removingExperiment());

        assertThat(serverSays(sample).getExperiment(), is(nullValue()));
    }

    @Test
    public void spaceAssociationOfSampleIsLeftIntact() throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).removingExperiment());

        assertThat(serverSays(sample), is(inSpace(space)));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void unassigningFailsIfTheSampleHasDataSets()
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        create(aDataSet().inSample(sample));

        perform(anUpdateOf(sample).removingExperiment());
    }

    @Test
    public void childSampleCanBeUnassigned() throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(child).removingExperiment());

        assertThat(serverSays(child).getExperiment(), is(nullValue()));
    }

    @Test
    public void experimentAssignmentOfParentSampleIsNotChangedWhenChildSampleIsUnassigned()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(child).removingExperiment());

        assertThat(serverSays(parent), is(inExperiment(experiment)));
    }

    @Test
    public void parentSampleCanBeUnassigned() throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(parent).removingExperiment());

        assertThat(serverSays(parent).getExperiment(), is(nullValue()));
    }

    @Test
    public void experimentAssignmentOfChildSampleIsNotChangedWhenParentSampleIsUnassigned()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(parent).removingExperiment());

        assertThat(serverSays(child), is(inExperiment(experiment)));
    }

    @Test
    public void componentSampleCanBeUnassigned() throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inContainer(container).inExperiment(experiment));

        perform(anUpdateOf(component).removingExperiment());

        assertThat(serverSays(component).getExperiment(), is(nullValue()));
    }

    @Test
    public void experimentAssignmentOfContainerSampleIsNotChangedWhenComponentSampleIsUnassigned()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inContainer(container).inExperiment(experiment));

        perform(anUpdateOf(component).removingExperiment());

        assertThat(serverSays(container), is(inExperiment(experiment)));
    }

    @Test
    public void containerSampleCanBeUnassigned() throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        create(aSample().inContainer(container).inExperiment(experiment));

        perform(anUpdateOf(container).removingExperiment());

        assertThat(serverSays(container).getExperiment(), is(nullValue()));
    }

    @Test
    public void experimentAssignmentOfComponentSampleIsNotChangedWhenContainerSampleIsUnassigned()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inContainer(container).inExperiment(experiment));

        perform(anUpdateOf(container).removingExperiment());

        assertThat(serverSays(component), is(inExperiment(experiment)));
    }

    @Test
    public void unassigningCanBeDoneThroughExperimentUpdate() throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(experiment).removingSamples());

        assertThat(serverSays(sample).getExperiment(), is(nullValue()));
    }

    @Test(dataProvider = "rolesAllowedToUnassignSampleFromExperiment", groups = "authorization")
    public void unassigningIsAllowedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));
        String user =
                create(aSession().withSpaceRole(spaceRole, space).withInstanceRole(instanceRole));

        perform(anUpdateOf(sample).removingExperiment().as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToUnassignSampleFromExperiment", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void unassigningIsNotAllowedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));
        String user =
                create(aSession().withSpaceRole(spaceRole, space).withInstanceRole(instanceRole));

        perform(anUpdateOf(sample).removingExperiment().as(user));
    }

    @BeforeClass
    void createFixture() throws Exception
    {
        space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        experiment = create(anExperiment().inProject(project));
    }

    GuardedDomain spaceDomain;

    GuardedDomain instance;

    AuthorizationRule unassignSampleFromExperimentRule;

    @BeforeClass
    void createAuthorizationRules()
    {
        instance = new InstanceDomain("instance");
        spaceDomain = new SpaceDomain("space", instance);

        unassignSampleFromExperimentRule =
                or(
                        rule(spaceDomain, RoleWithHierarchy.SPACE_POWER_USER),

                        and(
                                rule(spaceDomain, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)
                        )
                );
    }

    @DataProvider
    Object[][] rolesAllowedToUnassignSampleFromExperiment()
    {
        return RolePermutator.getAcceptedPermutations(unassignSampleFromExperimentRule,
                spaceDomain, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToUnassignSampleFromExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(unassignSampleFromExperimentRule),
                spaceDomain, instance);
    }
}
