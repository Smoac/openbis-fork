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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.Date;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEditableEntity;

/**
 * @author Izabela Adamczyk
 */
public class EditableEntity<T extends EntityType, S extends EntityTypePropertyType<T>, P extends EntityProperty<T, S>>
        implements IEditableEntity<T, S, P>
{

    private final EntityKind kind;

    private final List<S> etpts;

    private final List<P> properties;

    private final T type;

    private final String identifier;

    private Long id;

    private Date modificationDate;

    public EditableEntity(EntityKind kind, List<S> etpts, List<P> properties, T type,
            String identifier, Long id, Date modificationDate)
    {
        this.kind = kind;
        this.etpts = etpts;
        this.properties = properties;
        this.type = type;
        this.identifier = identifier;
        this.id = id;
        this.modificationDate = modificationDate;
    }

    public EntityKind getEntityKind()
    {
        return kind;
    }

    public List<S> getEntityTypePropertyTypes()
    {
        return etpts;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public List<P> getProperties()
    {
        return properties;
    }

    public T getType()
    {
        return type;
    }

    public void updateProperties(List<P> newProperties)
    {
        this.properties.clear();
        this.properties.addAll(newProperties);
    }

    public Long getId()
    {
        return id;
    }

    public Date getModificationDate()
    {
        return modificationDate;
    }

    public void setModificationDate(Date modificationDate)
    {
        this.modificationDate = modificationDate;
    }

}
