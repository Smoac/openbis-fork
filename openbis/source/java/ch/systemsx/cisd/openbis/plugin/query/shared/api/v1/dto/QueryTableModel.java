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

package ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model of query data.
 *
 * @author Franz-Josef Elmer
 */
public class QueryTableModel implements Serializable
{
    private static final long serialVersionUID = 1L;
    private final List<QueryTableColumn> columns;
    private final List<Serializable[]> rows;
    
    /**
     * Creates an instance for the specified columns.
     */
    public QueryTableModel(List<QueryTableColumn> columns)
    {
        this.columns = columns;
        rows = new ArrayList<Serializable[]>();
    }

    /**
     * Returns columns as specified in the constructor.
     */
    public List<QueryTableColumn> getColumns()
    {
        return columns;
    }

    /**
     * Adds a row of values.
     * 
     * @throws IllegalArgumentException if the number of values is not the same as the number of
     *             columns.
     */
    public void addRow(Serializable[] values)
    {
        if (values == null)
        {
            throw new IllegalArgumentException("Unspecified row.");
        }
        if (values.length != columns.size())
        {
            throw new IllegalArgumentException("Row has " + values.length + " instead of " + columns.size() + ".");
        }
        rows.add(values);
    }

    /**
     * Gets all rows.
     */
    public List<Serializable[]> getRows()
    {
        return rows;
    }
    
}
