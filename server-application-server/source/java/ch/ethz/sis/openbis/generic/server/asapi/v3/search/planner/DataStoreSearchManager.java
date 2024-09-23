/*
 * Copyright ETH 2019 - 2023 Zürich, Scientific IT Services
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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.DataStore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreKindSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PEMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

/**
 * Manages detailed search with data store search criteria.
 *
 * @author Viktor Kovtun
 */
public class DataStoreSearchManager extends AbstractLocalSearchManager<DataStoreSearchCriteria, DataStore, Long>
{

    public DataStoreSearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PEMapper<Long, Long> idsMapper)
    {
        super(searchDAO, authProvider, idsMapper);
    }

    @Override
    protected AbstractCompositeSearchCriteria createEmptyCriteria(final boolean negated)
    {
        return new DataStoreSearchCriteria();
    }

    @Override
    protected Set<Long> doFilterIDsByUserRights(final Set<Long> ids, final AuthorisationInformation authorisationInformation)
    {
        // No filtering of data stores is needed.
        return ids;
    }

    @Override
    public Set<Long> searchForIDs(final Long userId, final AuthorisationInformation authorisationInformation,
            final DataStoreSearchCriteria criteria, final AbstractCompositeSearchCriteria parentCriteria, final String idsColumnName)
    {
        final Collection<ISearchCriteria> criteriaCollection = criteria.getCriteria();
        final boolean containsKindSearchCriteria = criteriaCollection.stream()
                .anyMatch(criterion -> criterion instanceof DataStoreKindSearchCriteria);

        if (criteriaCollection.isEmpty() || containsKindSearchCriteria || parentCriteria instanceof DataStoreSearchCriteria)
        {
            return super.searchForIDs(userId, authorisationInformation, criteria, null, TableMapper.DATA_STORE);
        } else
        {
            final DataStoreSearchCriteria newCriteria = new DataStoreSearchCriteria();
            newCriteria.withAndOperator();

            newCriteria.withSubcriteria(criteria);
            newCriteria.withKind().thatIn(DataStoreKind.DSS);

            return super.searchForIDs(userId, authorisationInformation, newCriteria, null, TableMapper.DATA_STORE);
        }
    }

    @Override
    public List<Long> sortIDs(final Collection<Long> ids, final SortOptions<DataStore> sortOptions)
    {
        return doSortIDs(ids, sortOptions, TableMapper.DATA_STORE);
    }

}
