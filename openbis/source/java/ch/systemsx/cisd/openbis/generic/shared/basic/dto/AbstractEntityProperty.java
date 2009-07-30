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
        switch (propertyType.getDataType().getCode())
        {
            case CONTROLLEDVOCABULARY:
                return (getVocabularyTerm() != null) ? getVocabularyTerm().getCode() : getValue();
            case MATERIAL:
                return (getMaterial() != null) ? MaterialIdentifier.print(getMaterial().getCode(),
                        getMaterial().getMaterialType().getCode()) : getValue();
            default:
                return getValue();
        }
    }

    public String getValue()
    {
        return null;
    }

    public void setValue(String value)
    {
        throw new UnsupportedOperationException();
    }

    public Material getMaterial()
    {
        return null;
    }

    public void setMaterial(Material material)
    {
        throw new UnsupportedOperationException();
    }

    public VocabularyTerm getVocabularyTerm()
    {
        return null;
    }

    public void setVocabularyTerm(VocabularyTerm vocabularyTerm)
    {
        throw new UnsupportedOperationException();
    }

    // 
    // Comparable
    // 

    public int compareTo(IEntityProperty o)
    {
        return this.getPropertyType().getLabel().compareTo(o.getPropertyType().getLabel());
    }

}
