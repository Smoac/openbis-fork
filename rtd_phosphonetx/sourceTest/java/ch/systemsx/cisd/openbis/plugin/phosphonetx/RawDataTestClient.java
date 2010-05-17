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

package ch.systemsx.cisd.openbis.plugin.phosphonetx;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.DefaultLimsServiceStubFactory;
import ch.systemsx.cisd.openbis.generic.shared.OpenBisServiceFactory;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.IRawDataService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.MsInjectionDataInfo;

/**
 * @author Franz-Josef Elmer
 */
public class RawDataTestClient
{
    private static final String SERVER_URL = "http://localhost:8888";

    private static final String SERVICE_PATH_SUFFIX = "/rmi-phosphonetx-raw-data-v1";

    private static String getServicePath()
    {
        OpenBisServiceFactory openBisServiceFactory =
                new OpenBisServiceFactory(SERVER_URL, new DefaultLimsServiceStubFactory());
        openBisServiceFactory.createService();
        return openBisServiceFactory.getUsedServerUrl() + SERVICE_PATH_SUFFIX;
    }

    public static void main(String[] args)
    {
        // Create the service so we can figure out which URL to use.
        String servicePath = getServicePath();
        IRawDataService service =
                HttpInvokerUtils.createServiceStub(IRawDataService.class, servicePath, 5);

        for (String user : new String[]
            { "test", "test_a", "test_b", "test_c" })
        {
            try
            {
                System.out.println("User: " + user);
                String sessionToken = service.tryToAuthenticateAtRawDataServer("test", "a");
                List<DataStoreServerProcessingPluginInfo> services =
                        service.listDataStoreServerProcessingPluginInfos(sessionToken);
                for (DataStoreServerProcessingPluginInfo info : services)
                {
                    System.out.print(info.getLabel() + " ");
                }
                System.out.println();
                List<MsInjectionDataInfo> samples = service.listRawDataSamples(sessionToken, user);
                for (MsInjectionDataInfo sample : samples)
                {
                    System.out.println("  " + sample.getMsInjectionSampleCode() + " -> "
                            + sample.getBiologicalSampleIdentifier());
                }
            } catch (UserFailureException ex)
            {
                System.out.println(" Exception: " + ex);
            }

        }
        System.out.println("--------------------");
        String sessionToken = service.tryToAuthenticateAtRawDataServer("test_b", "t");

        List<MsInjectionDataInfo> samples = service.listRawDataSamples(sessionToken, "test_a");
        long[] ids = new long[samples.size()];
        for (int i = 0; i < samples.size(); i++)
        {
            MsInjectionDataInfo sample = samples.get(i);
            ids[i] = sample.getMsInjectionSampleID();
        }
        service.processingRawData(sessionToken, "test_a", "copy-data-sets", ids, null);
    }
}
