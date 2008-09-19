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

package ch.systemsx.cisd.openbis.generic.server.business;

import static ch.systemsx.cisd.common.utilities.ParameterChecker.checkIfNotNull;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.server.business.bo.IGenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupTable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * @author Tomasz Pylak
 */
final class GroupManager extends AbstractManager implements IGroupManager
{

    GroupManager(final IAuthorizationDAOFactory daoFactory,
            final IGenericBusinessObjectFactory boFactory)
    {
        super(daoFactory, boFactory);
    }

    //
    // IGroupManager
    //

    @Transactional
    public final void registerGroup(final Session session, final GroupIdentifier groupIdentifier,
            final String descriptionOrNull, final String groupLeaderOrNull)
    {
        assert session != null : "Unspecified session";
        checkIfNotNull(groupIdentifier, "group code");

        final IGroupBO groupBO = boFactory.createGroupBO(session);
        groupBO.define(groupIdentifier, descriptionOrNull, groupLeaderOrNull);
        groupBO.save();
    }

    @Transactional
    public final List<GroupPE> listGroups(final Session session,
            final DatabaseInstanceIdentifier databaseInstanceIdentifier)
    {
        assert session != null : "Unspecified session";
        checkIfNotNull(databaseInstanceIdentifier, "database instance identifier");

        final IGroupTable groupTable = boFactory.createGroupTable(session);
        groupTable.load(databaseInstanceIdentifier);
        return groupTable.getGroups();
    }

    @Transactional
    public final List<DatabaseInstancePE> listDatabaseInstances(final Session session)
    {
        assert session != null : "Unspecified session";

        return daoFactory.getDatabaseInstancesDAO().listDatabaseInstances();
    }
}
