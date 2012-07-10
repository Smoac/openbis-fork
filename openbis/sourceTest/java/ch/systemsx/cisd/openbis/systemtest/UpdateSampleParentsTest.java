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
public class UpdateSampleParentsTest extends BaseTest
{

    Space space;

    @Test
    public void sampleCanBeUpdatedToHaveAnotherSampleAsParent() throws Exception
    {
        Sample parentToBe = create(aSample().inSpace(space));
        Sample childToBe = create(aSample().inSpace(space));

        perform(anUpdateOf(childToBe).toHaveParent(parentToBe));

        assertThat(serverSays(childToBe).getParents(), containsExactly(parentToBe));
    }

    @Test
    public void sampleCanBeUpdatedToHaveDifferentParent() throws Exception
    {
        Sample currentParent = create(aSample().inSpace(space));
        Sample child = create(aSample().inSpace(space).withParent(currentParent));
        Sample newParent = create(aSample().inSpace(space));

        perform(anUpdateOf(child).toHaveParent(newParent));

        assertThat(serverSays(child).getParents(), containsExactly(newParent));
    }

    @Test
    public void parentOfASampleCanBeRemoved() throws Exception
    {
        Sample parent1 = create(aSample().inSpace(space));
        Sample parent2 = create(aSample().inSpace(space));
        Sample child = create(aSample().inSpace(space).withParents(parent1, parent2));

        perform(anUpdateOf(child).toHaveParent(parent1));

        assertThat(serverSays(child).getParents(), containsExactly(parent1));
    }

    @Test
    public void allParentsOfSampleCanBeRemoved() throws Exception
    {
        Sample parent1 = create(aSample().inSpace(space));
        Sample parent2 = create(aSample().inSpace(space));
        Sample child = create(aSample().inSpace(space).withParents(parent1, parent2));

        perform(anUpdateOf(child).toHaveParents());

        assertThat(serverSays(child).getParents().size(), is(0));
    }

    @Test
    public void duplicateParentDefinitionsAreSilentlyDismissed() throws Exception
    {
        Sample parent1 = create(aSample().inSpace(space));
        Sample parent2 = create(aSample().inSpace(space));
        Sample child = create(aSample().inSpace(space));

        perform(anUpdateOf(child).toHaveParents(parent1, parent2, parent1, parent2, parent2));

        assertThat(serverSays(child).getParents(), containsExactly(parent1, parent2));
    }

    @Test
    public void sampleCanBeItsOwnParent() throws Exception
    {
        Sample sample = create(aSample().inSpace(space));

        perform(anUpdateOf(sample).toHaveParent(sample));

        assertThat(serverSays(sample).getParents(), containsExactly(sample));
    }

    @Test
    public void sampleCanBeItsOwnGrandParent() throws Exception
    {
        Sample parent = create(aSample().inSpace(space));
        Sample child = create(aSample().inSpace(space));

        perform(anUpdateOf(child).toHaveParent(parent));
        perform(anUpdateOf(parent).toHaveParent(child));

        assertThat(serverSays(child).getParents(), containsExactly(parent));
        assertThat(serverSays(parent).getParents(), containsExactly(child));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void parentCannotBeInDifferentSpace() throws Exception
    {
        Sample parent = create(aSample().inSpace(space));
        Space anotherSpace = create(aSpace());
        Sample child = create(aSample().inSpace(anotherSpace));

        perform(anUpdateOf(child).toHaveParent(parent));
    }

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    @Test(dataProvider = "rolesAllowedToAddParentToSample", groups = "authorization")
    public void addingParentToSampleIsAllowedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample parentToBe = create(aSample().inSpace(space));
        Sample childToBe = create(aSample().inSpace(space));
        String user =
                create(aSession()
                        .withSpaceRole(spaceRole, space)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(childToBe).toHaveParent(parentToBe).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAddParentToSample", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void addingParentToSampleIsNotAllowedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample parentToBe = create(aSample().inSpace(space));
        Sample childToBe = create(aSample().inSpace(space));
        String user =
                create(aSession()
                        .withSpaceRole(spaceRole, space)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(childToBe).toHaveParent(parentToBe).as(user));
    }

    @Test(dataProvider = "rolesAllowedToAddParentToSample", groups = "authorization")
    public void removingParentToSampleIsAllowedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample parent1 = create(aSample().inSpace(space));
        Sample parent2 = create(aSample().inSpace(space));
        Sample child = create(aSample().inSpace(space).withParents(parent1, parent2));
        String user =
                create(aSession()
                        .withSpaceRole(spaceRole, space)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(child).toHaveParent(parent1).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAddParentToSample", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void removingParentToSampleIsNotAllowedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Sample parent1 = create(aSample().inSpace(space));
        Sample parent2 = create(aSample().inSpace(space));
        Sample child = create(aSample().inSpace(space).withParents(parent1, parent2));
        String user =
                create(aSession()
                        .withSpaceRole(spaceRole, space)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(child).toHaveParent(parent1).as(user));
    }

    @BeforeClass
    void createFixture() throws Exception
    {
        space = create(aSpace());
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
        instance = new InstanceDomain("instance");
        spaceDomain = new SpaceDomain("space", instance);

        addParentToSampleRule =
                or(
                        rule(spaceDomain, RoleWithHierarchy.SPACE_POWER_USER),

                        and(
                                rule(spaceDomain, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)
                        )
                );
    }

    @DataProvider
    Object[][] rolesAllowedToAddParentToSample()
    {
        return RolePermutator.getAcceptedPermutations(addParentToSampleRule, spaceDomain, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAddParentToSample()
    {
        return RolePermutator.getAcceptedPermutations(not(addParentToSampleRule), spaceDomain,
                instance);
    }
}
