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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.lims.base.dto.GroupPE;
import ch.systemsx.cisd.lims.base.dto.PersonPE;
import ch.systemsx.cisd.lims.base.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.lims.webclient.client.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Group;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.GroupTranslater;
import ch.systemsx.cisd.openbis.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class GenericClientService implements IGenericClientService
{
    static final String SESSION_KEY = "openbis-session";
    static final String SERVER_KEY = "openbis-generic-server";

    private static final Logger operationLog =
        LogFactory.getLogger(LogCategory.OPERATION, GenericClientService.class);

    private final IGenericServer server;
    private final IRequestContextProvider requestContextProvider;

    public GenericClientService(final IGenericServer server,
            final IRequestContextProvider requestContextProvider)
    {
        this.server = server;
        this.requestContextProvider = requestContextProvider;
    }
    
    void setConfigParameters(GenericConfigParameters configParameters)
    {
    }
    
    private SessionContext createSessionContext(final Session session)
    {
        SessionContext sessionContext = new SessionContext();
        sessionContext.setSessionID(session.getSessionToken());
        User user = new User();
        user.setUserName(session.getUserName());
        PersonPE person = session.tryToGetPerson();
        if (person != null)
        {
            GroupPE homeGroup = person.getHomeGroup();
            if (homeGroup != null)
            {
                user.setHomeGroupCode(homeGroup.getCode());
            }
        }
        sessionContext.setUser(user);
        return sessionContext;
    }
    
    private String getSessionToken()
    {
        HttpSession httpSession = getHttpSession();
        if (httpSession == null)
        {
            throw new InvalidSessionException("Session expired. Please login again.");
        }
        return getSession(httpSession).getSessionToken();
    }
    
    private Session getSession(final HttpSession httpSession)
    {
        Session session = (Session) httpSession.getAttribute(SESSION_KEY);
        if (session == null)
        {
            final String remoteHost =
                    requestContextProvider.getHttpServletRequest().getRemoteHost();
            final String msg =
                    "Attempt to get non-existent session from host '" + remoteHost
                            + "': user is not logged in.";
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(msg);
            }
            throw new InvalidSessionException(msg);

        }
        return session;
    }
    
    private HttpSession getHttpSession()
    {
        return getOrCreateHttpSession(false);
    }
    
    private HttpSession creatHttpSession()
    {
        return getOrCreateHttpSession(true);
    }
    
    private HttpSession getOrCreateHttpSession(boolean create)
    {
        return requestContextProvider.getHttpServletRequest().getSession(create);
    }
    
    /**
     * Converts any {@link ch.systemsx.cisd.common.exceptions.UserFailureException} or subclass of
     * it to a <i>GWT</i> {@link UserFailureException} (or subclass of it if this one could be
     * found in the <code>ch.systemsx.cisd.lims.webclient.client</code> package).
     */
    private final static ch.systemsx.cisd.openbis.generic.client.web.client.application.util.UserFailureException convertException(
            final ch.systemsx.cisd.common.exceptions.UserFailureException ex)
    {
        final String className = WEB_CLIENT_EXCEPTIONS_PACKAGE + ex.getClass().getSimpleName();
        try
        {
            return ClassUtils.create(UserFailureException.class, className, ex.getMessage());
        } catch (final CheckedExceptionTunnel e)
        {
            return new UserFailureException(ex.getMessage());
        }
    }

    public ApplicationInfo getApplicationInfo()
    {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.setVersion(BuildAndEnvironmentInfo.INSTANCE.getFullVersion());
        return applicationInfo;
    }

    public SessionContext tryToGetCurrentSessionContext()
    {
        final HttpSession httpSession = getHttpSession();
        if (httpSession == null)
        {
            return null;
        }
        final Session session = getSession(httpSession);
        return createSessionContext(session);
    }

    public SessionContext tryToLogin(String userID, String password)
    {
        Session session = server.tryToAuthenticate(userID, password);
        if (session == null)
        {
            return null;
        }
        HttpSession httpSession = creatHttpSession();
        // Expiration time of httpSession is 10 seconds less than of session
        httpSession.setMaxInactiveInterval(session.getSessionExpirationTime() / 1000 - 10);
        httpSession.setAttribute(SESSION_KEY, session);
        httpSession.setAttribute(SERVER_KEY, server);
        return createSessionContext(session);
    }

    public void logout()
    {
        HttpSession httpSession = getHttpSession();
        if (httpSession != null)
        {
            Session session = getSession(httpSession);
            httpSession.removeAttribute(SESSION_KEY);
            httpSession.removeAttribute(SERVER_KEY);
            httpSession.invalidate();
            server.logout(session.getSessionToken());
        }
    }

    public List<Group> listGroups(String databaseInstanceCode)
    {
        DatabaseInstanceIdentifier identifier = new DatabaseInstanceIdentifier(databaseInstanceCode);
        List<Group> result = new ArrayList<Group>();
        List<GroupPE> groups = server.listGroups(getSessionToken(), identifier);
        for (GroupPE group : groups)
        {
            result.add(GroupTranslater.translate(group));
        }
        return result;
    }

    public void registerGroup(String groupCode, String descriptionOrNull, String groupLeaderOrNull)
    {
        server.registerGroup(getSessionToken(), groupCode, descriptionOrNull, groupLeaderOrNull);
    }

}
