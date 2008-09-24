/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business;

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * Manager handling with Persons.
 * 
 * @author Franz-Josef Elmer
 */
public interface IPersonManager
{
    /**
     * Assigns home group to given person.
     */
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void assignHomeGroup(Session session, String userId, GroupIdentifier homeGroup);

    /**
     * Registers given role assignments.
     */
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void registerRoleAssignments(Session session, NewRoleAssignment[] roleAssignments);

    /**
     * Deletes role assignment.
     */
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    public void deleteRoleAssignments(Session session, RoleCode roleSetCode, String group,
            String person);

    /**
     * Registers a new role assignment.
     */
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    public void registerRoleAssignment(Session session, NewRoleAssignment newRoleAssignment);
}