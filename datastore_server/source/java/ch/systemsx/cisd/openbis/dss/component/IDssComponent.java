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

package ch.systemsx.cisd.openbis.dss.component;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.openbis.dss.rpc.shared.IDataSetDss;

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
 * that can be used in multiple threads.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public interface IDssComponent
{
    /**
     * Authenticates the <code>user</code> with given <code>password</code>.
     */
    public void login(final String user, final String password)
            throws AuthorizationFailureException;

    /**
     * Checks whether the session is alive.
     * 
     * @throws InvalidSessionException If the session is not alive.
     */
    public void checkSession() throws InvalidSessionException;

    /**
     * Get a proxy to the data set designated by the given data set code.
     */
    public IDataSetDss getDataSet(String code);

    /**
     * Logs the current user out.
     */
    public void logout();

}
