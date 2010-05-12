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

package ch.systemsx.cisd.openbis.plugin.query.client.api.v1;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.DefaultLimsServiceStubFactory;
import ch.systemsx.cisd.openbis.generic.shared.OpenBisServiceFactory;
import ch.systemsx.cisd.openbis.plugin.query.server.api.v1.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;

/**
 * Factory of {@link IQueryApiFacade}.
 * 
 * @author Franz-Josef Elmer
 */
public class FacadeFactory
{
    private static final int SERVER_TIMEOUT_MIN = 5;

    // Trick for discovering the server Url
    private static String getServiceUrl(String serverUrl)
    {
        OpenBisServiceFactory openBisServiceFactory =
                new OpenBisServiceFactory(serverUrl, new DefaultLimsServiceStubFactory());
        openBisServiceFactory.createService();
        return openBisServiceFactory.getUsedServerUrl() + ResourceNames.QUERY_PLUGIN_SERVER_URL;
    }

    /**
     * Creates a facade for specified server URL, user Id, and password.
     */
    public static IQueryApiFacade create(String serverURL, String userID, String password)
    {
        String serviceUrl = getServiceUrl(serverURL);
        IQueryApiServer service =
                HttpInvokerUtils.createServiceStub(IQueryApiServer.class, serviceUrl,
                        SERVER_TIMEOUT_MIN);
        String sessionToken = service.tryToAuthenticateAtQueryServer(userID, password);
        if (sessionToken == null)
        {
            throw new IllegalArgumentException("User " + userID + " couldn't be authenticated");
        }
        return new QueryApiFacade(service, sessionToken);
    }

    /**
     * Creates a facade for specified url and sessionToken.
     */
    public static IQueryApiFacade create(String serverURL, String sessionToken)
    {
        IQueryApiServer service =
                HttpInvokerUtils.createServiceStub(IQueryApiServer.class, serverURL
                        + ResourceNames.QUERY_PLUGIN_SERVER_URL, SERVER_TIMEOUT_MIN);
        return new QueryApiFacade(service, sessionToken);
    }
}
