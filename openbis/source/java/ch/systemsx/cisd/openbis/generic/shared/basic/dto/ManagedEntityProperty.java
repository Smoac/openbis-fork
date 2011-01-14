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

/**
 * An {@link IEntityProperty} implementation for managed properties.
 * 
 * @author Piotr Buczek
 */
public class ManagedEntityProperty implements IEntityProperty, IManagedEntityProperty
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private IEntityProperty delegatedProperty;

    // NOTE: defaults are set for testing - scripts should override them

    private boolean ownTab = true;

    private ManagedUiDescription uiDescription = new ManagedUiDescription();

    public ManagedEntityProperty(IEntityProperty delegatedProperty)
    {
        this.delegatedProperty = delegatedProperty;
    }

    //
    // IManagedEntityProperty
    //

    public boolean isOwnTab()
    {
        return ownTab;
    }

    public void setOwnTab(boolean ownTab)
    {
        this.ownTab = ownTab;
    }

    public IManagedUiDescription getUiDescription()
    {
        return uiDescription;
    }

    //
    // IEntityProperty delegated methods
    //

    public String tryGetAsString()
    {
        return delegatedProperty.tryGetAsString();
    }

    public String tryGetOriginalValue()
    {
        return delegatedProperty.tryGetOriginalValue();
    }

    public Material getMaterial()
    {
        return delegatedProperty.getMaterial();
    }

    public void setMaterial(Material material)
    {
        delegatedProperty.setMaterial(material);
    }

    public VocabularyTerm getVocabularyTerm()
    {
        return delegatedProperty.getVocabularyTerm();
    }

    public void setVocabularyTerm(VocabularyTerm vocabularyTerm)
    {
        delegatedProperty.setVocabularyTerm(vocabularyTerm);
    }

    public String getValue()
    {
        // TODO 2011-01-12, Piotr Buczek: remove special handling after testing
        String delegatedValue = delegatedProperty.getValue();
        return delegatedValue == null ? null : "(managed) " + delegatedValue;
    }

    public void setValue(String value)
    {
        delegatedProperty.setValue(value);
    }

    public PropertyType getPropertyType()
    {
        return delegatedProperty.getPropertyType();
    }

    public void setPropertyType(PropertyType propertyType)
    {
        delegatedProperty.setPropertyType(propertyType);
    }

    public void setOrdinal(Long ordinal)
    {
        delegatedProperty.setOrdinal(ordinal);
    }

    public Long getOrdinal()
    {
        return delegatedProperty.getOrdinal();
    }

    public int compareTo(IEntityProperty o)
    {
        return delegatedProperty.compareTo(o);
    }

    //
    // For serialization
    //

    public ManagedEntityProperty()
    {
    }

    @SuppressWarnings("unused")
    private IEntityProperty getDelegatedProperty()
    {
        return delegatedProperty;
    }

    @SuppressWarnings("unused")
    private void setDelegatedProperty(IEntityProperty delegatedProperty)
    {
        this.delegatedProperty = delegatedProperty;
    }

}
