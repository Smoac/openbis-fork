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

package ch.systemsx.cisd.openbis.plugin.query.server;

import java.util.List;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.openbis.generic.server.AbstractServerLogger;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IExpressionUpdates;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExpression;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.query.shared.IQueryServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryExpression;
import ch.systemsx.cisd.openbis.plugin.query.shared.basic.dto.QueryParameterBindings;

/**
 * @author Franz-Josef Elmer
 */
class QueryServerLogger extends AbstractServerLogger implements IQueryServer
{
    QueryServerLogger(final ISessionManager<Session> sessionManager,
            final boolean invocationSuccessful, final long elapsedTime)
    {
        super(sessionManager, invocationSuccessful, elapsedTime);
    }

    public String tryToGetQueryDatabaseLabel(String sessionToken)
    {
        logAccess(sessionToken, "try_to_get_query_database_label");
        return null;
    }

    public List<QueryExpression> listQueries(String sessionToken)
    {
        logAccess(sessionToken, "list_queries");
        return null;
    }

    public void registerQuery(String sessionToken, NewExpression expression)
    {
        logTracking(sessionToken, "register_query", "EXPRESSION(%s)", expression.getName());
    }

    public void deleteQueries(String sessionToken, List<TechId> filterIds)
    {
        logTracking(sessionToken, "delete_queries", "QUERIES(%s)", filterIds.size());
    }

    public void updateQuery(String sessionToken, IExpressionUpdates updates)
    {
        logTracking(sessionToken, "update_query", "ID(%s) QUERY_NAME(%s)", updates.getId(), updates
                .getName());
    }

    public TableModel queryDatabase(String sessionToken, String sqlQuery,
            QueryParameterBindings bindings)
    {
        logAccess(sessionToken, "query_database", "SQL(%s) BINDINGS(%s)", sqlQuery, bindings);
        return null;

    }

    public TableModel queryDatabase(String sessionToken, TechId queryId,
            QueryParameterBindings bindings)
    {
        logAccess(sessionToken, "query_database", "QUERY(%s) BINDINGS(%s)", queryId, bindings);
        return null;
    }

}
