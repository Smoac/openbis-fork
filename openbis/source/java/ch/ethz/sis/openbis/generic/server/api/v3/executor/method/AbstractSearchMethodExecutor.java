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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public abstract class AbstractSearchMethodExecutor<OBJECT, OBJECT_PE, CRITERION extends AbstractObjectSearchCriterion<?>, FETCH_OPTIONS extends FetchOptions<OBJECT>>
        extends AbstractMethodExecutor implements ISearchMethodExecutor<OBJECT, CRITERION, FETCH_OPTIONS>
{

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    @Autowired
    private CacheManager cacheManager;

    protected abstract ISearchObjectExecutor<CRITERION, OBJECT_PE> getSearchExecutor();

    protected abstract ITranslator<OBJECT_PE, OBJECT, FETCH_OPTIONS> getTranslator();

    @Override
    public SearchResult<OBJECT> search(final String sessionToken, final CRITERION criterion, final FETCH_OPTIONS fetchOptions)
    {
        if (criterion == null)
        {
            throw new IllegalArgumentException("Criterion cannot be null.");
        }
        if (fetchOptions == null)
        {
            throw new IllegalArgumentException("Fetch options cannot be null.");
        }
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

    private Collection<OBJECT> searchAndTranslate(IOperationContext context, CRITERION criterion, FETCH_OPTIONS fetchOptions)
    {
        operationLog.info("Cache mode: " + fetchOptions.getCacheMode());

        if (CacheMode.NO_CACHE.equals(fetchOptions.getCacheMode()))
        {
            return doSearchAndTranslate(context, criterion, fetchOptions);
        } else if (CacheMode.CACHE.equals(fetchOptions.getCacheMode()) || CacheMode.RELOAD_AND_CACHE.equals(fetchOptions.getCacheMode()))
        {
            CacheEntry entry = getCacheEntry(context, criterion, fetchOptions);
            populateCacheEntry(context, criterion, fetchOptions, entry);
            return entry.getObjects();
        } else
        {
            throw new IllegalArgumentException("Unsupported cache mode: " + fetchOptions.getCacheMode());
        }
    }

    private Collection<OBJECT> doSearchAndTranslate(IOperationContext context, CRITERION criterion, FETCH_OPTIONS fetchOptions)
    {
        operationLog.info("Searching...");

        List<OBJECT_PE> ids = getSearchExecutor().search(context, criterion);

        operationLog.info("Found " + ids.size() + " object id(s).");

        TranslationContext translationContext = new TranslationContext(context.getSession());
        Map<OBJECT_PE, OBJECT> idToObjectMap = getTranslator().translate(translationContext, ids, fetchOptions);
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

    private Cache getCache()
    {
        return cacheManager.getCache("searchCache");
    }

    @SuppressWarnings("unchecked")
    private CacheEntry getCacheEntry(IOperationContext context, CRITERION criterion, FETCH_OPTIONS fetchOptions)
    {
        Cache cache = getCache();
        CacheKey key = new CacheKey(context.getSession().getSessionToken(), criterion, fetchOptions);
        CacheEntry entry = null;

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
                cache.evict(key);
            }

            ValueWrapper wrapper = cache.get(key);

            if (wrapper == null)
            {
                entry = new CacheEntry();
                cache.put(key, entry);
                context.getSession().addCleanupListener(new CacheCleanupListener(key));
            } else
            {
                entry = (CacheEntry) wrapper.get();
            }

            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Released lock on session " + context.getSession().hashCode());
            }
        }

        return entry;
    }

    private void populateCacheEntry(IOperationContext context, CRITERION criterion, FETCH_OPTIONS fetchOptions, CacheEntry entry)
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
                entry.setObjects(doSearchAndTranslate(context, criterion, fetchOptions));

                operationLog.info("Updated cache entry " + entry.hashCode() + " to contain search result with " + entry.getObjects().size()
                        + " object(s).");
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

    public class CacheKey implements Serializable
    {

        private static final long serialVersionUID = 1L;

        private String sessionToken;

        private CRITERION criterion;

        private FETCH_OPTIONS fetchOptions;

        public CacheKey(String sessionToken, CRITERION criterion, FETCH_OPTIONS fetchOptions)
        {
            this.sessionToken = sessionToken;
            this.criterion = criterion;
            this.fetchOptions = fetchOptions;
        }

        @Override
        public int hashCode()
        {
            return sessionToken.hashCode() + criterion.getClass().hashCode() + fetchOptions.getClass().hashCode();
        }

        @Override
        @SuppressWarnings("unchecked")
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
            return sessionToken.equals(other.sessionToken) && criterion.equals(other.criterion)
                    && FetchOptionsMatcher.arePartsEqual(fetchOptions, other.fetchOptions);
        }
    }

    public class CacheEntry implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private Collection<OBJECT> objects;

        public Collection<OBJECT> getObjects()
        {
            return objects;
        }

        public void setObjects(Collection<OBJECT> objects)
        {
            this.objects = objects;
        }

    }

    private class CacheCleanupListener implements Session.ISessionCleaner
    {

        private CacheKey key;

        public CacheCleanupListener(CacheKey key)
        {
            this.key = key;
        }

        @Override
        public void cleanup()
        {
            ValueWrapper wrapper = getCache().get(key);

            if (wrapper != null)
            {
                operationLog.info("Clean up cached search result on logout.");
                getCache().evict(key);
            }
        }

    }

}
