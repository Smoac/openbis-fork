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

import org.springframework.dao.DataIntegrityViolationException;

import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.server.util.KeyExtractorFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
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
public final class RoleAssignmentTable extends AbstractBusinessObject implements
        IRoleAssignmentTable
{
    private List<RoleAssignmentPE> roleAssignments;

    private TableMap<String, PersonPE> personsByUserId;

    private TableMap<String, AuthorizationGroupPE> authorizationGroupsByCode;

    public RoleAssignmentTable(final IDAOFactory daoFactory, final Session session)
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

    private final AuthorizationGroupPE getAuthorizationGroup(final String authGroupId)
    {
        if (authorizationGroupsByCode == null)
        {
            authorizationGroupsByCode =
                    new TableMap<String, AuthorizationGroupPE>(getAuthorizationGroupDAO().list(),
                            KeyExtractorFactory.getAuthorizationGroupByCodeKeyExtractor());
        }
        final AuthorizationGroupPE authGroup = authorizationGroupsByCode.tryGet(authGroupId);
        if (authGroup == null)
        {
            throw UserFailureException.fromTemplate(
                    "No authorization group could be found for code '%s'", authGroupId);
        }
        return authGroup;
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
        final DatabaseInstanceIdentifier databaseInstanceIdentifier =
                newRoleAssignment.getDatabaseInstanceIdentifier();
        if (databaseInstanceIdentifier != null)
        {
            final DatabaseInstancePE databaseInstance =
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
                throw UserFailureException.fromTemplate("Specified space '%s' could not be found",
                        groupIdentifier);
            }
            roleAssignment.setGroup(group);
        }
        roleAssignment.setRegistrator(findRegistrator());
        roleAssignment.setRole(newRoleAssignment.getRole());
        if (Grantee.GranteeType.PERSON.equals(newRoleAssignment.getGrantee().getType()))
        {
            getPerson(newRoleAssignment.getGrantee().getCode()).addRoleAssignment(roleAssignment);
        } else
        {
            getAuthorizationGroup(newRoleAssignment.getGrantee().getCode().toUpperCase())
                    .addRoleAssignment(roleAssignment);
        }
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
            try
            {
                getRoleAssignmentDAO().createRoleAssignment(roleAssignment);
            } catch (final DataIntegrityViolationException ex)
            {
                throwException(ex, "Role assignment");
            }
        }
    }

}
