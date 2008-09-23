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
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.authentication.DefaultSessionManager;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.servlet.RequestContextProviderAdapter;
import ch.systemsx.cisd.common.spring.IInvocationLoggerFactory;
import ch.systemsx.cisd.openbis.generic.server.business.GenericManagers;
import ch.systemsx.cisd.openbis.generic.server.business.bo.GenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGenericBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IGroupBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.ISessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;

/**
 * Implementation of client-server interface.
 * 
 * @author Franz-Josef Elmer
 */
public class GenericServer implements IGenericServer, ISessionProvider,
        IInvocationLoggerFactory<IGenericServer>
{
    private final ISessionManager<Session> sessionManager;

    private final GenericManagers managers;

    private final IAuthorizationDAOFactory daoFactory;

    private final IGenericBusinessObjectFactory boFactory;

    public GenericServer(IAuthenticationService authenticationService,
            IRequestContextProvider requestContextProvider, IAuthorizationDAOFactory daoFactory,
            int sessionExpirationPeriodInMinutes, BeanPostProcessor processor)
    {
        this.daoFactory = daoFactory;
        this.sessionManager =
                new DefaultSessionManager<Session>(new SessionFactory(),
                        new LogMessagePrefixGenerator(), authenticationService,
                        new RequestContextProviderAdapter(requestContextProvider),
                        sessionExpirationPeriodInMinutes);
        boFactory = new GenericBusinessObjectFactory(daoFactory);
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

    @Transactional
    public List<GroupPE> listGroups(String sessionToken, DatabaseInstanceIdentifier identifier)
    {
        Session session = sessionManager.getSession(sessionToken);
        DatabaseInstancePE databaseInstance = GroupIdentifierHelper.getDatabaseInstance(identifier, daoFactory);
        List<GroupPE> groups = daoFactory.getGroupDAO().listGroups(databaseInstance);
        Long homeGroupID = session.tryGetHomeGroupId();
        for (final GroupPE group : groups)
        {
            group.setHome(homeGroupID != null && homeGroupID.equals(group.getId()));
        }
        return groups;
    }

    @Transactional
    public void registerGroup(String sessionToken, String groupCode, String descriptionOrNull,
            String groupLeaderOrNull)
    {
        Session session = sessionManager.getSession(sessionToken);
        IGroupBO groupBO = boFactory.createGroupBO(session);
        groupBO.define(groupCode, descriptionOrNull, groupLeaderOrNull);
        groupBO.save();
    }

    @Transactional
    public List<PersonPE> listPersons(String sessionToken)
    {
        Session session = sessionManager.getSession(sessionToken);
        return managers.getPersonManager().listPersons(session);
    }

    @Transactional
    public void registerPerson(String sessionToken, String code)
    {
        Session session = sessionManager.getSession(sessionToken);
        managers.getPersonManager().registerPerson(session, code);
    }

}
