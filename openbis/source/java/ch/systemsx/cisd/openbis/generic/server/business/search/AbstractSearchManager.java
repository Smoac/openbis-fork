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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;

/**
 * Manages detailed search with complex search criteria.
 * 
 * @author Piotr Buczek
 * @author Kaloyan Enimanev
 */
public class AbstractSearchManager<T>
{
    protected final IHibernateSearchDAO searchDAO;

    protected final T lister;

    public AbstractSearchManager(IHibernateSearchDAO searchDAO, T lister)
    {
        this.searchDAO = searchDAO;
        this.lister = lister;
    }

    protected DetailedSearchAssociationCriteria findAssociatedEntities(
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

    protected void mergeSubCriteria(DetailedSearchCriteria criteria,
            DetailedSearchSubCriteria subCriteriaToMerge)
    {
        criteria.getCriteria().addAll(subCriteriaToMerge.getCriteria().getCriteria());
        criteria.setConnection(subCriteriaToMerge.getCriteria().getConnection());
        criteria.setUseWildcardSearchMode(subCriteriaToMerge.getCriteria()
                .isUseWildcardSearchMode());
    }

}
