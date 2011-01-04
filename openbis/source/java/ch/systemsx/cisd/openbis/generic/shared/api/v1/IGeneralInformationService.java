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
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

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
     * Returns true if session with the specified token is still active, false otherwise. Available
     * since minor version 4.
     * 
     * @since 1.4
     */
    @Transactional(readOnly = true)
    public boolean isSessionActive(String sessionToken);

    /**
     * Returns all named role sets. The name is the key of the returned map.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public Map<String, Set<Role>> listNamedRoleSets(String sessionToken);

    /**
     * Returns all spaces of specified database instance enriched with their projects and role
     * assignments.
     * 
     * @param databaseInstanceCodeOrNull Code of an imported database instance or <code>null</code>
     *            for the home database instance is meant.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<SpaceWithProjectsAndRoleAssignments> listSpacesWithProjectsAndRoleAssignments(
            String sessionToken, String databaseInstanceCodeOrNull);

    /**
     * Return all samples that match the search criteria. Available since minor version 1.
     * 
     * @param searchCriteria The sample metadata values to be matched against.
     * @since 1.1
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<Sample> searchForSamples(String sessionToken, SearchCriteria searchCriteria);

    /**
     * Return all data sets attached to the given samples. Available since minor version 1.
     * 
     * @param samples The samples for which we return attached data sets.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<DataSet> listDataSets(String sessionToken, List<Sample> samples);

    /**
     * Return all experiments of the given type that belong to the supplied projects. Available
     * since minor version 2.
     * 
     * @param projects The projects for which we return attached experiments.
     * @param experimentType The experiment type of the experiments we want to list.
     * @since 1.2
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<Experiment> listExperiments(String sessionToken, List<Project> projects,
            String experimentType);

    /**
     * Return the data sets attached to the specified sample, optionally including child samples.
     * Available since minor version 3.
     * 
     * @param sample The sample for which we return attached data sets.
     * @param areOnlyDirectlyConnectedIncluded If true, only data sets that are directly connected
     *            to the sample are included, otherwise data sets of child samples are included as
     *            well.
     * @since 1.3
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<DataSet> listDataSetsForSample(String sessionToken, Sample sample,
            boolean areOnlyDirectlyConnectedIncluded);

    /**
     * Returns the URL for the default data store server for this openBIS AS. Available since minor
     * version 4.
     * 
     * @since 1.4
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleWithHierarchy.SPACE_OBSERVER)
    public String getDefaultPutDataStoreBaseURL(String sessionToken);

    /**
     * Returns the download URL for the data store of specified data set or null if such data set
     * does not exist. Available since minor version 4.
     * 
     * @since 1.4
     */
    @Transactional(readOnly = true)
    @RolesAllowed(value =
        { RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_ETL_SERVER })
    public String tryGetDataStoreBaseURL(String sessionToken, String dataSetCode);

}
