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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.api.v1;

import java.util.List;

import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.IRawDataService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.MsInjectionDataInfo;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class RawDataApiFacade implements IRawDataApiFacade
{
    private final IRawDataService service;
    private final String sessionToken;

    RawDataApiFacade(IRawDataService service, String sessionToken)
    {
        this.service = service;
        this.sessionToken = sessionToken;
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    public List<DataStoreServerProcessingPluginInfo> listDataStoreServerProcessingPluginInfos()
    {
        return service.listDataStoreServerProcessingPluginInfos(sessionToken);
    }

    public List<MsInjectionDataInfo> listRawDataSamples(String userID)
    {
        return service.listRawDataSamples(sessionToken, userID);
    }

    public void processingRawData(String userID, String dataSetProcessingKey,
            long[] rawDataSampleIDs, String dataSetType)
    {
        service.processingRawData(sessionToken, userID, dataSetProcessingKey, rawDataSampleIDs,
                dataSetType);
    }

    public void logout()
    {
        service.logout(sessionToken);
    }
    
}
