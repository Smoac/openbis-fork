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
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.FetchOptions;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author pkupczyk
 */
public abstract class AbstractCachingTranslator<I, O, F extends FetchOptions<?>> extends AbstractTranslator<I, O, F>
{

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    @Autowired
    private ApplicationContext applicationContext;

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
    protected final Map<I, O> doTranslate(TranslationContext context, Collection<I> allInputs, F fetchOptions)
    {
        Map<I, O> translated = new LinkedHashMap<I, O>();
        Map<I, O> updated = new HashMap<I, O>();
        TranslationCache cache = context.getTranslationCache();

        Collection<I> inputs = doShouldTranslate(context, allInputs, fetchOptions);

        for (I input : inputs)
        {
            if (cache.hasTranslatedObject(getClass().getName(), getId(input)))
            {
                handleAlreadyTranslatedInput(context, input, translated, updated, fetchOptions);
            } else
            {
                handleNewInput(context, input, translated, updated, fetchOptions);
            }
        }

        if (false == updated.isEmpty())
        {
            Object relations = getObjectsRelations(context, updated.keySet(), fetchOptions);

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
        Long id = getId(input);
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
        Long id = getId(input);
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
    }

    private Long getId(I input)
    {
        if (input instanceof IIdHolder)
        {
            return HibernateUtils.getId((IIdHolder) input);
        } else if (input instanceof Long)
        {
            return (Long) input;
        } else
        {
            throw new IllegalArgumentException("Unsupported input type: " + input.getClass());
        }
    }

    private final Collection<I> doShouldTranslate(TranslationContext context, Collection<I> inputs, F fetchOptions)
    {
        TranslationCache cache = context.getTranslationCache();
        Collection<I> toCheck = new LinkedHashSet<I>();
        Collection<I> toTranslate = new LinkedHashSet<I>();

        for (I input : inputs)
        {
            Long id = getId(input);

            if (cache.hasShouldTranslateObject(getClass().getName(), id))
            {
                boolean should = cache.getShouldTranslateObject(getClass().getName(), id);
                if (should)
                {
                    toTranslate.add(input);
                }

                if (operationLog.isDebugEnabled())
                {
                    if (should)
                    {
                        operationLog.debug("Found in cache that object with id: " + id + " should be translated");
                    } else
                    {
                        operationLog.debug("Found in cache that object with id: " + id + " should NOT be translated");
                    }
                }
            } else
            {
                toCheck.add(input);
            }
        }

        Collection<I> checked = shouldTranslate(context, toCheck, fetchOptions);
        toTranslate.addAll(checked);

        for (I input : checked)
        {
            Long id = getId(input);
            cache.putShouldTranslateObject(getClass().getName(), id, true);
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Should translate object with id: " + id);
            }
        }

        toCheck.removeAll(checked);
        for (I input : toCheck)
        {
            Long id = getId(input);
            cache.putShouldTranslateObject(getClass().getName(), id, false);
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Should NOT translate object with id: " + id);
            }
        }

        return toTranslate;
    }

    /**
     * Override this method if you want to conditionally skip translation (e.g. when the input object is not visible for a user the translation is
     * performed for)
     */
    protected Collection<I> shouldTranslate(TranslationContext context, Collection<I> inputs, F fetchOptions)
    {
        Collection<I> result = new LinkedHashSet<I>();

        for (I input : inputs)
        {
            if (shouldTranslate(context, input, fetchOptions))
            {
                result.add(input);
            }
        }

        return result;
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
     * {@link AbstractCachingTranslator#updateObject(TranslationContext, Object, Object, Object, FetchOptions)} method.
     */
    protected abstract O createObject(TranslationContext context, I input, F fetchOptions);

    /**
     * Override this method if you want to fetch related objects for all the inputs at once. This way you can greatly improve the performance of the
     * translation.
     */
    protected Object getObjectsRelations(TranslationContext context, Collection<I> inputs, F fetchOptions)
    {
        return new Object();
    }

    /**
     * Implementation of this method should update the translated version of the input object to meet the fetch options.
     */
    protected abstract void updateObject(TranslationContext context, I input, O output, Object relations, F fetchOptions);

}
