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

import com.google.gwt.user.client.rpc.RemoteService;

import ch.systemsx.cisd.cifex.shared.basic.EnvironmentFailureException;
import ch.systemsx.cisd.cifex.shared.basic.UserFailureException;
import ch.systemsx.cisd.cifex.shared.basic.dto.CurrentUserInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileUploadFeedback;
import ch.systemsx.cisd.cifex.shared.basic.dto.OwnerFileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * The <i>GWT</i> <i>CIFEX</i> service.
 * 
 * @author Christian Ribeaud
 */
public interface ICIFEXService extends RemoteService
{

    /**
     * Sends a keep-alive ping and returns back whether it was successful, i.e. whether the session is still alive.
     */
    public Boolean keepSessionAlive();

    /**
     * Authenticates given <code>user</code> with given <code>password</code>.
     * <p>
     * If <code>requestAdmin==true</code>, then request an admin login.
     * 
     * @return a <code>User</code> if the login was successful, <code>null</code> otherwise.
     */
    public UserInfoDTO tryLogin(final String user, final String password)
            throws UserFailureException, EnvironmentFailureException;

    /**
     * Logout the current user.
     */
    public void logout();

    /**
     * Returns the configuration data of this CIFEX instance.
     * 
     * @return The configuration data. Never <code>null</code>.
     * @throws InvalidSessionException if user not logged in.
     */
    public Configuration getConfiguration() throws InvalidSessionException;

    /**
     * Returns the currently logged in user if this user is already authenticated.
     * 
     * @return The currently logged in user.
     * @throws InvalidSessionException if user not logged in.
     */
    public CurrentUserInfoDTO getCurrentUser() throws InvalidSessionException;

    /**
     * Returns the currently logged in user (if a user is authenticated), refreshing its quota information.
     * 
     * @return The currently logged in user with current quota information.
     */
    public UserInfoDTO refreshQuotaInformationOfCurrentUser() throws InvalidSessionException;

    /**
     * Returns a list of <code>User</code>s.
     */
    public List<UserInfoDTO> listUsers() throws InvalidSessionException,
            InsufficientPrivilegesException;

    /**
     * Returns the user for the given <var>id</var>.
     * 
     * @throws IllegalArgumentException If a user with that id does not exist.
     */
    public UserInfoDTO getUser(final long id) throws InvalidSessionException,
            InsufficientPrivilegesException;

    /**
     * Returns the user for the given <var>code</var>, or <code>null</code>, if no such user exists.
     */
    public UserInfoDTO tryFindUserByUserCode(final String userCode) throws InvalidSessionException;

    /**
     * Returns the user for the given <var>code</var>, or <code>null</code>, if no such user exists ann could not be created as externally
     * authenticated user.
     */
    public UserInfoDTO tryFindUserByUserCodeOrCreate(final String userCode)
            throws InvalidSessionException, UserFailureException, EnvironmentFailureException;

    /**
     * Returns a list with all users, which have the given email address.
     */
    public List<UserInfoDTO> findUserByEmail(final String email) throws InvalidSessionException, UserFailureException, EnvironmentFailureException;

    /**
     * Returns the list of users that are owned by the given user.
     */
    public List<UserInfoDTO> listUsersOwnedBy(final long userId) throws InvalidSessionException,
            InsufficientPrivilegesException;

    /**
     * Returns users the file with given <var>fileId</var> has been shared with.
     */
    public List<UserInfoDTO> listUsersFileSharedWith(final long fileId)
            throws InvalidSessionException, InsufficientPrivilegesException;

    /**
     * Creates a new <code>User</code> in Cifex with the given <var>password</var> and returns the new user.
     * <p>
     * This method sends an email to the new user, to inform him about the new user account.
     * </p>
     */
    public UserInfoDTO createUser(final UserInfoDTO user, final String password,
            final String comment) throws EnvironmentFailureException, UserFailureException,
            InvalidSessionException, InsufficientPrivilegesException;

