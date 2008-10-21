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

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the sample properties columns for sample grid/table.
 * 
 * @author Izabela Adamczyk
 */
public class PropertyColumns
{

    List<LoadableColumnConfig> columns;

    public PropertyColumns()
    {
        columns = new ArrayList<LoadableColumnConfig>();
    }

    public void define(SampleType type)
    {
        columns.clear();
        for (final SampleTypePropertyType stpt : type.getSampleTypePropertyTypes())
        {
            columns.add(createPropertyColumn(stpt));
        }
    }

    public void resetLoaded()
    {
        for (LoadableColumnConfig cc : columns)
        {
            cc.setLoaded(false);
        }

    }

    public List<LoadableColumnConfig> getColumns()
    {
        return columns;
    }

    private final LoadableColumnConfig createPropertyColumn(final SampleTypePropertyType stpt)
    {
        final LoadableColumnConfig columnConfig = new LoadableColumnConfig();
        columnConfig.setMenuDisabled(true);
        PropertyType propertyType = stpt.getPropertyType();
        columnConfig.setId(SampleModel.PROPERTY_PREFIX + propertyType.isInternalNamespace()
                + propertyType.getSimpleCode());
        columnConfig.setHeader(propertyType.getLabel());
        columnConfig.setWidth(80);
        columnConfig.setHidden(stpt.isDisplayed() == false);
        columnConfig.setPropertyType(propertyType);
        return columnConfig;
    }

    public boolean isDirty()
    {
        for (LoadableColumnConfig cc : columns)
        {
            if (cc.isDirty())
            {
                return true;
            }
        }
        return false;
    }

    public List<PropertyType> getDirtyColumns()
    {
        final ArrayList<PropertyType> result = new ArrayList<PropertyType>();
        for (LoadableColumnConfig cc : columns)
        {
            if (cc.isDirty())
            {
                result.add(cc.getPropertyType());
            }
        }
        return result;
    }

    public List<PropertyType> getChosenColumns()
    {
        final ArrayList<PropertyType> result = new ArrayList<PropertyType>();
        for (LoadableColumnConfig cc : columns)
        {
            if (cc.isHidden() == false)
            {
                result.add(cc.getPropertyType());
            }
        }
        return result;
    }

}