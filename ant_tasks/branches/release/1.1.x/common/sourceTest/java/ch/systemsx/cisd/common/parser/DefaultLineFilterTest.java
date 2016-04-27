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

package ch.systemsx.cisd.common.parser;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for the {@link DefaultLineFilter} 
 *
 * @author Bernd Rinn
 */
public class DefaultLineFilterTest
{

    @Test
    public void testNoHeaderLine()
    {
        final DefaultLineFilter filter = new DefaultLineFilter();
        for (int i = 0; i < 4; ++i)
        {
            assert filter.acceptLine("text", i) : "Line " + i;
        }
    }
    
    @DataProvider(name = "headerNumbers")
    public Object[][] headerNumbers()
    {
        return new Object[][] { { 0 }, { 1 }, { 2 }, { 3 } };
    }
    
    @Test(dataProvider = "headerNumbers")
    public void testHeaderLine(int headerLine)
    {
        final DefaultLineFilter filter = new DefaultLineFilter(headerLine);
        for (int i = 0; i < 4; ++i)
        {
            if (headerLine == i)
            {
                assert filter.acceptLine("text", i) == false : "Line " + i + " (header line: " + headerLine + ")";
            } else {
                assert filter.acceptLine("text", i) : "Line " + i + " (header line: " + headerLine + ")";
            }
        }
    }

    @Test
    public void testEmptyHeaderLine()
    {
        final DefaultLineFilter filter = new DefaultLineFilter(0);
        assert filter.acceptLine("", 0) == false;
        assert filter.acceptLine("something", 1);
    }
    
    @Test
    public void testSkipEmptyLine()
    {
        final DefaultLineFilter filter = new DefaultLineFilter();
        assert filter.acceptLine("", 0) == false;
    }
    
    @Test
    public void testSkipBlankLine()
    {
        final DefaultLineFilter filter = new DefaultLineFilter();
        assert filter.acceptLine("  ", 0) == false;
    }
    
    @Test
    public void testSkipCommentLine()
    {
        final DefaultLineFilter filter = new DefaultLineFilter();
        assert filter.acceptLine("# Some comment", 0) == false;
    }
    
    @Test
    public void testSkipCommentLineWithLeadingWhiteSpace()
    {
        final DefaultLineFilter filter = new DefaultLineFilter();
        assert filter.acceptLine("\t# Some comment", 0) == false;
    }
    
}
