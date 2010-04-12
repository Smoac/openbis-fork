/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class Util
{
    private Util()
    {
    }

    /**
     * Returns an array of all entity properties defined by the specified entity type for which
     * values are found in the specified {@link Properties} object.
     * 
     * @throws UserFailureException if at least one mandatory property were missed.
     */
    static IEntityProperty[] getAndCheckProperties(Properties properties, EntityType entityType)
    {
        List<String> missingMandatoryProperties = new ArrayList<String>();
        List<IEntityProperty> sampleProperties = new ArrayList<IEntityProperty>();
        List<? extends EntityTypePropertyType<?>> sampleTypePropertyTypes = entityType.getAssignedPropertyTypes();
        for (EntityTypePropertyType<?> entityTypePropertyType : sampleTypePropertyTypes)
        {
            boolean mandatory = entityTypePropertyType.isMandatory();
            PropertyType propertyType = entityTypePropertyType.getPropertyType();
            String key = propertyType.getCode();
            String value = properties.getProperty(key);
            if (value == null)
            {
                if (mandatory)
                {
                    missingMandatoryProperties.add(key);
                }
            } else
            {
                EntityProperty property = new EntityProperty();
                property.setPropertyType(propertyType);
                property.setValue(value);
                sampleProperties.add(property);
            }
        }
        if (missingMandatoryProperties.isEmpty() == false)
        {
            throw new UserFailureException("The following mandatory properties are missed: "
                    + missingMandatoryProperties);
        }
        return sampleProperties.toArray(new IEntityProperty[sampleProperties.size()]);
    }
}