    /**
     * Update the fields of the user in the database and returns the changed user.
     * 
     * @param sendUpdateInformationToUser Inform to user about the changes?
     */
    public UserInfoDTO updateUser(final UserInfoDTO user, final String password,
            final boolean sendUpdateInformationToUser) throws InvalidSessionException,
            InsufficientPrivilegesException, EnvironmentFailureException;

    /**
     * Changes the user code from <var>before</var> to <var>after</var>.
     * 
     * @param before - code of the user before renaming
     * @param after - code of the user after renaming
     */
    public void changeUserCode(final String before, final String after)
            throws InvalidSessionException, InsufficientPrivilegesException,
            EnvironmentFailureException;

    /**
     * Updates the sharing links for file <var>fileId</var>, adding the users in <var>usersToAdd</var> and removing the users in
     * <var>usersToRemove</var>.
     */
    public void updateSharingLinks(long fileId, List<String> usersToAdd, List<String> usersToRemove)
            throws InvalidSessionException, InsufficientPrivilegesException, FileNotFoundException,
            UserFailureException, EnvironmentFailureException;

    /**
     * Tries to delete the user given by its <var>id</var>.
     * 
     * @throws UserNotFoundException if the user with the given <var>userCode</var> was not found.
     */
    public void deleteUser(final long id) throws InvalidSessionException,
            InsufficientPrivilegesException, UserNotFoundException;

    /**
     * Returns the file identified by <var>fileId</var>.
     * 
     * @throws IllegalArgumentException If a file with that id doesn't exist.
     */
    public FileInfoDTO getFile(final long fileId) throws InvalidSessionException,
            InsufficientPrivilegesException, IllegalArgumentException;

    /**
     * List the files that have been uploaded for the currently logged in user.
     * <p>
     * Never returns <code>null</code> but could return an empty array.
     * </p>
     */
    public List<FileInfoDTO> listDownloadFiles() throws InvalidSessionException;

    /**
     * List the files directly or indirectly owned by the currently logged user.
     * <p>
     * Never returns <code>null</code> but could return an empty array.
     * </p>
     */
    public List<OwnerFileInfoDTO> listOwnedFiles() throws InvalidSessionException;

    /**
     * List all files (only for admins).
     */
    public List<OwnerFileInfoDTO> listFiles() throws InvalidSessionException,
            InsufficientPrivilegesException;

    /**
     * Deletes file given by its <var>idStr</var>.
     * 
     * @throws FileNotFoundException If the file defined by <var>idStr</var> could not be found.
     */
    public void deleteFile(final long id) throws InvalidSessionException,
            InsufficientPrivilegesException, FileNotFoundException;

    /**
     * Registers the file names for the next upload request in the session.
     * 
     * @param filenamesForUpload the client absolute file paths. Can not be <code>null</code> or empty.
     */
    public void registerFilenamesForUpload(final String[] filenamesForUpload)
            throws InvalidSessionException;

    /**
     * Gets current file upload feedback.
     * <p>
     * Note that this method never returns <code>null</code> but waits till the first feedback is in the queue.
     * </p>
     */
    public FileUploadFeedback getFileUploadFeedback() throws InvalidSessionException;

    /**
     * Update the user data of the <var>fileId</var>.
     * 
     * @param fileId The id of the file to update.
     * @param name The new name of the file.
     * @param commentOrNull The new comment of the file.
     * @param expirationDate The new (requested) expiration date.
     * @return The new expiration date.
     */
    public Date updateFileUserData(final long fileId, final String name,
            final String commentOrNull, final Date expirationDate) throws InvalidSessionException,
            InsufficientPrivilegesException;

    /**
     * Try to change user type from internally authenticated to externally authenticated.
     * 
     * @throws EnvironmentFailureException if user is already externally authenticated or user with given code does not exist
     */
    public UserInfoDTO trySwitchToExternalAuthentication(final String userCode,
            final String plainPassword) throws EnvironmentFailureException,
            InvalidSessionException, InsufficientPrivilegesException;

    /**
     * Checks if 'switch to external authentication' option should be available for given <code>User<code>.
     */
    public Boolean showSwitchToExternalOption(UserInfoDTO user);

}