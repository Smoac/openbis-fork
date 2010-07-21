/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.authentication.stacked;

import static org.testng.AssertJUnit.*;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;

/**
 * Test cases for the {@link StackedAuthenticationService}.
 * 
 * @author Bernd Rinn
 */
public class StackedAuthenticationServiceTest
{
    private Mockery context;

    private IAuthenticationService authService1;

    private IAuthenticationService authService2;

    private IAuthenticationService stackedAuthService;

    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        addStandardExpectations();
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
    }

    private void addStandardExpectations()
    {
        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    one(authService1).supportsListingByLastName();
                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                }
            });
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testCheck()
    {
        context.checking(new Expectations()
            {
                {
                    one(authService1).check();
                    one(authService2).check();
                }
            });
        stackedAuthService.check();
        context.assertIsSatisfied();
    }

    @Test
    public void testSupportsListingByUserIdFalse()
    {
        assertFalse(stackedAuthService.supportsListingByUserId());
        context.assertIsSatisfied();
    }

    @Test
    public void testSupportsListingByEmailFalse()
    {
        assertFalse(stackedAuthService.supportsListingByEmail());
        context.assertIsSatisfied();
    }

    @Test
    public void testSupportsListingByLastNameFalse()
    {
        assertFalse(stackedAuthService.supportsListingByLastName());
        context.assertIsSatisfied();
    }

    @Test
    public void testSupportsListingByUserIdTrue()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    one(authService1).supportsListingByLastName();
                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    will(returnValue(true));
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertTrue(stackedAuthService.supportsListingByUserId());
        context.assertIsSatisfied();
    }

    @Test
    public void testSupportsListingByEmailTrue()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    will(returnValue(true));
                    one(authService1).supportsListingByLastName();
                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertTrue(stackedAuthService.supportsListingByEmail());
        context.assertIsSatisfied();
    }

    @Test
    public void testSupportsListingByLastNameTrue()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    one(authService1).supportsListingByLastName();
                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                    will(returnValue(true));
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertTrue(stackedAuthService.supportsListingByLastName());
        context.assertIsSatisfied();
    }

    @Test
    public void testAuthenticateApplicationOK()
    {
        final String token1 = "token1";
        final String token2 = "token2";

        context.checking(new Expectations()
            {
                {
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));
                }
            });
        assertNotNull(stackedAuthService.authenticateApplication());
        context.assertIsSatisfied();
    }

    @Test
    public void testAuthenticateApplicationServiceOneFails()
    {
        context.checking(new Expectations()
            {
                {
                    one(authService1).authenticateApplication();
                    will(returnValue(null));
                }
            });
        assertNull(stackedAuthService.authenticateApplication());
        context.assertIsSatisfied();
    }

    @Test
    public void testAuthenticateApplicationServiceTwoFails()
    {
        final String token1 = "token1";

        context.checking(new Expectations()
            {
                {
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(null));
                }
            });
        assertNull(stackedAuthService.authenticateApplication());
        context.assertIsSatisfied();
    }

    @Test
    public void testAuthenticateUserFalse()
    {
        final String token1 = "token1";
        final String token2 = "token2";
        final String user = "user";
        final String password = "password";

        context.checking(new Expectations()
            {
                {
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));
                    one(authService1).tryGetAndAuthenticateUser(token1, user, password);
                    one(authService2).tryGetAndAuthenticateUser(token2, user, password);
                }
            });
        assertNotNull(stackedAuthService.authenticateApplication());
        assertFalse(stackedAuthService.authenticateUser("doesntmatter", user, password));
        context.assertIsSatisfied();
    }

    @Test
    public void testAuthenticateUserFirstServiceTrue()
    {
        final String token1 = "token1";
        final String token2 = "token2";
        final String user = "user";
        final String password = "password";
        final Principal principal = new Principal(user, "", "", "", true);

        context.checking(new Expectations()
            {
                {
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));
                    one(authService1).tryGetAndAuthenticateUser(token1, user, password);
                    will(returnValue(principal));
                }
            });
        assertNotNull(stackedAuthService.authenticateApplication());
        assertTrue(stackedAuthService.authenticateUser("doesntmatter", user, password));
        context.assertIsSatisfied();
    }

    @Test
    public void testAuthenticateUserSecondServiceTrue()
    {
        final String token1 = "token1";
        final String token2 = "token2";
        final String user = "user";
        final String password = "password";
        final Principal principal = new Principal(user, "", "", "", true);

        context.checking(new Expectations()
            {
                {
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));
                    one(authService1).tryGetAndAuthenticateUser(token1, user, password);
                    one(authService2).tryGetAndAuthenticateUser(token2, user, password);
                    will(returnValue(principal));
                }
            });
        assertNotNull(stackedAuthService.authenticateApplication());
        assertTrue(stackedAuthService.authenticateUser("doesntmatter", user, password));
        context.assertIsSatisfied();
    }

    @Test
    public void testGetPrincipalFirstService()
    {
        final String token1 = "token1";
        final String token2 = "token2";
        final String user = "user";
        final String firstName = "first name";
        final String lastName = "last name";
        final String email = "email address";
        final Principal principal = new Principal(user, firstName, lastName, email, false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));
                    one(authService1).tryGetAndAuthenticateUser(token1, user, null);
                    will(returnValue(principal));
                }
            });
        assertNotNull(stackedAuthService.authenticateApplication());
        assertEquals(principal, stackedAuthService.getPrincipal("doesntmatter", user));
        context.assertIsSatisfied();
    }

    @Test
    public void testGetPrincipalSecondService()
    {
        final String token1 = "token1";
        final String token2 = "token2";
        final String user = "user";
        final String firstName = "first name";
        final String lastName = "last name";
        final String email = "email address";
        final Principal principal = new Principal(user, firstName, lastName, email, false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));
                    one(authService1).tryGetAndAuthenticateUser(token1, user, null);
                    one(authService2).tryGetAndAuthenticateUser(token2, user, null);
                    will(returnValue(principal));
                }
            });
        assertNotNull(stackedAuthService.authenticateApplication());
        assertEquals(principal, stackedAuthService.getPrincipal("doesntmatter", user));
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetPrincipalApplicationNotAuthenticated()
    {
        final String user = "user";

        stackedAuthService.getPrincipal("doesntmatter", user);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testGetPrincipalNoService()
    {
        final String token1 = "token1";
        final String token2 = "token2";
        final String user = "user";

        context.checking(new Expectations()
            {
                {
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));
                    one(authService1).tryGetAndAuthenticateUser(token1, user, null);
                    one(authService2).tryGetAndAuthenticateUser(token2, user, null);
                }
            });
        assertNotNull(stackedAuthService.authenticateApplication());
        stackedAuthService.getPrincipal("doesntmatter", user);
    }

    @Test
    public void testListByEmailFirstService()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String token1 = "token1";
        final String token2 = "token2";
        final String emailQuery = "some email with *";
        final Principal principal1 =
                new Principal("user1", "first name 1", "last name 1", "email 1", false);
        final Principal principal2 =
                new Principal("user2", "first name 2", "last name 2", "email 2", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    exactly(2).of(authService1).supportsListingByEmail();
                    will(returnValue(true));
                    one(authService1).supportsListingByLastName();
                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    exactly(2).of(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));

                    one(authService1).listPrincipalsByEmail(token1, emailQuery);
                    will(returnValue(Arrays.asList(principal1, principal2)));
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertNotNull(stackedAuthService.authenticateApplication());
        final List<Principal> result =
                stackedAuthService.listPrincipalsByEmail("doesntmatter", emailQuery);
        assertEquals(2, result.size());
        assertEquals(principal1, result.get(0));
        assertEquals(principal2, result.get(1));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByEmailSecondService()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String token1 = "token1";
        final String token2 = "token2";
        final String emailQuery = "some email with *";
        final Principal principal =
                new Principal("user3", "first name 3", "last name 3", "email 3", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    exactly(2).of(authService1).supportsListingByEmail();
                    one(authService1).supportsListingByLastName();
                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    exactly(2).of(authService2).supportsListingByEmail();
                    will(returnValue(true));
                    one(authService2).supportsListingByLastName();
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));

                    one(authService2).listPrincipalsByEmail(token2, emailQuery);
                    will(returnValue(Arrays.asList(principal)));
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertNotNull(stackedAuthService.authenticateApplication());
        final List<Principal> result =
                stackedAuthService.listPrincipalsByEmail("doesntmatter", emailQuery);
        assertEquals(1, result.size());
        assertEquals(principal, result.get(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByEmailBothServicees()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String token1 = "token1";
        final String token2 = "token2";
        final String emailQuery = "some email with *";
        final Principal principal1 =
                new Principal("user1", "first name 1", "last name 1", "email 1", false);
        final Principal principal2 =
                new Principal("user2", "first name 2", "last name 2", "email 2", false);
        final Principal principal3 =
                new Principal("user3", "first name 3", "last name 3", "email 3", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    exactly(2).of(authService1).supportsListingByEmail();
                    will(returnValue(true));
                    one(authService1).supportsListingByLastName();
                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    exactly(2).of(authService2).supportsListingByEmail();
                    will(returnValue(true));
                    one(authService2).supportsListingByLastName();
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));

                    one(authService1).listPrincipalsByEmail(token1, emailQuery);
                    will(returnValue(Arrays.asList(principal1, principal2)));
                    one(authService2).listPrincipalsByEmail(token2, emailQuery);
                    will(returnValue(Arrays.asList(principal3)));
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertNotNull(stackedAuthService.authenticateApplication());
        final List<Principal> result =
                stackedAuthService.listPrincipalsByEmail("doesntmatter", emailQuery);
        assertEquals(3, result.size());
        assertEquals(principal1, result.get(0));
        assertEquals(principal2, result.get(1));
        assertEquals(principal3, result.get(2));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByUserIdFirstService()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String token1 = "token1";
        final String token2 = "token2";
        final String userIdQuery = "some user id with *";
        final Principal principal1 =
                new Principal("user1", "first name 1", "last name 1", "email 1", false);
        final Principal principal2 =
                new Principal("user2", "first name 2", "last name 2", "email 2", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    exactly(2).of(authService1).supportsListingByUserId();
                    will(returnValue(true));
                    one(authService1).supportsListingByEmail();
                    one(authService1).supportsListingByLastName();
                    one(authService2).isRemote();
                    exactly(2).of(authService2).supportsListingByUserId();
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));

                    one(authService1).listPrincipalsByUserId(token1, userIdQuery);
                    will(returnValue(Arrays.asList(principal1, principal2)));
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertNotNull(stackedAuthService.authenticateApplication());
        final List<Principal> result =
                stackedAuthService.listPrincipalsByUserId("doesntmatter", userIdQuery);
        assertEquals(2, result.size());
        assertEquals(principal1, result.get(0));
        assertEquals(principal2, result.get(1));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByuserIdSecondService()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String token1 = "token1";
        final String token2 = "token2";
        final String userIdQuery = "some user id with *";
        final Principal principal =
                new Principal("user3", "first name 3", "last name 3", "email 3", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    exactly(2).of(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    one(authService1).supportsListingByLastName();
                    one(authService2).isRemote();
                    exactly(2).of(authService2).supportsListingByUserId();
                    will(returnValue(true));
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));

                    one(authService2).listPrincipalsByUserId(token2, userIdQuery);
                    will(returnValue(Arrays.asList(principal)));
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertNotNull(stackedAuthService.authenticateApplication());
        final List<Principal> result =
                stackedAuthService.listPrincipalsByUserId("doesntmatter", userIdQuery);
        assertEquals(1, result.size());
        assertEquals(principal, result.get(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByUserIdBothServicees()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String token1 = "token1";
        final String token2 = "token2";
        final String userIdQuery = "some user id with *";
        final Principal principal1 =
                new Principal("user1", "first name 1", "last name 1", "email 1", false);
        final Principal principal2 =
                new Principal("user2", "first name 2", "last name 2", "email 2", false);
        final Principal principal3 =
                new Principal("user3", "first name 3", "last name 3", "email 3", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    exactly(2).of(authService1).supportsListingByUserId();
                    will(returnValue(true));
                    one(authService1).supportsListingByEmail();
                    one(authService1).supportsListingByLastName();
                    one(authService2).isRemote();
                    exactly(2).of(authService2).supportsListingByUserId();
                    will(returnValue(true));
                    one(authService2).supportsListingByEmail();
                    one(authService2).supportsListingByLastName();
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));

                    one(authService1).listPrincipalsByUserId(token1, userIdQuery);
                    will(returnValue(Arrays.asList(principal1, principal2)));
                    one(authService2).listPrincipalsByUserId(token2, userIdQuery);
                    will(returnValue(Arrays.asList(principal3)));
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertNotNull(stackedAuthService.authenticateApplication());
        final List<Principal> result =
                stackedAuthService.listPrincipalsByUserId("doesntmatter", userIdQuery);
        assertEquals(3, result.size());
        assertEquals(principal1, result.get(0));
        assertEquals(principal2, result.get(1));
        assertEquals(principal3, result.get(2));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByLastNameFirstService()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String token1 = "token1";
        final String token2 = "token2";
        final String lastNameQuery = "some user id with *";
        final Principal principal1 =
                new Principal("user1", "first name 1", "last name 1", "email 1", false);
        final Principal principal2 =
                new Principal("user2", "first name 2", "last name 2", "email 2", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    exactly(2).of(authService1).supportsListingByLastName();
                    will(returnValue(true));
                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    one(authService2).supportsListingByEmail();
                    exactly(2).of(authService2).supportsListingByLastName();
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));

                    one(authService1).listPrincipalsByLastName(token1, lastNameQuery);
                    will(returnValue(Arrays.asList(principal1, principal2)));
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertNotNull(stackedAuthService.authenticateApplication());
        final List<Principal> result =
                stackedAuthService.listPrincipalsByLastName("doesntmatter", lastNameQuery);
        assertEquals(2, result.size());
        assertEquals(principal1, result.get(0));
        assertEquals(principal2, result.get(1));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByLastNameSecondService()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String token1 = "token1";
        final String token2 = "token2";
        final String lastNameQuery = "some user id with *";
        final Principal principal =
                new Principal("user3", "first name 3", "last name 3", "email 3", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    exactly(2).of(authService1).supportsListingByLastName();
                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    one(authService2).supportsListingByEmail();
                    exactly(2).of(authService2).supportsListingByLastName();
                    will(returnValue(true));
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));

                    one(authService2).listPrincipalsByLastName(token2, lastNameQuery);
                    will(returnValue(Arrays.asList(principal)));
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertNotNull(stackedAuthService.authenticateApplication());
        final List<Principal> result =
                stackedAuthService.listPrincipalsByLastName("doesntmatter", lastNameQuery);
        assertEquals(1, result.size());
        assertEquals(principal, result.get(0));
        context.assertIsSatisfied();
    }

    @Test
    public void testListByLastNameBothServicees()
    {
        context = new Mockery();
        authService1 = context.mock(IAuthenticationService.class, "auth service 1");
        authService2 = context.mock(IAuthenticationService.class, "auth service 2");
        final String token1 = "token1";
        final String token2 = "token2";
        final String lastNameQuery = "some user id with *";
        final Principal principal1 =
                new Principal("user1", "first name 1", "last name 1", "email 1", false);
        final Principal principal2 =
                new Principal("user2", "first name 2", "last name 2", "email 2", false);
        final Principal principal3 =
                new Principal("user3", "first name 3", "last name 3", "email 3", false);

        context.checking(new Expectations()
            {
                {
                    one(authService1).isRemote();
                    one(authService1).supportsListingByUserId();
                    one(authService1).supportsListingByEmail();
                    exactly(2).of(authService1).supportsListingByLastName();
                    will(returnValue(true));
                    one(authService2).isRemote();
                    one(authService2).supportsListingByUserId();
                    one(authService2).supportsListingByEmail();
                    exactly(2).of(authService2).supportsListingByLastName();
                    will(returnValue(true));
                    one(authService1).authenticateApplication();
                    will(returnValue(token1));
                    one(authService2).authenticateApplication();
                    will(returnValue(token2));

                    one(authService1).listPrincipalsByLastName(token1, lastNameQuery);
                    will(returnValue(Arrays.asList(principal1, principal2)));
                    one(authService2).listPrincipalsByLastName(token2, lastNameQuery);
                    will(returnValue(Arrays.asList(principal3)));
                }
            });
        stackedAuthService =
                new StackedAuthenticationService(Arrays.asList(authService1, authService2));
        assertNotNull(stackedAuthService.authenticateApplication());
        final List<Principal> result =
                stackedAuthService.listPrincipalsByLastName("doesntmatter", lastNameQuery);
        assertEquals(3, result.size());
        assertEquals(principal1, result.get(0));
        assertEquals(principal2, result.get(1));
        assertEquals(principal3, result.get(2));
        context.assertIsSatisfied();
    }

}
