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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * @author anttil
 */
@Test(groups = "system test")
public class RelationshipServiceTest extends SystemTestCase
{
    @BeforeMethod
    public void createUsers()
    {
        SessionContextDTO systemUser = commonServer.tryToAuthenticateAsSystem();
        commonServer.registerPerson(systemUser.getSessionToken(), "basic");
        commonServer.registerSpaceRole(systemUser.getSessionToken(), RoleCode.USER,
                new SpaceIdentifier("CISD", "CISD"), Grantee.createPerson("basic"));

        commonServer.registerPerson(systemUser.getSessionToken(), "power");
        commonServer.registerSpaceRole(systemUser.getSessionToken(), RoleCode.POWER_USER,
                new SpaceIdentifier("CISD", "CISD"), Grantee.createPerson("power"));

        commonServer.registerPerson(systemUser.getSessionToken(), "admin");
        commonServer.registerSpaceRole(systemUser.getSessionToken(), RoleCode.ADMIN,
                new SpaceIdentifier("CISD", "CISD"), Grantee.createPerson("admin"));

        commonServer.registerPerson(systemUser.getSessionToken(), "instance_admin");
        commonServer.registerInstanceRole(systemUser.getSessionToken(), RoleCode.ADMIN, Grantee
                .createPerson("instance_admin"));

    }

    @Test(expectedExceptions =
        { AuthorizationFailureException.class })
    public void basicUserIsNotAllowedToUpdateExperienceProjectRelationship()
    {
        SessionContextDTO basicUser = commonServer.tryToAuthenticate("basic", "password");
        ExperimentUpdatesDTO updates = getProjectUpdate(basicUser);
        commonServer.updateExperiment(basicUser.getSessionToken(), updates);
    }

    @Test(expectedExceptions =
        { AuthorizationFailureException.class })
    public void powerUserIsNotAllowedToUpdateExperienceProjectRelationship()
    {
        SessionContextDTO powerUser = commonServer.tryToAuthenticate("power", "password");
        ExperimentUpdatesDTO updates = getProjectUpdate(powerUser);
        commonServer.updateExperiment(powerUser.getSessionToken(), updates);
    }

    @Test
    public void spaceAdminIsAllowedToUpdateExperienceProjectRelationship()
    {
        SessionContextDTO adminUser = commonServer.tryToAuthenticate("admin", "password");
        ExperimentUpdatesDTO updates = getProjectUpdate(adminUser);
        commonServer.updateExperiment(adminUser.getSessionToken(), updates);

        Experiment experiment =
                commonServer.getExperimentInfo(adminUser.getSessionToken(),
                        new ExperimentIdentifier(
                                "CISD", "CISD", "DEFAULT", "EXP1"));
        assertThat(experiment.getProject().getCode(), is("DEFAULT"));
    }

    @Test
    public void instanceAdminIsAllowedToUpdateExperienceProjectRelationship()
    {
        SessionContextDTO instanceAdminUser =
                commonServer.tryToAuthenticate("instance_admin", "password");
        ExperimentUpdatesDTO updates = getProjectUpdate(instanceAdminUser);
        commonServer.updateExperiment(instanceAdminUser.getSessionToken(), updates);

        Experiment experiment =
                commonServer.getExperimentInfo(instanceAdminUser.getSessionToken(),
                        new ExperimentIdentifier(
                                "CISD", "CISD", "DEFAULT", "EXP1"));
        assertThat(experiment.getProject().getCode(), is("DEFAULT"));
    }

    private ExperimentUpdatesDTO getProjectUpdate(SessionContextDTO session)
    {
        ExperimentUpdatesDTO updates = new ExperimentUpdatesDTO();
        Experiment experiment =
                commonServer.getExperimentInfo(session.getSessionToken(), new ExperimentIdentifier(
                        "CISD", "CISD", "NEMO", "EXP1"));

        updates.setExperimentId(new TechId(experiment));
        updates.setVersion(experiment.getModificationDate());
        updates.setProjectIdentifier(new ProjectIdentifier("CISD", "CISD", "DEFAULT"));
        updates.setProperties(experiment.getProperties());
        updates.setAttachments(new ArrayList<NewAttachment>());
        updates.setNewSamples(new ArrayList<NewSamplesWithTypes>());
        return updates;
    }
}
