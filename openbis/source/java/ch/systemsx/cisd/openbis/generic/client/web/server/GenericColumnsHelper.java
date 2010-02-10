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

package ch.systemsx.cisd.openbis.generic.client.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SerializableComparableIDDecorator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.StringTableCell;
import ch.systemsx.cisd.openbis.generic.shared.util.DataTypeUtils;

/**
 * @author Franz-Josef Elmer
 */
public class GenericColumnsHelper
{
    public static final class Column
    {
        private final List<ISerializableComparable> values =
                new ArrayList<ISerializableComparable>();

        private final GenericTableColumnHeader header;

        public Column(GenericTableColumnHeader header)
        {
            this.header = header;
        }

        public GenericTableColumnHeader getHeader()
        {
            return header;
        }

        public List<ISerializableComparable> getValues()
        {
            return values;
        }

        void add(int index, ISerializableComparable value)
        {
            while (values.size() < index)
            {
                values.add(null);
            }
            values.add(value);
        }

        public void addStringWithID(int index, String string, Long id)
        {
            add(index, new SerializableComparableIDDecorator(new StringTableCell(string), id));
        }

        public void addDate(int index, Date date)
        {
            add(index, new DateTableCell(date));
        }

        public void addString(int index, String value)
        {
            add(index, new StringTableCell(value));
        }

        void addPrefixToColumnHeaderCodes(String prefix)
        {
            header.setCode(prefix + header.getCode());
        }

        void setIndex(int index)
        {
            header.setIndex(index);
        }
    }

    public static final class PropertyColumns
    {
        private final Map<String, Column> columns = new TreeMap<String, Column>();

        public void add(int index, IEntityProperty property)
        {
            PropertyType propertyType = property.getPropertyType();
            DataTypeCode dataType = propertyType.getDataType().getCode();
            String key = propertyType.getCode();
            Column column = columns.get(key);
            if (column == null)
            {
                GenericTableColumnHeader header = new GenericTableColumnHeader();
                header.setCode(key);
                header.setIndex(columns.size());
                header.setTitle(propertyType.getLabel());
                header.setType(dataType);
                column = new Column(header);
                columns.put(key, column);
            }
            column.add(index, DataTypeUtils.convertTo(dataType, property.tryGetAsString()));
        }

        public Set<String> getColumnCodes()
        {
            return columns.keySet();
        }

        public void addPrefixToColumnHeaderCodes(String prefix)
        {
            for (Map.Entry<String, Column> entry : columns.entrySet())
            {
                entry.getValue().addPrefixToColumnHeaderCodes(prefix);
            }
        }

        public int reindexColumns(int startIndex)
        {
            int index = startIndex;
            for (Map.Entry<String, Column> entry : columns.entrySet())
            {
                entry.getValue().setIndex(index++);
            }
            return index;
        }

        public Collection<? extends Column> getColumns()
        {
            return columns.values();
        }
    }
}
