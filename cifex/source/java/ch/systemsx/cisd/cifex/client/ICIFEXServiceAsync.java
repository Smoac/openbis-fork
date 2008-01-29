/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;

import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * The asynchronous <i>GWT</i> <i>LIMS</i> service.
 * 
 * @see ICIFEXService
 * @author Christian Ribeaud
 */
public interface ICIFEXServiceAsync extends RemoteService
{
    /**
     * Authenticates given <code>user</code> with given <code>password</code>.
     */
    public void tryToLogin(final String user, final String password, final AsyncCallback callback);

    /**
     * Logout the current user.
     */
    public void logout(final AsyncCallback callback);

    /**
     * Returns the currently logged user if this user is already authenticated.
     */
    public void getCurrentUser(final AsyncCallback callback);

    /**
     * Returns a list of <code>User</code>s.
     */
    public void listUsers(final AsyncCallback callback);

    /**
     * Creates a new <code>User</code> with the given parameter.
     */
    public void tryToCreateUser(final User user, final String password, final AsyncCallback callback);

    /**
     * List the files that the currently logged user has access on.
     */
    public void listDownloadFiles(final AsyncCallback callback);

}
