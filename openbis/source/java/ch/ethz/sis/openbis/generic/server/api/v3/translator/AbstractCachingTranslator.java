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
    @SuppressWarnings("unchecked")
    protected final O doTranslate(I input)
    {
        if (getTranslationCache().hasTranslatedObject(getClass().getName(), input.getId()))
        {
            O output = (O) getTranslationCache().getTranslatedObject(getClass().getName(), input.getId());

            if (output == null)
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog
                            .debug("Found that object was already rejected from translation: " + input.getId());
                }
                return null;
            }

            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Found in cache: " + output.getClass() + " with id: " + input.getId());
            }

            if (false == getTranslationCache().isFetchedWithOptions(output, getFetchOptions()))
            {
                getTranslationCache().setFetchedWithOptions(output, getFetchOptions());

                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("Updating from cache: " + output.getClass() + " with id: " + input.getId());
                }

                updateObject(input, output);
            }

            return output;
        } else
        {
            if (false == shouldTranslate(input))
            {
                operationLog.debug("Should not translate object: " + input.getClass() + " with id: " + input.getId());
                return null;
            }

            O output = createObject(input);

            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Created: " + output.getClass() + " with id: " + input.getId());
            }

            getTranslationCache().putTranslatedObject(getClass().getName(), input.getId(), output);
            getTranslationCache().setFetchedWithOptions(output, getFetchOptions());

            updateObject(input, output);

            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Updating created: " + output.getClass() + " with id: " + input.getId());
            }

            return output;
        }
    }

    /**
     * Override this method if you want to conditionally skip translation
     */
    public boolean shouldTranslate(I object)
    {
        return true;
    }

    protected abstract O createObject(I input);

    protected abstract void updateObject(I input, O output);

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
