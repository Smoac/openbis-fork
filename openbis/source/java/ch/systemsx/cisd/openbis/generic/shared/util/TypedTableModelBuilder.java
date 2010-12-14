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

package ch.systemsx.cisd.openbis.generic.shared.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.SimplePersonRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DoubleTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IntegerTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;

/**
 * Builder class for creating an instance of {@link TypedTableModel}.
 * 
 * @author Franz-Josef Elmer
 */
public class TypedTableModelBuilder<T extends ISerializable>
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

        public IColumnMetaData hideByDefault()
        {
            column.getHeader().setHidden(true);
            return this;
        }
    }

    private static interface IIndexProvider
    {
        public int getIndex();
    }

    private static interface IColumnItem
    {
        public List<Column> getColumns();
    }

    private final class ColumnGroup implements IColumnGroup, IColumnItem
    {
        private final String groupKey;

        private final Set<Column> cols = new LinkedHashSet<TypedTableModelBuilder.Column>();

        private ColumnGroup(String groupKey)
        {
            this.groupKey = groupKey;
        }

        public List<Column> getColumns()
        {
            return new ArrayList<TypedTableModelBuilder.Column>(cols);
        }

        public IColumn column(String id)
        {
            Column column = getOrCreateColumn(id);
            cols.add(column);
            return column;
        }

        public void addProperties(Collection<IEntityProperty> properties)
        {
            addProperties(groupKey, properties);
        }

        public void addProperties(String idPrefix, Collection<IEntityProperty> properties)
        {
            for (IEntityProperty property : properties)
            {
                PropertyType propertyType = property.getPropertyType();
                String label = propertyType.getLabel();
                boolean internalNamespace = propertyType.isInternalNamespace();
                String code =
                        idPrefix + (internalNamespace ? "INTERN" : "USER") + "-"
                                + propertyType.getSimpleCode();
                IColumn column = column(code).withTitle(label);
                DataTypeCode dataType = propertyType.getDataType().getCode();
                ISerializableComparable value;
                if (DataTypeCode.MATERIAL == dataType)
                {
                    value = new MaterialTableCell(property.getMaterial());
                    column.withEntityKind(EntityKind.MATERIAL);
                } else
                {
                    value = DataTypeUtils.convertTo(dataType, property.tryGetAsString());
                }
                column.withDataType(dataType).addValue(value);
            }
        }
    }

    private static final class Column implements IColumn, IColumnItem
    {
        private final TableModelColumnHeader header;

        private final List<ISerializableComparable> values =
                new ArrayList<ISerializableComparable>();

        private final IIndexProvider indexProvider;

        Column(String id, IIndexProvider indexProvider)
        {
            header = new TableModelColumnHeader(null, id, 0);
            this.indexProvider = indexProvider;
        }

        public TableModelColumnHeader getHeader()
        {
            return header;
        }

        public ISerializableComparable getValue(int index)
        {
            return index < values.size() ? values.get(index) : EMPTY_CELL;
        }

        public List<Column> getColumns()
        {
            return new ArrayList<TypedTableModelBuilder.Column>(Arrays.asList(this));
        }

        public IColumn withTitle(String title)
        {
            header.setTitle(title);
            return this;
        }

        public IColumn withDefaultWidth(int width)
        {
            header.setDefaultColumnWidth(width);
            return this;
        }

        public IColumn withDataType(DataTypeCode dataType)
        {
            header.setDataType(dataType);
            return this;
        }

        public IColumn withEntityKind(EntityKind entityKind)
        {
            header.setEntityKind(entityKind);
            return this;
        }

        public void addValue(ISerializableComparable valueOrNull)
        {
            int index = indexProvider.getIndex();
            if (index < 0)
            {
                throw new IllegalStateException(
                        "Row index is < 0: This is most likely caused by missing invocation of builder.addRow().");
            }
            while (index > values.size())
            {
                values.add(EMPTY_CELL);
            }
            values.add(index, valueOrNull);
        }

        public void addString(String valueOrNull)
        {
            setDataType(DataTypeCode.VARCHAR);
            StringTableCell value =
                    valueOrNull == null ? EMPTY_CELL : new StringTableCell(valueOrNull);
            addValue(value);
        }

        public void addInteger(Long valueOrNull)
        {
            setDataType(DataTypeCode.INTEGER);
            ISerializableComparable value =
                    valueOrNull == null ? EMPTY_CELL : new IntegerTableCell(valueOrNull);
            addValue(value);
        }

        public void addDouble(Double valueOrNull)
        {
            setDataType(DataTypeCode.REAL);
            ISerializableComparable value =
                    valueOrNull == null ? EMPTY_CELL : new DoubleTableCell(valueOrNull);
            addValue(value);
        }

        public void addDate(Date valueOrNull)
        {
            setDataType(DataTypeCode.TIMESTAMP);
            ISerializableComparable value =
                    valueOrNull == null ? EMPTY_CELL : new DateTableCell(valueOrNull);
            addValue(value);
        }

        private void setDataType(DataTypeCode dataType)
        {
            header.setDataType(DataTypeUtils.getCompatibleDataType(header.getDataType(), dataType));
        }

        public void addPerson(Person personOrNull)
        {
            addString(SimplePersonRenderer.createPersonName(personOrNull).toString());
        }
    }

    private final Map<String, Column> columns = new HashMap<String, Column>();

    private final Map<String, IColumnGroup> columnGroups = new HashMap<String, IColumnGroup>();

    private final List<IColumnItem> columnItems = new ArrayList<IColumnItem>();

    private final List<T> rowObjects = new ArrayList<T>();

    /**
     * Returns the model.
     */
    public TypedTableModel<T> getModel()
    {
        List<Column> orderedColumns = new ArrayList<Column>();
        for (IColumnItem item : columnItems)
        {
            List<Column> itemColumns = item.getColumns();
            Collections.sort(itemColumns, new Comparator<Column>()
                {
                    public int compare(Column c1, Column c2)
                    {
                        String t1 = StringUtils.trimToEmpty(c1.getHeader().getTitle());
                        String t2 = StringUtils.trimToEmpty(c2.getHeader().getTitle());
                        return t1.compareTo(t2);
                    }
                });
            orderedColumns.addAll(itemColumns);
        }
        for (int i = 0; i < orderedColumns.size(); i++)
        {
            TableModelColumnHeader header = orderedColumns.get(i).getHeader();
            header.setIndex(i);
        }
        List<TableModelColumnHeader> headers = new ArrayList<TableModelColumnHeader>();
        for (Column column : orderedColumns)
        {
            headers.add(column.getHeader());
        }
        List<TableModelRowWithObject<T>> rows = new ArrayList<TableModelRowWithObject<T>>();
        for (int i = 0, n = rowObjects.size(); i < n; i++)
        {
            T object = rowObjects.get(i);
            List<ISerializableComparable> rowValues =
                    new ArrayList<ISerializableComparable>(headers.size());
            for (Column column : orderedColumns)
            {
                rowValues.add(column.getValue(i));
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
        if (columns.containsKey(id))
        {
            throw new IllegalArgumentException("There is already a column with id '" + id + "'.");
        }
        Column column = getOrCreateColumnAsColumnItem(id);
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

    public IColumnGroup columnGroup(final String groupKey)
    {
        IColumnGroup columnGroup = columnGroups.get(groupKey);
        if (columnGroup == null)
        {
            ColumnGroup group = new ColumnGroup(groupKey);
            columnItems.add(group);
            columnGroup = group;
            columnGroups.put(groupKey, columnGroup);
        }
        return columnGroup;
    }

    public IColumn column(String id)
    {
        return getOrCreateColumnAsColumnItem(id);
    }

    private Column getOrCreateColumnAsColumnItem(String id)
    {
        boolean knownColumn = columns.containsKey(id);
        Column column = getOrCreateColumn(id);
        if (knownColumn == false)
        {
            columnItems.add(column);
        }
        return column;
    }

    private Column getOrCreateColumn(String id)
    {
        Column column = columns.get(id);
        if (column == null)
        {
            column = new Column(id, new IIndexProvider()
                {
                    public int getIndex()
                    {
                        return rowObjects.size() - 1;
                    }
                });
            columns.put(id, column);
        }
        return column;

    }

}
