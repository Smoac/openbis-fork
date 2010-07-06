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

package ch.systemsx.cisd.openbis.plugin.query.shared.api.v1;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.api.IRpcService;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;

/**
 * Public API interface to query server (version 1).
 * 
 * @author Franz-Josef Elmer
 */
// DO NOT CHANGE THE INTERFACE CONTRACT IN A NON-BACKWARD COMPATIBLE WAY!
public interface IQueryApiServer extends IRpcService
{
    /**
     * Name of this service for which it is registered at the RPC name server.
     */
    public static final String SERVICE_NAME = "query";
    
    /**
     * Service part of the URL to access this service remotely.
     */
    public final static String QUERY_PLUGIN_SERVER_URL = "/rmi-" + SERVICE_NAME + "-v1";
    
    /**
     * Tries to authenticate specified user with specified password. Returns session token if
     * succeeded otherwise <code>null</code> is returned.
     */
    @Transactional
    // this is not a readOnly transaction - it can create new users
    public String tryToAuthenticateAtQueryServer(String userID, String userPassword);

    /**
     * Logout the session with the specified session token.
     */
    @Transactional(readOnly = true)
    public void logout(String sessionToken);

    /**
     * Lists all queries available for the user of the specified session.
     */
    @Transactional(readOnly = true)
    public List<QueryDescription> listQueries(String sessionToken);

    /**
     * Executes specified query using specified parameter bindings.
     */
    @Transactional(readOnly = true)
    public QueryTableModel executeQuery(String sessionToken, long queryID,
            Map<String, String> parameterBindings);

}
