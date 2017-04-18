/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.DataStore;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.fetchoptions.DataStoreFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.datastore.ISearchDataStoreExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.datastore.IDataStoreTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchDataStoreMethodExecutor
        extends AbstractIdSearchMethodExecutor<DataStore, DataStorePE, DataStoreSearchCriteria, DataStoreFetchOptions>
        implements ISearchDataStoreMethodExecutor
{

    @Autowired
    private ISearchDataStoreExecutor searchExecutor;

    @Autowired
    private IDataStoreTranslator translator;

    @Override
    protected List<DataStorePE> searchPEs(IOperationContext context, DataStoreSearchCriteria criteria)
    {
        return searchExecutor.search(context, criteria);
    }

    @Override
    protected ITranslator<Long, DataStore, DataStoreFetchOptions> getTranslator()
    {
        return translator;
    }

}
