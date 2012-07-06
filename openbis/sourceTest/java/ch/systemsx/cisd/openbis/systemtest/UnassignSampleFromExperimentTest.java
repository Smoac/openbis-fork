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
public class UnassignSampleFromExperimentTest extends BaseTest
{
    private Sample sample;

    private Experiment experiment;

    private Space space;

    @Test
    public void experimentAssociationOfTheSampleIsRemoved()
            throws Exception
    {
        perform(anUpdateOf(sample).removingExperiment());

        assertThat(serverSays(sample).getExperiment(), is(nullValue()));
    }

    @Test
    public void spaceAssociationOfTheSampleIsLeftIntact() throws Exception
    {
        perform(anUpdateOf(sample).removingExperiment());

        assertThat(serverSays(sample), is(inSpace(space)));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void unassigningFailsIfTheSampleHasDataSets()
            throws Exception
    {
        create(aDataSet().inSample(sample));

        perform(anUpdateOf(sample).removingExperiment());
    }

    @Test
    public void childSampleCanBeUnassigned() throws Exception
    {
        Sample child = create(aSample().withParent(sample).inExperiment(experiment));

        perform(anUpdateOf(child).removingExperiment());

        assertThat(serverSays(child).getExperiment(), is(nullValue()));
        assertThat(serverSays(sample), is(inExperiment(experiment)));
    }

    @Test
    public void parentSampleCanBeUnassigned() throws Exception
    {
        Sample child = create(aSample().withParent(sample).inExperiment(experiment));

        perform(anUpdateOf(sample).removingExperiment());

        assertThat(serverSays(sample).getExperiment(), is(nullValue()));
        assertThat(serverSays(child), is(inExperiment(experiment)));
    }

    @Test
    public void componentSampleCanBeUnassigned() throws Exception
    {
        Sample component = create(aSample().inContainer(sample).inExperiment(experiment));

        perform(anUpdateOf(component).removingExperiment());

        assertThat(serverSays(component).getExperiment(), is(nullValue()));
        assertThat(serverSays(sample), is(inExperiment(experiment)));
    }

    @Test
    public void containerSampleCanBeUnassigned() throws Exception
    {
        Sample component = create(aSample().inContainer(sample).inExperiment(experiment));

        perform(anUpdateOf(sample).removingExperiment());

        assertThat(serverSays(sample).getExperiment(), is(nullValue()));
        assertThat(serverSays(component), is(inExperiment(experiment)));
    }

    @Test(dataProvider = "rolesAllowedToUnassignSampleFromExperiment")
    public void unassigningIsAllowedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(spaceRole, space).withInstanceRole(instanceRole));

        perform(anUpdateOf(sample).removingExperiment().as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToUnassignSampleFromExperiment", expectedExceptions =
        { AuthorizationFailureException.class })
    public void unassigningIsNotAllowedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String user =
                create(aSession().withSpaceRole(spaceRole, space).withInstanceRole(instanceRole));

        perform(anUpdateOf(sample).removingExperiment().as(user));
    }

    @BeforeClass
    void createFixture() throws Exception
    {
        space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        experiment = create(anExperiment().inProject(project));
        sample = create(aSample().inExperiment(experiment));
    }

    @BeforeClass
    void createAuthorizationRules()
    {

        spaceDomain = new GuardedDomain("space", RoleLevel.SPACE);
        instance = new GuardedDomain("instance", RoleLevel.INSTANCE);

        unassignSampleFromExperimentRule =
                or(
                        rule(spaceDomain, RoleWithHierarchy.SPACE_POWER_USER),

                        and(
                                rule(spaceDomain, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)),

                        rule(instance, RoleWithHierarchy.INSTANCE_ADMIN)
                );
    }

    public GuardedDomain spaceDomain;

    public GuardedDomain instance;

    public AuthorizationRule unassignSampleFromExperimentRule;

    @DataProvider
    Object[][] rolesAllowedToUnassignSampleFromExperiment()
    {
        return RolePermutator.getAcceptedPermutations(unassignSampleFromExperimentRule,
                spaceDomain,
                instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToUnassignSampleFromExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(unassignSampleFromExperimentRule),
                spaceDomain,
                instance);
    }
}
