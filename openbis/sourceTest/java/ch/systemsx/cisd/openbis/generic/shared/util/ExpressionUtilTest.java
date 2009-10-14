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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.translator.GridCustomExpressionTranslator.GridCustomFilterTranslator;

/**
 * Test cases for {@link GridCustomFilterTranslator}.
 * 
 * @author Izabela Adamczyk
 */
public class ExpressionUtilTest extends AssertJUnit
{
    @Test
    public void testExtractNoParameters() throws Exception
    {
        String expression = "";
        assertEquals("[]", extractParameters(expression).toString());
    }

    @Test
    public void testExtractOneParameter() throws Exception
    {
        String expression = "${abc}";
        assertEquals("[abc]", extractParameters(expression).toString());
    }

    @Test
    public void testExtractOneDuplicatedParameter() throws Exception
    {
        String expression = "${abc} ${abc}";
        assertEquals("[abc]", extractParameters(expression).toString());
    }

    @Test
    public void testExtractManyParameters() throws Exception
    {
        String expression = "${abc} ${abc} ${def} ${ghi}";
        assertEquals("[abc, def, ghi]", extractParameters(expression).toString());
    }

    private Collection<String> extractParameters(String expression)
    {
        List<String> list = new ArrayList<String>(ExpressionUtil.extractParameters(expression));
        Collections.sort(list);
        return list;
    }
    
}
