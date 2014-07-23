/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.search;

import java.util.List;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriterion;

/**
 * @author pkupczyk
 */
public abstract class AbstractSearchCriterionTranslatorFactory
{

    private List<ISearchCriterionTranslator> translators;

    public ISearchCriterionTranslator getTranslator(ISearchCriterion criterion)
    {
        for (ISearchCriterionTranslator translator : getTranslators())
        {
            if (translator.accepts(criterion))
            {
                return translator;
            }
        }

        throw new IllegalArgumentException("Could not find any translator that accepts criterion: " + criterion);
    }

    private List<ISearchCriterionTranslator> getTranslators()
    {
        if (translators == null)
        {
            translators = createTranslators();
        }
        return translators;
    }

    protected abstract List<ISearchCriterionTranslator> createTranslators();

}
