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

package ch.systemsx.cisd.openbis.systemtest;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import org.apache.log4j.Level;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class SetSessionUserTest extends SystemTestCase
{
    private BufferedAppender logRecorder;

    @BeforeMethod
    public void setUp()
    {
        logRecorder = new BufferedAppender("%m%n", Level.INFO);
    }
    
    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
    }

    @Test
    public void testNotInstanceAdmin()
    {
        
        SessionContextDTO session = commonServer.tryToAuthenticate("observer", "a");
        String sessionToken = session.getSessionToken();
        try
        {
            commonServer.setSessionUser(sessionToken, "test");
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals("Authorization failure: None of method roles '[INSTANCE.ADMIN]' " +
            		"could be found in roles of user 'observer'.", ex.getMessage());
        }
    }
    
    @Test
    public void testUnkownUser()
    {
        
        SessionContextDTO session = commonServer.tryToAuthenticate("test", "a");
        String sessionToken = session.getSessionToken();
        try
        {
            commonServer.setSessionUser(sessionToken, "dontKnow");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Unknown user: dontKnow", ex.getMessage());
        }
    }
    
    @Test
    public void testLogging()
    {
        
        SessionContextDTO session = commonServer.tryToAuthenticate("test", "a");
        String sessionToken = session.getSessionToken();
        
        commonServer.setSessionUser(sessionToken, "observer");
        
        String[] logContent = logRecorder.getLogContent().split("\n");
        assertEquals(3, logContent.length);
        String logLine = logContent[2];
        assertTrue("Following log line does start as expected: " + logLine, logLine
                .startsWith("[USER:'test' SPACE:'CISD' HOST:'localhost'"));
        assertTrue("Following log line does end as expected: " + logLine, logLine
                .endsWith("set_session_user  USER('observer')"));      
        
        commonServer.logout(sessionToken);
        
        System.out.println(logRecorder.getLogContent());
        logContent = logRecorder.getLogContent().split("\n");
        assertEquals(5, logContent.length);
        logLine = logContent[4];
        assertEquals("LOGOUT: Session '" + sessionToken + "' of user 'observer' has been closed.",
                logLine);
    }
    
    @Test
    public void testAuthorization()
    {
        
        SessionContextDTO session = commonServer.tryToAuthenticate("test", "a");
        String sessionToken = session.getSessionToken();
        commonServer.setSessionUser(sessionToken, "test");
        commonServer.setSessionUser(sessionToken, "observer"); // allowed because still user 'test'
        try
        {
            // not allowed because user 'observer' has no INSTANCE ADMIN rights
            commonServer.setSessionUser(sessionToken, "observer");
            fail("AuthorizationFailureException expected");
        } catch (AuthorizationFailureException ex)
        {
            assertEquals("Authorization failure: None of method roles '[INSTANCE.ADMIN]' " +
                    "could be found in roles of user 'observer'.", ex.getMessage());
        }
    }
}
