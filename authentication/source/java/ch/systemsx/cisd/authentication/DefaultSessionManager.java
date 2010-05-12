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

package ch.systemsx.cisd.authentication;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.server.IRemoteHostProvider;
import ch.systemsx.cisd.common.utilities.TokenGenerator;

/**
 * Default session manager. Needs
 * <ul>
 * <li>a {@link ISessionFactory} for creating new session objects,
 * <li>a {@link ILogMessagePrefixGenerator} for generating log messages which are logged by a logger
 * with category {@link LogCategory#AUTH},
 * <li>a {@link IAuthenticationService} for authenticating users,
 * <li>a {@link IRemoteHostProvider} for providing the remote host of the user client.
 * </ul>
 * 
 * @author Franz-Josef Elmer
 */
public class DefaultSessionManager<T extends BasicSession> implements ISessionManager<T>
{
    private static final String LOGOUT_PREFIX = "LOGOUT: ";

    private static final String LOGIN_PREFIX = "LOGIN: ";

    private static final char SESSION_TOKEN_SEPARATOR = '-';

    // should be different than SESSION_TOKEN_SEPARATOR
    private static final char TIMESTAMP_TOKEN_SEPARATOR = 'x';

    private static final Logger authenticationLog =
            LogFactory.getLogger(LogCategory.AUTH, DefaultSessionManager.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DefaultSessionManager.class);

    private static final TokenGenerator tokenGenerator = new TokenGenerator();

    private static final class FullSession<S extends BasicSession>
    {
        /** Session data. */
        private final S session;

        /** The last time when this session has been used (in milliseconds since 1970-01-01). */
        private long lastActiveTime;

        FullSession(final S session)
        {
            assert session != null : "Undefined session";

            this.session = session;
            touch();
        }

        /**
         * Returns the session.
         */
        public S getSession()
        {
            return session;
        }

        /**
         * Sets the time of last activity (used to determine whether the session
         * {@link #hasExpired()}.
         */
        void touch()
        {
            this.lastActiveTime = System.currentTimeMillis();
        }

        /**
         * Returns <code>true</code> if the session has expired.
         */
        boolean hasExpired()
        {
            return System.currentTimeMillis() - lastActiveTime > session.getSessionExpirationTime();
        }
    }

    private final ISessionFactory<T> sessionFactory;

    private final ILogMessagePrefixGenerator<T> prefixGenerator;

    /**
     * The map of session tokens to sessions. Access to this data structure needs to be
     * synchronized.
     */
    private final Map<String, FullSession<T>> sessions =
            new LinkedHashMap<String, FullSession<T>>();

    private final IAuthenticationService authenticationService;

    private final IRemoteHostProvider remoteHostProvider;

    /** The time after which an inactive session will be expired (in milliseconds). */
    private final int sessionExpirationPeriodMillis;

    public DefaultSessionManager(final ISessionFactory<T> sessionFactory,
            final ILogMessagePrefixGenerator<T> prefixGenerator,
            final IAuthenticationService authenticationService,
            final IRemoteHostProvider remoteHostProvider, final int sessionExpirationPeriodMinutes)
    {
        assert sessionFactory != null : "Missing session factory.";
        assert prefixGenerator != null : "Missing prefix generator";
        assert authenticationService != null : "Missing authentication service.";
        assert remoteHostProvider != null : "Missing remote host provider.";
        assert sessionExpirationPeriodMinutes >= 0 : "Session experation time has to be a positive value: "
                + sessionExpirationPeriodMinutes; // == 0 is for unit test

        this.sessionFactory = sessionFactory;
        this.prefixGenerator = prefixGenerator;
        this.authenticationService = authenticationService;
        this.remoteHostProvider = remoteHostProvider;
        sessionExpirationPeriodMillis =
                (int) (sessionExpirationPeriodMinutes * DateUtils.MILLIS_PER_MINUTE);

        operationLog.info(String.format("Authentication service: '%s'", authenticationService
                .getClass().getName()));
        operationLog.info(String.format("Session expiration period: %s", DurationFormatUtils
                .formatDurationHMS(sessionExpirationPeriodMillis)));
        authenticationService.check();
    }

    private final T createAndStoreSession(final String user, final Principal principal,
            final long now)
    {
        final String sessionToken =
                user + SESSION_TOKEN_SEPARATOR
                        + tokenGenerator.getNewToken(now, TIMESTAMP_TOKEN_SEPARATOR);
        synchronized (sessions)
        {
            final T session =
                    sessionFactory.create(sessionToken, user, principal, getRemoteHost(), now,
                            sessionExpirationPeriodMillis);
            final FullSession<T> createdSession = new FullSession<T>(session);
            sessions.put(createdSession.getSession().getSessionToken(), createdSession);
            return session;
        }
    }

    private static void checkIfNotBlank(final String object, final String name)
            throws UserFailureException
    {
        if (StringUtils.isBlank(object))
        {
            throw UserFailureException.fromTemplate("No '%s' specified.", name);
        }
    }

    private boolean isSessionUnavailable(final FullSession<T> session)
    {
        return session == null || doSessionExpiration(session);
    }

    private boolean doSessionExpiration(final FullSession<T> session)
    {
        return session != null && session.hasExpired();
    }

