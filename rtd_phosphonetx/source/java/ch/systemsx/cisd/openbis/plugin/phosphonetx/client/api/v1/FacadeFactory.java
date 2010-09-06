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
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.IProteomicsDataService;

/**
 * Factory of {@link IProteomicsDataApiFacade}.
 * 
 * @author Franz-Josef Elmer
 */
public class FacadeFactory
{
    private static final ServiceFinder SERVICE_FINDER =
            new ServiceFinder("openbis", IProteomicsDataService.SERVER_URL);

    private static final ServiceFinder GENERIC_INFO_SERVICE_FINDER =
            new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);

    /**
     * Creates a facade for specified server URL, user Id, and password.
     */
    public static IProteomicsDataApiFacade create(String serverURL, String userID, String password)
    {
        IGeneralInformationService infoService = createGenericInfoService(serverURL);
        IProteomicsDataService service = createService(serverURL);
        String sessionToken = infoService.tryToAuthenticateForAllServices(userID, password);
        if (sessionToken == null)
        {
            throw new IllegalArgumentException("User " + userID + " couldn't be authenticated");
        }
        return new ProteomicsDataApiFacade(service, infoService, sessionToken);
    }

    /**
     * Creates a facade for specified url and sessionToken.
     */
    public static IProteomicsDataApiFacade create(String serverURL, String sessionToken)
    {
        IProteomicsDataService service = createService(serverURL);
        IGeneralInformationService infoService = createGenericInfoService(serverURL);
        return new ProteomicsDataApiFacade(service, infoService, sessionToken);
    }

    private static IProteomicsDataService createService(String serverURL)
    {
        return SERVICE_FINDER.createService(IProteomicsDataService.class, serverURL);
    }

    private static IGeneralInformationService createGenericInfoService(String serverURL)
    {
        return GENERIC_INFO_SERVICE_FINDER.createService(IGeneralInformationService.class, serverURL);
    }
    
}
