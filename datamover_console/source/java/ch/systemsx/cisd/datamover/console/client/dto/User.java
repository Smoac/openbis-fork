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

package ch.systemsx.cisd.datamover.console.client.dto;

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
    private String userFullName;

    /**
     * Email Address of the user.
     * <p>
     * We are sure that this key is never <code>null</code>.
     * </p>
     */
    private String email;

    /**
     * Unique identifier of the user.
     * <p>
     * We are sure that this key is unique and never <code>null</code>. If no
     * <code>userCode</code> is specified, the email address is used as userCode.
     * </p>
     */
    private String userCode;

    public final String getEmail()
    {
        return email;
    }

    public final void setEmail(final String email)
    {
        this.email = email;
    }

    public final String getUserFullName()
    {
        return userFullName;
    }

    public final void setUserFullName(final String userFullName)
    {
        this.userFullName = userFullName;
    }

    public String getUserCode()
    {
        return userCode;
    }

    public void setUserCode(String userCode)
    {
        this.userCode = userCode;
    }

    //
    // Object
    //

    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof User == false)
        {
            return false;
        }
        final User that = (User) obj;
        return that.userCode.equals(userCode);
    }

    public final int hashCode()
    {
        return userCode.hashCode();
    }

    public final String toString()
    {
        return userCode;
    }
}