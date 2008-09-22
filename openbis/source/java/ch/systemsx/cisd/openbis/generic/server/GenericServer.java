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

import java.util.List;

import org.springframework.beans.factory.config.BeanPostProcessor;

import ch.systemsx.cisd.authentication.DefaultSessionManager;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.servlet.RequestContextProviderAdapter;
import ch.systemsx.cisd.common.spring.IInvocationLoggerFactory;
import ch.systemsx.cisd.openbis.generic.server.business.GenericManagers;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.ISessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;

/**
 * Implementation of client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
public class GenericServer implements IGenericServer, ISessionProvider, IInvocationLoggerFactory<IGenericServer>
{
    private final ISessionManager<Session> sessionManager;

    private final GenericManagers managers;

    public GenericServer(IAuthenticationService authenticationService,
            IRequestContextProvider requestContextProvider, IAuthorizationDAOFactory daoFactory,
            int sessionExpirationPeriodInMinutes, BeanPostProcessor processor)
    {
        this.sessionManager =
                new DefaultSessionManager<Session>(new SessionFactory(),
                        new LogMessagePrefixGenerator(), authenticationService,
                        new RequestContextProviderAdapter(requestContextProvider),
                        sessionExpirationPeriodInMinutes);
        this.managers = new GenericManagers(daoFactory, processor);
    }

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    public GenericServerLogger createLogger(boolean invocationSuccessful)
    {
        return new GenericServerLogger(sessionManager, invocationSuccessful);
    }

    public IAuthSession getSession(String sessionToken)
    {
        return sessionManager.getSession(sessionToken);
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
        Session session = sessionManager.getSession(sessionToken);
        managers.getPersonManager().registerPersonIfNecessary(session);
        return session;
    }

    public List<GroupPE> listGroups(String sessionToken, DatabaseInstanceIdentifier identifier)
    {
        Session session = sessionManager.getSession(sessionToken);
        return managers.getGroupManager().listGroups(session, identifier);
    }

    public void registerGroup(String sessionToken, String groupCode, String descriptionOrNull,
            String groupLeaderOrNull)
    {
        Session session = sessionManager.getSession(sessionToken);
        GroupIdentifier groupIdentifier =
                new GroupIdentifier(DatabaseInstanceIdentifier.HOME, groupCode);
        managers.getGroupManager().registerGroup(session, groupIdentifier, descriptionOrNull,
                groupLeaderOrNull);
    }
}
