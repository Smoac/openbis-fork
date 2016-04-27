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

package ch.systemsx.cisd.common.test;

import org.testng.AssertJUnit;

/**
 * Utilities for making assertions in unit tests.
 * 
 * @author Tomasz Pylak
 */
public class AssertionUtil
{
    /** asserts that given text starts with expectedSubstring */
    public static void assertStarts(String expectedPrefix, String text)
    {
        String errorMsg =
                String.format("String '%s' was expected to start with '%s'.", text, expectedPrefix);
        AssertJUnit.assertTrue(errorMsg, text.startsWith(expectedPrefix));
    }

    /** asserts that given text ends with expectedSubstring */
    public static void assertEnds(String expectedSuffix, String text)
    {
        String errorMsg =
                String.format("String '%s' was expected to end with '%s'.", text, expectedSuffix);
        AssertJUnit.assertTrue(errorMsg, text.endsWith(expectedSuffix));
    }

    /** asserts that given text contains expectedSubstring */
    public static void assertContains(String expectedSubstring, String text)
    {
        String errorMsg =
                String.format("String '%s' was expected to be a substring of '%s'.",
                        expectedSubstring, text);
        AssertJUnit.assertTrue(errorMsg, text.contains(expectedSubstring));
    }

    /** asserts that given text contains expectedSubstring. Comparision is case insensitive. */
    public static void assertContainsInsensitive(String expectedSubstring, String text)
    {
        assertContains(expectedSubstring.toUpperCase(), text.toUpperCase());
    }
}
