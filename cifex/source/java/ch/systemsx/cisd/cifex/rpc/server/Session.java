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

import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * A session for rpc based calls.
 * 
 * @author Franz-Josef Elmer
 */
public final class Session
{
    private final String sessionID;

    private final UserDTO user;

    private final String url;

    private long lastActiveMillis;

    Session(String sessionID, UserDTO user, String url)
    {
        this.sessionID = sessionID;
        this.user = user;
        this.url = url;
        touchSession();
    }

    public String getSessionID()
    {
        return sessionID;
    }

    public UserDTO getUser()
    {
        return user;
    }

    final String getUrl()
    {
        return url;
    }

    public long getLastActiveMillis()
    {
        return lastActiveMillis;
    }

    public void touchSession()
    {
        lastActiveMillis = System.currentTimeMillis();
    }

}
