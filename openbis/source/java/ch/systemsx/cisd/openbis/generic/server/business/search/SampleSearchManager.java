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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;

/**
 * Manages detailed search with complex sample search criteria.
 * 
 * @author Piotr Buczek
 */
public class SampleSearchManager extends AbstractSearchManager<ISampleLister>
{
    public SampleSearchManager(IHibernateSearchDAO searchDAO, ISampleLister sampleLister)
    {
        super(searchDAO, sampleLister);
    }

    public List<Sample> searchForSamples(DetailedSearchCriteria criteria)
            throws DataAccessException
    {
        final DetailedSearchCriteria parentCriteria = new DetailedSearchCriteria();
        final DetailedSearchCriteria childCriteria = new DetailedSearchCriteria();
        final List<DetailedSearchSubCriteria> otherSubCriterias =
                new ArrayList<DetailedSearchSubCriteria>();
        groupSampleSubCriteria(criteria.getSubCriterias(), parentCriteria, childCriteria,
                otherSubCriterias);

        final List<Long> mainSampleIds = findSampleIds(criteria, otherSubCriterias);

        final Set<Long> filteredSampleIds = new HashSet<Long>();
        if (false == parentCriteria.isEmpty())
        {
            final List<Long> parentSampleIds =
                    findSampleIds(parentCriteria,
                            Collections.<DetailedSearchSubCriteria> emptyList());
            if (mainSampleIds.size() > parentSampleIds.size())
            {
                listParentsChildrenAndFilterChildren(mainSampleIds, parentSampleIds,
                        filteredSampleIds);
            } else
            {
                listChildrensParentsAndFilterChildren(mainSampleIds, parentSampleIds,
                        filteredSampleIds);
            }
        } else if (false == childCriteria.isEmpty()) // FIXME this should be if
        {
            final List<Long> childSampleIds =
                    findSampleIds(childCriteria,
                            Collections.<DetailedSearchSubCriteria> emptyList());
            if (mainSampleIds.size() > childSampleIds.size())
            {
                listChildrensParentsAndFilterParents(childSampleIds, mainSampleIds,
                        filteredSampleIds);
            } else
            {
                listParentsChildrenAndFilterParents(childSampleIds, mainSampleIds,
                        filteredSampleIds);
            }
        } else
        {
            filteredSampleIds.addAll(mainSampleIds);
        }
        return lister.list(new ListOrSearchSampleCriteria(filteredSampleIds));
    }

    private void listChildrensParentsAndFilterChildren(final List<Long> allChildrenIds,
            final List<Long> allParentIds, final Set<Long> filteredChildrenIds)
    {
        Map<Long, Set<Long>> childToParentIds =
                lister.getChildToParentsIdsMap(allChildrenIds);
        for (Entry<Long, Set<Long>> entry : childToParentIds.entrySet())
        {
            Long childId = entry.getKey();
            Set<Long> parentIds = entry.getValue();
            parentIds.retainAll(allParentIds);
            if (parentIds.isEmpty() == false)
            {
                filteredChildrenIds.add(childId);
            }
        }
    }

    private void listParentsChildrenAndFilterParents(final List<Long> allChildrenIds,
            final List<Long> allParentIds, final Set<Long> filteredParentIds)
    {
        Map<Long, Set<Long>> parentToChildIds =
                lister.getParentToChildrenIdsMap(allParentIds);
        for (Entry<Long, Set<Long>> entry : parentToChildIds.entrySet())
        {
            Long parentId = entry.getKey();
            Set<Long> childIds = entry.getValue();
            childIds.retainAll(allChildrenIds);
            if (childIds.isEmpty() == false)
            {
                filteredParentIds.add(parentId);
            }
        }
    }

    private void listParentsChildrenAndFilterChildren(final List<Long> allChildrenIds,
            final List<Long> allParentIds, final Set<Long> filteredChildrenIds)
    {
        Map<Long, Set<Long>> parentToChildIds =
                lister.getParentToChildrenIdsMap(allParentIds);
        for (Set<Long> childrenIds : parentToChildIds.values())
        {
            filteredChildrenIds.addAll(childrenIds);
        }
        filteredChildrenIds.retainAll(allChildrenIds);
    }

    private void listChildrensParentsAndFilterParents(final List<Long> allChildrenIds,
            final List<Long> allParentIds, final Set<Long> filteredParentsIds)
    {
        Map<Long, Set<Long>> childToParentIds =
                lister.getChildToParentsIdsMap(allChildrenIds);
        for (Set<Long> parentIds : childToParentIds.values())
        {
            filteredParentsIds.addAll(parentIds);
        }
        filteredParentsIds.retainAll(allParentIds);
    }

    private void groupSampleSubCriteria(List<DetailedSearchSubCriteria> allSubCriterias,
            DetailedSearchCriteria parentCriteria, DetailedSearchCriteria childCriteria,
            List<DetailedSearchSubCriteria> otherSubCriterias)
    {
        parentCriteria.setCriteria(new ArrayList<DetailedSearchCriterion>());
        childCriteria.setCriteria(new ArrayList<DetailedSearchCriterion>());
        for (DetailedSearchSubCriteria subCriteria : allSubCriterias)
        {
            switch (subCriteria.getTargetEntityKind())
            {
                case SAMPLE_PARENT:
                    // merge all parent sub criteria into one
                    parentCriteria.getCriteria().addAll(subCriteria.getCriteria().getCriteria());
                    parentCriteria.setConnection(subCriteria.getCriteria().getConnection());
                    parentCriteria.setUseWildcardSearchMode(subCriteria.getCriteria()
                            .isUseWildcardSearchMode());
                    break;
                case SAMPLE_CHILD:
                    // merge all child sub criteria into one
                    childCriteria.getCriteria().addAll(subCriteria.getCriteria().getCriteria());
                    childCriteria.setConnection(subCriteria.getCriteria().getConnection());
                    childCriteria.setUseWildcardSearchMode(subCriteria.getCriteria()
                            .isUseWildcardSearchMode());
                    break;
                default:
                    otherSubCriterias.add(subCriteria);
            }
        }
    }

    private List<Long> findSampleIds(DetailedSearchCriteria criteria,
            List<DetailedSearchSubCriteria> subCriterias)
    {
        // for now we connect all sub criteria with logical AND
        List<DetailedSearchAssociationCriteria> associations =
                new ArrayList<DetailedSearchAssociationCriteria>();
        for (DetailedSearchSubCriteria subCriteria : subCriterias)
        {
            associations.add(findAssociatedEntities(subCriteria));
        }
        if (subCriterias.isEmpty() && criteria.getCriteria().isEmpty())
        {
            // if no criteria were provided find all samples
            criteria.getCriteria().add(
                    new DetailedSearchCriterion(DetailedSearchField
                            .createAttributeField(SampleAttributeSearchFieldKind.CODE), "*"));
        }
        final List<Long> sampleIds =
                searchDAO.searchForEntityIds(criteria,
                        DtoConverters.convertEntityKind(EntityKind.SAMPLE), associations);
        return sampleIds;
    }

}
