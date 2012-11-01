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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.openbis.generic.server.authorization.IAuthorizationDataProvider;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.util.SpaceCodeHelper;

/**
 * Abstract super class of predicates based on data spaces.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractSpacePredicate<T> extends AbstractDatabaseInstancePredicate<T>
{

    protected List<SpacePE> spaces;

    protected boolean okForNonExistentSpaces;

    protected AbstractSpacePredicate()
    {
        this(false);
    }

    protected AbstractSpacePredicate(boolean okForNonExistentSpaces)
    {
        this.okForNonExistentSpaces = okForNonExistentSpaces;
    }

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
            if (okForNonExistentSpaces)
            {
                return Status.OK;
            } else
            {
                return createError(person);
            }
        }

        final boolean matching = isMatching(allowedRoles, databaseInstanceUUID, spaceCodeOrNull);
        if (matching)
        {
            return Status.OK;
        }
        return createError(person);
    }

    protected Status evaluate(final PersonPE person, final List<RoleWithIdentifier> allowedRoles,
            final long spaceTechId)
    {
        final SpacePE space = tryFindSpace(spaceTechId);
        if (space == null)
        {
            if (okForNonExistentSpaces)
            {
                return Status.OK;
            } else
            {
                return createError(person);
            }
        }

        final boolean matching =
                isMatching(allowedRoles, space.getDatabaseInstance().getId(), spaceTechId);
        if (matching)
        {
            return Status.OK;
        }
        return createError(person);
    }

    private Status createError(final PersonPE person)
    {
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

    private SpacePE tryFindSpace(final long spaceTechId)
    {
        for (final SpacePE space : spaces)
        {
            if (equalIdentifier(space, spaceTechId))
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

    private boolean isMatching(final List<RoleWithIdentifier> allowedRoles,
            final long databaseTechId, final long spaceTechId)
    {
        for (final RoleWithIdentifier role : allowedRoles)
        {
            final RoleLevel roleLevel = role.getRoleLevel();
            if (roleLevel.equals(RoleLevel.SPACE)
                    && equalIdentifier(role.getAssignedSpace(), spaceTechId))
            {
                return true;
            } else if (roleLevel.equals(RoleLevel.INSTANCE)
                    && role.getAssignedDatabaseInstance().getId() == databaseTechId)
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

    private boolean equalIdentifier(final SpacePE space, final long spaceTechId)
    {
        return (space.getId() == spaceTechId);
    }

    protected Status evaluateSpace(final PersonPE person,
            final List<RoleWithIdentifier> allowedRoles, final SpacePE spaceOrNull)
    {
        if (spaceOrNull == null)
        {
            return Status.createError(String.format("User '%s' does not have enough privileges.",
                    person.getUserId()));
        }

        final String spaceCode = SpaceCodeHelper.getSpaceCode(person, spaceOrNull);
        final DatabaseInstancePE databaseInstance = spaceOrNull.getDatabaseInstance();
        return evaluate(person, allowedRoles, databaseInstance, spaceCode);
    }

}
