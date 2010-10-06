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
 * Builder class for creating an instance of {@link TypedTableModel}.
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
            return index < values.size() ? values.get(index) : EMPTY_CELL;
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
    
    /**
     * Returns the model.
     */
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

    /**
     * Adds a column with specified id.
     * 
     * @return an {@link IColumnMetaData} instance which allows to set title, default width, and/or
     *         data type.
     * @throws IllegalArgumentException if a column with specified is has already been added.
     */
    public IColumnMetaData addColumn(String id)
    {
        Column column = createColumn(null, null, id);
        String id1 = column.getHeader().getId();
        Column oldColumn = columns.put(id1, column);
        if (oldColumn != null)
        {
            throw new IllegalArgumentException("There is already a column with id '" + id1 + "'.");
        }
        return new ColumnMetaData(column);
    }
    
    /**
     * Adds a row with optional row object. This method has to be called before adding values to
     * columns.
     */
    public void addRow(T objectOrNull)
    {
        rowObjects.add(objectOrNull);
    }

    /**
     * Adds a string value to specified column. The column will be created if it does not exist.
     */
    public void addStringValueToColumn(String id, String valueOrNull)
    {
        addStringValueToColumn(null, id, valueOrNull);
    }

    /**
     * Adds a string value to the column specified by its id. The column will be created if it does
     * not exist.
     * 
     * @param title Title of the column. Will be ignored if the column already exists.
     */
    public void addStringValueToColumn(String title, String id, String valueOrNull)
    {
        StringTableCell value = valueOrNull == null ? EMPTY_CELL : new StringTableCell(valueOrNull);
        addValueToColumn(title, DataTypeCode.VARCHAR, id, value);
    }
    
    /**
     * Adds an integer value to specified column. The column will be created if it does not exist.
     */
    public void addIntegerValueToColumn(String id, Long valueOrNull)
    {
        addIntegerValueToColumn(null, id, valueOrNull);
    }

    /**
     * Adds an integer value to the column specified by its id. The column will be created if it
     * does not exist.
     * 
     * @param title Title of the column. Will be ignored if the column already exists.
     */
    public void addIntegerValueToColumn(String title, String id, Long valueOrNull)
    {
        ISerializableComparable value =
                valueOrNull == null ? EMPTY_CELL : new IntegerTableCell(valueOrNull);
        addValueToColumn(title, DataTypeCode.INTEGER, id, value);
    }
    
    /**
     * Adds a double value to specified column. The column will be created if it does not exist.
     */
    public void addDoubleValueToColumn(String id, Double valueOrNull)
    {
        addDoubleValueToColumn(null, id, valueOrNull);
    }
    
    /**
     * Adds a double value to the column specified by its id. The column will be created if it
     * does not exist.
     * 
     * @param title Title of the column. Will be ignored if the column already exists.
     */
    public void addDoubleValueToColumn(String title, String id, Double valueOrNull)
    {
        ISerializableComparable value =
                valueOrNull == null ? EMPTY_CELL : new DoubleTableCell(valueOrNull);
        addValueToColumn(title, DataTypeCode.REAL, id, value);
    }

    /**
     * Adds specified value to the column specified by its id. The column will be created if it
     * does not exist.
     * 
     * @param titleOrNull Title of the column. Will be ignored if the column already exists.
     * @param dataTypeOrNull Data type of the column. Will be ignored if trhe column already exists.
     */
    public void addValueToColumn(String titleOrNull, DataTypeCode dataTypeOrNull, String id,
            ISerializableComparable value)
    {
        getOrCreateColumn(titleOrNull, dataTypeOrNull, id).insertValueAt(rowObjects.size() - 1,
                value);
    }
    
    private Column getOrCreateColumn(String titleOrNull, DataTypeCode dataTypeOrNull, String id)
    {
        Column column = columns.get(id);
        if (column == null)
        {
            column = createColumn(titleOrNull, dataTypeOrNull, id);
            columns.put(id, column);
        }
        return column;
    }
    
    private Column createColumn(String titleOrNull, DataTypeCode dataTypeOrNull, String id)
    {
        TableModelColumnHeader header = createHeader(titleOrNull, id);
        if (dataTypeOrNull != null)
        {
            header.setDataType(dataTypeOrNull);
        }
        return new Column(header);
    }

    private TableModelColumnHeader createHeader(String titleOrNull, String id)
    {
        return new TableModelColumnHeader(titleOrNull, id, columns.size());
    }

}

