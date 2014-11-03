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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.common;

import java.util.Collection;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;

/**
 * @author pkupczyk
 */
public abstract class AbstractCollectionTranslator
{

    public <I, O> Collection<O> translate(Collection<? extends I> collection, ITranslator<I, O> itemTranslator)
    {
        if (collection == null)
        {
            return null;
        }

        Collection<O> result = createCollection();

        for (I item : collection)
        {
            O translationResult = itemTranslator.translate(item);
            if (translationResult != null)
            {
                result.add(translationResult);
            }
        }

        return result;
    }

    protected abstract <O> Collection<O> createCollection();

}
