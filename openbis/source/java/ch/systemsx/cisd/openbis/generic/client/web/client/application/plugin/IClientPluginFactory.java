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

import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.IEditableEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;

/**
 * Client plugin factory.
 * 
 * @author Christian Ribeaud
 */
public interface IClientPluginFactory
{
    /**
     * Returns all the entity type codes for given <var>entityKind</var> supported by this
     * implementation.
     */
    public Set<String> getEntityTypeCodes(final EntityKind entityKind);

    /**
     * Creates and returns a {@link IClientPlugin} implementation specific to given <var>entityKind</var>
     * and given <var>entityType</var> each time this method is called.
     */
    public <T extends EntityType, S extends EntityTypePropertyType<T>, P extends EntityProperty<T, S>, I extends IIdentifierHolder, V extends IEditableEntity<T, S, P>> IClientPlugin<T, S, P, I, V> createClientPlugin(
            final EntityKind entityKind);
}