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

import java.util.Date;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.MsInjectionDataInfo;

/**
 * Example of usage of Raw Data API.
 *
 * @author Franz-Josef Elmer
 */
public class RawDataApiTest
{
    public static void main(String[] args)
    {
        if (args.length != 4)
        {
            System.err.println("Usage: <openbis-server-url> <login id> <password> <user id>");
            return;
        }

        String serverURL = args[0];
        String loginID = args[1];
        String password = args[2];
        String userID = args[3];
        IRawDataApiFacade facade = FacadeFactory.create(serverURL, loginID, password);
        
        System.out.println("MS_INJECTION samples:");
        List<MsInjectionDataInfo> rawDataSamples = facade.listRawDataSamples(userID);
        for (MsInjectionDataInfo info : rawDataSamples)
        {
            Map<String, Date> latestDataSets = info.getLatestDataSetRegistrationDates();
            if (latestDataSets.isEmpty() == false)
            {
                System.out.println("   " + info.getMsInjectionSampleCode() + " -> "
                        + info.getBiologicalSampleIdentifier() + ": latest data sets: "
                        + latestDataSets);
            }
        }
        
        System.out.println("DSS processing plugins:");
        List<DataStoreServerProcessingPluginInfo> infos = facade.listDataStoreServerProcessingPluginInfos();
        for (DataStoreServerProcessingPluginInfo info : infos)
        {
            System.out.println("   key:" + info.getKey() + ", label:'" + info.getLabel()
                    + "', data set types:" + info.getDatasetTypeCodes());
        }
        facade.logout();
    }
}
