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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
            final IUserActionLog userBehaviorLogOrNull, final String testingFlag,
            final long cleaningTimeInterval, final long sessionExpirationPeriodMinutes)
    {
        this.requestContextProviderOrNull = requestContextProvider;
        this.userBehaviorLogOrNull = userBehaviorLogOrNull;
        this.testMode = "true".equals(testingFlag);
        if (testMode == false)
        {
            startSessionExpirationTimer(cleaningTimeInterval, sessionExpirationPeriodMinutes);
        }
    }

    private void startSessionExpirationTimer(final long cleaningTimeInterval,
            final long sessionExpirationPeriodMinutes)
    {
        final Timer timer = new Timer("Session Expiration", true);
        final long sessionExpirationPeriodMillis = 60 * 1000 * sessionExpirationPeriodMinutes;
        timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    final long now = System.currentTimeMillis();
                    synchronized (this)
                    {
                        for (Session session : sessions.values())
                        {
                            if (now - session.getLastActiveMillis() > sessionExpirationPeriodMillis)
                            {
                                removeSession(session.getSessionID(), true);
                            }
                        }
                    }
                }
            }, 0L, cleaningTimeInterval);
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
     * @throws EnvironmentFailureException if no session could be found.
     */
    public synchronized Session getSession(String sessionID) throws InvalidSessionException
    {
        Session session = sessions.get(sessionID);
        if (session == null)
        {
            throw new InvalidSessionException("No session found for ID " + sessionID);
        }
        session.touchSession();
        storeRPCSessionInHTTPSession(session);
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
