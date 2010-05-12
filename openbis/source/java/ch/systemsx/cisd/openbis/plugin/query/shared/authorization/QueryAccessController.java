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
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.authorization.AuthorizationAdvisor;
import ch.systemsx.cisd.openbis.generic.server.authorization.DefaultAccessController;
import ch.systemsx.cisd.openbis.generic.shared.authorization.Role;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.Role.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.query.shared.DatabaseDefinition;

/**
 * @author Piotr Buczek
 */
public class QueryAccessController
{

    private static final Logger authorizationLog =
            LogFactory.getLogger(LogCategory.AUTH, AuthorizationAdvisor.class);

    private static Map<String, DatabaseDefinition> definitionsByDbKey;

    public static void initialize(Map<String, DatabaseDefinition> definitions)
    {
        definitionsByDbKey = definitions;
    }

    public static void checkWriteAccess(Session session, String dbKey, String operation)
    {
        DatabaseDefinition database = definitionsByDbKey.get(dbKey);
        PersonPE person = session.tryGetPerson();
        GroupPE dataSpaceOrNull = database.tryGetDataSpace();
        RoleSet minimalRole = database.getCreatorMinimalRole();

        if (isAuthorized(person, dataSpaceOrNull, minimalRole) == false)
        {
            String errorMsg =
                    createErrorMessage(operation, session.getUserName(), dataSpaceOrNull,
                            minimalRole, database.getLabel());
            throw createAuthorizationError(session, operation, errorMsg);
        }
    }

    public static void checkReadAccess(Session session, String dbKey)
    {
        DatabaseDefinition database = definitionsByDbKey.get(dbKey);
        PersonPE person = session.tryGetPerson();
        GroupPE dataSpaceOrNull = database.tryGetDataSpace();
        RoleSet minimalRole = RoleSet.OBSERVER;

        if (isAuthorized(person, dataSpaceOrNull, minimalRole) == false)
        {
            String errorMsg =
                    createErrorMessage("perform", session.getUserName(), dataSpaceOrNull,
                            minimalRole, database.getLabel());
            throw createAuthorizationError(session, "perform", errorMsg);
        }
    }

    private static boolean isAuthorized(PersonPE person, GroupPE dataSpaceOrNull,
            RoleSet minimalRole)
    {
        final Set<Role> requiredRoles = minimalRole.getRoles();
        if (person != null)
        {
            List<RoleWithIdentifier> userRoles = DefaultAccessController.getUserRoles(person);
            userRoles.retainAll(requiredRoles);
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
            final GroupPE requiredSpace)
    {

        for (final RoleWithIdentifier role : userRoles)
        {
            final RoleLevel roleGroup = role.getRoleLevel();
            if (roleGroup.equals(RoleLevel.SPACE) && role.getAssignedGroup().equals(requiredSpace))
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

    private static String createErrorMessage(String operation, String userName,
            GroupPE dataSpaceOrNull, RoleSet minimalRole, String database)
    {
        String minimalRoleDescription = minimalRole.name();
        if (dataSpaceOrNull != null)
        {
            minimalRoleDescription += " in space " + dataSpaceOrNull.getCode();
        }

        return String.format("User '%s' does not have enough privileges to %s "
                + "a query in database '%s'. One needs to be at least %s.", userName, operation,
                database, minimalRoleDescription);
    }

    private static AuthorizationFailureException createAuthorizationError(Session session,
            String operation, String errorMessage)
    {
        final String groupCode = session.tryGetHomeGroupCode();
        authorizationLog
                .info(String.format("[USER:'%s' SPACE:%s]: Authorization failure while "
                        + "trying to %s a query: %s", session.getUserName(),
                        groupCode == null ? "<UNDEFINED>" : "'" + groupCode + "'", operation,
                        errorMessage));
        return new AuthorizationFailureException(errorMessage);
    }
}
