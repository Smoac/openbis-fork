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

package ch.systemsx.cisd.openbis.generic.shared.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * Abstract super class of predicates based on groups.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractGroupPredicate<T> extends AbstractDatabaseInstancePredicate<T>
{

    protected List<GroupPE> groups;

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        groups = provider.listGroups();
    }

    protected Status evaluate(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final DatabaseInstancePE databaseInstance, final String groupCodeOrNull)
    {
        final String databaseInstanceUUID = databaseInstance.getUuid();
        return evaluate(person, allowedRoles, databaseInstanceUUID, databaseInstance.getCode(),
                groupCodeOrNull);
    }

    protected Status evaluate(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final String databaseInstanceUUID, final String databaseInstanceCode,
            final String groupCodeOrNull)
    {
        if (tryFindGroup(databaseInstanceUUID, groupCodeOrNull) == null)
        {
            return Status.createError(String.format(
                    "User '%s' does not have enough privileges to access data in the space '%s'.",
                    person.getUserId(), new GroupIdentifier(databaseInstanceCode, groupCodeOrNull)));
        }
        final boolean matching = isMatching(allowedRoles, databaseInstanceUUID, groupCodeOrNull);
        if (matching)
        {
            return Status.OK;
        }
        return Status.createError(String.format(
                "User '%s' does not have enough privileges to access data in the space '%s'.",
                person.getUserId(), new GroupIdentifier(databaseInstanceCode, groupCodeOrNull)));
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

    private boolean isMatching(final List<RoleWithIdentifier> allowedRoles,
            final String databaseInstanceUUID, final String groupCodeOrNull)
    {
        for (final RoleWithIdentifier role : allowedRoles)
        {
            final RoleLevel roleGroup = role.getRoleLevel();
            if (roleGroup.equals(RoleLevel.SPACE)
                    && equalIdentifier(role.getAssignedGroup(), databaseInstanceUUID,
                            groupCodeOrNull))
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

    private boolean equalIdentifier(final GroupPE group, final String databaseInstanceUUID,
            final String groupCodeOrNull)
    {
        return (groupCodeOrNull == null || group.getCode().equals(groupCodeOrNull))
                && group.getDatabaseInstance().getUuid().equals(databaseInstanceUUID);
    }

}
