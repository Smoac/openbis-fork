/*
 * Copyright 2007 ETH Zuerich, CISD
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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.server.IRemoteHostProvider;
import ch.systemsx.cisd.common.test.Retry10;

/**
 * Test cases for the {@link DefaultSessionManager}.
 * 
 * @author Bernd Rinn
 */
public class DefaultSessionManagerTest
{

    private static final String REMOTE_HOST = "remote-host";

    /** Kind of dummy <code>Principal</code> to point out that the login was successful. */
    private static final Principal principal =
            new Principal("bla", StringUtils.EMPTY, StringUtils.EMPTY,
                    StringUtils.EMPTY, true);

    private static final int SESSION_EXPIRATION_PERIOD_MINUTES = 1;

    private Mockery context;

    private IAuthenticationService authenticationService;

    private ISessionFactory<BasicSession> sessionFactory;

    private ILogMessagePrefixGenerator<BasicSession> prefixGenerator;

    private IRemoteHostProvider remoteHostProvider;

    private ISessionManager<BasicSession> sessionManager;

    private BufferedAppender logRecorder;

    private IPrincipalProvider principalProvider;

    private void assertExceptionMessageForInvalidSessionToken(final UserFailureException ex)
    {
        final String message = ex.getMessage();
        assertTrue(message, message.indexOf("login again") > 0);
    }

