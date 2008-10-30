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

package ch.systemsx.cisd.openbis.plugin;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ApplicationInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.User;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.UserFailureExceptionTranslator;
import ch.systemsx.cisd.openbis.generic.server.SessionConstants;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * An <i>abstract</i> {@link IClientService} implementation.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractClientService implements IClientService
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractClientService.class);

    @Resource(name = "request-context-provider")
    private IRequestContextProvider requestContextProvider;

    public AbstractClientService()
    {
    }

    private final SessionContext createSessionContext(final Session session)
    {
        final SessionContext sessionContext = new SessionContext();
        sessionContext.setSessionID(session.getSessionToken());
        final User user = new User();
        user.setUserName(session.getUserName());
        final PersonPE person = session.tryGetPerson();
        if (person != null)
        {
            final GroupPE homeGroup = person.getHomeGroup();
            if (homeGroup != null)
            {
                user.setHomeGroupCode(homeGroup.getCode());
            }
        }
        sessionContext.setUser(user);
        return sessionContext;
    }

    protected final String getSessionToken()
    {
        final HttpSession httpSession = getHttpSession();
        if (httpSession == null)
        {
            throw new InvalidSessionException("Session expired. Please login again.");
        }
        return getSession(httpSession).getSessionToken();
    }

    private final Session getSession(final HttpSession httpSession)
    {
        final Session session =
                (Session) httpSession.getAttribute(SessionConstants.OPENBIS_SESSION_ATTRIBUTE_KEY);
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

    private final HttpSession getHttpSession()
    {
        return getOrCreateHttpSession(false);
    }

    private final HttpSession creatHttpSession()
    {
        return getOrCreateHttpSession(true);
    }

    private final HttpSession getOrCreateHttpSession(final boolean create)
    {
        return requestContextProvider.getHttpServletRequest().getSession(create);
    }

    /** Returns the {@link IServer} implementation for this client service. */
    protected abstract IServer getServer();

    //
    // IClientService
    //

    public final ApplicationInfo getApplicationInfo()
    {
        final ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.setVersion(BuildAndEnvironmentInfo.INSTANCE.getFullVersion());
        return applicationInfo;
    }

    public final SessionContext tryToGetCurrentSessionContext()
    {
        final HttpSession httpSession = getHttpSession();
        if (httpSession == null)
        {
            return null;
        }
        final Session session = getSession(httpSession);
        return createSessionContext(session);
    }

    public final SessionContext tryToLogin(final String userID, final String password)
    {
        try
        {
            final Session session = getServer().tryToAuthenticate(userID, password);
            if (session == null)
            {
                return null;
            }
            final HttpSession httpSession = creatHttpSession();
            // Expiration time of httpSession is 10 seconds less than of session
            final int sessionExpirationTimeInMillis = session.getSessionExpirationTime();
            final int sessionExpirationTimeInSeconds = sessionExpirationTimeInMillis / 1000;
            if (sessionExpirationTimeInMillis < 0)
            {
                httpSession.setMaxInactiveInterval(-1);
            } else if (sessionExpirationTimeInMillis < 1000 || sessionExpirationTimeInSeconds < 10)
            {
                httpSession.setMaxInactiveInterval(0);
            } else
            {
                httpSession.setMaxInactiveInterval(sessionExpirationTimeInSeconds - 10);
            }
            httpSession.setAttribute(SessionConstants.OPENBIS_SESSION_ATTRIBUTE_KEY, session);
            httpSession.setAttribute(SessionConstants.OPENBIS_SERVER_ATTRIBUTE_KEY, getServer());
            return createSessionContext(session);
        } catch (final ch.systemsx.cisd.common.exceptions.UserFailureException e)
        {
            throw UserFailureExceptionTranslator.translate(e);
        } catch (final IllegalStateException e)
        {
            operationLog.error("Session already invalidated.", e);
            return null;
        }
    }

    public final void logout()
    {
        final HttpSession httpSession = getHttpSession();
        if (httpSession != null)
        {
            final Session session = getSession(httpSession);
            httpSession.removeAttribute(SessionConstants.OPENBIS_SESSION_ATTRIBUTE_KEY);
            httpSession.removeAttribute(SessionConstants.OPENBIS_SERVER_ATTRIBUTE_KEY);
            httpSession.invalidate();
            getServer().logout(session.getSessionToken());
        }
    }
}
