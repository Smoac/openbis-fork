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
import ch.systemsx.cisd.common.exceptions.UserFailureException;
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
public class AssignDataSetToSampleTest extends BaseTest
{

    private Sample sourceSample;

    private Sample destinationSample;

    private Experiment sourceExperiment;

    private Experiment destinationExperiment;

    private Space sourceSpace;

    private Space destinationSpace;

    @Test
    public void dataSetWithSampleCanBeAssignedToNewSample() throws Exception
    {
        ExternalData dataset = create(aDataSet().inSample(sourceSample));

        perform(anUpdateOf(dataset).toSample(destinationSample));

        assertThat(serverSays(dataset), is(inSample(destinationSample)));
    }

    @Test
    public void dataSetIsAssignedWithTheExperimentOfTheNewSample() throws Exception
    {
        ExternalData dataset = create(aDataSet().inSample(sourceSample));

        perform(anUpdateOf(dataset).toSample(destinationSample));

        assertThat(serverSays(dataset), is(inExperiment(destinationExperiment)));
    }

    @Test
    public void dataSetWithoutSampleCanBeAssignedToNewSample() throws Exception
    {
        ExternalData dataset = create(aDataSet().inExperiment(sourceExperiment));

        perform(anUpdateOf(dataset).toSample(destinationSample));

        assertThat(serverSays(dataset), is(inSample(destinationSample)));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void dataSetCannotBeAssignedToSpaceSample() throws Exception
    {
        Sample sample = create(aSample().inSpace(destinationSpace));
        ExternalData dataset = create(aDataSet().inSample(sourceSample));

        perform(anUpdateOf(dataset).toSample(sample));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void dataSetCannotBeAssignedToSharedSample() throws Exception
    {
        Sample sample = create(aSample());
        ExternalData dataset = create(aDataSet().inSample(sourceSample));

        perform(anUpdateOf(dataset).toSample(sample));
    }

    @Test
    public void childDataSetCanBeAssignedToNewSample() throws Exception
    {
        ExternalData parent = create(aDataSet().inSample(sourceSample));
        ExternalData child = create(aDataSet().inSample(sourceSample).withParent(parent));

        perform(anUpdateOf(child).toSample(destinationSample));

        assertThat(serverSays(child), is(inSample(destinationSample)));
    }

    @Test
    public void sampleAssignmentOfParentDataSetIsNotChangedWhenChildDataSetIsAssignedToNewSample()
            throws Exception
    {
        ExternalData parent = create(aDataSet().inSample(sourceSample));
        ExternalData child = create(aDataSet().inSample(sourceSample).withParent(parent));

        perform(anUpdateOf(child).toSample(destinationSample));

        assertThat(serverSays(parent), is(inSample(sourceSample)));
    }

    @Test
    public void parentDataSetCanBeAssignedToNewSample() throws Exception
    {
        ExternalData parent = create(aDataSet().inSample(sourceSample));
        create(aDataSet().inSample(sourceSample).withParent(parent));

        perform(anUpdateOf(parent).toSample(destinationSample));

        assertThat(serverSays(parent), is(inSample(destinationSample)));
    }

    @Test
    public void sampleAssignmentOfChildDataSetIsNotChangedWhenParentDatasetIsAssignedToNewSample()
            throws Exception
    {
        ExternalData parent = create(aDataSet().inSample(sourceSample));
        ExternalData child = create(aDataSet().inSample(sourceSample).withParent(parent));

        perform(anUpdateOf(parent).toSample(destinationSample));

        assertThat(serverSays(child), is(inSample(sourceSample)));
    }

    @Test
    public void componentDataSetCanBeAssignedToNewSample() throws Exception
    {
        ExternalData component = create(aDataSet().inSample(sourceSample));
        create(aDataSet().inSample(sourceSample).withComponent(component));

        perform(anUpdateOf(component).toSample(destinationSample));

        assertThat(serverSays(component), is(inSample(destinationSample)));
    }

    @Test
    public void sampleAssignmentOfContainerDataSetIsNotChangedWhenComponentDataSetIsAssignedToNewSample()
            throws Exception
    {
        ExternalData component = create(aDataSet().inSample(sourceSample));
        ExternalData container = create(aDataSet().inSample(sourceSample).withComponent(component));

        perform(anUpdateOf(component).toSample(destinationSample));

        assertThat(serverSays(container), is(inSample(sourceSample)));
    }

    @Test
    public void containerDataSetCanBeAssignedToNewSample() throws Exception
    {
        ExternalData component = create(aDataSet().inSample(sourceSample));
        ExternalData container = create(aDataSet().inSample(sourceSample).withComponent(component));

        perform(anUpdateOf(container).toSample(destinationSample));

        assertThat(serverSays(container), is(inSample(destinationSample)));
    }

    @Test
    public void sampleAssignmentOfComponentDataSetIsNotChangedWhenContainerDataSetIsAssignedToNewSample()
            throws Exception
    {
        ExternalData component = create(aDataSet().inSample(sourceSample));
        ExternalData container = create(aDataSet().inSample(sourceSample).withComponent(component));

        perform(anUpdateOf(container).toSample(destinationSample));

        assertThat(serverSays(component), is(inSample(sourceSample)));
    }

    @Test(dataProvider = "rolesAllowedToAssignDataSetToSample", groups = "authorization")
    public void assigningDataSetToSampleIsAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        ExternalData dataset = create(aDataSet().inSample(sourceSample));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(dataset).toSample(destinationSample).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignDataSetToSample", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningDataSetToSampleIsNotAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        ExternalData dataset = create(aDataSet().inSample(sourceSample));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(dataset).toSample(destinationSample).as(user));
    }

    @BeforeClass
    protected void createFixture() throws Exception
    {
        sourceSpace = create(aSpace());
        Project sourceProject = create(aProject().inSpace(sourceSpace));
        sourceExperiment = create(anExperiment().inProject(sourceProject));
        sourceSample = create(aSample().inExperiment(sourceExperiment));

        destinationSpace = create(aSpace());
        Project destinationProject = create(aProject().inSpace(destinationSpace));
        destinationExperiment = create(anExperiment().inProject(destinationProject));
        destinationSample = create(aSample().inExperiment(destinationExperiment));
    }

    @BeforeClass
    void createAuthorizationRules()
    {
        space1 = new GuardedDomain("space1", RoleLevel.SPACE);
        space2 = new GuardedDomain("space2", RoleLevel.SPACE);
        instance = new GuardedDomain("instance", RoleLevel.INSTANCE);

        assignDataSetToSampleRule =
                or(
                        and(
                                rule(space1, RoleWithHierarchy.SPACE_POWER_USER),
                                rule(space2, RoleWithHierarchy.SPACE_POWER_USER)),
                        rule(instance, RoleWithHierarchy.INSTANCE_ADMIN)
                );
    }

    public GuardedDomain space1;

    public GuardedDomain space2;

    public GuardedDomain instance;

    public AuthorizationRule assignDataSetToSampleRule;

    @DataProvider
    Object[][] rolesAllowedToAssignDataSetToSample()
    {
        return RolePermutator.getAcceptedPermutations(assignDataSetToSampleRule, space1, space2,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignDataSetToSample()
    {
        return RolePermutator.getAcceptedPermutations(not(assignDataSetToSampleRule), space1,
                space2, instance);
    }
}
