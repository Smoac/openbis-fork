/*
 * Copyright 2012 ETH Zuerich, CISD
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
 * Adapter adapting {@link VocabularyImmutable} to {@link IVocabulary}. Setter methods do nothing.
 * 
 * @author Franz-Josef Elmer
 */
class VocabularyWrapper extends VocabularyImmutable implements IVocabulary
{
    VocabularyWrapper(VocabularyImmutable vocabulary)
    {
        super(vocabulary.getVocabulary());
    }

    @Override
    public void setDescription(String description)
    {
    }

    @Override
    public void setManagedInternally(boolean isManagedInternally)
    {
    }

    @Override
    public void setInternalNamespace(boolean isInternalNamespace)
    {
    }

    @Override
    public void setChosenFromList(boolean isChosenFromList)
    {
    }

    @Override
    public void setUrlTemplate(String urlTemplate)
    {
    }

    @Override
    public void addTerm(IVocabularyTerm term)
    {
    }
}
