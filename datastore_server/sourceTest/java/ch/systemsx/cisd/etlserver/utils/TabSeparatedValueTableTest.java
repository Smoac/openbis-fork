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

package ch.systemsx.cisd.etlserver.utils;

import java.io.StringReader;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TabSeparatedValueTableTest extends AssertJUnit
{
    @Test
    public void testEmptyFile()
    {
        try
        {
            new TabSeparatedValueTable(new StringReader(""), "source");
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Empty file 'source'.", ex.getMessage());
        }
    }
    
    @Test
    public void testNoColumnsNoRows()
    {
        StringReader source = new StringReader("\n");
        TabSeparatedValueTable table = new TabSeparatedValueTable(source, "");
        assertEquals("[]", table.getHeaders().toString());
        assertEquals(false, table.hasMoreRows());
        assertEquals(null, table.tryToGetNextRow());
        assertEquals(0, table.getColumns().size());
    }
    
    @Test
    public void testNoColumnsSeveralRows()
    {
        StringReader source = new StringReader("\n\n\n");
        TabSeparatedValueTable table = new TabSeparatedValueTable(source, "");
        assertEquals("[]", table.getHeaders().toString());
        assertEquals(true, table.hasMoreRows());
        assertEquals("[]", table.tryToGetNextRow().toString());
        assertEquals(0, table.getColumns().size());
        assertEquals(false, table.hasMoreRows());
        assertEquals(null, table.tryToGetNextRow());
    }
    
    @Test
    public void testGetHeaders()
    {
        StringReader source = new StringReader("alpha\tbeta");
        List<String> headers = new TabSeparatedValueTable(source, "").getHeaders();
        assertEquals("[alpha, beta]", headers.toString());
    }
    
    @Test
    public void testRowIterationForNoRows()
    {
        StringReader source = new StringReader("alpha\tbeta");
        TabSeparatedValueTable table = new TabSeparatedValueTable(source, "");
        
        assertEquals(false, table.hasMoreRows());
        assertEquals(null, table.tryToGetNextRow());
    }
    
    @Test
    public void testRowIterationSomeRows()
    {
        StringReader source = new StringReader("alpha\tbeta\n11\t12\n\t22\n31\n\n");
        TabSeparatedValueTable table = new TabSeparatedValueTable(source, "");
        
        assertEquals(true, table.hasMoreRows());
        assertEquals("[11, 12]", table.tryToGetNextRow().toString());
        assertEquals(true, table.hasMoreRows());
        assertEquals("[, 22]", table.tryToGetNextRow().toString());
        assertEquals(true, table.hasMoreRows());
        assertEquals("[31, ]", table.tryToGetNextRow().toString());
        assertEquals(true, table.hasMoreRows());
        assertEquals("[, ]", table.tryToGetNextRow().toString());
        assertEquals(false, table.hasMoreRows());
        assertEquals(null, table.tryToGetNextRow());
    }
    
    @Test
    public void testGetColumns()
    {
        StringReader source = new StringReader("alpha\tbeta\n11\t12\n\t22\n31\n\n");
        TabSeparatedValueTable table = new TabSeparatedValueTable(source, "");
        List<Column> columns = table.getColumns();

        assertEquals(2, columns.size());
        assertEquals("alpha", columns.get(0).getHeader());
        assertEquals("[11, , 31, ]", columns.get(0).getValues().toString());
        assertEquals("beta", columns.get(1).getHeader());
        assertEquals("[12, 22, , ]", columns.get(1).getValues().toString());
        assertEquals(false, table.hasMoreRows());
        assertEquals(null, table.tryToGetNextRow());
    }
    
    @Test
    public void testGetColumnsCombinedWithIterator()
    {
        StringReader source = new StringReader("alpha\tbeta\n11\t12\n\t22\n31\n\n");
        TabSeparatedValueTable table = new TabSeparatedValueTable(source, "");
        assertEquals(true, table.hasMoreRows());
        assertEquals("[11, 12]", table.tryToGetNextRow().toString());
        
        List<Column> columns = table.getColumns();
        
        assertEquals(2, columns.size());
        assertEquals("alpha", columns.get(0).getHeader());
        assertEquals("[, 31, ]", columns.get(0).getValues().toString());
        assertEquals("beta", columns.get(1).getHeader());
        assertEquals("[22, , ]", columns.get(1).getValues().toString());
        assertEquals(false, table.hasMoreRows());
        assertEquals(null, table.tryToGetNextRow());
    }
}
