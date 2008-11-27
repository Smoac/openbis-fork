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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.CompositeMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DictonaryBasedMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * The <i>generic</i> {@link IViewContext} implementation.
 * 
 * @author Franz-Josef Elmer
 */
public final class CommonViewContext extends AbstractViewContext<ICommonClientServiceAsync>
{
    // We use here 'generic' because we did not split the javascript dictionary yet.
    private static final String TECHNOLOGY_NAME = "generic";

    private final ICommonClientServiceAsync service;

    private final IMessageProvider messageProvider;

    private final IGenericImageBundle imageBundle;

    private final GenericViewModel viewModel;

    private final IPageController pageController;

    private final IClientPluginFactoryProvider clientPluginFactoryProvider;

    CommonViewContext(final ICommonClientServiceAsync service,
            final IMessageProvider messageProvider, final IGenericImageBundle imageBundle,
            final IPageController pageController)
    {
        this.service = service;
        this.messageProvider =
                new CompositeMessageProvider(new DictonaryBasedMessageProvider(TECHNOLOGY_NAME),
                        messageProvider);
        this.imageBundle = imageBundle;
        this.pageController = pageController;
        viewModel = new GenericViewModel();
        clientPluginFactoryProvider = new DefaultClientPluginFactoryProvider(this);
    }

    //
    // IViewContext
    //

    public final IMessageProvider getMessageProvider()
    {
        return messageProvider;
    }

    public final ICommonClientServiceAsync getService()
    {
        return service;
    }

    public final GenericViewModel getModel()
    {
        return viewModel;
    }

    public final IGenericImageBundle getImageBundle()
    {
        return imageBundle;
    }

    public final IPageController getPageController()
    {
        return pageController;
    }

    public final IClientPluginFactoryProvider getClientPluginFactoryProvider()
    {
        return clientPluginFactoryProvider;
    }

    public final IViewContext<ICommonClientServiceAsync> getCommonViewContext()
    {
        return this;
    }
}
