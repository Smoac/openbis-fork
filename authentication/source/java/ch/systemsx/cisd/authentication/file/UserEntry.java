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

package ch.systemsx.cisd.authentication.file;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.common.utilities.PasswordHasher;

/**
 * A class representing a user entry in the password file.
 * 
 * @author Bernd Rinn
 */
class UserEntry extends AbstractHashable
{
    private static final int NUMBER_OF_COLUMNS_IN_PASSWORD_FILE = 5;

    private static final int USER_ID_IDX = 0;

    private static final int EMAIL_IDX = 1;

    private static final int FIRST_NAME_IDX = 2;

    private static final int LAST_NAME_IDX = 3;

    private static final int PASSWORD_IDX = 4;

    private final String[] passwordFileEntry;

    UserEntry(String passwordFileLine) throws IllegalArgumentException
    {
        this.passwordFileEntry = split(passwordFileLine);
    }

    private static String[] split(String passwordFileLine) throws IllegalArgumentException
    {
        assert passwordFileLine != null;

        final String[] entry = passwordFileLine.split(":");
        if (entry.length != NUMBER_OF_COLUMNS_IN_PASSWORD_FILE)
        {
            final String msg =
                    String.format("Password line is ill-formatted: '%s'", passwordFileLine);
            throw new IllegalArgumentException(msg);
        }
        return entry;
    }

    UserEntry(String userId, String email, String firstName, String lastName, String password)
    {
        assert userId != null;
        assert email != null;
        assert firstName != null;
        assert lastName != null;
        assert password != null;

        this.passwordFileEntry = new String[]
            { userId, email, firstName, lastName, PasswordHasher.computeSaltedHash(password) };
    }

    /**
     * Returns the password line suitable for putting into the password file.
     */
    String asPasswordLine()
    {
        return StringUtils.join(passwordFileEntry, ':');
    }

    /**
     * Returns the user entry as {@link Principal}.
     */
    Principal asPrincipal()
    {
        return new Principal(getUserId(), getFirstName(), getLastName(), getEmail(), false);
    }

    /**
     * Returns the user id.
     */
    String getUserId()
    {
        return passwordFileEntry[USER_ID_IDX];
    }

    /**
     * Returns the email address.
     */
    String getEmail()
    {
        return passwordFileEntry[EMAIL_IDX];
    }

    /**
     * Sets the email address of the user. <var>email</var> can be blank, but must not be
     * <code>null</code>.
     */
    void setEmail(String email)
    {
        assert email != null;

        passwordFileEntry[EMAIL_IDX] = email;
    }

    /**
     * Returns the first name.
     */
    String getFirstName()
    {
        return passwordFileEntry[FIRST_NAME_IDX];
    }

    /**
     * Sets the first name of the user. <var>firstName</var> can be blank, but must not be
     * <code>null</code>.
     */
    void setFirstName(String firstName)
    {
        assert firstName != null;

        passwordFileEntry[FIRST_NAME_IDX] = firstName;
    }

    /**
     * Returns the last name.
     */
    String getLastName()
    {
        return passwordFileEntry[LAST_NAME_IDX];
    }

    /**
     * Sets the last name of the user. <var>lastName</var> can be blank, but must not be
     * <code>null</code>.
     */
    void setLastName(String lastName)
    {
        assert lastName != null;

        passwordFileEntry[LAST_NAME_IDX] = lastName;
    }

    private String getPasswordHash()
    {
        return passwordFileEntry[PASSWORD_IDX];
    }

    /**
     * Sets the password of the user. <var>plainPassword</var> must not be blank.
     * <p>
     * The password is actually salted, an SHA1 hash of the salted password is computed and the
     * Base64 encoded version of the salt concatenated with the SHA1 hash is stored.
     */
    void setPassword(String plainPassword)
    {
        assert StringUtils.isNotBlank(plainPassword);

        passwordFileEntry[PASSWORD_IDX] = PasswordHasher.computeSaltedHash(plainPassword);
    }

    /**
     * Returns <code>true</code>, if the <var>password</var> is matching the password of this
     * user entry.
     */
    boolean isPasswordCorrect(String password)
    {
        return PasswordHasher.isPasswordCorrect(password, getPasswordHash());
    }

}
