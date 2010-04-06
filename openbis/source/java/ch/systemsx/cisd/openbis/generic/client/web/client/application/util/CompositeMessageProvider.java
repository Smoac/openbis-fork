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

import java.util.ArrayList;
import java.util.List;


/**
 * A {@link IMessageProvider} implementation based on <i>Composite</i> pattern.
 * 
 * @author Christian Ribeaud
 */
public final class CompositeMessageProvider implements IMessageProvider
{
    private final List<IMessageProvider> messageProviders = new ArrayList<IMessageProvider>();
    private String name;

    public void add(IMessageProvider messageProvider)
    {
        messageProviders.add(messageProvider);
    }

    //
    // IMessageProvider
    //

    public String getName()
    {
        if (name == null)
        {
            StringBuffer buffer = new StringBuffer();
            for (IMessageProvider messageProvider : messageProviders)
            {
                if (buffer.length() > 0)
                {
                    buffer.append(", ");
                }
                buffer.append(messageProvider.getName());
            }
            name = "[" + buffer.toString() + "]";
        }
        return name;
    }
    
    public final boolean containsKey(final String key)
    {
        for (final IMessageProvider messageProvider : messageProviders)
        {
            if (messageProvider.containsKey(key))
            {
                return true;
            }
        }
        return false;
    }

    public final String getMessage(final String key, final Object... parameters)
    {
        for (final IMessageProvider messageProvider : messageProviders)
        {
            if (messageProvider.containsKey(key))
            {
                return messageProvider.getMessage(key, parameters);
            }
        }
        return "Unknown key '" + key + "' in dictonaries " + getName() + ".";
    }

}
