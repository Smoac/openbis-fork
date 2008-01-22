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

package ch.systemsx.cisd.common.utilities;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.utilities.StringUtilities;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for the {@link StringUtilities}.
 * 
 * @author Bernd Rinn
 */
public class StringUtilitiesTest
{

    @Test
    public final void testCapitalize()
    {
        assertEquals("Test", StringUtilities.capitalize("test"));
        assertEquals("Test", StringUtilities.capitalize("TEST"));
        assertEquals("Test", StringUtilities.capitalize("tEsT"));
        assertEquals("Test test", StringUtilities.capitalize("Test Test"));
    }

    @Test
    public final void testConcatenateEmptyList()
    {
        final List<String> list = Arrays.asList();
        assertEquals("", StringUtilities.concatenateWithNewLine(list));
    }

    @Test
    public final void testConcatenate()
    {
        final List<String> list = Arrays.asList("one", "two", "three");
        assertEquals("one two three", StringUtilities.concatenateWithSpace(list));
        final String brokenDownInLines = String.format("one%1$stwo%1$sthree", System.getProperty("line.separator"));
        assertEquals(brokenDownInLines, StringUtilities.concatenateWithNewLine(list));
    }
    
    @Test
    public void testEncrypt()
    {
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", StringUtilities.encrypt(""));
        assertEquals("900150983cd24fb0d6963f7d28e17f72", StringUtilities.encrypt("abc"));
    }

    @Test
    public final void testGetOrdinal()
    {
        try
        {
            StringUtilities.getOrdinal(-1);
            fail("Ordinal of negative number not possible.");
        } catch (AssertionError e)
        {
            // Nothing to do here.
        }
        assertEquals("0th", StringUtilities.getOrdinal(0));
        assertEquals("1st", StringUtilities.getOrdinal(1));
        assertEquals("2nd", StringUtilities.getOrdinal(2));
        assertEquals("3rd", StringUtilities.getOrdinal(3));
        assertEquals("11th", StringUtilities.getOrdinal(11));
        assertEquals("112th", StringUtilities.getOrdinal(112));
        assertEquals("3313th", StringUtilities.getOrdinal(3313));
        assertEquals("101st", StringUtilities.getOrdinal(101));
        assertEquals("53rd", StringUtilities.getOrdinal(53));
        assertEquals("19th", StringUtilities.getOrdinal(19));
    }

}
