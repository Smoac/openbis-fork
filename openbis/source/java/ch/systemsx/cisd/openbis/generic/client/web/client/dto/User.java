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

/**
 * User information to be shown in Web client.
 * 
 * @author Franz-Josef Elmer
 */
public final class User implements IsSerializable
{
    private String userName;

    private String homeGroupCode;

    private String userEmail;

    public User()
    {
    }

    public User(String userName, String homeGroupCodeOrNull, String userEmail)
    {
        this.userName = userName;
        this.homeGroupCode = homeGroupCodeOrNull;
        this.userEmail = userEmail;
    }

    /** can be null */
    public final String getHomeGroupCode()
    {
        return homeGroupCode;
    }

    public final void setHomeGroupCode(final String homeGroupCode)
    {
        this.homeGroupCode = homeGroupCode;
    }

    public final String getUserName()
    {
        return userName;
    }

    public final void setUserName(final String userName)
    {
        this.userName = userName;
    }

    public void setUserEmail(String userEmail)
    {
        this.userEmail = userEmail;
    }

    public String getUserEmail()
    {
        return userEmail;
    }

}
