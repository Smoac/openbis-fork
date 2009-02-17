/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.upload.server;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class UploadSessionManagerTest extends AssertJUnit
{
    @Test
    public void testDifferentSessionIDs()
    {
        UploadSessionManager manager = new UploadSessionManager(false);
        UserDTO user = new UserDTO();
        user.setUserCode("user");
        UploadSession session1 = manager.createSession(user, "url");
        UploadSession session2 = manager.createSession(user, "url");
        
        assertSame(user, session1.getUser());
        assertEquals("url", session1.getUrl());
        String session1ID = session1.getSessionID();
        String session2ID = session2.getSessionID();
        assertFalse("different session IDs expected: " + session1ID + " " + session2ID, session1ID
                .equals(session2ID));
    }
    
    @Test
    public void testGetSession()
    {
        UploadSessionManager manager = new UploadSessionManager(false);
        UserDTO user = new UserDTO();
        user.setUserCode("user");
        UploadSession session = manager.createSession(user, "url");
        
        assertSame(session, manager.getSession(session.getSessionID()));
    }
    
    @Test
    public void testGetNonExistingSession()
    {
        UploadSessionManager manager = new UploadSessionManager(false);
        try
        {
            manager.getSession("session-id");
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("No upload session found for ID session-id", ex.getMessage());
        }
    }
    
    @Test
    public void testRemoveSession()
    {
        UploadSessionManager manager = new UploadSessionManager(false);
        UserDTO user = new UserDTO();
        user.setUserCode("user");
        UploadSession session = manager.createSession(user, "url");
        String sessionID = session.getSessionID();
        assertSame(session, manager.getSession(sessionID));
        
        manager.removeSession(sessionID);
        
        try
        {
            manager.getSession(sessionID);
            fail("EnvironmentFailureException expected");
        } catch (EnvironmentFailureException ex)
        {
            assertEquals("No upload session found for ID " + sessionID, ex.getMessage());
        }
        
        
    }
}
