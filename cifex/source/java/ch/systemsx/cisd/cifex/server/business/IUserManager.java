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
import ch.systemsx.cisd.common.logging.LogAnnotation;
import ch.systemsx.cisd.common.logging.LogCategory;

/**
 * @author Franz-Josef Elmer
 */
public interface IUserManager
{

    /**
     * Returns <code>true</code>, if there are not yet any users in the database.
     */
    public boolean isDatabaseEmpty();
    
    /**
     * Tries to find the user with the specified user code.
     * 
     * @return <code>null</code> if not found.
     */
    public UserDTO tryFindUserByCode(String code);

    /**
     * Creates the specified user in the database. As a side effect the unqiue ID of <code>user</code> will be set.
     */
    @LogAnnotation(logCategory = LogCategory.TRACKING)
    public void createUser(UserDTO user);

    /**
     * Returns a list of all users.
     */
    public List<UserDTO> listUsers();

    /** Removes expired users from user base */
    @LogAnnotation(logCategory = LogCategory.TRACKING)
    public void deleteExpiredUsers();

    /**
     * Deletes the specified user.
     */
    @LogAnnotation(logCategory = LogCategory.TRACKING)
    public void deleteUser(String code);
    
    /**
     * Updates the fields of the specified user.
     */
    @LogAnnotation(logCategory = LogCategory.TRACKING)
    public void updateUser(UserDTO user, String encryptedPassword);
}
