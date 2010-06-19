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
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.cifex.shared.basic.dto.CurrentUserInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileUploadFeedback;
import ch.systemsx.cisd.cifex.shared.basic.dto.OwnerFileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * The asynchronous <i>GWT</i> <i>LIMS</i> service.
 * 
 * @see ICIFEXService
 * @author Christian Ribeaud
 */
public interface ICIFEXServiceAsync
{
    /**
     * Sends a keep-alive ping and returns back whether it was successful, i.e. whether the session
     * is still alive.
     */
    public void keepSessionAlive(final AsyncCallback<Boolean> callback);

    /**
     * Authenticates given <code>user</code> with given <code>password</code>.
     */
    public void tryLogin(final String userCode, final String password,
            final AsyncCallback<CurrentUserInfoDTO> callback);

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
    public void getCurrentUser(final AsyncCallback<CurrentUserInfoDTO> callback);

    /**
     * Returns the currently logged in user (if a user is authenticated), refreshing its quota
     * information.
     */
    public void refreshQuotaInformationOfCurrentUser(final AsyncCallback<UserInfoDTO> callback);

    /**
     * Returns a list of <code>User</code>s.
     */
    public void listUsers(final AsyncCallback<List<UserInfoDTO>> callback);

    /** Returns the list of users that are owned by the given user. */
    public void listUsersOwnedBy(final long userId, final AsyncCallback<List<UserInfoDTO>> callback);

    /**
     * Returns the user for the given <var>id</var>.
     * 
     * @throws IllegalArgumentException If a user with that id does not exist.
     */
    public void getUser(final long id, final AsyncCallback<UserInfoDTO> callback);

    /** Gets the user by the userCode. */
    public void tryFindUserByUserCode(final String userCode,
            final AsyncCallback<UserInfoDTO> callback);

    /** Gets the user by the userCode, creates it if user exists in external authentication service. */
    public void tryFindUserByUserCodeOrCreate(final String userCode,
            final AsyncCallback<UserInfoDTO> callback);

    /** Returns a list of users with the given email. */
    public void findUserByEmail(final String email, final AsyncCallback<List<UserInfoDTO>> callback);

    /**
     * Creates a new <code>User</code> in Cifex with the given <var>password</var>.
     * <p>
     * This method sends an email to the new user, to inform him about the new user account.
     * </p>
     */
    public void createUser(final UserInfoDTO user, final String password, final String comment,
            final AsyncCallback<UserInfoDTO> callback);

    /**
     * Update the fields of the user in the database.
     * 
     * @param sendUserNotification Should the user receive a mail with the new information?
     */
    public void updateUser(final UserInfoDTO user, final String password,
            final boolean sendUserNotification, final AsyncCallback<UserInfoDTO> callback);

    /**
     * Changes the user code from <var>before</var> to <var>after</var>.
     */
    public void changeUserCode(final String before, final String after,
            final AsyncCallback<Void> callback);

    /**
     * Deletes an user given by its <var>id</var>.
     */
    public void deleteUser(final long id, final AsyncCallback<Void> callback);

    /**
     * Deals with the file identified by <var>fileId</var>.
     * 
     * @throws IllegalArgumentException If a file with that id doesn't exist.
     */
    public void getFile(final long fileId, final AsyncCallback<FileInfoDTO> callback);

    /**
     * List the files that have been uploaded for the currently logged in user.
     */
    public void listDownloadFiles(final AsyncCallback<List<FileInfoDTO>> callback);

    /**
     * List the files directly or indirectly owned by the currently logged user.
     */
    public void listOwnedFiles(final AsyncCallback<List<OwnerFileInfoDTO>> fileAsyncCallback);

    /**
     * List all files (only for admins).
     */
    public void listFiles(final AsyncCallback<List<OwnerFileInfoDTO>> fileAsyncCallback);

    /**
     * Deletes file given by its <code>id</code>.
     */
    public void deleteFile(final long id, final AsyncCallback<Void> callback);

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
     * Update the user data of the <var>fileId</var>. The callback will return the expiration date
     * actually set for the file.
     */
    public void updateFileUserData(final long fileId, final String name,
            final String commentOrNull, final Date expirationDate,
            final AsyncCallback<Date> callback);

    /**
     * List users the file with given <var>fileId</var> has been shared with.
     */
    public void listUsersFileSharedWith(long fileId,
            AsyncCallback<List<UserInfoDTO>> showUsersFileSharedWithAsyncCallback);

    /**
     * Updates the sharing links for file <var>fileId</var>, adding the users in
     * <var>usersToAdd</var> and removing the users in <var>usersToRemove</var>.
     */
    public void updateSharingLinks(long fileId, List<String> usersToAdd,
            List<String> usersToRemove, AsyncCallback<Void> callback);

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
