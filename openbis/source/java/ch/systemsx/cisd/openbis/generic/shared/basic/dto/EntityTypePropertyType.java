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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An <i>abstract</i> entity type property type.
 * 
 * @author Christian Ribeaud
 */
public abstract class EntityTypePropertyType<T extends EntityType> implements IsSerializable,
        Comparable<EntityTypePropertyType<T>>, Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private EntityKind entityKind;

    private boolean mandatory;

    private boolean managedInternally;

    private PropertyType propertyType;

    private T entityType;

    public EntityTypePropertyType(EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public final boolean isMandatory()
    {
        return mandatory;
    }

    public final void setMandatory(final boolean mandatory)
    {
        this.mandatory = mandatory;
    }

    public final boolean isManagedInternally()
    {
        return managedInternally;
    }

    public final void setManagedInternally(final boolean managedInternally)
    {
        this.managedInternally = managedInternally;
    }

    public final PropertyType getPropertyType()
    {
        return propertyType;
    }

    public final void setPropertyType(final PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }

    public final T getEntityType()
    {
        return entityType;
    }

    public final void setEntityType(final T entityType)
    {
        this.entityType = entityType;
    }

    //
    // Object
    //

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || obj instanceof EntityTypePropertyType == false)
        {
            return false;
        }
        EntityTypePropertyType<T> that = (EntityTypePropertyType<T>) obj;
        return this.compareTo(that) == 0;
    }

    @Override
    public int hashCode()
    {
        return getPropertyType().hashCode();
    }

    //
    // Comparable
    //

    public final int compareTo(final EntityTypePropertyType<T> o)
    {
        assert o != null : "Unspecified entity type property type.";
        final PropertyType propertyType1 = getPropertyType();
        final PropertyType propertyType2 = o.getPropertyType();
        return propertyType1.compareTo(propertyType2);
    }
}
