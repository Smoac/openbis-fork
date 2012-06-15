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

package ch.systemsx.cisd.openbis.systemtest.relationshipservice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;

public class ChangeSpaceOfAProjectTest extends RelationshipServiceTest
{
    @Test
    public void assigningProjecToSpaceChangesSpaceOfAllSamplesOfAllExperimentsInThatProject()
    {
        Space sourceSpace = create(aSpace());
        Space destinationSpace = create(aSpace());
        Project project = create(aProject().inSpace(sourceSpace));

        Experiment experiment1 = create(anExperiment().inProject(project));
        Experiment experiment2 = create(anExperiment().inProject(project));

        Sample sample1 = create(aSample().inExperiment(experiment1));
        Sample sample2 = create(aSample().inExperiment(experiment2));

        ProjectUpdatesDTO updates = create(aProjectUpdate(project).withSpace(destinationSpace));

        String session =
                create(aSession().withSpaceRole(RoleCode.ADMIN, sourceSpace).withSpaceRole(
                        RoleCode.ADMIN, destinationSpace));

        commonServer.updateProject(session, updates);

        assertThat(serverSays(project), is(inSpace(destinationSpace)));
        assertThat(serverSays(sample1), is(inSpace(destinationSpace)));
        assertThat(serverSays(sample2), is(inSpace(destinationSpace)));
    }

    @Test(dataProvider = "All 2-permutations of space level roles weaker than {ADMIN, ADMIN}",
            expectedExceptions =
                { AuthorizationFailureException.class })
    public void assigningProjectToSpaceIsNotAllowedWithSpaceRolesWeakerThanAdmin(
            RoleCode sourceRole, RoleCode destinationRole)
    {
        Space sourceSpace = create(aSpace());
        Space destinationSpace = create(aSpace());
        Project project = create(aProject().inSpace(sourceSpace));

        ProjectUpdatesDTO updates = create(aProjectUpdate(project).withSpace(destinationSpace));

        String session =
                create(aSession().withSpaceRole(sourceRole, sourceSpace).withSpaceRole(
                        destinationRole, destinationSpace));

        commonServer.updateProject(session, updates);
    }

    @Test(dataProvider = "Instance level roles below ADMIN",
            expectedExceptions =
                { AuthorizationFailureException.class })
    public void assigningProjectToSpaceIsNotAllowedWithInstanceRolesWeakerThanAdmin(RoleCode role)
    {
        Space sourceSpace = create(aSpace());
        Space destinationSpace = create(aSpace());
        Project project = create(aProject().inSpace(sourceSpace));

        ProjectUpdatesDTO updates = create(aProjectUpdate(project).withSpace(destinationSpace));

        String session =
                create(aSession().withInstanceRole(role));

        commonServer.updateProject(session, updates);
    }

}
