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

package ch.systemsx.cisd.cifex.server.business;

import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.common.Password;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogAnnotation;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * @author Franz-Josef Elmer
 */
public interface IUserManager
{

    /**
     * Returns <code>true</code>, if there are not yet any users in the database.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public boolean isDatabaseEmpty();

    /**
     * Returns the user with the specified <var>id</var>.
     * 
     * @return <code>null</code> if a user with the given <var>code</var> is not found.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public UserDTO getUser(long id);

    /**
     * Returns the user with the specified user code.
     * 
     * @return <code>null</code> if a user with the given <var>code</var> is not found.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public UserDTO tryFindUserByCode(String code);

    /**
     * Returns a list with all users, which have the given email address.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public List<UserDTO> findUserByEmail(final String email);

    /**
     * Returns a list of all users.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public List<UserDTO> listUsers();

    /**
     * Returns a list of users which where registered by the given user.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public List<UserDTO> listUsersRegisteredBy(final long userId);

    /**
     * Refreshes the quota information for the given <var>user</var>
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public void refreshQuotaInformation(UserDTO user);

    /**
     * Returns <code>true</code> if and only if the <var>user</var> has currently some files to
     * download.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public boolean hasUserFilesForDownload(UserDTO user);

    /**
     * Creates the specified <var>user</var> in the database. The password and the registrator need
     * already be set if this is desired.
     * <p>
     * As a side effect the unique ID of <code>user</code> will be set.
     * 
     * @param registratorOrNull The user who registers this user or <code>null</code> if this user
     *            gets created by an automatic process.
     * @throws UserFailureException If a user with that id already exists in the database
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public UserDTO createUser(UserDTO user, UserDTO registratorOrNull) throws UserFailureException;

    /**
     * Creates the specified user in the database and sends an email to the new user with the
     * credentials. As a side effect the unqiue ID , the password and the registrator of
     * <code>user</code>will be set.
     * 
     * @param user The information about the user to create.
     * @param password The password of the user, or a blank String, if a password should be created.
     * @param registrator The information about the user who creates this user.
     * @param basicURL The basic URL of the request, to be used for email creation.
     * @throws UserFailureException If a user with that id already exists in the database
     * @throws EnvironmentFailureException If the email to the new user could not be sent.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public UserDTO createUserAndSendEmail(UserDTO user, String password, UserDTO registrator,
            String comment, String basicURL) throws UserFailureException,
            EnvironmentFailureException;

    /**
     * Removes expired users from user base.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.DEBUG)
    public void deleteExpiredUsers(final IUserActionLog logOrNull);

    /**
     * Deletes the specified user.
     * 
     * @throws IllegalArgumentException If the user with the given <var>id</var> was not found in
     *             the database.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public void deleteUser(long id, UserDTO requestUser, final IUserActionLog logOrNull)
            throws IllegalArgumentException;

    /**
     * Updates the fields of the specified user, providing the old user for comparison. Compared to
     * {@link #updateUser(UserDTO, Password, UserDTO, IUserActionLog)}, this avoids getting the old
     * user from the database.
     * 
     * @return The updated user information.
     * @throws UserFailureException If the <var>user</var> was not found in the database.
     * @throws IllegalArgumentException If the <var>user</var> is regular in the database and now
     *             should be set temporary.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public UserDTO updateUser(UserDTO oldUser, UserDTO user, Password passwordOrNull,
            final UserDTO requestUser, final IUserActionLog logOrNull) throws UserFailureException,
            IllegalArgumentException;

    /**
     * Updates the fields of the specified user.
     * 
     * @return The updated user information.
     * @throws UserFailureException If the <var>user</var> was not found in the database.
     * @throws IllegalArgumentException If the <var>user</var> is regular in the database and now
     *             should be set temporary.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public UserDTO updateUser(UserDTO user, Password passwordOrNull,
            final UserDTO requestUserOrNull, final IUserActionLog logOrNull)
            throws UserFailureException, IllegalArgumentException;

    /**
     * Changes the user code.
     * 
     * @throws UserFailureException If the user with code <var>before</var> was not found in the
     *             database, is externally authenticated or user with code <var>after</var> already
     *             exists.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public void changeUserCode(String before, String after) throws UserFailureException;

    /**
     * Returns a list of users the file with given <var>fileId</var> has been shared with.
     * 
     * @throws UserFailureException If the <var>fileId</var> was not found in the database.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public List<UserDTO> listUsersFileSharedWith(long fileId) throws UserFailureException;

    /**
     * Returns a list of all known users which either have a user code in <var>userCodesOrNull</var>
     * or an email that is in <var>emailsOrNull</var>. If CIFEX is configured to use an external
     * authentication service, this users will query the external authentication service for users
     * matching these conditions and create the users in the internal CIFEX user database if so.
     * 
     * @param userCodesOrNull list of user codes to get the users for or <code>null</code> if it
     *            should be ignored
     * @param emailsOrNull list of email addresses to get the users for or <code>null</code> if it
     *            should be ignored
     * @return The list of known users matching the given criteria
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public Collection<UserDTO> getUsers(List<String> userCodesOrNull, List<String> emailsOrNull,
            final IUserActionLog logOrNull);

}
