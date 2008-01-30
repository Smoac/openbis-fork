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

package ch.systemsx.cisd.cifex.client.dto;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A small class that describes an user.
 * 
 * @author Christian Ribeaud
 */
public final class User implements IsSerializable
{
    /**
     * User name.
     * <p>
     * Could be <code>null</code> if not defined.
     * </p>
     */
    private String userName;

    /**
     * Unique identifier in the database.
     * <p>
     * We are sure that this key is unique and not <code>null</code>.
     * </p>
     */
    private String email;

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

    private Date expirationDate;
    
    private User registrator;

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
        this.email = email;
    }

    public final boolean isPermanent()
    {
        return permanent;
    }

    public final void setPermanent(final boolean permanent)
    {
        this.permanent = permanent;
    }

    public final String getUserName()
    {
        return userName;
    }

    public final void setUserName(final String userID)
    {
        this.userName = userID;
    }

    public final Date getExpirationDate()
    {
        return expirationDate;
    }

    public final void setExpirationDate(final Date expirationDate)
    {
        this.expirationDate = expirationDate;
    }

    public final User getRegistrator()
    {
        return registrator;
    }

    public final void setRegistrator(User registrator)
    {
        this.registrator = registrator;
    }

}