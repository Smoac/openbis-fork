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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.collection.IKeyExtractor;
import ch.systemsx.cisd.common.collection.TableMap;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.shared.basic.string.AlternativesStringFilter;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ColumnDistinctValues;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridColumnFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridCustomColumnInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridFilters;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridRowModels;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetFetchConfig.ResultSetFetchMode;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.GridExpressionUtils;
import ch.systemsx.cisd.openbis.generic.client.web.server.calculator.ITableDataProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.XMLPropertyTransformer;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.PrimitiveValue;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DateTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridCustomColumn;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SortInfo.SortDir;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableGridColumnDefinition;

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

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            CachedResultSetManager.class);

    private static final GridCustomColumnInfo translate(GridCustomColumn columnDefinition)
    {
        return new GridCustomColumnInfo(columnDefinition.getCode(), columnDefinition.getName(),
                columnDefinition.getDescription(), columnDefinition.getDataType());
    }

    private static <T> String getOriginalValue(IColumnDefinition<T> definition,
            final GridRowModel<T> row)
    {
        Comparable<?> value = definition.tryGetComparableValue(row);

        if (value != null && value instanceof DateTableCell)
        {
            final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d = ((DateTableCell) value).getDateTime();
            return df.format(d);
        }

        return value == null ? "" : value.toString();
    }

    static interface IColumnCalculator
    {
        /**
         * Calculates the values of the specified custom column definition by using specified data
         * and specified column definitions. In case of an error the column value is an error
         * message.
         * 
         * @param errorMessagesAreLong if <code>true</code> a long error message will be created.
         */
        public <T> List<PrimitiveValue> evalCustomColumn(List<T> data,
                GridCustomColumn customColumn, Set<IColumnDefinition<T>> availableColumns,
                boolean errorMessagesAreLong);
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

        private final List<TableModelColumnHeader> headers;

        private final List<IColumnDefinition<T>> headerColumnDefinitions =
                new ArrayList<IColumnDefinition<T>>();

        private Map<String, Column> calculatedColumns = new HashMap<String, Column>();

        TableData(List<T> originalData, List<TableModelColumnHeader> headers,
                ICustomColumnsProvider customColumnsProvider, IColumnCalculator columnCalculator)
        {
            this.originalData = originalData;
            this.headers = headers;
            for (final TableModelColumnHeader header : headers)
            {
                headerColumnDefinitions.add(new IColumnDefinition<T>()
                    {

                        @Override
                        public String getValue(GridRowModel<T> rowModel)
                        {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public Comparable<?> tryGetComparableValue(GridRowModel<T> rowModel)
                        {
                            T originalObject = rowModel.getOriginalObject();
                            if (originalObject instanceof TableModelRow)
                            {
                                TableModelRow row = (TableModelRow) originalObject;
                                return row.getValues().get(header.getIndex());
                            }
                            return null;
                        }

                        @Override
                        public String getHeader()
                        {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public String getIdentifier()
                        {
                            return header.getId();
                        }

                        @Override
                        public DataTypeCode tryToGetDataType()
                        {
                            return null;
                        }

                        @Override
                        public String tryToGetProperty(String key)
                        {
                            throw new UnsupportedOperationException();
                        }

                        @Override
                        public boolean isCustom()
                        {
                            return false;
                        }
                    });
            }
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
                    Set<IColumnDefinition<T>> availableColumns = getAvailableColumns(config);
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

            return new GridRowModels<T>(rows, headers, customColumnInfos, columnDistinctValues);
        }

        private Set<IColumnDefinition<T>> getAvailableColumns(IResultSetConfig<?, T> config)
        {
            Set<IColumnDefinition<T>> columns = new HashSet<IColumnDefinition<T>>();
            HashSet<String> columnIDs = new HashSet<String>();
            for (IColumnDefinition<T> definition : headerColumnDefinitions)
            {
                columns.add(definition);
                columnIDs.add(definition.getIdentifier());
            }
            Set<IColumnDefinition<T>> columnsFromConfig = config.getAvailableColumns();
            for (IColumnDefinition<T> definition : columnsFromConfig)
            {
                if (columnIDs.contains(definition.getIdentifier()) == false)
                {
                    columns.add(definition);
                }
            }
            return columns;
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
            String sortField = resultConfig.getSortInfo().getSortField();
            if (sortField != null)
            {
                ids.add(sortField);
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
                String value = getOriginalValue(column, rowModel);
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

    // all cache access is done in a monitor
    private final Map<K, Future<?>> cache = Collections
            .synchronizedMap(new HashMap<K, Future<?>>());

    private final Set<K> lockedResultSets = Collections.synchronizedSet(new HashSet<K>());

    private final ThreadPoolExecutor executor = new NamingThreadPoolExecutor(
            "Background Table Loader").corePoolSize(10).daemonize();

    private final XMLPropertyTransformer xmlPropertyTransformer = new XMLPropertyTransformer();

    private final IColumnCalculator columnCalculator;

    public CachedResultSetManager(final IResultSetKeyGenerator<K> resultSetKeyProvider,
            ICustomColumnsProvider customColumnsProvider)
    {
        this(resultSetKeyProvider, customColumnsProvider, new IColumnCalculator()
            {
                @Override
                public <T> List<PrimitiveValue> evalCustomColumn(List<T> data,
                        GridCustomColumn customColumn, Set<IColumnDefinition<T>> availableColumns,
                        boolean errorMessagesAreLong)
                {
                    return GridExpressionUtils.evalCustomColumn(TableDataProviderFactory
                            .createDataProvider(data, new ArrayList<IColumnDefinition<T>>(
                                    availableColumns)), customColumn, errorMessagesAreLong);
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
    private static final <T> T cast(final Object object)
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

            if (DataTypeCode.TIMESTAMP.equals(gridFilterInfo.getFilteredField().tryToGetDataType()))
            {
                result.setDateFilterValue(pattern);
            } else
            {
                result.setFilterValue(pattern);
            }
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
            IColumnDefinition<T> definition = filteredField;
            String value = getOriginalValue(definition, row).toLowerCase();
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
            ITableDataProvider dataProvider =
                    TableDataProviderFactory.createDataProvider(rows,
                            new ArrayList<IColumnDefinition<T>>(availableColumns));
            List<Integer> indices =
                    GridExpressionUtils.applyCustomFilter(dataProvider, customFilterInfo);
            filteredRows = new ArrayList<GridRowModel<T>>();
            for (Integer index : indices)
            {
                filteredRows.add(rows.get(index));
            }
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

    private static <T> void sortData(final GridRowModels<T> data, final SortInfo sortInfo,
            final Set<IColumnDefinition<T>> availableColumns)
    {
        assert data != null : "Unspecified data.";
        assert sortInfo != null : "Unspecified sort information.";
        if (data.size() == 0)
        {
            return;
        }
        final SortDir sortDir = sortInfo.getSortDir();
        final String sortField = sortInfo.getSortField();
        if (sortDir == SortDir.NONE || sortField == null)
        {
            return;
        } else
        {
            IColumnDefinition<T> sortFieldDefinition = null;
            for (IColumnDefinition<T> column : availableColumns)
            {
                if (sortField.equals(column.getIdentifier()))
                {
                    sortFieldDefinition = column;
                    Comparator<GridRowModel<T>> comparator =
                            ColumnSortUtils.createComparator(sortDir, sortFieldDefinition);
                    Collections.sort(data, comparator);
                    break;
                }
            }

        }
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

    @Override
    public final <T> IResultSet<K, T> getResultSet(final String sessionToken,
            final IResultSetConfig<K, T> resultConfig, final IOriginalDataProvider<T> dataProvider)
    {
        // Get the raw result set
        IResultSet<K, T> cachedResultSet =
                getRawResultSet(sessionToken, resultConfig, dataProvider);

        return cachedResultSet;
    }

    /**
     * Raw handling of retrieving the result set from the cache -- no HTML escaping is applied.
     */
    private final <T> IResultSet<K, T> getRawResultSet(final String sessionToken,
            final IResultSetConfig<K, T> resultConfig, final IOriginalDataProvider<T> dataProvider)
    {
        assert resultConfig != null : "Unspecified result configuration";
        assert dataProvider != null : "Unspecified data retriever";
        ResultSetFetchConfig<K> cacheConfig = resultConfig.getCacheConfig();
        ResultSetFetchMode mode = cacheConfig.getMode();
        debug("getResultSet(cache config = " + cacheConfig + ")");

        K dataKey = cacheConfig.tryGetResultSetKey();
        switch (mode)
        {
            case RECOMPUTE_AND_CACHE:
                return fetchAndCacheResultForSpecifiedKey(sessionToken, resultConfig, dataProvider,
                        dataKey);
            case CLEAR_COMPUTE_AND_CACHE:
                removeResultSet(dataKey);
                //$FALL-THROUGH$ -
            case COMPUTE_AND_CACHE:
                return fetchAndCacheResult(sessionToken, resultConfig, dataProvider);
            default:
                TableData<T> tableData = tryGetCachedTableData(dataKey);
                if (tableData != null)
                {
                    return calculateSortAndFilterResult(sessionToken, tableData, resultConfig,
                            dataKey, false);
                } else
                {
                    return fetchAndCacheResult(sessionToken, resultConfig, dataProvider);
                }

        }
    }

    private <T> IResultSet<K, T> fetchAndCacheResult(final String sessionToken,
            final IResultSetConfig<K, T> resultConfig, final IOriginalDataProvider<T> dataProvider)
    {
        final K dataKey = resultSetKeyProvider.createKey();
        return fetchAndCacheResultForSpecifiedKey(sessionToken, resultConfig, dataProvider, dataKey);
    }

    private <T> IResultSet<K, T> fetchAndCacheResultForSpecifiedKey(final String sessionToken,
            final IResultSetConfig<K, T> resultConfig, final IOriginalDataProvider<T> dataProvider,
            final K dataKey)
    {
        int limit = resultConfig.getLimit();
        if (limit == IResultSetConfig.NO_LIMIT)
        {
            limit = Integer.MAX_VALUE;
        }
        debug("Retrieving " + limit + " record for a new key " + dataKey);
        List<T> rows = dataProvider.getOriginalData(limit);
        final List<TableModelColumnHeader> headers = dataProvider.getHeaders();
        final TableData<T> tableData =
                new TableData<T>(rows, headers, customColumnsProvider, columnCalculator);
        xmlPropertyTransformer.transformXMLProperties(rows);

        Future<TableData<T>> future;
        boolean partial = rows.size() >= limit;
        if (partial)
        {
            debug("Only partially loaded data for key " + dataKey);
            future = loadCompleteTableInBackground(dataProvider, dataKey);
        } else
        {
            debug("Completely loaded for key " + dataKey);
            future = createFutureWhichIsPresent(dataKey, tableData);
        }
        addToCache(dataKey, future);
        // TODO, 2011-03-08, FJE: In connection with bug LMS-1960 I found that resultConfig
        // (which contains e.g. available columns) is often out-dated and therefore inconsistent
        // with tableData (provided by dataProvider). The bug is fixed (by checking index in
        // TypedTableGridColumnDefinition.tryGetComparableValue()), but now a new bug pops up: The
        // combo box of a column filter element can have the wrong values.
        return calculateSortAndFilterResult(sessionToken, tableData,
                createMatchingConfig(resultConfig, headers), dataKey, partial);
    }

    private <T> IResultSetConfig<K, T> createMatchingConfig(IResultSetConfig<K, T> resultSetConfig,
            List<TableModelColumnHeader> headers)
    {
        if (headers.isEmpty())
        {
            // Not TypedTableGrid
            return resultSetConfig;
        }
        if (hasNoStaleAvailableColumn(resultSetConfig, headers))
        {
            return resultSetConfig;
        }
        Set<IColumnDefinition<T>> newAvailableColumns = new HashSet<IColumnDefinition<T>>();
        Set<String> idsOfPresentedColumns = resultSetConfig.getIDsOfPresentedColumns();
        Set<String> newIdsOfPresentedColumns = new HashSet<String>();
        SortInfo sortInfo = resultSetConfig.getSortInfo();
        TableMap<String, GridColumnFilterInfo<T>> columnFilterInfos =
                getColumFilters(resultSetConfig);
        List<GridColumnFilterInfo<T>> newColumnFilterInfos =
                new ArrayList<GridColumnFilterInfo<T>>();
        for (TableModelColumnHeader header : headers)
        {
            @SuppressWarnings("unchecked")
            IColumnDefinition<T> definition =
                    (IColumnDefinition<T>) new TypedTableGridColumnDefinition<Serializable>(header,
                            null, "", null);
            newAvailableColumns.add(definition);
            String id = header.getId();
            if (header.isHidden() == false || idsOfPresentedColumns.contains(id))
            {
                newIdsOfPresentedColumns.add(id);
            }
            if (sortInfo != null)
            {
                String sortField = sortInfo.getSortField();
                if (sortField != null && sortField.equals(id))
                {
                    sortInfo.setSortField(id);
                }
            }
            GridColumnFilterInfo<T> filterInfo = columnFilterInfos.tryGet(id);
            if (filterInfo != null)
            {
                String pattern = filterInfo.tryGetFilterPattern();
                newColumnFilterInfos.add(new GridColumnFilterInfo<T>(definition, pattern));
            }
        }
        // add ids of presented custom columns
        for (String id : idsOfPresentedColumns)
        {
            if (id.startsWith("$"))
            {
                newIdsOfPresentedColumns.add(id);
            }
        }

        DefaultResultSetConfig<K, T> newConfig = new DefaultResultSetConfig<K, T>();
        newConfig.setAvailableColumns(newAvailableColumns);
        newConfig.setCacheConfig(resultSetConfig.getCacheConfig());
        newConfig.setCustomColumnErrorMessageLong(resultSetConfig.isCustomColumnErrorMessageLong());
        // custom filter will be ignored because it may be stale too
        GridFilters<T> newFilters =
                newColumnFilterInfos.isEmpty() ? GridFilters.<T> createEmptyFilter() : GridFilters
                        .createColumnFilter(newColumnFilterInfos);
        newConfig.setFilters(newFilters);
        newConfig.setGridDisplayId(resultSetConfig.tryGetGridDisplayId());
        newConfig.setIDsOfPresentedColumns(newIdsOfPresentedColumns);
        newConfig.setLimit(resultSetConfig.getLimit());
        newConfig.setOffset(resultSetConfig.getOffset());
        newConfig.setSortInfo(sortInfo);
        return newConfig;
    }

    private <T> TableMap<String, GridColumnFilterInfo<T>> getColumFilters(
            IResultSetConfig<K, T> resultSetConfig)
    {
        GridFilters<T> filters = resultSetConfig.getFilters();
        List<GridColumnFilterInfo<T>> filterInfosOrNull = filters.tryGetFilterInfos();
        if (filterInfosOrNull == null)
        {
            filterInfosOrNull = Collections.emptyList();
        }
        TableMap<String, GridColumnFilterInfo<T>> columnFilterInfos =
                new TableMap<String, GridColumnFilterInfo<T>>(filterInfosOrNull,
                        new IKeyExtractor<String, GridColumnFilterInfo<T>>()
                            {
                                @Override
                                public String getKey(GridColumnFilterInfo<T> e)
                                {
                                    return e.getFilteredField().getIdentifier();
                                }
                            });
        return columnFilterInfos;
    }

    private <T> boolean hasNoStaleAvailableColumn(IResultSetConfig<K, T> resultSetConfig,
            List<TableModelColumnHeader> headers)
    {
        Set<String> headerIds = new HashSet<String>();
        for (TableModelColumnHeader header : headers)
        {
            headerIds.add(header.getId());
        }
        Set<IColumnDefinition<T>> availableColumns = resultSetConfig.getAvailableColumns();
        if (availableColumns != null)
        {
            if (availableColumns.size() != headers.size())
            {
                return false;
            }
            for (IColumnDefinition<T> definition : availableColumns)
            {
                if (headerIds.contains(definition.getIdentifier()) == false)
                {
                    return false;
                }
            }
        }
        return true;
    }

    private <T> Future<TableData<T>> createFutureWhichIsPresent(final K dataKey,
            final TableData<T> tableData)
    {
        return new Future<TableData<T>>()
            {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning)
                {
                    return true;
                }

                @Override
                public boolean isCancelled()
                {
                    return false;
                }

                @Override
                public boolean isDone()
                {
                    return true;
                }

                @Override
                public TableData<T> get() throws InterruptedException, ExecutionException
                {
                    return tableData;
                }

                @Override
                public TableData<T> get(long timeout, TimeUnit unit) throws InterruptedException,
                        ExecutionException, TimeoutException
                {
                    return get();
                }
            };
    }

    private <T> Future<TableData<T>> loadCompleteTableInBackground(
            final IOriginalDataProvider<T> dataProvider, final K dataKey)
    {
        Future<TableData<T>> future;
        Callable<TableData<T>> callable = new Callable<TableData<T>>()
            {
                @Override
                public TableData<T> call() throws Exception
                {
                    List<T> rows = dataProvider.getOriginalData(Integer.MAX_VALUE);
                    List<TableModelColumnHeader> headers = dataProvider.getHeaders();
                    debug(rows.size() + " records loaded for key " + dataKey);
                    TableData<T> tableData =
                            new TableData<T>(rows, headers, customColumnsProvider, columnCalculator);
                    xmlPropertyTransformer.transformXMLProperties(rows);
                    return tableData;
                }
            };
        future = executor.submit(callable);
        return future;
    }

    private <T> void addToCache(K dataKey, Future<TableData<T>> tableData)
    {
        unlockResultSet(dataKey);
        cache.put(dataKey, tableData);
        debug(cache.size() + " keys in cache: " + cache.keySet());
    }

    private static <K, T> IResultSet<K, T> calculateSortAndFilterResult(String sessionToken,
            TableData<T> tableData, final IResultSetConfig<K, T> resultConfig, K dataKey,
            boolean partial)
    {
        GridRowModels<T> data = tableData.getRows(sessionToken, resultConfig);
        return filterLimitAndSort(resultConfig, data, dataKey, partial);
    }

    private <T> TableData<T> tryGetCachedTableData(K dataKey)
    {
        waitUntilUnlocked(dataKey);
        Future<TableData<T>> tableData = cast(cache.get(dataKey));
        if (tableData == null)
        {
            operationLog.warn("Reference to the stale cache key " + dataKey);
            return null;
        }
        try
        {
            return tableData.get();
        } catch (InterruptedException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (ExecutionException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex.getCause());
        }
    }

    private static <K, T> IResultSet<K, T> filterLimitAndSort(
            final IResultSetConfig<K, T> resultConfig, GridRowModels<T> data, K dataKey,
            boolean partial)
    {
        GridRowModels<T> filteredData =
                filterData(data, resultConfig.getAvailableColumns(), resultConfig.getFilters());
        final int size = filteredData.size();
        final int offset = getOffset(size, resultConfig.getOffset());
        final int limit = getLimit(size, resultConfig.getLimit(), offset);
        final SortInfo sortInfo = resultConfig.getSortInfo();
        sortData(filteredData, sortInfo, resultConfig.getAvailableColumns());
        final GridRowModels<T> list = subList(filteredData, offset, limit);
        return new DefaultResultSet<K, T>(dataKey, list, size, partial);
    }

    @Override
    public final void removeResultSet(final K resultSetKey)
    {
        unlockResultSet(resultSetKey);
        assert resultSetKey != null : "Unspecified data key holder.";
        if (cache.remove(resultSetKey) != null)
        {
            debug(String.format("Result set for key '%s' has been removed.", resultSetKey));
        } else
        {
            operationLog.warn(String.format("No result set for key '%s' could be found.",
                    resultSetKey));
        }
    }

    @Override
    public void lockResultSet(K resultSetKey)
    {
        lockedResultSets.add(resultSetKey);
    }

    private void unlockResultSet(K resultSetKey)
    {
        lockedResultSets.remove(resultSetKey);
    }

    private void waitUntilUnlocked(K resultSetKey)
    {
        while (lockedResultSets.contains(resultSetKey))
        {
            try
            {
                Thread.sleep(100);
            } catch (Exception ex)
            {
                operationLog.error("Interrupted while waiting on unlocking " + resultSetKey, ex);
            }
        }
    }

    private void debug(String msg)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(msg);
        }
    }
}
