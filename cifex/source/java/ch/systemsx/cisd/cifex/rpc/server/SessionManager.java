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

package ch.systemsx.cisd.cifex.rpc.server;

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.utilities.TokenGenerator;

/**
 * Manager of {@link Session} instances.
 *
 * @author Franz-Josef Elmer
 */
public class SessionManager
{
    private final boolean testMode;
    private final TokenGenerator tokenGenerator = new TokenGenerator();
    private final Map<String, Session> sessions = new HashMap<String, Session>();
    
    private int nextID;
    
    public SessionManager(boolean testMode)
    {
        this.testMode = testMode;
    }
    
    /**
     * Creates a new session for the specified user and URL.
     */
    public Session createSession(UserDTO user, String url)
    {
        String uploadSessionID =
                testMode ? Integer.toString(nextID++) : tokenGenerator.getNewToken(System
                        .currentTimeMillis());
        Session uploadSession = new Session(uploadSessionID, user, url);
        sessions.put(uploadSessionID, uploadSession);
        return uploadSession;
    }
    
    /**
     * Retrieves the session for the specified ID.
     * 
     * @throws EnvironmentFailureException if no session could be found.
     */
    public Session getSession(String uploadSessionID) throws InvalidSessionException
    {
        Session uploadSession = sessions.get(uploadSessionID);
        if (uploadSession == null)
        {
            throw new InvalidSessionException("No session found for ID " + uploadSessionID);
        }
        return uploadSession;
    }
    
    /**
     * Removes the session for the specified ID.
     */
    public void removeSession(String uploadSessionID)
    {
        sessions.remove(uploadSessionID);
    }
}
