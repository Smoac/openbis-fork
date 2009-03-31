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

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.IEditableEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;

/**
 * The client plugin.
 * <p>
 * It specifies widgets for following operations:
 * <ul>
 * <li>Detailed view of a given entity identifier.</li>
 * <li>Registration of an entity of a given type.</li>
 * <li>Batch registration of an entity of a given type.</li>
 * </p>
 * 
 * @author Christian Ribeaud
 */
public interface IClientPlugin<T extends EntityType, S extends EntityTypePropertyType<T>, P extends EntityProperty<T, S>, I extends IIdentifierHolder, V extends IEditableEntity<T, S, P>>
{
    /**
     * Shows a detailed view of the entity specified by its <var>identifier</var>.
     */
    public ITabItemFactory createEntityViewer(final I identifier);

    /**
     * Shows a registration form for entities of given <var>entityType</var>.
     */
    public DatabaseModificationAwareWidget createRegistrationForEntityType(final T entityType);

    /**
     * Shows a batch registration form for entities of given <var>entityType</var>.
     */
    public Widget createBatchRegistrationForEntityType(final T entityType);

    /**
     * Shows a editor of the specified entity..
     */
    public ITabItemFactory createEntityEditor(final V editableEntity);
}
