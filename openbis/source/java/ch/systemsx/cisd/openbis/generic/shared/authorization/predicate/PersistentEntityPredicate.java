/*
 * Copyright 2012 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleLevel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * A predicate for persistent entities.
 * 
 * @author anttil
 */
public abstract class PersistentEntityPredicate<T> implements IPredicate<T>
{

    @Override
    public Status evaluate(PersonPE person, List<RoleWithIdentifier> allowedRoles, T valueOrNull)
            throws UserFailureException
    {
        if (valueOrNull == null)
        {
            return Status.createError("null value cannot be authorized");
        }

        for (RoleWithIdentifier allowed : allowedRoles)
        {
            if (allowed.getRoleLevel().equals(RoleLevel.INSTANCE))
            {
                if (allowed.getAssignedDatabaseInstance().equals(getInstance(valueOrNull)))
                {
                    return Status.OK;
                }

                SpacePE space = getSpace(valueOrNull);

                if (space != null)
                {
                    if (space.getDatabaseInstance().equals(allowed.getAssignedDatabaseInstance()))
                    {
                        return Status.OK;
                    }
                }

            } else
            {
                if (allowed.getAssignedSpace().equals(getSpace(valueOrNull)))
                {
                    return Status.OK;
                }
            }
        }

        return Status.createError();
    }

    public abstract SpacePE getSpace(T value);

    public abstract DatabaseInstancePE getInstance(T value);

    @Override
    public void init(IAuthorizationDataProvider provider)
    {

    }

}
