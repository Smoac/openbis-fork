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
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.cifex.client.FileNotFoundException;
import ch.systemsx.cisd.cifex.client.ICIFEXService;
import ch.systemsx.cisd.cifex.client.InsufficientPrivilegesException;
import ch.systemsx.cisd.cifex.client.InvalidSessionException;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;
import ch.systemsx.cisd.cifex.server.business.DummyUserActionLog;
import ch.systemsx.cisd.cifex.server.business.FileInformation;
import ch.systemsx.cisd.cifex.server.business.IBusinessContext;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IFileManager;
import ch.systemsx.cisd.cifex.server.business.IUserManager;
import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.cifex.server.common.Password;
import ch.systemsx.cisd.cifex.shared.basic.EnvironmentFailureException;
import ch.systemsx.cisd.cifex.shared.basic.UserFailureException;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.utilities.BeanUtils;

/**
 * Test cases for corresponding {@link CIFEXServiceImpl} class.
 * 
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = CIFEXServiceImpl.class)
public class CIFEXServiceImplTest
{
    private static final String ERROR_MSG_FILE_FOUND_IN_THE_DATABASE =
            "File [id=%s] not found in the database. Try to refresh the page.";

    private static final String APPLICATION_TOKEN_EXAMPLE = "application-token";

    private static final String SESSION_TOKEN_EXAMPLE = "session-token42";

    private static final String DEFAULT_FILE_ID = "1";

    private static final String DEFAULT_USER_CODE = "alice";

    private static final String DEFAULT_PLAIN_PASSWORD = "secret password";

    private static final String DEFAULT_USER_LAST_NAME = "Wonderland";

    private static final String DEFAULT_USER_FIRST_NAME = "Alice";

    private static final String DEFAULT_USER_EMAIL = "alice@wonderland.com";

    private Mockery context;

    private IDomainModel domainModel;

    private IBusinessContext businessContext;

    private IUserManager userManager;

    private IFileManager fileManager;

    private IRequestContextProvider requestContextProvider;

    private IAuthenticationService authenticationService;

    private HttpSession httpSession;

    private HttpServletRequest httpServletRequest;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        domainModel = context.mock(IDomainModel.class);
        businessContext = context.mock(IBusinessContext.class);
        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getBusinessContext();
                    will(returnValue(businessContext));
                    allowing(businessContext).isNewExternallyAuthenticatedUserStartActive();
                    will(returnValue(true));
                }
            });
        userManager = context.mock(IUserManager.class);
        fileManager = context.mock(IFileManager.class);
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

        final ICIFEXService service = createService(null);
        assertEquals(code, service.getCurrentUser().getUserCode());
        assertEquals(email.toLowerCase(), userDTO.getEmail());
        context.assertIsSatisfied();
    }

    @Test
    public void testIsNotAuthenticated() throws InvalidSessionException
    {
        prepareForGettingUserFromHTTPSession(null, false);

        final ICIFEXService service = createService(null);
        assertEquals(null, service.getCurrentUser());

        context.assertIsSatisfied();
    }

    @Test
    public void testLoginInternalUserFailedBecauseOfInvalidUser() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        final String password = "pswd";
        userDTO.setUserFullName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setPassword(new Password(password));
        prepareForFindUser("blabla", null);

        final CIFEXServiceImpl service = createService(null);
        final UserInfoDTO user = service.tryLogin("blabla", password);
        assertEquals(null, user);

        context.assertIsSatisfied();
    }

    @Test
    public void testLoginInternalUserFailedBecauseOfInvalidPassword() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        final String password = "pswd";
        userDTO.setUserFullName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setPasswordHash(new Password(password).createPasswordHash());
        prepareForFindUser(userDTO.getUserCode(), userDTO);

        final CIFEXServiceImpl service = createService(new NullAuthenticationService());
        final UserInfoDTO user = service.tryLogin(userDTO.getUserCode(), "blabla");
        assertEquals(null, user);

        context.assertIsSatisfied();
    }

    @Test
    public void testInitialLoginWithoutExternalService() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        final String password = "pswd";
        userDTO.setUserFullName(null);
        userDTO.setUserCode("user@users.org");
        userDTO.setEmail("user@users.org");
        userDTO.setPassword(new Password(password));
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

        final CIFEXServiceImpl service = createService(new NullAuthenticationService());
        final UserInfoDTO user = service.tryLogin(userDTO.getEmail(), password);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertNull(user.getUserFullName());
        assertTrue(user.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testLoginInternalUser() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        final String password = "pswd";
        userDTO.setUserFullName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setPasswordHash(new Password(password).createPasswordHash());
        prepareForFindUser(userDTO.getUserCode(), userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true);

        final CIFEXServiceImpl service = createService(new NullAuthenticationService());
        final UserInfoDTO user = service.tryLogin(userDTO.getUserCode(), password);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserFullName(), user.getUserFullName());
        assertFalse(userDTO.isAdmin());

        context.assertIsSatisfied();
    }

    @Test
    public void testAdminAsNormalInternalUser() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        final String password = "pswd";
        userDTO.setUserCode("user");
        userDTO.setUserFullName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setAdmin(true);
        userDTO.setPassword(new Password(password));
        prepareForFindUser(userDTO.getUserCode(), userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true, false);

        final CIFEXServiceImpl service = createService(new NullAuthenticationService());
        final UserInfoDTO user = service.tryLogin(userDTO.getUserCode(), password);
        assertEquals(userDTO.getUserCode(), user.getUserCode());
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserFullName(), user.getUserFullName());
        assertTrue(userDTO.isAdmin());

        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = InsufficientPrivilegesException.class)
    public void testChangeUserCodeByNoAdmin() throws InvalidSessionException,
            InsufficientPrivilegesException, EnvironmentFailureException
    {
        final String before = "before";
        final String after = "after";
        final UserDTO userDTO = new UserDTO();
        final String email = "Email";
        userDTO.setUserCode(after);
        userDTO.setEmail(email);
        userDTO.setAdmin(false);
        prepareForGettingUserFromHTTPSession(userDTO, false);

        context.checking(new Expectations()
            {
                {
                    one(domainModel).getUserManager();
                    will(returnValue(userManager));

                    one(userManager).tryFindUserByCode(before);
                    will(returnValue(userDTO));
                }
            });
        final CIFEXServiceImpl service = createService(null);
        service.changeUserCode(before, after);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = EnvironmentFailureException.class)
    public void testCreateUserExistingInExternalService() throws InvalidSessionException,
            InsufficientPrivilegesException, EnvironmentFailureException, UserFailureException
    {
        final UserInfoDTO userToCreate = new UserInfoDTO();
        userToCreate.setEmail(DEFAULT_USER_EMAIL);
        userToCreate.setUserCode(DEFAULT_USER_CODE);
        userToCreate.setUserFullName(DEFAULT_USER_FIRST_NAME + " " + DEFAULT_USER_LAST_NAME);
        final UserDTO admin = new UserDTO();
        admin.setAdmin(true);
        admin.setEmail("admin@admins.com");
        admin.setUserCode("admin");
        admin.setPermanent(true);
        prepareForGettingUserFromHTTPSession(admin, false);
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).check();

                    one(domainModel).getUserManager();
                    will(returnValue(userManager));

                    one(authenticationService).authenticateApplication();
                    will(returnValue(APPLICATION_TOKEN_EXAMPLE));

                    one(authenticationService).getPrincipal(APPLICATION_TOKEN_EXAMPLE,
                            userToCreate.getUserCode());

                }
            });
        final CIFEXServiceImpl service = createService(authenticationService);
        service.createUser(userToCreate, "pass", BeanUtils.createBean(UserInfoDTO.class, admin),
                "My great new user");
        context.assertIsSatisfied();
    }

    private void prepareForBasicURL(final Expectations expectations)
    {
        expectations.one(httpServletRequest).getScheme();
        expectations.will(Expectations.returnValue("http"));

        expectations.one(httpServletRequest).getServerName();
        expectations.will(Expectations.returnValue("cifex.org"));

        expectations.one(httpServletRequest).getServerPort();
        expectations.will(Expectations.returnValue(8080));

        expectations.one(httpServletRequest).getContextPath();
        expectations.will(Expectations.returnValue(null));
    }

    @Test
    public void testCreateUserNotExistingInExternalService() throws InvalidSessionException,
            InsufficientPrivilegesException, EnvironmentFailureException, UserFailureException
    {
        final String password = "pass";
        final UserInfoDTO userToCreate = new UserInfoDTO();
        userToCreate.setEmail(DEFAULT_USER_EMAIL);
        userToCreate.setUserCode(DEFAULT_USER_CODE);
        userToCreate.setUserFullName(DEFAULT_USER_FIRST_NAME + " " + DEFAULT_USER_LAST_NAME);
        final UserDTO admin = new UserDTO();
        admin.setAdmin(true);
        admin.setEmail("admin@admins.com");
        admin.setUserCode("admin");
        admin.setPermanent(true);
        prepareForGettingUserFromHTTPSession(admin, false);
        final String comment = "My great new user";
        final String basicUrl = "http://cifex.org:8080";
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).check();

                    one(domainModel).getUserManager();
                    will(returnValue(userManager));

                    one(authenticationService).authenticateApplication();
                    will(returnValue(APPLICATION_TOKEN_EXAMPLE));

                    one(authenticationService).getPrincipal(APPLICATION_TOKEN_EXAMPLE,
                            userToCreate.getUserCode());
                    will(throwException(new IllegalArgumentException()));

                    prepareForBasicURL(this);

                    one(userManager).createUserAndSendEmail(
                            BeanUtils.createBean(UserDTO.class, userToCreate), password, admin,
                            comment, basicUrl);

                }
            });
        final CIFEXServiceImpl service = createService(authenticationService);
        service.createUser(userToCreate, password, BeanUtils.createBean(UserInfoDTO.class, admin),
                comment);
        context.assertIsSatisfied();
    }

    @Test
    public void testCreateUserNoExternalAuthenticationServiceAvailable()
            throws InvalidSessionException, InsufficientPrivilegesException,
            EnvironmentFailureException, UserFailureException
    {
        final String password = "pass";
        final UserInfoDTO userToCreate = new UserInfoDTO();
        userToCreate.setEmail(DEFAULT_USER_EMAIL);
        userToCreate.setUserCode(DEFAULT_USER_CODE);
        userToCreate.setUserFullName(DEFAULT_USER_FIRST_NAME + " " + DEFAULT_USER_LAST_NAME);
        final UserDTO admin = new UserDTO();
        admin.setAdmin(true);
        admin.setEmail("admin@admins.com");
        admin.setUserCode("admin");
        admin.setPermanent(true);
        prepareForGettingUserFromHTTPSession(admin, false);
        final String comment = "My great new user";
        final String basicUrl = "http://cifex.org:8080";
        context.checking(new Expectations()
            {
                {
                    one(domainModel).getUserManager();
                    will(returnValue(userManager));

                    prepareForBasicURL(this);

                    one(userManager).createUserAndSendEmail(
                            BeanUtils.createBean(UserDTO.class, userToCreate), password, admin,
                            comment, basicUrl);

                }
            });
        final CIFEXServiceImpl service = createService(null);
        service.createUser(userToCreate, password, BeanUtils.createBean(UserInfoDTO.class, admin),
                comment);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = InsufficientPrivilegesException.class)
    public void testChangeUserCodeByAdminToHimself() throws InvalidSessionException,
            InsufficientPrivilegesException, EnvironmentFailureException
    {
        final String before = "before";
        final String after = "after";
        final UserDTO userDTO = new UserDTO();
        final String email = "Email";
        userDTO.setUserCode(before);
        userDTO.setEmail(email);
        userDTO.setAdmin(true);
        prepareForGettingUserFromHTTPSession(userDTO, false);

        context.checking(new Expectations()
            {
                {
                    one(domainModel).getUserManager();
                    will(returnValue(userManager));

                    one(userManager).tryFindUserByCode(before);
                    will(returnValue(userDTO));
                }
            });
        final CIFEXServiceImpl service = createService(null);
        service.changeUserCode(before, after);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = InsufficientPrivilegesException.class)
    public void testChangeUserCodeByAdminForExternalUser() throws InvalidSessionException,
            InsufficientPrivilegesException, EnvironmentFailureException
    {
        final String before = "before";
        final String after = "after";
        final UserDTO userDTO = new UserDTO();
        final String email = "Email";
        userDTO.setUserCode("Admin");
        userDTO.setEmail(email);
        userDTO.setAdmin(true);

        final UserDTO userToChange = new UserDTO();
        userToChange.setUserCode(before);
        userToChange.setEmail(email);
        userToChange.setExternallyAuthenticated(true);
        prepareForGettingUserFromHTTPSession(userDTO, false);

        context.checking(new Expectations()
            {
                {
                    one(domainModel).getUserManager();
                    will(returnValue(userManager));

                    one(userManager).tryFindUserByCode(before);
                    will(returnValue(userToChange));
                }
            });
        final CIFEXServiceImpl service = createService(null);
        service.changeUserCode(before, after);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testChangeUserCodeByAdminIllegalCode() throws InvalidSessionException,
            InsufficientPrivilegesException, EnvironmentFailureException
    {
        final String before = "before";
        final String after = "af ter";
        final UserDTO userDTO = new UserDTO();
        final String email = "Email";
        userDTO.setUserCode("Admin");
        userDTO.setEmail(email);
        userDTO.setAdmin(true);

        final UserDTO userToChange = new UserDTO();
        userToChange.setUserCode(before);
        userToChange.setEmail(email);
        prepareForGettingUserFromHTTPSession(userDTO, false);

        context.checking(new Expectations()
            {
                {
                    one(domainModel).getUserManager();
                    will(returnValue(userManager));

                }
            });
        final CIFEXServiceImpl service = createService(null);
        service.changeUserCode(before, after);
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteFile() throws InvalidSessionException, InsufficientPrivilegesException,
            FileNotFoundException
    {
        final UserDTO userDTO = new UserDTO();
        final String code = "userCode";
        final String email = "Email";
        userDTO.setUserCode(code);
        userDTO.setEmail(email);
        prepareForGettingUserFromHTTPSession(userDTO, false);

        final long fileID = Long.parseLong(DEFAULT_FILE_ID);
        final FileDTO fileDTO = new FileDTO(fileID);
        final FileInformation fileInformation = new FileInformation(fileID, fileDTO, null);

        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getFileManager();
                    will(returnValue(fileManager));

                    one(fileManager).getFileInformationFilestoreUnimportant(fileID);
                    will(returnValue(fileInformation));

                    one(fileManager).isAllowedDeletion(userDTO, fileDTO);
                    will(returnValue(true));

                    one(fileManager).deleteFile(fileDTO);
                }
            });

        final ICIFEXService service = createService(null);
        service.deleteFile(DEFAULT_FILE_ID);
        context.assertIsSatisfied();
    }

    @Test
    public void testDeleteFileNotExistingInDB() throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        final UserDTO userDTO = new UserDTO();
        final String code = "userCode";
        final String email = "Email";
        userDTO.setUserCode(code);
        userDTO.setEmail(email);
        prepareForGettingUserFromHTTPSession(userDTO, false);

        final long fileID = Long.parseLong(DEFAULT_FILE_ID);
        final String errorMessage = String.format(ERROR_MSG_FILE_FOUND_IN_THE_DATABASE, fileID);
        final FileInformation fileInformation = new FileInformation(fileID, errorMessage);

        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getFileManager();
                    will(returnValue(fileManager));

                    one(fileManager).getFileInformationFilestoreUnimportant(fileID);
                    will(returnValue(fileInformation));
                }
            });

        final ICIFEXService service = createService(null);
        boolean exceptionThrown = false;
        try
        {
            service.deleteFile(DEFAULT_FILE_ID);
        } catch (final FileNotFoundException ex)
        {
            exceptionThrown = true;
            assertEquals(ex.getMessage(), errorMessage);
        }
        assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testAdminLoginWithoutExternalService() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        final String password = "pswd";
        userDTO.setUserCode("user");
        userDTO.setUserFullName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setAdmin(true);
        userDTO.setPassword(new Password(password));
        prepareForFindUser(userDTO.getUserCode(), userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true);

        final CIFEXServiceImpl service = createService(new NullAuthenticationService());
        final UserInfoDTO user = service.tryLogin(userDTO.getUserCode(), password);
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
        final String userCode = "u";
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
                    one(userManager).tryFindUserByCodeFillRegistrator(userCode);
                    will(returnValue(null));
                }
            });

        final ICIFEXService service = createService(authenticationService);
        try
        {
            service.tryLogin(userCode, "p");
            fail("UserFailureException expected.");
        } catch (final EnvironmentFailureException ex)
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
        context.checking(new Expectations()
            {
                {
                    one(userManager).tryFindUserByCodeFillRegistrator(userName);
                    will(returnValue(null));
                }
            });
        final ICIFEXService service = createService(authenticationService);
        try
        {
            service.tryLogin(userName, password);
            fail("UserFailureException expected.");
        } catch (final EnvironmentFailureException ex)
        {
            assertEquals("Cannot find user 'u'.", ex.getMessage());
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testTrySwichToExternalAuthenticationNoServisAvailable()
            throws InvalidSessionException, InsufficientPrivilegesException,
            EnvironmentFailureException
    {
        final CIFEXServiceImpl service = createService(new NullAuthenticationService());
        boolean exceptionThrown = false;
        try
        {
            service.trySwitchToExternalAuthentication(DEFAULT_USER_CODE, DEFAULT_PLAIN_PASSWORD);
        } catch (final EnvironmentFailureException e)
        {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testTrySwichToExternalAuthenticationUserDoesNotExistInDb()
            throws InvalidSessionException, InsufficientPrivilegesException,
            EnvironmentFailureException
    {
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).check();
                    one(domainModel).getUserManager();
                    will(returnValue(userManager));
                    one(userManager).tryFindUserByCode(DEFAULT_USER_CODE);
                    will(returnValue(null));
                }
            });
        final CIFEXServiceImpl service = createService(authenticationService);
        boolean exceptionThrown = false;
        try
        {
            service.trySwitchToExternalAuthentication(DEFAULT_USER_CODE, DEFAULT_PLAIN_PASSWORD);
        } catch (final EnvironmentFailureException e)
        {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testTrySwichToExternalAuthenticationUserExistsInDbAlreadyExternal()
            throws InvalidSessionException, InsufficientPrivilegesException,
            EnvironmentFailureException
    {
        final UserDTO userDTO = new UserDTO();
        userDTO.setExternallyAuthenticated(true);
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).check();
                    one(domainModel).getUserManager();
                    will(returnValue(userManager));
                    one(userManager).tryFindUserByCode(DEFAULT_USER_CODE);
                    will(returnValue(userDTO));
                }
            });
        final CIFEXServiceImpl service = createService(authenticationService);
        boolean exceptionThrown = false;
        try
        {
            service.trySwitchToExternalAuthentication(DEFAULT_USER_CODE, DEFAULT_PLAIN_PASSWORD);
        } catch (final EnvironmentFailureException e)
        {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testTrySwichToExternalAuthenticationUserExistsInDbAndIsNotExternalNotAuthenticated()
            throws InvalidSessionException, InsufficientPrivilegesException,
            EnvironmentFailureException
    {
        final UserDTO userDTO = new UserDTO();
        userDTO.setExternallyAuthenticated(false);
        context.checking(new Expectations()
            {
                {
                    one(domainModel).getUserManager();
                    will(returnValue(userManager));
                    one(userManager).tryFindUserByCode(DEFAULT_USER_CODE);
                    will(returnValue(userDTO));

                    one(authenticationService).check();
                    one(authenticationService).authenticateApplication();
                    will(returnValue(APPLICATION_TOKEN_EXAMPLE));
                    one(authenticationService).authenticateUser(APPLICATION_TOKEN_EXAMPLE,
                            DEFAULT_USER_CODE, DEFAULT_PLAIN_PASSWORD);
                    will(returnValue(false));
                }
            });

        final CIFEXServiceImpl service = createService(authenticationService);
        boolean exceptionThrown = false;
        try
        {
            service.trySwitchToExternalAuthentication(DEFAULT_USER_CODE, DEFAULT_PLAIN_PASSWORD);
        } catch (final InsufficientPrivilegesException e)
        {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public void testTrySwichToExternalAuthenticationUserExistsInDbAndIsNotExternalAuthenticated()
            throws InvalidSessionException, InsufficientPrivilegesException,
            EnvironmentFailureException
    {
        final UserDTO oldUser = new UserDTO();
        oldUser.setExternallyAuthenticated(false);
        oldUser.setUserCode(DEFAULT_USER_CODE);
        oldUser.setAdmin(false);
        oldUser.setPermanent(false);
        oldUser.setExpirationDate(new Date());
        oldUser.setRegistrator(new UserDTO());
        final String oldUserFullName = "Old Alice Name";
        oldUser.setUserFullName(oldUserFullName);
        final String oldUserEmail = "oldalice@users.com";
        oldUser.setEmail(oldUserEmail);

        final UserDTO externalyUpdatedUser = new UserDTO();
        externalyUpdatedUser.setExternallyAuthenticated(false);
        externalyUpdatedUser.setUserCode(DEFAULT_USER_CODE);
        externalyUpdatedUser.setAdmin(oldUser.isAdmin());
        externalyUpdatedUser.setPermanent(oldUser.isPermanent());
        externalyUpdatedUser.setExpirationDate(oldUser.getExpirationDate());
        externalyUpdatedUser.setRegistrator(oldUser.getRegistrator());
        externalyUpdatedUser
                .setUserFullName(DEFAULT_USER_FIRST_NAME + " " + DEFAULT_USER_LAST_NAME);
        externalyUpdatedUser.setEmail(DEFAULT_USER_EMAIL);

        final UserDTO newUser = new UserDTO();
        newUser.setExternallyAuthenticated(true);
        newUser.setUserCode(DEFAULT_USER_CODE);
        newUser.setAdmin(oldUser.isAdmin());
        newUser.setPermanent(true);
        newUser.setExpirationDate(null);
        newUser.setRegistrator(null);
        newUser.setUserFullName(DEFAULT_USER_FIRST_NAME + " " + DEFAULT_USER_LAST_NAME);
        newUser.setEmail(DEFAULT_USER_EMAIL);

        final Principal principal =
                new Principal(DEFAULT_USER_CODE, DEFAULT_USER_FIRST_NAME, DEFAULT_USER_LAST_NAME,
                        DEFAULT_USER_EMAIL);
        context.checking(new Expectations()
            {
                {
                    exactly(2).of(domainModel).getUserManager();
                    will(returnValue(userManager));
                    exactly(2).of(userManager).tryFindUserByCode(DEFAULT_USER_CODE);
                    will(returnValue(oldUser));

                    one(authenticationService).check();
                    one(authenticationService).authenticateApplication();
                    will(returnValue(APPLICATION_TOKEN_EXAMPLE));
                    one(authenticationService).authenticateUser(APPLICATION_TOKEN_EXAMPLE,
                            DEFAULT_USER_CODE, DEFAULT_PLAIN_PASSWORD);
                    will(returnValue(true));
                    one(authenticationService).getPrincipal(APPLICATION_TOKEN_EXAMPLE,
                            DEFAULT_USER_CODE);
                    will(returnValue(principal));

                    one(userManager).updateUser(externalyUpdatedUser, null);
                    one(userManager).updateUser(newUser, null);

                    one(requestContextProvider).getHttpServletRequest();
                    will(returnValue(httpServletRequest));

                    one(httpServletRequest).getSession(false);
                    will(returnValue(httpSession));

                    one(httpSession).getAttribute("cifex-user");
                    will(returnValue(oldUser));
                }
            });
        Assert.assertEquals(oldUser.getEmail(), oldUserEmail);
        Assert.assertEquals(oldUser.isAdmin(), false);
        Assert.assertEquals(oldUser.isExternallyAuthenticated(), false);
        Assert.assertEquals(oldUser.getUserFullName(), oldUserFullName);
        Assert.assertFalse(oldUser.getRegistrator() == null);
        Assert.assertEquals(oldUser.isPermanent(), false);
        Assert.assertFalse(oldUser.getExpirationDate() == null);

        final CIFEXServiceImpl service = createService(authenticationService);

        service.trySwitchToExternalAuthentication(DEFAULT_USER_CODE, DEFAULT_PLAIN_PASSWORD);
        Assert.assertEquals(newUser.getEmail(), DEFAULT_USER_EMAIL);
        Assert.assertEquals(newUser.isAdmin(), false);
        Assert.assertEquals(newUser.isExternallyAuthenticated(), true);
        Assert.assertEquals(newUser.getUserFullName(), newUser.getUserFullName());
        Assert.assertEquals(newUser.getRegistrator(), null);
        Assert.assertEquals(newUser.isPermanent(), true);
        Assert.assertEquals(newUser.getExpirationDate(), null);

        Assert.assertEquals(oldUser.getEmail(), DEFAULT_USER_EMAIL);
        Assert.assertEquals(oldUser.isAdmin(), false);
        Assert.assertEquals(oldUser.isExternallyAuthenticated(), true);
        Assert.assertEquals(oldUser.getUserFullName(), newUser.getUserFullName());
        Assert.assertEquals(oldUser.getRegistrator(), null);
        Assert.assertEquals(oldUser.isPermanent(), true);
        Assert.assertEquals(oldUser.getExpirationDate(), null);

        context.assertIsSatisfied();
    }

    @Test
    public void testSecondLoginWithExternalService() throws Exception
    {
        final UserDTO userDTO = new UserDTO();
        final String userCode = "ae";
        final String password = "pswd";
        final String lastName = "Einstein";
        final String firstName = "Albert";
        final String fullName = firstName + " " + lastName;
        final String email = "user@users.org";
        userDTO.setUserCode(userCode);
        userDTO.setExternallyAuthenticated(true);
        userDTO.setUserFullName(fullName);
        userDTO.setEmail(email);
        userDTO.setPasswordHash(new Password(password).createPasswordHash());
        final Principal principal = new Principal(userCode, firstName, lastName, email);
        prepareForExternalAuthentication(userDTO.getUserCode(), password, principal);
        prepareForFindUser(principal.getUserId(), userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true);

        context.checking(new Expectations()
            {
                {
                    one(userManager).tryFindUserByCode(userCode);
                    will(returnValue(userDTO));
                }
            });

        final CIFEXServiceImpl service = createService(authenticationService);
        final UserInfoDTO user = service.tryLogin(userDTO.getUserCode(), password);
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
        oldUserDTO.setUserCode(code);
        oldUserDTO.setUserFullName(oldFullName);
        oldUserDTO.setEmail(email);
        oldUserDTO.setPasswordHash(new Password(password).createPasswordHash());
        oldUserDTO.setExternallyAuthenticated(true);
        final UserDTO newUserDTO = BeanUtils.createBean(UserDTO.class, oldUserDTO);
        newUserDTO.setUserFullName(newFullName);
        final Principal principal = new Principal(code, firstName, lastName, email);
        prepareForExternalAuthentication(oldUserDTO.getUserCode(), password, principal);
        prepareForFindUser(principal.getUserId(), oldUserDTO);
        context.checking(new Expectations()
            {
                {
                    one(userManager).tryFindUserByCode(code);
                    will(returnValue(oldUserDTO));
                    one(userManager).updateUser(newUserDTO, null);
                }
            });
        prepareForGettingUserFromHTTPSession(newUserDTO, true);

        final CIFEXServiceImpl service = createService(authenticationService);
        final UserInfoDTO user = service.tryLogin(oldUserDTO.getUserCode(), password);
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
        oldUserDTO.setUserCode(code);
        oldUserDTO.setUserFullName(fullName);
        oldUserDTO.setEmail(oldEmail);
        oldUserDTO.setPasswordHash(new Password(password).createPasswordHash());
        oldUserDTO.setExternallyAuthenticated(true);
        final UserDTO newUserDTO = BeanUtils.createBean(UserDTO.class, oldUserDTO);
        newUserDTO.setEmail(newEmail);
        final Principal principal = new Principal(code, firstName, lastName, newEmail);
        prepareForExternalAuthentication(oldUserDTO.getUserCode(), password, principal);
        prepareForFindUser(principal.getUserId(), oldUserDTO);
        context.checking(new Expectations()
            {
                {
                    one(userManager).tryFindUserByCode(code);
                    will(returnValue(oldUserDTO));
                    one(userManager).updateUser(newUserDTO, null);
                }
            });
        prepareForGettingUserFromHTTPSession(newUserDTO, true);
        assertEquals(oldUserDTO.getEmail(), oldEmail);

        final CIFEXServiceImpl service = createService(authenticationService);
        final UserInfoDTO user = service.tryLogin(oldUserDTO.getUserCode(), password);

        assertEquals(oldUserDTO.getEmail(), user.getEmail());
        assertEquals(newEmail, user.getEmail());
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

        final Principal principal = new Principal(userName, firstName, lastName, email);
        final UserDTO userDTO = new UserDTO();
        userDTO.setUserCode(userName);
        userDTO.setUserFullName(firstName + " " + lastName);
        userDTO.setEmail(email);
        userDTO.setPasswordHash(new Password(password).createPasswordHash());
        userDTO.setPermanent(true);
        userDTO.setExternallyAuthenticated(true);
        prepareForExternalAuthentication(userName, password, principal);
        prepareForFindUser(principal.getUserId(), null);
        context.checking(new Expectations()
            {
                {
                    one(userManager).tryFindUserByCode(userName);
                    will(returnValue(null));
                    // We do not store the password of externally authenticated users.
                    final UserDTO createdUserDTO = BeanUtils.createBean(UserDTO.class, userDTO);
                    createdUserDTO.setPasswordHash(null);
                    one(userManager).createUser(createdUserDTO);
                }
            });
        prepareForGettingUserFromHTTPSession(userDTO, true);

        final CIFEXServiceImpl service = createService(authenticationService);
        final UserInfoDTO user = service.tryLogin(userDTO.getUserCode(), password);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserFullName(), user.getUserFullName());
        assertFalse(userDTO.isAdmin());
        assertTrue(userDTO.isPermanent());

        context.assertIsSatisfied();
    }

    @Test
    public void testFirstLoginWithExternalServiceTemporaryUserAlreadyExists() throws Exception
    {
        final String externalPassword = "ext-pswd";
        final String password = "pswd";
        final String userName = "ae";
        final String lastName = "Einstein";
        final String firstName = "Albert";
        final String email = "ae@users.org";

        final UserDTO userDTO = new UserDTO();
        userDTO.setUserCode(userName);
        userDTO.setUserFullName(firstName + " " + lastName);
        userDTO.setEmail(email);
        userDTO.setPasswordHash(new Password(password).createPasswordHash());
        userDTO.setPermanent(false);
        userDTO.setExternallyAuthenticated(false);
        userDTO.setExpirationDate(new Date());
        context.checking(new Expectations()
            {
                {
                    one(authenticationService).check();
                    one(domainModel).getUserManager();
                    will(returnValue(userManager));

                    one(userManager).isDatabaseEmpty();
                    will(returnValue(false));

                    one(userManager).tryFindUserByCodeFillRegistrator(userName);
                    will(returnValue(userDTO));

                }
            });

        final CIFEXServiceImpl service = createService(authenticationService);
        final UserInfoDTO user = service.tryLogin(userDTO.getUserCode(), externalPassword);
        assertTrue(user == null);

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
                        final Password password = userDTO.getPassword();
                        userDTO.setPassword(null);
                        final UserDTO transferredUserDTO =
                                BeanUtils.createBean(UserDTO.class, userDTO);
                        userDTO.setPassword(password);
                        transferredUserDTO.setPasswordHash(null);
                        if (resetAdmin)
                        {
                            transferredUserDTO.setAdmin(false);
                        }
                        one(httpSession).setAttribute(CIFEXServiceImpl.SESSION_ATTRIBUTE_USER_NAME,
                                transferredUserDTO);
                        one(httpSession).setAttribute(
                                with(same(CIFEXServiceImpl.UPLOAD_FEEDBACK_QUEUE)),
                                with(any(LinkedBlockingQueue.class)));
                        one(httpSession).getId();
                        will(returnValue(SESSION_TOKEN_EXAMPLE));
                    } else
                    {
                        one(httpSession).getAttribute(CIFEXServiceImpl.SESSION_ATTRIBUTE_USER_NAME);
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
                    one(userManager).tryFindUserByCodeFillRegistrator(code);
                    final UserDTO dbUserDTO;
                    if (userDTO != null && Password.isEmpty(userDTO.getPassword()) == false)
                    {
                        final Password password = userDTO.getPassword();
                        userDTO.setPassword(null);
                        dbUserDTO = BeanUtils.createBean(UserDTO.class, userDTO);
                        userDTO.setPassword(password);
                        dbUserDTO.setPasswordHash(password.createPasswordHash());
                    } else
                    {
                        dbUserDTO = userDTO;
                    }
                    will(returnValue(dbUserDTO));
                }
            });
    }

    private CIFEXServiceImpl createService(final IAuthenticationService aService)
    {
        return new CIFEXServiceImpl(domainModel, requestContextProvider, new DummyUserActionLog(),
                aService, 1);
    }

    @SuppressWarnings("unused")
    @DataProvider(name = "currentUserAndUserToUpdate")
    private Object[][] provideAllBooleans()
    {
        final UserDTO adminRegistrant = createUser(true, true, "admin1", null);
        final UserDTO adminChanger = createUser(true, true, "admin2", null);
        final UserDTO alice = createUser(true, false, "alice", adminRegistrant);
        final UserDTO aliceWannabeAdmin = createUser(true, true, "alice", adminRegistrant);
        final UserDTO aliceTemp = createUser(false, false, "alice", adminRegistrant);
        final UserDTO permNotRegisteredByAlice = createUser(true, false, "perm1", adminRegistrant);
        final UserDTO permRegisteredByAlice = createUser(true, false, "perm2", alice);
        final UserDTO tempNotRegisteredByAlice = createUser(false, false, "temp1", adminRegistrant);
        final UserDTO tempRegisteredByAlice = createUser(false, false, "temp2", alice);

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

    final static UserDTO createUser(final boolean permanent, final boolean admin,
            final String code, final UserDTO registrator)
    {
        final UserDTO user = new UserDTO();
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
            final UserDTO userToUpdate, final boolean canDo)
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
                            && currentUser.getUserCode().equals(userToUpdate.getUserCode()) == false)
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
            CIFEXServiceImpl.checkUpdateOfUserIsAllowed(userToUpdate, userToUpdate, currentUser,
                    userManager);
        } catch (final InvalidSessionException ex)
        {
            invalidSessionExceptionThrown = true;

        } catch (final InsufficientPrivilegesException ex)
        {
            insufficientPrivilegesExceptionThrown = true;

        } catch (final Exception ex)
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

    @Test
    public void testCheckUpdateOfUserAdminChangeActiveFlag() throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        final UserDTO currentUser = new UserDTO();
        currentUser.setUserCode("requestUser");
        currentUser.setPermanent(true);
        currentUser.setAdmin(true);
        currentUser.setActive(true);
        final UserDTO userToUpdate = new UserDTO();
        userToUpdate.setAdmin(false);
        userToUpdate.setActive(true);
        userToUpdate.setUserCode("test");
        final UserDTO oldUserToUpdate = BeanUtils.createBean(UserDTO.class, userToUpdate);
        oldUserToUpdate.setActive(false);
        CIFEXServiceImpl.checkUpdateOfUserIsAllowed(oldUserToUpdate, userToUpdate, currentUser,
                userManager);
    }

    @Test(expectedExceptions = InsufficientPrivilegesException.class)
    public void testCheckUpdateOfUserNonAdminChangeActiveFlag() throws InvalidSessionException,
            InsufficientPrivilegesException
    {
        final UserDTO currentUser = new UserDTO();
        currentUser.setUserCode("requestUser");
        currentUser.setPermanent(true);
        currentUser.setAdmin(false);
        currentUser.setActive(true);
        final UserDTO userToUpdate = new UserDTO();
        userToUpdate.setAdmin(false);
        userToUpdate.setActive(true);
        userToUpdate.setUserCode("test");
        final UserDTO oldUserToUpdate = BeanUtils.createBean(UserDTO.class, userToUpdate);
        oldUserToUpdate.setActive(false);
        CIFEXServiceImpl.checkUpdateOfUserIsAllowed(oldUserToUpdate, userToUpdate, currentUser,
                userManager);
    }

}
