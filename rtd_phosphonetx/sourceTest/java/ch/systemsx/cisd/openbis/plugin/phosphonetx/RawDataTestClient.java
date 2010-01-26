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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataService;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class RawDataTestClient
{
    private static final String SERVER_URL = "http://localhost:8888/openbis";
    private static final String SERVICE_PATH = SERVER_URL + "/rmi-phosphonetx-raw-data";

    public static void main(String[] args)
    {
        IRawDataService service = HttpInvokerUtils.createServiceStub(IRawDataService.class, SERVICE_PATH, 5);
        
        for (String user : new String[] {"test", "test_a", "test_b", "test_c"})
        {
            try
            {
                System.out.println("User: " + user);
                SessionContextDTO session = service.tryToAuthenticate("test", "a");
                String sessionToken = session.getSessionToken();
                List<Sample> samples = service.listRawDataSamples(sessionToken, user);
                for (Sample sample : samples)
                {
                    System.out.println("  " + sample.getCode()+" -> "+sample.getGeneratedFrom().getIdentifier());
                }
            } catch (UserFailureException ex)
            {
                System.out.println(" Exception: " + ex);
            }
            
        }
        System.out.println("--------------------");
        SessionContextDTO session = service.tryToAuthenticate("test_b", "t");
        String sessionToken = session.getSessionToken();
        
        List<Sample> samples = service.listRawDataSamples(sessionToken, "test_a");
        long[] ids = new long[samples.size()];
        for (int i = 0; i < samples.size(); i++)
        {
            Sample sample = samples.get(i);
            ids[i] = sample.getId();
        }
        service.copyRawData(sessionToken, "test_a", ids);
    }
}
