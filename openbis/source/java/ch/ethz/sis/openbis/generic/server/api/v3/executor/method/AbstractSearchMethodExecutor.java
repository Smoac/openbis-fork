/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.CacheMode;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.FetchOptionsMatcher;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.SortAndPage;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractObjectSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SearchResult;

/**
 * @author pkupczyk
 */
public abstract class AbstractSearchMethodExecutor<OBJECT, OBJECT_PE, CRITERION extends AbstractObjectSearchCriterion<?>, FETCH_OPTIONS extends FetchOptions<OBJECT>>
        extends AbstractMethodExecutor implements ISearchMethodExecutor<OBJECT, CRITERION, FETCH_OPTIONS>
{

    @Autowired
    private CacheManager cacheManager;

    protected abstract ISearchObjectExecutor<CRITERION, OBJECT_PE> getSearchExecutor();

    protected abstract ITranslator<OBJECT_PE, OBJECT, FETCH_OPTIONS> getTranslator();

    @Override
    public SearchResult<OBJECT> search(final String sessionToken, final CRITERION criterion, final FETCH_OPTIONS fetchOptions)
    {
        return executeInContext(sessionToken, new IMethodAction<SearchResult<OBJECT>>()
            {
                @Override
                public SearchResult<OBJECT> execute(IOperationContext context)
                {
                    Collection<OBJECT> allResults = searchAndTranslate(context, criterion, fetchOptions);
                    List<OBJECT> sortedAndPaged = sortAndPage(context, allResults, fetchOptions);
                    return new SearchResult<OBJECT>(sortedAndPaged, allResults.size());
                }
            });
    }

    @SuppressWarnings("unchecked")
    private Collection<OBJECT> searchAndTranslate(IOperationContext context, CRITERION criterion, FETCH_OPTIONS fetchOptions)
    {
        if (CacheMode.NO_CACHE.equals(fetchOptions.getCacheMode()))
        {
            return doSearchAndTranslate(context, criterion, fetchOptions);
        } else if (CacheMode.CACHE.equals(fetchOptions.getCacheMode()) || CacheMode.RELOAD_AND_CACHE.equals(fetchOptions.getCacheMode()))
        {
            Cache cache = cacheManager.getCache("searchCache");
            CacheKey key = new CacheKey(context.getSession().getSessionToken(), fetchOptions);
            Collection<OBJECT> results = null;

            if (CacheMode.RELOAD_AND_CACHE.equals(fetchOptions.getCacheMode()))
            {
                cache.evict(key);
            } else
            {
                ValueWrapper wrapper = cache.get(key);
                if (wrapper != null)
                {
                    results = (Collection<OBJECT>) wrapper.get();
                }
            }

            if (results == null)
            {
                results = doSearchAndTranslate(context, criterion, fetchOptions);
                cache.put(key, results);
            }

            return results;
        } else
        {
            throw new IllegalArgumentException("Unsupported cache mode: " + fetchOptions.getCacheMode());
        }
    }

    private Collection<OBJECT> doSearchAndTranslate(IOperationContext context, CRITERION criterion, FETCH_OPTIONS fetchOptions)
    {
        List<OBJECT_PE> ids = getSearchExecutor().search(context, criterion);
        TranslationContext translationContext = new TranslationContext(context.getSession());
        Map<OBJECT_PE, OBJECT> idToObjectMap = getTranslator().translate(translationContext, ids, fetchOptions);
        return idToObjectMap.values();
    }

    private List<OBJECT> sortAndPage(IOperationContext context, Collection<OBJECT> results, FETCH_OPTIONS fetchOptions)
    {
        if (results == null || results.isEmpty())
        {
            return Collections.emptyList();
        }

        SortAndPage sap = new SortAndPage();
        Collection<OBJECT> objects = sap.sortAndPage(results, fetchOptions);

        return new ArrayList<OBJECT>(objects);
    }

    private static class CacheKey
    {

        private String sessionToken;

        private FetchOptions<?> fetchOptions;

        public CacheKey(String sessionToken, FetchOptions<?> fetchOptions)
        {
            this.sessionToken = sessionToken;
            this.fetchOptions = fetchOptions;
        }

        @Override
        public int hashCode()
        {
            return sessionToken.hashCode() + fetchOptions.getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }

            CacheKey other = (CacheKey) obj;
            return sessionToken.equals(other.sessionToken) && FetchOptionsMatcher.arePartsEqual(fetchOptions, other.fetchOptions);
        }
    }

}
