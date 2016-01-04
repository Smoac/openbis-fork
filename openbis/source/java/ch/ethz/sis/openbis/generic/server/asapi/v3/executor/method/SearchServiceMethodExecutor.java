/*
 * Copyright 2015 ETH Zuerich, SIS
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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.Service;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.ServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.ServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.SortAndPage;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class SearchServiceMethodExecutor extends AbstractSearchObjectManuallyExecutor<ServiceSearchCriteria, Service>
        implements ISearchServiceMethodExecutor
{
    @Autowired
    private IServiceProvider serviceProvider;

    @Resource(name = ComponentNames.SESSION_MANAGER)
    private IOpenBisSessionManager sessionManager;

    @Override
    public SearchResult<Service> search(String sessionToken, ServiceSearchCriteria searchCriteria, ServiceFetchOptions fetchOptions)
    {
        List<Service> services = search(new OperationContext(sessionManager.getSession(sessionToken)), searchCriteria);
        List<Service> filtered = new ArrayList<>(new SortAndPage().sortAndPage(services, fetchOptions));
        return new SearchResult<Service>(filtered, services.size());
    }

    @Override
    protected List<Service> listAll()
    {
        return serviceProvider.getServices();
    }

    @Override
    protected Matcher getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof CodeSearchCriteria)
        {
            return new CodeMatcher();
        }
        throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
    }

    private class CodeMatcher extends StringFieldMatcher
    {

        @Override
        protected String getFieldValue(Service object)
        {
            return object.getCode().getPermId();
        }

    }
}
