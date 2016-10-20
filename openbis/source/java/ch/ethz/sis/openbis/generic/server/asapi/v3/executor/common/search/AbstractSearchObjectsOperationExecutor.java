/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.CacheMode;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.cache.ISearchCache;
import ch.ethz.sis.openbis.generic.server.asapi.v3.cache.SearchCacheCleanupListener;
import ch.ethz.sis.openbis.generic.server.asapi.v3.cache.SearchCacheEntry;
import ch.ethz.sis.openbis.generic.server.asapi.v3.cache.SearchCacheKey;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.OperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.SortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author pkupczyk
 */
public abstract class AbstractSearchObjectsOperationExecutor<OBJECT, OBJECT_PE, CRITERIA extends AbstractSearchCriteria, FETCH_OPTIONS extends FetchOptions<OBJECT>>
        extends OperationExecutor<SearchObjectsOperation<CRITERIA, FETCH_OPTIONS>, SearchObjectsOperationResult<OBJECT>>
        implements ISearchObjectsOperationExecutor
{

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    @Autowired
    protected ISearchCache<CRITERIA, FETCH_OPTIONS, OBJECT> cache;

    protected abstract List<OBJECT_PE> doSearch(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions);

    protected abstract Map<OBJECT_PE, OBJECT> doTranslate(TranslationContext translationContext, List<OBJECT_PE> ids, FETCH_OPTIONS fetchOptions);

    protected abstract SearchObjectsOperationResult<OBJECT> getOperationResult(SearchResult<OBJECT> searchResult);

    @Override
    protected SearchObjectsOperationResult<OBJECT> doExecute(IOperationContext context, SearchObjectsOperation<CRITERIA, FETCH_OPTIONS> operation)
    {
        CRITERIA criteria = operation.getCriteria();
        FETCH_OPTIONS fetchOptions = operation.getFetchOptions();

        if (criteria == null)
        {
            throw new IllegalArgumentException("Criteria cannot be null.");
        }
        if (fetchOptions == null)
        {
            throw new IllegalArgumentException("Fetch options cannot be null.");
        }

        Collection<OBJECT> allResults = searchAndTranslate(context, criteria, fetchOptions);
        List<OBJECT> sortedAndPaged = sortAndPage(context, allResults, fetchOptions);

        SearchResult<OBJECT> searchResult = new SearchResult<OBJECT>(sortedAndPaged, allResults.size());
        return getOperationResult(searchResult);
    }

    private Collection<OBJECT> searchAndTranslate(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        CacheMode cacheMode = fetchOptions.getCacheMode();
        operationLog.info("Cache mode: " + cacheMode);

        if (CacheMode.NO_CACHE.equals(cacheMode))
        {
            return doSearchAndTranslate(context, criteria, fetchOptions);
        } else if (CacheMode.CACHE.equals(cacheMode) || CacheMode.RELOAD_AND_CACHE.equals(cacheMode))
        {
            SearchCacheEntry<OBJECT> entry = getCacheEntry(context, criteria, fetchOptions);
            populateCacheEntry(context, criteria, fetchOptions, entry);
            return entry.getObjects();
        } else
        {
            throw new IllegalArgumentException("Unsupported cache mode: " + cacheMode);
        }
    }

    protected Collection<OBJECT> doSearchAndTranslate(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        operationLog.info("Searching...");

        List<OBJECT_PE> ids = doSearch(context, criteria, fetchOptions);

        operationLog.info("Found " + ids.size() + " object id(s).");

        TranslationContext translationContext = new TranslationContext(context.getSession());
        Map<OBJECT_PE, OBJECT> idToObjectMap = doTranslate(translationContext, ids, fetchOptions);
        Collection<OBJECT> objects = idToObjectMap.values();

        operationLog.info("Translated " + objects.size() + " object(s).");

        return objects;
    }

    private List<OBJECT> sortAndPage(IOperationContext context, Collection<OBJECT> results, FETCH_OPTIONS fetchOptions)
    {
        if (results == null || results.isEmpty())
        {
            return Collections.emptyList();
        }

        SortAndPage sap = new SortAndPage();
        Collection<OBJECT> objects = sap.sortAndPage(results, fetchOptions);

        operationLog.info("Return " + objects.size() + " object(s) after sorting and paging.");

        return new ArrayList<OBJECT>(objects);
    }

    private SearchCacheEntry<OBJECT> getCacheEntry(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        SearchCacheKey<CRITERIA, FETCH_OPTIONS> key =
                new SearchCacheKey<CRITERIA, FETCH_OPTIONS>(context.getSession().getSessionToken(), criteria, fetchOptions);
        SearchCacheEntry<OBJECT> entry = null;

        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Will try to lock on session " + context.getSession().hashCode());
        }

        synchronized (context.getSession())
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Locked on session " + context.getSession().hashCode());
            }

            if (CacheMode.RELOAD_AND_CACHE.equals(fetchOptions.getCacheMode()))
            {
                cache.remove(key);
            }

            entry = cache.get(key);

            if (entry == null)
            {
                entry = new SearchCacheEntry<OBJECT>();
                cache.put(key, entry);
                context.getSession().addCleanupListener(new SearchCacheCleanupListener<CRITERIA, FETCH_OPTIONS>(cache, key));
            }

            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Released lock on session " + context.getSession().hashCode());
            }
        }

        return entry;
    }

    private void populateCacheEntry(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions, SearchCacheEntry<OBJECT> entry)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Will try to lock on cache entry " + entry.hashCode());
        }

        synchronized (entry)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Locked on cache entry " + entry.hashCode());
            }

            if (entry.getObjects() == null)
            {
                entry.setObjects(doSearchAndTranslate(context, criteria, fetchOptions));
                SearchCacheKey<CRITERIA, FETCH_OPTIONS> key =
                        new SearchCacheKey<CRITERIA, FETCH_OPTIONS>(context.getSession().getSessionToken(), criteria, fetchOptions);
                // put the entry to the cache again to trigger the size recalculation
                cache.put(key, entry);
            } else
            {
                operationLog.info("Found cache entry " + entry.hashCode() + " that contains search result with " + entry.getObjects().size()
                        + " object(s).");
            }

            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Released lock on cache entry " + entry.hashCode());
            }
        }
    }

}
