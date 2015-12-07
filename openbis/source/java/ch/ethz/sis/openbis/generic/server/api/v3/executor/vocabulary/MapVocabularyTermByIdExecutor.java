/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.vocabulary;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.vocabulary.id.IVocabularyId;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.vocabulary.id.IVocabularyTermId;
import ch.ethz.sis.openbis.generic.as.api.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.MapObjectById;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.vocabulary.ListVocabularyTermByCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
@Component
public class MapVocabularyTermByIdExecutor implements IMapVocabularyTermByIdExecutor
{

    @Autowired
    private IMapVocabularyByIdExecutor mapVocabularyByIdExecutor;

    @Override
    public Map<IVocabularyTermId, VocabularyTermPE> map(IOperationContext context, IVocabularyId vocabularyId,
            Collection<? extends IVocabularyTermId> vocabularyTermIds)
    {
        List<IListObjectById<? extends IVocabularyTermId, VocabularyTermPE>> listers =
                new LinkedList<IListObjectById<? extends IVocabularyTermId, VocabularyTermPE>>();

        if (vocabularyId != null)
        {
            Map<IVocabularyId, VocabularyPE> vocabularies = mapVocabularyByIdExecutor.map(context, Collections.singleton(vocabularyId));

            VocabularyPE vocabulary = vocabularies.get(vocabularyId);
            if (vocabulary == null)
            {
                throw new ObjectNotFoundException(vocabularyId);
            }

            listers.add(new ListVocabularyTermByCode(vocabulary));
        }

        return new MapObjectById<IVocabularyTermId, VocabularyTermPE>().map(listers, vocabularyTermIds);
    }

}
