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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.vocabulary.VocabularyFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;

/**
 * @author pkupczyk
 */
@Component
public class VocabularyTranslator extends AbstractCachingTranslator<VocabularyPE, Vocabulary, VocabularyFetchOptions> implements
        IVocabularyTranslator
{

    @Autowired
    private IPersonTranslator personTranslator;

    @Override
    protected Vocabulary createObject(TranslationContext context, VocabularyPE vocabulary, VocabularyFetchOptions fetchOptions)
    {
        Vocabulary result = new Vocabulary();

        result.setCode(vocabulary.getCode());
        result.setDescription(vocabulary.getDescription());
        result.setRegistrationDate(vocabulary.getRegistrationDate());
        result.setModificationDate(vocabulary.getModificationDate());
        result.setFetchOptions(new VocabularyFetchOptions());

        return result;
    }

    @Override
    protected void updateObject(TranslationContext context, VocabularyPE vocabulary, Vocabulary result, Relations relations,
            VocabularyFetchOptions fetchOptions)
    {
        if (fetchOptions.hasRegistrator())
        {
            Person registrator = personTranslator.translate(context, vocabulary.getRegistrator(), fetchOptions.withRegistrator());
            result.setRegistrator(registrator);
            result.getFetchOptions().withRegistratorUsing(fetchOptions.withRegistrator());
        }
    }

}
