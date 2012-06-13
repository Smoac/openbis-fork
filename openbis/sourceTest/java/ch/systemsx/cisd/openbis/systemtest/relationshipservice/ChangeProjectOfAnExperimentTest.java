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
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;

/**
 * @author anttil
 */
public class ChangeProjectOfAnExperimentTest extends RelationshipServiceTest
{
    @Test(dataProvider = "All 2-permutations of space level roles weaker than {ADMIN, ADMIN}",
            expectedExceptions =
                { AuthorizationFailureException.class })
    public void assigningExperimentToProjectInDifferentSpaceIsNotAllowedWithoutSpaceAdminRoleOnBothSpaces(
            RoleCode sourceSpaceRole, RoleCode destinationSpaceRole)
    {
        Space sourceSpace = create(aSpace());
        Space destinationSpace = create(aSpace());

        Project sourceProject = create(aProject().inSpace(sourceSpace));
        Project destinationProject = create(aProject().inSpace(destinationSpace));

        Experiment experiment = create(anExperiment().inProject(sourceProject));

        ExperimentUpdatesDTO updates =
                create(anExperimentUpdate(experiment).withProject(destinationProject));

        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole,
                        destinationSpace));

        commonServer.updateExperiment(session, updates);
    }

    @Test(dataProvider = "Instance level roles below ADMIN",
            expectedExceptions =
                { AuthorizationFailureException.class })
    public void assigningExperimentToProjectIsNotAllowedWithInstanceRolesWeakerThanAdmin(
            RoleCode role)
    {
        Space space = create(aSpace());

        Project sourceProject = create(aProject().inSpace(space));
        Project destinationProject = create(aProject().inSpace(space));

        Experiment experiment = create(anExperiment().inProject(sourceProject));

        ExperimentUpdatesDTO updates =
                create(anExperimentUpdate(experiment).withProject(destinationProject));

        String session =
                create(aSession().withInstanceRole(role));

        commonServer.updateExperiment(session, updates);
    }

    @Test
    public void instanceAdminIsAllowedToAssignExperimentToProjectWithoutSpaceAdminRole()
    {
        Space space = create(aSpace());

        Project sourceProject = create(aProject().inSpace(space));
        Project destinationProject = create(aProject().inSpace(space));

        Experiment experiment = create(anExperiment().inProject(sourceProject));

        ExperimentUpdatesDTO updates =
                create(anExperimentUpdate(experiment).withProject(destinationProject));

        String session =
                create(aSession().withInstanceRole(RoleCode.ADMIN));

        commonServer.updateExperiment(session, updates);
    }

    @Test
    public void assigningExperimentToProjectInAnotherSpaceChangesTheSpaceOfSamplesInThatExperimentAccordingly()
    {
        Space sourceSpace = aSpace().create();
        Space destinationSpace = aSpace().create();

        Project sourceProject = aProject().withCode("source").inSpace(sourceSpace).create();
        Project destinationProject =
                aProject().withCode("destination").inSpace(destinationSpace).create();

        Experiment experiment = anExperiment().inProject(sourceProject).create();

        Sample sample = aSample().inExperiment(experiment).create();

        ExperimentUpdatesDTO updates =
                anExperimentUpdate(experiment).withProject(destinationProject).create();

        String session =
                aSession().withSpaceRole(RoleCode.ADMIN, sourceSpace).withSpaceRole(RoleCode.ADMIN,
                        destinationSpace).create();

        commonServer.updateExperiment(session, updates);

        assertThat(serverSays(experiment), is(inProject(destinationProject)));
        assertThat(serverSays(sample), is(inSpace(destinationSpace)));
    }

}
