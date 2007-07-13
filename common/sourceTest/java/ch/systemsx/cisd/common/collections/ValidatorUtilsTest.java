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

package ch.systemsx.cisd.common.collections;

import static org.testng.AssertJUnit.*;

import java.util.regex.Pattern;

import org.testng.annotations.Test;

/**
 * Tests for {@link ValidatorUtils}.
 * 
 * @author Christian Ribeaud
 */
public final class ValidatorUtilsTest
{
    @Test
    public final void testConvertToRegEx()
    {
        String s = null;
        try
        {
            ValidatorUtils.convertToRegEx(s);
            fail("Pattern can not be null.");
        } catch (AssertionError e)
        {
            // Nothing to do here.
        }
        
        s = "hello";
        String regEx = ValidatorUtils.convertToRegEx(s);
        assertEquals("hello", regEx);
        Pattern pattern = Pattern.compile(regEx);
        assertEquals(false, pattern.matcher("heelo").matches());
        assertEquals(true, pattern.matcher("hello").matches());
        
        s = "he?lo";
        regEx = ValidatorUtils.convertToRegEx(s);
        assertEquals("he.lo", regEx);
        pattern = Pattern.compile(regEx);
        assertEquals(true, pattern.matcher("heelo").matches());
        assertEquals(false, pattern.matcher("helllo").matches());
        assertEquals(false, pattern.matcher("xhello").matches());
    
        s = "he?l";
        regEx = ValidatorUtils.convertToRegEx(s);
        assertEquals("he.l", regEx);
        pattern = Pattern.compile(regEx);
        assertEquals(false, pattern.matcher("cheul").matches());
        assertEquals(true, pattern.matcher("heul").matches());
        assertEquals(false, pattern.matcher("hello").matches());
        assertEquals(false, pattern.matcher("helloo").matches());
        
        s = "he*o";
        regEx = ValidatorUtils.convertToRegEx(s);
        assertEquals("he.*o", regEx);
        pattern = Pattern.compile(regEx);
        assertEquals(true, pattern.matcher("hello").matches());
        assertEquals(true, pattern.matcher("helllllllllllllllo").matches());
        assertEquals(true, pattern.matcher("helloo").matches());
        assertEquals(false, pattern.matcher("chelloo").matches());
        
        s = "h?l*o";
        regEx = ValidatorUtils.convertToRegEx(s);
        assertEquals("h.l.*o", regEx);
        
        s = "h\\*ll*o";
        regEx = ValidatorUtils.convertToRegEx(s);
        assertEquals("h\\*ll.*o", regEx);
        pattern = Pattern.compile(regEx);
        assertEquals(true, pattern.matcher("h*llooooooooooo").matches());
    }

    @Test
    public final void testCreatePatternValidator()
    {
        assertNull(ValidatorUtils.createPatternValidator(null));
        Validator<String> validator = ValidatorUtils.createPatternValidator("he*");
        assert validator.isValid("he");
        assert validator.isValid("hello");
        assert validator.isValid("hullo") == false;
        validator = ValidatorUtils.createPatternValidator("he?lo");
        assert validator.isValid("helo") == false;
        assert validator.isValid("hello");
        assert validator.isValid("he lo");
        assert validator.isValid("he.lo");
        assert validator.isValid("he\nlo") == false;
    }
}