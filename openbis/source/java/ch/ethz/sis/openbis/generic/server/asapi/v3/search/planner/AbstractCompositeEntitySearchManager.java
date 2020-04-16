/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PEMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractCompositeEntitySearchManager<CRITERIA extends AbstractCompositeSearchCriteria,
        OBJECT, OBJECT_PE> extends AbstractLocalSearchManager<CRITERIA, OBJECT, OBJECT_PE>
{

    public AbstractCompositeEntitySearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PEMapper<Long, OBJECT_PE> idsTranslator)
    {
        super(searchDAO, authProvider, idsTranslator);
    }

    protected abstract Class<? extends AbstractCompositeSearchCriteria> getParentsSearchCriteriaClass();

    protected abstract Class<? extends AbstractCompositeSearchCriteria> getChildrenSearchCriteriaClass();

    protected abstract CRITERIA createEmptyCriteria();

    protected Set<Long> doSearchForIDs(final Long userId, final AuthorisationInformation authorisationInformation, final CRITERIA criteria, final SearchOperator searchOperator, final String idsColumnName,
            final TableMapper tableMapper)
    {
        final Class<? extends AbstractCompositeSearchCriteria> parentsSearchCriteriaClass = getParentsSearchCriteriaClass();
        final Class<? extends AbstractCompositeSearchCriteria> childrenSearchCriteriaClass = getChildrenSearchCriteriaClass();
        final Collection<ISearchCriteria> parentsCriteria = getCriteria(criteria, parentsSearchCriteriaClass);
        final Collection<ISearchCriteria> childrenCriteria = getCriteria(criteria, childrenSearchCriteriaClass);
        final Collection<ISearchCriteria> mainCriteria;
        if (parentsSearchCriteriaClass != null && childrenSearchCriteriaClass != null)
        {
            mainCriteria = getOtherCriteriaThan(criteria, parentsSearchCriteriaClass, childrenSearchCriteriaClass);
        } else if (parentsSearchCriteriaClass == null && childrenSearchCriteriaClass == null)
        {
            mainCriteria = criteria.getCriteria();
        } else
        {
            throw new RuntimeException("Either both or none of parent/child search criteria should be null.");
        }
        final SearchOperator finalSearchOperator = (searchOperator == null) ? criteria.getOperator() : searchOperator;

        return doSearchForIDs(userId, parentsCriteria, childrenCriteria, mainCriteria, finalSearchOperator, idsColumnName, tableMapper,
                authorisationInformation);
    }

    protected Set<Long> doSearchForIDs(final Long userId, final Collection<ISearchCriteria> parentsCriteria,
            final Collection<ISearchCriteria> childrenCriteria, final Collection<ISearchCriteria> mainCriteria,
            final SearchOperator finalSearchOperator, final String idsColumnName, final TableMapper tableMapper, final AuthorisationInformation authorisationInformation)
    {
        final Set<Long> mainCriteriaIntermediateResults;
        if (!mainCriteria.isEmpty())
        {
            // The main criteria have no recursive ISearchCriteria into it, to facilitate building a query
            final DummyCompositeSearchCriterion containerCriterion = new DummyCompositeSearchCriterion(mainCriteria, finalSearchOperator);
            final Set<Long> mainCriteriaNotFilteredResults = getSearchDAO().queryDBWithNonRecursiveCriteria(userId, containerCriterion, tableMapper,
                    idsColumnName, authorisationInformation);
            mainCriteriaIntermediateResults = filterIDsByUserRights(userId, authorisationInformation, mainCriteriaNotFilteredResults);
        } else
        {
            mainCriteriaIntermediateResults = null;
        }

        final Set<Long> parentCriteriaIntermediateResults;
        if (!parentsCriteria.isEmpty())
        {
            // The parents criteria can be or not recursive, they are resolved by a recursive call
            final Set<Long> finalParentIds = findFinalRelationshipIds(userId, authorisationInformation, finalSearchOperator, parentsCriteria, tableMapper);
            final Set<Long> finalParentIdsFiltered = filterIDsByUserRights(userId, authorisationInformation, finalParentIds);
            parentCriteriaIntermediateResults = getChildrenIdsOf(finalParentIdsFiltered, tableMapper);
        } else
        {
            parentCriteriaIntermediateResults = null;
        }

        final Set<Long> childrenCriteriaIntermediateResults;
        if (!childrenCriteria.isEmpty())
        {
            // The children criteria can be or not recursive, they are resolved by a recursive call
            final Set<Long> finalChildrenIds = findFinalRelationshipIds(userId, authorisationInformation, finalSearchOperator, childrenCriteria, tableMapper);
            final Set<Long> finalChildrenIdsFiltered = filterIDsByUserRights(userId, authorisationInformation, finalChildrenIds);
            childrenCriteriaIntermediateResults = getParentsIdsOf(finalChildrenIdsFiltered, tableMapper);
        } else
        {
            childrenCriteriaIntermediateResults = null;
        }

        // Reaching this point we have the intermediate results of all recursive queries
        final Set<Long> results;
        if (containsValues(mainCriteriaIntermediateResults) || containsValues(parentCriteriaIntermediateResults) ||
                containsValues(childrenCriteriaIntermediateResults))
        {
            // If we have results, we merge them
            results = mergeResults(finalSearchOperator,
                    mainCriteriaIntermediateResults != null ? Collections.singleton(mainCriteriaIntermediateResults) : Collections.emptySet(),
                    childrenCriteriaIntermediateResults != null ? Collections.singleton(childrenCriteriaIntermediateResults) : Collections.emptySet(),
                    parentCriteriaIntermediateResults != null ? Collections.singleton(parentCriteriaIntermediateResults) : Collections.emptySet());
        } else if (mainCriteria.isEmpty() && parentsCriteria.isEmpty() && childrenCriteria.isEmpty())
        {
            // If we don't have results and criteria are empty, return all.
            results = getAllIds(userId, authorisationInformation, idsColumnName, tableMapper);
        } else
        {
            // If we don't have results and criteria are not empty, there are no results.
            results = Collections.emptySet();
        }

        return results;
    }

    /**
     * Returns IDs using parent or child relationship criteria.
     *
     * @param authorisationInformation
     * @param operator the operator used to merge the results.
     * @param relatedEntitiesCriteria parent or child criteria.
     * @param tableMapper
     * @return IDs found from parent/child criteria.
     */
    @SuppressWarnings("unchecked")
    private Set<Long> findFinalRelationshipIds(final Long userId, final AuthorisationInformation authorisationInformation, final SearchOperator operator,
            final Collection<ISearchCriteria> relatedEntitiesCriteria, final TableMapper tableMapper)
    {
        final List<Set<Long>> relatedIds = relatedEntitiesCriteria.stream().flatMap(entitySearchCriteria -> {
            final Set<Long> foundParentIds = doSearchForIDs(userId, authorisationInformation, (CRITERIA) entitySearchCriteria, operator, ColumnNames.ID_COLUMN,
                    tableMapper);
            return foundParentIds.isEmpty() ? Stream.empty() : Stream.of(foundParentIds);
        }).collect(Collectors.toList());

        return mergeResults(operator, relatedIds);
    }

    /*
     * These methods require a simple SQL query to the database
     */

    /**
     * Queries the DB to return all entity IDs.
     *
     * @return set of IDs of all entities.
     * @param userId requesting user ID.
     * @param authorisationInformation
     * @param idsColumnName the name of the column, whose values to be returned.
     * @param tableMapper the table mapper to be used during translation.
     */
    private Set<Long> getAllIds(final Long userId, final AuthorisationInformation authorisationInformation, final String idsColumnName,
            final TableMapper tableMapper)
    {
        final CRITERIA criteria = createEmptyCriteria();
        final DummyCompositeSearchCriterion containerCriterion = new DummyCompositeSearchCriterion();
        containerCriterion.setCriteria(Collections.singletonList(criteria));
        return getSearchDAO().queryDBWithNonRecursiveCriteria(userId, containerCriterion, tableMapper, idsColumnName, authorisationInformation);
    }

    private Set<Long> getChildrenIdsOf(final Set<Long> parentIdSet, final TableMapper tableMapper)
    {
        return getSearchDAO().findChildIDs(tableMapper, parentIdSet);
    }

    private Set<Long> getParentsIdsOf(final Set<Long> childIdSet, final TableMapper tableMapper)
    {
        return getSearchDAO().findParentIDs(tableMapper, childIdSet);
    }

}
