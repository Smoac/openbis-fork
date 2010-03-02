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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorResolverRegistry;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

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

    private final T service;

    public AbstractPluginViewContext(final IViewContext<ICommonClientServiceAsync> commonViewContext)
    {
        this.commonViewContext = commonViewContext;
        addMessageSource(getTechnology());
        service = createClientServiceAsync();
        final ServiceDefTarget endpoint = (ServiceDefTarget) service;
        endpoint.setServiceEntryPoint(GenericConstants.createServicePath(getTechnology()));
        initializeLocatorHandlerRegistry(commonViewContext.getLocatorResolverRegistry());
    }

    /**
     * Returns the name of the technology.
     */
    protected abstract String getTechnology();

    /**
     * Creates the service. Implementations will usually invoke {@link GWT#create(Class)} with the
     * corresponding synchronous service interface.
     */
    protected abstract T createClientServiceAsync();

    //
    // IViewContext
    //

    public final T getService()
    {
        return service;
    }

    public final IViewContext<ICommonClientServiceAsync> getCommonViewContext()
    {
        return commonViewContext;
    }

    public final ICommonClientServiceAsync getCommonService()
    {
        return commonViewContext.getService();
    }

    public final GenericViewModel getModel()
    {
        return commonViewContext.getModel();
    }

    public void initDisplaySettingsManager()
    {
        commonViewContext.initDisplaySettingsManager();
    }

    public DisplaySettingsManager getDisplaySettingsManager()
    {
        return commonViewContext.getDisplaySettingsManager();
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

    /** @see IMessageProvider#containsKey(String) */
    public boolean containsKey(String key)
    {
        return commonViewContext.containsKey(key);
    }

    /** @see IMessageProvider#getMessage(String, Object...) */
    public String getMessage(String key, Object... parameters)
    {
        return commonViewContext.getMessage(key, parameters);
    }

    /** @see IMessageProvider#getName() */
    public String getName()
    {
        return commonViewContext.getName();
    }

    public void addMessageSource(String messageSource)
    {
        commonViewContext.addMessageSource(messageSource);
    }

    public ViewLocatorResolverRegistry getLocatorResolverRegistry()
    {
        // Delegate to the common view context
        return commonViewContext.getLocatorResolverRegistry();
    }

    /**
     * Register any handlers for locators specified in the openBIS URL.
     */
    protected void initializeLocatorHandlerRegistry(ViewLocatorResolverRegistry handlerRegistry)
    {

    }

}
