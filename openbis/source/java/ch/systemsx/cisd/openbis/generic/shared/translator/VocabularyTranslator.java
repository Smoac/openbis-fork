/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.utilities.ReflectingStringEscaper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Izabela Adamczyk
 */
public final class VocabularyTranslator
{
    private VocabularyTranslator()
    {
        // Can not be instantiated.
    }

    public final static Vocabulary translate(final VocabularyPE vocabulary)
    {
        if (vocabulary == null)
        {
            return null;
        }
        final Vocabulary result = new Vocabulary();
        result.setId(HibernateUtils.getId(vocabulary));
        result.setCode(vocabulary.getCode());
        result.setDescription(vocabulary.getDescription());
        result.setInternalNamespace(vocabulary.isInternalNamespace());
        result.setManagedInternally(vocabulary.isManagedInternally());
        result.setChosenFromList(vocabulary.isChosenFromList());
        result.setURLTemplate(vocabulary.getURLTemplate());
        result.setRegistrationDate(vocabulary.getRegistrationDate());
        result.setRegistrator(PersonTranslator.translate(vocabulary.getRegistrator()));
        return ReflectingStringEscaper.escapeShallow(result);
    }

    public static List<Vocabulary> translate(List<VocabularyPE> vocabularies)
    {
        List<Vocabulary> result = new ArrayList<Vocabulary>();
        for (VocabularyPE v : vocabularies)
        {
            result.add(translate(v));
        }
        return result;
    }

}
