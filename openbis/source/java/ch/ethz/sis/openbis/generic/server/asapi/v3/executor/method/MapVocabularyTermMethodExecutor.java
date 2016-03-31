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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary.IMapVocabularyTermByIdExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.vocabulary.IVocabularyTermTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
@Component
public class MapVocabularyTermMethodExecutor extends AbstractMapMethodExecutor<IVocabularyTermId, Long, VocabularyTerm, VocabularyTermFetchOptions>
        implements IMapVocabularyTermMethodExecutor
{

    @Autowired
    private IMapVocabularyTermByIdExecutor mapExecutor;

    @Autowired
    private IVocabularyTermTranslator translator;

    @Override
    protected IMapObjectByIdExecutor<IVocabularyTermId, Long> getMapExecutor()
    {
        return new IMapObjectByIdExecutor<IVocabularyTermId, Long>()
            {
                @Override
                public Map<IVocabularyTermId, Long> map(IOperationContext context, Collection<? extends IVocabularyTermId> ids)
                {
                    Map<IVocabularyTermId, Long> idMap = new LinkedHashMap<IVocabularyTermId, Long>();
                    Map<IVocabularyTermId, VocabularyTermPE> peMap = mapExecutor.map(context, ids);

                    for (Map.Entry<IVocabularyTermId, VocabularyTermPE> entry : peMap.entrySet())
                    {
                        idMap.put(entry.getKey(), entry.getValue().getId());
                    }

                    return idMap;
                }
            };
    }

    @Override
    protected ITranslator<Long, VocabularyTerm, VocabularyTermFetchOptions> getTranslator()
    {
        return translator;
    }

}
