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
     * <p>
     * If <code>requestAdmin==true</code>, then request an admin login.
     */
    public void tryToLogin(final String userCode, final String password, final boolean requestAdmin,
            final AsyncCallback callback);

    /**
     * Logout the current user.
     */
    public void logout(final AsyncCallback callback);

    /**
     * Returns the configuration data of this CIFEX instance.
     */
    public void getConfiguration(final AsyncCallback callback);

    /**
     * Returns the currently logged user if this user is already authenticated.
     */
    public void getCurrentUser(final AsyncCallback callback);

    /**
     * Returns a list of <code>User</code>s.
     */
    public void listUsers(final AsyncCallback callback);

    /**
     * Creates a new <code>User</code> with the given <var>password</var>. If <var>registratorOrNull</var> is not
     * <code>null</code>, it will be interpreted as the user who creates the new user.
     */
    public void tryToCreateUser(final User user, final String password, final User registratorOrNull,
            final AsyncCallback callback);

    /**
     * List the files that the currently logged user has access on.
     */
    public void listDownloadFiles(final AsyncCallback callback);

    /**
     * Deletes an user given by its <var>userCode</var>.
     */
    public void tryToDeleteUser(final String userCode, final AsyncCallback callback);

    /**
     * Deletes file given by its <code>id</code>.
     */
    public void tryToDeleteFile(final long id, final AsyncCallback callback);

    /**
     * List the files uploaded by the currently logged user.
     */
    public void listUploadedFiles(final AsyncCallback fileAsyncCallback);

    /**
     * Registers the file names for the next upload request in the session.
     */
    public void registerFilenamesForUpload(final String[] filenamesForUpload, final AsyncCallback callback);

    /**
     * Waits for the file upload to finish.
     */
    public void waitForUploadToFinish(final AsyncCallback callback);

    /**
     * Returns the footer data (version and administrator email).
     */
    public void getFooterData(final AsyncCallback callback);

}
