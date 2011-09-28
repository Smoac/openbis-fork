/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl;

import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabulary;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IVocabularyTerm;

/**
 * @author Kaloyan Enimanev
 */
public class Vocabulary extends VocabularyImmutable implements IVocabulary
{

    Vocabulary(String code)
    {
        super(code);
    }

    public void setDescription(String description)
    {
        getVocabulary().setDescription(description);
    }

    public void setManagedInternally(boolean isManagedInternally)
    {
        getVocabulary().setManagedInternally(isManagedInternally);
    }

    public void setInternalNamespace(boolean isInternalNamespace)
    {
        getVocabulary().setInternalNamespace(isInternalNamespace);
    }

    public void setChosenFromList(boolean isChosenFromList)
    {
        getVocabulary().setChosenFromList(isChosenFromList);
    }

    public void setUrlTemplate(String urlTemplate)
    {
        getVocabulary().setURLTemplate(urlTemplate);
    }

    public void addTerm(IVocabularyTerm term)
    {
        VocabularyTerm internalTerm = (VocabularyTerm) term;
        getVocabulary().getTerms().add(internalTerm.getVocabularyTerm());
    }

}
