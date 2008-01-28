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
     * Finds with the email of the user the specified user id.
     * 
     * @param email EmailAdress. Can not be blank.
     * @return <code>null</code>, if no user with that id exists.
     */
    public UserDTO tryFindUserByEmail(String email) throws DataAccessException;

    /**
     * Inserts given <code>User</code> into the database.
     * <p>
     * As side effect the <i>unique identifier</i> returned by the database is set to given <code>User</code> object
     * using {@link UserDTO#setID(Long)}.
     * </p>
     * 
     * @param user <code>User</code> object to be inserted into the database. Can not be <code>null</code>.
     */
    public void createUser(UserDTO user) throws DataAccessException;

    /**
     * @returns The list of all users currently present in the database.
     */
    public List<UserDTO> listUsers() throws DataAccessException;

    /**
     * Deletes the given <code>User</code> from the Database.
     */
    public boolean removeUser(Long userID) throws DataAccessException;

    /** Removes expired users from database */
    public void deleteExpiredUsers();
}
