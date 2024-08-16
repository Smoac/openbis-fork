/*
 * Copyright ETH 2015 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.datastore;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreKindSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodeMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.CodesMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SimpleFieldMatcher;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchDataStoreExecutor extends AbstractSearchObjectManuallyExecutor<DataStoreSearchCriteria, DataStorePE> implements
        ISearchDataStoreExecutor
{

    @Autowired
    private IDataStoreAuthorizationExecutor authorizationExecutor;

    @Override
    public List<DataStorePE> search(IOperationContext context, DataStoreSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<DataStorePE> listAll()
    {
        return daoFactory.getDataStoreDAO().listDataStores(true, true);
    }

    @Override
    protected List<DataStorePE> getMatching(final IOperationContext context, final List<DataStorePE> dataStorePES,
            final DataStoreSearchCriteria criteria)
    {
        if (criteria.getCriteria() == null || criteria.getCriteria().isEmpty())
        {
            // For backwards compatibility, by default, AFS should be filtered out
            return dataStorePES.stream().filter(dataStorePE -> !Objects.equals(dataStorePE.getCode(), Constants.AFS_DATA_STORE_CODE))
                    .collect(Collectors.toList());
        } else
        {
            return super.getMatching(context, dataStorePES, criteria);
        }
    }

    @Override
    protected Matcher<DataStorePE> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof PermIdSearchCriteria || criteria instanceof CodeSearchCriteria)
        {
            return new CodeMatcher<DataStorePE>();
        } else if (criteria instanceof CodesSearchCriteria)
        {
            return new CodesMatcher<DataStorePE>();
        } else if (criteria instanceof DataStoreKindSearchCriteria)
        {
            return new DataStoreKindMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private static class DataStoreKindMatcher extends Matcher<DataStorePE>
    {

        @Override
        public List<DataStorePE> getMatching(final IOperationContext context, final List<DataStorePE> dataStorePES,
                final ISearchCriteria criteria)
        {
            if (criteria instanceof DataStoreKindSearchCriteria)
            {
                final DataStoreKindSearchCriteria dataStoreKindSearchCriteria = (DataStoreKindSearchCriteria) criteria;
                final Set<DataStoreKind> dataStoreKinds = Set.of(dataStoreKindSearchCriteria.getDataStoreKinds());

                return dataStorePES.stream().filter(dataStorePE ->
                {
                    final String code = dataStorePE.getCode();
                    return Objects.equals(code, Constants.AFS_DATA_STORE_CODE) ? dataStoreKinds.contains(DataStoreKind.AFS)
                            : dataStoreKinds.contains(DataStoreKind.DSS);
                }).collect(Collectors.toList());
            } else
            {
                throw new IllegalArgumentException("No accepted search criteria: " + criteria.getClass());
            }
        }

    }

    private class IdMatcher extends SimpleFieldMatcher<DataStorePE>
    {

        @Override
        protected boolean isMatching(IOperationContext context, DataStorePE object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof DataStorePermId)
            {
                return object.getCode().equals(((DataStorePermId) id).getPermId());
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + id.getClass());
            }
        }
    }

}
