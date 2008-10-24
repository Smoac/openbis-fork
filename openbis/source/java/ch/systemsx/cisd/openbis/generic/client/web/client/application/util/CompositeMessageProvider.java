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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import java.util.MissingResourceException;

/**
 * A {@link IMessageProvider} implementation based on <i>Composite</i> pattern.
 * 
 * @author Christian Ribeaud
 */
public final class CompositeMessageProvider implements IMessageProvider
{
    private final AbstractDictionaryBasedMessageProvider[] messageProviders;

    public CompositeMessageProvider(final AbstractDictionaryBasedMessageProvider... messageProviders)
    {
        assert messageProviders != null : "Unspecified message providers.";
        assert messageProviders.length > 0 : "No message provider has been specified.";
        this.messageProviders = messageProviders;
    }

    //
    // IMessageProvider
    //

    public final String getMessage(final String key, final Object... parameters)
    {
        for (final AbstractDictionaryBasedMessageProvider messageProvider : messageProviders)
        {
            if (messageProvider.containsKey(key))
            {
                return messageProvider.getMessage(key, parameters);
            }
        }
        throw new MissingResourceException("Can not find key '" + key + "' in any dictionary.",
                null, key);
    }
}
