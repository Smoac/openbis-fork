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

package ch.ethz.sis.openbis.generic.server.api.v3.translator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * @author pkupczyk
 */
public abstract class AbstractCachingTranslator<I extends IIdHolder, O, F> extends AbstractTranslator<I, O, F>
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, AbstractCachingTranslator.class);

    @Override
    protected final O doTranslate(TranslationContext context, I object, F fetchOptions)
    {
        Map<I, O> translated = doTranslate(context, Collections.singleton(object), fetchOptions);
        if (translated.isEmpty())
        {
            return null;
        } else
        {
            return translated.get(object);
        }
    }

    @Override
    protected final Map<I, O> doTranslate(TranslationContext context, Collection<I> inputs, F fetchOptions)
    {
        Map<I, O> translated = new LinkedHashMap<I, O>();
        Map<I, O> updated = new HashMap<I, O>();
        TranslationCache cache = context.getTranslationCache();

        for (I input : inputs)
        {
            if (cache.hasTranslatedObject(getClass().getName(), input.getId()))
            {
                handleAlreadyTranslatedInput(context, input, translated, updated, fetchOptions);
            } else
            {
                handleNewInput(context, input, translated, updated, fetchOptions);
            }
        }

        if (false == updated.isEmpty())
        {
            Relations relations = getObjectsRelations(context, updated.keySet(), fetchOptions);
            relations.load();

            for (Map.Entry<I, O> updatedEntry : updated.entrySet())
            {
                updateObject(context, updatedEntry.getKey(), updatedEntry.getValue(), relations, fetchOptions);
            }
        }

        return translated;
    }

    @SuppressWarnings("unchecked")
    private final void handleAlreadyTranslatedInput(TranslationContext context, I input, Map<I, O> translated, Map<I, O> updated, F fetchOptions)
    {
        Long id = input.getId();
        TranslationCache cache = context.getTranslationCache();

        O output = (O) cache.getTranslatedObject(getClass().getName(), id);

        if (output == null)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Found that object was already rejected from translation: " + id);
            }
        } else
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Found in cache: " + output.getClass() + " with id: " + id);
            }

            if (cache.isFetchedWithOptions(output, fetchOptions))
            {
                translated.put(input, output);
            } else
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("Updating from cache: " + output.getClass() + " with id: " + id);
                }

                cache.setFetchedWithOptions(output, fetchOptions);
                updated.put(input, output);
                translated.put(input, output);
            }
        }
    }

    private void handleNewInput(TranslationContext context, I input, Map<I, O> translated, Map<I, O> updated, F fetchOptions)
    {
        Long id = input.getId();
        if (shouldTranslate(context, input, fetchOptions))
        {
            O output = createObject(context, input, fetchOptions);
            TranslationCache cache = context.getTranslationCache();

            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Created: " + output.getClass() + " with id: " + id);
            }

            cache.putTranslatedObject(getClass().getName(), id, output);
            cache.setFetchedWithOptions(output, fetchOptions);
            updated.put(input, output);
            translated.put(input, output);

            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Updating created: " + output.getClass() + " with id: " + id);
            }
        } else
        {
            operationLog.debug("Should not translate object: " + input.getClass() + " with id: " + id);
        }
    }

    /**
     * Override this method if you want to conditionally skip translation (e.g. when the input object is not visible for a user the translation is
     * performed for)
     */
    protected boolean shouldTranslate(TranslationContext context, I input, F fetchOptions)
    {
        return true;
    }

    /**
     * Implementation of this method should create a translated version of the input object. Only basic attributes of the input object should be
     * translated here. Parts that have a corresponding fetch option should be translated in the
     * {@link AbstractCachingTranslator#updateObject(TranslationContext, IIdHolder, Object, Relations, Object)} method.
     */
    protected abstract O createObject(TranslationContext context, I input, F fetchOptions);

    /**
     * Override this method if you want to fetch related objects for all the inputs at once. This way you can greatly improve the performance of the
     * translation.
     */
    protected Relations getObjectsRelations(TranslationContext context, Collection<I> inputs, F fetchOptions)
    {
        return new Relations();
    }

    /**
     * Implementation of this method should update the translated version of the input object to meet the fetch options.
     */
    protected abstract void updateObject(TranslationContext context, I input, O output, Relations relations, F fetchOptions);

}
