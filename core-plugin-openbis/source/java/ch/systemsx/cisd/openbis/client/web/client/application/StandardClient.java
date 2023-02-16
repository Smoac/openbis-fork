/*
 * Copyright ETH 2009 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.client.web.client.application;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Client;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.DefaultClientPluginFactoryProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactoryProvider;

/**
 * Client for standard set of technologies.
 * 
 * @author Franz-Josef Elmer
 */
public class StandardClient extends Client
{
    private static final class ClientPluginFactoryProvider extends
            DefaultClientPluginFactoryProvider
    {
        ClientPluginFactoryProvider(IViewContext<ICommonClientServiceAsync> originalViewContext)
        {
            super(originalViewContext);
            registerPluginFactory(new ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory(
                    originalViewContext));
        }

    }

    @Override
    protected IClientPluginFactoryProvider createPluginFactoryProvider(
            final IViewContext<ICommonClientServiceAsync> commonContext)
    {
        return new ClientPluginFactoryProvider(commonContext);
    }

}
