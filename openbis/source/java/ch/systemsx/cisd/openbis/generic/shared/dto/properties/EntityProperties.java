/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto.properties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyTypeDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleEntityProperty;

/**
 * Type property implementation with some extensions used by DAOs.<br>
 * Note that "orNull" and "try" naming convention is used everywhere.
 * 
 * @author Tomasz Pylak
 */
public class EntityProperties extends AbstractHashable
{
    // TODO 2008-02-16, Tomasz Pylak: this field should be removed, setProperty has to have schema
    // as parameter.
    // It will make instance of this class smaller, so we could delete SimpleEntityProperty class
    // and replace it with this class when axis web-service interface will not be used anymore.
    private final EntityPropertyTypeDTOContainer propertyTypeContainer;

    private final Map<String, EntityPropertyValue> properties;

    public EntityProperties(final EntityPropertyTypeDTOContainer propertiesSchema)
    {
        this.properties = new LinkedHashMap<String, EntityPropertyValue>();
        this.propertyTypeContainer = propertiesSchema;
        createInitialProperties();
    }

    private final void createInitialProperties()
    {
        for (final String name : propertyTypeContainer.getAllPropertyCodes())
        {
            final EntityPropertyTypeDTO spec = propertyTypeContainer.getPropertyType(name);
            final EntityPropertyValue value =
                    EntityPropertyValue.createFromUntyped(null, spec.getDataTypeCode());
            properties.put(name, value);
        }
    }

    public boolean hasProperty(final String name)
    {
        return properties.containsKey(name);
    }

    public final EntityPropertyValue getProperty(final String name) throws IllegalArgumentException
    {
        final EntityPropertyValue value = properties.get(name);
        if (value == null)
        {
            throw new IllegalArgumentException("No entity property value for '" + name + "'.");
        }
        return value;
    }

    // checks if value is mandatory and casts it to appropriate type
    public final void setProperty(final String name, final String untypedValueOrNull)
    {
        final EntityPropertyValue value =
                propertyTypeContainer.validateAndCreateValue(name, untypedValueOrNull);
        properties.put(name, value);
    }

    public static EntityProperties createFromSimple(final SimpleEntityProperty[] simpleProperties,
            final EntityPropertyTypeDTOContainer propertySchema)
    {
        if (simpleProperties == null)
        {
            return null;
        }
        final EntityProperties properties = new EntityProperties(propertySchema);
        for (int i = 0; i < simpleProperties.length; i++)
        {
            final SimpleEntityProperty property = simpleProperties[i];
            final String name = property.getCode();
            if (propertySchema.hasPropertySchema(name))
            {
                properties.setProperty(name, EntityPropertyValue.createFromSimple(property)
                        .tryGetUntypedValue());
            } else
            {
                throw new IllegalArgumentException("Unknown property '" + name + "'.");
            }
        }
        return properties;
    }

    public SimpleEntityProperty[] createSimple()
    {
        final List<SimpleEntityProperty> result = new ArrayList<SimpleEntityProperty>();
        for (final Map.Entry<String, EntityPropertyValue> entry : properties.entrySet())
        {
            final String label =
                    propertyTypeContainer.getPropertyType(entry.getKey()).getPropertyType()
                            .getLabel();
            SimpleEntityProperty property = entry.getValue().createSimple(entry.getKey(), label);
            result.add(property);
        }
        return result.toArray(new SimpleEntityProperty[result.size()]);
    }
}
