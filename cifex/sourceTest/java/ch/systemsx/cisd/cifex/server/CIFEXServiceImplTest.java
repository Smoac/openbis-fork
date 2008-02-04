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

package ch.systemsx.cisd.cifex.server;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.fail;

import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.client.UserFailureException;
import ch.systemsx.cisd.cifex.client.dto.User;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IUserManager;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.utilities.BeanUtils;
import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
 * Test cases for corresponding {@link CIFEXServiceImpl} class.
 * 
 * @author Franz-Josef Elmer
 */
public class CIFEXServiceImplTest
{
    private static final String APPLICATION_TOKEN_EXAMPLE = "application-token";

    private static final String SESSION_TOKEN_EXAMPLE = "session-token42";

    private Mockery context;

    private IDomainModel domainModel;

    private IUserManager userManager;

    private IRequestContextProvider requestContextProvider;

    private IAuthenticationService authenticationService;

    private HttpSession httpSession;

    private HttpServletRequest httpServletRequest;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        domainModel = context.mock(IDomainModel.class);
        userManager = context.mock(IUserManager.class);
        requestContextProvider = context.mock(IRequestContextProvider.class);
        authenticationService = context.mock(IAuthenticationService.class);
        httpSession = context.mock(HttpSession.class);
        httpServletRequest = context.mock(HttpServletRequest.class);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testIsAuthenticated() throws InvalidSessionException
    {
        final UserDTO userDTO = new UserDTO();
        final String email = "Email";
        userDTO.setEmail(email);
        prepareForGettingUserFromHTTPSession(userDTO, false);

        ICIFEXService service = createService(null);
        assertEquals(userDTO.getEmail(), service.getCurrentUser().getEmail());
        assertEquals(userDTO.getEmail(), email);
        context.assertIsSatisfied();
    }

    @Test
    public void testIsNotAuthenticated() throws InvalidSessionException
    {
        prepareForGettingUserFromHTTPSession(null, false);

        ICIFEXService service = createService(null);
        assertEquals(null, service.getCurrentUser());

        context.assertIsSatisfied();
    }

    @Test
    public void testLoginWithoutExternalServiceFailedBecauseOfInvalidUser() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        prepareForFindUser("blabla", null);

        CIFEXServiceImpl service = createService(null);
        User user = service.tryToLogin("blabla", password, false);
        assertEquals(null, user);

