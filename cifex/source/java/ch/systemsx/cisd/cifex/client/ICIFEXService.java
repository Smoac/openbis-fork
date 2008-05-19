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

import com.google.gwt.user.client.rpc.RemoteService;

import ch.systemsx.cisd.cifex.client.dto.Configuration;
import ch.systemsx.cisd.cifex.client.dto.File;
import ch.systemsx.cisd.cifex.client.dto.FileUploadFeedback;
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
    public User tryLogin(final String user, final String password) throws UserFailureException,
            EnvironmentFailureException;

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
     * Returns the user for the given <var>code</var>, or <code>null</code>, if no such user
     * exists.
     * 
     * @throws InvalidSessionException
     */
    public User tryFindUserByUserCode(final String userCode) throws InvalidSessionException;

    /**
     * Returns a list of users, which where registered by the given user.
     * 
     * @throws InvalidSessionException
     */
    public User[] listUsersRegisteredBy(final String userCode) throws InvalidSessionException;

    /**
     * Returns users the file with given <var>fileId</var> has been shared with.
     * 
     * @throws InvalidSessionException
     */
    public User[] listUsersFileSharedWith(final String fileId) throws InvalidSessionException;

    /**
     * Creates a new <code>User</code> in Cifex with the given <var>password</var>. If
     * <var>registratorOrNull</var> is not <code>null</code>, it will be interpreted as the user
     * who creates the new user.
     * <p>
     * This method sends an email to the new user, to inform him about the new user account.
     * </p>
     */
    public void createUser(final User user, final String password, final User registratorOrNull,
            final String comment) throws EnvironmentFailureException, UserFailureException,
            InvalidSessionException, InsufficientPrivilegesException;

    /**
     * Update the fields of the user in the database.
     * 
     * @param sendUpdateInformationToUser Inform to user about the changes?
     */
    public void updateUser(final User user, final String password,
            final boolean sendUpdateInformationToUser) throws InvalidSessionException,
            InsufficientPrivilegesException, EnvironmentFailureException;

    /**
     * Revokes user with given userCode access to file with fileId.
     * 
     * @throws InvalidSessionException, InsufficientPrivilegesException
     */
    public void deleteSharingLink(String fileId, String userCode) throws InvalidSessionException,
            InsufficientPrivilegesException, FileNotFoundException;

    /**
     * Tries to delete the user given by its user <var>userCode</var>.
     * 
     * @throws UserNotFoundException if the user with the given <var>userCode</var> was not found.
     */
    public void deleteUser(final String userCode) throws InvalidSessionException,
            InsufficientPrivilegesException, UserNotFoundException;

    /**
     * List the files that have been uploaded for the currently logged in user.
     * <p>
     * Never returns <code>null</code> but could return an empty array.
     * </p>
     */
    public File[] listDownloadFiles() throws InvalidSessionException;

    /**
     * List the files uploaded by the currently logged user.
     * <p>
     * Never returns <code>null</code> but could return an empty array.
     * </p>
     */
    public File[] listUploadedFiles() throws InvalidSessionException;

    /**
     * List all files (only for admins).
     */
    public File[] listFiles() throws InvalidSessionException, InsufficientPrivilegesException;

    /**
     * Deletes file given by its <var>idStr</var>.
     * 
     * @throws FileNotFoundException If the file defined by <var>idStr</var> could not be found.
     */
    public void deleteFile(final String idStr) throws InvalidSessionException,
            InsufficientPrivilegesException, FileNotFoundException;

    /**
     * Registers the file names for the next upload request in the session.
     * 
     * @param filenamesForUpload the client absolute file paths. Can not be <code>null</code> or
     *            empty.
     */
    public void registerFilenamesForUpload(final String[] filenamesForUpload)
            throws InvalidSessionException;

    /**
     * Gets current file upload feedback.
     * <p>
     * Note that this method never returns <code>null</code> but waits till the first feedback is
     * in the queue.
     * </p>
     */
    public FileUploadFeedback getFileUploadFeedback() throws InvalidSessionException;

    /**
     * Update the Expiration Date of the file with the given <var>idStr</var>. Only an Admin can
     * set an own ExpirationDate, for all the others, the default expiration Date is used.
     * 
     * @param newExpirationDate The new Expiration date, can only used from an admin.
     */
    public void updateFileExpiration(final String idStr, final Date newExpirationDate)
            throws InvalidSessionException, InsufficientPrivilegesException;

    /**
     * Creates a sharing link between file and users.
     */
    public void createSharingLink(final String fileIdStr, final String emailsOfUsers)
            throws InvalidSessionException, InsufficientPrivilegesException, FileNotFoundException,
            UserFailureException;

}