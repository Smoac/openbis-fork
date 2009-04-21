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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SessionContext implements IsSerializable
{
    private User user;
    private String sessionID;
    private DisplaySettings displaySettings;

    public final User getUser()
    {
        return user;
    }

    public final void setUser(User user)
    {
        this.user = user;
    }

    public final String getSessionID()
    {
        return sessionID;
    }

    public final void setSessionID(String sessionID)
    {
        this.sessionID = sessionID;
    }

    public final DisplaySettings getDisplaySettings()
    {
        return displaySettings;
    }

    public final void setDisplaySettings(DisplaySettings displaySettings)
    {
        this.displaySettings = displaySettings;
    }

    
}
