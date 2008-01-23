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

package ch.systemsx.cisd.cifex.server.business.dto;

import java.util.Date;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class UserDTO extends ID
{
    private String userName;
    
    private String email;
    
    private String encryptedPassword;
    
    private boolean admin;
    
    private boolean permanent;
    
    private Date registrationDate;
    
    private Date expirationDate;

    public final boolean isAdmin()
    {
        return admin;
    }

    public final void setAdmin(boolean admin)
    {
        this.admin = admin;
    }

    public final String getEmail()
    {
        return email;
    }

    public final void setEmail(String email)
    {
        this.email = email;
    }

    public final String getEncryptedPassword()
    {
        return encryptedPassword;
    }

    public final void setEncryptedPassword(String encryptedPassword)
    {
        this.encryptedPassword = encryptedPassword;
    }

    public final boolean isPermanent()
    {
        return permanent;
    }

    public final void setPermanent(boolean permanent)
    {
        this.permanent = permanent;
    }

    public final String getUserName()
    {
        return userName;
    }

    public final void setUserName(String userID)
    {
        this.userName = userID;
    }

    public final Date getExpirationDate()
    {
        return expirationDate;
    }

    public final void setExpirationDate(Date expirationDate)
    {
        this.expirationDate = expirationDate;
    }

    public final Date getRegistrationDate()
    {
        return registrationDate;
    }

    public final void setRegistrationDate(Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }
    
}
