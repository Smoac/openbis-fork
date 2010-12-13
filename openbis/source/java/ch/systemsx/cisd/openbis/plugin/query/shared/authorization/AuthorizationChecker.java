/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.shared.authorization;

import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.server.authorization.DefaultAccessController;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Default implementation of {@link IAuthorizationChecker}.
 * 
 * @author Piotr Buczek
 * @author Izabela Adamczyk
 */
public class AuthorizationChecker implements IAuthorizationChecker
{

    public boolean isAuthorized(PersonPE person, SpacePE dataSpaceOrNull, RoleWithHierarchy minimalRole)
    {
        final Set<RoleWithHierarchy> requiredRoles = minimalRole.getRoles();
        if (person != null)
        {
            List<RoleWithIdentifier> userRoles = DefaultAccessController.getUserRoles(person);
            DefaultAccessController.retainMatchingRoleWithIdentifiers(userRoles, requiredRoles);
            if (userRoles.size() > 0)
            {
                if (dataSpaceOrNull == null)
                {
                    return true;
                } else
                {
                    return isSpaceMatching(userRoles, dataSpaceOrNull);
                }
            } else
            {
                return false;
            }
        }
        return false;
    }

    private static boolean isSpaceMatching(List<RoleWithIdentifier> userRoles,
            final SpacePE requiredSpace)
    {

        for (final RoleWithIdentifier role : userRoles)
        {
            final RoleLevel roleGroup = role.getRoleLevel();
            if (roleGroup.equals(RoleLevel.SPACE) && role.getAssignedSpace().equals(requiredSpace))
            {
                return true;
            } else if (roleGroup.equals(RoleLevel.INSTANCE)
                    && role.getAssignedDatabaseInstance().equals(
                            requiredSpace.getDatabaseInstance()))
            {
                // permissions on the database instance level allow to access all groups in this
                // instance
                return true;
            }
        }
        return false;
    }
}