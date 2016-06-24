/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.dss.generic.server.cifs;

import org.alfresco.jlan.server.auth.ClientInfo;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class OpenBisClientInfo extends ClientInfo
{
    private String sessionToken;

    OpenBisClientInfo(String user, byte[] pwd)
    {
        super(user, pwd);
        
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    void setSessionToken(String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    @Override
    public String toString()
    {
        return super.toString() + ", sessionToken: " + sessionToken;
    }

    
}
