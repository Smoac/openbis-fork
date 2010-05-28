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

package ch.systemsx.cisd.openbis.dss.client.api.v1;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.IDssServiceRpcGeneric;

/**
 * A component that manages a connection to openBIS and 1 or more data store servers.
 * <p>
 * The component is a kind of state machine. In the initial state, only login is allowed. After
 * login, other operations may be called. Thus clients should follow the following usage pattern:
 * <ol>
 * <li>login</li>
 * <li>...do stuff...</li>
 * <li>logout</li>
 * </ol>
 * <p>
 * The IDssComponent itself is designed to be used in a single thread, though it may return objects
 * that can be used in multiple threads. Documentation for the return values clairifies their level
 * of thread safety.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDssComponent
{
    /**
     * Checks whether the session is alive.
     * 
     * @throws InvalidSessionException If the session is not alive.
     */
    public void checkSession() throws InvalidSessionException;

    /**
     * Returns the session token.
     * 
     * @return The session token for an authenticated user.
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     */
    public String getSessionToken() throws IllegalStateException;

    /**
     * Get a proxy to the data set designated by the given data set code.
     * 
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to
     *             the server.
     */
    public IDataSetDss getDataSet(String code) throws IllegalStateException,
            EnvironmentFailureException;

    /**
     * Get a proxy to the default DSS server for the openBIS AS.
     * 
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to
     *             the server.
     */
    public IDssServiceRpcGeneric getDefaultDssService() throws IllegalStateException,
            EnvironmentFailureException;

    /**
     * Logs the current user out.
     */
    public void logout();

}
