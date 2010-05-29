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

package ch.systemsx.cisd.cifex.shared.basic.dto;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.cifex.shared.basic.Constants;

/**
 * Basic information about a user.
 * 
 * @author Bernd Rinn
 */
public class BasicUserInfoDTO implements IsSerializable, Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     * Unique identifier of the user in the database.
     */
    private long id;

    /**
     * Unique identifier of the user.
     * <p>
     * We are sure that this key is unique and never <code>null</code>. If no <code>userCode</code>
     * is specified, the email address is used as userCode.
     * </p>
     */
    private String userCode;

    /**
     * User name.
     * <p>
     * Could be <code>null</code> if not defined.
     * </p>
     */
    private String userFullName;

    /**
     * Email address of the user.
     * <p>
     * We are sure that this key is never <code>null</code>.
     * </p>
     */
    private String email;

    public void setID(long id)
    {
        this.id = id;
    }

    public long getID()
    {
        return id;
    }

    public String getUserCode()
    {
        return userCode;
    }

    public void setUserCode(String userCode)
    {
        this.userCode = userCode;
    }

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

    public void updateFrom(final BasicUserInfoDTO updateUser)
    {
        setUserCode(updateUser.getUserCode());
        setUserFullName(updateUser.getUserFullName());
        setEmail(updateUser.getEmail());
    }
    
    //
    // Object
    //

    @Override
    public final boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        if (obj instanceof BasicUserInfoDTO == false)
        {
            return false;
        }
        final BasicUserInfoDTO that = (BasicUserInfoDTO) obj;
        return that.userCode.equals(userCode);
    }

    @Override
    public final int hashCode()
    {
        return userCode.hashCode();
    }

    @Override
    public final String toString()
    {
        return userCode;
    }

    /**
     * Concatenates the user codes of <var>users</var>, separated by ",".
     */
    public final static String concatUserCodes(final BasicUserInfoDTO[] users)
    {
        assert users != null : "Unspecified user.";
    
        if (users.length == 0)
        {
            return Constants.TABLE_NULL_VALUE;
        }
        String anchor = "";
        for (int i = 0; i < users.length; ++i)
        {
            if (i < users.length - 1)
            {
                anchor += users[i].getUserCode() + ", ";
            } else
            {
                anchor += users[i].getUserCode();
            }
        }
        return anchor;
    }

}
