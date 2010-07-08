/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;

/**
 * Service for retrieving general informations.
 * 
 * @author Franz-Josef Elmer
 */
public interface IGeneralInformationService extends IRpcService
{
    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "general-information";

    /**
     * Application part of the URL to access this service remotely.
     */
    public static final String SERVICE_URL = "/rmi-" + SERVICE_NAME + "-v1";

    /**
     * Tries to authenticate specified user with specified password. Returns session token if
     * succeeded otherwise <code>null</code> is returned. The returned session token can be used for
     * all methods and interfaces of the same openBIS server.
     */
    @Transactional
    // this is not a readOnly transaction - it can create new users
    public String tryToAuthenticateForAllServices(String userID, String userPassword);

    /**
     * Logout the session with the specified session token.
     */
    @Transactional(readOnly = true)
    public void logout(String sessionToken);

    /**
     * Returns all named role sets. The name is the key of the returned map.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.INSTANCE_OBSERVER)
    public Map<String, Set<Role>> listNamedRoleSets(String sessionToken);

    /**
     * Returns all spaces of specified database instance enriched with their projects and role
     * assignments.
     * 
     * @param databaseInstanceCodeOrNull Code of an imported database instance or <code>null</code>
     *            for the home database instance is meant.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<SpaceWithProjectsAndRoleAssignments> listSpacesWithProjectsAndRoleAssignments(
            String sessionToken, String databaseInstanceCodeOrNull);

}
