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
public class UpdateSampleContainmentTest extends BaseTest
{

    Space space;

    @Test
    public void sampleCanBeSetToBeContainedByAnotherSample() throws Exception
    {
        Sample container = create(aSample().inSpace(space));
        Sample componentCandidate = create(aSample().inSpace(space));

        perform(anUpdateOf(componentCandidate).toHaveContainer(container));

        assertThat(serverSays(componentCandidate).getContainer(), is(container));
    }

    @Test
    public void containerCanBeInAnotherSpace() throws Exception
    {
        Space spaceOfContainer = create(aSpace());
        Sample container = create(aSample().inSpace(spaceOfContainer));
        Sample componentCandidate = create(aSample().inSpace(space));

        perform(anUpdateOf(componentCandidate).toHaveContainer(container));

        assertThat(serverSays(componentCandidate).getContainer(), is(container));
    }

    @Test
    public void containerCanBeSharedSample() throws Exception
    {
        Sample container = create(aSample());
        Sample componentCandidate = create(aSample().inSpace(space));

        perform(anUpdateOf(componentCandidate).toHaveContainer(container));

        assertThat(serverSays(componentCandidate).getContainer(), is(container));
    }

    @Test
    public void componentCanBeSharedSample() throws Exception
    {
        Sample container = create(aSample().inSpace(space));
        Sample componentCandidate = create(aSample());

        perform(anUpdateOf(componentCandidate).toHaveContainer(container));

        assertThat(serverSays(componentCandidate).getContainer(), is(container));
    }

    @Test
    public void containmentCanBeRemoved() throws Exception
    {
        Sample container = create(aSample().inSpace(space));
        Sample component = create(aSample().inSpace(space).inContainer(container));

        perform(anUpdateOf(component).removingContainer());

        assertThat(serverSays(component).getContainer(), is(nullValue()));
    }

    @Test
    public void containerOfSampleCanBeChanged() throws Exception
    {
        Sample container = create(aSample().inSpace(space));
        Sample component = create(aSample().inSpace(space).inContainer(container));
        Sample newContainer = create(aSample().inSpace(space));

        perform(anUpdateOf(component).toHaveContainer(newContainer));

        assertThat(serverSays(component).getContainer(), is(newContainer));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void sampleCannotBeUpdatedToBeComponentOfComponentSample() throws Exception
    {
        Sample container = create(aSample().inSpace(space));
        Sample component = create(aSample().inSpace(space).inContainer(container));
        Sample subComponent = create(aSample().inSpace(space));

        perform(anUpdateOf(subComponent).toHaveContainer(component));
    }

    @Test
    public void sampleCanContainItself() throws Exception
    {
        Sample sample = create(aSample().inSpace(space));

        perform(anUpdateOf(sample).toHaveContainer(sample));

        assertThat(serverSays(sample).getContainer(), is(serverSays(sample)));
    }

    Space containerSpace;

    Space componentSpace;

    @Test(dataProvider = "rolesAllowedToSetContainerToSample", groups = "authorization")
    public void settingContainerToSampleIsAllowedFor(
            RoleWithHierarchy containerSpaceRole,
            RoleWithHierarchy componentSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample container = create(aSample().inSpace(containerSpace));
        Sample componentCandidate = create(aSample().inSpace(componentSpace));

        String user =
                create(aSession().withSpaceRole(containerSpaceRole, containerSpace).withSpaceRole(
                        componentSpaceRole, componentSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(componentCandidate).toHaveContainer(container).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToSetContainerToSample", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void settingContainerToSampleIsNotAllowedFor(
            RoleWithHierarchy containerSpaceRole,
            RoleWithHierarchy componentSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample container = create(aSample().inSpace(containerSpace));
        Sample componentCandidate = create(aSample().inSpace(componentSpace));

        String user =
                create(aSession().withSpaceRole(containerSpaceRole, containerSpace).withSpaceRole(
                        componentSpaceRole, componentSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(componentCandidate).toHaveContainer(container).as(user));
    }

    @Test(dataProvider = "rolesAllowedToRemoveContainerFromSample", groups = "authorization")
    public void removingContainerFromSampleIsAllowedFor(
            RoleWithHierarchy containerSpaceRole,
            RoleWithHierarchy componentSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample container = create(aSample().inSpace(containerSpace));
        Sample component = create(aSample().inSpace(componentSpace).inContainer(container));

        String user =
                create(aSession().withSpaceRole(containerSpaceRole, containerSpace).withSpaceRole(
                        componentSpaceRole, componentSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(component).removingContainer().as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToRemoveContainerFromSample", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void removingContainerFromSampleIsNotAllowedFor(
            RoleWithHierarchy containerSpaceRole,
            RoleWithHierarchy componentSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample container = create(aSample().inSpace(containerSpace));
        Sample component = create(aSample().inSpace(componentSpace).inContainer(container));

        String user =
                create(aSession().withSpaceRole(containerSpaceRole, containerSpace).withSpaceRole(
                        componentSpaceRole, componentSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(component).removingContainer().as(user));
    }

    @BeforeClass
    void createFixture() throws Exception
    {
        space = create(aSpace());
        containerSpace = create(aSpace());
        componentSpace = create(aSpace());
    }

    GuardedDomain containerDomain;

    GuardedDomain componentDomain;

    GuardedDomain instance;

    AuthorizationRule setContainerToSampleRule;

    AuthorizationRule removeContainerFromSampleRule;

    @BeforeClass
    void createAuthorizationRules()
    {
        instance = new InstanceDomain("instance");
        containerDomain = new SpaceDomain("container", instance);
        componentDomain = new SpaceDomain("component", instance);

        setContainerToSampleRule =
                or(
                        and(
                                or(
                                        rule(containerDomain, RoleWithHierarchy.SPACE_POWER_USER),
                                        rule(containerDomain, RoleWithHierarchy.SPACE_ETL_SERVER)
                                ),
                                rule(componentDomain, RoleWithHierarchy.SPACE_POWER_USER)

                        ),
                        and(
                                rule(componentDomain, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)
                        )
                );

        removeContainerFromSampleRule =
                or(
                        rule(componentDomain, RoleWithHierarchy.SPACE_POWER_USER),

                        and(
                                rule(componentDomain, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)
                        )
                );
    }

    @DataProvider
    Object[][] rolesAllowedToSetContainerToSample()
    {
        return RolePermutator.getAcceptedPermutations(setContainerToSampleRule, containerDomain,
                componentDomain,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToSetContainerToSample()
    {
        return RolePermutator.getAcceptedPermutations(not(setContainerToSampleRule),
                containerDomain,
                componentDomain, instance);
    }

    @DataProvider
    Object[][] rolesAllowedToRemoveContainerFromSample()
    {
        return RolePermutator.getAcceptedPermutations(removeContainerFromSampleRule,
                containerDomain,
                componentDomain,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToRemoveContainerFromSample()
    {
        return RolePermutator.getAcceptedPermutations(not(removeContainerFromSampleRule),
                containerDomain,
                componentDomain, instance);
    }
}
