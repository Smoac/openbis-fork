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

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.server.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewRoleAssignment;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * The only productive implementation of {@link IRoleAssignmentTable}. We are using an interface
 * here to keep the system testable.
 * 
 * @author Christian Ribeaud
 */
final class RoleAssignmentTable extends AbstractBusinessObject implements IRoleAssignmentTable
{
    private List<RoleAssignmentPE> roleAssignments;

    private TableMap<String, PersonPE> personsByUserId;

    RoleAssignmentTable(final IAuthorizationDAOFactory daoFactory, final Session session)
    {
        super(daoFactory, session);
    }

    private final PersonPE getPerson(final String userId)
    {
        if (personsByUserId == null)
        {
            personsByUserId =
                    new TableMap<String, PersonPE>(getPersonDAO().listPersons(),
                            KeyExtractorFactory.getPersonByUserIdKeyExtractor());
        }
        final PersonPE person = personsByUserId.tryGet(userId);
        if (person == null)
        {
            throw UserFailureException.fromTemplate("No person could be found for user id '%s'",
                    userId);
        }
        return person;
    }

    //
    // IRoleAssignmentTable
    //

    public final void add(final NewRoleAssignment newRoleAssignment)
    {
        if (roleAssignments == null)
        {
            roleAssignments = new ArrayList<RoleAssignmentPE>();
        }
        final RoleAssignmentPE roleAssignment = new RoleAssignmentPE();
        roleAssignment.setPerson(getPerson(newRoleAssignment.getUserId()));
        final DatabaseInstanceIdentifier databaseInstanceIdentifier =
                newRoleAssignment.getDatabaseInstanceIdentifier();
        if (databaseInstanceIdentifier != null)
        {
            DatabaseInstancePE databaseInstance =
                    GroupIdentifierHelper.getDatabaseInstance(databaseInstanceIdentifier, this);
            roleAssignment.setDatabaseInstance(databaseInstance);
        } else
        {
            final GroupIdentifier groupIdentifier = newRoleAssignment.getGroupIdentifier();
            final GroupPE group =
                    GroupIdentifierHelper
                            .tryGetGroup(groupIdentifier, session.tryGetPerson(), this);
            if (group == null)
            {
                throw UserFailureException.fromTemplate("Specified group '%s' could not be found",
                        groupIdentifier);
            }
            roleAssignment.setGroup(group);
        }
        roleAssignment.setRegistrator(findRegistrator());
        roleAssignment.setRole(newRoleAssignment.getRole());
        roleAssignments.add(roleAssignment);
    }

    //
    // AbstractBusinessObject
    //

    public final void save() throws UserFailureException
    {
        assert roleAssignments != null : "Role assignments unspecified";
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            getRoleAssignmentDAO().createRoleAssignment(roleAssignment);
        }
    }

}
