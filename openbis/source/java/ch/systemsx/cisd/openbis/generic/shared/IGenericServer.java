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

import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.ReturnValueFilter;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.GroupValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;

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
    public void registerPerson(String sessionToken, String code);

}
