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

import java.util.Date;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;

import ch.systemsx.cisd.cifex.client.dto.FileUploadFeedback;
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
    public void tryLogin(final String userCode, final String password, final AsyncCallback callback);

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

    /** Returns a list of users, which where registered by the given user. */
    public void listUsersRegisteredBy(final String userCode, final AsyncCallback callback);

    /** Gets the user by the userCode. */
    public void tryFindUserByUserCode(final String userCode, final AsyncCallback callback);

    /**
     * Creates a new <code>User</code> with the given <var>password</var>. If <var>registratorOrNull</var> is not
     * <code>null</code>, it will be interpreted as the user who creates the new user.
     */
    public void createUser(final User user, final String password, final User registratorOrNull,
            final String comment, final AsyncCallback callback);

    /** Update the fields of the user in the database. */
    public void updateUser(final User user, final String password, final AsyncCallback callback);

    /**
     * Deletes an user given by its <var>userCode</var>.
     */
    public void deleteUser(final String userCode, final AsyncCallback callback);

    /**
     * List the files that have been uploaded for the currently logged in user.
     */
    public void listDownloadFiles(final AsyncCallback callback);

    /**
     * List the files uploaded by the currently logged user.
     */
    public void listUploadedFiles(final AsyncCallback fileAsyncCallback);

    /**
     * List all files (only for admins).
     */
    public void listFiles(final AsyncCallback fileAsyncCallback);

    /**
     * Deletes file given by its <code>id</code>.
     */
    public void deleteFile(final long id, final AsyncCallback callback);

    /**
     * Registers the file names for the next upload request in the session.
     * 
     * @param filenamesForUpload the client absolute file paths. Can not be <code>null</code> or empty.
     */
    public void registerFilenamesForUpload(final String[] filenamesForUpload,
            final AsyncCallback callback);

    /**
     * Get file upload feedback.
     * <p>
     * A never-<code>null</code> {@link FileUploadFeedback} is expected in given <code>AsyncCallback</code>.
     * </p>
     */
    public void getFileUploadFeedback(final AsyncCallback callback);

    /**
     * Update the Expiration Date of the file with the given ID. Only an Admin can set an own ExpirationDate, for all
     * the others, the default expiration Date is used.
     * 
     * @param newExpirationDate The new Expiration date, can only used from an admin. <code>null</code> if the default
     *            expiration date should be used.
     */
    public void updateFileExpiration(final long fileId, final Date newExpirationDate,
            final AsyncCallback callback);

}
