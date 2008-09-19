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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;

/**
 * Business object operating on collection of {@ling GroupPE} loaded from database.
 * 
 * @author Izabela Adamczyk
 */
final class GroupTable extends AbstractBusinessObject implements IGroupTable
{

    private List<GroupPE> groups;

    GroupTable(final IAuthorizationDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    private final void setHomeGroupFlags()
    {
        final Long homeGroupIdOrNull = session.tryGetHomeGroupId();
        for (final GroupPE group : groups)
        {
            group.setHome(homeGroupIdOrNull != null && group.getId().equals(homeGroupIdOrNull));
        }
    }

    //
    // IGroupTable
    //

    public final List<GroupPE> getGroups()
    {
        assert groups != null : "Groups not loaded";
        return Collections.unmodifiableList(groups);
    }

    public final void load(final DatabaseInstanceIdentifier databaseInstanceIdentifier)
    {
        long databaseInstanceId =
                GroupIdentifierHelper.getDatabaseInstanceId(databaseInstanceIdentifier, this);
        groups = getGroupDAO().listGroups(databaseInstanceId);
        setHomeGroupFlags();
    }

    public final void save() throws UserFailureException
    {
        throw new UnsupportedOperationException();
    }
}
