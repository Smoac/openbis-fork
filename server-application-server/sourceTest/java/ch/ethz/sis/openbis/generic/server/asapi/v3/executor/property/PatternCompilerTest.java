/*
 *  Copyright ETH 2024 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.property;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertEquals;

public class PatternCompilerTest
{

    IPatternCompiler compiler;

    @BeforeMethod
    public void setUp()
    {
        compiler = new PatternCompiler();
    }

    @Test
    public void testUnknownPatternType_throwsException()
    {
        try{
            compiler.compilePattern("some pattern", "UNKNOWN_PATTERN_TYPE");
            fail();
        } catch (UserFailureException e)
        {
            assertEquals("Unknown pattern type specified!", e.getMessage());
        }
    }

    @Test
    public void testRegular_basic()
    {
        Pattern pattern = compiler.compilePattern("a", "PATTERN");
        assertTrue(pattern.matcher("a").matches());
    }

    @Test
    public void testValues_basic()
    {
        Pattern pattern = compiler.compilePattern("\"A\",\"B\",\"C\"", "VALUES");
        assertTrue(pattern.matcher("A").matches());
        assertTrue(pattern.matcher("B").matches());
        assertTrue(pattern.matcher("C").matches());

        assertFalse(pattern.matcher("A2").matches());
        assertFalse(pattern.matcher("2B").matches());
        assertFalse(pattern.matcher("CC").matches());
    }

    @Test
    public void testValues_withWhitespaces()
    {
        Pattern pattern = compiler.compilePattern("\"Alice\",  \"B o b\"   ,    \" Cynthia \"", "VALUES");
        assertTrue(pattern.matcher("Alice").matches());
        assertFalse(pattern.matcher(" Alice ").matches());
        assertFalse(pattern.matcher(" Alice").matches());

        assertTrue(pattern.matcher("B o b").matches());
        assertFalse(pattern.matcher("Bob").matches());
        assertFalse(pattern.matcher("Bo b").matches());

        assertTrue(pattern.matcher(" Cynthia ").matches());
        assertFalse(pattern.matcher(" Cynthia").matches());
        assertFalse(pattern.matcher("Cynthia").matches());
    }

    @Test
    public void testValues_specialCharacters()
    {
        Pattern pattern = compiler.compilePattern("\"Ali\"ce\",  \"B\\nob\"   ,    \"\\t Cynthia \"", "VALUES");
        assertTrue(pattern.matcher("Ali\"ce").matches());
        assertFalse(pattern.matcher("Alice").matches());

        assertTrue(pattern.matcher("B\nob").matches());
        assertFalse(pattern.matcher("Bob").matches());

        assertTrue(pattern.matcher("\t Cynthia ").matches());
        assertFalse(pattern.matcher(" Cynthia ").matches());
    }

    @Test
    public void testRanges_basic()
    {
        Pattern pattern = compiler.compilePattern("1-10", "RANGES");
        assertTrue(pattern.matcher("1").matches());
        assertTrue(pattern.matcher("5").matches());
        assertTrue(pattern.matcher("10").matches());
        assertTrue(pattern.matcher("0000000000010").matches());
        assertTrue(pattern.matcher("00010.99").matches());

        assertFalse(pattern.matcher("0").matches());
        assertFalse(pattern.matcher("1.0.0").matches());
        assertFalse(pattern.matcher("-5").matches());
        assertFalse(pattern.matcher("11").matches());
        assertFalse(pattern.matcher("0.0").matches());
        assertFalse(pattern.matcher("0150").matches());
    }

    @Test
    public void testRanges_multipleRanges()
    {
        Pattern pattern = compiler.compilePattern("33-150,  (-10)-10", "RANGES");
        assertTrue(pattern.matcher("33").matches());
        assertTrue(pattern.matcher("150").matches());
        assertTrue(pattern.matcher("00150.00").matches());
        assertTrue(pattern.matcher("99.9").matches());
        assertTrue(pattern.matcher("0000000000010").matches());
        assertTrue(pattern.matcher("00010.99").matches());
        assertTrue(pattern.matcher("-9.9").matches());
        assertTrue(pattern.matcher("000").matches());
        assertTrue(pattern.matcher("-10").matches());
        assertTrue(pattern.matcher("-5").matches());

        assertFalse(pattern.matcher("180").matches());
        assertFalse(pattern.matcher("-50").matches());
        assertFalse(pattern.matcher("22").matches());
        assertFalse(pattern.matcher("-11.000001").matches());
        assertFalse(pattern.matcher("151.0").matches());
    }




}
