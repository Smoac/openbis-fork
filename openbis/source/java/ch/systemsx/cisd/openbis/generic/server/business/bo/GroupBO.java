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

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.IdentifierHelper;

/**
 * The only productive implementation of {@link IGroupBO}. We are using an interface here to keep
 * the system testable.
 * 
 * @author Christian Ribeaud
 */
public final class GroupBO extends AbstractBusinessObject implements IGroupBO
{

    private GroupPE group;

    public GroupBO(final IDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    //
    // IGroupBO
    //

    public final void save() throws UserFailureException
    {
        assert group != null : "Group not defined";
        try
        {
            getGroupDAO().createGroup(group);
        } catch (final DataAccessException e)
        {
            throwException(e, "Group '" + IdentifierHelper.createGroupIdentifier(group) + "'");
        }
    }

    public final void define(String groupCode, final String descriptionOrNull,
            final String groupLeaderOrNull) throws UserFailureException
    {
        assert groupCode != null : "Unspecified group code.";
        group = new GroupPE();
        final GroupIdentifier groupIdentifier =
                new GroupIdentifier(DatabaseInstanceIdentifier.HOME, groupCode);
        final DatabaseInstancePE databaseInstance =
                GroupIdentifierHelper.getDatabaseInstance(groupIdentifier, this);
        group.setDatabaseInstance(databaseInstance);
        group.setCode(groupIdentifier.getGroupCode());
        group.setDescription(descriptionOrNull);
        group.setRegistrator(findRegistrator());
        if (groupLeaderOrNull != null)
        {
            final PersonPE groupLeader = getPersonDAO().tryFindPersonByUserId(groupLeaderOrNull);
            if (groupLeader == null)
            {
                final String msg =
                        "A group leader with the given user-name '" + groupLeaderOrNull
                                + "' does not exist!";
                throw new UserFailureException(msg);
            }
            group.setGroupLeader(groupLeader);
        }
    }

    public GroupPE getGroup() throws UserFailureException
    {
        return group;
    }

    public void load(final GroupIdentifier groupIdentifier) throws UserFailureException
    {
        group = GroupIdentifierHelper.tryGetGroup(groupIdentifier, session.tryGetPerson(), this);
        if (group == null)
        {
            throw new UserFailureException(String.format("Group '%s' does not exist.",
                    groupIdentifier));
        }
    }
}
