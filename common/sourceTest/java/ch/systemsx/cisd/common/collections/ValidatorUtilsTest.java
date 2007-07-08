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
        s = "he?lo";
        assertEquals("he.lo", ValidatorUtils.convertToRegEx(s));
        s = "he?l?";
        assertEquals("he.l.", ValidatorUtils.convertToRegEx(s));
        s = "he*o";
        assertEquals("he.*o", ValidatorUtils.convertToRegEx(s));
        s = "he*o*";
        assertEquals("he.*o.*", ValidatorUtils.convertToRegEx(s));
        s = "?h?l*o*";
        assertEquals(".h.l.*o.*", ValidatorUtils.convertToRegEx(s));
    }
    
    @Test
    public final void testCreatePatternValidator() {
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