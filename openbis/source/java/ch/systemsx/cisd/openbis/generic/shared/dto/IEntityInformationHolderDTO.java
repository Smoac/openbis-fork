/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Entity identifier, type and kind.
 * 
 * @author Izabela Adamczyk
 */
public interface IEntityInformationHolderDTO extends IIdentifiable
{
    /**
     * Returns the entity type of this matching entity.
     */
    public EntityTypePE getEntityType();

    /**
     * Return the entity kind of this matching entity.
     */
    public EntityKind getEntityKind();
}
