/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * Test cases for {@link FilterTranslator}.
 * 
 * @author Izabela Adamczyk
 */
public class FilterTranslatorTest extends AssertJUnit
{
    @Test
    public void testExtractNoParameters() throws Exception
    {
        String expression = "";
        assertEquals(0, FilterTranslator.extractParameters(expression).size());
    }

    @Test
    public void testExtractOneParameter() throws Exception
    {
        String expression = "${abc}";
        assertEquals(1, FilterTranslator.extractParameters(expression).size());
    }

    @Test
    public void testExtractOneDuplicatedParameter() throws Exception
    {
        String expression = "${abc} ${abc}";
        assertEquals(1, FilterTranslator.extractParameters(expression).size());
    }

    @Test
    public void testExtractManyParameters() throws Exception
    {
        String expression = "${abc} ${abc} ${def} ${ghi}";
        assertEquals(3, FilterTranslator.extractParameters(expression).size());
    }

    @Test
    public void testExtractColumnsNoParameters() throws Exception
    {
        String expression = "";
        assertEquals(0, FilterTranslator.extractColumns(expression).size());
    }

    @Test
    public void testExtractColumnsOneParameter() throws Exception
    {
        String expression = "col(abc)";
        assertEquals(1, FilterTranslator.extractColumns(expression).size());
    }

    @Test
    public void testExtractColumnOneDuplicatedParameter() throws Exception
    {
        String expression = "col(abc) col(abc) ";
        assertEquals(1, FilterTranslator.extractColumns(expression).size());
    }

    @Test
    public void testExtractColumnManyParameters() throws Exception
    {
        String expression = "col(abc) col(abc) col(def) col(ghi)";
        assertEquals(3, FilterTranslator.extractColumns(expression).size());
    }

}
