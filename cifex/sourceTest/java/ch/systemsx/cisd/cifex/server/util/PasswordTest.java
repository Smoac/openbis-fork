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

package ch.systemsx.cisd.cifex.server.util;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

import ch.systemsx.cisd.cifex.server.common.Password;
import ch.systemsx.cisd.common.utilities.PasswordHasher;
import ch.systemsx.cisd.common.utilities.StringUtilities;

/**
 * Test cases for the {@link Password} class.
 *
 * @author Bernd Rinn
 */
public class PasswordTest
{
    
    @Test
    public void testIsEmpty()
    {
        assertTrue(Password.isEmpty(null));
        assertTrue(Password.isEmpty(new Password(null)));
        assertTrue(Password.isEmpty(new Password("")));
        assertFalse(Password.isEmpty(new Password("password")));
    }
    
    @Test
    public void testEquals()
    {
        assertTrue(Password.equals(null, null));
        assertTrue(Password.equals(null, new Password(null)));
        assertTrue(Password.equals(null, new Password("")));
        assertTrue(Password.equals(new Password(null), null));
        assertTrue(Password.equals(new Password(""), null));
        assertTrue(Password.equals(new Password(""), new Password("")));
        assertTrue(Password.equals(new Password("abc"), new Password("abc")));
        assertFalse(Password.equals(new Password("abc"), new Password("")));
        assertFalse(Password.equals(new Password(null), new Password("abc")));
        assertFalse(Password.equals(new Password("abc"), new Password(null)));
        assertFalse(Password.equals(null, new Password("abc")));
        assertFalse(Password.equals(new Password("abc"), null));
        assertFalse(Password.equals(new Password(""), new Password("abc")));
        assertFalse(Password.equals(new Password("abc"), new Password("def")));
        assertFalse(Password.equals(new Password("abc"), new Password("ABC")));
    }

    @Test
    public void testHashCode()
    {
        assertTrue(new Password(null).hashCode() == new Password(null).hashCode());
        assertTrue(new Password("").hashCode() == new Password("").hashCode());
        assertTrue(new Password("").hashCode() == "".hashCode());
        assertFalse(new Password(null).hashCode() == new Password("").hashCode());
        assertTrue(new Password("abcd").hashCode() == "abcd".hashCode());
    }

    @Test
    public void testEqual()
    {
        assertFalse(new Password(null).equals(null));
        assertFalse(new Password("").equals(null));
        assertTrue(new Password("").equals(new Password("")));
        assertTrue(new Password("abc").equals(new Password("abc")));
        assertFalse(new Password("abc").equals(new Password("")));
        assertFalse(new Password(null).equals(new Password("abc")));
        assertFalse(new Password("abc").equals(new Password(null)));
        assertFalse(new Password("abc").equals(null));
        assertFalse(new Password("").equals(new Password("abc")));
        assertFalse(new Password("abc").equals(new Password("def")));
        assertFalse(new Password("abc").equals(new Password("ABC")));
    }
    
    @Test
    public void testCreatePasswordHash()
    {
        final String passwd = "passw0rd";
        final Password password = new Password(passwd);
        final String hash1 = password.createPasswordHash();
        final String hash2 = password.createPasswordHash();
        // Check the length since the hash needs to fit in the database column of this size.
        assertEquals(32, hash1.length());
        assertEquals(32, hash2.length());
        assertFalse(hash1.equals(hash2));
        assertTrue(PasswordHasher.isPasswordCorrect(passwd, hash1));
        assertTrue(PasswordHasher.isPasswordCorrect(passwd, hash2));
    }
    
    @Test(expectedExceptions = { IllegalStateException.class })
    public void testCreatePasswordHashNullPassword()
    {
        new Password(null).createPasswordHash();
    }
    
    @Test(expectedExceptions = { IllegalStateException.class })
    public void testCreatePasswordHashEmptyPassword()
    {
        new Password("").createPasswordHash();
    }
    
    @Test
    public void testMatches()
    {
        final String passwd = "passw0rd";
        final Password password1 = new Password(passwd);
        final Password password2 = new Password(passwd);
        final String hash1 = password1.createPasswordHash();
        final String hash2 = password2.createPasswordHash();
        assertTrue(password1.matches(hash1));
        assertTrue(password1.matches(hash2));
        assertFalse(password1.matches(null));
        assertFalse(password1.matches(""));
        assertTrue(password2.matches(hash1));
        assertTrue(password2.matches(hash2));
        final String anotherPasswd = "new passw1rd";
        final Password anotherPassword = new Password(anotherPasswd);
        final String anotherHash = anotherPassword.createPasswordHash();
        assertTrue(anotherPassword.matches(anotherHash));
        assertFalse(anotherPassword.matches(hash1));
        assertFalse(anotherPassword.matches(hash2));
        assertFalse(password1.matches(anotherHash));
        assertFalse(password1.matches(null));
        assertFalse(password1.matches(""));
        final Password nullPassword = new Password(null);
        final Password emptyPassword = new Password("");
        assertFalse(nullPassword.matches(null));
        assertFalse(nullPassword.matches(""));
        assertFalse(emptyPassword.matches(null));
        assertFalse(emptyPassword.matches(""));
    }

    @Test
    public void testMatchesLegacyHashes()
    {
        final String passwd = "passw0rd";
        final String legacyHash = StringUtilities.computeMD5Hash(passwd);
        final String wrongLegacyHash = StringUtilities.computeMD5Hash(passwd + "1");
        final Password password1 = new Password(passwd);
        assertTrue(password1.matches(legacyHash));
        assertFalse(password1.matches(wrongLegacyHash));
    }
}
