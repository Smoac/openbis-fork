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

import java.util.*;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

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

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;

/**
 * @author pkupczyk
 */
public abstract class AbstractSearchObjectsOperationExecutor<OBJECT, OBJECT_PE, CRITERIA extends AbstractSearchCriteria, FETCH_OPTIONS extends FetchOptions<OBJECT>>
        extends OperationExecutor<SearchObjectsOperation<CRITERIA, FETCH_OPTIONS>, SearchObjectsOperationResult<OBJECT>>
        implements ISearchObjectsOperationExecutor
{

    private static final String[] SORTS_TO_IGNORE = new String[] { EntityWithPropertiesSortOptions.FETCHED_FIELDS_SCORE };

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    @Autowired
    protected ISearchCache<CRITERIA, FETCH_OPTIONS, OBJECT> cache;

    protected abstract List<OBJECT_PE> doSearch(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions);

    protected abstract Map<OBJECT_PE, OBJECT> doTranslate(TranslationContext translationContext, List<OBJECT_PE> ids, FETCH_OPTIONS fetchOptions);

    protected abstract SearchObjectsOperationResult<OBJECT> getOperationResult(SearchResult<OBJECT> searchResult);

    protected abstract ILocalSearchManager<CRITERIA, OBJECT, OBJECT_PE> getSearchManager();

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
        List<OBJECT> sortedAndPaged = sortAndPage(context, allResults, criteria, fetchOptions);

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

    private List<OBJECT> sortAndPage(IOperationContext context, Collection<OBJECT> results,  CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        if (results == null || results.isEmpty())
        {
            return Collections.emptyList();
        }

        SortAndPage sap = new SortAndPage();
        Collection<OBJECT> objects = sap.sortAndPage(results, criteria, fetchOptions);

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

    protected SearchObjectsOperationResult<OBJECT> executeDirectSQLSearch(final IOperationContext context,
            final SearchObjectsOperation<CRITERIA, FETCH_OPTIONS> operation)
    {
        final CRITERIA criteria = operation.getCriteria();
        final FETCH_OPTIONS fetchOptions = operation.getFetchOptions();

        if (criteria == null)
        {
            throw new IllegalArgumentException("Criteria cannot be null.");
        }
        if (fetchOptions == null)
        {
            throw new IllegalArgumentException("Fetch options cannot be null.");
        }

        final PersonPE personPE = context.getSession().tryGetPerson();
        final Set<Long> spaceIds = personPE.getAllPersonRoles().stream().filter((roleAssignmentPE) -> roleAssignmentPE.getSpace() != null)
                .map((roleAssignmentPE) -> roleAssignmentPE.getSpace().getId()).collect(Collectors.toSet());
        final Set<Long> projectIds = personPE.getAllPersonRoles().stream().filter((roleAssignmentPE) -> roleAssignmentPE.getProject() != null)
                .map((roleAssignmentPE) -> roleAssignmentPE.getProject().getId()).collect(Collectors.toSet());
        final AuthorisationInformation authorisationInformation = new AuthorisationInformation(!personPE.getRoleAssignments().isEmpty(),
                spaceIds, projectIds);

        final Long userId = personPE.getId();
        final TranslationContext translationContext = new TranslationContext(context.getSession());

        // There results from the manager should already be filtered.
        final Set<Long> allResultsIds = getSearchManager().searchForIDs(userId, authorisationInformation, criteria, null, ID_COLUMN);
        final List<Long> sortedAndPagedResultIds = sortAndPage(allResultsIds, fetchOptions);
        final List<OBJECT_PE> sortedAndPagedResultPEs = getSearchManager().translate(sortedAndPagedResultIds);
        final Map<OBJECT_PE, OBJECT> sortedAndPagedResultV3DTOs = doTranslate(translationContext, sortedAndPagedResultPEs, fetchOptions);

        final List<OBJECT> finalResults = new ArrayList<>(sortedAndPagedResultV3DTOs.values());
        final List<OBJECT> sortedFinalResults = getSortedFinalResults(criteria, fetchOptions, finalResults);
        final SearchResult<OBJECT> searchResult = new SearchResult<>(sortedFinalResults, allResultsIds.size());

        return getOperationResult(searchResult);
    }

    protected List<OBJECT> getSortedFinalResults(final CRITERIA criteria, final FETCH_OPTIONS fetchOptions, final List<OBJECT> finalResults)
    {
        // No paging is needed, the result should just be sorted.
        final Integer from = fetchOptions.getFrom();
        fetchOptions.from(null);
        final Integer count = fetchOptions.getCount();
        fetchOptions.count(null);
        final List<OBJECT> sortedFinalResults = new SortAndPage().sortAndPage(finalResults, criteria, fetchOptions);
        fetchOptions.from(from);
        fetchOptions.count(count);
        return sortedFinalResults;
    }

    protected List<Long> sortAndPage(final Set<Long> ids, final FetchOptions<OBJECT> fo)
    {
        //
        // Filter out sorts to ignore
        //

        SortOptions<OBJECT> sortOptions = fo.getSortBy();

        if (sortOptions != null) {
            List<Sorting> sortingToRemove = new ArrayList<>();
            for (Sorting sorting : sortOptions.getSortings()) {
                for (String sortToIgnore : SORTS_TO_IGNORE) {
                    if (sorting.getField().equals(sortToIgnore)) {
                        sortingToRemove.add(sorting);
                    }
                }
            }

            for (Sorting sorting:sortingToRemove) {
                sortOptions.getSortings().remove(sorting);
                operationLog.warn("[SQL Query Engine - backwards compatibility warning - stop using this feature] SORTING ORDER IGNORED!: " + sorting.getField());
            }

            if (sortOptions.getSortings().isEmpty()) {
                sortOptions = null;
            }
        }

        //
        //
        //

        final Set<Long> orderedIDs = (sortOptions != null) ? getSearchManager().sortIDs(ids, sortOptions) : ids;

        final List<Long> toPage = new ArrayList<>(orderedIDs);
        final Integer fromRecord = fo.getFrom();
        final Integer recordsCount = fo.getCount();
        final boolean hasPaging = fromRecord != null && recordsCount != null;
        return hasPaging ? toPage.subList(fromRecord, Math.min(fromRecord + recordsCount, toPage.size())) : toPage;
    }

}
