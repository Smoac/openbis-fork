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
import ch.systemsx.cisd.common.utilities.TokenGenerator;
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

    private final ComparatorRegistry comparatorRegistry;

    public CachedResultSetManager(final IResultSetKeyGenerator<K> resultSetKeyProvider)
    {
        this.resultSetKeyProvider = resultSetKeyProvider;
        comparatorRegistry = new ComparatorRegistry();
    }

    @SuppressWarnings("unchecked")
    private final <T> List<T> cast(final List<?> list)
    {
        return (List<T>) list;
    }

    @SuppressWarnings("unchecked")
    private final <T> void sortData(final List<T> data, final SortInfo sortInfo)
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
        }
        final Comparator<T> fieldComparator =
                comparatorRegistry.getComparator((Class<T>) data.get(0).getClass(), sortField);
        Collections.sort(data, sortDir == SortDir.ASC ? fieldComparator : new ReverseComparator(
                fieldComparator));
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
            final IResultSetConfig<K> resultConfig, final IOriginalDataProvider<T> dataProvider)
    {
        assert resultConfig != null : "Unspecified result configuration";
        assert dataProvider != null : "Unspecified data retriever";
        final List<T> data;
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
        final int size = data.size();
        final int offset = getOffset(size, resultConfig.getOffset());
        final int limit = getLimit(size, resultConfig.getLimit(), offset);
        final SortInfo sortInfo = resultConfig.getSortInfo();
        sortData(data, sortInfo);
        final List<T> list = subList(data, offset, limit);
        return new DefaultResultSet<K, T>(dataKey, list, size);
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

    //
    // Helper classes
    //

    public final static class TokenBasedResultSetKeyGenerator implements
            IResultSetKeyGenerator<String>
    {

        private static final long serialVersionUID = 1L;

        private final TokenGenerator tokenGenerator;

        public TokenBasedResultSetKeyGenerator()
        {
            this.tokenGenerator = new TokenGenerator();
        }

        //
        // IResultSetKeyProvider
        //

        public final String createKey()
        {
            return tokenGenerator.getNewToken(System.currentTimeMillis());
        }
    }

    public final static class CounterBasedResultSetKeyGenerator implements
            IResultSetKeyGenerator<Integer>
    {
        private static final long serialVersionUID = 1L;

        private int counter;

        public CounterBasedResultSetKeyGenerator()
        {
        }

        //
        // IResultSetKeyProvider
        //

        public final Integer createKey()
        {
            return counter++;
        }
    }
}
