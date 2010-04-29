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

import java.util.List;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.plugin.query.server.api.v1.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class QueryApiFacade
{
    private static final int SERVER_TIMEOUT_MIN = 5;
    
    public static QueryApiFacade create(String serverURL, String userID, String password)
    {
        IQueryApiServer service = HttpInvokerUtils.createServiceStub(IQueryApiServer.class, serverURL + ResourceNames.QUERY_PLUGIN_SERVER_URL, SERVER_TIMEOUT_MIN);
        String sessionToken = service.tryToAuthenticateAtQueryServer(userID, password);
        if (sessionToken == null)
        {
            throw new IllegalArgumentException("User " + userID + "couldn't be authenticated");
        }
        return new QueryApiFacade(service, sessionToken);
    }
    
    private final IQueryApiServer service;
    private final String sessionToken;
    
    QueryApiFacade(IQueryApiServer service, String sessionToken)
    {
        this.service = service;
        this.sessionToken = sessionToken;
    }
    
    public void logout()
    {
        service.logout(sessionToken);
    }
    
    public List<QueryDescription> listQueries()
    {
        return service.listQueries(sessionToken);
    }
}
