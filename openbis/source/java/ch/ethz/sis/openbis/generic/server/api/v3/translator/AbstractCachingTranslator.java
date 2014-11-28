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
public abstract class AbstractCachingTranslator<I extends IIdHolder, O, F> extends AbstractTranslator<I, O>
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, AbstractCachingTranslator.class);

    protected TranslationContext translationContext;

    private F fetchOptions;

    public AbstractCachingTranslator(TranslationContext translationContext, F fetchOptions)
    {
        this.translationContext = translationContext;
        this.fetchOptions = fetchOptions;
    }

    @Override
    protected final O doTranslate(I object)
    {
        Map<I, O> translated = doTranslate(Collections.singleton(object));
        if (translated.isEmpty())
        {
            return null;
        } else
        {
            return translated.get(object);
        }
    }

    @Override
    protected final Map<I, O> doTranslate(Collection<I> inputs)
    {
        Map<I, O> translated = new LinkedHashMap<I, O>();
        Map<I, O> updated = new HashMap<I, O>();

        for (I input : inputs)
        {
            if (getTranslationCache().hasTranslatedObject(getClass().getName(), input.getId()))
            {
                handleAlreadyTranslatedInput(input, translated, updated);
            } else
            {
                handleNewInput(input, translated, updated);
            }
        }

        if (false == updated.isEmpty())
        {
            Relations relations = getObjectsRelations(updated.keySet());
            relations.load();

            for (Map.Entry<I, O> updatedEntry : updated.entrySet())
            {
                updateObject(updatedEntry.getKey(), updatedEntry.getValue(), relations);
            }
        }

        return translated;
    }

    @SuppressWarnings("unchecked")
    private final void handleAlreadyTranslatedInput(I input, Map<I, O> translated, Map<I, O> updated)
    {
        Long id = input.getId();
        O output = (O) getTranslationCache().getTranslatedObject(getClass().getName(), id);

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

            if (getTranslationCache().isFetchedWithOptions(output, getFetchOptions()))
            {
                translated.put(input, output);
            } else
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("Updating from cache: " + output.getClass() + " with id: " + id);
                }

                getTranslationCache().setFetchedWithOptions(output, getFetchOptions());
                updated.put(input, output);
                translated.put(input, output);
            }
        }
    }

    private void handleNewInput(I input, Map<I, O> translated, Map<I, O> updated)
    {
        Long id = input.getId();
        if (shouldTranslate(input))
        {
            O output = createObject(input);

            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Created: " + output.getClass() + " with id: " + id);
            }

            getTranslationCache().putTranslatedObject(getClass().getName(), id, output);
            getTranslationCache().setFetchedWithOptions(output, getFetchOptions());
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
    protected boolean shouldTranslate(I input)
    {
        return true;
    }

    /**
     * Override this method if you want to fetch related objects for all the inputs at once. This way you can greatly improve the performance of the
     * translation.
     */
    protected Relations getObjectsRelations(Collection<I> inputs)
    {
        return new Relations();
    }

    /**
     * Implementation of this method should create a translated version of the input object. Only basic attributes of the input object should be
     * translated here. Parts that have a corresponding fetch option should be translated in the
     * {@link AbstractCachingTranslator#updateObject(IIdHolder, Object, Relations)} method.
     */
    protected abstract O createObject(I input);

    /**
     * Implementation of this method should update the translated version of the input object to meet the fetch options (see
     * {@link AbstractCachingTranslator#getFetchOptions()}.
     */
    protected abstract void updateObject(I input, O output, Relations relations);

    protected final TranslationContext getTranslationContext()
    {
        return translationContext;
    }

    protected final TranslationCache getTranslationCache()
    {
        return getTranslationContext().getTranslationCache();
    }

    protected final F getFetchOptions()
    {
        return fetchOptions;
    }

}
