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

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.DelayedTask;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IUpdater;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.CompositeMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DictonaryBasedMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * The <i>generic</i> {@link IViewContext} implementation.
 * 
 * @author Franz-Josef Elmer
 */
public final class CommonViewContext implements IViewContext<ICommonClientServiceAsync>
{
    private static final String TECHNOLOGY_NAME = "common";

    private final ICommonClientServiceAsync service;

    private final IGenericImageBundle imageBundle;

    private final GenericViewModel viewModel;

    private final IPageController pageController;

    private DisplaySettingsManager displaySettingsManager;

    private IClientPluginFactoryProvider clientPluginFactoryProvider;

    private IMessageProvider messageProvider;

    CommonViewContext(final ICommonClientServiceAsync service,
            final IMessageProvider messageProvider, final IGenericImageBundle imageBundle,
            final IPageController pageController)
    {
        this.service = service;
        this.messageProvider = new DictonaryBasedMessageProvider(TECHNOLOGY_NAME);
        this.imageBundle = imageBundle;
        this.pageController = pageController;
        viewModel = new GenericViewModel();
    }

    final void setClientPluginFactoryProvider(
            IClientPluginFactoryProvider clientPluginFactoryProvider)
    {
        this.clientPluginFactoryProvider = clientPluginFactoryProvider;
    }

    //
    // IViewContext
    //

    public final ICommonClientServiceAsync getService()
    {
        return service;
    }

    public final GenericViewModel getModel()
    {
        return viewModel;
    }

    public DisplaySettingsManager getDisplaySettingsManager()
    {
        if (displaySettingsManager == null)
        {
            displaySettingsManager = createDisplaySettingsManager();
        }
        return displaySettingsManager;
    }

    private DisplaySettingsManager createDisplaySettingsManager()
    {
        final DisplaySettings displaySettings = viewModel.getSessionContext().getDisplaySettings();
        Listener<BaseEvent> listener = new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent event)
                {
                    AbstractAsyncCallback<Void> callback =
                            new AbstractAsyncCallback<Void>(CommonViewContext.this)
                                {
                                    @Override
                                    public final void process(final Void result)
                                    {
                                    }
                                };
                    service.updateDisplaySettings(displaySettings, callback);
                }
            };
        final DelayedTask updateDisplaySettingsTask = new DelayedTask(listener);

        return new DisplaySettingsManager(displaySettings, new IUpdater()
            {
                public void update()
                {
                    updateDisplaySettingsTask.delay(10000);
                }
            });
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

    /** @see IMessageProvider#containsKey(String) */
    public boolean containsKey(String key)
    {
        return messageProvider.containsKey(key);
    }

    /** @see IMessageProvider#getMessage(String, Object...) */
    public String getMessage(String key, Object... parameters)
    {
        return messageProvider.getMessage(key, parameters);
    }

    /** @see IMessageProvider#getName() */
    public String getName()
    {
        return messageProvider.getName();
    }

    public ICommonClientServiceAsync getCommonService()
    {
        return getService();
    }

    public void addMessageSource(String messageSource)
    {
        messageProvider =
                new CompositeMessageProvider(new DictonaryBasedMessageProvider(messageSource),
                        messageProvider);
    }
}
