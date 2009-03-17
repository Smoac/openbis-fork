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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.util.Date;
import java.util.List;

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

    protected String identifier;

    private final Long id;

    private final Date modificationDate;

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

    public void setProperties(List<P> newProperties)
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
}
