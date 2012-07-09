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
public class AssignProjectToSpaceTest extends BaseTest
{
    Space sourceSpace;

    Space destinationSpace;

    @Test
    public void projectCanBeAssignedToNewSpace() throws Exception
    {
        Project project = create(aProject().inSpace(sourceSpace));

        perform(anUpdateOf(project).toSpace(destinationSpace));

        assertThat(serverSays(project), is(inSpace(destinationSpace)));
    }

    @Test
    public void assigningProjectToNewSpaceChangesSpaceAssignmentOfSamplesInExperimentsInThatProject()
            throws Exception
    {
        Project project = create(aProject().inSpace(sourceSpace));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));

        perform(anUpdateOf(project).toSpace(destinationSpace));

        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

    @Test(dataProvider = "rolesAllowedToAssignProjectToSpace", groups = "authorization")
    public void assigningProjectToSpaceIsAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Project project = create(aProject().inSpace(sourceSpace));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(project).toSpace(destinationSpace).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignProjectToSpace", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningProjectToSpaceIsNotAllowedFor(
            RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        Project project = create(aProject().inSpace(sourceSpace));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        perform(anUpdateOf(project).toSpace(destinationSpace).as(user));
    }

    @BeforeClass
    protected void createFixture() throws Exception
    {
        sourceSpace = create(aSpace());
        destinationSpace = create(aSpace());
    }

    @BeforeClass
    void createAuthorizationRules()
    {
        space1 = new GuardedDomain("space1", RoleLevel.SPACE);
        space2 = new GuardedDomain("space2", RoleLevel.SPACE);
        instance = new GuardedDomain("instance", RoleLevel.INSTANCE);

        assignProjectToSpaceRule =
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

    public AuthorizationRule assignProjectToSpaceRule;

    @DataProvider
    Object[][] rolesAllowedToAssignProjectToSpace()
    {
        return RolePermutator.getAcceptedPermutations(assignProjectToSpaceRule, space1,
                space2,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignProjectToSpace()
    {
        return RolePermutator.getAcceptedPermutations(not(assignProjectToSpaceRule), space1,
                space2, instance);
    }

}
