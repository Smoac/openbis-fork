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

import java.util.List;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
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
     * Returns the user with the specified user code.
     * 
     * @return <code>null</code> if a user with the given <var>code</var> is not found.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public UserDTO tryFindUserByCode(String code);

    /**
     * Returns a list of all users.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public List<UserDTO> listUsers();

    /**
     * Returns a list of users, which where registered by the given user.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.TRACE)
    public List<UserDTO> listUsersRegisteredBy(final String userCode);

    /**
     * Creates the specified user in the database. As a side effect the unqiue ID of
     * <code>user</code> will be set.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public void createUser(UserDTO user);

    /**
     * Removes expired users from user base.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION, logLevel = LogLevel.DEBUG)
    public void deleteExpiredUsers();

    /**
     * Deletes the specified user.
     * 
     * @throws UserFailureException If the user with the given <var>userCode</var> was not found in
     *             the database.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public void deleteUser(String userCode) throws UserFailureException;

    /**
     * Updates the fields of the specified user.
     * 
     * @throws UserFailureException If the <var>user</var> was not found in the database.
     * @throws IllegalArgumentException If the <var>user</var> is regular in the database and now
     *             should be set temporary.
     */
    @LogAnnotation(logCategory = LogCategory.OPERATION)
    public void updateUser(UserDTO user, String encryptedPassword) throws UserFailureException,
            IllegalArgumentException;

}
