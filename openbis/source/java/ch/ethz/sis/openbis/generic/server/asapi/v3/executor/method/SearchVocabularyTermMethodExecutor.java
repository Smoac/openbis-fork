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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.search.VocabularyTermSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary.ISearchVocabularyTermExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.vocabulary.IVocabularyTermTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
@Component
public class SearchVocabularyTermMethodExecutor
        extends AbstractSearchMethodExecutor<VocabularyTerm, Long, VocabularyTermSearchCriteria, VocabularyTermFetchOptions>
        implements ISearchVocabularyTermMethodExecutor
{

    @Autowired
    private ISearchVocabularyTermExecutor searchExecutor;

    @Autowired
    private IVocabularyTermTranslator translator;

    @Override
    protected ISearchObjectExecutor<VocabularyTermSearchCriteria, Long> getSearchExecutor()
    {
        return new ISearchObjectExecutor<VocabularyTermSearchCriteria, Long>()
            {
                @Override
                public List<Long> search(IOperationContext context, VocabularyTermSearchCriteria criteria)
                {
                    List<VocabularyTermPE> terms = searchExecutor.search(context, criteria);
                    List<Long> ids = new ArrayList<Long>();

                    for (VocabularyTermPE term : terms)
                    {
                        ids.add(term.getId());
                    }

                    return ids;
                }
            };
    }

    @Override
    protected ITranslator<Long, VocabularyTerm, VocabularyTermFetchOptions> getTranslator()
    {
        return translator;
    }

}
