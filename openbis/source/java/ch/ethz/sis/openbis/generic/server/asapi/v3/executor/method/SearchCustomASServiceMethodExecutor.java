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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.CustomASService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.CustomASServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.CustomASServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.StringFieldMatcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.SortAndPage;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class SearchCustomASServiceMethodExecutor extends AbstractSearchObjectManuallyExecutor<CustomASServiceSearchCriteria, CustomASService>
        implements ISearchCustomASServiceMethodExecutor
{
    @Autowired
    private IServiceProvider serviceProvider;

    @Resource(name = ComponentNames.SESSION_MANAGER)
    private IOpenBisSessionManager sessionManager;

    @Override
    public SearchResult<CustomASService> search(String sessionToken, CustomASServiceSearchCriteria searchCriteria,
            CustomASServiceFetchOptions fetchOptions)
    {
        List<CustomASService> services = search(new OperationContext(sessionManager.getSession(sessionToken)), searchCriteria);
        List<CustomASService> filtered = new ArrayList<>(new SortAndPage().sortAndPage(services, searchCriteria, fetchOptions));
        return new SearchResult<CustomASService>(filtered, services.size());
    }

    @Override
    protected List<CustomASService> listAll()
    {
        return serviceProvider.getCustomASServices();
    }

    @Override
    protected Matcher<CustomASService> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof CodeSearchCriteria)
        {
            return new CodeMatcher();
        }
        throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
    }

    private class CodeMatcher extends StringFieldMatcher<CustomASService>
    {

        @Override
        protected String getFieldValue(CustomASService object)
        {
            return object.getCode().getPermId();
        }

    }
}
