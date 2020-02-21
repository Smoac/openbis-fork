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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.SortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;

/**
 * @author pkupczyk
 */
public abstract class SearchObjectsOperationExecutor<OBJECT, OBJECT_PE, CRITERIA extends AbstractSearchCriteria,
        FETCH_OPTIONS extends FetchOptions<OBJECT>> extends AbstractSearchObjectsOperationExecutor<OBJECT, OBJECT_PE, CRITERIA, FETCH_OPTIONS>
{

    protected abstract ISearchObjectExecutor<CRITERIA, OBJECT_PE> getExecutor();

    protected abstract ITranslator<OBJECT_PE, OBJECT, FETCH_OPTIONS> getTranslator();

    @Override
    protected final List<OBJECT_PE> doSearch(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        return getExecutor().search(context, criteria);
    }

    @Override
    protected final Map<OBJECT_PE, OBJECT> doTranslate(TranslationContext translationContext, List<OBJECT_PE> ids, FETCH_OPTIONS fetchOptions)
    {
        return getTranslator().translate(translationContext, ids, fetchOptions);
    }

    @Override
    protected ISearchManager<CRITERIA, OBJECT, OBJECT_PE> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

}
