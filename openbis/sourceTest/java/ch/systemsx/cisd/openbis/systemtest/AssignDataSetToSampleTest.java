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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
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
public class AssignDataSetToSampleTest extends BaseTest
{
    Sample sourceSample;

    Sample destinationSample;

    Experiment sourceExperiment;

    Experiment destinationExperiment;

    Space sourceSpace;

    Space destinationSpace;

    @Test
    public void dataSetWithSampleCanBeAssignedToAnotherSample() throws Exception
    {
        ExternalData dataset = create(aDataSet().inSample(sourceSample));

        perform(anUpdateOf(dataset).toSample(destinationSample));

        assertThat(dataset, is(inSample(destinationSample)));
    }

    @Test
    public void dataSetIsAssignedWithTheExperimentOfTheNewSample() throws Exception
    {
        ExternalData dataset = create(aDataSet().inSample(sourceSample));

        perform(anUpdateOf(dataset).toSample(destinationSample));

        assertThat(dataset, is(inExperiment(destinationExperiment)));
    }

    @Test
    public void dataSetWithoutSampleCanBeAssignedToSample() throws Exception
    {
        ExternalData dataset = create(aDataSet().inExperiment(sourceExperiment));

        perform(anUpdateOf(dataset).toSample(destinationSample));

        assertThat(dataset, is(inSample(destinationSample)));
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
    public void childDataSetCanBeAssignedToAnotherSample() throws Exception
    {
        ExternalData parent = create(aDataSet().inSample(sourceSample));
        ExternalData child = create(aDataSet().inSample(sourceSample).withParent(parent));

        perform(anUpdateOf(child).toSample(destinationSample));

        assertThat(child, is(inSample(destinationSample)));
    }

    @Test
    public void sampleAssignmentOfParentDataSetIsNotChangedWhenChildDataSetIsAssignedToAnotherSample()
            throws Exception
    {
        ExternalData parent = create(aDataSet().inSample(sourceSample));
        ExternalData child = create(aDataSet().inSample(sourceSample).withParent(parent));

        perform(anUpdateOf(child).toSample(destinationSample));

        assertThat(parent, is(inSample(sourceSample)));
    }

    @Test
    public void parentDataSetCanBeAssignedToAnotherSample() throws Exception
    {
        ExternalData parent = create(aDataSet().inSample(sourceSample));
        create(aDataSet().inSample(sourceSample).withParent(parent));

        perform(anUpdateOf(parent).toSample(destinationSample));

        assertThat(parent, is(inSample(destinationSample)));
    }

    @Test
    public void sampleAssignmentOfChildDataSetIsNotChangedWhenParentDatasetIsAssignedToAnotherSample()
            throws Exception
    {
        ExternalData parent = create(aDataSet().inSample(sourceSample));
        ExternalData child = create(aDataSet().inSample(sourceSample).withParent(parent));

        perform(anUpdateOf(parent).toSample(destinationSample));

        assertThat(child, is(inSample(sourceSample)));
    }

    @Test
    public void componentDataSetCanBeAssignedToAnotherSample() throws Exception
    {
        ExternalData component = create(aDataSet().inSample(sourceSample));
        create(aDataSet().inSample(sourceSample).withComponent(component));

        perform(anUpdateOf(component).toSample(destinationSample));

        assertThat(component, is(inSample(destinationSample)));
    }

    @Test
    public void sampleAssignmentOfContainerDataSetIsNotChangedWhenComponentDataSetIsAssignedToAnotherSample()
            throws Exception
    {
        ExternalData component = create(aDataSet().inSample(sourceSample));
        ExternalData container = create(aDataSet().inSample(sourceSample).withComponent(component));

        perform(anUpdateOf(component).toSample(destinationSample));

        assertThat(container, is(inSample(sourceSample)));
    }

    @Test
    public void containerDataSetCanBeAssignedToAnotherSample() throws Exception
    {
        ExternalData component = create(aDataSet().inSample(sourceSample));
        ExternalData container = create(aDataSet().inSample(sourceSample).withComponent(component));

        perform(anUpdateOf(container).toSample(destinationSample));

        assertThat(container, is(inSample(destinationSample)));
    }

    @Test
    public void sampleAssignmentOfComponentDataSetIsNotChangedWhenContainerDataSetIsAssignedToAnotherSample()
            throws Exception
    {
        ExternalData component = create(aDataSet().inSample(sourceSample));
        ExternalData container = create(aDataSet().inSample(sourceSample).withComponent(component));

        perform(anUpdateOf(container).toSample(destinationSample));

        assertThat(component, is(inSample(sourceSample)));
    }

    @Test
    public void dataSetCanBeUnassignedFromSample() throws Exception
    {
        ExternalData data = create(aDataSet().inSample(sourceSample));

        perform(anUpdateOf(data).removingSample());

        assertThat(data, hasNoSample());
        assertThat(data, is(inExperiment(sourceExperiment)));
    }

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    @Test(dataProvider = "rolesAllowedToAssignDataSetToSample", groups = "authorization")
    public void assigningDataSetToSampleIsAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        ExternalData dataset = create(aDataSet().inSample(sourceSample));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(dataset).toSample(destinationSample).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignDataSetToSample", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningDataSetToSampleIsNotAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        ExternalData dataset = create(aDataSet().inSample(sourceSample));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(dataset).toSample(destinationSample).as(user));
    }

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createFixture() throws Exception
    {
        sourceSpace = create(aSpace());
        Project sourceProject = create(aProject().inSpace(sourceSpace));
        sourceExperiment = create(anExperiment().inProject(sourceProject));
        sourceSample = create(aSample().inExperiment(sourceExperiment));

        destinationSpace = create(aSpace());
        Project destinationProject = create(aProject().inSpace(destinationSpace));
        destinationExperiment = create(anExperiment().inProject(destinationProject));
        destinationSample = create(aSample().inExperiment(destinationExperiment));

        unrelatedAdmin = create(aSpace());
        unrelatedObserver = create(aSpace());
        unrelatedNone = create(aSpace());
    }

    GuardedDomain source;

    GuardedDomain destination;

    GuardedDomain instance;

    AuthorizationRule assignDataSetToSampleRule;

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createAuthorizationRules()
    {
        instance = new InstanceDomain();
        source = new SpaceDomain(instance);
        destination = new SpaceDomain(instance);

        assignDataSetToSampleRule =
                and(rule(source, RoleWithHierarchy.SPACE_POWER_USER),
                        rule(destination, RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider
    Object[][] rolesAllowedToAssignDataSetToSample()
    {
        return RolePermutator.getAcceptedPermutations(assignDataSetToSampleRule, source,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignDataSetToSample()
    {
        return RolePermutator.getAcceptedPermutations(not(assignDataSetToSampleRule), source,
                destination, instance);
    }
}
