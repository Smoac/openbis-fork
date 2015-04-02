/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.vocabulary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.Relations;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.person.IPersonTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.person.Person;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.vocabulary.Vocabulary;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.vocabulary.VocabularyTerm;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.vocabulary.VocabularyTermFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;

/**
 * @author pkupczyk
 */
@Component
public class VocabularyTermTranslator extends AbstractCachingTranslator<VocabularyTermPE, VocabularyTerm, VocabularyTermFetchOptions> implements
        IVocabularyTermTranslator
{

    @Autowired
    private IPersonTranslator personTranslator;

    @Autowired
    private IVocabularyTranslator vocabularyTranslator;

    @Override
    protected VocabularyTerm createObject(TranslationContext context, VocabularyTermPE term, VocabularyTermFetchOptions fetchOptions)
    {
        VocabularyTerm result = new VocabularyTerm();

        result.setCode(term.getCode());
        result.setDescription(term.getDescription());
        result.setLabel(term.getLabel());
        result.setOfficial(term.isOfficial());
        result.setOrdinal(term.getOrdinal());
        result.setRegistrationDate(term.getRegistrationDate());
        result.setModificationDate(term.getModificationDate());
        result.setFetchOptions(new VocabularyTermFetchOptions());

        return result;
    }

    @Override
    protected void updateObject(TranslationContext context, VocabularyTermPE term, VocabularyTerm result, Relations relations,
            VocabularyTermFetchOptions fetchOptions)
    {
        if (fetchOptions.hasRegistrator())
        {
            Person registrator = personTranslator.translate(context, term.getRegistrator(), fetchOptions.withRegistrator());
            result.setRegistrator(registrator);
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }

        if (fetchOptions.hasVocabulary())
        {
            Vocabulary vocabulary = vocabularyTranslator.translate(context, term.getVocabulary(), fetchOptions.withVocabulary());
            result.setVocabulary(vocabulary);
            result.getFetchOptions().withVocabularyUsing(fetchOptions.withVocabulary());
        }
    }

}
