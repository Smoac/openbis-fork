/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.ProcessingService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.fetchoptions.ProcessingServiceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.ProcessingServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchProcessingServicesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.SearchProcessingServicesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class SearchProcessingServicesOperationExecutor
        extends
        SearchObjectsOperationExecutor<ProcessingService, ProcessingService, ProcessingServiceSearchCriteria, ProcessingServiceFetchOptions>
        implements ISearchProcessingServicesOperationExecutor
{
    @Autowired
    private ISearchProcessingServiceExecutor searchExecutor;

    @Override
    protected ISearchObjectExecutor<ProcessingServiceSearchCriteria, ProcessingService> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<ProcessingService, ProcessingService, ProcessingServiceFetchOptions> getTranslator()
    {
        return new AbstractTranslator<ProcessingService, ProcessingService, ProcessingServiceFetchOptions>()
            {
                @Override
                protected ProcessingService doTranslate(TranslationContext context, ProcessingService object,
                        ProcessingServiceFetchOptions fetchOptions)
                {
                    object.setFetchOptions(fetchOptions);
                    return object;
                }
            };
    }

    @Override
    protected SearchObjectsOperationResult<ProcessingService> getOperationResult(SearchResult<ProcessingService> searchResult)
    {
        return new SearchProcessingServicesOperationResult(searchResult);
    }

    @Override
    protected Class<? extends SearchObjectsOperation<ProcessingServiceSearchCriteria, ProcessingServiceFetchOptions>> getOperationClass()
    {
        return SearchProcessingServicesOperation.class;
    }

}
