/*
 * Copyright ETH 2017 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.semanticannotation;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.SemanticAnnotation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.fetchoptions.SemanticAnnotationFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SearchSemanticAnnotationsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SearchSemanticAnnotationsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsPEOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.semanticannotation.ISemanticAnnotationTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SemanticAnnotationPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchSemanticAnnotationsOperationExecutor extends
        SearchObjectsPEOperationExecutor<SemanticAnnotation, SemanticAnnotationPE, SemanticAnnotationSearchCriteria, SemanticAnnotationFetchOptions>
        implements ISearchSemanticAnnotationsOperationExecutor
{

    @Autowired
    private ISearchSemanticAnnotationExecutor searchExecutor;

    @Autowired
    private ISemanticAnnotationTranslator translator;

    @Override
    protected Class<? extends SearchObjectsOperation<SemanticAnnotationSearchCriteria, SemanticAnnotationFetchOptions>> getOperationClass()
    {
        return SearchSemanticAnnotationsOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<SemanticAnnotationSearchCriteria, SemanticAnnotationPE> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, SemanticAnnotation, SemanticAnnotationFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<SemanticAnnotation> getOperationResult(SearchResult<SemanticAnnotation> searchResult)
    {
        return new SearchSemanticAnnotationsOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<SemanticAnnotationSearchCriteria, SemanticAnnotation, Long> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

}
