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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.DataSetExperimentPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.DataSetPropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.DataSetSamplePropertyColDef;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.specific.data.DataSetSearchHitColDefKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.entity.PropertyTypesFilterUtil;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;

/**
 * @author Tomasz Pylak
 */
public class DataSetSearchHitModel extends BaseEntityModel<ExternalData>
{
    private static final long serialVersionUID = 1L;

    private static final int PROPERTY_COLUMN_WIDTH = 150;

    private static final String LABEL_EXPERIMENT_PROPERTY_PREFIX = "Exp. ";

    private static final String LABEL_SAMPLE_PROPERTY_PREFIX = "Sample ";

    private static final String LABEL_DATA_SET_PROPERTY_PREFIX = "Data Set ";

    public DataSetSearchHitModel(final ExternalData entity)
    {
        super(entity, createColumnsSchema(entity));
    }

    // here we create the columns definition having just one table row. We need them only to render
    // column values (headers have been already created), so no message provider is needed.
    private static List<IColumnDefinitionUI<ExternalData>> createColumnsSchema(ExternalData entity)
    {
        List<IColumnDefinitionUI<ExternalData>> list = createCommonColumnsSchema(null);

        List<PropertyType> datasetProperties =
                extractPropertyTypes(DataSetPropertyColDef.getDataSetProperties(entity));
        list.addAll(createDatasetPropertyTypeColDefs(datasetProperties));

        List<PropertyType> experimentProperties =
                extractPropertyTypes(DataSetExperimentPropertyColDef
                        .getExperimentProperties(entity));
        list.addAll(createExperimentPropertyTypeColDefs(experimentProperties));

        List<PropertyType> sampleProperties =
                extractPropertyTypes(DataSetSamplePropertyColDef.getSampleProperties(entity));
        list.addAll(createSamplePropertyTypeColDefs(sampleProperties));

        return list;
    }

    private static List<IColumnDefinitionUI<ExternalData>> createDatasetPropertyTypeColDefs(
            List<PropertyType> propertyTypes)
    {
        List<IColumnDefinitionUI<ExternalData>> list = createEmptyList();
        for (PropertyType prop : propertyTypes)
        {
            list.add(createDatasetPropertyTypeColDef(prop));
        }
        return list;
    }

    private static List<IColumnDefinitionUI<ExternalData>> createExperimentPropertyTypeColDefs(
            List<PropertyType> propertyTypes)
    {
        List<IColumnDefinitionUI<ExternalData>> list = createEmptyList();
        for (PropertyType prop : propertyTypes)
        {
            list.add(createExperimentPropertyTypeColDef(prop));
        }
        return list;
    }

    private static List<IColumnDefinitionUI<ExternalData>> createSamplePropertyTypeColDefs(
            List<PropertyType> propertyTypes)
    {
        List<IColumnDefinitionUI<ExternalData>> list = createEmptyList();
        for (PropertyType prop : propertyTypes)
        {
            list.add(createSamplePropertyTypeColDef(prop));
        }
        return list;
    }

    private static ArrayList<IColumnDefinitionUI<ExternalData>> createEmptyList()
    {
        return new ArrayList<IColumnDefinitionUI<ExternalData>>();
    }

    private static List<PropertyType> extractPropertyTypes(
            List<? extends EntityProperty<?, ?>> properties)
    {
        List<PropertyType> propertyTypes = new ArrayList<PropertyType>();
        for (EntityProperty<?, ?> prop : properties)
        {
            PropertyType propertyType = prop.getEntityTypePropertyType().getPropertyType();
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

            List<PropertyType> experimentProperties =
                    PropertyTypesFilterUtil
                            .filterExperimentPropertyTypes(mergedPropertyTypesOrNull);
            columns.addColumns(createExperimentPropertyTypeColDefs(experimentProperties));

            List<PropertyType> sampleProperties =
                    PropertyTypesFilterUtil.filterSamplePropertyTypes(mergedPropertyTypesOrNull);
            columns.addColumns(createSamplePropertyTypeColDefs(sampleProperties));
        }
        return columns;
    }

    private static IColumnDefinitionUI<ExternalData> createDatasetPropertyTypeColDef(
            PropertyType propertyType)
    {
        String label = LABEL_DATA_SET_PROPERTY_PREFIX + propertyType.getLabel();
        return new DataSetPropertyColDef(propertyType, true, PROPERTY_COLUMN_WIDTH, label);
    }

    private static IColumnDefinitionUI<ExternalData> createSamplePropertyTypeColDef(
            PropertyType propertyType)
    {
        String label = LABEL_SAMPLE_PROPERTY_PREFIX + propertyType.getLabel();
        return new DataSetSamplePropertyColDef(propertyType, true, PROPERTY_COLUMN_WIDTH, label);
    }

    private static IColumnDefinitionUI<ExternalData> createExperimentPropertyTypeColDef(
            PropertyType propertyType)
    {
        String label = LABEL_EXPERIMENT_PROPERTY_PREFIX + propertyType.getLabel();
        return new DataSetExperimentPropertyColDef(propertyType, true, PROPERTY_COLUMN_WIDTH, label);
    }

    private static List<IColumnDefinitionUI<ExternalData>> createCommonColumnsSchema(
            IMessageProvider msgProviderOrNull)
    {
        return createColumnsDefinition(DataSetSearchHitColDefKind.values(), msgProviderOrNull);
    }
}
