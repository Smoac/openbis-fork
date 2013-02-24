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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
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
public class AssignSampleToExperimentTest extends BaseTest
{
    Experiment sourceExperiment;

    Experiment destinationExperiment;

    Project sourceProject;

    Project destinationProject;

    Space sourceSpace;

    Space destinationSpace;

    @Test
    public void sampleWithExperimentCanBeAssignedToAnotherExperiment() throws Exception
    {
        Sample sample = create(aSample().inExperiment(sourceExperiment));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(sample, is(inExperiment(destinationExperiment)));
    }

    @Test
    public void sampleIsAssignedWithSpaceOfNewExperiment() throws Exception
    {
        Sample sample = create(aSample().inExperiment(sourceExperiment));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(sample, is(inSpace(destinationSpace)));
    }

    @Test
    public void dataSetsOfSampleAreAssociatedWithNewExperiment() throws Exception
    {
        Sample sample = create(aSample().inExperiment(sourceExperiment));
        AbstractExternalData dataSet = create(aDataSet().inSample(sample));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(dataSet, is(inExperiment(destinationExperiment)));
    }

    @Test
    public void spaceSampleCanBeAssignedToExperiment() throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(sample, is(inExperiment(destinationExperiment)));
        assertThat(sample, is(inSpace(destinationSpace)));
    }

    @Test
    public void sharedSampleCanBeAssignedToExperiment() throws Exception
    {
        Sample sample = create(aSample());

        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(sample, is(inExperiment(destinationExperiment)));
        assertThat(sample, is(inSpace(destinationSpace)));
    }

    @Test
    public void childSampleCanBeAssignedToAnotherExperiment() throws Exception
    {
        Sample parent = create(aSample().inExperiment(sourceExperiment));
        Sample child = create(aSample().withParent(parent).inExperiment(sourceExperiment));

        perform(anUpdateOf(child).toExperiment(destinationExperiment));

        assertThat(child, is(inExperiment(destinationExperiment)));
        assertThat(child, is(inSpace(destinationSpace)));
    }

    @Test
    public void experimentAssignmentOfParentSampleIsNotChangedWhenChildSampleIsAssignedToAnotherExperiment()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(sourceExperiment));
        Sample child = create(aSample().withParent(parent).inExperiment(sourceExperiment));

        perform(anUpdateOf(child).toExperiment(destinationExperiment));

        assertThat(parent, is(inExperiment(sourceExperiment)));
        assertThat(parent, is(inSpace(sourceSpace)));
    }

    @Test
    public void parentSampleCanBeAssignedToAnotherExperiment() throws Exception
    {
        Sample parent = create(aSample().inExperiment(sourceExperiment));
        create(aSample().withParent(parent).inExperiment(sourceExperiment));

        perform(anUpdateOf(parent).toExperiment(destinationExperiment));

        assertThat(parent, is(inExperiment(destinationExperiment)));
        assertThat(parent, is(inSpace(destinationSpace)));
    }

    @Test
    public void experimentAssignmentOfChildSampleIsNotChangedWhenParentSampleIsAssignmedToAnotherExperiment()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(sourceExperiment));
        Sample child = create(aSample().withParent(parent).inExperiment(sourceExperiment));

        perform(anUpdateOf(parent).toExperiment(destinationExperiment));

        assertThat(child, is(inExperiment(sourceExperiment)));
        assertThat(child, is(inSpace(sourceSpace)));
    }

    @Test
    public void componentSampleCanBeAssignedToAnotherExperiment() throws Exception
    {
        Sample container = create(aSample().inExperiment(sourceExperiment));
        Sample component = create(aSample().inContainer(container).inExperiment(sourceExperiment));

        perform(anUpdateOf(component).toExperiment(destinationExperiment));

        assertThat(component, is(inExperiment(destinationExperiment)));
        assertThat(component, is(inSpace(destinationSpace)));
    }

    @Test
    public void experimentAssignmentOfContainerSampleIsNotChangedWhenComponentSampleIsAssignedToAnotherExperiment()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(sourceExperiment));
        Sample component = create(aSample().inContainer(container).inExperiment(sourceExperiment));

        perform(anUpdateOf(component).toExperiment(destinationExperiment));

        assertThat(container, is(inExperiment(sourceExperiment)));
        assertThat(container, is(inSpace(sourceSpace)));
    }

    @Test
    public void containerSampleCanBeAssignedToAnotherExperiment() throws Exception
    {
        Sample container = create(aSample().inExperiment(sourceExperiment));
        create(aSample().inContainer(container).inExperiment(sourceExperiment));

        perform(anUpdateOf(container).toExperiment(destinationExperiment));

        assertThat(container, is(inExperiment(destinationExperiment)));
        assertThat(container, is(inSpace(destinationSpace)));
    }

    @Test
    public void experimentAssignmentOfComponentSampleIsNotChangedWhenContainerSampleIsAssignedToAnotherExperiment()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(sourceExperiment));
        Sample component = create(aSample().inContainer(container).inExperiment(sourceExperiment));

        perform(anUpdateOf(container).toExperiment(destinationExperiment));

        assertThat(component, is(inExperiment(sourceExperiment)));
        assertThat(component, is(inSpace(sourceSpace)));
    }

    @Test
    public void sampleWithoutExperimentCanBeAssignedToExperimentInSameSpaceThroughExperimentUpdate()
            throws Exception
    {
        Sample sample = create(aSample().inSpace(destinationSpace));

        perform(anUpdateOf(destinationExperiment).withSamples(sample));

        assertThat(sample, is(inExperiment(destinationExperiment)));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void sampleWithoutExperimentCanNotBeAssignedToExperimentInAnotherSpaceThroughExperimentUpdate()
            throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));

        perform(anUpdateOf(destinationExperiment).withSamples(sample));

        assertThat(sample, is(inExperiment(destinationExperiment)));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void sampleWithExperimentCanNotBeAssignedToAnotherExperimentThroughExperimentUpdate()
            throws Exception
    {
        Experiment destinationExperimentInSameSpace =
                create(anExperiment().inProject(sourceProject));
        Sample sample = create(aSample().inExperiment(sourceExperiment));

        perform(anUpdateOf(destinationExperimentInSameSpace).withSamples(sample));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void sharedSampleCanNotBeAssignedToExperimentThroughExperimentUpdate() throws Exception
    {
        Sample sample = create(aSample());

        perform(anUpdateOf(destinationExperiment).withSamples(sample));

        assertThat(sample, is(inExperiment(destinationExperiment)));
    }

    @Test
    public void registeringExperimentWithSampleInSameSpaceThatIsNotAssignedToAnyExperimentAssignsTheSampleToTheExperiment()
            throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));

        Experiment experiment = create(anExperiment().inProject(sourceProject).withSamples(sample));

        assertThat(sample, is(inExperiment(experiment)));
    }

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    @Test(dataProvider = "rolesAllowedToAssignSampleToExperiment", groups = "authorization")
    public void assigningSampleToExperimentIsAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(sourceExperiment));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToExperiment", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningSampleToExperimentIsNotAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(sourceExperiment));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment).as(user));
    }

    @Test(dataProvider = "rolesAllowedToAssignSharedSampleToExperiment", groups = "authorization")
    public void assigningSharedSampleToExperimentIsAllowedFor(
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample());
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSharedSampleToExperiment", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningSharedSampleToExperimentIsNotAllowedFor(
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample());
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment).as(user));
    }

    @Test(dataProvider = "rolesAllowedToAssignSampleToExperimentThroughExperimentUpdate", groups = "authorization")
    public void assigningSampleToExperimentThroughExperimentUpdateIsAllowedFor(
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample().inSpace(destinationSpace));
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(destinationExperiment).withSamples(sample).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToExperimentThroughExperimentUpdate", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningSampleToExperimentThroughExperimentUpdateIsNotAllowedFor(
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample().inSpace(destinationSpace));
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(destinationExperiment).withSamples(sample).as(user));
    }

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createFixture() throws Exception
    {
        sourceSpace = create(aSpace().withCode("sourceSpace"));
        destinationSpace = create(aSpace().withCode("destinationSpace"));

        sourceProject = create(aProject().inSpace(sourceSpace));
        destinationProject = create(aProject().inSpace(destinationSpace));

        sourceExperiment =
                create(anExperiment().inProject(sourceProject).withCode("sourceExperiment"));
        destinationExperiment =
                create(anExperiment().inProject(destinationProject).withCode(
                        "destinationExperiment"));

        unrelatedAdmin = create(aSpace());
        unrelatedObserver = create(aSpace());
        unrelatedNone = create(aSpace());
    }

    GuardedDomain source;

    GuardedDomain destination;

    GuardedDomain instance;

    AuthorizationRule assignSampleToExperimentRule;

    AuthorizationRule assignSampleToExperimentThroughExperimentUpdateRule;

    AuthorizationRule assignSharedSampleToExperimentRule;

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createAuthorizationRules()
    {
        instance = new InstanceDomain();
        source = new SpaceDomain(instance);
        destination = new SpaceDomain(instance);

        assignSampleToExperimentRule =
                or(and(rule(source, RoleWithHierarchy.SPACE_POWER_USER),
                        rule(destination, RoleWithHierarchy.SPACE_POWER_USER)),

                        and(rule(source, RoleWithHierarchy.SPACE_USER),
                                rule(destination, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)));

        assignSampleToExperimentThroughExperimentUpdateRule =
                or(rule(source, RoleWithHierarchy.SPACE_POWER_USER),

                        and(rule(source, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)));

        assignSharedSampleToExperimentRule =
                and(rule(destination, RoleWithHierarchy.SPACE_USER),
                        rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER));
    }

    @DataProvider
    Object[][] rolesAllowedToAssignSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(assignSampleToExperimentRule, source,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(assignSampleToExperimentRule), source,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesAllowedToAssignSampleToExperimentThroughExperimentUpdate()
    {
        return RolePermutator.getAcceptedPermutations(
                assignSampleToExperimentThroughExperimentUpdateRule, source, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSampleToExperimentThroughExperimentUpdate()
    {
        return RolePermutator.getAcceptedPermutations(
                not(assignSampleToExperimentThroughExperimentUpdateRule), source, instance);
    }

    @DataProvider
    Object[][] rolesAllowedToAssignSharedSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(assignSharedSampleToExperimentRule,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSharedSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(assignSharedSampleToExperimentRule),
                destination, instance);
    }
}
