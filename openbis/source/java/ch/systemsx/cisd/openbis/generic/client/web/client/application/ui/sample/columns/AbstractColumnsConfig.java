/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;

/**
 * @author Tomasz Pylak
 */
public abstract class AbstractColumnsConfig
{
    private List<ColumnConfig> columns;

    private List<IColumnDefinition<Sample>> columnDefs;

    public AbstractColumnsConfig()
    {
        this.columns = new ArrayList<ColumnConfig>();
        this.columnDefs = new ArrayList<IColumnDefinition<Sample>>();
    }

    protected void defineColumns(List<? extends ISampleColDefUI> columnsSchema, boolean isSortable)
    {
        columns.clear();
        columnDefs.clear();
        for (ISampleColDefUI column : columnsSchema)
        {
            columns.add(createColumn(column, isSortable));
            columnDefs.add(column);
        }
    }

    private ColumnConfig createColumn(ISampleColDefUI column, boolean isSortable)
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setMenuDisabled(false);
        columnConfig.setSortable(isSortable);
        columnConfig.setId(column.getIdentifier());
        columnConfig.setHeader(column.getHeader());

        columnConfig.setWidth(column.getWidth());
        columnConfig.setHidden(column.isHidden());
        return columnConfig;
    }

    public List<IColumnDefinition<Sample>> getColumnDefs()
    {
        return columnDefs;
    }

    public List<ColumnConfig> getColumns()
    {
        return columns;
    }
}
