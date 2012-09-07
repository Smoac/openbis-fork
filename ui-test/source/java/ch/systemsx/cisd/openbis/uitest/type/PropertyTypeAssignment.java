/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.type;

import java.util.Map;

import ch.systemsx.cisd.openbis.uitest.infra.Browsable;
import ch.systemsx.cisd.openbis.uitest.infra.EntityType;

/**
 * @author anttil
 */
public class PropertyTypeAssignment implements Browsable
{

    private PropertyType propertyType;

    private EntityType entityType;

    private boolean mandatory;

    private String initialValue;

    public PropertyTypeAssignment(PropertyType propertyType, EntityType entityType,
            boolean mandatory, String initialValue)
    {
        this.entityType = entityType;
        this.propertyType = propertyType;
        this.mandatory = mandatory;
        this.initialValue = initialValue;
    }

    @Override
    public boolean isRepresentedBy(Map<String, String> row)
    {
        System.out.println("Searching from " + row);
        return propertyType.getCode().equalsIgnoreCase(row.get("Property Type Code")) &&
                entityType.getCode().equalsIgnoreCase(row.get("Entity Type"));
    }

    @Override
    public String toString()
    {
        return "PropertyTypeAssignment [" + this.propertyType + ", " + this.entityType + "]";
    }

    public PropertyType getPropertyType()
    {
        return propertyType;
    }

    public EntityType getSampleType()
    {
        return entityType;
    }

    public boolean isMandatory()
    {
        return mandatory;
    }

    public String getInitialValue()
    {
        return initialValue;
    }
}