    private void logAuthenticed(final T session)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(LOGIN_PREFIX + "User '" + session.getUserName()
                    + "' has been successfully authenticated from host '" + getRemoteHost()
                    + "'. Session token: '" + session.getSessionToken() + "'.");
        }
        final String prefix = prefixGenerator.createPrefix(session);
        authenticationLog.info(prefix + ": login");
    }

    private void logFailedAuthentication(final String user)
    {
        operationLog.warn(LOGIN_PREFIX + "User '" + user + "' failed to authenticate from host '"
                + getRemoteHost() + "'.");
        logAuthenticationFailure(user);
    }

    private void logSessionFailure(final String user, final RuntimeException ex)
    {
        logAuthenticationFailure(user);
        operationLog.error(LOGIN_PREFIX + "Error when trying to authenticate user '" + user + "'.",
                ex);
    }

    private void logAuthenticationFailure(final String user)
    {
        final String prefix = prefixGenerator.createPrefix(user, getRemoteHost());
        authenticationLog.info(prefix + ": login   ...FAILED");
    }

    private void logSessionExpired(final FullSession<T> fullSession)
    {
        final T session = fullSession.getSession();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("%sExpiring session '%s' for user '%s' "
                    + "after %d minutes of inactivity.", LOGOUT_PREFIX, session.getSessionToken(),
                    session.getUserName(), sessionExpirationPeriodMillis
                            / DateUtils.MILLIS_PER_MINUTE));
        }
        final String prefix = prefixGenerator.createPrefix(session);
        authenticationLog.info(prefix + ": session_expired  [inactive "
                + DurationFormatUtils.formatDurationHMS(sessionExpirationPeriodMillis) + "]");
    }

    private void logLogout(final T session)
    {
        final String prefix = prefixGenerator.createPrefix(session);
        authenticationLog.info(prefix + ": logout");
        if (operationLog.isInfoEnabled())
        {
            final String user = session.getUserName();
            operationLog.info(LOGOUT_PREFIX + "Session '" + session.getSessionToken()
                    + "' of user '" + user + "' has been closed.");
        }
    }

    public boolean isAWellFormedSessionToken(String sessionTokenOrNull)
    {
        if (sessionTokenOrNull == null)
        {
            return false;
        }
        final String[] splittedToken =
                StringUtils.split(sessionTokenOrNull, SESSION_TOKEN_SEPARATOR);
        if (splittedToken.length < 2)
        {
            return false;
        }
        String[] splittedTimeStampToken =
                StringUtils.split(splittedToken[1], TIMESTAMP_TOKEN_SEPARATOR);
        if (splittedTimeStampToken.length < 2)
        {
            return false;
        }
        try
        {
            Long.parseLong(splittedTimeStampToken[0]);
        } catch (NumberFormatException ex)
        {
            return false;
        }
        return splittedTimeStampToken[1].length() == 32;
    }

    public T getSession(final String sessionToken) throws InvalidSessionException
    {
        checkIfNotBlank(sessionToken, "sessionToken");

        synchronized (sessions)
        {
            final String[] splittedToken = StringUtils.split(sessionToken, SESSION_TOKEN_SEPARATOR);
            if (splittedToken.length < 2)
            {
                final String msg =
                        "Session token '" + sessionToken + "' is malformed. Please login again.";
                if (authenticationLog.isInfoEnabled())
                {
                    authenticationLog.info(msg);
                }
                throw new InvalidSessionException(msg);
            }
            final String user = getUserName(splittedToken);
            final FullSession<T> session = sessions.get(sessionToken);
            if (session == null)
            {
                final String msg =
                        "Session token '" + sessionToken + "' is invalid: user is not logged in.";
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(msg);
                }
                throw new InvalidSessionException(msg);
            }
            if (sessionToken.equals(session.getSession().getSessionToken()) == false)
            {
                final String msg =
                        "Session token '" + sessionToken
                                + "' is invalid: wrong token. Please login again.";
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(msg);
                }
                throw new InvalidSessionException(msg);
            }
            if (doSessionExpiration(session))
            {
                logSessionExpired(session);
                sessions.remove(user);
            }
            if (isSessionUnavailable(session))
            {
                throw new InvalidSessionException(
                        "Session no longer available. Please login again.");
            }
            // This is where we know for sure we have a session.
            session.touch();
            return session.getSession();
        }
    }

    // take all tokens till the third token counting from the back
    private static String getUserName(String[] splittedSessionToken)
    {
        int exclusiveEndIndex = splittedSessionToken.length - 1;
        return StringUtils
                .join(splittedSessionToken, SESSION_TOKEN_SEPARATOR, 0, exclusiveEndIndex);
    }

    public String tryToOpenSession(final String user, final String password)
    {
        checkIfNotBlank(user, "user");
        checkIfNotBlank(password, "password");
        try
        {
            final String applicationToken = authenticationService.authenticateApplication();
            if (applicationToken == null)
            {
                operationLog.error("User '" + user
                        + "' failed to authenticate: application not authenticated.");
                return null;
            }
            String sessionToken = null;
            final long now = System.currentTimeMillis();
            final boolean isAuthenticated =
                    authenticationService.authenticateUser(applicationToken, user, password);
            if (isAuthenticated)
            {
                try
                {
                    final Principal principal =
                            authenticationService.getPrincipal(applicationToken, user);
                    final T session = createAndStoreSession(user, principal, now);
                    sessionToken = session.getSessionToken();
                    logAuthenticed(session);
                } catch (final IllegalArgumentException ex)
                {
                    // getPrincipal() of an authenticated user should not fail, if it does, this
                    // is an environment failure.
                    throw new EnvironmentFailureException(ex.getMessage(), ex);
                }
            } else
            {
                logFailedAuthentication(user);
            }
            return sessionToken;
        } catch (final RuntimeException ex)
        {
            logSessionFailure(user, ex);
            throw ex;
        }
    }

    public void closeSession(final String sessionToken) throws InvalidSessionException
    {
        synchronized (sessions)
        {
            final T session = getSession(sessionToken);
            sessions.remove(sessionToken);
            logLogout(session);
        }
    }

    public String getRemoteHost()
    {
        return remoteHostProvider.getRemoteHost();
    }

}
