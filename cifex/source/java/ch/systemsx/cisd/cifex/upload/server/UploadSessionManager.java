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

import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.TokenGenerator;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class UploadSessionManager
{
    private final TokenGenerator tokenGenerator = new TokenGenerator();
    private final Map<String, UploadSession> sessions = new HashMap<String, UploadSession>();
    
    public UploadSession createSession(UserDTO user, String url)
    {
        String uploadSessionID = tokenGenerator.getNewToken(System.currentTimeMillis());
        UploadSession uploadSession = new UploadSession(uploadSessionID, user, url);
        sessions.put(uploadSessionID, uploadSession);
        return uploadSession;
    }
    
    public UploadSession getSession(String uploadSessionID)
    {
        UploadSession uploadSession = sessions.get(uploadSessionID);
        if (uploadSession == null)
        {
            throw new EnvironmentFailureException("No upload session found for ID " + uploadSessionID);
        }
        return uploadSession;
    }
    
    public void removeSession(String uploadSessionID)
    {
        sessions.remove(uploadSessionID);
    }
}
