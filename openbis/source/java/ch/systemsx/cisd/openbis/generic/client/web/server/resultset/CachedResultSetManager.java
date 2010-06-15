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

package ch.systemsx.cisd.openbis.generic.client.web.server.resultset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.shared.basic.AlternativesStringFilter;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ColumnDistinctValues;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridColumnFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridFilters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig.ResultSetFetchMode;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.GridExpressionUtils;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.IColumnCalculator;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo.SortDir;

/**
 * A {@link IResultSetManager} implementation which caches the full data retrieved using
 * {@link IOriginalDataProvider}.
 * 
 * @author Christian Ribeaud
 */
public final class CachedResultSetManager<K> implements IResultSetManager<K>, Serializable
{
    private static final long serialVersionUID = 1L;

    /** how many values can one column have to consider it reasonably distinct */
    @Private
    static final int MAX_DISTINCT_COLUMN_VALUES_SIZE = 50;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CachedResultSetManager.class);

    private static final GridCustomColumnInfo translate(GridCustomColumn columnDefinition)
    {
        return new GridCustomColumnInfo(columnDefinition.getCode(), columnDefinition.getName(),
                columnDefinition.getDescription(), columnDefinition.getDataType());
    }

    private static final class Column
    {
        private final GridCustomColumn columnDefinition;

        private List<PrimitiveValue> values;

        Column(GridCustomColumn columnDefinition)
        {
            this.columnDefinition = columnDefinition;
        }

        GridCustomColumnInfo getInfo()
        {
            return translate(columnDefinition);
        }

        boolean hasSameExpression(GridCustomColumn column)
        {
            return columnDefinition.getExpression().equals(column.getExpression());
        }

        public void setValues(List<PrimitiveValue> values)
        {
            this.values = values;
        }

        public List<PrimitiveValue> getValues()
        {
            return values;
        }
    }

    private static class TableData<T>
    {
        private final List<T> originalData;

        private final ICustomColumnsProvider customColumnsProvider;

        private final IColumnCalculator columnCalculator;

        private Map<String, Column> calculatedColumns = new HashMap<String, Column>();

        TableData(List<T> originalData, ICustomColumnsProvider customColumnsProvider,
                IColumnCalculator columnCalculator)
        {
            this.originalData = originalData;
            this.customColumnsProvider = customColumnsProvider;
            this.columnCalculator = columnCalculator;
        }

        GridRowModels<T> getRows(String sessionToken, IResultSetConfig<?, T> config)
        {
            Set<Entry<String, Column>> cachedColumns = calculatedColumns.entrySet();
            List<GridCustomColumn> allCustomColumnDefinitions =
                    loadAllCustomColumnDefinitions(sessionToken, config);
            List<GridCustomColumn> neededCustomColumns =
                    filterNeededCustomColumnDefinitions(allCustomColumnDefinitions, config);
            removedColumnsNoLongerNeeded(cachedColumns, neededCustomColumns);

            for (GridCustomColumn customColumn : neededCustomColumns)
            {
                String code = customColumn.getCode();
                Column column = calculatedColumns.get(code);
                if (column == null)
                {
                    Set<IColumnDefinition<T>> availableColumns = config.getAvailableColumns();
                    boolean errorMessageLong = config.isCustomColumnErrorMessageLong();
                    long time = System.currentTimeMillis();
                    List<PrimitiveValue> values =
                            columnCalculator.evalCustomColumn(originalData, customColumn,
                                    availableColumns, errorMessageLong);
                    operationLog.info((System.currentTimeMillis() - time)
                            + "ms for calculating column '" + customColumn.getName() + "' over "
                            + originalData.size() + " rows.");
                    column = new Column(customColumn);
                    column.setValues(values);
                    calculatedColumns.put(code, column);
                }
            }

            List<GridRowModel<T>> rows = new ArrayList<GridRowModel<T>>();
            for (int i = 0; i < originalData.size(); i++)
            {
                T rowData = originalData.get(i);
                HashMap<String, PrimitiveValue> customColumnValues =
                        new HashMap<String, PrimitiveValue>();
                for (Entry<String, Column> entry : cachedColumns)
                {
                    String columnCode = entry.getKey();
                    customColumnValues.put(columnCode, entry.getValue().getValues().get(i));
                }
                rows.add(new GridRowModel<T>(rowData, customColumnValues));
            }

            List<ColumnDistinctValues> columnDistinctValues =
                    calculateColumnDistinctValues(rows, config.getFilters());
            List<GridCustomColumnInfo> customColumnInfos =
                    extractColumnInfos(allCustomColumnDefinitions);

            return new GridRowModels<T>(rows, customColumnInfos, columnDistinctValues);
        }

        private List<GridCustomColumn> loadAllCustomColumnDefinitions(String sessionToken,
                IResultSetConfig<?, T> resultConfig)
        {
            String gridId = resultConfig.tryGetGridDisplayId();
            ArrayList<GridCustomColumn> result = new ArrayList<GridCustomColumn>();
            if (gridId != null)
            {
                List<GridCustomColumn> columns =
                        customColumnsProvider.getGridCustomColumn(sessionToken, gridId);
                for (GridCustomColumn column : columns)
                {
                    result.add(column);
                }
            }
            return result;
        }

        private List<GridCustomColumn> filterNeededCustomColumnDefinitions(
                List<GridCustomColumn> allDefinitions, IResultSetConfig<?, T> resultConfig)
        {
            Set<String> ids = gatherAllColumnIDs(resultConfig);
            ArrayList<GridCustomColumn> result = new ArrayList<GridCustomColumn>();
            for (GridCustomColumn column : allDefinitions)
            {
                if (ids.contains(column.getCode()))
                {
                    result.add(column);
                }
            }
            return result;
        }

        private Set<String> gatherAllColumnIDs(final IResultSetConfig<?, T> resultConfig)
        {
            Set<String> ids = new HashSet<String>();
            Set<String> idsOfPresentedColumns = resultConfig.getIDsOfPresentedColumns();
            if (idsOfPresentedColumns != null)
            {
                ids.addAll(idsOfPresentedColumns);
            }
            IColumnDefinition<T> sortField = resultConfig.getSortInfo().getSortField();
            if (sortField != null)
            {
                ids.add(sortField.getIdentifier());
            }
            GridFilters<T> filters = resultConfig.getFilters();
            List<GridColumnFilterInfo<T>> filterInfos = filters.tryGetFilterInfos();
            if (filterInfos != null)
            {
                for (GridColumnFilterInfo<T> filterInfo : filterInfos)
                {
                    ids.add(filterInfo.getFilteredField().getIdentifier());
                }
            }
            CustomFilterInfo<T> customFilterInfo = filters.tryGetCustomFilterInfo();
            if (customFilterInfo != null)
            {
                String expression = customFilterInfo.getExpression();
                Set<IColumnDefinition<T>> availableColumns = resultConfig.getAvailableColumns();
                for (IColumnDefinition<T> columnDefinition : availableColumns)
                {
                    String identifier = columnDefinition.getIdentifier();
                    if (expression.indexOf(identifier) >= 0)
                    {
                        ids.add(identifier);
                    }
                }
            }
            return ids;
        }

        private void removedColumnsNoLongerNeeded(Set<Entry<String, Column>> cachedColumns,
                List<GridCustomColumn> neededCustomColumns)
        {
            Map<String, GridCustomColumn> map = new HashMap<String, GridCustomColumn>();
            for (GridCustomColumn customColumn : neededCustomColumns)
            {
                map.put(customColumn.getCode(), customColumn);
            }

            for (Iterator<Entry<String, Column>> iterator = cachedColumns.iterator(); iterator
                    .hasNext();)
            {
                Entry<String, Column> entry = iterator.next();
                String code = entry.getKey();
                GridCustomColumn customColumn = map.get(code);
                if (customColumn == null)
                {
                    iterator.remove();
                } else if (entry.getValue().hasSameExpression(customColumn) == false)
                {
                    iterator.remove();
                }
            }
        }

        private List<ColumnDistinctValues> calculateColumnDistinctValues(
                List<GridRowModel<T>> rowModels, GridFilters<T> gridFilters)
        {
            List<ColumnDistinctValues> result = new ArrayList<ColumnDistinctValues>();
            List<GridColumnFilterInfo<T>> filterInfos = gridFilters.tryGetFilterInfos();
            if (filterInfos == null)
            {
                return result;
            }
            for (GridColumnFilterInfo<T> column : filterInfos)
            {
                ColumnDistinctValues distinctValues =
                        tryCalculateColumnDistinctValues(rowModels, column.getFilteredField());
                if (distinctValues != null)
                {
                    result.add(distinctValues);
                }
            }
            return result;
        }

        /** @return null if values are not from a small set */
        private ColumnDistinctValues tryCalculateColumnDistinctValues(
                List<GridRowModel<T>> rowModels, IColumnDefinition<T> column)
        {
            Set<String> distinctValues = new LinkedHashSet<String>();
            for (GridRowModel<T> rowModel : rowModels)
            {
                String value = column.getValue(rowModel);
                distinctValues.add(value);
                if (distinctValues.size() > MAX_DISTINCT_COLUMN_VALUES_SIZE)
                {
                    return null;
                }
            }
            ArrayList<String> distinctValuesList = new ArrayList<String>(distinctValues);
            return new ColumnDistinctValues(column.getIdentifier(), distinctValuesList);
        }

        private List<GridCustomColumnInfo> extractColumnInfos(
                List<GridCustomColumn> allCustomColumnDefinitions)
        {
            List<GridCustomColumnInfo> result = new ArrayList<GridCustomColumnInfo>();
            for (GridCustomColumn definition : allCustomColumnDefinitions)
            {
                Column column = calculatedColumns.get(definition.getCode());
                if (column != null)
                {
                    result.add(column.getInfo());
                } else
                {
                    result.add(translate(definition));
                }
            }
            return result;
        }
    }

    private final IResultSetKeyGenerator<K> resultSetKeyProvider;

    private final ICustomColumnsProvider customColumnsProvider;

    private final Map<K, TableData<?>> cache = new HashMap<K, TableData<?>>();

    private final IColumnCalculator columnCalculator;

    public CachedResultSetManager(final IResultSetKeyGenerator<K> resultSetKeyProvider,
            ICustomColumnsProvider customColumnsProvider)
    {
        this(resultSetKeyProvider, customColumnsProvider, new IColumnCalculator()
            {
                public <T> List<PrimitiveValue> evalCustomColumn(List<T> data,
                        GridCustomColumn customColumn, Set<IColumnDefinition<T>> availableColumns,
                        boolean errorMessagesAreLong)
                {
                    return GridExpressionUtils.evalCustomColumn(data, customColumn,
                            availableColumns, errorMessagesAreLong);
                }
            });
    }

    public CachedResultSetManager(final IResultSetKeyGenerator<K> resultSetKeyProvider,
            ICustomColumnsProvider customColumnsProvider, IColumnCalculator columnCalculator)
    {
        this.resultSetKeyProvider = resultSetKeyProvider;
        this.customColumnsProvider = customColumnsProvider;
        this.columnCalculator = columnCalculator;
    }

    @SuppressWarnings("unchecked")
    private final <T> T cast(final Object object)
    {
        return (T) object;
    }

    /**
     * Server-side filter info object.
     */
    private static class FilterInfo<T>
    {
        private final IColumnDefinition<T> filteredField;

        private final AlternativesStringFilter filter;

        private FilterInfo(GridColumnFilterInfo<T> gridFilterInfo)
        {
            this.filteredField = gridFilterInfo.getFilteredField();
            this.filter = createAlternativesFilter(gridFilterInfo);
        }

        private AlternativesStringFilter createAlternativesFilter(
                GridColumnFilterInfo<T> gridFilterInfo)
        {
            final AlternativesStringFilter result = new AlternativesStringFilter();
            final String pattern = gridFilterInfo.tryGetFilterPattern().toLowerCase();
            result.setFilterValue(pattern);
            return result;
        }

        static <T> FilterInfo<T> tryCreate(GridColumnFilterInfo<T> filterInfo)
        {
            if (filterInfo.tryGetFilterPattern() == null)
            {
                return null;
            } else
            {
                return new FilterInfo<T>(filterInfo);
            }
        }

        public boolean isMatching(final GridRowModel<T> row)
        {
            final String value = filteredField.getValue(row).toLowerCase();
            return filter.passes(value);
        }
    }

    private static final <T> GridRowModels<T> filterData(final GridRowModels<T> rows,
            Set<IColumnDefinition<T>> availableColumns, GridFilters<T> filters)
    {
        List<GridRowModel<T>> filteredRows = rows;
        CustomFilterInfo<T> customFilterInfo = filters.tryGetCustomFilterInfo();
        if (customFilterInfo != null)
        {
            long time = System.currentTimeMillis();
            filteredRows =
                    GridExpressionUtils.applyCustomFilter(rows, availableColumns, customFilterInfo);
            operationLog.info((System.currentTimeMillis() - time) + "ms for filtering "
                    + rows.size() + " rows with custom filter '" + customFilterInfo.getName()
                    + "'.");
        }
        List<GridColumnFilterInfo<T>> filterInfos = filters.tryGetFilterInfos();
        if (filterInfos != null)
        {
            filteredRows = applyStandardColumnFilter(rows, filterInfos);
        }
        return rows.cloneWithData(filteredRows);
    }

    private static <T> List<GridRowModel<T>> applyStandardColumnFilter(final GridRowModels<T> rows,
            final List<GridColumnFilterInfo<T>> filterInfos)
    {
        List<GridRowModel<T>> filtered = new ArrayList<GridRowModel<T>>();
        List<FilterInfo<T>> appliedFilterInfos = processAppliedFilters(filterInfos);
        for (GridRowModel<T> row : rows)
        {
            if (isMatching(row, appliedFilterInfos))
            {
                filtered.add(row);
            }
        }
        return filtered;
    }

    private static <T> List<FilterInfo<T>> processAppliedFilters(
            final List<GridColumnFilterInfo<T>> filterInfos)
    {
        List<FilterInfo<T>> serverFilterInfos = new ArrayList<FilterInfo<T>>(filterInfos.size());
        for (GridColumnFilterInfo<T> filterInfo : filterInfos)
        {
            FilterInfo<T> info = FilterInfo.tryCreate(filterInfo);
            if (info != null)
            {
                serverFilterInfos.add(info);
            }
        }
        return serverFilterInfos;
    }

    // returns true if a row matches all the filters
    private static final <T> boolean isMatching(final GridRowModel<T> row,
            final List<FilterInfo<T>> serverFilterInfos)
    {
        for (FilterInfo<T> filter : serverFilterInfos)
        {
            if (filter.isMatching(row) == false)
            {
                return false;
            }
        }
        return true;
    }

    private final <T> void sortData(final GridRowModels<T> data, final SortInfo<T> sortInfo)
    {
        assert data != null : "Unspecified data.";
        assert sortInfo != null : "Unspecified sort information.";
        if (data.size() == 0)
        {
            return;
        }
        final SortDir sortDir = sortInfo.getSortDir();
        final IColumnDefinition<T> sortField = sortInfo.getSortField();
        if (sortDir == SortDir.NONE || sortField == null)
        {
            return;
        }
        Comparator<GridRowModel<T>> comparator =
                ColumnSortUtils.createComparator(sortDir, sortField);
        Collections.sort(data, comparator);
    }

    private static int getLimit(final int size, final int limit, final int offset)
    {
        assert size > -1 : "Size can not be negative";
        if (limit < 0)
        {
            return size - offset;
        }
        return Math.min(size - offset, limit);
    }

    private static int getOffset(final int size, final int offset)
    {
        assert size > -1 : "Size can not be negative";
        if (size == 0)
        {
            return 0;
        }
        return Math.min(size - 1, Math.max(offset, 0));
    }

    /**
     * Encapsulates list returned by {@link List#subList(int, int)} in a new <code>List</code> as
     * <i>GWT</i> complains because of a serialization concern.
     */
    private final static <T> GridRowModels<T> subList(final GridRowModels<T> data,
            final int offset, final int limit)
    {
        final int toIndex = offset + limit;
        return data.cloneWithData(data.subList(offset, toIndex));
    }

    //
    // IDataManager
    //

    public final synchronized <T> IResultSet<K, T> getResultSet(final String sessionToken,
            final IResultSetConfig<K, T> resultConfig, final IOriginalDataProvider<T> dataProvider)
    {
        assert resultConfig != null : "Unspecified result configuration";
        assert dataProvider != null : "Unspecified data retriever";
        ResultSetFetchConfig<K> cacheConfig = resultConfig.getCacheConfig();
        ResultSetFetchMode mode = cacheConfig.getMode();
        debug("getResultSet(cache config = " + cacheConfig + ")");

        if (mode == ResultSetFetchMode.COMPUTE_AND_CACHE
                || mode == ResultSetFetchMode.CLEAR_COMPUTE_AND_CACHE)
        {
            if (mode == ResultSetFetchMode.CLEAR_COMPUTE_AND_CACHE)
            {
                removeResultSet(cacheConfig.tryGetResultSetKey());
            }
            return calculateResultSetAndSave(sessionToken, resultConfig, dataProvider);
        }
        K dataKey = cacheConfig.tryGetResultSetKey();
        return calculateSortAndFilterResult(sessionToken, resultConfig, dataKey);
    }

    private <T> IResultSet<K, T> calculateResultSetAndSave(final String sessionToken,
            final IResultSetConfig<K, T> resultConfig, final IOriginalDataProvider<T> dataProvider)
    {
        K dataKey = resultSetKeyProvider.createKey();
        debug("retrieving the data with a new key " + dataKey);
        List<T> rows = dataProvider.getOriginalData();
        cache.put(dataKey, new TableData<T>(rows, customColumnsProvider, columnCalculator));
        return calculateSortAndFilterResult(sessionToken, resultConfig, dataKey);
    }

    private <T> IResultSet<K, T> calculateSortAndFilterResult(final String sessionToken,
            final IResultSetConfig<K, T> resultConfig, K dataKey)
    {
        GridRowModels<T> data = calculateRowModels(sessionToken, resultConfig, dataKey);
        return filterLimitAndSort(resultConfig, data, dataKey);
    }

    private <T> GridRowModels<T> calculateRowModels(final String sessionToken,
            final IResultSetConfig<K, T> resultConfig, K dataKey)
    {
        TableData<T> tableData = cast(cache.get(dataKey));
        if (tableData == null)
        {
            throw new IllegalArgumentException("Invalid result set key: " + dataKey);
        }
        return tableData.getRows(sessionToken, resultConfig);
    }

    private <T> IResultSet<K, T> filterLimitAndSort(final IResultSetConfig<K, T> resultConfig,
            GridRowModels<T> data, K dataKey)
    {
        GridRowModels<T> filteredData =
                filterData(data, resultConfig.getAvailableColumns(), resultConfig.getFilters());
        final int size = filteredData.size();
        final int offset = getOffset(size, resultConfig.getOffset());
        final int limit = getLimit(size, resultConfig.getLimit(), offset);
        final SortInfo<T> sortInfo = resultConfig.getSortInfo();
        sortData(filteredData, sortInfo);
        final GridRowModels<T> list = subList(filteredData, offset, limit);
        return new DefaultResultSet<K, T>(dataKey, list, size);
    }

    public final synchronized void removeResultSet(final K resultSetKey)
    {
        assert resultSetKey != null : "Unspecified data key holder.";
        if (cache.remove(resultSetKey) != null)
        {
            debug(String.format("Result set for key '%s' has been removed.", resultSetKey));
        } else
        {
            debug(String.format("No result set for key '%s' could be found.", resultSetKey));
        }
    }

    private void debug(String msg)
    {
        operationLog.debug(msg);
    }
}
