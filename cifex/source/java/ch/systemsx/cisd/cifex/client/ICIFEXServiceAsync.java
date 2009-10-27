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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;

import ch.systemsx.cisd.cifex.shared.basic.dto.AdminFileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileUploadFeedback;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

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
    public void tryLogin(final String userCode, final String password,
            final AsyncCallback<UserInfoDTO> callback);

    /**
     * Logout the current user.
     */
    public void logout(final AsyncCallback<Void> callback);

    /**
     * Returns the configuration data of this CIFEX instance.
     */
    public void getConfiguration(final AsyncCallback<Configuration> callback);

    /**
     * Returns the currently logged user if this user is already authenticated.
     */
    public void getCurrentUser(final AsyncCallback<UserInfoDTO> callback);

    /**
     * Returns a list of <code>User</code>s.
     */
    public void listUsers(final AsyncCallback<List<UserInfoDTO>> callback);

    /** Returns a list of users, which where registered by the given user. */
    public void listUsersRegisteredBy(final String userCode,
            final AsyncCallback<List<UserInfoDTO>> callback);

    /** Gets the user by the userCode. */
    public void tryFindUserByUserCode(final String userCode,
            final AsyncCallback<UserInfoDTO> callback);

    /** Returns a list of users with the given email. */
    public void tryFindUserByEmail(final String email,
            final AsyncCallback<List<UserInfoDTO>> callback);

    /**
     * Creates a new <code>User</code> with the given <var>password</var>. If
     * <var>registratorOrNull</var> is not <code>null</code>, it will be interpreted as the user who
     * creates the new user.
     */
    public void createUser(final UserInfoDTO user, final String password,
            final UserInfoDTO registratorOrNull, final String comment,
            final AsyncCallback<Void> callback);

    /**
     * Update the fields of the user in the database.
     * 
     * @param sendUserNotification Should the user receive a mail with the new information?
     */
    public void updateUser(final UserInfoDTO user, final String password,
            final boolean sendUserNotification, final AsyncCallback<Void> callback);

    /**
     * Changes the user code from <var>before</var> to <var>after</var>.
     */
    public void changeUserCode(final String before, final String after,
            final AsyncCallback<Void> callback);

    /**
     * Deletes an user given by its <var>userCode</var>.
     */
    public void deleteUser(final String userCode, final AsyncCallback<Void> callback);

    /**
     * List the files that have been uploaded for the currently logged in user.
     */
    public void listDownloadFiles(final AsyncCallback<List<FileInfoDTO>> callback);

    /**
     * List the files uploaded by the currently logged user.
     */
    public void listUploadedFiles(final AsyncCallback<List<FileInfoDTO>> fileAsyncCallback);

    /**
     * List all files (only for admins).
     */
    public void listFiles(final AsyncCallback<List<AdminFileInfoDTO>> fileAsyncCallback);

    /**
     * Deletes file given by its <code>idStr</code>.
     */
    public void deleteFile(final String idStr, final AsyncCallback<Void> callback);

    /**
     * Registers the file names for the next upload request in the session.
     * 
     * @param filenamesForUpload the client absolute file paths. Can not be <code>null</code> or
     *            empty.
     */
    public void registerFilenamesForUpload(final String[] filenamesForUpload,
            final AsyncCallback<Void> callback);

    /**
     * Get file upload feedback.
     * <p>
     * A never-<code>null</code> {@link FileUploadFeedback} is expected in given
     * <code>AsyncCallback</code>.
     * </p>
     */
    public void getFileUploadFeedback(final AsyncCallback<FileUploadFeedback> callback);

    /**
     * Update the Expiration Date of the file with the given <var>fileIdStr</var>.
     */
    public void updateFileExpiration(final String fileIdStr, final AsyncCallback<Void> callback);

    /**
     * List users the file with given <var>fileId</var> has been shared with.
     */
    public void listUsersFileSharedWith(String fileId,
            AsyncCallback<List<UserInfoDTO>> showUsersFileSharedWithAsyncCallback);

    /** Revokes user with given userCode access to file with fileId. */
    public void deleteSharingLink(String fileId, String userCode, AsyncCallback<Void> callback);

    /**
     * Creates a sharing link between file and users.
     */
    public void createSharingLink(String fileId, String emailsOfUsers, AsyncCallback<Void> callback);

    /**
     * Try to change user type from internally authenticated to externally authenticated.
     */
    public void trySwitchToExternalAuthentication(final String userCode,
            final String plainPassword, final AsyncCallback<UserInfoDTO> callback);

    /**
     * Checks if 'switch to external authentication' option should be available for given
     * <code>User<code>.
     */
    public void showSwitchToExternalOption(UserInfoDTO user, AsyncCallback<Boolean> callback);
}
