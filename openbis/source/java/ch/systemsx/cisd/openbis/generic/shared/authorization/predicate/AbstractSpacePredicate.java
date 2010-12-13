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
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * Abstract super class of predicates based on data spaces.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractSpacePredicate<T> extends AbstractDatabaseInstancePredicate<T>
{

    protected List<SpacePE> spaces;

    @Override
    public void init(IAuthorizationDataProvider provider)
    {
        super.init(provider);
        spaces = provider.listSpaces();
    }

    protected Status evaluate(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final DatabaseInstancePE databaseInstance, final String spaceCodeOrNull)
    {
        final String databaseInstanceUUID = databaseInstance.getUuid();
        return evaluate(person, allowedRoles, databaseInstanceUUID, databaseInstance.getCode(),
                spaceCodeOrNull);
    }

    protected Status evaluate(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final String databaseInstanceUUID, final String databaseInstanceCode,
            final String spaceCodeOrNull)
    {
        if (tryFindSpace(databaseInstanceUUID, spaceCodeOrNull) == null)
        {
            return Status.createError(String.format("User '%s' does not have enough privileges.",
                    person.getUserId()));
        }
        final boolean matching = isMatching(allowedRoles, databaseInstanceUUID, spaceCodeOrNull);
        if (matching)
        {
            return Status.OK;
        }
        return Status.createError(String.format("User '%s' does not have enough privileges.",
                person.getUserId()));
    }

    private SpacePE tryFindSpace(final String databaseInstanceUUID, final String spaceCode)
    {
        for (final SpacePE space : spaces)
        {
            if (equalIdentifier(space, databaseInstanceUUID, spaceCode))
            {
                return space;
            }
        }
        return null;
    }

    private boolean isMatching(final List<RoleWithIdentifier> allowedRoles,
            final String databaseInstanceUUID, final String spaceCodeOrNull)
    {
        for (final RoleWithIdentifier role : allowedRoles)
        {
            final RoleLevel roleLevel = role.getRoleLevel();
            if (roleLevel.equals(RoleLevel.SPACE)
                    && equalIdentifier(role.getAssignedSpace(), databaseInstanceUUID,
                            spaceCodeOrNull))
            {
                return true;
            } else if (roleLevel.equals(RoleLevel.INSTANCE)
                    && role.getAssignedDatabaseInstance().getUuid().equals(databaseInstanceUUID))
            {
                // permissions on the database instance level allow to access all spaces in this
                // instance
                return true;
            }
        }
        return false;
    }

    private boolean equalIdentifier(final SpacePE space, final String databaseInstanceUUID,
            final String spaceCodeOrNull)
    {
        return (spaceCodeOrNull == null || space.getCode().equals(spaceCodeOrNull))
                && space.getDatabaseInstance().getUuid().equals(databaseInstanceUUID);
    }

}
