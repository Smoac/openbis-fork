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

package ch.systemsx.cisd.openbis.generic.server.business.bo.util;

import ch.systemsx.cisd.common.exceptions.InternalErr;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.exception.UndefinedGroupException;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * Finds a group or database instance for a given owner identifier.
 * 
 * @author Tomasz Pylak
 */
public class SampleOwnerFinder
{
    private final IAuthorizationDAOFactory daoFactory;

    private final PersonPE personPE;

    public SampleOwnerFinder(final IAuthorizationDAOFactory daoFactory, final PersonPE personPE)
    {
        assert daoFactory != null : "Unspecified DAOFactory";
        assert personPE != null : "Unspecified person";
        this.daoFactory = daoFactory;
        this.personPE = personPE;
    }

    public SampleOwner figureSampleOwner(final SampleOwnerIdentifier owner)
    {
        final SampleOwner ownerId = tryFigureSampleOwner(owner);
        if (ownerId == null)
        {
            throw UserFailureException.fromTemplate("Incorrect space or database name in '%s'",
                    owner);
        }
        return ownerId;
    }

    // determines the owner of the sample if it belongs to the home group or home database
    public SampleOwner tryFigureSampleOwner(final SampleOwnerIdentifier owner)
    {
        if (owner.isDatabaseInstanceLevel())
        {
            final DatabaseInstanceIdentifier databaseInstanceIdentifier =
                    owner.getDatabaseInstanceLevel();
            return tryFigureSampleDatabaseOwner(databaseInstanceIdentifier);
        } else if (owner.isGroupLevel())
        {
            return tryFigureSampleGroupOwner(owner);
        } else
            throw InternalErr.error();
    }

    private SampleOwner tryFigureSampleGroupOwner(final SampleOwnerIdentifier owner)
    {
        if (owner.isInsideHomeGroup())
        {
            return createHomeGroupOwner(owner);
        } else
        {
            final GroupIdentifier groupIdentifier = owner.getGroupLevel();
            return tryFindAbsoluteGroupOwner(groupIdentifier);
        }
    }

    private SampleOwner tryFigureSampleDatabaseOwner(
            final DatabaseInstanceIdentifier databaseInstanceIdentifier)
    {
        final DatabaseInstancePE databaseInstance =
                GroupIdentifierHelper
                        .tryGetDatabaseInstance(databaseInstanceIdentifier, daoFactory);
        if (databaseInstance == null)
        {
            return null;
        }
        return SampleOwner.createDatabaseInstance(databaseInstance);
    }

    private SampleOwner tryFindAbsoluteGroupOwner(final GroupIdentifier groupIdentifier)
    {
        final GroupPE group =
                GroupIdentifierHelper.tryGetGroup(groupIdentifier, personPE, daoFactory);
        if (group == null)
        {
            return null;
        }
        return SampleOwner.createGroup(group);
    }

    private SampleOwner createHomeGroupOwner(final SampleOwnerIdentifier identifier)
    {
        final GroupPE homeGroup = personPE.getHomeGroup();
        if (homeGroup == null)
        {
            throw new UndefinedGroupException();
        }
        return SampleOwner.createGroup(homeGroup);
    }
}
