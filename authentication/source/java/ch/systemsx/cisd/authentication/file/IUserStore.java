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

package ch.systemsx.cisd.authentication.file;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * An abstraction of a store for {@link UserEntry}s.
 *
 * @author Bernd Rinn
 */
interface IUserStore extends ISelfTestable
{
    /**
     * Returns the unique identifier of the store.
     */
    String getId();

    /**
     * Returns <code>true</code>, if the password file backing this class exists.
     */
    boolean exists();
    
    /**
     * Returns the {@link UserEntry} of <var>user</var>, or <code>null</code>, if this user does
     * not exist.
     */
    UserEntry tryGetUser(String user) throws EnvironmentFailureException;
    
    /**
     * Adds the <var>user</var> if it exists, otherwise updates (replaces) the entry with the given
     * entry.
     */
    void addOrUpdateUser(UserEntry user) throws EnvironmentFailureException;

    /**
     * Removes the user with id <var>userId</var> if it exists.
     * 
     * @return <code>true</code>, if the user has been removed.
     */
    boolean removeUser(String userId) throws EnvironmentFailureException;
    
    /**
     * Returns <code>true</code>, if <var>user</var> is known and has the given <var>password</var>.
     */
    boolean isPasswordCorrect(String user, String password) throws EnvironmentFailureException;
    
    /**
     * Returns a list of all users currently found in the password file.
     */
    List<UserEntry> listUsers() throws EnvironmentFailureException;

}
