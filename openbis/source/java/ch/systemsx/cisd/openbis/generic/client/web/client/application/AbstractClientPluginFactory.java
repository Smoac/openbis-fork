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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;

/**
 * An <i>abstract</i> {@link IClientPluginFactory} implementation.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractClientPluginFactory<T> implements IClientPluginFactory
{
    private final IViewContext<T> viewContext;

    protected AbstractClientPluginFactory(
            final IViewContext<IGenericClientServiceAsync> originalViewContext)
    {
        this.viewContext = createViewContext(originalViewContext);
    }

    protected abstract IViewContext<T> createViewContext(
            IViewContext<IGenericClientServiceAsync> originalViewContext);

    public final IViewContext<T> getViewContext()
    {
        return viewContext;
    }

}
