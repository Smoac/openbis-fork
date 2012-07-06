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
public class AssignSampleToExperimentTest extends BaseTest
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
    public void sampleIsAssociatedWithTheSpaceOfTheNewExperiment()
            throws Exception
    {
        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

    @Test
    public void dataSetsOfTheSampleAreAssociatedWithTheNewExperiment() throws Exception
    {
        ExternalData dataSet = create(aDataSet().inSample(sample));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(serverSays(dataSet), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void spaceSampleCanBeAssignedToAnExperiment() throws Exception
    {
        Sample spaceSample = create(aSample().inSpace(sourceSpace));

        perform(anUpdateOf(spaceSample).toExperiment(destinationExperiment));

        assertThat(serverSays(spaceSample), is(inExperiment(destinationExperiment)));
        assertThat(serverSays(spaceSample), is(inSpace(destinationSpace)));
    }

    @Test
    public void sharedSampleCanBeAssignedToAnExperiment() throws Exception
    {
        Sample sharedSample = create(aSample());

        perform(anUpdateOf(sharedSample).toExperiment(destinationExperiment));

        assertThat(serverSays(sharedSample), is(inExperiment(destinationExperiment)));
        assertThat(serverSays(sharedSample), is(inSpace(destinationSpace)));
    }

    @Test
    public void childSampleCanBeAssignedToAnotherExperiment() throws Exception
    {
        Sample child = create(aSample().withParent(sample).inExperiment(sourceExperiment));

        perform(anUpdateOf(child).toExperiment(destinationExperiment));

        assertThat(serverSays(child), is(inExperiment(destinationExperiment)));
        assertThat(serverSays(child), is(inSpace(destinationSpace)));
    }

    @Test
    public void parentSampleCanBeAssignedToAnotherExperiment() throws Exception
    {
        Sample child = create(aSample().withParent(sample).inExperiment(sourceExperiment));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(serverSays(sample), is(inExperiment(destinationExperiment)));
        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
        assertThat(serverSays(child), is(inExperiment(sourceExperiment)));
        assertThat(serverSays(child), is(inSpace(sourceSpace)));
    }

    @Test
    public void componentSampleCanBeAssignedToAnotherExperiment() throws Exception
    {
        Sample component = create(aSample().inContainer(sample).inExperiment(sourceExperiment));

        perform(anUpdateOf(component).toExperiment(destinationExperiment));

        assertThat(serverSays(component), is(inExperiment(destinationExperiment)));
        assertThat(serverSays(component), is(inSpace(destinationSpace)));
    }

    @Test
    public void containerSampleCanBeAssignedToAnotherExperiment() throws Exception
    {
        Sample component = create(aSample().inContainer(sample).inExperiment(sourceExperiment));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(serverSays(sample), is(inExperiment(destinationExperiment)));
        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
        assertThat(serverSays(component), is(inExperiment(sourceExperiment)));
        assertThat(serverSays(component), is(inSpace(sourceSpace)));
    }

    @Test(dataProvider = "rolesAllowedToAssignSampleToExperiment")
    public void assigningSampleToExperimentIsAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToExperiment", expectedExceptions =
        { AuthorizationFailureException.class })
    public void assigningSampleToExperimentIsNotAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment).as(user));
    }

    @Test(dataProvider = "rolesAllowedToAssignSharedSampleToExperiment")
    public void assigningSharedSampleToExperimentIsAllowedFor(
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole));
        Sample sharedSample = create(aSample());

        perform(anUpdateOf(sharedSample).toExperiment(destinationExperiment).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSharedSampleToExperiment", expectedExceptions =
        { AuthorizationFailureException.class })
    public void assigningSharedSampleToExperimentIsNotAllowedFor(
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole));
        Sample sharedSample = create(aSample());

        perform(anUpdateOf(sharedSample).toExperiment(destinationExperiment).as(user));
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

    @BeforeClass
    void createAuthorizationRules()
    {
        space1 = new GuardedDomain("space1", RoleLevel.SPACE);
        space2 = new GuardedDomain("space2", RoleLevel.SPACE);
        instance = new GuardedDomain("instance", RoleLevel.INSTANCE);

        assignSampleToExperimentRule =
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

        assignSharedSampleToExperimentRule =
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

    public AuthorizationRule assignSampleToExperimentRule;

    public AuthorizationRule assignSharedSampleToExperimentRule;

    @DataProvider
    Object[][] rolesAllowedToAssignSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(assignSampleToExperimentRule, space1, space2,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(assignSampleToExperimentRule), space1,
                space2, instance);
    }

    @DataProvider
    Object[][] rolesAllowedToAssignSharedSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(assignSharedSampleToExperimentRule,
                space1, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSharedSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(assignSharedSampleToExperimentRule),
                space1, instance);
    }
}
