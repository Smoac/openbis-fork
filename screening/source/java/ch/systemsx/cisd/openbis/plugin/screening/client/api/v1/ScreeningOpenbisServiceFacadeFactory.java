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

package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import ch.systemsx.cisd.openbis.generic.shared.DefaultLimsServiceStubFactory;
import ch.systemsx.cisd.openbis.generic.shared.OpenBisServiceFactory;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class ScreeningOpenbisServiceFacadeFactory
{
    // Helper method for figuring out the actual url of openbis.
    private static String getOpenBisUrl(String serverUrl)
    {
        OpenBisServiceFactory openBisServiceFactory =
                new OpenBisServiceFactory(serverUrl, new DefaultLimsServiceStubFactory());
        openBisServiceFactory.createService();
        return openBisServiceFactory.getUsedServerUrl();
    }

    /**
     * Creates a service facade which communicates with the openBIS server at the specified URL.
     * Authenticates the user.
     * 
     * @return null if the user could not be authenticated.
     */
    public static IScreeningOpenbisServiceFacade tryCreate(String userId, String userPassword,
            String serverUrl)
    {
        String openBisUrl = getOpenBisUrl(serverUrl);
        return ScreeningOpenbisServiceFacade.tryCreate(userId, userPassword, openBisUrl);
    }

    /**
     * Creates a service facade which communicates with the openBIS server at the specified URL for
     * an authenticated user.
     * 
     * @param sessionToken The session token for the authenticated user
     * @param serverUrl The URL for the openBIS application server
     */
    public static IScreeningOpenbisServiceFacade tryCreate(String sessionToken, String serverUrl)
    {
        return ScreeningOpenbisServiceFacade.tryCreate(sessionToken, serverUrl);
    }
}
