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

package ch.systemsx.cisd.openbis.generic.server.business.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;

/**
 * Manages detailed search for complex data set search criterion.
 * 
 * @author Kaloyan Enimanev
 */
public class DataSetSearchManager extends AbstractSearchManager<IDatasetLister>
{
    private final IRelationshipHandler CHILDREN_RELATIONSHIP_HANDLER = new IRelationshipHandler()
        {

            @Override
            public Collection<Long> findRelatedIdsByCriteria(String userId,
                    DetailedSearchCriteria criteria,
                    List<DetailedSearchSubCriteria> otherSubCriterias)
            {
                return findDataSetIds(userId, criteria, otherSubCriterias);
            }

            @Override
            public Map<Long, Set<Long>> listIdsToRelatedIds(Collection<Long> dataSetIds)
            {
                return lister.listChildrenIds(dataSetIds);
            }

            @Override
            public Map<Long, Set<Long>> listRelatedIdsToIds(Collection<Long> childrenDataSetIds)
            {
                return lister.listParentIds(childrenDataSetIds);
            }
        };

    private final IRelationshipHandler PARENT_RELATIONSHIP_HANDLER = new IRelationshipHandler()
        {

            @Override
            public Collection<Long> findRelatedIdsByCriteria(String userId,
                    DetailedSearchCriteria criteria,
                    List<DetailedSearchSubCriteria> otherSubCriterias)
            {
                return findDataSetIds(userId, criteria, otherSubCriterias);
            }

            @Override
            public Map<Long, Set<Long>> listIdsToRelatedIds(Collection<Long> dataSetIds)
            {
                return lister.listParentIds(dataSetIds);
            }

            @Override
            public Map<Long, Set<Long>> listRelatedIdsToIds(Collection<Long> parentDataSetIds)
            {
                return lister.listChildrenIds(parentDataSetIds);
            }
        };

    public DataSetSearchManager(IHibernateSearchDAO searchDAO, IDatasetLister lister)
    {
        super(searchDAO, lister);
    }

    public List<AbstractExternalData> searchForDataSets(String userId, DetailedSearchCriteria criteria)
            throws DataAccessException
    {

        DetailedSearchCriteria parentCriteria = new DetailedSearchCriteria();
        DetailedSearchCriteria childCriteria = new DetailedSearchCriteria();
        List<DetailedSearchSubCriteria> otherSubCriterias =
                new ArrayList<DetailedSearchSubCriteria>();
        groupDataSetSubCriteria(criteria.getSubCriterias(), parentCriteria, childCriteria,
                otherSubCriterias);

        List<Long> dataSetIds = findDataSetIds(userId, criteria, otherSubCriterias);
        Collection<Long> filteredDataSetIds = dataSetIds;

        if (false == parentCriteria.isEmpty())
        {
            filteredDataSetIds =
                    filterSearchResultsBySubcriteria(userId, dataSetIds, parentCriteria,
                            PARENT_RELATIONSHIP_HANDLER);
        }

        if (false == childCriteria.isEmpty())
        {
            filteredDataSetIds =
                    filterSearchResultsBySubcriteria(userId, dataSetIds, childCriteria,
                            CHILDREN_RELATIONSHIP_HANDLER);
        }

        return lister.listByDatasetIds(restrictResultSetIfNecessary(filteredDataSetIds));
    }

    private List<Long> findDataSetIds(String userId, DetailedSearchCriteria criteria,
            List<DetailedSearchSubCriteria> otherSubCriterias)
    {
        List<DetailedSearchAssociationCriteria> associations =
                new ArrayList<DetailedSearchAssociationCriteria>();
        for (DetailedSearchSubCriteria subCriteria : otherSubCriterias)
        {
            associations.add(findAssociatedEntities(userId, subCriteria));
        }

        if (criteria.getCriteria().isEmpty() && otherSubCriterias.isEmpty())
        {
            // if no criteria were provided find all data sets
            criteria.getCriteria().add(
                    new DetailedSearchCriterion(DetailedSearchField
                            .createAttributeField(DataSetAttributeSearchFieldKind.CODE), "*"));
        }

        final List<Long> dataSetIds =
                searchDAO.searchForEntityIds(userId, criteria,
                        DtoConverters.convertEntityKind(EntityKind.DATA_SET), associations);
        return dataSetIds;
    }

    private void groupDataSetSubCriteria(List<DetailedSearchSubCriteria> allSubCriterias,
            DetailedSearchCriteria parentCriteria, DetailedSearchCriteria childCriteria,
            List<DetailedSearchSubCriteria> otherSubCriterias)
    {
        parentCriteria.setCriteria(new ArrayList<DetailedSearchCriterion>());
        childCriteria.setCriteria(new ArrayList<DetailedSearchCriterion>());
        for (DetailedSearchSubCriteria subCriteria : allSubCriterias)
        {
            switch (subCriteria.getTargetEntityKind())
            {
                case DATA_SET_PARENT:
                    // merge all parent sub criteria into one
                    mergeSubCriteria(parentCriteria, subCriteria);
                    break;
                case DATA_SET_CHILD:
                    // merge all child sub criteria into one
                    mergeSubCriteria(childCriteria, subCriteria);
                    break;
                default:
                    otherSubCriterias.add(subCriteria);
            }
        }

    }

}
