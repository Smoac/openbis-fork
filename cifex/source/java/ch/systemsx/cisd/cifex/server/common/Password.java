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

package ch.systemsx.cisd.cifex.server.common;

import org.apache.commons.lang.StringUtils;
import ch.systemsx.cisd.common.utilities.PasswordHasher;
import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
 * Stores a password and allows to match it against a password hash stored in the internal user
 * database.
 * 
 * @author Bernd Rinn
 */
public final class Password
{
    private final String plainPasswordOrNull;

    /**
     * Returns <code>true</code>, if the two passwords are equal to each other and
     * <code>false</code> otherwise.
     */
    public static boolean equals(Password password1, Password password2)
    {
        if (isEmpty(password1))
        {
            return isEmpty(password2);
        }
        return password1.equals(password2);
    }

    /**
     * Returns <code>true</code>, if the password is empty.
     */
    public static boolean isEmpty(Password password)
    {
        return password == null || StringUtils.isBlank(password.plainPasswordOrNull);
    }

    public Password(String plainPasswordOrNull)
    {
        this.plainPasswordOrNull = plainPasswordOrNull;
    }

    /**
     * Returns the plain password or <code>null</code>, if the password is not set.
     */
    public String tryGetPlain()
    {
        return plainPasswordOrNull;
    }
    
    /**
     * Creates a hash for the password.
     * 
     * @throws IllegalStateException If this password is empty.
     */
    public String createPasswordHash() throws IllegalStateException
    {
        if (isEmpty(this))
        {
            throw new IllegalStateException("Cannot compute password hash of null password.");
        }
        return PasswordHasher.computeSaltedHash(plainPasswordOrNull);
    }

    /**
     * Returns <code>true</code>, if the <var>passwordHash</var> matches this password and
     * <code>false</code> otherwise. Note that an empty password or hash is never matched.
     */
    public boolean matches(String passwordHash)
    {
        if (isEmpty(this) || StringUtils.isBlank(passwordHash))
        {
            return false;
        }
        if (PasswordHasher.isPasswordCorrect(plainPasswordOrNull, passwordHash))
        {
            return true;
        }
        // Check legacy password hash.
        if (StringUtils.equals(StringUtilities.computeMD5Hash(plainPasswordOrNull), passwordHash))
        {
            return true;
        }
        return false;
    }

    //
    // Object
    //

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || obj instanceof Password == false)
        {
            return false;
        }
        final Password that = (Password) obj;
        if (plainPasswordOrNull == null)
        {
            return (that.plainPasswordOrNull == null);
        }
        return plainPasswordOrNull.equals(that.plainPasswordOrNull);
    }

    @Override
    public int hashCode()
    {
        if (plainPasswordOrNull == null)
        {
            return 23;
        }
        return plainPasswordOrNull.hashCode();
    }

}
