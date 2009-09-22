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

package ch.systemsx.cisd.openbis.generic.server;

import ch.systemsx.cisd.authentication.ISessionFactory;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * Factory of {@link Session} objects.
 * 
 * @author   Franz-Josef Elmer
 */
public final class SessionFactory implements ISessionFactory<Session>
{

    //
    // ISessionFactory
    //

    public final Session create(final String sessionToken, final String userName,
            final Principal principal, final String remoteHost, final long sessionStart,
            final int expirationTime)
    {
        return new Session(userName, sessionToken, principal, remoteHost, sessionStart,
                expirationTime);
    }

}
