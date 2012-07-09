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
public class UnassignSampleFromSpaceTest extends BaseTest
{
    public Space space;

    public Experiment experiment;

    @Test
    public void spaceLevelSampleCanBeUnassignedFromSpace() throws Exception
    {
        Sample sample = create(aSample().inSpace(space));

        perform(anUpdateOf(sample).removingSpace());

        assertThat(serverSays(sample).getSpace(), is(nullValue()));
    }

    @Test
    public void sampleWithAnExperimentCanBeUnassignedFromSpace() throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).removingSpace());

        assertThat(serverSays(sample).getSpace(), is(nullValue()));
    }

    @Test
    public void experimentAssignmentOfSampleIsRemovedWhenSampleIsUnassignedFromSpace()
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).removingSpace());

        assertThat(serverSays(sample).getExperiment(), is(nullValue()));
    }

    @Test
    public void childSampleCanBeUnassignedFromSpace() throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(child).removingSpace());

        assertThat(serverSays(child).getSpace(), is(nullValue()));
    }

    @Test
    public void spaceAssignmentOfParentSampleIsNotChangedWhenChildSampleIsUnassignedFromSpace()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(child).removingSpace());

        assertThat(serverSays(parent), is(inSpace(space)));
    }

    @Test
    public void parentSampleCanBeUnassignedFromSpace() throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(parent).removingSpace());

        assertThat(serverSays(parent).getSpace(), is(nullValue()));
    }

    @Test
    public void spaceAssignmentOfChildSampleIsNotChangedWhenParentSampleIsUnassignedFromSpace()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().withParent(parent).inExperiment(experiment));

        perform(anUpdateOf(parent).removingSpace());

        assertThat(serverSays(child), is(inSpace(space)));
    }

    @Test
    public void componentOfSpaceLevelSampleCanBeShared() throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(component).removingSpace());

        assertThat(serverSays(component).getSpace(), is(nullValue()));
    }

    @Test
    public void spaceAssociationOfContainerSampleIsNotChangedWhenComponentSampleIsUnassignedFromSpace()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(component).removingSpace());

        assertThat(serverSays(container), is(inSpace(space)));
    }

    @Test
    public void containerOfSpaceLevelSampleCanBeShared() throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(container).removingSpace());

        assertThat(serverSays(container).getSpace(), is(nullValue()));
    }

    @Test
    public void spaceAssociationOfComponentSampleIsNotChangedWhenContainerSampleIsUnassignedFromSpace()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(container).removingSpace());

        assertThat(serverSays(component), is(inSpace(space)));
    }

    @Test(dataProvider = "rolesAllowedToUnassignSampleFromSpace")
    public void unassigningIsAllowedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(spaceRole, space).withInstanceRole(instanceRole));
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).removingSpace().as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToUnassignSampleFromSpace", expectedExceptions =
        { AuthorizationFailureException.class })
    public void unassigningIsNotAllowedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(spaceRole, space).withInstanceRole(instanceRole));
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).removingSpace().as(user));
    }

    @BeforeClass
    void createFixture() throws Exception
    {
        space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        experiment = create(anExperiment().inProject(project));
    }

    @BeforeClass
    void createAuthorizationRules()
    {

        spaceDomain = new GuardedDomain("space", RoleLevel.SPACE);
        instance = new GuardedDomain("instance", RoleLevel.INSTANCE);

        unassignSampleFromSpaceRule =
                or(
                        and(
                                rule(spaceDomain, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)
                        ),
                        rule(instance, RoleWithHierarchy.INSTANCE_ADMIN)
                );
    }

    public GuardedDomain spaceDomain;

    public GuardedDomain instance;

    public AuthorizationRule unassignSampleFromSpaceRule;

    @DataProvider
    Object[][] rolesAllowedToUnassignSampleFromSpace()
    {
        return RolePermutator.getAcceptedPermutations(unassignSampleFromSpaceRule,
                spaceDomain,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToUnassignSampleFromSpace()
    {
        return RolePermutator.getAcceptedPermutations(not(unassignSampleFromSpaceRule),
                spaceDomain,
                instance);
    }
}
