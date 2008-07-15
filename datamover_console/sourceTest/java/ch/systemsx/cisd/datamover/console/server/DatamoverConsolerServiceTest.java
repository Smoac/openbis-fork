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

package ch.systemsx.cisd.datamover.console.server;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.fail;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.datamover.console.client.EnvironmentFailureException;
import ch.systemsx.cisd.datamover.console.client.IDatamoverConsoleService;
import ch.systemsx.cisd.datamover.console.client.InvalidSessionException;
import ch.systemsx.cisd.datamover.console.client.dto.ApplicationInfo;
import ch.systemsx.cisd.datamover.console.client.dto.DatamoverInfo;
import ch.systemsx.cisd.datamover.console.client.dto.DatamoverStatus;
import ch.systemsx.cisd.datamover.console.client.dto.User;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=ConfigParameters.class)
public class DatamoverConsolerServiceTest
{
    private static final int EXPIRATION_TIME = 42;

    private static final String LOGGED_OUT_MESSAGE =
            "You are not logged in or your session has expired. Please log in.";
    
    private Mockery context;
    private IAuthenticationService authenticationService;
    private IRequestContextProvider requestContextProvider;
    private IDatamoverConsoleFactory consoleFactory;
    private IConsoleActionLog actionLog;
    private HttpServletRequest request;
    private HttpSession session;
    private IDatamoverConsole dm1;
    private IDatamoverConsole dm2;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        authenticationService = context.mock(IAuthenticationService.class);
        requestContextProvider = context.mock(IRequestContextProvider.class);
        consoleFactory = context.mock(IDatamoverConsoleFactory.class);
        actionLog = context.mock(IConsoleActionLog.class);
        request = context.mock(HttpServletRequest.class);
        session = context.mock(HttpSession.class);
        dm1 = context.mock(IDatamoverConsole.class, "datamover 1");
        dm2 = context.mock(IDatamoverConsole.class, "datamover 2");
    }
    
    @AfterMethod
    public final void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetApplicationInfo()
    {
        context.checking(new Expectations()
        {
            {
                one(dm1).check();
                one(dm2).check();
            }
        });
        ApplicationInfo applicationInfo = createService().getApplicationInfo();
        assertEquals(42000, applicationInfo.getRefreshTimeInterval());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testTryToGetCurrentUserUndefined()
    {
        prepareForNotAuthenticated();
        
        assertEquals(null, createService().tryToGetCurrentUser());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testTryToGetCurrentUser()
    {
        prepareForAuthenticated();
        final User user = new User();
        context.checking(new Expectations()
            {
                {
                    one(session).getAttribute(DatamoverConsoleService.SESSION_USER);
                    will(returnValue(user));
                }
            });
        
        assertSame(user, createService().tryToGetCurrentUser());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testTryToLoginForInvalidApplication()
    {
        context.checking(new Expectations()
            {
                {
                    one(dm1).check();
                    one(dm2).check();

                    one(authenticationService).authenticateApplication();
                    will(returnValue(null));
                    
                    one(actionLog).logFailedLoginAttempt("u");
                }
            });
        
        try
        {
            createService().tryToLogin("u", "p");
        } catch (EnvironmentFailureException e)
        {
            assertEquals("User 'u' failed to authenticate: application not authenticated.", 
                    e.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testTryToLoginWithInvalidCredentials()
    {
        final String userName = "user";
        final String password = "passwd";
        context.checking(new Expectations()
        {
            {
                one(dm1).check();
                one(dm2).check();
                
                one(authenticationService).authenticateApplication();
                String aToken = "atoken";
                will(returnValue(aToken));
                
                one(authenticationService).authenticateUser(aToken, userName, password);
                will(returnValue(false));
                
                one(actionLog).logFailedLoginAttempt(userName);
            }
        });
        
        assertEquals(null, createService().tryToLogin(userName, password));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testTryToLoginWithValidCredentials()
    {
        final String userName = "user";
        final String password = "passwd";
        final User user = new User();
        user.setUserCode("u");
        context.checking(new Expectations()
            {
                {
                    one(dm1).check();
                    one(dm2).check();
                    
                    one(authenticationService).authenticateApplication();
                    String aToken = "atoken";
                    will(returnValue(aToken));
                    
                    one(authenticationService).authenticateUser(aToken, userName, password);
                    will(returnValue(true));
                    
                    one(authenticationService).getPrincipal(aToken, userName);
                    will(returnValue(new Principal("u", "John", "Doe", "jd@u.a")));
                    
                    one(requestContextProvider).getHttpServletRequest();
                    will(returnValue(request));
                    
                    one(request).getSession(true);
                    will(returnValue(session));
                    
                    one(session).setMaxInactiveInterval(EXPIRATION_TIME * 60);
                    one(session).setAttribute(DatamoverConsoleService.SESSION_USER, user);
                    
                    one(actionLog).logSuccessfulLogin();
                }
            });
        
        final User actualUser = createService().tryToLogin(userName, password);
        assertEquals("u", actualUser.getUserCode());
        assertEquals("John Doe", actualUser.getUserFullName());
        assertEquals("jd@u.a", actualUser.getEmail());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testLogout()
    {
        prepareForAuthenticated();
        context.checking(new Expectations()
            {
                {
                    one(session).invalidate();
                }
            });
        
        createService().logout();
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testLogoutWithoutSession()
    {
        prepareForNotAuthenticated();
        
        createService().logout();
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListDatamoverInfos()
    {
        prepareForAuthenticated();
        IDatamoverConsoleService service = createService();
        context.checking(new Expectations()
            {
                {
                    one(dm1).tryObtainTarget();
                    will(returnValue("first target"));
                    one(dm1).obtainStatus();
                    will(returnValue(DatamoverStatus.PROCESSING));

                    one(dm2).tryObtainTarget();
                    will(returnValue("target1"));
                    one(dm2).obtainStatus();
                    will(returnValue(DatamoverStatus.SHUTDOWN));
                }
            });
        
        List<DatamoverInfo> infos = service.listDatamoverInfos();
        
        assertEquals(2, infos.size());
        DatamoverInfo info1 = infos.get(0);
        assertEquals("dm1", info1.getName());
        assertEquals("first target", info1.getTargetLocation());
        assertEquals(DatamoverStatus.PROCESSING, info1.getStatus());
        DatamoverInfo info2 = infos.get(1);
        assertEquals("dm2", info2.getName());
        assertEquals("target1", info2.getTargetLocation());
        assertEquals(DatamoverStatus.SHUTDOWN, info2.getStatus());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListDatamoverInfoWithError()
    {
        final String msg = "[first target]\nsomething's fishy";
        prepareForAuthenticated();
        IDatamoverConsoleService service = createService();
        context.checking(new Expectations()
            {
                {
                    one(dm1).tryObtainTarget();
                    will(returnValue("first target"));
                    one(dm1).obtainStatus();
                    will(returnValue(DatamoverStatus.ERROR));
                    one(dm1).tryObtainErrorMessage();
                    will(returnValue(msg));

                    one(dm2).tryObtainTarget();
                    will(returnValue("target1"));
                    one(dm2).obtainStatus();
                    will(returnValue(DatamoverStatus.IDLE));
                }
            });
        
        final List<DatamoverInfo> infos = service.listDatamoverInfos();
        
        assertEquals(2, infos.size());
        DatamoverInfo info1 = infos.get(0);
        assertEquals("dm1", info1.getName());
        assertEquals("first target", info1.getTargetLocation());
        assertEquals(DatamoverStatus.ERROR, info1.getStatus());
        assertEquals(msg, info1.getErrorMessage());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetTargets()
    {
        prepareForAuthenticated();
        IDatamoverConsoleService service = createService();
        Map<String, String> targets = service.getTargets();
        
        assertEquals("{t1=target1, t2=target2}", targets.toString());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testListDatamoverInfosNotAuthenticated()
    {
        prepareForNotAuthenticated();
        IDatamoverConsoleService service = createService();
        
        try
        {
            service.listDatamoverInfos();
            fail("InvalidSessionException expected");
        } catch (InvalidSessionException e)
        {
            assertEquals(LOGGED_OUT_MESSAGE, e.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testGetTargetsNotAuthenticated()
    {
        prepareForNotAuthenticated();
        IDatamoverConsoleService service = createService();
        
        try
        {
            service.getTargets();
            fail("InvalidSessionException expected");
        } catch (InvalidSessionException e)
        {
            assertEquals(LOGGED_OUT_MESSAGE, e.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testStartDatamover()
    {
        final String location = "new location";
        final String datamoverName = "dm1";
        prepareForAuthenticated();
        context.checking(new Expectations()
            {
                {
                    one(dm1).start(location);
                    one(actionLog).logStartDatamover(datamoverName, location);
                }
            });
        
        IDatamoverConsoleService service = createService();
        service.startDatamover(datamoverName, location);
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testStartUnknownDatamover()
    {
        prepareForAuthenticated();
        
        IDatamoverConsoleService service = createService();
        service.startDatamover("unknown", "new location");
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testStartDatamoverNotAuthenticated()
    {
        prepareForNotAuthenticated();
        IDatamoverConsoleService service = createService();
        
        try
        {
            service.startDatamover("dm1", "l2");
            fail("InvalidSessionException expected");
        } catch (InvalidSessionException e)
        {
            assertEquals(LOGGED_OUT_MESSAGE, e.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testShutdownDatamover()
    {
        final String datamoverName = "dm1";
        prepareForAuthenticated();
        context.checking(new Expectations()
            {
                {
                    one(dm1).shutdown();
                    one(actionLog).logShutdownDatamover(datamoverName);
                }
            });
        
        IDatamoverConsoleService service = createService();
        service.shutdownDatamover(datamoverName);
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testShutdownUnknownDatamover()
    {
        prepareForAuthenticated();
        
        IDatamoverConsoleService service = createService();
        service.shutdownDatamover("unknown");
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testShutdownDatamoverNotAuthenticated()
    {
        prepareForNotAuthenticated();
        IDatamoverConsoleService service = createService();
        
        try
        {
            service.shutdownDatamover("dm2");
            fail("InvalidSessionException expected");
        } catch (InvalidSessionException e)
        {
            assertEquals(LOGGED_OUT_MESSAGE, e.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    private void prepareForAuthenticated()
    {
        context.checking(new Expectations()
            {
                {
                    one(dm1).check();
                    one(dm2).check();
                    
                    one(requestContextProvider).getHttpServletRequest();
                    will(returnValue(request));
                    
                    one(request).getSession(false);
                    will(returnValue(session));
                }
            });
    }
    
    private void prepareForNotAuthenticated()
    {
        context.checking(new Expectations()
        {
            {
                one(dm1).check();
                one(dm2).check();
                
                one(requestContextProvider).getHttpServletRequest();
                will(returnValue(request));
                
                one(request).getSession(false);
                will(returnValue(null));
            }
        });
    }
    
    private IDatamoverConsoleService createService()
    {
        context.checking(new Expectations()
            {
                {
                    one(consoleFactory).create("dm1", "wd1");
                    will(returnValue(dm1));
                    one(consoleFactory).create("dm2", "wd2");
                    will(returnValue(dm2));
                }
            });
        DatamoverConsoleService service =
                new DatamoverConsoleService(authenticationService, requestContextProvider,
                        consoleFactory, actionLog);
        Properties properties = new Properties();
        properties.setProperty(ConfigParameters.REFRESH_TIME_INTERVAL, "42");
        properties.setProperty(ConfigParameters.TARGETS, "t1 t2");
        properties.setProperty("t1." + ConfigParameters.LOCATION, "target1");
        properties.setProperty("t2." + ConfigParameters.LOCATION, "target2");
        properties.setProperty(ConfigParameters.DATAMOVERS, "dm1 dm2");
        properties.setProperty("dm1." + ConfigParameters.WORKING_DIRECTORY, "wd1");
        properties.setProperty("dm2." + ConfigParameters.WORKING_DIRECTORY, "wd2");
        service.setConfigParameters(new ConfigParameters(properties));
        service.setSessionExpirationPeriodInMinutes(EXPIRATION_TIME);
        return service;
    }

}
