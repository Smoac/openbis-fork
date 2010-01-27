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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.shared;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RoleSet;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * Service for querying raw data.
 *
 * @author Franz-Josef Elmer
 */
public interface IRawDataService extends IServer
{
    /**
     * Returns all samples of type MS_INJECTION in group MS_DATA which have a parent sample which
     * the specified user is allow to read.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.INSTANCE_ADMIN_OBSERVER)
    public List<Sample> listRawDataSamples(String sessionToken, String userID);

    /**
     * Lists all processing DSS services.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.INSTANCE_ADMIN_OBSERVER)
    public List<DatastoreServiceDescription> listDataStoreServices(String sessionToken);
    
    /**
     * Processes the data sets of specified samples by the DSS processing plug-in of specified key
     * for the specified user. Implementations should check that the specified user is allowed
     * to read specified samples.
     */
    @Transactional(readOnly = true)
    @RolesAllowed(RoleSet.INSTANCE_ADMIN_OBSERVER)
    public void processingRawData(String sessionToken, String userID, String dataSetProcessingKey,
            long[] rawDataSampleIDs);
}
