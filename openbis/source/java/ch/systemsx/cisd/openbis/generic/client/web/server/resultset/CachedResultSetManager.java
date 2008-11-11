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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.comparators.ReverseComparator;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.utilities.FieldComparator;
import ch.systemsx.cisd.common.utilities.TokenGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetKeyHolder;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SortInfo;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SortInfo.SortDir;

/**
 * A {@link IResultSetManager} implementation which caches the full data retrieved using
 * {@link IResultSetRetriever}.
 * 
 * @author Christian Ribeaud
 */
public final class CachedResultSetManager<K> implements IResultSetManager<K>, Serializable
{
    private static final long serialVersionUID = 1L;

    private final IResultSetKeyProvider<K> resultSetKeyProvider;

    @Private
    final Map<K, List<?>> results = new HashMap<K, List<?>>();

    public CachedResultSetManager(final IResultSetKeyProvider<K> resultSetKeyProvider)
    {
        this.resultSetKeyProvider = resultSetKeyProvider;
    }

    @SuppressWarnings("unchecked")
    private final <T> List<T> cast(final List<?> list)
    {
        return (List<T>) list;
    }

    @SuppressWarnings("unchecked")
    private final <T> void sortData(final List<T> data, final SortInfo sortInfo)
    {
        // TODO: Remove null check.
        if (sortInfo == null)
        {
            return;
        }
        final SortDir sortDir = sortInfo.getSortDir();
        if (sortDir == SortDir.NONE)
        {
            return;
        }
        final FieldComparator fieldComparator = new FieldComparator(sortInfo.getSortField());
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

    //
    // IDataManager
    //

    public final synchronized <T> IResultSet<K, T> getResultSet(
            final IResultSetConfig<K> resultConfig, final IResultSetRetriever<T> dataRetriever)
    {
        assert resultConfig != null : "Unspecified result configuration";
        assert dataRetriever != null : "Unspecified data retriever";
        final List<T> data;
        K dataKey = resultConfig.getResultSetKey();
        if (dataKey == null)
        {
            dataKey = resultSetKeyProvider.getKey();
            data = dataRetriever.getData();
            results.put(dataKey, data);
        } else
        {
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

    // TODO: Put doc here.
    private final static <T> List<T> subList(final List<T> data, final int offset, final int limit)
    {
        final List<T> list = new ArrayList<T>();
        for (int i = offset; i < offset + limit; i++)
        {
            list.add(data.get(i));
        }
        return list;
    }

    public final synchronized void removeData(final IResultSetKeyHolder<K> dataKeyHolder)
    {
        assert dataKeyHolder != null : "Unspecified data key holder.";
        results.remove(dataKeyHolder.getResultSetKey());
    }

    //
    // Helper classes
    //

    public final static class TokenBasedResultSetKeyProvider implements
            IResultSetKeyProvider<String>
    {

        private final TokenGenerator tokenGenerator;

        public TokenBasedResultSetKeyProvider()
        {
            this.tokenGenerator = new TokenGenerator();
        }

        //
        // IResultSetKeyProvider
        //

        public final String getKey()
        {
            return tokenGenerator.getNewToken(System.currentTimeMillis());
        }
    }

    public final static class CounterBasedResultSetKeyProvider implements
            IResultSetKeyProvider<Integer>
    {
        private int counter;

        public CounterBasedResultSetKeyProvider()
        {
        }

        //
        // IResultSetKeyProvider
        //

        public final Integer getKey()
        {
            return counter++;
        }
    }
}
