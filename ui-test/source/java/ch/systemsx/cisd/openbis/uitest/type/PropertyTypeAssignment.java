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

import java.util.Arrays;
import java.util.Collection;

import ch.systemsx.cisd.openbis.uitest.infra.application.GuiApplicationRunner;
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

    public PropertyType getPropertyType()
    {
        return propertyType;
    }

    public EntityType getEntityType()
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

    @Override
    public BrowserRow getBrowserContent(GuiApplicationRunner openbis)
    {
        return openbis.browseTo(this);
    }

    @Override
    public Collection<String> getColumns()
    {
        return Arrays.asList("Property Type Code", "Entity Type", "Mandatory?");
    }

    @Override
    public String getCode()
    {
        return propertyType.getCode();
    }

    @Override
    public int hashCode()
    {
        int result = 17;
        result = 31 * result + propertyType.getCode().hashCode();
        result = 31 * result + entityType.getCode().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof PropertyTypeAssignment)
        {
            PropertyTypeAssignment assignment = (PropertyTypeAssignment) o;
            return assignment.getPropertyType().getCode().equals(propertyType.getCode()) &&
                    assignment.getEntityType().getCode().equals(entityType.getCode());
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "PropertyTypeAssignment [" + this.propertyType + ", " + this.entityType + "]";
    }

}
