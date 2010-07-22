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

package ch.systemsx.cisd.authentication.file;

import static org.testng.AssertJUnit.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test cases for the {@link FileAuthenticationService}. 
 *
 * @author Bernd Rinn
 */
public class FileAuthenticationServiceTest
{

    private Mockery context;

    private IUserStore userStore;
    
    private FileAuthenticationService authService;

    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        userStore = context.mock(IUserStore.class);
        authService = new FileAuthenticationService(userStore);
    }

    @AfterMethod
    public void tearDown()
    {
        context.assertIsSatisfied();
    }

    @Test
    public void testAuthenticateUserSuccess()
    {
        final String user = "User";
        final String password = "passw0rd";
        context.checking(new Expectations()
        {
            {
                one(userStore).isPasswordCorrect(user, password);
                will(returnValue(true));
            }
        });
        assertTrue(authService.authenticateUser("doesntmatter", user, password));
        context.assertIsSatisfied();
    }
    
    @Test
    public void testAuthenticateUserFailure()
    {
        final String user = "User";
        final String password = "passw0rd";
        context.checking(new Expectations()
        {
            {
                one(userStore).isPasswordCorrect(user, password);
                will(returnValue(false));
            }
        });
        assertFalse(authService.authenticateUser("doesntmatter", user, password));
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetPrincipalSuccess()
    {
        final String uid = "uid";
        final UserEntry user = new UserEntry(uid, "email", "first", "last", "pwd");
        context.checking(new Expectations()
        {
            {
                one(userStore).tryGetUser(uid);
                will(returnValue(user));
            }
        });
        assertEquals(user.asPrincipal(), authService.getPrincipal("doesntmatter", uid));
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetPrincipalFailure()
    {
        final String uid = "uid";
        context.checking(new Expectations()
        {
            {
                one(userStore).tryGetUser(uid);
                will(returnValue(null));
            }
        });
        try
        {
            authService.getPrincipal("doesntmatter", uid);
            fail("Unknown user went undetected.");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Cannot find user 'uid'.", ex.getMessage());
        }
        context.assertIsSatisfied();
    }
    
}
