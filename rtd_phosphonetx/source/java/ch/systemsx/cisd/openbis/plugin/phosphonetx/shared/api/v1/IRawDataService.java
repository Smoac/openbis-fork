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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.MsInjectionDataInfo;

/**
 * Service for querying raw data.
 * 
 * @author Franz-Josef Elmer
 */
public interface IRawDataService extends IRpcService
{
    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "phosphonetx-raw-data";
    
    /**
     * Application part of the URL to access this service remotely.
     */
    public static final String SERVER_URL = "/rmi-" + SERVICE_NAME + "-v1";

    /**
     * Tries to authenticate specified user with specified password. Returns session token if
     * succeeded otherwise <code>null</code> is returned.
     */
    @Transactional
    // this is not a readOnly transaction - it can create new users
    public String tryToAuthenticateAtRawDataServer(String userID, String userPassword);

    /**
     * Logout the session with the specified session token.
     */
    @Transactional(readOnly = true)
    public void logout(String sessionToken);
    
    /**
     * Returns all samples of type MS_INJECTION in space MS_DATA which have a parent sample which
     * the specified user is allow to read.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.INSTANCE_ADMIN_OBSERVER)
    public List<MsInjectionDataInfo> listRawDataSamples(String sessionToken, String userID);

    /**
     * Lists all processing plugins on DSS.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.INSTANCE_ADMIN_OBSERVER)
    public List<DataStoreServerProcessingPluginInfo> listDataStoreServerProcessingPluginInfos(
            String sessionToken);
    
    /**
     * Processes the data sets of specified samples by the DSS processing plug-in of specified key
     * for the specified user. Implementations should check that the specified user is allowed to
     * read specified samples.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.INSTANCE_ADMIN_OBSERVER)
    public void processingRawData(String sessionToken, String userID, String dataSetProcessingKey,
            long[] rawDataSampleIDs, String dataSetType);
}
