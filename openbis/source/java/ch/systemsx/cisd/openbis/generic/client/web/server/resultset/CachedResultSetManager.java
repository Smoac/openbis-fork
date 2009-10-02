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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.evaluator.EvaluatorException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.CustomFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.util.FilterUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GridFilterInfo;
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

    private final IResultSetKeyGenerator<K> resultSetKeyProvider;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, CachedResultSetManager.class);

    @Private
    final Map<K, List<?>> results = new HashMap<K, List<?>>();

    public CachedResultSetManager(final IResultSetKeyGenerator<K> resultSetKeyProvider)
    {
        this.resultSetKeyProvider = resultSetKeyProvider;
    }

    @SuppressWarnings("unchecked")
    private final <T> List<T> cast(final List<?> list)
    {
        return (List<T>) list;
    }

    /**
     * Server-side filter info object.
     */
    private static class FilterInfo<T>
    {
        private final IColumnDefinition<T> filteredField;

        private final String[] filterExpressionAlternatives;

        FilterInfo(GridFilterInfo<T> gridFilterInfo)
        {
            this.filteredField = gridFilterInfo.getFilteredField();

            // - each token is used as an alternative
            // - tokens are separated with whitespace
            // - quotes (both double and single quote) wrap data into tokens
            StrTokenizer tokenizer =
                    new StrTokenizer(gridFilterInfo.getFilterPattern().toLowerCase());
            tokenizer.setQuoteMatcher(StrMatcher.quoteMatcher());
            this.filterExpressionAlternatives = tokenizer.getTokenArray();
        }

        @SuppressWarnings("unchecked")
        static <T> FilterInfo<T> create(GridFilterInfo<T> filterInfo)
        {
            return new FilterInfo(filterInfo);
        }

        final IColumnDefinition<T> getFilteredField()
        {
            return filteredField;
        }

        final String[] getFilterExpressionAlternatives()
        {
            return filterExpressionAlternatives;
        }
    }

    private static final <T> List<T> filterData(final List<T> rows,
            Set<IColumnDefinition<T>> availableColumns,
            final List<GridFilterInfo<T>> filterInfos, CustomFilterInfo<T> customFilterInfo)
    {
        List<T> filtered = new ArrayList<T>();
        if (customFilterInfo != null)
        {
            try
            {
                FilterUtils.applyCustomFilter(rows, availableColumns, customFilterInfo, filtered);
            } catch (Exception ex)
            {
                String msg;
                if (ex instanceof EvaluatorException)
                {
                    msg = "Problem occured during applying the filter: " + ex.getMessage();
                } else
                {
                    msg = "Serious problem occured during applying the filter: " + ex;
                }
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(msg, ex);
                }
                throw new UserFailureException(msg);
            }
        } else
        {
            List<FilterInfo<T>> serverFilterInfos =
                    new ArrayList<FilterInfo<T>>(filterInfos.size());
            for (GridFilterInfo<T> info : filterInfos)
            {
                serverFilterInfos.add(FilterInfo.create(info));

            }
            for (T row : rows)
            {
                if (isMatching(row, serverFilterInfos))
                {
                    filtered.add(row);
                }
            }
        }
        return filtered;
    }

    // returns true if a row matches all the filters
    private static final <T> boolean isMatching(final T row, final List<FilterInfo<T>> filterInfos)
    {
        for (FilterInfo<T> filter : filterInfos)
        {
            if (isMatching(row, filter) == false)
            {
                return false;
            }
        }
        return true;
    }

    private static final <T> boolean isMatching(final T row, final FilterInfo<T> filterInfo)
    {
        IColumnDefinition<T> filteredField = filterInfo.getFilteredField();
        String value = filteredField.getValue(row).toLowerCase();
        return isMatching(value, filterInfo.getFilterExpressionAlternatives());
    }

    private static boolean isMatching(String value, String[] filterPatternAlternatives)
    {
        for (String pattern : filterPatternAlternatives)
        {
            if (value.contains(pattern))
            {
                return true;
            }
        }
        return false;
    }

    private final <T> void sortData(final List<T> data, final SortInfo<T> sortInfo)
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
        Comparator<T> comparator = createComparator(sortDir, sortField);
        Collections.sort(data, comparator);
    }

    private static <T> Comparator<T> createComparator(final SortDir sortDir,
            final IColumnDefinition<T> sortField)
    {
        Comparator<T> comparator = new Comparator<T>()
            {

                @SuppressWarnings("unchecked")
                public int compare(T o1, T o2)
                {
                    Comparable v1 = sortField.getComparableValue(o1);
                    Comparable v2 = sortField.getComparableValue(o2);
                    return v1.compareTo(v2);
                }
            };
        return applySortDir(sortDir, comparator);
    }

    @SuppressWarnings("unchecked")
    private static <T> Comparator<T> applySortDir(final SortDir sortDir, Comparator<T> comparator)
    {
        if (sortDir == SortDir.DESC)
        {
            return new ReverseComparator(comparator);
        } else
        {
            return comparator;
        }
    }

    @Private
    final static int getLimit(final int size, final int limit, final int offset)
    {
        assert size > -1 : "Size can not be negative";
        if (limit < 0)
        {
            return size - offset;
        }
        return Math.min(size - offset, limit);
    }

    @Private
    final static int getOffset(final int size, final int offset)
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
    private final static <T> List<T> subList(final List<T> data, final int offset, final int limit)
    {
        final int toIndex = offset + limit;
        return new ArrayList<T>(data.subList(offset, toIndex));
    }

    //
    // IDataManager
    //

    public final synchronized <T> IResultSet<K, T> getResultSet(
            final IResultSetConfig<K, T> resultConfig, final IOriginalDataProvider<T> dataProvider)
    {
        assert resultConfig != null : "Unspecified result configuration";
        assert dataProvider != null : "Unspecified data retriever";
        List<T> data;
        K dataKey = resultConfig.getResultSetKey();
        if (dataKey == null)
        {
            debug("Unknown result set key: retrieving the data.");
            dataKey = resultSetKeyProvider.createKey();
            data = dataProvider.getOriginalData();
            results.put(dataKey, data);
        } else
        {
            debug(String.format("Data for result set key '%s' already cached.", dataKey));
            data = cast(results.get(dataKey));
            if (data == null)
            {
                debug(String.format("Invalid result set key '%s'.", dataKey));
            }
        }
        assert data != null : "Unspecified data";
        data =
                filterData(data, resultConfig.getAvailableColumns(), resultConfig.getFilterInfos(),
                        resultConfig.tryGetCustomFilterInfo());
        final int size = data.size();
        final int offset = getOffset(size, resultConfig.getOffset());
        final int limit = getLimit(size, resultConfig.getLimit(), offset);
        final SortInfo<T> sortInfo = resultConfig.getSortInfo();
        sortData(data, sortInfo);
        final List<T> list = subList(data, offset, limit);
        return new DefaultResultSet<K, T>(dataKey, list, size);
    }

    public final synchronized void removeResultSet(final K resultSetKey)
    {
        assert resultSetKey != null : "Unspecified data key holder.";
        if (results.remove(resultSetKey) != null)
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
