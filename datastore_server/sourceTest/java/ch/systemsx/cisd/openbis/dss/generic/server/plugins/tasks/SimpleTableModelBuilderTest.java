/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks;

import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SimpleTableModelBuilderTest extends AssertJUnit
{
    @Test
    public void testAddNonUniqueHeader()
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader("col");
        try
        {
            builder.addHeader("col");
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("There is already a header with title 'col'.", ex.getMessage());
        }
    }
    
    @Test
    public void testUnknownCell()
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader("col");
        IRowBuilder rowBuilder = builder.addRow();
        try
        {
            rowBuilder.setCell("my header", new StringTableCell("hello"));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Unkown column header title: my header", ex.getMessage());
        }
    }
    
    @Test
    public void testNoColumnsAndEmptyTable()
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        
        TableModel tableModel = builder.getTableModel();
        
        assertEquals(0, tableModel.getHeader().size());
        assertEquals(0, tableModel.getRows().size());
    }
    
    @Test
    public void testEmptyTable()
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        builder.addHeader("col");
        
        TableModel tableModel = builder.getTableModel();
        
        assertEquals(1, tableModel.getHeader().size());
        assertEquals(0, tableModel.getRows().size());
    }
    
    @Test
    public void testNoHeaders()
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        builder.addRow(Arrays.<ISerializableComparable>asList());
        
        TableModel tableModel = builder.getTableModel();
        
        assertEquals(0, tableModel.getHeader().size());
        assertEquals(1, tableModel.getRows().size());
        assertEquals(0, tableModel.getRows().get(0).getValues().size());
    }
    
    @Test
    public void testAddRow()
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        builder.addHeader("col");
        builder.addHeader("col", 200);
        builder.addRow(Arrays.asList(new StringTableCell("hello"), new IntegerTableCell(42)));
        builder.addRow(Arrays.asList(new StringTableCell("world"), new DateTableCell(4711)));
        
        TableModel tableModel = builder.getTableModel();
        
        assertHeader("col", "col", 150, 0, DataTypeCode.VARCHAR, tableModel.getHeader().get(0));
        assertHeader("col", "col2", 200, 1, DataTypeCode.VARCHAR, tableModel.getHeader().get(1));
        assertEquals(2, tableModel.getHeader().size());
        List<TableModelRow> rows = tableModel.getRows();
        assertEquals(new StringTableCell("hello"), rows.get(0).getValues().get(0));
        assertEquals(new IntegerTableCell(42), rows.get(0).getValues().get(1));
        assertEquals(2, rows.get(0).getValues().size());
        assertEquals(new StringTableCell("world"), rows.get(1).getValues().get(0));
        assertEquals(new DateTableCell(4711), rows.get(1).getValues().get(1));
        assertEquals(2, rows.get(1).getValues().size());
        assertEquals(2, rows.size());
    }
    
    @Test
    public void testAddRowWithWrongNumberOfCells()
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder();
        builder.addHeader("col");
        try
        {
            builder.addRow(Arrays.asList(new StringTableCell("hello"), new IntegerTableCell(42)));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("1 row values expected instead of 2.", ex.getMessage());
        }
    }
    
    @Test
    public void testRowBuilder()
    {
        SimpleTableModelBuilder builder = new SimpleTableModelBuilder(true);
        builder.addHeader("col1");
        builder.addHeader("col2", 300);
        IRowBuilder rowBuilder = builder.addRow();
        rowBuilder.setCell("col2", new IntegerTableCell(42));
        rowBuilder = builder.addRow();
        rowBuilder.setCell("col1", new StringTableCell("hello"));
        rowBuilder.setCell("col2", 3.125);
        rowBuilder = builder.addRow();
        rowBuilder.setCell("col1", "world");
        rowBuilder.setCell("col2", 4711);
        
        TableModel tableModel = builder.getTableModel();
        
        assertHeader("col1", "col1", 150, 0, DataTypeCode.VARCHAR, tableModel.getHeader().get(0));
        assertHeader("col2", "col2", 300, 1, DataTypeCode.REAL, tableModel.getHeader().get(1));
        assertEquals(2, tableModel.getHeader().size());
        List<TableModelRow> rows = tableModel.getRows();
        assertEquals(new StringTableCell(""), rows.get(0).getValues().get(0));
        assertEquals(new IntegerTableCell(42), rows.get(0).getValues().get(1));
        assertEquals(2, rows.get(0).getValues().size());
        assertEquals(new StringTableCell("hello"), rows.get(1).getValues().get(0));
        assertEquals(new DoubleTableCell(3.125), rows.get(1).getValues().get(1));
        assertEquals(2, rows.get(1).getValues().size());
        assertEquals(new StringTableCell("world"), rows.get(2).getValues().get(0));
        assertEquals(new IntegerTableCell(4711), rows.get(2).getValues().get(1));
        assertEquals(2, rows.get(1).getValues().size());
        assertEquals(3, rows.size());
    }
    
    private void assertHeader(String expectedTitle, String expectedID, int expectedDefaultWidth,
            int expectedIndex, DataTypeCode expectedDataType, TableModelColumnHeader header)
    {
        assertEquals(expectedTitle, header.getTitle());
        assertEquals("Header '" + header + "'", expectedDefaultWidth, header.getDefaultColumnWidth());
        assertEquals("Header '" + header + "'", expectedIndex, header.getIndex());
        assertEquals("Header '" + header + "'", expectedID, header.getId());
        assertEquals("Header '" + header + "'", expectedDataType, header.getDataType());
    }
}
