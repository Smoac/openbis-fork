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

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.GroupValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * Manager handling with groups.
 * 
 * @author Basil Neff
 */
public interface IGroupManager
{
    /**
     * Register the given group.
     * 
     * @param groupIdentifier never <code>null</code>.
     */
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void registerGroup(final Session session, final GroupIdentifier groupIdentifier,
            String descriptionOrNull, final String groupLeaderOrNull) throws UserFailureException;

    /**
     * List all available groups in the given database instance. If the database instance code is
     * <code>null</code>, the database instance of the installation is taken.
     * 
     * @param databaseInstanceIdentifier never <code>null</code> but could contain a
     *            <code>null</code> code.
     */
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = GroupValidator.class)
    public List<GroupPE> listGroups(final Session session,
            final DatabaseInstanceIdentifier databaseInstanceIdentifier)
            throws UserFailureException;

    /**
     * List all available {@link DatabaseInstancePE} in this installation.
     */
    @RolesAllowed(RoleSet.OBSERVER)
    public List<DatabaseInstancePE> listDatabaseInstances(final Session session)
            throws UserFailureException;
}
