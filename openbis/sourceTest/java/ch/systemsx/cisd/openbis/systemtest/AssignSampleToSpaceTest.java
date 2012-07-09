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
public class AssignSampleToSpaceTest extends BaseTest
{

    public Experiment experiment;

    public Space sourceSpace;

    public Space destinationSpace;

    @Test
    public void sampleWithExperimentCanBeAssignedToNewSpace() throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).toSpace(destinationSpace));

        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

    @Test
    public void experimentAssignmentOfSampleIsRemovedWhenSampleIsAssignedToNewSpace()
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(sample).toSpace(destinationSpace));

        assertThat(serverSays(sample).getExperiment(), is(nullValue()));
    }

    @Test
    public void spaceSampleCanBeAssignedToNewSpace() throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));

        perform(anUpdateOf(sample).toSpace(destinationSpace));

        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

    @Test
    public void sharedSampleCanBeAssignedToSpace() throws Exception
    {
        Sample sample = create(aSample());

        perform(anUpdateOf(sample).toSpace(destinationSpace));

        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

    @Test
    public void childSampleCanBeAssignedToNewSpace()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().inExperiment(experiment).withParent(parent));

        perform(anUpdateOf(child).toSpace(destinationSpace));

        assertThat(serverSays(child), is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceAssignmentOfParentSampleIsNotChangedWhenChildSampleIsAssignedToNewSpace()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().inExperiment(experiment).withParent(parent));

        perform(anUpdateOf(child).toSpace(destinationSpace));

        assertThat(serverSays(parent), is(inSpace(sourceSpace)));
    }

    @Test
    public void parentSampleCanBeAssignedToNewSpace() throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        create(aSample().inExperiment(experiment).withParent(parent));

        perform(anUpdateOf(parent).toSpace(destinationSpace));

        assertThat(serverSays(parent), is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceAssignmentOfChildSampleIsNotChangedWhenParentSampleIsAssignedToNewSpace()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(experiment));
        Sample child = create(aSample().inExperiment(experiment).withParent(parent));

        perform(anUpdateOf(parent).toSpace(destinationSpace));

        assertThat(serverSays(child), is(inSpace(sourceSpace)));
    }

    @Test
    public void componentSampleCanBeAssignedToNewSpace() throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(component).toSpace(destinationSpace));

        assertThat(serverSays(component), is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceAssignmentOfContainerSampleIsNotChangedWhenComponentSampleIsAssignedToNewSpace()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(component).toSpace(destinationSpace));

        assertThat(serverSays(container), is(inSpace(sourceSpace)));
    }

    @Test
    public void containerSampleCanBeAssignedToNewSpace() throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(container).toSpace(destinationSpace));

        assertThat(serverSays(container), is(inSpace(destinationSpace)));
    }

    @Test
    public void spaceAssignmentOfComponentSampleIsNotChangedWhenContainerSampleIsAssingnedToNewSpace()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(experiment));
        Sample component = create(aSample().inExperiment(experiment).inContainer(container));

        perform(anUpdateOf(container).toSpace(destinationSpace));

        assertThat(serverSays(component), is(inSpace(sourceSpace)));
    }

    @Test(dataProvider = "rolesAllowedToAssignSampleToSpace", groups = "authorization")
    public void assigningSampleToSpaceIsAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));
        Sample sample = create(aSample().inSpace(sourceSpace));

        perform(anUpdateOf(sample).toSpace(destinationSpace).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToSpace", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningSampleToSpaceIsNotAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));
        Sample sample = create(aSample().inSpace(sourceSpace));

        perform(anUpdateOf(sample).toSpace(destinationSpace).as(user));
    }

    @Test(dataProvider = "rolesAllowedToAssignSharedSampleToSpace", groups = "authorization")
    public void assigningSharedSampleToSpaceIsAllowedFor(
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole));
        Sample sharedSample = create(aSample());

        perform(anUpdateOf(sharedSample).toSpace(destinationSpace).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSharedSampleToSpace", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningSharedSampleToSpaceIsNotAllowedFor(
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole));
        Sample sharedSample = create(aSample());

        perform(anUpdateOf(sharedSample).toSpace(destinationSpace).as(user));
    }

    @Test
    public void assigningSampleToSameSpaceIsAllowedToAllSpaceUsersAsNoRealChangeIsMade()
            throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));
        String user = create(aSession().withSpaceRole(RoleWithHierarchy.SPACE_USER, sourceSpace));

        perform(anUpdateOf(sample).toSpace(sourceSpace).as(user));

        assertThat(serverSays(sample), is(inSpace(sourceSpace)));
    }

    @BeforeClass
    void createFixture() throws Exception
    {
        sourceSpace = create(aSpace());
        destinationSpace = create(aSpace());
        Project project = create(aProject().inSpace(sourceSpace));
        experiment =
                create(anExperiment().inProject(project));
    }

    @BeforeClass
    void createAuthorizationRules()
    {
        space1 = new GuardedDomain("space1", RoleLevel.SPACE);
        space2 = new GuardedDomain("space2", RoleLevel.SPACE);
        instance = new GuardedDomain("instance", RoleLevel.INSTANCE);

        assignSampleToSpaceRule =
                or(
                        and(
                                rule(space1, RoleWithHierarchy.SPACE_POWER_USER),
                                rule(space2, RoleWithHierarchy.SPACE_POWER_USER)),

                        and(
                                rule(space1, RoleWithHierarchy.SPACE_USER),
                                rule(space2, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)),

                        rule(instance, RoleWithHierarchy.INSTANCE_ADMIN)
                );

        assignSharedSampleToSpaceRule =
                or(
                        and(
                                rule(space1, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)),
                        rule(instance, RoleWithHierarchy.INSTANCE_ADMIN)
                );
    }

    public GuardedDomain space1;

    public GuardedDomain space2;

    public GuardedDomain instance;

    public AuthorizationRule assignSampleToSpaceRule;

    public AuthorizationRule assignSharedSampleToSpaceRule;

    @DataProvider
    Object[][] rolesAllowedToAssignSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(assignSampleToSpaceRule, space1, space2,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(not(assignSampleToSpaceRule), space1,
                space2, instance);
    }

    @DataProvider
    Object[][] rolesAllowedToAssignSharedSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(assignSharedSampleToSpaceRule,
                space1, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSharedSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(not(assignSharedSampleToSpaceRule),
                space1, instance);
    }
}
