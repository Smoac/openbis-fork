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

package ch.systemsx.cisd.openbis.generic.client.web.server.translator;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * A {@link PropertyType} &lt;---&gt; {@link PropertyTypePE} translator.
 * 
 * @author Izabela Adamczyk
 */
public final class PropertyTypeTranslator
{

    private PropertyTypeTranslator()
    {
        // Can not be instantiated.
    }

    public final static PropertyType translate(final PropertyTypePE propertyType)
    {
        final PropertyType result = new PropertyType();
        result.setCode(propertyType.getCode());
        result.setInternalNamespace(propertyType.isInternalNamespace());
        result.setManagedInternally(propertyType.isManagedInternally());
        result.setLabel(propertyType.getLabel());
        result.setDataType(DataTypeTranslator.translate(propertyType.getType()));
        result.setVocabulary(VocabularyTranslator.translate(propertyType.getVocabulary()));
        result.setDescription(propertyType.getDescription());
        result.setSampleTypePropertyTypes(SampleTypePropertyTypeTranslator.translate(propertyType
                .getSampleTypePropertyTypes(), result));
        result.setMaterialTypePropertyTypes(MaterialTypePropertyTypeTranslator.translate(
                propertyType.getMaterialTypePropertyTypes(), result));
        result.setExperimentTypePropertyTypes(ExperimentTypePropertyTypeTranslator.translate(
                propertyType.getExperimentTypePropertyTypes(), result));
        return result;
    }

    public final static PropertyTypePE translate(final PropertyType propertyType)
    {
        final PropertyTypePE result = new PropertyTypePE();
        result.setCode(propertyType.getCode());
        result.setInternalNamespace(propertyType.isInternalNamespace());
        result.setLabel(propertyType.getLabel());
        return result;
    }

    public final static List<PropertyTypePE> translate(final List<PropertyType> propertyCodes)
    {
        final List<PropertyTypePE> result = new ArrayList<PropertyTypePE>();
        for (final PropertyType s : propertyCodes)
        {
            result.add(translate(s));
        }
        return result;
    }

}
