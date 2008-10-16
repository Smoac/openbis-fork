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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;

class PropertyColumns
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
        columnConfig.setId(SampleModel.PROPERTY_PREFIX
                + stpt.getPropertyType().isInternalNamespace()
                + stpt.getPropertyType().getSimpleCode());
        columnConfig.setHeader(stpt.getPropertyType().getLabel());
        columnConfig.setWidth(80);
        columnConfig.setHidden(stpt.isDisplayed() == false);
        columnConfig.setPropertyType(stpt.getPropertyType());
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

    class LoadableColumnConfig extends ColumnConfig
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

}