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

import static ch.systemsx.cisd.datamover.console.server.DatamoverConsoleService.SESSION_USER;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;
import ch.systemsx.cisd.datamover.console.client.dto.User;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ActionLogTest 
{
    private static final String SESSION_ID = "session-id";
    private static final String HOST = "test-host";
    private static final String USER = "AE";
    
    private Mockery context;
    private IRequestContextProvider requestContextProvider;
    private HttpServletRequest request;
    private HttpSession session;
    private ActionLog actionLog;
    
    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        requestContextProvider = context.mock(IRequestContextProvider.class);
        request = context.mock(HttpServletRequest.class);
        session = context.mock(HttpSession.class);
        
        context.checking(new Expectations()
            {
                {
                    allowing(requestContextProvider).getHttpServletRequest();
                    will(returnValue(request));
                    
                    allowing(request).getRemoteHost();
                    will(returnValue(HOST));
                }
            });
        actionLog = new ActionLog(requestContextProvider);
    }
    
    @Test
    public void testLogStartDatamover()
    {
        prepare();
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.TRACKING, "{USER: " + USER
                        + ", HOST: " + HOST + ", WEBSESSION: " + SESSION_ID
                        + "} Start Datamover 'dm1' with target 't1'");
        
        actionLog.logStartDatamover("dm1", "t1");
        
        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }
    
    @Test
    public void testLogShutdownDatamover()
    {
        prepare();
        LogMonitoringAppender appender =
                LogMonitoringAppender.addAppender(LogCategory.TRACKING, "{USER: " + USER
                        + ", HOST: " + HOST + ", WEBSESSION: " + SESSION_ID
                        + "} Shutdown Datamover 'dm1'");
        
        actionLog.logShutdownDatamover("dm1");
        
        appender.verifyLogHasHappened();
        context.assertIsSatisfied();
    }

    private void prepare()
    {
        context.checking(new Expectations()
            {
                {
                    one(request).getSession();
                    will(returnValue(session));
                    
                    one(session).getId();
                    will(returnValue(SESSION_ID));
                    
                    one(session).getAttribute(SESSION_USER);
                    final User user = new User();
                    user.setUserCode(USER);
                    will(returnValue(user));
                }
            });
    }

}
