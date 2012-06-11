/*
 * Copyright 2009 ETH Zuerich, CISD
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
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author anttil
 */
@Test(groups = "system test")
public class RelationshipServiceTest extends SystemTestCase
{

    private SpaceIdentifier sourceSpace = new SpaceIdentifier("CISD", "CISD");

    private SpaceIdentifier destinationSpace = new SpaceIdentifier("CISD", "TESTGROUP");

    private String systemSessionToken;

    private ExperimentUpdatesDTO projectUpdate;

    @BeforeClass
    public void loginSystemUser()
    {
        systemSessionToken = commonServer.tryToAuthenticateAsSystem().getSessionToken();
        projectUpdate = getProjectUpdate();
        createSpaceUser("basic_user", RoleCode.USER, RoleCode.USER);
        createSpaceUser("power_user", RoleCode.POWER_USER, RoleCode.POWER_USER);
        createSpaceUser("source_space_admin", RoleCode.ADMIN, RoleCode.USER);
        createSpaceUser("destination_space_admin", RoleCode.USER, RoleCode.ADMIN);
        createSpaceUser("both_space_admin", RoleCode.ADMIN, RoleCode.ADMIN);
        createInstanceUser("instance_admin", RoleCode.ADMIN);
    }

    @Test(expectedExceptions =
        { AuthorizationFailureException.class })
    public void basicUserIsNotAllowedToUpdateExperienceProjectRelationship()
    {
        String session = authenticate("basic_user");
        commonServer.updateExperiment(session, projectUpdate);
    }

    @Test(expectedExceptions =
        { AuthorizationFailureException.class })
    public void powerUserIsNotAllowedToUpdateExperienceProjectRelationship()
    {
        String session = authenticate("power_user");
        commonServer.updateExperiment(session, projectUpdate);
    }

    @Test(expectedExceptions =
        { AuthorizationFailureException.class })
    public void spaceAdminOfOnlySourceSpaceIsNotAllowedToUpdateExperienceProjectRelationship()
    {
        String session = authenticate("source_space_admin");
        commonServer.updateExperiment(session, projectUpdate);
    }

    @Test(expectedExceptions =
        { AuthorizationFailureException.class })
    public void spaceAdminOfOnlyDestinationSpaceIsNotAllowedToUpdateExperienceProjectRelationship()
    {
        String session = authenticate("destination_space_admin");
        commonServer.updateExperiment(session, projectUpdate);
    }

    @Test
    public void spaceAdminOfBothSpacesIsAllowedToUpdateExperienceProjectRelationship()
    {
        String session = authenticate("both_space_admin");
        commonServer.updateExperiment(session, projectUpdate);

        Experiment experiment =
                commonServer.getExperimentInfo(session,
                        new ExperimentIdentifier(
                                "CISD", "TESTGROUP", "TESTPROJ", "EXP1"));
        assertThat(experiment.getProject().getCode(), is("TESTPROJ"));
    }

    @Test
    public void instanceAdminIsAllowedToUpdateExperienceProjectRelationship()
    {
        String session = authenticate("instance_admin");
        commonServer.updateExperiment(session, projectUpdate);

        Experiment experiment =
                commonServer.getExperimentInfo(session,
                        new ExperimentIdentifier(
                                "CISD", "TESTGROUP", "TESTPROJ", "EXP1"));
        assertThat(experiment.getProject().getCode(), is("TESTPROJ"));
    }

    private String authenticate(String user)
    {
        return commonServer.tryToAuthenticate(user, "password").getSessionToken();
    }

    private void createSpaceUser(String userName, RoleCode sourceSpaceRole,
            RoleCode destinationSpaceRole)
    {
        String sessionToken = commonServer.tryToAuthenticateAsSystem().getSessionToken();
        commonServer.registerPerson(sessionToken, userName);
        commonServer.registerSpaceRole(sessionToken, sourceSpaceRole,
                sourceSpace, Grantee.createPerson(userName));
        commonServer.registerSpaceRole(sessionToken, destinationSpaceRole,
                destinationSpace, Grantee.createPerson(userName));
    }

    private void createInstanceUser(String userName, RoleCode role)
    {
        commonServer.registerPerson(systemSessionToken, userName);
        commonServer.registerInstanceRole(systemSessionToken, role, Grantee.createPerson(userName));
    }

    private ExperimentUpdatesDTO getProjectUpdate()
    {
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        Experiment experiment =
                commonServer.getExperimentInfo(systemSessionToken, new ExperimentIdentifier(
                        "CISD", "CISD", "NEMO", "EXP1"));

        updates.setExperimentId(new TechId(experiment));
        updates.setVersion(experiment.getModificationDate());
        updates.setProjectIdentifier(new ProjectIdentifier("CISD", "TESTGROUP", "TESTPROJ"));
        updates.setProperties(experiment.getProperties());
        updates.setAttachments(new ArrayList<NewAttachment>());
        updates.setNewSamples(new ArrayList<NewSamplesWithTypes>());
        return updates;
    }
}
