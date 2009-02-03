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

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GridFilterInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SortInfo.SortDir;

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

    private static final <T> List<T> filterData(final List<T> rows,
            final List<GridFilterInfo<T>> filterInfos)
    {
        List<T> filtered = new ArrayList<T>();
        for (T row : rows)
        {
            if (isMatching(row, filterInfos))
            {
                filtered.add(row);
            }
        }
        return filtered;
    }

    // returns true if a row matches all the filters
    private static final <T> boolean isMatching(final T row,
            final List<GridFilterInfo<T>> filterInfos)
    {
        for (GridFilterInfo<T> filter : filterInfos)
        {
            if (isMatching(row, filter) == false)
            {
                return false;
            }
        }
        return true;
    }

    private static final <T> boolean isMatching(final T row, final GridFilterInfo<T> filterInfo)
    {
        IColumnDefinition<T> filteredField = filterInfo.getFilteredField();
        String value = filteredField.getValue(row);
        return isMatching(value, filterInfo.getFilterPattern());
    }

    private static boolean isMatching(String value, String filterPattern)
    {
        return value.toLowerCase().startsWith(filterPattern.toLowerCase());
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

                public int compare(T o1, T o2)
                {
                    String v1 = sortField.getValue(o1);
                    String v2 = sortField.getValue(o2);
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
            operationLog.debug("Unknown result set key: retrieving the data.");
            dataKey = resultSetKeyProvider.createKey();
            data = dataProvider.getOriginalData();
            results.put(dataKey, data);
        } else
        {
            operationLog.debug(String.format("Data for result set key '%s' already cached.",
                    dataKey));
            data = cast(results.get(dataKey));
        }
        assert data != null : "Unspecified data";
        final int totalLength = data.size();
        data = filterData(data, resultConfig.getFilterInfos());
        final int size = data.size();
        final int offset = getOffset(size, resultConfig.getOffset());
        final int limit = getLimit(size, resultConfig.getLimit(), offset);
        final SortInfo<T> sortInfo = resultConfig.getSortInfo();
        sortData(data, sortInfo);
        final List<T> list = subList(data, offset, limit);
        return new DefaultResultSet<K, T>(dataKey, list, totalLength);
    }

    public final synchronized void removeResultSet(final K resultSetKey)
    {
        assert resultSetKey != null : "Unspecified data key holder.";
        if (results.remove(resultSetKey) != null)
        {
            operationLog.debug(String.format("Result set for key '%s' has been removed.",
                    resultSetKey));
        } else
        {
            operationLog.debug(String.format("No result set for key '%s' could be found.",
                    resultSetKey));
        }
    }
}
