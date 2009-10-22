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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stores the original object which will be a basis to calculate a grid row together with calculated
 * all custom columns values.
 * 
 * @author Tomasz Pylak
 */
public class GridRowModel<T> implements IsSerializable
{
    private T originalObject;

    // We would like to have a Map field, but we cannot do it because of a bug in
    // displaying serialization warnings in GWT 1.5. It was fixed in GWT 1.6
    private List<GridCustomColumnValue> calculatedColumnValues;

    public static <T> GridRowModel<T> createWithoutCustomColumns(T originalObject)
    {
        return new GridRowModel<T>(originalObject, new HashMap<String, PrimitiveValue>());
    }

    public GridRowModel(T originalObject, HashMap<String, PrimitiveValue> calculatedColumnMap)
    {
        this.originalObject = originalObject;
        this.calculatedColumnValues = asList(calculatedColumnMap);
    }

    private List<GridCustomColumnValue> asList(HashMap<String, PrimitiveValue> map)
    {
        List<GridCustomColumnValue> result = new ArrayList<GridCustomColumnValue>();
        for (Entry<String, PrimitiveValue> entry : map.entrySet())
        {
            GridCustomColumnValue column = new GridCustomColumnValue();
            column.setColumnId(entry.getKey());
            column.setValue(entry.getValue());
            result.add(column);
        }
        return result;
    }

    public T getOriginalObject()
    {
        return originalObject;
    }

    public List<GridCustomColumnValue> getCalculatedColumnValues()
    {
        return calculatedColumnValues;
    }

    public PrimitiveValue findColumnValue(String columnId)
    {
        for (GridCustomColumnValue value : calculatedColumnValues)
        {
            if (value.getColumnId().equals(columnId))
            {
                return value.getValue();
            }
        }
        throw new IllegalStateException("Column not found: " + columnId);
    }

    // GWT only
    @SuppressWarnings("unused")
    private GridRowModel()
    {
    }
}
