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
import java.util.Map;

import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * Facade for openBIS query service.
 * 
 * @author Franz-Josef Elmer
 */
public interface IQueryApiFacade
{
    /**
     * Return the session token for the logged-in user.
     */
    public String getSessionToken();

    /**
     * Lists all queries the user has access rights.
     */
    public List<QueryDescription> listQueries();

    /**
     * Executes specified query by using specified parameter bindings.
     */
    public QueryTableModel executeQuery(long queryID, Map<String, String> parameterBindings);

    /**
     * Logs current user out.
     */
    public void logout();

}