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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.ClientPluginFactory;

/**
 * The provider of {@link IClientPluginFactory} implementations.
 * 
 * @author Christian Ribeaud
 */
public final class DefaultClientPluginFactoryProvider implements IClientPluginFactoryProvider
{
    private final Map<EntityKind, IClientPluginFactory> pluginFactoryByEntityKind =
            new HashMap<EntityKind, IClientPluginFactory>();

    private IClientPluginFactory genericPluginFactory;

    public DefaultClientPluginFactoryProvider(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        genericPluginFactory = new ClientPluginFactory(originalViewContext);
        registerPluginFactories(originalViewContext);
    }

    private final void registerPluginFactories(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        // Automatically generated part - START
        registerPluginFactory(new ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory(
                originalViewContext));
        // Automatically generated part - END
    }

    private final void registerPluginFactory(final IClientPluginFactory pluginFactory)
    {
        assert pluginFactory != null : "Unspecified client plugin factory.";
        for (final EntityKind entityKind : EntityKind.values())
        {
            final IClientPluginFactory clientPluginFactory =
                    pluginFactoryByEntityKind.get(entityKind);
            if (clientPluginFactory != null)
            {
                final Set<String> set =
                        new HashSet<String>(clientPluginFactory.getEntityTypeCodes(entityKind));
                set.retainAll(pluginFactory.getEntityTypeCodes(entityKind));
                if (set.size() > 0)
                {
                    throw new IllegalArgumentException("There is already a plugin factory ("
                            + clientPluginFactory.getClass().getName()
                            + ") registered for entity type code(s) '" + set + "'.");
                }
            }
            pluginFactoryByEntityKind.put(entityKind, pluginFactory);
        }
    }

    //
    // IClientPluginFactoryProvider
    //

    public final <T extends EntityType> IClientPluginFactory getClientPluginFactory(
            final EntityKind entityKind, final EntityType entityType)
    {
        assert entityKind != null : "Unspecified entity kind.";
        assert entityType != null : "Unspecified entity type.";

        final IClientPluginFactory pluginFactory = pluginFactoryByEntityKind.get(entityKind);
        final Set<String> entityTypeCodes = pluginFactory.getEntityTypeCodes(entityKind);
        if (entityTypeCodes.contains(entityType.getCode()))
        {
            return pluginFactory;
        }
        return genericPluginFactory;
    }
}
