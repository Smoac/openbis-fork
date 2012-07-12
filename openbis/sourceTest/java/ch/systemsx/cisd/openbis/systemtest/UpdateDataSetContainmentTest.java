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

import static ch.systemsx.cisd.openbis.systemtest.base.auth.RuleBuilder.not;
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
public class UpdateDataSetContainmentTest extends BaseTest
{

    Sample sample;

    @Test
    public void dataSetCanBeSetToBeContainedByAnotherDataSetByUpdatingTheContainerDataSet()
            throws Exception
    {
        ExternalData componentCandidate = create(aDataSet().inSample(sample));
        ExternalData container = create(aDataSet().inSample(sample).asContainer());

        perform(anUpdateOf(container).withComponent(componentCandidate));

        assertThat(serverSays(componentCandidate).tryGetContainer().getCode(),
                is(container.getCode()));
    }

    @Test
    public void dataSetCanBeSetToBeContainedByAnotherDataSetByUpdatingTheComponentDataSet()
            throws Exception
    {
        ExternalData componentCandidate = create(aDataSet().inSample(sample));
        ExternalData container = create(aDataSet().inSample(sample).asContainer());

        perform(anUpdateOf(componentCandidate).withContainer(container));

        assertThat(serverSays(componentCandidate).tryGetContainer().getCode(),
                is(container.getCode()));
    }

    @Test
    public void containmentCanBeRemoved() throws Exception
    {
        ExternalData component = create(aDataSet().inSample(sample));
        ExternalData container =
                create(aDataSet().inSample(sample).asContainer().withComponent(component));

        perform(anUpdateOf(container).withComponents());

        assertThat(serverSays(component).tryGetContainer(), is(nullValue()));
    }

    @Test
    public void containerOfDataSetCanBeChanged() throws Exception
    {
        ExternalData component = create(aDataSet().inSample(sample));
        create(aDataSet().inSample(sample).asContainer().withComponent(component));
        ExternalData newContainer = create(aDataSet().inSample(sample).asContainer());

        perform(anUpdateOf(newContainer).withComponent(component));

        assertThat(serverSays(component).tryGetContainer().getCode(),
                is(newContainer.getCode()));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void dataSetCannotContainItself() throws Exception
    {
        ExternalData dataset = create(aDataSet().inSample(sample));

        perform(anUpdateOf(dataset).withComponent(dataset));
    }

    @Test
    public void subcomponentsAreAllowed() throws Exception
    {
        ExternalData component = create(aDataSet().inSample(sample));
        ExternalData container =
                create(aDataSet().inSample(sample).asContainer().withComponent(component));
        ExternalData subcomponent = create(aDataSet().inSample(sample));

        perform(anUpdateOf(serverSays(component)).withComponent(serverSays(subcomponent)));

        assertThat(serverSays(component).tryGetContainer().getCode(), is(container.getCode()));
        assertThat(serverSays(subcomponent).tryGetContainer().getCode(), is(component.getCode()));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void containerCannotBeInDifferentSpaceThanComponent() throws Exception
    {
        Space containerSpace = create(aSpace());
        Project containerProject = create(aProject().inSpace(containerSpace));
        Experiment containerExperiment = create(anExperiment().inProject(containerProject));
        Sample containerSample = create(aSample().inExperiment(containerExperiment));
        ExternalData container = create(aDataSet().inSample(containerSample));
        ExternalData component = create(aDataSet().inSample(sample));

        perform(anUpdateOf(container).withComponent(component));

    }

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    @Test(dataProvider = "rolesAllowedToAddContainerToDataSet", groups = "authorization")
    public void addingContainerToDataSetIsAllowedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        ExternalData container = create(aDataSet().inSample(sample));
        ExternalData component = create(aDataSet().inSample(sample));
        String user =
                create(aSession()
                        .withSpaceRole(spaceRole, sample.getSpace())
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(container).withComponent(component).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAddContainerToDataSet", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void addingContainerToDataSetNotIsAllowedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        ExternalData container = create(aDataSet().inSample(sample));
        ExternalData component = create(aDataSet().inSample(sample));
        String user =
                create(aSession()
                        .withSpaceRole(spaceRole, sample.getSpace())
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(container).withComponent(component).as(user));
    }

    @Test(dataProvider = "rolesAllowedToAddContainerToDataSet", groups = "authorization")
    public void removingContainerFromDataSetIsAllowedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        ExternalData component = create(aDataSet().inSample(sample));
        ExternalData container =
                create(aDataSet().inSample(sample).asContainer().withComponent(component));

        String user =
                create(aSession()
                        .withSpaceRole(spaceRole, sample.getSpace())
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(container).withComponents().as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAddContainerToDataSet", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void removingContainerFromDataSetNotIsAllowedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        ExternalData component = create(aDataSet().inSample(sample));
        ExternalData container =
                create(aDataSet().inSample(sample).asContainer().withComponent(component));
        String user =
                create(aSession()
                        .withSpaceRole(spaceRole, sample.getSpace())
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(container).withComponents().as(user));
    }

    @BeforeClass
    void createFixture() throws Exception
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        sample = create(aSample().inExperiment(experiment));

        unrelatedAdmin = create(aSpace());
        unrelatedObserver = create(aSpace());
        unrelatedNone = create(aSpace());
    }

    GuardedDomain spaceDomain;

    GuardedDomain instance;

    AuthorizationRule addParentToSampleRule;

    @BeforeClass
    void createAuthorizationRules()
    {
        instance = new InstanceDomain();
        spaceDomain = new SpaceDomain(instance);

        addParentToSampleRule = rule(spaceDomain, RoleWithHierarchy.SPACE_POWER_USER);
    }

    @DataProvider
    Object[][] rolesAllowedToAddContainerToDataSet()
    {
        return RolePermutator.getAcceptedPermutations(addParentToSampleRule, spaceDomain, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAddContainerToDataSet()
    {
        return RolePermutator.getAcceptedPermutations(not(addParentToSampleRule), spaceDomain,
                instance);
    }
}
