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
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.cifex.client.EnvironmentFailureException;
import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.InsufficientPrivilegesException;
import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.client.dto.User;
import ch.systemsx.cisd.cifex.server.business.DummyUserActionLog;
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
        final String code = "userCode";
        final String email = "Email";
        userDTO.setUserCode(code);
        userDTO.setEmail(email);
        prepareForGettingUserFromHTTPSession(userDTO, false);

        ICIFEXService service = createService(null);
        assertEquals(code, service.getCurrentUser().getUserCode());
        assertEquals(email.toLowerCase(), userDTO.getEmail());
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
        userDTO.setUserFullName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        prepareForFindUser("blabla", null);

        CIFEXServiceImpl service = createService(null);
        User user = service.tryLogin("blabla", password);
        assertEquals(null, user);

        context.assertIsSatisfied();
    }

    @Test
    public void testLoginWithoutExternalServiceFailedBecauseOfInvalidPassword() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserFullName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        prepareForFindUser(userDTO.getUserCode(), userDTO);

        CIFEXServiceImpl service = createService(new NullAuthenticationService());
        User user = service.tryLogin(userDTO.getUserCode(), "blabla");
        assertEquals(null, user);

        context.assertIsSatisfied();
    }

    @Test
    public void testInitialLoginWithoutExternalService() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserFullName(null);
        userDTO.setUserCode("user@users.org");
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
        User user = service.tryLogin(userDTO.getEmail(), password);
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
        userDTO.setUserFullName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        prepareForFindUser(userDTO.getUserCode(), userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true);

        CIFEXServiceImpl service = createService(new NullAuthenticationService());
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryLogin(userDTO.getUserCode(), password);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserFullName(), user.getUserFullName());
        assertFalse(userDTO.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testAdminAsNormalUserLoginWithoutExternalService() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserCode("user");
        userDTO.setUserFullName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setAdmin(true);
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        prepareForFindUser(userDTO.getUserCode(), userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true, false);

        CIFEXServiceImpl service = createService(new NullAuthenticationService());
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryLogin(userDTO.getUserCode(), password);
        assertEquals(userDTO.getUserCode(), user.getUserCode());
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserFullName(), user.getUserFullName());
        assertTrue(userDTO.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testAdminLoginWithoutExternalService() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserCode("user");
        userDTO.setUserFullName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setAdmin(true);
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        prepareForFindUser(userDTO.getUserCode(), userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true);

        CIFEXServiceImpl service = createService(new NullAuthenticationService());
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryLogin(userDTO.getUserCode(), password);
        assertEquals(userDTO.getUserCode(), user.getUserCode());
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserFullName(), user.getUserFullName());
        assertTrue(userDTO.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testLoginWithExternalServiceFailedBecauseApplicationAuthenticationFailed()
            throws Exception
    {
        prepareForDBEmptyCheck();
        context.checking(new Expectations()
            {
                {
                    allowing(requestContextProvider).getHttpServletRequest();
                    will(returnValue(httpServletRequest));
                    allowing(httpServletRequest).getRemoteHost();
                    will(returnValue("someRemoteHost"));
                    allowing(httpServletRequest).getRemoteAddr();
                    will(returnValue("someRemoteAddress"));
                    one(authenticationService).check();
                    one(authenticationService).authenticateApplication();
                    will(returnValue(null));
                }
            });

        ICIFEXService service = createService(authenticationService);
        try
        {
            service.tryLogin("u", "p");
            fail("UserFailureException expected.");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals(
                    "User \'u\' couldn\'t be authenticated because authentication of the application at the "
                            + "external authentication service failed.", ex.getMessage());
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
            service.tryLogin(userName, password);
            fail("UserFailureException expected.");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("Cannot find user 'u'.", ex.getMessage());
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
        userDTO.setUserFullName(userName);
        userDTO.setEmail(email);
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).check();
                    one(authenticationService).authenticateApplication();
                    will(returnValue(APPLICATION_TOKEN_EXAMPLE));

                    one(authenticationService).authenticateUser(APPLICATION_TOKEN_EXAMPLE,
                            userName, password);
                    will(returnValue(false));
                }
            });
        prepareForFindUser(userName, userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true);

        CIFEXServiceImpl service = createService(authenticationService);
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryLogin(userName, password);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserFullName(), user.getUserFullName());
        assertFalse(userDTO.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testFailedLoginAtExternalServiceAndSuccessfulLoginAtInternalService()
            throws Exception
    {
        final String userName = "u";
        final String password = "p";
        prepareForDBEmptyCheck();
        context.checking(new Expectations()
            {
                {
                    allowing(requestContextProvider).getHttpServletRequest();
                    will(returnValue(httpServletRequest));
                    allowing(httpServletRequest).getRemoteHost();
                    will(returnValue("someRemoteHost"));
                    allowing(httpServletRequest).getRemoteAddr();
                    will(returnValue("someRemoteAddress"));
                    one(authenticationService).check();
                    one(authenticationService).authenticateApplication();
                    will(returnValue(APPLICATION_TOKEN_EXAMPLE));

                    one(authenticationService).authenticateUser(APPLICATION_TOKEN_EXAMPLE,
                            userName, password);
                    will(returnValue(false));

                    one(userManager).tryFindUserByCode(userName);
                    will(returnValue(null));
                }
            });

        ICIFEXService service = createService(authenticationService);
        assertEquals(null, service.tryLogin(userName, password));

        context.assertIsSatisfied();
    }

    @Test
    public void testSecondLoginWithExternalService() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        final String password = "pswd";
        final String lastName = "Einstein";
        final String firstName = "Albert";
        final String fullName = firstName + " " + lastName;
        final String email = "user@users.org";

        userDTO.setUserFullName(fullName);
        userDTO.setEmail(email);
        userDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        Principal principal = new Principal("ae", firstName, lastName, email);
        prepareForExternalAuthentication(userDTO.getUserFullName(), password, principal);
        prepareForFindUser(principal.getUserId(), userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true);

        CIFEXServiceImpl service = createService(authenticationService);
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryLogin(userDTO.getUserFullName(), password);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserFullName(), user.getUserFullName());
        assertFalse(userDTO.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testSecondLoginWithExternalServiceWithNameChanged() throws Exception
    {
        final String code = "ae";
        final String password = "pswd";
        final String firstName = "Albert";
        final String lastName = "Zweistein";
        final String oldFullName = "Albert Einstein";
        final String newFullName = firstName + " " + lastName;
        final String email = "user@users.org";

        final UserDTO oldUserDTO = new UserDTO();
        oldUserDTO.setUserFullName(oldFullName);
        oldUserDTO.setEmail(email);
        oldUserDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        final UserDTO newUserDTO = BeanUtils.createBean(UserDTO.class, oldUserDTO);
        newUserDTO.setUserFullName(newFullName);
        final Principal principal = new Principal(code, firstName, lastName, email);
        prepareForExternalAuthentication(oldUserDTO.getUserCode(), password, principal);
        prepareForFindUser(principal.getUserId(), oldUserDTO);
        context.checking(new Expectations()
            {
                {
                    one(userManager).updateUser(newUserDTO, null);
                }
            });
        prepareForGettingUserFromHTTPSession(newUserDTO, true);

        CIFEXServiceImpl service = createService(authenticationService);
        service.setSessionExpirationPeriodInMinutes(1);
        final User user = service.tryLogin(oldUserDTO.getUserCode(), password);
        assertEquals(oldUserDTO.getEmail(), user.getEmail());
        assertEquals(oldUserDTO.getUserFullName(), user.getUserFullName());
        assertFalse(oldUserDTO.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testSecondLoginWithExternalServiceWithEmailChanged() throws Exception
    {
        final String code = "ae";
        final String password = "pswd";
        final String firstName = "Albert";
        final String lastName = "Einstein";
        final String fullName = "Albert Einstein";
        final String oldEmail = "user@users.org";
        final String newEmail = "ae@users.org";

        final UserDTO oldUserDTO = new UserDTO();
        oldUserDTO.setUserFullName(fullName);
        oldUserDTO.setEmail(oldEmail);
        oldUserDTO.setEncryptedPassword(StringUtilities.computeMD5Hash(password));
        final UserDTO newUserDTO = BeanUtils.createBean(UserDTO.class, oldUserDTO);
        newUserDTO.setEmail(newEmail);
        final Principal principal = new Principal(code, firstName, lastName, newEmail);
        prepareForExternalAuthentication(oldUserDTO.getUserCode(), password, principal);
        prepareForFindUser(principal.getUserId(), oldUserDTO);
        context.checking(new Expectations()
            {
                {
                    one(userManager).updateUser(newUserDTO, null);
                }
            });
        prepareForGettingUserFromHTTPSession(newUserDTO, true);

        CIFEXServiceImpl service = createService(authenticationService);
        service.setSessionExpirationPeriodInMinutes(1);
        final User user = service.tryLogin(oldUserDTO.getUserCode(), password);
        assertEquals(oldUserDTO.getEmail(), user.getEmail());
        assertEquals(oldUserDTO.getUserFullName(), user.getUserFullName());
        assertFalse(oldUserDTO.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testFirstLoginWithExternalService() throws Exception
    {
        final String password = "pswd";
        final String userName = "ae";
        final String lastName = "Einstein";
        final String firstName = "Albert";
        final String email = "ae@users.org";

        Principal principal = new Principal(userName, firstName, lastName, email);
        prepareForExternalAuthentication(userName, password, principal);
        prepareForFindUser(principal.getUserId(), null);
        final UserDTO userDTO = new UserDTO();
        userDTO.setUserCode(userName);
        userDTO.setUserFullName(firstName + " " + lastName);
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
        User user = service.tryLogin(userDTO.getUserCode(), password);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserFullName(), user.getUserFullName());
        assertFalse(userDTO.isAdmin());

        context.assertIsSatisfied();
    }

    private void prepareForExternalAuthentication(final String userName, final String password,
            final Principal principal)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(requestContextProvider).getHttpServletRequest();
                    will(returnValue(httpServletRequest));
                    allowing(httpServletRequest).getRemoteHost();
                    will(returnValue("someRemoteHost"));
                    allowing(httpServletRequest).getRemoteAddr();
                    will(returnValue("someRemoteAddress"));

                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));

                    one(authenticationService).check();
                    one(authenticationService).authenticateApplication();
                    will(returnValue(APPLICATION_TOKEN_EXAMPLE));

                    one(authenticationService).authenticateUser(APPLICATION_TOKEN_EXAMPLE,
                            userName, password);
                    will(returnValue(true));

                    one(authenticationService).getPrincipal(APPLICATION_TOKEN_EXAMPLE, userName);
                    if (principal != null)
                    {
                        will(returnValue(principal));
                    } else
                    {
                        will(throwException(new IllegalArgumentException("Cannot find user '"
                                + userName + "'.")));
                    }
                }
            });
    }

    private void prepareForGettingUserFromHTTPSession(final UserDTO userDTO,
            final boolean createFlag)
    {
        prepareForGettingUserFromHTTPSession(userDTO, createFlag, false);
    }

    private void prepareForGettingUserFromHTTPSession(final UserDTO userDTO,
            final boolean createFlag, final boolean resetAdmin)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(requestContextProvider).getHttpServletRequest();
                    will(returnValue(httpServletRequest));

                    if (createFlag)
                    {
                        one(httpServletRequest).getSession(true);
                        will(returnValue(httpSession));
                    }
                    allowing(httpServletRequest).getSession(false);
                    will(returnValue(httpSession));

                    if (createFlag)
                    {
                        one(httpSession).setMaxInactiveInterval(60);
                        final UserDTO transferredUserDTO =
                                BeanUtils.createBean(UserDTO.class, userDTO);
                        transferredUserDTO.setEncryptedPassword(null);
                        if (resetAdmin)
                        {
                            transferredUserDTO.setAdmin(false);
                        }
                        one(httpSession).setAttribute(CIFEXServiceImpl.SESSION_NAME,
                                transferredUserDTO);
                        one(httpSession).setAttribute(
                                with(same(CIFEXServiceImpl.UPLOAD_FEEDBACK_QUEUE)),
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

    private void prepareForFindUser(final String code, final UserDTO userDTO)
    {
        prepareForFindUser(code, userDTO, false);
    }

    private void prepareForFindUser(final String code, final UserDTO userDTO, final boolean dbEmpty)
    {
        prepareForDBEmptyCheck(dbEmpty);
        context.checking(new Expectations()
            {
                {
                    allowing(requestContextProvider).getHttpServletRequest();
                    will(returnValue(httpServletRequest));
                    allowing(httpServletRequest).getRemoteHost();
                    will(returnValue("someRemoteHost"));
                    allowing(httpServletRequest).getRemoteAddr();
                    will(returnValue("someRemoteAddress"));
                    one(userManager).tryFindUserByCode(code);
                    will(returnValue(userDTO));
                }
            });
    }

    private CIFEXServiceImpl createService(IAuthenticationService aService)
    {
        return new CIFEXServiceImpl(domainModel, requestContextProvider, new DummyUserActionLog(),
                aService);
    }

    @SuppressWarnings("unused")
    @DataProvider(name = "currentUserAndUserToUpdate")
    private Object[][] provideAllBooleans()
    {
        UserDTO adminRegistrant = createUser(true, true, "admin1", null);
        UserDTO adminChanger = createUser(true, true, "admin2", null);
        UserDTO alice = createUser(true, false, "alice", adminRegistrant);
        UserDTO aliceWannabeAdmin = createUser(true, true, "alice", adminRegistrant);
        UserDTO aliceTemp = createUser(false, false, "alice", adminRegistrant);
        UserDTO permNotRegisteredByAlice = createUser(true, false, "perm1", adminRegistrant);
        UserDTO permRegisteredByAlice = createUser(true, false, "perm2", alice);
        UserDTO tempNotRegisteredByAlice = createUser(false, false, "temp1", adminRegistrant);
        UserDTO tempRegisteredByAlice = createUser(false, false, "temp2", alice);

        return new Object[][]
            {
                // admin can change everything
                    { adminChanger, adminChanger, true },
                    { adminChanger, adminRegistrant, true },
                    { adminChanger, alice, true },
                    { adminChanger, tempRegisteredByAlice, true },

                    // permanent user can change
                    { alice, alice, true }, // himself
                        { alice, tempRegisteredByAlice, true }, // temp registered by him
                        // permanent user cannot change

                    { alice, adminRegistrant, false }, // admin
                        { alice, adminChanger, false },
                        { alice, permNotRegisteredByAlice, false }, // other perm
                        { alice, permRegisteredByAlice, false },
                        { alice, tempNotRegisteredByAlice, false }, // temp not registered by him
                        { alice, aliceWannabeAdmin, false }, // himself to admin
                        { alice, aliceTemp, true }, // himself to temp

                        // temporary user cannot change anything
                    { tempRegisteredByAlice, adminChanger, false },
                    { tempRegisteredByAlice, adminRegistrant, false },
                    { tempRegisteredByAlice, alice, false },
                    { tempRegisteredByAlice, tempRegisteredByAlice, false },
                    { tempRegisteredByAlice, tempNotRegisteredByAlice, false } };
    }

    final static UserDTO createUser(boolean permanent, boolean admin, String code,
            UserDTO registrator)
    {
        UserDTO user = new UserDTO();
        user.setUserCode(code);
        user.setAdmin(admin);
        user.setPermanent(permanent);
        if (registrator == null)
        {
            user.setRegistrator(new UserDTO());
        } else
        {
            user.setRegistrator(registrator);
        }
        return user;
    }

    @Test(dataProvider = "currentUserAndUserToUpdate")
    public void testCheckUpdateOfUserIsAllowedCurrentUserIsAdmin(final UserDTO currentUser,
            final UserDTO userToUpdate, boolean canDo)
    {
        boolean invalidSessionExceptionThrown = false;
        boolean insufficientPrivilegesExceptionThrown = false;
        boolean unknownExceptionThrown = false;
        final List<UserDTO> ownedUsers = new ArrayList<UserDTO>();
        context.checking(new Expectations()
            {
                {
                    // changer is not admin, not temporary and tries to change someone else
                    if (currentUser.isAdmin() == false
                            && currentUser.isPermanent()
                            && (currentUser.getUserCode().equals(userToUpdate.getUserCode()) == false))
                    {
                        one(userManager).listUsersRegisteredBy(currentUser.getUserCode());
                        if (currentUser.getUserCode().equals(
                                userToUpdate.getRegistrator().getUserCode()))
                        {
                            ownedUsers.add(userToUpdate);
                        }
                        will(returnValue(ownedUsers));
                    }
                }
            });
        try
        {
            CIFEXServiceImpl.checkUpdateOfUserIsAllowed(userToUpdate, currentUser, userManager);
        } catch (InvalidSessionException ex)
        {
            invalidSessionExceptionThrown = true;

        } catch (InsufficientPrivilegesException ex)
        {
            insufficientPrivilegesExceptionThrown = true;

        } catch (Exception ex)
        {
            unknownExceptionThrown = true;
        }

        assertFalse(invalidSessionExceptionThrown);
        if (canDo)
        {
            assertFalse(insufficientPrivilegesExceptionThrown);
        } else
        {
            assertTrue(insufficientPrivilegesExceptionThrown);
        }
        assertFalse(unknownExceptionThrown);

        context.assertIsSatisfied();
    }
}
