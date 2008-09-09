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

import ch.systemsx.cisd.authentication.DefaultSessionManager;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.servlet.RequestContextProviderAdapter;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class GenericServer implements IGenericServer
{
    private final ISessionManager<Session> sessionManager;

    public GenericServer(IAuthenticationService authenticationService,
            IRequestContextProvider requestContextProvider, int sessionExpirationPeriodInMinutes)
    {
        this(new DefaultSessionManager<Session>(new SessionFactory(),
                new LogMessagePrefixGenerator(), authenticationService,
                new RequestContextProviderAdapter(requestContextProvider),
                sessionExpirationPeriodInMinutes));
    }
    
    GenericServer(ISessionManager<Session> sessionManager)
    {
        this.sessionManager = sessionManager;
    }
    
    public int getVersion()
    {
        return 1;
    }

    public void logout(String sessionToken)
    {
        sessionManager.closeSession(sessionToken);
    }

    public Session tryToAuthenticate(String user, String password)
    {
        String sessionToken = sessionManager.tryToOpenSession(user, password);
        if (sessionToken == null)
        {
            return null;
        }
        return sessionManager.getSession(sessionToken);
    }

}
