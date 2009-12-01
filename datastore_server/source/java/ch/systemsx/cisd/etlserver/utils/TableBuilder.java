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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class TableBuilder
{
    private final List<Column> columns;
    
    public TableBuilder(List<String> headers)
    {
        this(headers.toArray(new String[headers.size()]));
    }
    
    public TableBuilder(String... headers)
    {
        columns = new ArrayList<Column>(headers.length);
        for (String header : headers)
        {
            columns.add(new Column(header));
        }
    }
    
    public void addRow(List<String> values)
    {
        addRow(values.toArray(new String[values.size()]));
    }
    
    public void addRow(String... values)
    {
        for (int i = 0, n = columns.size(); i < n; i++)
        {
           Column column = columns.get(i);
           column.add(i < values.length ? values[i] : "");
        }
    }
    
    public List<Column> getColumns()
    {
        return columns;
    }
}
