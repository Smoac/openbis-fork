/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.Role.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.GroupCodeHelper;

/**
 * An <code>IPredicate</code> implementation based on {@link GroupIdentifier}.
 * 
 * @author Christian Ribeaud
 */
public final class GroupIdentifierPredicate extends
        AbstractDatabaseInstancePredicate<GroupIdentifier>
{
    private List<GroupPE> groups;

    public GroupIdentifierPredicate()
    {
    }

    private final static boolean isMatching(final List<RoleWithIdentifier> allowedRoles,
            final String databaseInstanceUUID, final String groupCode)
    {
        for (final RoleWithIdentifier role : allowedRoles)
        {
            final RoleLevel roleGroup = role.getRoleGroup();
            if (roleGroup.equals(RoleLevel.GROUP)
                    && equalIdentifier(role.getAssignedGroup(), databaseInstanceUUID, groupCode))
            {
                return true;
            } else if (roleGroup.equals(RoleLevel.INSTANCE)
                    && role.getAssignedDatabaseInstance().getUuid().equals(databaseInstanceUUID))
            {
                // permissions on the database instance level allow to access all groups in this
                // instance
                return true;
            }
        }
        return false;
    }

    private final void ensureGroupExists(final GroupIdentifier groupIdentifier,
            final String databaseInstanceUUID, final String groupCode)
    {
        if (tryFindGroup(databaseInstanceUUID, groupCode) == null)
        {
            throw UserFailureException.fromTemplate("No group could be found for identifier '%s'.",
                    groupIdentifier);
        }
    }

    private GroupPE tryFindGroup(final String databaseInstanceUUID, final String groupCode)
    {
        for (final GroupPE group : groups)
        {
            if (equalIdentifier(group, databaseInstanceUUID, groupCode))
            {
                return group;
            }
        }
        return null;
    }

    private static boolean equalIdentifier(final GroupPE group, final String databaseInstanceUUID,
            final String groupCode)
    {
        return group.getCode().equals(groupCode)
                && group.getDatabaseInstance().getUuid().equals(databaseInstanceUUID);
    }

    //
    // AbstractDatabaseInstancePredicate
    //

    @Override
    public final void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        groups = provider.listGroups();
    }

    @Override
    final String getCandidateDescription()
    {
        return "group identifier";
    }

    @Override
    final Status doEvaluation(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final GroupIdentifier groupIdentifier)
    {
        assert inited : "Predicate has not been initialized";
        final String groupCode = GroupCodeHelper.getGroupCode(person, groupIdentifier);
        final DatabaseInstancePE databaseInstance = getDatabaseInstance(groupIdentifier);
        final String databaseInstanceUUID = databaseInstance.getUuid();
        final GroupIdentifier fullGroupIdentifier =
                new GroupIdentifier(databaseInstance.getCode(), groupCode);
        ensureGroupExists(fullGroupIdentifier, databaseInstanceUUID, groupCode);
        final boolean matching = isMatching(allowedRoles, databaseInstanceUUID, groupCode);
        if (matching)
        {
            return Status.OK;
        }
        return Status.createError(String.format(
                "User '%s' does not have enough privileges to access data in the group '%s'.",
                person.getUserId(), fullGroupIdentifier));
    }
}
