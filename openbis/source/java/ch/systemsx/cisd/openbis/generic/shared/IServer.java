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

package ch.systemsx.cisd.openbis.generic.shared;

import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.openbis.generic.shared.authorization.ISessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * An basic server.
 * 
 * @author Christian Ribeaud
 */
public interface IServer extends ISessionProvider
{

    /**
     * Returns the version of this interface.
     */
    public int getVersion();

    /**
     * Tries to authenticate the specified user with given password.
     * 
     * @return <code>null</code> if authentication failed.
     */
    @Transactional
    public Session tryToAuthenticate(String user, String password);

    /**
     * Logout the session with the specified session token.
     */
    public void logout(String sessionToken);

}