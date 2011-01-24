/*
 * Copyright 2011 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiDescription;

/**
 * An {@link IEntityProperty} implementation for managed properties.
 * 
 * @author Piotr Buczek
 */
public class ManagedEntityProperty implements IEntityProperty, IManagedProperty
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private IEntityProperty entityProperty;

    private IManagedProperty managedProperty;

    public ManagedEntityProperty(IEntityProperty entityProperty, IManagedProperty managedProperty)
    {
        this.entityProperty = entityProperty;
        this.managedProperty = managedProperty;
    }

    //
    // IManagedProperty
    //

    public boolean isOwnTab()
    {
        return managedProperty.isOwnTab();
    }

    public void setOwnTab(boolean ownTab)
    {
        managedProperty.setOwnTab(ownTab);
    }

    public IManagedUiDescription getUiDescription()
    {
        return managedProperty.getUiDescription();
    }

    public String getPropertyTypeCode()
    {
        return managedProperty.getPropertyTypeCode();
        // return getPropertyType().getCode();
    }

    public String getRawValue()
    {
        return managedProperty.getRawValue();
    }

    public void setRawValue(String rawValue)
    {
        managedProperty.setRawValue(rawValue);
    }

    //
    // IEntityProperty not delegated methods
    //

    public boolean isManaged()
    {
        return true;
    }

    //
    // IEntityProperty delegated methods
    //

    public String tryGetAsString()
    {
        return entityProperty.tryGetAsString();
    }

    public String tryGetOriginalValue()
    {
        return entityProperty.tryGetOriginalValue();
    }

    public Material getMaterial()
    {
        return entityProperty.getMaterial();
    }

    public void setMaterial(Material material)
    {
        entityProperty.setMaterial(material);
    }

    public VocabularyTerm getVocabularyTerm()
    {
        return entityProperty.getVocabularyTerm();
    }

    public void setVocabularyTerm(VocabularyTerm vocabularyTerm)
    {
        entityProperty.setVocabularyTerm(vocabularyTerm);
    }

    public String getValue()
    {
        return entityProperty.getValue();
    }

    public void setValue(String value)
    {
        entityProperty.setValue(value);
    }

    public PropertyType getPropertyType()
    {
        return entityProperty.getPropertyType();
    }

    public void setPropertyType(PropertyType propertyType)
    {
        entityProperty.setPropertyType(propertyType);
    }

    public void setOrdinal(Long ordinal)
    {
        entityProperty.setOrdinal(ordinal);
    }

    public Long getOrdinal()
    {
        return entityProperty.getOrdinal();
    }

    public int compareTo(IEntityProperty o)
    {
        return entityProperty.compareTo(o);
    }

    //
    // For serialization
    //

    public ManagedEntityProperty()
    {
    }

    // @SuppressWarnings("unused")
    // private IEntityProperty getEntityProperty()
    // {
    // return entityProperty;
    // }
    //
    // @SuppressWarnings("unused")
    // private void setEntityProperty(IEntityProperty entityProperty)
    // {
    // this.entityProperty = entityProperty;
    // }

    // public IEntityProperty asEntityProperty()
    // {
    // return this;
    // }

}
