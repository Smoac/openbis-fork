/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpSession;

import ch.systemsx.cisd.cifex.server.business.IUserActionLog;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.servlet.IActionLog.LogoutReason;
import ch.systemsx.cisd.common.utilities.TokenGenerator;

/**
 * Manager of {@link Session} instances.
 * 
 * @author Franz-Josef Elmer
 */
public class SessionManager
{
    private final boolean testMode;

    private final TokenGenerator tokenGenerator = new TokenGenerator();

    private final Map<String, Session> sessions = new HashMap<String, Session>();

    private final IRequestContextProvider requestContextProviderOrNull;

    private final IUserActionLog userBehaviorLogOrNull;

    private int nextID;

    public SessionManager(final IRequestContextProvider requestContextProvider,
            final IUserActionLog userBehaviorLogOrNull, final String testingFlag)
    {
        this.requestContextProviderOrNull = requestContextProvider;
        this.userBehaviorLogOrNull = userBehaviorLogOrNull;
        this.testMode = "true".equals(testingFlag);
    }

    /**
     * Returns all sessions currently in the session manager.
     * <p>
     * <b>Note:</b> The caller is expected to hold the synchronization monitor of this class when
     * calling this method and processing the sessions.
     */
    Collection<Session> getAllSessions()
    {
        return sessions.values();
    }

    /**
     * Creates a new session for the specified user and URL.
     */
    public synchronized Session createSession(UserDTO user, String url)
    {
        String sessionID =
                (testMode ? Integer.toString(nextID++) : tokenGenerator.getNewToken(System
                        .currentTimeMillis()));
        Session session = new Session(sessionID, user, url);
        sessions.put(sessionID, session);
        storeRPCSessionInHTTPSession(session);
        if (userBehaviorLogOrNull != null)
        {
            userBehaviorLogOrNull.logSuccessfulLogin();
        }
        return session;
    }

    /**
     * Retrieves the session for the specified ID.
     * 
     * @param sessionID The id of the session to get.
     * @throws EnvironmentFailureException if no session could be found.
     */
    public synchronized Session getSession(String sessionID) throws InvalidSessionException
    {
        return getSession(sessionID, true);
    }

    /**
     * Retrieves the session for the specified ID.
     * 
     * @param sessionID The id of the session to get.
     * @param storeInHTTPSession If <code>true</code>, the session object is stored in the HTTP
     *            session. Only call it like this when you are in a web requests and in the
     *            DispatcherServlet thread!
     * @throws EnvironmentFailureException if no session could be found.
     */
    synchronized Session getSession(String sessionID, boolean storeInHTTPSession)
            throws InvalidSessionException
    {
        Session session = sessions.get(sessionID);
        if (session == null)
        {
            throw new InvalidSessionException("No session found for ID " + sessionID);
        }
        session.touchSession();
        if (storeInHTTPSession)
        {
            storeRPCSessionInHTTPSession(session);
        }
        return session;
    }

    /**
     * Removes the session for the specified ID.
     */
    public synchronized void removeSession(String sessionID, boolean timedOut)
    {
        final Session removedSession = sessions.remove(sessionID);
        if (userBehaviorLogOrNull != null && removedSession != null)
        {
            userBehaviorLogOrNull.logLogout(removedSession, timedOut ? LogoutReason.SESSION_TIMEOUT
                    : LogoutReason.SESSION_LOGOUT);
        }
    }

    /**
     * Removes the session for the specified user.
     */
    public synchronized void removeSessionsForUser(String userCode)
    {
        final Iterator<Session> sessionIt = sessions.values().iterator();
        while (sessionIt.hasNext())
        {
            final Session session = sessionIt.next();
            if (userCode.equals(session.getUser().getUserCode()))
            {
                sessionIt.remove();
                if (userBehaviorLogOrNull != null)
                {
                    userBehaviorLogOrNull.logLogout(session, LogoutReason.USER_DELETED);
                }
            }
        }
    }

    private void storeRPCSessionInHTTPSession(Session session)
    {
        if (requestContextProviderOrNull != null)
        {
            final HttpSession httpSession =
                    requestContextProviderOrNull.getHttpServletRequest().getSession();
            httpSession.setAttribute(CIFEXRPCService.SESSION_ATTRIBUTE_RPC_SESSION, session);
        }
    }

}
