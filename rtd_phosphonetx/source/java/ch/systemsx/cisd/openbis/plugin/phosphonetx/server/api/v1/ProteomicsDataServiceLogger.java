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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.api.v1;

import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.IProteomicsDataService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.MsInjectionDataInfo;

/**
 * @author Franz-Josef Elmer
 */
class ProteomicsDataServiceLogger extends AbstractServerLogger implements IProteomicsDataService
{

    ProteomicsDataServiceLogger(ISessionManager<Session> sessionManager, IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    public String tryToAuthenticateAtRawDataServer(String userID, String userPassword)
    {
        return null;
    }

    public List<MsInjectionDataInfo> listRawDataSamples(String sessionToken, String userID)
    {
        logAccess(sessionToken, "list_raw_data_samples", "USER_ID(%s)", userID);
        return null;
    }

    public List<DataStoreServerProcessingPluginInfo> listDataStoreServerProcessingPluginInfos(
            String sessionToken)
    {
        logAccess(sessionToken, "list_data_store_services", "");
        return null;
    }

    public void processingRawData(String sessionToken, String userID, String dataSetProcessingKey,
            long[] rawDataSampleIDs, String dataSetType)
    {
        int numberOfDataSets = rawDataSampleIDs == null ? 0 : rawDataSampleIDs.length;
        logAccess(sessionToken, "copy_raw_data",
                "USER_ID(%s) DSS_PROCESSING_PLUGIN(%s) NUMBER_OF_DATA_SETS(%s) DATA_SET_TYPE(%s)",
                userID, dataSetProcessingKey, numberOfDataSets, dataSetType);
    }

    public void processDataSets(String sessionToken, String userID, String dataSetProcessingKey,
            List<String> dataSetCodes)
    {
        logAccess(sessionToken, "process_data_sets",
                "USER_ID(%s) DSS_PROCESSING_PLUGIN(%s) DATA_SETS(%s)", userID,
                dataSetProcessingKey, dataSetCodes);
    }

    public List<Experiment> listSearchExperiments(String sessionToken, String userID)
    {
        logAccess(sessionToken, "list_search_experiments");
        return null;
    }

    public void processSearchData(String sessionToken, String userID, String dataSetProcessingKey,
            long[] searchExperimentIDs)
    {
        int numberOfDataSets = searchExperimentIDs == null ? 0 : searchExperimentIDs.length;
        logAccess(sessionToken, "copy_search_data", "NUMBER_OF_DATA_SETS(%s)", numberOfDataSets);
    }

    public int getMajorVersion()
    {
        return 1;
    }

    public int getMinorVersion()
    {
        return 0;
    }

}
