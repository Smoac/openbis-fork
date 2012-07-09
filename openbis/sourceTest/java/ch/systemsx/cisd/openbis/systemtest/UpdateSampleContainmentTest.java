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
        Space containerSpace = create(aSpace());
        Sample container = create(aSample().inSpace(containerSpace));
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

    Space sourceSpace;

    Space destinationSpace;

    @Test(dataProvider = "rolesAllowedToSetContainerToSample", groups = "authorization")
    public void settingContainerToSampleIsAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample container = create(aSample().inSpace(sourceSpace));
        Sample componentCandidate = create(aSample().inSpace(destinationSpace));

        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(componentCandidate).toHaveContainer(container).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToSetContainerToSample", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void settingContainerToSampleIsNotAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample container = create(aSample().inSpace(sourceSpace));
        Sample componentCandidate = create(aSample().inSpace(destinationSpace));

        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(componentCandidate).toHaveContainer(container).as(user));
    }

    @Test(dataProvider = "rolesAllowedToRemoveContainerFromSample", groups = "authorization")
    public void removingContainerFromSampleIsAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample container = create(aSample().inSpace(sourceSpace));
        Sample component = create(aSample().inSpace(destinationSpace).inContainer(container));

        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(component).removingContainer().as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToRemoveContainerFromSample", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void removingContainerFromSampleIsNotAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample container = create(aSample().inSpace(sourceSpace));
        Sample component = create(aSample().inSpace(destinationSpace).inContainer(container));

        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(component).removingContainer().as(user));
    }

    @BeforeClass
    protected void createFixture() throws Exception
    {
        space = create(aSpace());
        sourceSpace = create(aSpace());
        destinationSpace = create(aSpace());
    }

    @BeforeClass
    protected void createAuthorizationRules()
    {
        space1 = new GuardedDomain("space1", RoleLevel.SPACE);
        space2 = new GuardedDomain("space2", RoleLevel.SPACE);
        instance = new GuardedDomain("instance", RoleLevel.INSTANCE);

        setContainerToSampleRule =
                or(
                        and(
                                or(
                                        rule(space1, RoleWithHierarchy.SPACE_POWER_USER),
                                        rule(space1, RoleWithHierarchy.SPACE_ETL_SERVER)
                                ),
                                rule(space2, RoleWithHierarchy.SPACE_POWER_USER)

                        ),
                        and(
                                rule(space2, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)
                        ),
                        rule(instance, RoleWithHierarchy.INSTANCE_ADMIN)
                );

        removeContainerFromSampleRule =
                or(
                        rule(space2, RoleWithHierarchy.SPACE_POWER_USER),

                        and(
                                rule(space2, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)
                        ),
                        rule(instance, RoleWithHierarchy.INSTANCE_ADMIN)
                );
    }

    public GuardedDomain space1;

    public GuardedDomain space2;

    public GuardedDomain instance;

    public AuthorizationRule setContainerToSampleRule;

    public AuthorizationRule removeContainerFromSampleRule;

    @DataProvider
    Object[][] rolesAllowedToSetContainerToSample()
    {
        return RolePermutator.getAcceptedPermutations(setContainerToSampleRule, space1, space2,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToSetContainerToSample()
    {
        return RolePermutator.getAcceptedPermutations(not(setContainerToSampleRule), space1,
                space2, instance);
    }

    @DataProvider
    Object[][] rolesAllowedToRemoveContainerFromSample()
    {
        return RolePermutator.getAcceptedPermutations(removeContainerFromSampleRule, space1,
                space2,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToRemoveContainerFromSample()
    {
        return RolePermutator.getAcceptedPermutations(not(removeContainerFromSampleRule), space1,
                space2, instance);
    }

}
