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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocatorResolverRegistry;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * The view context interface.
 * 
 * @author Christian Ribeaud
 */
public interface IViewContext<T extends IClientServiceAsync> extends IMessageProvider
{
    public void addMessageSource(String messageSource);

    public IViewContext<ICommonClientServiceAsync> getCommonViewContext();

    public T getService();

    public GenericViewModel getModel();

    public void initDisplaySettingsManager();

    public DisplaySettingsManager getDisplaySettingsManager();

    public IGenericImageBundle getImageBundle();

    public IPageController getPageController();

    public IClientPluginFactoryProvider getClientPluginFactoryProvider();

    public ICommonClientServiceAsync getCommonService();

    public ViewLocatorResolverRegistry getLocatorResolverRegistry();
}
