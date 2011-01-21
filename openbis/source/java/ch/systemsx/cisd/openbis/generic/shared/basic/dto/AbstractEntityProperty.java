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

/**
 * The abstract base implementation of {@link IEntityProperty}, only featuring a
 * {@link PropertyType}.
 * <p>
 * All getters (except {@link #getPropertyType()} will return <code>null</code>, all setters (except
 * {@link #setPropertyType(PropertyType)} will throw an {@link UnsupportedOperationException}.
 * 
 * @author Bernd Rinn
 */
public abstract class AbstractEntityProperty implements IEntityProperty
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private PropertyType propertyType;

    private Long ordinal;

    public PropertyType getPropertyType()
    {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }

    public String tryGetAsString()
    {
        if (propertyType == null)
        {
            return null;
        }
        DataType dataType = propertyType.getDataType();
        if (dataType == null)
        {
            return getValue();
        }
        switch (dataType.getCode())
        {
            case CONTROLLEDVOCABULARY:
                VocabularyTerm vocabularyTerm = getVocabularyTerm();
                return (vocabularyTerm != null) ? vocabularyTerm.getCode() : getValue();
            case MATERIAL:
                Material material = getMaterial();
                return (material != null) ? MaterialIdentifier.print(material.getCode(), material
                        .getMaterialType().getCode()) : getValue();
            default:
                return getValue();
        }
    }

    public String tryGetOriginalValue()
    {
        return tryGetAsString();
    }

    public String getValue()
    {
        return null;
    }

    public void setValue(String value)
    {
    }

    public Material getMaterial()
    {
        return null;
    }

    public void setMaterial(Material material)
    {
    }

    public VocabularyTerm getVocabularyTerm()
    {
        return null;
    }

    public void setVocabularyTerm(VocabularyTerm vocabularyTerm)
    {
    }

    public void setOrdinal(Long ordinal)
    {
        this.ordinal = ordinal;
    }

    public Long getOrdinal()
    {
        return ordinal;
    }

    public boolean isManaged()
    {
        return false;
    }

    //
    // Object
    //

    @Override
    public String toString()
    {
        return propertyType + ": " + tryGetAsString();
    }

    //
    // Comparable
    //

    public int compareTo(IEntityProperty o)
    {
        Long thisOrdinal = this.getOrdinal();
        Long otherOrdinal = o.getOrdinal();
        if (thisOrdinal != null && otherOrdinal != null)
        {
            return thisOrdinal.compareTo(otherOrdinal);
        }
        PropertyType thisPropertyType = this.getPropertyType();
        PropertyType otherPropertyType = o.getPropertyType();
        if (thisPropertyType.getLabel().equals(otherPropertyType.getLabel()))
        {
            return thisPropertyType.getCode().compareTo(otherPropertyType.getCode());
        } else
        {
            return thisPropertyType.getLabel().compareTo(otherPropertyType.getLabel());
        }
    }

}
