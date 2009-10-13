/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.PropertyTypeRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.CommonExternalDataColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.DataSetPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesFilterUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Tomasz Pylak
 */
public class DataSetSearchHitModel extends BaseEntityModel<ExternalData>
{
    private static final long serialVersionUID = 1L;

    private static final int PROPERTY_COLUMN_WIDTH = 150;

    public DataSetSearchHitModel(final GridRowModel<ExternalData> entity)
    {
        super(entity, createColumnsSchema(entity));
    }

    // here we create the columns definition having just one table row. We need them only to render
    // column values (headers have been already created), so no message provider is needed.
    private static List<IColumnDefinitionUI<ExternalData>> createColumnsSchema(
            GridRowModel<ExternalData> entity)
    {
        List<IColumnDefinitionUI<ExternalData>> list = createCommonColumnsSchema(null);

        List<IEntityProperty> properties =
                DataSetPropertyColDef.getDataSetProperties(entity.getOriginalObject());
        List<PropertyType> datasetProperties = extractPropertyTypes(properties);
        list.addAll(createDatasetPropertyTypeColDefs(datasetProperties));
        return list;
    }

    private static List<IColumnDefinitionUI<ExternalData>> createDatasetPropertyTypeColDefs(
            List<PropertyType> propertyTypes)
    {
        List<IColumnDefinitionUI<ExternalData>> list = createEmptyList();
        for (PropertyType prop : propertyTypes)
        {
            list.add(createDatasetPropertyTypeColDef(prop, propertyTypes));
        }
        return list;
    }

    private static ArrayList<IColumnDefinitionUI<ExternalData>> createEmptyList()
    {
        return new ArrayList<IColumnDefinitionUI<ExternalData>>();
    }

    private static List<PropertyType> extractPropertyTypes(
            List<? extends IEntityProperty> properties)
    {
        List<PropertyType> propertyTypes = new ArrayList<PropertyType>();
        for (IEntityProperty prop : properties)
        {
            PropertyType propertyType = prop.getPropertyType();
            propertyTypes.add(propertyType);
        }
        return propertyTypes;
    }

    public static ColumnDefsAndConfigs<ExternalData> createColumnsSchema(
            IMessageProvider messageProvider, List<PropertyType> mergedPropertyTypesOrNull)
    {
        List<IColumnDefinitionUI<ExternalData>> commonColumnsSchema =
                createCommonColumnsSchema(messageProvider);
        ColumnDefsAndConfigs<ExternalData> columns =
                ColumnDefsAndConfigs.create(commonColumnsSchema);

        if (mergedPropertyTypesOrNull != null)
        {
            List<PropertyType> datasetProperties =
                    PropertyTypesFilterUtil.filterDataSetPropertyTypes(mergedPropertyTypesOrNull);
            columns.addColumns(createDatasetPropertyTypeColDefs(datasetProperties));
        }
        return columns;
    }

    private static IColumnDefinitionUI<ExternalData> createDatasetPropertyTypeColDef(
            PropertyType propertyType, List<PropertyType> propertyTypes)
    {
        String label = PropertyTypeRenderer.getDisplayName(propertyType, propertyTypes);
        return new DataSetPropertyColDef(propertyType, true, PROPERTY_COLUMN_WIDTH, label);
    }

    private static List<IColumnDefinitionUI<ExternalData>> createCommonColumnsSchema(
            IMessageProvider msgProviderOrNull)
    {
        return createColumnsDefinition(CommonExternalDataColDefKind.values(), msgProviderOrNull);
    }
}
