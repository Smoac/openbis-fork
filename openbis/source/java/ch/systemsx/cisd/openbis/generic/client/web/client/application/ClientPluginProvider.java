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

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ClientPluginFactory;

/**
 * The provider of {@link IClientPluginFactory} implementations.
 * 
 * @author Christian Ribeaud
 */
public final class ClientPluginProvider
{
    private final static Set<IClientPluginFactory> plugins = new HashSet<IClientPluginFactory>();

    private static IClientPluginFactory genericPluginFactory;

    private static IViewContext<IGenericClientServiceAsync> originalViewContext;

    private ClientPluginProvider()
    {
        // Can not be instantiated.
    }

    /**
     * Sets the original view context created on {@link Client}.
     */
    public static final void setOriginalViewContext(
            final IViewContext<IGenericClientServiceAsync> originalViewContext)
    {
        ClientPluginProvider.originalViewContext = originalViewContext;
        genericPluginFactory = new ClientPluginFactory(originalViewContext);
        registerPluginFactories();
    }

    private final static void registerPluginFactories()
    {
        // Automatically generated part - START
        registerPluginFactory(new ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory(
                originalViewContext));
        // Automatically generated part - END
    }

    private final static void registerPluginFactory(final IClientPluginFactory pluginFactory)
    {
        for (final IClientPluginFactory plugin : plugins)
        {
            final Set<String> set = new HashSet<String>(plugin.getSampleTypeCodes());
            set.retainAll(pluginFactory.getSampleTypeCodes());
            // TODO 2008-10-22, Christian Ribeaud: Uncomment once we understood why openBIS Smoke
            // Tests are broken in Eclipse environment.
            // if (set.size() > 0)
            // {
            // throw new IllegalArgumentException(
            // "There is already a plugin factory registered for sample type code(s) '"
            // + set + "'.");
            // }
        }
        plugins.add(pluginFactory);
    }

    /**
     * For given sample type code return corresponding {@link IClientPluginFactory}.
     * 
     * @return never <code>null</code> but could return the <i>generic</i> implementation.
     */
    public final static IClientPluginFactory getPluginFactory(final String sampleTypeCode)
    {
        assert sampleTypeCode != null : "Unspecified sample type code";
        for (final IClientPluginFactory pluginFactory : plugins)
        {
            final Set<String> sampleTypeCodes = pluginFactory.getSampleTypeCodes();
            if (sampleTypeCodes.contains(sampleTypeCode))
            {
                return pluginFactory;
            }
        }
        return genericPluginFactory;
    }
}
