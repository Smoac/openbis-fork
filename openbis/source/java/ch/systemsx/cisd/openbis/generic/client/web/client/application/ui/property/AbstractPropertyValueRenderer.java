/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * A default <code>PropertyValueRenderer</code> implementation which catches <code>null</code>
 * value by rendering <code>-</code>.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractPropertyValueRenderer<T> implements IPropertyValueRenderer<T>
{
    private final IMessageProvider messageProvider;

    /** The table <code>null</code> value representation. */
    public static final String TABLE_NULL_VALUE = "-";

    public AbstractPropertyValueRenderer(final IMessageProvider messageProvider)
    {
        this.messageProvider = messageProvider;
    }

    protected final IMessageProvider getMessageProvider()
    {
        return messageProvider;
    }

    /** Here we are sure to get a non-<code>null</code> value. */
    protected abstract String renderNotNull(final T value);

    //
    // PropertyValueRenderer
    //

    public final String render(final T object)
    {
        if (object == null)
        {
            return TABLE_NULL_VALUE;
        }
        return renderNotNull(object);
    }
}