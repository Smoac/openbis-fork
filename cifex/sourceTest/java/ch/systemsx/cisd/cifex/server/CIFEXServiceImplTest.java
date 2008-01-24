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
import static org.testng.AssertJUnit.fail;

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
import ch.systemsx.cisd.cifex.client.UserFailureException;
import ch.systemsx.cisd.cifex.client.dto.User;
import ch.systemsx.cisd.cifex.server.business.IDomainModel;
import ch.systemsx.cisd.cifex.server.business.IUserManager;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
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
    public void testIsAuthenticated()
    {
        prepareForGettingUserFromHTTPSession(new UserDTO(), false);

        ICIFEXService service = createService(null);
        assertEquals(true, service.isAuthenticated());

        context.assertIsSatisfied();
    }

    @Test
    public void testIsNotAuthenticated()
    {
        prepareForGettingUserFromHTTPSession(null, false);

        ICIFEXService service = createService(null);
        assertEquals(false, service.isAuthenticated());

        context.assertIsSatisfied();
    }
    
    @Test
    public void testLoginWithoutExternalServiceFailedBecauseOfInvalidUser() throws UserFailureException
    {
        final UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setEncryptedPassword(StringUtilities.encrypt(password));
        prepareForFindUser("blabla", null);
        
        CIFEXServiceImpl service = createService(null);
        User user = service.tryToLogin("blabla", password);
        assertEquals(null, user);
        
        context.assertIsSatisfied();
    }

    @Test
    public void testLoginWithoutExternalServiceFailedBecauseOfInvalidPassword() throws UserFailureException
    {
        final UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setEncryptedPassword(StringUtilities.encrypt(password));
        prepareForFindUser(userDTO.getEmail(), userDTO);
        
        CIFEXServiceImpl service = createService(new NullAuthenticationService());
        User user = service.tryToLogin(userDTO.getEmail(), "blabla");
        assertEquals(null, user);
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testLoginWithoutExternalService() throws UserFailureException
    {
        final UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setEncryptedPassword(StringUtilities.encrypt(password));
        prepareForFindUser(userDTO.getEmail(), userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true);
        
        CIFEXServiceImpl service = createService(new NullAuthenticationService());
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryToLogin(userDTO.getEmail(), password);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserName(), user.getUserName());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testLoginWithExternalServiceFailedBecauseApplicationAuthenticationFailed()
    {
        context.checking(new Expectations()
        {
            {
                one(domainModel).getUserManager();
                will(returnValue(userManager));
                
                one(authenticationService).check();
                one(authenticationService).authenticateApplication();
                will(returnValue(null));
            }
        });
        
        ICIFEXService service = createService(authenticationService);
        try
        {
            service.tryToLogin("u", "p");
            fail("UserFailureException expected.");
        } catch (UserFailureException ex)
        {
            assertEquals("Authentication of the server at the external authentication service failed.", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testLoginWithExternalServiceFailedBecausePrincipalNotFound()
    {
        final String userName = "u";
        final String password = "p";
        prepareForExternalAuthentication(userName, password, null);
        
        ICIFEXService service = createService(authenticationService);
        try
        {
            service.tryToLogin(userName, password);
            fail("UserFailureException expected.");
        } catch (UserFailureException ex)
        {
            assertEquals("Authentication was successful but user information couldn't be retrieved.", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testFailedLoginAtExternalService() throws UserFailureException
    {
        final String userName = "u";
        final String password = "p";
        context.checking(new Expectations()
            {
                {
                    one(domainModel).getUserManager();
                    will(returnValue(userManager));

                    one(authenticationService).check();
                    one(authenticationService).authenticateApplication();
                    will(returnValue(APPLICATION_TOKEN_EXAMPLE));

                    one(authenticationService).authenticateUser(APPLICATION_TOKEN_EXAMPLE, userName, password);
                    will(returnValue(false));
                }
            });

        ICIFEXService service = createService(authenticationService);
        assertEquals(null, service.tryToLogin(userName, password));

        context.assertIsSatisfied();
    }
    
    @Test
    public void testSecondLoginWithExternalService() throws UserFailureException
    {
        UserDTO userDTO = new UserDTO();
        String password = "pswd";
        userDTO.setUserName("user");
        userDTO.setEmail("user@users.org");
        userDTO.setEncryptedPassword(StringUtilities.encrypt(password));
        Principal principal = new Principal("ae", "Albert", "Einstein", "my-email");
        prepareForExternalAuthentication(userDTO.getUserName(), password, principal);
        prepareForFindUser(principal.getEmail(), userDTO);
        prepareForGettingUserFromHTTPSession(userDTO, true);
        
        CIFEXServiceImpl service = createService(authenticationService);
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryToLogin(userDTO.getUserName(), password);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserName(), user.getUserName());
        
        context.assertIsSatisfied();
    }

    @Test
    public void testFirstLoginWithExternalService() throws UserFailureException
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
        userDTO.setEncryptedPassword(StringUtilities.encrypt(password));
        userDTO.setPermanent(true);
        userDTO.setExternallyAuthenticated(true);
        context.checking(new Expectations()
            {
                {
                    one(userManager).createUser(userDTO);
                }
            });
        prepareForGettingUserFromHTTPSession(userDTO, true);
        
        CIFEXServiceImpl service = createService(authenticationService);
        service.setSessionExpirationPeriodInMinutes(1);
        User user = service.tryToLogin(userDTO.getUserName(), password);
        assertEquals(userDTO.getEmail(), user.getEmail());
        assertEquals(userDTO.getUserName(), user.getUserName());
        
        context.assertIsSatisfied();
    }
    
    private void prepareForExternalAuthentication(final String userName, final String password, final Principal principal)
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
                        one(httpSession).setAttribute(CIFEXServiceImpl.SESSION_NAME, userDTO);
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

    private void prepareForFindUser(final String email, final UserDTO userDTO)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(domainModel).getUserManager();
                    will(returnValue(userManager));

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
