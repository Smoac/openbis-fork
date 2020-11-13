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
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.relationship.IGetRelationshipIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PEMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractCompositeEntitySearchManager<CRITERIA extends AbstractCompositeSearchCriteria,
        OBJECT, OBJECT_PE> extends AbstractLocalSearchManager<CRITERIA, OBJECT, OBJECT_PE>
{

    public AbstractCompositeEntitySearchManager(final ISQLSearchDAO searchDAO,
            final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PEMapper<Long, OBJECT_PE> idsTranslator)
    {
        super(searchDAO, authProvider, idsTranslator);
    }

    protected abstract Class<? extends AbstractCompositeSearchCriteria> getParentsSearchCriteriaClass();

    protected abstract Class<? extends AbstractCompositeSearchCriteria> getChildrenSearchCriteriaClass();

    protected abstract CRITERIA createEmptyCriteria();

    protected Set<Long> doSearchForIDs(final Long userId, final AuthorisationInformation authorisationInformation,
            final CRITERIA criteria, final SearchOperator searchOperator, final String idsColumnName,
            final TableMapper tableMapper)
    {
        final CRITERIA emptyCriteria = createEmptyCriteria();
        final Class<? extends AbstractCompositeSearchCriteria> parentsSearchCriteriaClass =
                getParentsSearchCriteriaClass();
        final Class<? extends ISearchCriteria> childrenSearchCriteriaClass = getChildrenSearchCriteriaClass();
        final Collection<ISearchCriteria> mainCriteria;
        if (parentsSearchCriteriaClass != null && childrenSearchCriteriaClass != null)
        {
            mainCriteria = getOtherCriteriaThan(criteria, parentsSearchCriteriaClass, childrenSearchCriteriaClass,
                    emptyCriteria.getClass());
        } else if (parentsSearchCriteriaClass == null && childrenSearchCriteriaClass == null)
        {
            mainCriteria = criteria.getCriteria();
        } else
        {
            throw new RuntimeException("Either both or none of parent/child search criteria should be null.");
        }

        final CriteriaVo criteriaVo = new CriteriaVo(mainCriteria, getCriteria(criteria, parentsSearchCriteriaClass),
                getCriteria(criteria, childrenSearchCriteriaClass),
                Collections.emptyList(), (List) getCriteria(criteria, emptyCriteria.getClass()),
                (searchOperator == null) ? criteria.getOperator() : searchOperator);

        return doSearchForIDs(userId, criteriaVo, idsColumnName, tableMapper, authorisationInformation);
    }

    protected Set<Long> doSearchForIDs(final Long userId, final CriteriaVo criteriaVo,
            final String idsColumnName, final TableMapper tableMapper,
            final AuthorisationInformation authorisationInformation)
    {
        final Collection<? extends ISearchCriteria> parentRelationshipsCriteria = criteriaVo.getParentsCriteria();
        final Collection<? extends ISearchCriteria> childRelationshipsCriteria = criteriaVo.getChildrenCriteria();
        final Collection<? extends ISearchCriteria> containerCriteria = criteriaVo.getContainerCriteria();
        final Collection<CRITERIA> nestedCriteria = criteriaVo.getNestedCriteria();
        final Collection<ISearchCriteria> mainCriteria = criteriaVo.getMainCriteria();
        final SearchOperator finalSearchOperator = criteriaVo.getSearchOperator();

        final Set<Long> mainCriteriaIntermediateResults;
        if (!mainCriteria.isEmpty())
        {
            // The main criteria have no recursive ISearchCriteria into it, to facilitate building a query
            final CRITERIA containerCriterion = createEmptyCriteria();
            containerCriterion.withOperator(finalSearchOperator);
            containerCriterion.setCriteria(mainCriteria);
            mainCriteriaIntermediateResults = getSearchDAO().queryDBWithNonRecursiveCriteria(userId, containerCriterion,
                    tableMapper, idsColumnName, authorisationInformation);
        } else
        {
            mainCriteriaIntermediateResults = null;
        }

        final Set<Long> parentCriteriaIntermediateResults;
        if (!parentRelationshipsCriteria.isEmpty())
        {
            final Set<Long> finalParentIds = findFinalRelationshipIds(userId, authorisationInformation,
                    finalSearchOperator, parentRelationshipsCriteria, tableMapper);
            final Set<Long> finalParentIdsFiltered = filterIDsByUserRights(userId, authorisationInformation,
                    finalParentIds);
            parentCriteriaIntermediateResults = getChildrenIdsOf(finalParentIdsFiltered, tableMapper,
                    IGetRelationshipIdExecutor.RelationshipType.PARENT_CHILD);
        } else
        {
            parentCriteriaIntermediateResults = null;
        }

        final Set<Long> containerCriteriaIntermediateResults;
        if (!containerCriteria.isEmpty())
        {
            final Set<Long> finalContainerIds = findFinalRelationshipIds(userId, authorisationInformation,
                    finalSearchOperator, containerCriteria, tableMapper);
            final Set<Long> finalContainerIdsFiltered = filterIDsByUserRights(userId, authorisationInformation,
                    finalContainerIds);
            containerCriteriaIntermediateResults = getChildrenIdsOf(finalContainerIdsFiltered, tableMapper,
                    IGetRelationshipIdExecutor.RelationshipType.CONTAINER_COMPONENT);
        } else
        {
            containerCriteriaIntermediateResults = null;
        }

        final Set<Long> childrenCriteriaIntermediateResults;
        if (!childRelationshipsCriteria.isEmpty())
        {
            final Set<Long> finalChildrenIds = findFinalRelationshipIds(userId, authorisationInformation,
                    finalSearchOperator, childRelationshipsCriteria, tableMapper);
            final Set<Long> finalChildrenIdsFiltered = filterIDsByUserRights(userId,
                    authorisationInformation, finalChildrenIds);
            childrenCriteriaIntermediateResults = getParentsIdsOf(finalChildrenIdsFiltered, tableMapper,
                    IGetRelationshipIdExecutor.RelationshipType.PARENT_CHILD);
        } else
        {
            childrenCriteriaIntermediateResults = null;
        }

        final Collection<Set<Long>> nestedCriteriaIntermediateResults;
        if (!nestedCriteria.isEmpty())
        {
            nestedCriteriaIntermediateResults = nestedCriteria.stream().map(criteria ->
                    doSearchForIDs(userId, authorisationInformation, criteria, criteria.getOperator(),
                            idsColumnName, tableMapper))
                    .collect(Collectors.toList());
        } else
        {
            nestedCriteriaIntermediateResults = Collections.emptyList();
        }

        // Reaching this point we have the intermediate results of all recursive queries
        final Set<Long> results;
        if (containsValues(mainCriteriaIntermediateResults) || containsValues(parentCriteriaIntermediateResults)
                || containsValues(containerCriteriaIntermediateResults)
                || containsValues(childrenCriteriaIntermediateResults)
                || containsValues(nestedCriteriaIntermediateResults))
        {
            // If we have results, we merge them
            results = mergeResults(finalSearchOperator,
                    mainCriteriaIntermediateResults != null
                            ? Collections.singleton(mainCriteriaIntermediateResults) : Collections.emptySet(),
                    childrenCriteriaIntermediateResults != null
                            ? Collections.singleton(childrenCriteriaIntermediateResults) : Collections.emptySet(),
                    parentCriteriaIntermediateResults != null
                            ? Collections.singleton(parentCriteriaIntermediateResults) : Collections.emptySet(),
                    containerCriteriaIntermediateResults != null
                            ? Collections.singleton(containerCriteriaIntermediateResults) : Collections.emptySet(),
                    nestedCriteriaIntermediateResults);
        } else if (mainCriteria.isEmpty() && parentRelationshipsCriteria.isEmpty()
                && childRelationshipsCriteria.isEmpty())
        {
            // If we don't have results and criteria are empty, return all.
            results = getAllIds(userId, authorisationInformation, idsColumnName, tableMapper);
        } else
        {
            // If we don't have results and criteria are not empty, there are no results.
            results = Collections.emptySet();
        }

        return filterIDsByUserRights(userId, authorisationInformation, results);
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
    private Set<Long> findFinalRelationshipIds(final Long userId,
            final AuthorisationInformation authorisationInformation, final SearchOperator operator,
            final Collection<? extends ISearchCriteria> relatedEntitiesCriteria, final TableMapper tableMapper)
    {
        final List<Set<Long>> relatedIds = relatedEntitiesCriteria.stream().flatMap(entitySearchCriteria ->
        {
            final Set<Long> foundParentIds = doSearchForIDs(userId, authorisationInformation,
                    (CRITERIA) entitySearchCriteria, operator, ColumnNames.ID_COLUMN, tableMapper);
            return foundParentIds.isEmpty() ? Stream.empty() : Stream.of(foundParentIds);
        }).collect(Collectors.toList());

        return mergeResults(operator, relatedIds);
    }

    /**
     * Queries the DB to return all entity IDs.
     *
     * @return set of IDs of all entities.
     * @param userId requesting user ID.
     * @param authorisationInformation user authorisation information.
     * @param idsColumnName the name of the column, whose values to be returned.
     * @param tableMapper the table mapper to be used during translation.
     */
    protected Set<Long> getAllIds(final Long userId, final AuthorisationInformation authorisationInformation,
            final String idsColumnName,
            final TableMapper tableMapper)
    {
        final CRITERIA criteria = createEmptyCriteria();
        final CRITERIA containerCriterion = createEmptyCriteria();
        containerCriterion.setCriteria(Collections.singletonList(criteria));
        return getSearchDAO().queryDBWithNonRecursiveCriteria(userId, containerCriterion, tableMapper, idsColumnName,
                authorisationInformation);
    }

    private Set<Long> getChildrenIdsOf(final Set<Long> parentIdSet, final TableMapper tableMapper,
            final IGetRelationshipIdExecutor.RelationshipType relationshipType)
    {
        return getSearchDAO().findChildIDs(tableMapper, parentIdSet, relationshipType);
    }

    private Set<Long> getParentsIdsOf(final Set<Long> childIdSet, final TableMapper tableMapper,
            final IGetRelationshipIdExecutor.RelationshipType relationshipType)
    {
        return getSearchDAO().findParentIDs(tableMapper, childIdSet, relationshipType);
    }

    protected class CriteriaVo
    {
        private final Collection<ISearchCriteria> mainCriteria;
        private final Collection<? extends ISearchCriteria> parentsCriteria;
        private final Collection<? extends ISearchCriteria> childrenCriteria;
        private final Collection<? extends ISearchCriteria> containerCriteria;
        private final Collection<CRITERIA> nestedCriteria;
        private final SearchOperator searchOperator;

        public CriteriaVo(final Collection<ISearchCriteria> mainCriteria,
                final Collection<? extends ISearchCriteria> parentsCriteria,
                final Collection<? extends ISearchCriteria> childrenCriteria,
                final Collection<? extends ISearchCriteria> containerCriteria,
                final Collection<CRITERIA> nestedCriteria, final SearchOperator searchOperator)
        {
            this.mainCriteria = mainCriteria;
            this.parentsCriteria = parentsCriteria;
            this.childrenCriteria = childrenCriteria;
            this.containerCriteria = containerCriteria;
            this.nestedCriteria = nestedCriteria;
            this.searchOperator = searchOperator;
        }

        public Collection<ISearchCriteria> getMainCriteria()
        {
            return mainCriteria;
        }

        public Collection<? extends ISearchCriteria> getParentsCriteria()
        {
            return parentsCriteria;
        }

        public Collection<? extends ISearchCriteria> getChildrenCriteria()
        {
            return childrenCriteria;
        }

        public Collection<? extends ISearchCriteria> getContainerCriteria()
        {
            return containerCriteria;
        }

        public Collection<CRITERIA> getNestedCriteria()
        {
            return nestedCriteria;
        }

        public SearchOperator getSearchOperator()
        {
            return searchOperator;
        }

    }

}
