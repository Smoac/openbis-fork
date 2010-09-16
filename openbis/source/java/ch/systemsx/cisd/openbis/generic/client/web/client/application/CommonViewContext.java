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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorResolverRegistry;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.CompositeMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DictonaryBasedMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.log.IProfilingTable;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.log.ProfilingTable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;

/**
 * The <i>generic</i> {@link IViewContext} implementation.
 * 
 * @author Franz-Josef Elmer
 */
public final class CommonViewContext implements IViewContext<ICommonClientServiceAsync>
{

    /**
     * Holds static state of client that should be accessible from everywhere on the client.
     * 
     * @author Piotr Buczek
     */
    public final static class ClientStaticState
    {
        private static boolean simpleMode;

        public static void init(final boolean isSimpleMode)
        {
            simpleMode = isSimpleMode;
        }

        public static boolean isSimpleMode()
        {
            return simpleMode;
        }
    }

    private static final String TECHNOLOGY_NAME = "common";

    private final ICommonClientServiceAsync service;

    private final IGenericImageBundle imageBundle;

    private final GenericViewModel viewModel;

    private final IPageController pageController;

    private DisplaySettingsManager displaySettingsManager;

    private IClientPluginFactoryProvider clientPluginFactoryProvider;

    private final CompositeMessageProvider messageProvider;

    private final ViewLocatorResolverRegistry locatorHandlerRegistry;

    private final IProfilingTable profilingTable;

    private final String basicPageTitle;

    CommonViewContext(final ICommonClientServiceAsync service,
            final IGenericImageBundle imageBundle, final IPageController pageController,
            boolean isLoggingEnabled, boolean isSimpleMode, String basicPageTitle)
    {
        this.service = service;
        this.imageBundle = imageBundle;
        this.pageController = pageController;
        this.profilingTable = ProfilingTable.create(isLoggingEnabled);
        this.basicPageTitle = basicPageTitle;
        messageProvider = new CompositeMessageProvider();
        messageProvider.add(new DictonaryBasedMessageProvider(TECHNOLOGY_NAME));
        viewModel = new GenericViewModel();
        locatorHandlerRegistry = new ViewLocatorResolverRegistry();
        ClientStaticState.init(isSimpleMode);
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

    public void initDisplaySettingsManager()
    {
        final DisplaySettings loggedUserDisplaySettings =
                viewModel.getSessionContext().getDisplaySettings();
        displaySettingsManager =
                createDisplaySettingsManager(loggedUserDisplaySettings, viewModel
                        .getApplicationInfo().getWebClientConfiguration());
    }

    public DisplaySettingsManager getDisplaySettingsManager()
    {
        assert displaySettingsManager != null : "displaySettingsManager not initialized";
        return displaySettingsManager;
    }

    private DisplaySettingsManager createDisplaySettingsManager(
            final DisplaySettings displaySettings,
            WebClientConfiguration webClientConfigurationDTO)
    {
        IDelegatedAction settingsUpdater = new IDelegatedAction()
            {
                public void execute()
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
        return new DisplaySettingsManager(displaySettings, settingsUpdater,
                webClientConfigurationDTO);
    }

    public final IGenericImageBundle getImageBundle()
    {
        return imageBundle;
    }

    public final IPageController getPageController()
    {
        return pageController;
    }

    public final String getBasicPageTitle()
    {
        return basicPageTitle;
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
        messageProvider.add(new DictonaryBasedMessageProvider(messageSource));
    }

    public ViewLocatorResolverRegistry getLocatorResolverRegistry()
    {
        return locatorHandlerRegistry;
    }

    // ----- delegation to profilingTable

    public final void clearLog()
    {
        profilingTable.clearLog();
    }

    public final List<String> getLoggedEvents()
    {
        return profilingTable.getLoggedEvents();
    }

    public final int log(String description)
    {
        return profilingTable.log(description);
    }

    public void log(int taskId, String description)
    {
        profilingTable.log(taskId, description);
    }

    public final void logStop(int taskId)
    {
        profilingTable.logStop(taskId);
    }

    public boolean isLoggingEnabled()
    {
        return profilingTable.isLoggingEnabled();
    }

    public boolean isSimpleMode()
    {
        return ClientStaticState.isSimpleMode();
    }

}
