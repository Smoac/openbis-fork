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
    private final IRelationshipHandler CHILDREN_RELATIONSHIP_HANDLER = new IRelationshipHandler()
        {

            @Override
            public Collection<Long> findRelatedIdsByCriteria(DetailedSearchCriteria criteria,
                    List<DetailedSearchSubCriteria> otherSubCriterias)
            {
                return findSampleIds(criteria, otherSubCriterias);
            }

            @Override
            public Map<Long, Set<Long>> listIdsToRelatedIds(Collection<Long> sampleIds)
            {
                return lister.getParentToChildrenIdsMap(sampleIds);
            }

            @Override
            public Map<Long, Set<Long>> listRelatedIdsToIds(Collection<Long> childrenSampleIds)
            {
                return lister.getChildToParentsIdsMap(childrenSampleIds);
            }
        };

    private final IRelationshipHandler PARENT_RELATIONSHIP_HANDLER = new IRelationshipHandler()
        {

            @Override
            public Collection<Long> findRelatedIdsByCriteria(DetailedSearchCriteria criteria,
                    List<DetailedSearchSubCriteria> otherSubCriterias)
            {
                return findSampleIds(criteria, otherSubCriterias);
            }

            @Override
            public Map<Long, Set<Long>> listIdsToRelatedIds(Collection<Long> sampleIds)
            {
                return lister.getChildToParentsIdsMap(sampleIds);
            }

            @Override
            public Map<Long, Set<Long>> listRelatedIdsToIds(Collection<Long> parentSampleIds)
            {
                return lister.getParentToChildrenIdsMap(parentSampleIds);
            }
        };

    public SampleSearchManager(IHibernateSearchDAO searchDAO, ISampleLister sampleLister)
    {
        super(searchDAO, sampleLister);
    }

    public List<Sample> searchForSamples(DetailedSearchCriteria criteria)
            throws DataAccessException
    {
        return lister.list(new ListOrSearchSampleCriteria(searchForSampleIDs(criteria)));
    }

    public Collection<Long> searchForSampleIDs(DetailedSearchCriteria criteria)
    {
        DetailedSearchCriteria parentCriteria = new DetailedSearchCriteria();
        DetailedSearchCriteria childCriteria = new DetailedSearchCriteria();
        List<DetailedSearchSubCriteria> otherSubCriterias =
                new ArrayList<DetailedSearchSubCriteria>();
        groupSampleSubCriteria(criteria.getSubCriterias(), parentCriteria, childCriteria,
                otherSubCriterias);

        final List<Long> mainSampleIds = findSampleIds(criteria, otherSubCriterias);

        Collection<Long> filteredSampleIds = mainSampleIds;
        if (false == parentCriteria.isEmpty())
        {
            filteredSampleIds =
                    filterSearchResultsBySubcriteria(filteredSampleIds, parentCriteria,
                            PARENT_RELATIONSHIP_HANDLER);
        }

        if (false == childCriteria.isEmpty())
        {
            filteredSampleIds =
                    filterSearchResultsBySubcriteria(filteredSampleIds, childCriteria,
                            CHILDREN_RELATIONSHIP_HANDLER);
        }

        Collection<Long> sampleIDs = restrictResultSetIfNecessary(filteredSampleIds);
        return sampleIDs;
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
                    mergeSubCriteria(parentCriteria, subCriteria);
                    break;
                case SAMPLE_CHILD:
                    // merge all child sub criteria into one
                    mergeSubCriteria(childCriteria, subCriteria);
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
