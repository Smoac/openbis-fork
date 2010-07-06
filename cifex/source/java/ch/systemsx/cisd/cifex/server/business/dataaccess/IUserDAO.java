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

package ch.systemsx.cisd.cifex.server.business.dataaccess;

import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * <i>Data Access Object</i> for Users.
 * 
 * @author Basil Neff
 */
public interface IUserDAO
{

    /**
     * Finds the user with the given user code.
     * 
     * @param userCode user code. Can not be blank.
     * @return <code>null</code>, if no user with that id exists.
     */
    public UserDTO tryFindUserByCode(String userCode) throws DataAccessException;

    /**
     * Returns the user with the given database <var>id</var>.
     * 
     * @param id The database id to look up the user for.
     */
    public UserDTO getUserById(final long id) throws DataAccessException;

    /**
     * Returns <code>true</code> if and only if the user with given <var>id</var> has currently
     * files to download.
     */
    public boolean hasUserFilesForDownload(final long id);

    /**
     * Returns the user code for the given <var>id</var>.
     * 
     * @return <code>null</code>, if no user with that <var>id</var> exists.
     */
    public String tryFindUserCodeById(long id) throws DataAccessException;

    /**
     * Returns <code>true</code> if the database has a user with <var>code</var>.
     */
    public boolean hasUserCode(final String code) throws DataAccessException;

    /**
     * Returns a list with all users, which have the given email address.
     */
    public List<UserDTO> findUserByEmail(final String email) throws DataAccessException;

    /**
     * Inserts given <code>User</code> into the database.
     * <p>
     * As side effect the <i>unique identifier</i> returned by the database is set to given
     * <code>User</code> object using {@link UserDTO#setID(Long)}.
     * </p>
     * 
     * @param user <code>User</code> object to be inserted into the database. Can not be
     *            <code>null</code>.
     */
    public void createUser(UserDTO user) throws DataAccessException;

    /**
     * @return The number of users in the database.
     */
    public int getNumberOfUsers() throws DataAccessException;

    /**
     * Returns <code>true</code> if the given <var>user</var> is the "main user" of the quota group
     * the user is in.
     * <p>
     * <i>Note: A user is called "main user" of his quota group, if all other users in this group
     * have been registered by him.</i>
     */
    public boolean isMainUserOfQuotaGroup(UserDTO user) throws DataAccessException;

    /**
     * Fills all registrators of all <var>users</var> that are contained in the given list of users.
     * <p>
     * Note that only if <var>users</var> is the complete list of users all registrators will have
     * been filled.
     */
    public void fillInRegistrators(final List<UserDTO> users);

    /**
     * @return The list of all users currently present in the database.
     */
    public List<UserDTO> listUsers() throws DataAccessException;

    /**
     * Lists all users which have an id which is contained in <var>userIds</var>.
     * <p>
     * Note that user ids in the given list that are not found in the database are no error but
     * lead to fewer entries in the resulting list.
     * <p>
     * Note that {@link UserDTO#getRegistrator()} of all entries of the returned list will provide a
     * minimal object that has only the user id filled.
     * 
     * @see #fillInRegistrators(List)
     * @return The list of all users with one of the given <var>userIds</var>.
     */
    public List<UserDTO> listUsersById(long... userIds) throws DataAccessException;

    /**
     * Lists all users which have a user code which is contained in <var>userCodes</var>.
     * <p>
     * Note that user codes in the given list that are not found in the database are no error but
     * lead to fewer entries in the resulting list.
     * <p>
     * Note that {@link UserDTO#getRegistrator()} of all entries of the returned list will provide a
     * minimal object that has only the user id filled.
     * 
     * @see #fillInRegistrators(List)
     * @return The list of all users with one of the given <var>userCodes</var>.
     */
    public List<UserDTO> listUsersByCode(String... userCodes) throws DataAccessException;

    /**
     * Lists all users which have an email address which is contained in <var>emailAddresses</var>.
     * <p>
     * Note that email addresses in the given list that are not found in the database are no error
     * but lead to fewer entries in the resulting list.
     * <p>
     * Note that {@link UserDTO#getRegistrator()} of all entries of the returned list will provide a
     * minimal object that has only the user id filled.
     * 
     * @see #fillInRegistrators(List)
     * @return The list of all users with one of the given <var>emailAddresses</var>.
     */
    public List<UserDTO> listUsersByEmail(String... emailAddresses) throws DataAccessException;

    /**
     * @return The list of users that have been registered by the user with given <var>userId</var>.
     */
    public List<UserDTO> listUsersRegisteredBy(long userId);

    /**
     * Returns a list of users the file with given <var>fileId</var> has been shared with.
     */
    public List<UserDTO> listUsersFileSharedWith(long fileId) throws DataAccessException;

    /**
     * Refreshes the quota information (and the file retention time) from the database.
     */
    public void refreshQuotaInformation(UserDTO user) throws DataAccessException;

    /**
     * Deletes the given <var>user</var> from the database. The <var>requestUserIdOrNull</var> is
     * the id of the user who requests the deletion, if any. It will be <code>null</code> for any
     * expired user that is deleted by a backend process. The user denoted by
     * <var>requestUserIdOrNull</var> will be the new owner of all files and users that the user to
     * be deleted owns, <i>if</i> the user to be deleted has no registrator.
     * 
     * @return <code>true</code>, if the user was actually deleted and <code>false</code>, if the
     *         <var>user</var> was not found in the database.
     */
    public boolean deleteUser(UserDTO user, Long requestUserIdOrNull) throws DataAccessException;

    /** Returns a list of expired users. */
    public List<UserDTO> listExpiredUsers();

    /** Update the fields of the given user. */
    public void updateUser(UserDTO user);

    /** Change user code. */
    public void changeUserCode(String before, String after);

}
