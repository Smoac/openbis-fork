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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;

/**
 * Manages detailed search for complex data set search criterion.
 * 
 * @author Kaloyan Enimanev
 */
public class DataSetSearchManager extends AbstractSearchManager<IDatasetLister>
{

    public DataSetSearchManager(IHibernateSearchDAO searchDAO, IDatasetLister lister)
    {
        super(searchDAO, lister);
    }

    public List<ExternalData> searchForDataSets(DetailedSearchCriteria criteria)
            throws DataAccessException
    {
        
        DetailedSearchCriteria parentCriteria = new DetailedSearchCriteria();
        DetailedSearchCriteria childCriteria = new DetailedSearchCriteria();
        List<DetailedSearchSubCriteria> otherSubCriterias =
                new ArrayList<DetailedSearchSubCriteria>();
        groupDataSetSubCriteria(criteria.getSubCriterias(), parentCriteria, childCriteria,
                otherSubCriterias);

        List<Long> dataSetIds = findDataSetIds(criteria, otherSubCriterias);
        
        Collection<Long> filteredDataSetIds = dataSetIds;

        if (false == parentCriteria.isEmpty())
        {
            List<Long> parentDataSetIds =
                    findDataSetIds(parentCriteria,
                            Collections.<DetailedSearchSubCriteria> emptyList());
            filteredDataSetIds = filterDataSetIdsByRelatedParentIds(dataSetIds, parentDataSetIds);
        }

        if (false == childCriteria.isEmpty())
        {
            List<Long> childrenDataSetIds =
                    findDataSetIds(childCriteria,
                            Collections.<DetailedSearchSubCriteria> emptyList());
            filteredDataSetIds =
                    filterDataSetIdByRelatedChildrenIds(dataSetIds, childrenDataSetIds);
        }

        return lister.listByDatasetIds(filteredDataSetIds);
    }

    private Collection<Long> filterDataSetIdsByRelatedParentIds(List<Long> dataSetIds,
            List<Long> parentDataSetIds)
    {
        if (dataSetIds.size() > parentDataSetIds.size())
        {
            Map<Long, Set<Long>> parentToChildren = lister.listChildrenIds(parentDataSetIds);
            return filterJoinedIds(dataSetIds, parentToChildren.values());
        } else
        {
            Map<Long, Set<Long>> dataSetsToParent = lister.listParentIds(dataSetIds);
            return filteIdsByJoinTable(dataSetIds, parentDataSetIds, dataSetsToParent);
        }
    }

    private Collection<Long> filterDataSetIdByRelatedChildrenIds(List<Long> dataSetIds,
            List<Long> childrenDataSetIds)
    {
        if (dataSetIds.size() > childrenDataSetIds.size())
        {
            Map<Long, Set<Long>> childrenToDataSets = lister.listParentIds(childrenDataSetIds);
            return filterJoinedIds(dataSetIds, childrenToDataSets.values());
        } else
        {
            Map<Long, Set<Long>> dataSetsToChildren = lister.listChildrenIds(dataSetIds);
            return filteIdsByJoinTable(dataSetIds, childrenDataSetIds, dataSetsToChildren);
        }
    }

    private Collection<Long> filterJoinedIds(List<Long> leftJoinSide,
            Collection<Set<Long>> rightJoinSide)
    {
        Set<Long> joinResult = new HashSet<Long>();
        for (Set<Long> joinedIds : rightJoinSide)
        {
            joinedIds.retainAll(leftJoinSide);
            joinResult.addAll(joinedIds);
        }
        return joinResult;
    }

    private Collection<Long> filteIdsByJoinTable(List<Long> leftJoinSide, List<Long> rightJoinSide,
            Map<Long, Set<Long>> leftToRightJoinTable)
    {
        for (Entry<Long, Set<Long>> entry : leftToRightJoinTable.entrySet())
        {
            if (Collections.disjoint(rightJoinSide, entry.getValue()))
            {
                leftJoinSide.remove(entry.getKey());
            }
        }
        return leftJoinSide;
    }


    private List<Long> findDataSetIds(DetailedSearchCriteria criteria,
            List<DetailedSearchSubCriteria> otherSubCriterias)
    {
        List<DetailedSearchAssociationCriteria> associations =
            new ArrayList<DetailedSearchAssociationCriteria>();
        for (DetailedSearchSubCriteria subCriteria : otherSubCriterias)
        {
            associations.add(findAssociatedEntities(subCriteria));
        }

        if (criteria.getCriteria().isEmpty() && otherSubCriterias.isEmpty())
        {
            // if no criteria were provided find all data sets
            criteria.getCriteria().add(
                    new DetailedSearchCriterion(DetailedSearchField
                            .createAttributeField(DataSetAttributeSearchFieldKind.CODE), "*"));
        }

        final List<Long> dataSetIds =
            searchDAO.searchForEntityIds(criteria,
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
