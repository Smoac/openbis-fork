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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TypedTableModelBuilder<T extends IsSerializable>
{
    private static final StringTableCell EMPTY_CELL = new StringTableCell("");

    private static final class ColumnMetaData implements IColumnMetaData
    {
        private final Column column;

        ColumnMetaData(Column column)
        {
            this.column = column;
        }
        
        public IColumnMetaData withTitle(String title)
        {
            column.getHeader().setTitle(title);
            return this;
        }

        public IColumnMetaData withDefaultWidth(int width)
        {
            column.getHeader().setDefaultColumnWidth(width);
            return this;
        }
        
        public IColumnMetaData withDataType(DataTypeCode dataType)
        {
            column.getHeader().setDataType(dataType);
            return this;
        }
    }
    
    private static final class Column
    {
        private final TableModelColumnHeader header;
        private final List<ISerializableComparable> values = new ArrayList<ISerializableComparable>();

        Column(TableModelColumnHeader header)
        {
            this.header = header;
        }
        
        public TableModelColumnHeader getHeader()
        {
            return header;
        }
        
        public ISerializableComparable getValue(int index)
        {
            return values.get(index);
        }

        public void insertValueAt(int index, ISerializableComparable value)
        {
            while (index > values.size())
            {
                values.add(EMPTY_CELL);
            }
            values.add(index, value);
        }
    }
    
    private final Map<String, Column> columns = new HashMap<String, Column>();
    
    private final List<T> rowObjects = new ArrayList<T>();
    
    public TypedTableModel<T> getModel()
    {
        List<Column> cols = new ArrayList<Column>(columns.values());
        Collections.sort(cols, new Comparator<Column>()
            {
                public int compare(Column c1, Column c2)
                {
                    return c1.getHeader().getIndex() - c2.getHeader().getIndex();
                }
            });
        List<TableModelColumnHeader> headers = new ArrayList<TableModelColumnHeader>();
        for (Column column : cols)
        {
            headers.add(column.getHeader());
        }
        List<TableModelRowWithObject<T>> rows = new ArrayList<TableModelRowWithObject<T>>();
        for (int i = 0, n = rowObjects.size(); i < n; i++)
        {
            T object = rowObjects.get(i);
            List<ISerializableComparable> rowValues = new ArrayList<ISerializableComparable>(headers.size());
            for (int j = 0, m = headers.size(); j < m; j++)
            {
                rowValues.add(null);
            }
            for (int j = 0, m = headers.size(); j < m; j++)
            {
                int index = headers.get(j).getIndex();
                rowValues.set(index, cols.get(index).getValue(i));
            }
            rows.add(new TableModelRowWithObject<T>(object, rowValues));
        }
        return new TypedTableModel<T>(headers, rows);
    }
    
    public IColumnMetaData addColumn(String id)
    {
        Column column = createColumn(null, id);
        addColumn(column);
        return new ColumnMetaData(column);
    }
    
    private void addColumn(Column column)
    {
        String id = column.getHeader().getId();
        Column oldColumn = columns.put(id, column);
        if (oldColumn != null)
        {
            throw new IllegalArgumentException("There is already a column with id '" + id + "'.");
        }
    }

    public void addStringValueToColumn(String id, String value)
    {
        addStringValueToColumn(null, id, value);
    }

    public void addStringValueToColumn(String title, String id, String value)
    {
        addValueToColumn(title, id, value == null ? EMPTY_CELL : new StringTableCell(value));
    }
    
    public void addIntegerValueToColumn(String id, Long value)
    {
        addIntegerValueToColumn(null, id, value);
    }
    
    public void addIntegerValueToColumn(String title, String id, Long value)
    {
        addValueToColumn(title, id, value == null ? EMPTY_CELL : new IntegerTableCell(value));
    }
    
    public void addDoubleValueToColumn(String id, Double value)
    {
        addDoubleValueToColumn(null, id, value);
    }
    
    public void addDoubleValueToColumn(String title, String id, Double value)
    {
        addValueToColumn(title, id, value == null ? EMPTY_CELL : new DoubleTableCell(value));
    }
    
    public void addValueToColumn(String titleOrNull, String id, ISerializableComparable value)
    {
        getOrCreateColumn(titleOrNull, id).insertValueAt(rowObjects.size() - 1, value);
    }
    
    private Column getOrCreateColumn(String titleOrNull, String id)
    {
        Column column = columns.get(id);
        if (column == null)
        {
            column = createColumn(titleOrNull, id);
            columns.put(id, column);
        }
        return column;
    }
    
    private Column createColumn(String titleOrNull, String id)
    {
        TableModelColumnHeader header = createHeader(titleOrNull, id);
        return new Column(header);
    }

    private TableModelColumnHeader createHeader(String titleOrNull, String id)
    {
        TableModelColumnHeader header = new TableModelColumnHeader(titleOrNull, id, columns.size());
        return header;
    }

    public void addRow(T object)
    {
        rowObjects.add(object);
    }

}

