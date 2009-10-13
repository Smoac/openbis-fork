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

package ch.systemsx.cisd.openbis.generic.client.web.server.calculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;

/**
 * Row object used in jython expressions to access column values.
 * <p>
 * All public methods of this class are part of the Filter/Calculated Column API.
 * 
 * @author Franz-Josef Elmer
 */
final class Row<T>
{

    private final Map<String, IColumnDefinition<T>> definitionsByID =
            new LinkedHashMap<String, IColumnDefinition<T>>();

    private final Map<String, List<ColumnDefinition>> definitionsByProperties =
            new HashMap<String, List<ColumnDefinition>>();

    private GridRowModel<T> row;

    Row(Set<IColumnDefinition<T>> availableColumns)
    {
        for (IColumnDefinition<T> columnDefinition : availableColumns)
        {
            definitionsByID.put(columnDefinition.getIdentifier(), columnDefinition);
        }
    }

    void setRowData(GridRowModel<T> row)
    {
        this.row = row;
    }

    /**
     * Returns the value of the column specified by its ID.
     * 
     * @throws IllegalArgumentException if no column with specified ID exists.
     */
    public Object col(String columnID)
    {
        IColumnDefinition<T> columnDefinition = definitionsByID.get(columnID);
        if (columnDefinition == null)
        {
            throw new IllegalArgumentException("Undefined column: " + columnID);
        }

        return columnDefinition.getComparableValue(row);
    }

    /**
     * Returns all column definitions which have a property with specified key.
     * 
     * @param propertyKeyOrNull The key of the property. If <code>null</code> all column definitions
     *            are returned.
     * @return an empty list if no column definition found.
     */
    public List<ColumnDefinition> colDefs(String propertyKeyOrNull)
    {
        List<ColumnDefinition> definitions = definitionsByProperties.get(propertyKeyOrNull);
        if (definitions == null)
        {
            definitions = new ArrayList<ColumnDefinition>();
            Set<Entry<String, IColumnDefinition<T>>> entries = definitionsByID.entrySet();
            for (Entry<String, IColumnDefinition<T>> entry : entries)
            {
                IColumnDefinition<T> columnDefinition = entry.getValue();
                if (propertyKeyOrNull == null
                        || columnDefinition.tryToGetProperty(propertyKeyOrNull) != null)
                {
                    definitions.add(new ColumnDefinition(columnDefinition));
                }
            }
            definitionsByProperties.put(propertyKeyOrNull, definitions);
        }
        return Collections.unmodifiableList(definitions);
    }

    /**
     * Returns all column values where the column definition has a property with specified key and
     * value.
     * 
     * @return an empty list if no columns found.
     */
    public List<Object> cols(String propertyKey, String propertyValue)
    {
        List<Object> values = new ArrayList<Object>();
        List<ColumnDefinition> definitions = colDefs(propertyKey);
        for (ColumnDefinition definition : definitions)
        {
            String property = definition.property(propertyKey);
            if (propertyValue.equals(property))
            {
                values.add(col(definition.id()));
            }
        }
        return Collections.unmodifiableList(values);
    }

    /**
     * Returns all column values grouped by the property value of the property specified by the key.
     * 
     * @return an empty list if no columns found.
     */
    public List<ColumnGroup> colsGroupedBy(String propertyKey)
    {
        Map<String, List<Object>> map = new LinkedHashMap<String, List<Object>>();
        List<ColumnDefinition> definitions = colDefs(propertyKey);
        for (ColumnDefinition definition : definitions)
        {
            String property = definition.property(propertyKey);
            List<Object> values = map.get(property);
            if (values == null)
            {
                values = new ArrayList<Object>();
                map.put(property, values);
            }
            values.add(col(definition.id()));
        }
        Set<Entry<String, List<Object>>> entrySet = map.entrySet();
        List<ColumnGroup> groups = new ArrayList<ColumnGroup>(entrySet.size());
        for (Entry<String, List<Object>> entry : entrySet)
        {
            groups.add(new ColumnGroup(entry.getKey(), entry.getValue()));
        }
        return Collections.unmodifiableList(groups);
    }

}