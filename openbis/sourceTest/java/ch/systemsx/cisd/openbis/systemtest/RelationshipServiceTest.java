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
    private static final String PREFIX = "RS_";

    private static final String BASIC_USER = PREFIX + "basic_user";

    private static final String POWER_USER = PREFIX + "power_user";

    private static final String SOURCE_SPACE_ADMIN = PREFIX + "source_space_admin";

    private static final String DESTINATION_SPACE_ADMIN = PREFIX + "destination_space_admin";

    private static final String BOTH_SPACE_ADMIN = PREFIX + "both_space_admin";

    private static final String INSTANCE_ADMIN = PREFIX + "instance_admin";

    private SpaceIdentifier sourceSpace = new SpaceIdentifier("CISD", "CISD");

    private SpaceIdentifier destinationSpace = new SpaceIdentifier("CISD", "TESTGROUP");

    private ExperimentUpdatesDTO projectUpdate;

    @BeforeClass
    public void loginSystemUser()
    {
        projectUpdate = getProjectUpdate();
        createSpaceUser(BASIC_USER, RoleCode.USER, RoleCode.USER);
        createSpaceUser(POWER_USER, RoleCode.POWER_USER, RoleCode.POWER_USER);
        createSpaceUser(SOURCE_SPACE_ADMIN, RoleCode.ADMIN, RoleCode.USER);
        createSpaceUser(DESTINATION_SPACE_ADMIN, RoleCode.USER, RoleCode.ADMIN);
        createSpaceUser(BOTH_SPACE_ADMIN, RoleCode.ADMIN, RoleCode.ADMIN);
        createInstanceUser(INSTANCE_ADMIN, RoleCode.ADMIN);
    }

    @Test(expectedExceptions =
        { AuthorizationFailureException.class })
    public void basicUserIsNotAllowedToUpdateExperienceProjectRelationship()
    {
        String session = authenticateAs(BASIC_USER);
        commonServer.updateExperiment(session, projectUpdate);
    }

    @Test(expectedExceptions =
        { AuthorizationFailureException.class })
    public void powerUserIsNotAllowedToUpdateExperienceProjectRelationship()
    {
        String session = authenticateAs(POWER_USER);
        commonServer.updateExperiment(session, projectUpdate);
    }

    @Test(expectedExceptions =
        { AuthorizationFailureException.class })
    public void spaceAdminOfOnlySourceSpaceIsNotAllowedToUpdateExperienceProjectRelationship()
    {
        String session = authenticateAs(SOURCE_SPACE_ADMIN);
        commonServer.updateExperiment(session, projectUpdate);
    }

    @Test(expectedExceptions =
        { AuthorizationFailureException.class })
    public void spaceAdminOfOnlyDestinationSpaceIsNotAllowedToUpdateExperienceProjectRelationship()
    {
        String session = authenticateAs(DESTINATION_SPACE_ADMIN);
        commonServer.updateExperiment(session, projectUpdate);
    }

    @Test
    public void spaceAdminOfBothSpacesIsAllowedToUpdateExperienceProjectRelationship()
    {
        String session = authenticateAs(BOTH_SPACE_ADMIN);
        commonServer.updateExperiment(session, projectUpdate);

        Experiment experiment =
                commonServer.getExperimentInfo(session, new ExperimentIdentifier("CISD",
                        "TESTGROUP", "TESTPROJ", "EXP1"));
        assertThat(experiment.getProject().getCode(), is("TESTPROJ"));
    }

    @Test
    public void instanceAdminIsAllowedToUpdateExperienceProjectRelationship()
    {
        String session = authenticateAs(INSTANCE_ADMIN);
        commonServer.updateExperiment(session, projectUpdate);

        Experiment experiment =
                commonServer.getExperimentInfo(session, new ExperimentIdentifier("CISD",
                        "TESTGROUP", "TESTPROJ", "EXP1"));
        assertThat(experiment.getProject().getCode(), is("TESTPROJ"));
    }

    private void createSpaceUser(String userName, RoleCode sourceSpaceRole,
            RoleCode destinationSpaceRole)
    {
        registerPerson(userName);
        assignSpaceRole(userName, sourceSpaceRole, sourceSpace);
        assignSpaceRole(userName, destinationSpaceRole, destinationSpace);
    }

    private void createInstanceUser(String userName, RoleCode role)
    {
        registerPerson(userName);
        assignInstanceRole(userName, role);
    }

    private ExperimentUpdatesDTO getProjectUpdate()
    {
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        Experiment experiment =
                commonServer.getExperimentInfo(systemSessionToken, new ExperimentIdentifier("CISD",
                        "CISD", "NEMO", "EXP1"));

        updates.setExperimentId(new TechId(experiment));
        updates.setVersion(experiment.getModificationDate());
        updates.setProjectIdentifier(new ProjectIdentifier("CISD", "TESTGROUP", "TESTPROJ"));
        updates.setProperties(experiment.getProperties());
        updates.setAttachments(new ArrayList<NewAttachment>());
        updates.setNewSamples(new ArrayList<NewSamplesWithTypes>());
        return updates;
    }
}