        context.assertIsSatisfied();
    }

    @Test
    public void testLoginWithoutExternalServiceFailedBecauseOfInvalidPassword() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        prepareForFindUser(userDTO.getEmail(), userDTO);

        CIFEXServiceImpl service = createService(new NullAuthenticationService());
        User user = service.tryToLogin(userDTO.getEmail(), "blabla", false);
        assertEquals(null, user);

        context.assertIsSatisfied();
    }

    @Test
    public void testInitialLoginWithoutExternalService() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserName(null);
        userDTO.setEmail("user@users.org");
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        userDTO.setPermanent(true);
        userDTO.setAdmin(true);
        prepareForDBEmptyCheck(true);
        context.checking(new Expectations()
            {
                {
                    one(userManager).createUser(userDTO);
                }
            });
        prepareForGettingUserFromHTTPSession(userDTO, true);

        CIFEXServiceImpl service = createService(new NullAuthenticationService());
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryToLogin(userDTO.getEmail(), password, false);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertNull(user.getUserFullName());
        assertTrue(user.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testLoginWithoutExternalService() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        prepareForFindUser(userDTO.getEmail(), userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true);

        CIFEXServiceImpl service = createService(new NullAuthenticationService());
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryToLogin(userDTO.getEmail(), password, false);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserName(), user.getUserFullName());
        assertFalse(userDTO.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testAdminAsNormalUserLoginWithoutExternalService() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setAdmin(true);
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        prepareForFindUser(userDTO.getEmail(), userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true, true);

        CIFEXServiceImpl service = createService(new NullAuthenticationService());
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryToLogin(userDTO.getEmail(), password, false);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserName(), user.getUserFullName());
        assertFalse(userDTO.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testAdminLoginWithoutExternalService() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setAdmin(true);
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        prepareForFindUser(userDTO.getEmail(), userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true);

        CIFEXServiceImpl service = createService(new NullAuthenticationService());
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryToLogin(userDTO.getEmail(), password, true);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserName(), user.getUserFullName());
        assertTrue(userDTO.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testFailedAdminLoginWithoutExternalService() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        prepareForFindUser(userDTO.getEmail(), userDTO);

        CIFEXServiceImpl service = createService(new NullAuthenticationService());
        service.setSessionExpirationPeriodInMinutes(1);
        try
        {
            service.tryToLogin(userDTO.getEmail(), password, true);
            fail("Expected user failure exception due to admin request.");
        } catch (UserFailureException ex)
        {
            assertTrue(ex.getMessage().indexOf("admin permissions") >= 0);
        }
        context.assertIsSatisfied();
    }

    @Test
    public void testLoginWithExternalServiceFailedBecauseApplicationAuthenticationFailed() throws Exception
    {
        prepareForDBEmptyCheck();
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).check();
                    one(authenticationService).authenticateApplication();
                    will(returnValue(null));
                }
            });

        ICIFEXService service = createService(authenticationService);
        try
        {
            service.tryToLogin("u", "p", false);
            fail("UserFailureException expected.");
        } catch (UserFailureException ex)
        {
            assertEquals("Authentication of the server application at the external authentication service failed.", ex
                    .getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testLoginWithExternalServiceFailedBecausePrincipalNotFound() throws Exception
    {
        final String userName = "u";
        final String password = "p";
        prepareForDBEmptyCheck();
        prepareForExternalAuthentication(userName, password, null);

        ICIFEXService service = createService(authenticationService);
        try
        {
            service.tryToLogin(userName, password, false);
            fail("UserFailureException expected.");
        } catch (UserFailureException ex)
        {
            assertEquals("Unable to retrieve user information.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testFailedLoginAtExternalServiceAndInternalService() throws Exception
    {
        final String userName = "u";
        final String password = "p";
        final String email = "user@users.org";
        final UserDTO userDTO = new UserDTO();
        userDTO.setUserName(userName);
        userDTO.setEmail(email);
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).check();
                    one(authenticationService).authenticateApplication();
                    will(returnValue(APPLICATION_TOKEN_EXAMPLE));

                    one(authenticationService).authenticateUser(APPLICATION_TOKEN_EXAMPLE, userName, password);
                    will(returnValue(false));
                }
            });
        prepareForFindUser(userName, userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true);

        CIFEXServiceImpl service = createService(authenticationService);
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryToLogin(userName, password, false);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserName(), user.getUserFullName());
        assertFalse(userDTO.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testFailedLoginAtExternalServiceAndSuccessfulLoginAtInternalService() throws Exception
    {
        final String userName = "u";
        final String password = "p";
        prepareForDBEmptyCheck();
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).check();
                    one(authenticationService).authenticateApplication();
                    will(returnValue(APPLICATION_TOKEN_EXAMPLE));

                    one(authenticationService).authenticateUser(APPLICATION_TOKEN_EXAMPLE, userName, password);
                    will(returnValue(false));

                    one(userManager).tryToFindUser(userName);
                    will(returnValue(null));
                }
            });

        ICIFEXService service = createService(authenticationService);
        assertEquals(null, service.tryToLogin(userName, password, false));

        context.assertIsSatisfied();
    }

    @Test
    public void testSecondLoginWithExternalService() throws Exception
    {
        UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        Principal principal = new Principal("ae", "Albert", "Einstein", "my-email");
        prepareForExternalAuthentication(userDTO.getUserName(), password, principal);
        prepareForFindUser(principal.getEmail(), userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true);

        CIFEXServiceImpl service = createService(authenticationService);
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryToLogin(userDTO.getUserName(), password, false);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserName(), user.getUserFullName());
        assertFalse(userDTO.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testFirstLoginWithExternalService() throws Exception
    {
        String password = "pswd";
        String userName = "ae";
        String email = "ae@users.org";
        Principal principal = new Principal(userName, "Albert", "Einstein", email);
        prepareForExternalAuthentication(userName, password, principal);
        prepareForFindUser(principal.getEmail(), null);
        final UserDTO userDTO = new UserDTO();
        userDTO.setUserName(userName);
        userDTO.setEmail(email);
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        userDTO.setPermanent(true);
        userDTO.setExternallyAuthenticated(true);
        context.checking(new Expectations()
            {
                {
                    // We do not store the password of externally authenticated users.
                    final UserDTO createdUserDTO = BeanUtils.createBean(UserDTO.class, userDTO);
                    createdUserDTO.setEncryptedPassword(null);
                    one(userManager).createUser(createdUserDTO);
                }
            });
        prepareForGettingUserFromHTTPSession(userDTO, true);

        CIFEXServiceImpl service = createService(authenticationService);
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryToLogin(userDTO.getUserName(), password, false);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserName(), user.getUserFullName());
        assertFalse(userDTO.isAdmin());

        context.assertIsSatisfied();
    }

    private void prepareForExternalAuthentication(final String userName, final String password,
            final Principal principal)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));

                    one(authenticationService).check();
                    one(authenticationService).authenticateApplication();
                    will(returnValue(APPLICATION_TOKEN_EXAMPLE));

                    one(authenticationService).authenticateUser(APPLICATION_TOKEN_EXAMPLE, userName, password);
                    will(returnValue(true));

                    one(authenticationService).getPrincipal(APPLICATION_TOKEN_EXAMPLE, userName);
                    will(returnValue(principal));
                }
            });
    }

    private void prepareForGettingUserFromHTTPSession(final UserDTO userDTO, final boolean createFlag)
    {
        prepareForGettingUserFromHTTPSession(userDTO, createFlag, false);
    }

    private void prepareForGettingUserFromHTTPSession(final UserDTO userDTO, final boolean createFlag,
            final boolean resetAdmin)
    {
        context.checking(new Expectations()
            {
                {
                    one(requestContextProvider).getHttpServletRequest();
                    will(returnValue(httpServletRequest));

                    one(httpServletRequest).getSession(createFlag);
                    will(returnValue(httpSession));

                    if (createFlag)
                    {
                        one(httpSession).setMaxInactiveInterval(60);
                        final UserDTO transferredUserDTO = BeanUtils.createBean(UserDTO.class, userDTO);
                        transferredUserDTO.setEncryptedPassword(null);
                        if (resetAdmin)
                        {
                            transferredUserDTO.setAdmin(false);
                        }
                        one(httpSession).setAttribute(CIFEXServiceImpl.SESSION_NAME, transferredUserDTO);
                        one(httpSession).setAttribute(with(same(CIFEXServiceImpl.UPLOAD_QUEUE)),
                                with(any(LinkedBlockingQueue.class)));
                        one(httpSession).setAttribute(with(same(CIFEXServiceImpl.UPLOAD_MSG_QUEUE)),
                                with(any(LinkedBlockingQueue.class)));
                        one(httpSession).getId();
                        will(returnValue(SESSION_TOKEN_EXAMPLE));
                    } else
                    {
                        one(httpSession).getAttribute(CIFEXServiceImpl.SESSION_NAME);
                        will(returnValue(userDTO));
                    }
                }
            });
    }

    private void prepareForDBEmptyCheck()
    {
        prepareForDBEmptyCheck(false);
    }

    private void prepareForDBEmptyCheck(final boolean dbEmpty)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));

                    one(userManager).isDatabaseEmpty();
                    will(returnValue(dbEmpty));
                }
            });
    }

    private void prepareForFindUser(final String email, final UserDTO userDTO)
    {
        prepareForFindUser(email, userDTO, false);
    }

    private void prepareForFindUser(final String email, final UserDTO userDTO, final boolean dbEmpty)
    {
        prepareForDBEmptyCheck(dbEmpty);
        context.checking(new Expectations()
            {
                {
                    one(userManager).tryToFindUser(email);
                    will(returnValue(userDTO));
                }
            });
    }

    private CIFEXServiceImpl createService(IAuthenticationService aService)
    {
        return new CIFEXServiceImpl(domainModel, requestContextProvider, aService);
    }
}
