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

package ch.systemsx.cisd.openbis.generic.server.business;

import java.util.ArrayList;
import java.util.Collection;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;

/**
 * Manages detailed search with complex search criteria.
 * 
 * @author Piotr Buczek
 */
public class DetailedSearchManager
{
    private final IHibernateSearchDAO searchDAO;

    private final ISampleLister sampleLister;

    public DetailedSearchManager(IHibernateSearchDAO searchDAO, ISampleLister sampleLister)
    {
        this.searchDAO = searchDAO;
        this.sampleLister = sampleLister;
    }

    public List<Sample> searchForSamples(DetailedSearchCriteria criteria,
            List<DetailedSearchSubCriteria> subCriterias) throws DataAccessException
    {
        final DetailedSearchCriteria parentCriteria = new DetailedSearchCriteria();
        final List<DetailedSearchSubCriteria> otherSubCriterias =
                new ArrayList<DetailedSearchSubCriteria>();
        groupSampleSubCriteria(subCriterias, parentCriteria, otherSubCriterias);

        final List<Long> mainSampleIds = findSampleIds(criteria, otherSubCriterias);

        final Set<Long> filteredSampleIds;
        if (parentCriteria.isEmpty())
        {
            filteredSampleIds = new HashSet<Long>(mainSampleIds);
        } else
        {
            final List<Long> parentSampleIds =
                    findSampleIds(parentCriteria,
                            Collections.<DetailedSearchSubCriteria> emptyList());

            filteredSampleIds = new HashSet<Long>();

            if (mainSampleIds.size() > parentSampleIds.size())
            {
                // search for connections
                Map<Long, Set<Long>> parentToChildIds =
                        sampleLister.listChildrenIds(parentSampleIds);
                for (Set<Long> childrenIds : parentToChildIds.values())
                {
                    filteredSampleIds.addAll(childrenIds);
                }
                // filter main parents
                filteredSampleIds.retainAll(mainSampleIds);
            } else
            {
                // search for connections
                Map<Long, Set<Long>> childToParentIds = sampleLister.listParentIds(mainSampleIds);

                // filter main parents
                for (Entry<Long, Set<Long>> entry : childToParentIds.entrySet())
                {
                    Long childId = entry.getKey();
                    Set<Long> parentIds = entry.getValue();
                    parentIds.retainAll(parentSampleIds);
                    if (parentIds.isEmpty() == false)
                    {
                        filteredSampleIds.add(childId);
                    }
                }
            }
        }
        return sampleLister.list(new ListOrSearchSampleCriteria(filteredSampleIds));
    }

    private void groupSampleSubCriteria(List<DetailedSearchSubCriteria> allSubCriterias,
            DetailedSearchCriteria parentCriteria, List<DetailedSearchSubCriteria> otherSubCriterias)
    {
        parentCriteria.setCriteria(new ArrayList<DetailedSearchCriterion>());
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
        final List<Long> sampleIds =
                searchDAO.searchForEntityIds(criteria,
                        DtoConverters.convertEntityKind(EntityKind.SAMPLE), associations);
        return sampleIds;
    }

    private DetailedSearchAssociationCriteria findAssociatedEntities(
            DetailedSearchSubCriteria subCriteria)
    {
        // for now we don't support sub criteria of sub criteria
        List<DetailedSearchAssociationCriteria> associations = Collections.emptyList();
        final Collection<Long> associatedIds =
                searchDAO.searchForEntityIds(subCriteria.getCriteria(), DtoConverters
                        .convertEntityKind(subCriteria.getTargetEntityKind().getEntityKind()),
                        associations);

        return new DetailedSearchAssociationCriteria(subCriteria.getTargetEntityKind(),
                associatedIds);
    }

}
