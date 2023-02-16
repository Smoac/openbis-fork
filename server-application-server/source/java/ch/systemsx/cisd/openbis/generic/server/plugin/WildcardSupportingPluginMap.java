/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.server.plugin;

import ch.systemsx.cisd.openbis.generic.shared.basic.WildcardSupportingMap;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKindAndTypeCode;

/**
 * Utility class that manages mappings from entity types/codes (possibly including wildcards) to IServerPlugin objects.
 * 
 * @author Chandrasekhar Ramakrishnan
 */

public class WildcardSupportingPluginMap<T extends IServerPlugin> extends WildcardSupportingMap<T>
{

    /**
     * Add a mapping from the entity kind/code to a factory.
     */
    public void addMapping(EntityKindAndTypeCode entityKindAndCode, T plugin)
    {
        addThingMapping(entityKindAndCode, plugin);
    }

    /**
     * Return the first factory that matches the given entityKindAndCode, or null if none is found.
     */
    public T tryPlugin(EntityKindAndTypeCode entityKindAndCode)
    {
        return tryThing(entityKindAndCode);
    }
}
