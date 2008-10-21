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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

/**
 * {@link ColumnConfig} extension containing {@link PropertyType} for which it has been created and
 * information if data for given column were loaded.
 * 
 * @author Izabela Adamczyk
 */
public class LoadableColumnConfig extends ColumnConfig
{

    private boolean loaded = false;

    private PropertyType propertyType;

    public boolean isLoaded()
    {
        return loaded;
    }

    public void setPropertyType(PropertyType propertyType)
    {
        this.propertyType = propertyType;
    }

    public void setLoaded(boolean loaded)
    {
        this.loaded = loaded;
    }

    public boolean isDirty()
    {
        return isLoaded() == false && isHidden() == false;
    }

    public PropertyType getPropertyType()
    {
        return propertyType;
    }

}