    @SuppressWarnings("unchecked")
    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        sessionFactory = context.mock(ISessionFactory.class);
        prefixGenerator = context.mock(ILogMessagePrefixGenerator.class);
        authenticationService = context.mock(IAuthenticationService.class);
        remoteHostProvider = context.mock(IRemoteHostProvider.class);
        principalProvider = context.mock(IPrincipalProvider.class);
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).check();
                }
            });
        sessionManager = createSessionManager(SESSION_EXPIRATION_PERIOD_MINUTES);
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
    }

    @SuppressWarnings("unchecked")
    private ISessionManager<BasicSession> createSessionManager(int sessionExpiration)
    {
        return new DefaultSessionManager(sessionFactory, prefixGenerator, authenticationService,
                remoteHostProvider, sessionExpiration, true);
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    private void prepareRemoteHostSessionFactoryAndPrefixGenerator(final String user,
            final Principal sessionPrincipal)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(remoteHostProvider).getRemoteHost();
                    will(returnValue(REMOTE_HOST));

                    one(sessionFactory).create(with(any(String.class)), with(equal(user)),
                            with(equal(sessionPrincipal)), with(equal(REMOTE_HOST)),
                            with(any(Long.class)), with(any(Integer.class)));
                    BasicSession session =
                            new BasicSession(user + "-1", user, principal, REMOTE_HOST, 42L, 0);
                    will(returnValue(session));

                    atLeast(1).of(prefixGenerator).createPrefix(session);
                    will(returnValue("[USER:'" + user + "', HOST:'remote-host']"));
                }
            });
    }

    @Test
    public void testSuccessfulAuthentication()
    {
        final String user = "bla";
        prepareRemoteHostSessionFactoryAndPrefixGenerator(user, principal);
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).tryGetAndAuthenticateUser(user, "blub");
                    will(returnValue(principal));
                }
            });

        final String token = sessionManager.tryToOpenSession("bla", "blub");
        assertEquals("bla-1", token);
        assertEquals(
                "INFO  OPERATION.DefaultSessionManager - "
                        + "LOGIN: User 'bla' has been successfully authenticated from host 'remote-host'. Session token: '"
                        + token
                        + "'."
                        + OSUtilities.LINE_SEPARATOR
                        + "INFO  AUTH.DefaultSessionManager - [USER:'bla', HOST:'remote-host']: login",
                logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testSuccessfulAuthenticationWithEmailAddress()
    {
        final String user = "bla";
        final String userEmail = "bla@blub.com";
        final Principal sessionPrincipal =
                new Principal(user, StringUtils.EMPTY, StringUtils.EMPTY, userEmail, false);
        prepareRemoteHostSessionFactoryAndPrefixGenerator(user, sessionPrincipal);
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).tryGetAndAuthenticateUser(userEmail, "blub");
                    will(returnValue(null));
                    one(authenticationService).supportsListingByEmail();
                    will(returnValue(true));
                    one(authenticationService).listPrincipalsByEmail(userEmail);
                    will(returnValue(Arrays.asList(sessionPrincipal)));
                    one(authenticationService).authenticateUser(user, "blub");
                    will(returnValue(true));
                }
            });

        final String token = sessionManager.tryToOpenSession(userEmail, "blub");
        assertEquals("bla-1", token);
        assertEquals(
                "INFO  OPERATION.DefaultSessionManager - "
                        + "LOGIN: User 'bla' has been successfully authenticated from host 'remote-host'. Session token: '"
                        + token
                        + "'."
                        + OSUtilities.LINE_SEPARATOR
                        + "INFO  AUTH.DefaultSessionManager - [USER:'bla', HOST:'remote-host']: login",
                logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testFailedAuthentication()
    {
        final String user = "bla";
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).tryGetAndAuthenticateUser(user, "blub");
                    will(returnValue(null));

                    allowing(remoteHostProvider).getRemoteHost();
                    will(returnValue(REMOTE_HOST));

                    one(prefixGenerator).createPrefix(user, REMOTE_HOST);
                    will(returnValue("[USER:'bla', HOST:'remote-host']"));
                }
            });
        assert null == sessionManager.tryToOpenSession(user, "blub");
        assertEquals(
                "WARN  OPERATION.DefaultSessionManager - "
                        + "LOGIN: User 'bla' failed to authenticate from host 'remote-host'."
                        + OSUtilities.LINE_SEPARATOR
                        + "INFO  AUTH.DefaultSessionManager - [USER:'bla', HOST:'remote-host']: login   ...FAILED",
                logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testAuthenticationForUnavailableAuthenticationService()
    {
        final String errorMsg = "I pretend to be not here!";
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).check();
                    will(throwException(new EnvironmentFailureException(errorMsg)));
                }
            });
        try
        {
            createSessionManager(1);
            fail("Unavailable authentication service not expected");
        } catch (final EnvironmentFailureException e)
        {
            assertEquals(errorMsg, e.getMessage());
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testExpirationOfSession()
    {
        final String user = "bla";
        prepareRemoteHostSessionFactoryAndPrefixGenerator(user, principal);
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).check();

                    one(authenticationService).tryGetAndAuthenticateUser(user, "blub");
                    will(returnValue(principal));
                }
            });

        sessionManager = createSessionManager(0);
        final String sessionToken = sessionManager.tryToOpenSession("bla", "blub");
        assert sessionToken.length() > 0;
        try
        {
            Thread.sleep(100);
        } catch (final InterruptedException ex)
        {
            // ignored
        }
        logRecorder.resetLogContent();
        try
        {
            sessionManager.getSession(sessionToken);
            fail("UserFailureException expected because session has expired.");
        } catch (final UserFailureException e)
        {
            assertExceptionMessageForInvalidSessionToken(e);
        }
        assertEquals(
                "INFO  OPERATION.DefaultSessionManager - "
                        + "LOGOUT: Expiring session '"
                        + sessionToken
                        + "' for user 'bla' after 0 minutes of inactivity."
                        + OSUtilities.LINE_SEPARATOR
                        + "INFO  AUTH.DefaultSessionManager - [USER:'bla', HOST:'remote-host']: session_expired  [inactive 0:00:00.000]",
                logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test(retryAnalyzer = Retry10.class)
    public void testSessionRemoval()
    {
        final String user = "bla";
        final String password = "blub";
        prepareRemoteHostSessionFactoryAndPrefixGenerator(user, principal);
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).tryGetAndAuthenticateUser(user, password);
                    will(returnValue(principal));
                }
            });

        final String sessionToken = sessionManager.tryToOpenSession(user, password);

        sessionManager.closeSession(sessionToken);
        try
        {
            sessionManager.getSession(sessionToken);
            fail("UserFailureException expected because session token no longer valid.");
        } catch (final UserFailureException ex)
        {
            final String message = ex.getMessage();
            assertTrue(message, message.indexOf("user is not logged in") > 0);
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testTryToOpenSessionWithPrincipalProvider()
    {
        final String user = "u1";
        final Principal sessionPrincipal =
            new Principal(user, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, true);
        prepareRemoteHostSessionFactoryAndPrefixGenerator(user, sessionPrincipal);
        context.checking(new Expectations()
            {
                {
                    one(principalProvider).tryToGetPrincipal(user);
                    will(returnValue(sessionPrincipal));
                }
            });

        String token = sessionManager.tryToOpenSession(user, principalProvider);

        assertEquals(user + "-1", token);
        assertEquals(
                "INFO  OPERATION.DefaultSessionManager - "
                        + "LOGIN: User 'u1' has been successfully authenticated from host 'remote-host'. Session token: '"
                        + token
                        + "'."
                        + OSUtilities.LINE_SEPARATOR
                        + "INFO  AUTH.DefaultSessionManager - [USER:'u1', HOST:'remote-host']: login",
                logRecorder.getLogContent());
        context.assertIsSatisfied();
    }
}
