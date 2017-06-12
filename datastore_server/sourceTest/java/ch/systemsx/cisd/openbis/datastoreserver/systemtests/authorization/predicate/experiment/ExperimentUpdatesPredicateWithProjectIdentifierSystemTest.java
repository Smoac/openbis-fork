/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.experiment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.experiment.ExperimentPredicateTestService;

/**
 * @author pkupczyk
 */
public class ExperimentUpdatesPredicateWithProjectIdentifierSystemTest extends CommonPredicateSystemTest<ExperimentUpdatesDTO>
{

    @Override
    protected ExperimentUpdatesDTO createNonexistentObject()
    {
        // we want to test projectIdentifier only therefore here we set a correct experimentId

        ExperimentUpdatesDTO dto = new ExperimentUpdatesDTO();
        dto.setExperimentId(new TechId(23L)); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST
        dto.setProjectIdentifier(new ProjectIdentifier("IDONTEXIST", "IDONTEXIST"));
        return dto;
    }

    @Override
    protected ExperimentUpdatesDTO createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        // we want to test projectIdentifier only therefore here we set a chosen experimentId

        ExperimentUpdatesDTO dto = new ExperimentUpdatesDTO();
        dto.setExperimentId(new TechId(23L)); // /TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST
        dto.setProjectIdentifier(new ProjectIdentifier(spacePE.getCode(), projectPE.getCode()));
        return dto;
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider session, List<ExperimentUpdatesDTO> objects)
    {
        // we want to test projectIdentifier access only therefore here we add assignment to have access to experimentId

        Set<RoleAssignmentPE> roleAssignments = new HashSet<RoleAssignmentPE>();
        roleAssignments.addAll(session.getSession().tryGetPerson().getRoleAssignments());
        roleAssignments.add(createSpaceRole(RoleCode.ADMIN, getCommonService().tryFindSpace("TEST-SPACE")));
        session.getSession().tryGetPerson().setRoleAssignments(roleAssignments);

        getBean(ExperimentPredicateTestService.class).testExperimentUpdatesPredicate(session, objects.get(0));
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t)
    {
        assertException(t, UserFailureException.class, "No experiment updates specified.");
    }

    @Override
    protected void assertWithNoAllowedRoles(PersonPE person, Throwable t)
    {
        assertAuthorizationFailureExceptionThatNotEnoughPrivileges(t);
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t)
    {
        assertNoException(t);
    }

}
