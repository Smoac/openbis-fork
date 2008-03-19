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

package ch.systemsx.cisd.common.db;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

/**
 * Test cases for the {@link SQLCommandTokenizer}.
 * 
 * @author Bernd Rinn
 */
public class SQLCommandTokenizerTest
{
    @Test
    public void testNormal()
    {
        final SQLCommandTokenizer cmdTokenizer = new SQLCommandTokenizer("A;\nb;\ncC;");
        assertEquals("a", cmdTokenizer.getNextCommand());
        assertEquals("b", cmdTokenizer.getNextCommand());
        assertEquals("cc", cmdTokenizer.getNextCommand());
        assertNull(cmdTokenizer.getNextCommand());
    }

    @Test
    public void testOneLine()
    {
        final SQLCommandTokenizer cmdTokenizer = new SQLCommandTokenizer("A;b;cC;");
        assertEquals("a", cmdTokenizer.getNextCommand());
        assertEquals("b", cmdTokenizer.getNextCommand());
        assertEquals("cc", cmdTokenizer.getNextCommand());
        assertNull(cmdTokenizer.getNextCommand());
    }

    @Test
    public void testMissingSemicolon()
    {
        final SQLCommandTokenizer cmdTokenizer = new SQLCommandTokenizer("A;\nb;\ncC");
        assertEquals("a", cmdTokenizer.getNextCommand());
        assertEquals("b", cmdTokenizer.getNextCommand());
        assertEquals("cc", cmdTokenizer.getNextCommand());
        assertNull(cmdTokenizer.getNextCommand());
    }

    @Test
    public void testMissingSemicolonButEOL()
    {
        final SQLCommandTokenizer cmdTokenizer = new SQLCommandTokenizer("A;\nb;\ncC\n");
        assertEquals("a", cmdTokenizer.getNextCommand());
        assertEquals("b", cmdTokenizer.getNextCommand());
        assertEquals("cc", cmdTokenizer.getNextCommand());
        assertNull(cmdTokenizer.getNextCommand());
    }

    @Test
    public void testWhitespaces()
    {
        final SQLCommandTokenizer cmdTokenizer =
                new SQLCommandTokenizer(" A ; \n b   B;\t  c\tC\n ");
        assertEquals("a", cmdTokenizer.getNextCommand());
        assertEquals("b b", cmdTokenizer.getNextCommand());
        assertEquals("c c", cmdTokenizer.getNextCommand());
        assertNull(cmdTokenizer.getNextCommand());
    }

    @Test
    public void testCommentLine()
    {
        final SQLCommandTokenizer cmdTokenizer =
                new SQLCommandTokenizer(" A ; \n\n  -- Some Comment \nb   B;\t  c\tC\n ");
        assertEquals("a", cmdTokenizer.getNextCommand());
        assertEquals("b b", cmdTokenizer.getNextCommand());
        assertEquals("c c", cmdTokenizer.getNextCommand());
        assertNull(cmdTokenizer.getNextCommand());
    }

    @Test
    public void testMoreCommentLines()
    {
        final SQLCommandTokenizer cmdTokenizer =
                new SQLCommandTokenizer(" A ; \n\n  -- Some Comment \n--'Bla'\n\nb   B;\t  c\tC\n ");
        assertEquals("a", cmdTokenizer.getNextCommand());
        assertEquals("b b", cmdTokenizer.getNextCommand());
        assertEquals("c c", cmdTokenizer.getNextCommand());
        assertNull(cmdTokenizer.getNextCommand());
    }

    @Test
    public void testConstants()
    {
        final SQLCommandTokenizer cmdTokenizer =
                new SQLCommandTokenizer("A 'A  \\'B\tc';\nb;\ncC;");
        assertEquals("a 'A  \\'B\tc'", cmdTokenizer.getNextCommand());
        assertEquals("b", cmdTokenizer.getNextCommand());
        assertEquals("cc", cmdTokenizer.getNextCommand());
        assertNull(cmdTokenizer.getNextCommand());
    }

}
