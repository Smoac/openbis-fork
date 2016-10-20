/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.globalsearch;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.SearchGloballyOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.SearchGloballyOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.globalsearch.IGlobalSearchObjectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;

/**
 * @author pkupczyk
 */
@Component
public class SearchGloballyOperationExecutor
        extends SearchObjectsOperationExecutor<GlobalSearchObject, MatchingEntity, GlobalSearchCriteria, GlobalSearchObjectFetchOptions>
        implements ISearchGloballyOperationExecutor
{

    @Autowired
    private IGlobalSearchExecutor searchExecutor;

    @Autowired
    private IGlobalSearchObjectTranslator translator;

    @Autowired
    private IGlobalAuthorizationExecutor authorizationExecutor;

    @Override
    public Map<IOperation, IOperationResult> execute(IOperationContext context, List<? extends IOperation> operations)
    {
        authorizationExecutor.canSearch(context);
        return super.execute(context, operations);
    }

    @Override
    protected Class<? extends SearchObjectsOperation<GlobalSearchCriteria, GlobalSearchObjectFetchOptions>> getOperationClass()
    {
        return SearchGloballyOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<GlobalSearchCriteria, MatchingEntity> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<MatchingEntity, GlobalSearchObject, GlobalSearchObjectFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<GlobalSearchObject> getOperationResult(SearchResult<GlobalSearchObject> searchResult)
    {
        return new SearchGloballyOperationResult(searchResult);
    }

}
