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

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;

/**
 * An <i>abstract</i> {@link IViewContext} implementation which should be extended by each plugin
 * based technology.
 * 
 * @author Christian Ribeaud
 */
public abstract class AbstractPluginViewContext<T extends IClientServiceAsync> implements
        IViewContext<T>
{
    private final IViewContext<ICommonClientServiceAsync> commonViewContext;

    public AbstractPluginViewContext(final IViewContext<ICommonClientServiceAsync> commonViewContext)
    {
        this.commonViewContext = commonViewContext;
    }

    //
    // IViewContext
    //

    public final IViewContext<ICommonClientServiceAsync> getCommonViewContext()
    {
        return commonViewContext;
    }

    public final GenericViewModel getModel()
    {
        return commonViewContext.getModel();
    }

    public final IPageController getPageController()
    {
        return commonViewContext.getPageController();
    }

    public final IClientPluginFactoryProvider getClientPluginFactoryProvider()
    {
        return commonViewContext.getClientPluginFactoryProvider();
    }

    public final IGenericImageBundle getImageBundle()
    {
        return commonViewContext.getImageBundle();
    }

}
