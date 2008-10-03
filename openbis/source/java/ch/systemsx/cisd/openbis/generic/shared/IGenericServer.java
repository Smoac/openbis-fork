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

package ch.systemsx.cisd.openbis.generic.shared;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.AuthorizationGuard;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.predicate.GroupIdentifierPredicate;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.GroupValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleOwnerIdentifier;

/**
 * Definition of the client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGenericServer
{
    /**
     * Returns the version of this interface.
     */
    public int getVersion();

    /**
     * Tries to authenticate the specified user with given password.
     * 
     * @return <code>null</code> if authentication failed.
     */
    public Session tryToAuthenticate(String user, String password);

    /**
     * Logout the session with the specified session token.
     */
    public void logout(String sessionToken);

    /**
     * Returns all groups which belong to the specified database instance.
     */
    @RolesAllowed(RoleSet.OBSERVER)
    @ReturnValueFilter(validatorClass = GroupValidator.class)
    public List<GroupPE> listGroups(String sessionToken, DatabaseInstanceIdentifier identifier);

    /**
     * Registers a new group with specified code and optional description and group leader ID.
     */
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void registerGroup(String sessionToken, String groupCode, String descriptionOrNull,
            String groupLeaderOrNull);

    /**
     * Returns all persons from current instance.
     */
    @RolesAllowed(RoleSet.OBSERVER)
    public List<PersonPE> listPersons(String sessionToken);

    /**
     * Registers a new person.
     */
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void registerPerson(String sessionToken, String userID);

    /**
     * Returns a list of all roles.
     */
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    public List<RoleAssignmentPE> listRoles(String sessionToken);

    /**
     * Registers a new group role.
     */
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    public void registerGroupRole(String sessionToken, RoleCode roleCode,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class)
            GroupIdentifier identifier, String person);

    /**
     * Registers a new instance role.
     */
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void registerInstanceRole(String sessionToken, RoleCode roleCode, String person);

    /**
     * Deletes role described by given role code, group identifier and user id.
     */
    @RolesAllowed(RoleSet.GROUP_ADMIN)
    public void deleteGroupRole(String sessionToken, RoleCode roleCode,
            @AuthorizationGuard(guardClass = GroupIdentifierPredicate.class)
            GroupIdentifier groupIdentifier, String person);

    /**
     * Deletes role described by given role code and user id.
     */
    @RolesAllowed(RoleSet.INSTANCE_ADMIN)
    public void deleteInstanceRole(String sessionToken, RoleCode roleCode, String person);

    /**
     * Lists sample types which are appropriate for listing.
     */
    @RolesAllowed(RoleSet.OBSERVER)
    public List<SampleTypePE> listSampleTypes(String sessionToken);

    /**
     * Lists samples using given configuration. Only sample properties which are configured to be
     * displayed are included in the result.
     */
    @RolesAllowed(RoleSet.OBSERVER)
    public List<SamplePE> listSamples(String sessionToken,
            List<SampleOwnerIdentifier> ownerIdentifiers, SampleTypePE sampleType);

}
