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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.SampleModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;

/**
 * Defines the sample properties columns for sample grid/table.
 * <p>
 * Currently these columns are not sortable.
 * </p>
 * 
 * @author Izabela Adamczyk
 */
// TODO 2008-11-13, Christian Ribeaud: Make these columns sortable. It is not just about turning on
// a flag. There is more to do on the server side...
public final class PropertyColumns
{
    private final List<ColumnConfig> columns;

    public PropertyColumns()
    {
        columns = new ArrayList<ColumnConfig>();
    }

    public final void define(final SampleType sampleType)
    {
        columns.clear();
        for (final SampleTypePropertyType sampleTypePropertyType : sampleType
                .getSampleTypePropertyTypes())
        {
            columns.add(createPropertyColumn(sampleTypePropertyType));
        }
    }

    public final List<ColumnConfig> getColumns()
    {
        return columns;
    }

    private final ColumnConfig createPropertyColumn(
            final SampleTypePropertyType sampleTypePropertyType)
    {
        final PropertyType propertyType = sampleTypePropertyType.getPropertyType();
        final ColumnConfig columnConfig =
                ColumnConfigFactory.createDefaultColumnConfig(propertyType.getLabel(), SampleModel
                        .createID(propertyType));
        columnConfig.setSortable(false);
        columnConfig.setWidth(80);
        columnConfig.setHidden(sampleTypePropertyType.isDisplayed() == false);
        return columnConfig;
    }
}