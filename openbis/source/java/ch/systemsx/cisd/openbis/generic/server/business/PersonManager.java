/*
 * Copyright 2007 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.common.utilities.ParameterChecker.checkIfNotBlank;
import static ch.systemsx.cisd.common.utilities.ParameterChecker.checkIfNotNull;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IPersonBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IRoleAssignmentTable;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * The <code>AbstractManager</code> extension which cares about the persons.
 * 
 * @author Franz-Josef Elmer
 */
final class PersonManager extends AbstractManager implements IPersonManager
{

    PersonManager(final IAuthorizationDAOFactory daoFactory,
            final IGenericBusinessObjectFactory boFactory)
    {
        super(daoFactory, boFactory);
    }

    //
    // IPersonManager
    //

    @Transactional
    public final void assignHomeGroup(final Session session, final String userId,
            final GroupIdentifier groupIdentifier)
    {
        assert session != null : "Unspecified session";
        checkIfNotBlank(userId, "user");
        checkIfNotNull(groupIdentifier, "group identifier");

        final IPersonBO personBO = boFactory.createPersonBO(session);
        personBO.load(userId);
        final IGroupBO groupBO = boFactory.createGroupBO(session);
        groupBO.load(groupIdentifier);
        personBO.setHomeGroup(groupBO.getGroup());
    }

    @Transactional
    public final void registerRoleAssignments(final Session session,
            final NewRoleAssignment[] roleAssignments)
    {
        assert session != null : "Unspecified session";
        checkIfNotNull(roleAssignments, "role assignments");

        final IRoleAssignmentTable table = boFactory.createRoleAssignmentTable(session);
        for (final NewRoleAssignment roleAssignment : roleAssignments)
        {
            table.add(roleAssignment);
        }
        table.save();
    }

    @Transactional
    public final void registerRoleAssignment(final Session session,
            final NewRoleAssignment roleAssignment)
    {
        assert session != null : "Unspecified session";
        checkIfNotNull(roleAssignment, "role assignment");

        final IRoleAssignmentTable table = boFactory.createRoleAssignmentTable(session);
        table.add(roleAssignment);
        table.save();
    }

    @Transactional
    public void registerPerson(Session session, String code)
    {
        assert session != null : "Unspecified session";
        IPersonBO personBO = boFactory.createPersonBO(session);
        personBO.registerPerson(code);
    }

    @Transactional
    public void deleteRoleAssignments(Session session, RoleCode role, String group, String person)
    {
        RoleAssignmentPE roleAssignment =
                daoFactory.getRoleAssignmentDAO().tryFindRoleAssignment(role, group, person);
        if (roleAssignment == null)
        {
            throw new UserFailureException("Given role does not exist.");
        }
        daoFactory.getRoleAssignmentDAO().deleteRoleAssignment(roleAssignment);
    }

}
