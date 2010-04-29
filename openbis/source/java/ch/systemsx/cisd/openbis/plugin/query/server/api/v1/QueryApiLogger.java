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

package ch.systemsx.cisd.openbis.plugin.query.server.api.v1;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class QueryApiLogger extends AbstractServerLogger implements IQueryApiServer
{

    QueryApiLogger(ISessionManager<Session> sessionManager, IInvocationLoggerContext context)
    {
        super(sessionManager, context);
    }

    public QueryTableModel executeQuery(long queryID, Map<String, String> parameterBindings)
    {
        return null;
    }

    public List<QueryDescription> listQueries(String sessionToken)
    {
        logAccess(sessionToken, "list_queries");
        return null;
    }

    public String tryToAuthenticateAtQueryServer(String userID, String userPassword)
    {
        return null;
    }

}
