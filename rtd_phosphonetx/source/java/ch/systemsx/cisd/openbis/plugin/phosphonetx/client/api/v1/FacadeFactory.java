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

import ch.systemsx.cisd.common.api.client.ServiceFinder;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.api.v1.Constants;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.IRawDataService;

/**
 * Factory of {@link IRawDataApiFacade}.
 *
 * @author Franz-Josef Elmer
 */
public class FacadeFactory
{
    private static final ServiceFinder SERVICE_FINDER = new ServiceFinder("openbis", Constants.RAW_DATA_SERVER_URL);

    /**
     * Creates a facade for specified server URL, user Id, and password.
     */
    public static IRawDataApiFacade create(String serverURL, String userID, String password)
    {
        IRawDataService service = createService(serverURL);
        String sessionToken = service.tryToAuthenticateAtRawDataServer(userID, password);
        if (sessionToken == null)
        {
            throw new IllegalArgumentException("User " + userID + " couldn't be authenticated");
        }
        return new RawDataApiFacade(service, sessionToken);
    }

    /**
     * Creates a facade for specified url and sessionToken.
     */
    public static IRawDataApiFacade create(String serverURL, String sessionToken)
    {
        return new RawDataApiFacade(createService(serverURL), sessionToken);
    }

    private static IRawDataService createService(String serverURL)
    {
        return SERVICE_FINDER.createService(IRawDataService.class, serverURL);
    }
    
}
