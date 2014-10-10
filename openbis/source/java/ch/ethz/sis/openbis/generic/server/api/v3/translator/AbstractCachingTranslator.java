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
import java.util.LinkedList;
import java.util.List;
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
    protected O doTranslate(I object)
    {
        Collection<O> translated = doTranslate(Collections.singleton(object));
        if (translated.isEmpty())
        {
            return null;
        } else
        {
            return translated.iterator().next();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<O> doTranslate(Collection<I> inputs)
    {
        List<O> translated = new LinkedList<O>();
        Map<I, O> updated = new HashMap<I, O>();

        for (I input : inputs)
        {
            if (getTranslationCache().hasTranslatedObject(getClass().getName(), input.getId()))
            {
                O output = (O) getTranslationCache().getTranslatedObject(getClass().getName(), input.getId());

                if (output == null)
                {
                    if (operationLog.isDebugEnabled())
                    {
                        operationLog.debug("Found that object was already rejected from translation: " + input.getId());
                    }
                } else
                {
                    if (operationLog.isDebugEnabled())
                    {
                        operationLog.debug("Found in cache: " + output.getClass() + " with id: " + input.getId());
                    }

                    if (getTranslationCache().isFetchedWithOptions(output, getFetchOptions()))
                    {
                        translated.add(output);
                    } else
                    {
                        if (operationLog.isDebugEnabled())
                        {
                            operationLog.debug("Updating from cache: " + output.getClass() + " with id: " + input.getId());
                        }

                        getTranslationCache().setFetchedWithOptions(output, getFetchOptions());
                        updated.put(input, output);
                        translated.add(output);
                    }
                }
            } else
            {
                if (shouldTranslate(input))
                {
                    O output = createObject(input);

                    if (operationLog.isDebugEnabled())
                    {
                        operationLog.debug("Created: " + output.getClass() + " with id: " + input.getId());
                    }

                    getTranslationCache().putTranslatedObject(getClass().getName(), input.getId(), output);
                    getTranslationCache().setFetchedWithOptions(output, getFetchOptions());
                    updated.put(input, output);
                    translated.add(output);

                    if (operationLog.isDebugEnabled())
                    {
                        operationLog.debug("Updating created: " + output.getClass() + " with id: " + input.getId());
                    }
                } else
                {
                    operationLog.debug("Should not translate object: " + input.getClass() + " with id: " + input.getId());
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
        }

        return translated;
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

    protected TranslationContext getTranslationContext()
    {
        return translationContext;
    }

    protected TranslationCache getTranslationCache()
    {
        return getTranslationContext().getTranslationCache();
    }

    protected F getFetchOptions()
    {
        return fetchOptions;
    }

}
