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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import java.io.Serializable;

import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * An extract from the Session object, used internally on the server side.
 * 
 * @author Tomasz Pylak
 */
public class SessionContextDTO implements Serializable
{
    private static final long serialVersionUID = IServer.VERSION;

    private String sessionToken;

    private String userName;

    private DisplaySettings displaySettings;

    private String homeGroupCodeOrNull;

    private int sessionExpirationTime;

    private String userEmail;

    public void setSessionToken(String sessionToken)
    {
        this.sessionToken = sessionToken;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public void setDisplaySettings(DisplaySettings displaySettings)
    {
        this.displaySettings = displaySettings;
    }

    public void setUserEmail(String userEmail)
    {
        this.userEmail = userEmail;
    }

    public void setHomeGroupCode(String homeGroupCodeOrNull)
    {
        this.homeGroupCodeOrNull = homeGroupCodeOrNull;
    }

    public void setSessionExpirationTime(int sessionExpirationTime)
    {
        this.sessionExpirationTime = sessionExpirationTime;
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    public String getUserName()
    {
        return userName;
    }

    public DisplaySettings getDisplaySettings()
    {
        return displaySettings;
    }

    public String tryGetHomeGroupCode()
    {
        return homeGroupCodeOrNull;
    }

    public int getSessionExpirationTime()
    {
        return sessionExpirationTime;
    }

    public String getUserEmail()
    {
        return userEmail;
    }

}