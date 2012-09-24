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

import ch.systemsx.cisd.openbis.uitest.page.tab.BrowserCell;
import ch.systemsx.cisd.openbis.uitest.page.tab.BrowserRow;

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
    public boolean isRepresentedBy(BrowserRow row)
    {
        BrowserCell propertyTypeCode = row.get("Property Type Code");
        BrowserCell entityTypeCode = row.get("Entity Type");

        return propertyTypeCode != null &&
                entityTypeCode != null &&
                propertyTypeCode.getText().equalsIgnoreCase(propertyType.getCode()) &&
                entityTypeCode.getText().equalsIgnoreCase(entityType.getCode());
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
