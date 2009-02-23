/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.rpc.client.cli;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Authentication credentials.
 * 
 * @author Christian Ribeaud
 */
public class Credentials
{
    private final static char USER_NAME_PASSWORD_SEP_CHAR = ':';

    private final String user;

    private final String password;

    /**
     * Creates a new instance of <code>Credentials</code>.
     * <p>
     * Note that <var>user</var> can not be <code>null</code>.
     * </p>
     */
    public Credentials(String user, String password)
    {
        this.user = user;
        this.password = password;
    }

    /**
     * The constructor with the username and password combined string argument.
     * 
     * @param usernamePassword the username:password formed string
     * @see #toString
     */
    public Credentials(String usernamePassword)
    {
        this(extractCredentials(usernamePassword)[0], extractCredentials(usernamePassword)[1]);
    }

    private final static String[] extractCredentials(String usernamePassword)
    {
        int atColon = usernamePassword.indexOf(USER_NAME_PASSWORD_SEP_CHAR);
        String userName = null;
        String password = null;
        if (atColon >= 0)
        {
            userName = usernamePassword.substring(0, atColon);
            password = usernamePassword.substring(atColon + 1);
        } else
        {
            userName = usernamePassword;
        }
        return new String[]
            { userName, password };
    }

    /** Returns the password. */
    public final String getPassword()
    {
        return password;
    }

    /** Return the user name. */
    public final String getUserName()
    {
        return user;
    }

    //
    // Object
    //

    /**
     * Get this object string.
     * 
     * @return the username:password formed string
     */
    @Override
    public String toString()
    {
        final StringBuffer result = new StringBuffer();
        result.append(getUserName());
        result.append(USER_NAME_PASSWORD_SEP_CHAR);
        result.append(String.valueOf(getPassword()));
        return result.toString();
    }

    /**
     * Does a hash of both user name and password.
     * 
     * @return The hash code including user name and password.
     */
    @Override
    public int hashCode()
    {
        final HashCodeBuilder hashCode = new HashCodeBuilder();
        hashCode.append(getUserName());
        hashCode.append(getPassword());
        return hashCode.toHashCode();
    }

    /**
     * These credentials are assumed equal if the username and password are the same.
     * 
     * @param o The other object to compare with.
     * @return <code>true</code> if the object is equivalent.
     */
    @Override
    public boolean equals(final Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (this == o)
        {
            return true;
        }
        // note - to allow for sub-classing, this checks that class is the same
        // rather than do "instanceof".
        if (this.getClass().equals(o.getClass()))
        {
            final Credentials that = (Credentials) o;
            final EqualsBuilder equalsBuilder = new EqualsBuilder();
            equalsBuilder.append(that.getUserName(), getUserName());
            equalsBuilder.append(that.getPassword(), getPassword());
            return equalsBuilder.isEquals();
        }
        return false;
    }
}