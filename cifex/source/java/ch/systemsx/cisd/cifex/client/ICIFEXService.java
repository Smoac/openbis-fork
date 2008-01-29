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

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

import ch.systemsx.cisd.cifex.client.dto.File;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * The <i>GWT</i> <i>CIFEX</i> service.
 * 
 * @author Christian Ribeaud
 */
public interface ICIFEXService extends RemoteService
{

    /**
     * Authenticates given <code>user</code> with given <code>password</code>.
     * 
     * @return a <code>User</code> if the login was successful, <code>null</code> otherwise.
     */
    public User tryToLogin(final String user, final String password) throws UserFailureException;

    /**
     * Logout the current user.
     */
    public void logout();

    /**
     * Returns the currently logged user if this user is already authenticated.
     * 
     * @return the currently logged user.
     * @throws InvalidSessionException if user not logged in.
     */
    public User getCurrentUser() throws InvalidSessionException;

    /**
     * Returns a list of <code>User</code>s.
     * 
     * @gwt.typeArgs <ch.systemsx.cisd.cifex.client.dto.User>
     */
    public List listUsers();

    /**
     * Create a new Cifex <code>User</code> with the given parameters.
     */
    public void tryToCreateUser(final String email, final String username, final String password,
            final boolean permanent, final boolean admin);

    /**
     * List the files that the currently logged user has access on.
     * <p>
     * Never returns <code>null</code> but could return an empty array.
     * </p>
     */
    public File[] listDownloadFiles() throws UserFailureException;
}