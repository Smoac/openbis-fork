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
     * <p>
     * If <code>requestAdmin==true</code>, then request an admin login.
     * 
     * @return a <code>User</code> if the login was successful, <code>null</code> otherwise.
     */
    public User tryToLogin(final String user, final String password, final boolean requestAdmin)
            throws UserFailureException;

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
     */
    public User[] listUsers() throws InvalidSessionException, InsufficientPrivilegesException;

    /**
     * Creates a new <code>User</code> in Cifex with the given <var>password</var>. If <var>registratorOrNull</var>
     * is not <code>null</code>, it will be interpreted as the user who creates the new user.
     */
    public void tryToCreateUser(final User user, final String password, final User registratorOrNull)
            throws EnvironmentFailureException, InvalidSessionException, InsufficientPrivilegesException;

    /**
     * List the files that the currently logged user has access on.
     * <p>
     * Never returns <code>null</code> but could return an empty array.
     * </p>
     */
    public File[] listDownloadFiles() throws InvalidSessionException;

    /**
     * Tries to delete the user given by its <var>email</var>.
     */
    public void tryToDeleteUser(final String email) throws InvalidSessionException, InsufficientPrivilegesException;

    /**
     * Deletes file given by its <var>id</var>
     */
    public void tryToDeleteFile(final long id) throws InvalidSessionException, InsufficientPrivilegesException;

    /**
     * List the files uploaded by the currently logged user.
     * <p>
     * Never returns <code>null</code> but could return an empty array.
     * </p>
     */
    public File[] listUploadedFiles() throws InvalidSessionException;
}