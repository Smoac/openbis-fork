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

package ch.systemsx.cisd.openbis.generic.server.util;

import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TypedTableModelBuilderTest extends AssertJUnit
{
    private static final class MockSerializable implements IsSerializable
    {
    }
    
    @Test
    public void testSimpleBuilding()
    {
        TypedTableModelBuilder<IsSerializable> builder = new TypedTableModelBuilder<IsSerializable>();
        builder.addColumn("A").withTitle("Alpha");
        builder.addColumn("B").withDataType(DataTypeCode.REAL).withDefaultWidth(500);
        MockSerializable object = new MockSerializable();
        builder.addRow(object);
        builder.column("A").addString("hello");
        builder.column("B").addDouble(42.5);
        
        TypedTableModel<IsSerializable> model = builder.getModel();
        List<TableModelColumnHeader> headers = model.getHeader();
        assertHeadersOrder(headers, "A", "B");
        assertEquals("Alpha", headers.get(0).getTitle());
        assertEquals(150, headers.get(0).getDefaultColumnWidth());
        assertEquals(DataTypeCode.VARCHAR, headers.get(0).getDataType());
        assertEquals(null, headers.get(1).getTitle());
        assertEquals(500, headers.get(1).getDefaultColumnWidth());
        assertEquals(DataTypeCode.REAL, headers.get(1).getDataType());
        List<TableModelRowWithObject<IsSerializable>> rows = model.getRows();
        assertSame(object, rows.get(0).getObjectOrNull());
        assertEquals(new StringTableCell("hello"), rows.get(0).getValues().get(0));
        assertEquals(new DoubleTableCell(42.5), rows.get(0).getValues().get(1));
        assertEquals(2, rows.get(0).getValues().size());
        assertEquals(1, rows.size());
    }
    
    @Test
    public void testAddIntegerValueToColumn()
    {
        TypedTableModelBuilder<IsSerializable> builder = new TypedTableModelBuilder<IsSerializable>();
        MockSerializable object0 = new MockSerializable();
        builder.addRow(object0);
        builder.column("A").withTitle("Alpha").addInteger(42L);
        MockSerializable object1 = new MockSerializable();
        builder.addRow(object1);
        builder.column("A").addInteger(null);
        builder.addRow(null);
        builder.column("A").withTitle("a").addInteger(4711L);
        
        TypedTableModel<IsSerializable> model = builder.getModel();
        List<TableModelColumnHeader> headers = model.getHeader();
        assertHeadersOrder(headers, "A");
        assertEquals("a", headers.get(0).getTitle());
        assertEquals(DataTypeCode.INTEGER, headers.get(0).getDataType());
        List<TableModelRowWithObject<IsSerializable>> rows = model.getRows();
        assertSame(object0, rows.get(0).getObjectOrNull());
        assertEquals(new IntegerTableCell(42), rows.get(0).getValues().get(0));
        assertSame(object1, rows.get(1).getObjectOrNull());
        assertEquals(new StringTableCell(""), rows.get(1).getValues().get(0));
        assertSame(null, rows.get(2).getObjectOrNull());
        assertEquals(new IntegerTableCell(4711), rows.get(2).getValues().get(0));
        assertEquals(3, rows.size());
    }
    
    @Test
    public void testAddDoubleValueToColumn()
    {
        TypedTableModelBuilder<IsSerializable> builder = new TypedTableModelBuilder<IsSerializable>();
        MockSerializable object0 = new MockSerializable();
        builder.addRow(object0);
        builder.column("A").withTitle("Alpha").addDouble(4.25);
        MockSerializable object1 = new MockSerializable();
        builder.addRow(object1);
        builder.column("A").addDouble(null);
        builder.addRow(null);
        builder.column("A").withTitle("a").addDouble(4711.5);
        
        TypedTableModel<IsSerializable> model = builder.getModel();
        List<TableModelColumnHeader> headers = model.getHeader();
        assertHeadersOrder(headers, "A");
        assertEquals("a", headers.get(0).getTitle());
        assertEquals(DataTypeCode.REAL, headers.get(0).getDataType());
        List<TableModelRowWithObject<IsSerializable>> rows = model.getRows();
        assertSame(object0, rows.get(0).getObjectOrNull());
        assertEquals(new DoubleTableCell(4.25), rows.get(0).getValues().get(0));
        assertSame(object1, rows.get(1).getObjectOrNull());
        assertEquals(new StringTableCell(""), rows.get(1).getValues().get(0));
        assertSame(null, rows.get(2).getObjectOrNull());
        assertEquals(new DoubleTableCell(4711.5), rows.get(2).getValues().get(0));
        assertEquals(3, rows.size());
    }
    
    @Test
    public void testRowsWithEmptyCells()
    {
        TypedTableModelBuilder<IsSerializable> builder = new TypedTableModelBuilder<IsSerializable>();
        builder.addRow(null);
        builder.column("A").addString("a");
        builder.addRow(null);
        builder.column("B").addString("b");
        TypedTableModel<IsSerializable> model = builder.getModel();
        
        assertHeadersOrder(model.getHeader(), "A", "B");
        List<TableModelRowWithObject<IsSerializable>> rows = model.getRows();
        assertEquals("[a, ]", rows.get(0).getValues().toString());
        assertEquals("[, b]", rows.get(1).getValues().toString());
        assertEquals(2, rows.size());
    }
    
    private void assertHeadersOrder(List<TableModelColumnHeader> headers, String... headerIds)
    {
        for (int i = 0; i < headerIds.length; i++)
        {
            TableModelColumnHeader header = headers.get(i);
            assertEquals(headerIds[i], header.getId());
            assertEquals(i, header.getIndex());
        }
        assertEquals(headerIds.length, headers.size());
    }
}
