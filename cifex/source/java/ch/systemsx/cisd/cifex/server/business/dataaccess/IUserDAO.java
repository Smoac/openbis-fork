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
     * @return <code>null</code>, if no user with that code exists.
     */
    public UserDTO tryFindUserByCode(String userCode) throws DataAccessException;

    /**
     * Finds the user with the given database id.
     * 
     * @param id The database id to look up the user for.
     * @return <code>null</code>, if no user with that code exists.
     */
    public UserDTO tryFindUserById(final long id) throws DataAccessException;
    
    /**
     * Returns the user code for the given <var>id</var>.
     * 
     * @return <code>null</code>, if no user with that <var>id</var> exists.
     */
    public String tryFindUserCodeById(long id) throws DataAccessException;
    
    /**
     * Returns a list with all users, which have the given email address.
     */
    public List<UserDTO> tryFindUserByEmail(final String email) throws DataAccessException;
    
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
     * @return The list of all users currently present in the database.
     */
    public List<UserDTO> listUsers() throws DataAccessException;

    /**
     * @return The list of users that have been registered by the user with given <var>userCode</var>.
     */
    public List<UserDTO> listUsersRegisteredBy(String userCode);

    /**
     * Deletes the user with the given <code>userId</code> from the database.
     * 
     * @return <code>true</code>, if the user was actually deleted and <code>false</code>, if
     *         no user was found with that id.
     */
    public boolean deleteUser(long userId) throws DataAccessException;

    /** Returns a list of expired users. */
    public List<UserDTO> listExpiredUsers();

    /** Update the fields of the given user. */
    public void updateUser(UserDTO user);

    /** Change user code. */
    public void changeUserCode(String before, String after);

    /**
     * Returns a list of users the file with given <var>fileId</var> has been shared with.
     */
    public List<UserDTO> listUsersFileSharedWith(long fileId) throws DataAccessException;
}
