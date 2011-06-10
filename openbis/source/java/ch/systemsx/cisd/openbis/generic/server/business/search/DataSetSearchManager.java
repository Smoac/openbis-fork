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
import java.util.List;

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
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
        
        List<DetailedSearchAssociationCriteria> associations =
            new ArrayList<DetailedSearchAssociationCriteria>();
        for (DetailedSearchSubCriteria subCriteria : criteria.getSubCriterias())
        {
            associations.add(findAssociatedEntities(subCriteria));
        }
        final List<Long> dataSetIds =
            searchDAO.searchForEntityIds(criteria,
                    DtoConverters.convertEntityKind(EntityKind.DATA_SET), associations);
        
        return lister.listByDatasetIds(dataSetIds);
    }


}
