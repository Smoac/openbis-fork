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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jmock.api.ExpectationError;

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
                String.format("String \n'%s'\nwas expected to start with \n'%s'.", text,
                        expectedPrefix);
        assertTrue(errorMsg, text.startsWith(expectedPrefix));
    }

    /** asserts that given text ends with expectedSubstring */
    public static void assertEnds(String expectedSuffix, String text)
    {
        String errorMsg =
                String.format("String \n'%s'\nwas expected to end with \n'%s'.", text,
                        expectedSuffix);
        assertTrue(errorMsg, text.endsWith(expectedSuffix));
    }

    /** asserts that given text contains expectedSubstring */
    public static void assertContains(String expectedSubstring, String text)
    {
        String errorMsg =
                String.format("String \n'%s'\nwas expected to be a substring of \n'%s'.",
                        expectedSubstring, text);
        assertTrue(errorMsg, text.contains(expectedSubstring));
    }

    /** asserts that given text contains expectedSubstring. Comparision is case insensitive. */
    public static void assertContainsInsensitive(String expectedSubstring, String text)
    {
        assertContains(expectedSubstring.toUpperCase(), text.toUpperCase());
    }

    public static void assertContainsLines(String expected, String actual)
    {
        Collection<String> expectedLines = getLines(expected);
        Collection<String> actualLines = getLines(actual);

        actualLines.retainAll(expectedLines);

        assertTrue("Expected to contain lines:\n" + expected + "\nactual lines:\n" + actual, expectedLines.equals(actualLines));
    }

    /** asserts that two int arrays are equal **/
    public static void assertArraysEqual(int[] a1, int[] a2)
    {
        assertEquals(a1.length, a2.length);
        for (int i = 0; i < a1.length; i++)
        {
            assertEquals("Different elements at position, " + i, a1[i], a2[i]);
        }
    }

    /** asserts that two float arrays are equal **/
    public static void assertArraysEqual(float[] a1, float[] a2)
    {
        assertEquals(a1.length, a2.length);
        for (int i = 0; i < a1.length; i++)
        {
            assertEquals("Different elements at position, " + i, a1[i], a2[i]);
        }
    }

    /**
     * asserts that two enums have the same values. Usage example:
     * 
     * <pre>
     * List&lt;MyEnum1&gt; values1 = Arrays.asList(MyEnum1.values());
     * List&lt;MyEnum2&gt; values2 = Arrays.asList(MyEnum2.values());
     * AssertionUtil.assertEnumsEqual(values1, values2);
     * </pre>
     */
    public static void assertEnumsEqual(List<? extends Enum<?>> values1,
            List<? extends Enum<?>> values2)
    {
        Set<String> valuesSet1 = asSet(values1);
        Set<String> valuesSet2 = asSet(values2);
        assertEquals(valuesSet1, valuesSet2);
    }

    private static Set<String> asSet(List<? extends Enum<?>> enumValues)
    {
        Set<String> stringValues = new HashSet<String>();
        for (Enum<?> enumInst : enumValues)
        {
            stringValues.add(enumInst.name());
        }
        return stringValues;
    }

    private static Set<String> getLines(String text)
    {
        if (text == null || text.isEmpty())
        {
            return new HashSet<String>();
        } else
        {
            return new HashSet<String>(Arrays.asList(text.split("\n")));
        }
    }

    /**
     * returns true if error was caused by unexpected invocation.
     */
    public static Throwable tryAsErrorCausedByUnexpectedInvocation(Throwable t)
    {
        if (t == null)
        {
            return null;
        }
        if (t instanceof ExpectationError)
        {
            return t;
        }
        return tryAsErrorCausedByUnexpectedInvocation(t.getCause());
    }
}
