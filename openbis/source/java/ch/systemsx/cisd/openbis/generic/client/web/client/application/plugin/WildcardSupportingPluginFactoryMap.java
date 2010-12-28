/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.ArrayList;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKindAndTypeCode;

/**
 * Utility class that manages mappings from entity types/codes (possibly including wildcards) to
 * IClientPluginFactory objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class WildcardSupportingPluginFactoryMap
{
    /**
     * Internal class for keeping mappings from entityKind/codes (which may include wildcards) to
     * factories.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class PluginFactoryMapping
    {
        private final EntityKindAndTypeCode entityKindAndCode;

        private final IClientPluginFactory factory;

        private PluginFactoryMapping(EntityKindAndTypeCode entityKindAndCode,
                IClientPluginFactory factory)
        {
            this.entityKindAndCode = entityKindAndCode;
            this.factory = factory;
        }

        public IClientPluginFactory getFactory()
        {
            return factory;
        }

        boolean matchesEntityKindAndTypeCode(EntityKindAndTypeCode otherEntityKindAndTypeCode)
        {
            if (false == entityKindAndCode.entityKindsMatch(otherEntityKindAndTypeCode))
            {
                return false;
            }

            return otherEntityKindAndTypeCode.getEntityTypeCode().matches(
                    entityKindAndCode.getEntityTypeCode());
        }
    }

    private final ArrayList<PluginFactoryMapping> mappings = new ArrayList<PluginFactoryMapping>();

    /**
     * Add a mapping from the entity kind/code to a factory.
     */
    public void addMapping(EntityKindAndTypeCode entityKindAndCode, IClientPluginFactory factory)
    {
        mappings.add(new PluginFactoryMapping(entityKindAndCode, factory));
    }

    /**
     * Return the first factory that matches the given entityKindAndCode, or null if none is found.
     */
    public IClientPluginFactory tryPluginFactory(EntityKindAndTypeCode entityKindAndCode)
    {
        for (PluginFactoryMapping mapping : mappings)
        {
            if (mapping.matchesEntityKindAndTypeCode(entityKindAndCode))
            {
                return mapping.getFactory();
            }
        }

        return null;
    }
}
