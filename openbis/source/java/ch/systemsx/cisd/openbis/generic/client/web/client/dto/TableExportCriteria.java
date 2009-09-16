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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridFilterInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo;

/**
 * Describes which entities should be exported, in which order and which columns should be present.
 * The result of the export is a table with header.
 * 
 * @author Tomasz Pylak
 */
public class TableExportCriteria<T/* exported entity */> implements IResultSetKeyHolder<String>,
        IsSerializable
{
    // a key at which data are stored in the server cache
    private String resultSetKey;

    private SortInfo<T> sortInfo = new SortInfo<T>();

    /** @see IResultSetConfig#getFilterInfos() */
    private List<GridFilterInfo<T>> filterInfos = new ArrayList<GridFilterInfo<T>>();

    // which columns should be exported
    private List<IColumnDefinition<T>> columnDefs;

    private CustomFilterInfo<T> customFilterInfo;

    // GWT only
    public TableExportCriteria()
    {
    }

    public TableExportCriteria(String resultSetKey, SortInfo<T> sortInfo,
            List<GridFilterInfo<T>> filterInfos, List<IColumnDefinition<T>> columnDefs,
            CustomFilterInfo<T> customFilterInfo)
    {
        this.resultSetKey = resultSetKey;
        this.sortInfo = sortInfo;
        this.filterInfos = filterInfos;
        this.columnDefs = columnDefs;
        this.customFilterInfo = customFilterInfo;
    }

    public String getResultSetKey()
    {
        return resultSetKey;
    }

    public SortInfo<T> getSortInfo()
    {
        return sortInfo;
    }

    public List<GridFilterInfo<T>> getFilterInfos()
    {
        return filterInfos;
    }

    public List<IColumnDefinition<T>> getColumnDefs()
    {
        return columnDefs;
    }

    public CustomFilterInfo<T> tryGetCustomFilterInfo()
    {
        return customFilterInfo;
    }
}
