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
 * An <code>ID</code> extension which describes an user in the database.
 * 
 * @author Franz-Josef Elmer
 */
public class UserDTO extends ID
{
    private static final long serialVersionUID = 1L;

    private String userFullName;

    /**
     * Email Address of the user. Is not Unique!
     */
    private String email;

    private String encryptedPassword;

    private UserDTO registratorOrNull;

    /**
     * Whether this user is an administrator or not.
     * <p>
     * Note that an administrator is a <i>permanent</i> user as well.
     * </p>
     */
    private boolean admin;

    /**
     * Whether this user is <i>permanent</i> or not.
     * <p>
     * A non-permanent user has a non-<code>null</code> {@link #expirationDate} value.
     * </p>
     */
    private boolean permanent;

    private boolean externallyAuthenticated;

    private Date registrationDate;

    private Date expirationDate;

    /**
     * The unique userCode of the <code>UserDTO</code>.
     */
    private String userCode;

    public final boolean isExternallyAuthenticated()
    {
        return externallyAuthenticated;
    }

    public final void setExternallyAuthenticated(final boolean externallyAuthenticated)
    {
        this.externallyAuthenticated = externallyAuthenticated;
    }

    public final boolean isAdmin()
    {
        return admin;
    }

    public final void setAdmin(final boolean admin)
    {
        this.admin = admin;
    }

    public final String getEmail()
    {
        return email;
    }

    public final void setEmail(final String email)
    {
        this.email = email.toLowerCase();
    }

    public final String getEncryptedPassword()
    {
        return encryptedPassword;
    }

    public final void setEncryptedPassword(final String encryptedPassword)
    {
        this.encryptedPassword = encryptedPassword;
    }

    public final boolean isPermanent()
    {
        return permanent;
    }

    public final void setPermanent(final boolean permanent)
    {
        this.permanent = permanent;
    }

    public final String getUserFullName()
    {
        return userFullName;
    }

    public final void setUserFullName(final String fullName)
    {
        this.userFullName = fullName;
    }

    public final Date getExpirationDate()
    {
        return expirationDate;
    }

    public final void setExpirationDate(final Date expirationDate)
    {
        this.expirationDate = expirationDate;
    }

    public final Date getRegistrationDate()
    {
        return registrationDate;
    }

    public final void setRegistrationDate(final Date registrationDate)
    {
        this.registrationDate = registrationDate;
    }

    public final UserDTO getRegistrator()
    {
        return registratorOrNull;
    }

    public final void setRegistrator(UserDTO registrator)
    {
        this.registratorOrNull = registrator;
    }

    public String getUserCode()
    {
        return userCode;
    }

    public void setUserCode(String userCode)
    {
        this.userCode = userCode;
    }

}
