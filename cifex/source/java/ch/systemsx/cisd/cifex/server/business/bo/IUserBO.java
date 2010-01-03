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

package ch.systemsx.cisd.cifex.server.business.bo;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.common.Password;

/**
 * Contains the logic of creating and updating users.
 * 
 * @author Franz-Josef Elmer
 * @author Bernd Rinn
 */
public interface IUserBO
{
    /**
     * Defines the new <var>user</var>, enforcing basic business rules.
     * 
     * @param user The user to be defined for saving.
     * @param requestUserOrNull The user requesting creation of the new user
     * @param forceTemporaryUser Force the new user to be temporary, even if the request user would
     *            be allowed to create permanent users.
     */
    public void defineForCreate(UserDTO user, UserDTO requestUserOrNull, boolean forceTemporaryUser);

    /**
     * Defines the existing <var>user</var> for update, enforcing basic business rules. Note that
     * {@link UserDTO#getRegistrator()} of <var>user</var> needs to be set correctly in order for
     * all business rules being enforced.
     * 
     * @param oldUserToUpdateOrNull The old user information, if available. Otherwise, the old user
     *            information will be obtained from the database
     * @param newUserToUpdate The new user information
     * @param passwordOrNull The new password if any
     * @param requestUserOrNull The user that requests the update, for enforcing business rules
     */
    public void defineForUpdate(UserDTO oldUserToUpdateOrNull, UserDTO newUserToUpdate,
            Password passwordOrNull, UserDTO requestUserOrNull);

    /**
     * Returns the user information. This method is only available after calling
     * {@link #defineForUpdate(UserDTO, UserDTO, Password, UserDTO)} or
     * {@link #defineForCreate(UserDTO, UserDTO, boolean)}.
     * 
     * @throws IllegalStateException If
     *             {@link #defineForUpdate(UserDTO, UserDTO, Password, UserDTO)} has not been
     *             called.
     */
    public UserDTO getUser() throws IllegalStateException;

    /**
     * Returns the old user information (before the update). This method is only available after
     * calling {@link #defineForUpdate(UserDTO, UserDTO, Password, UserDTO)}.
     * 
     * @throws IllegalStateException If
     *             {@link #defineForUpdate(UserDTO, UserDTO, Password, UserDTO)} has not been
     *             called.
     */
    public UserDTO getOldUser() throws IllegalStateException;

    /**
     * Saves the user information to the database.
     */
    public void save();
}
