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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server;

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchGridColumnIds.EXPERIMENT;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchGridColumnIds.FILE_FORMAT_TYPE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchGridColumnIds.IMAGE_ANALYSIS_DATA_SET;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchGridColumnIds.IMAGE_DATA_SET;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchGridColumnIds.PLATE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchGridColumnIds.WELL;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchGridColumnIds.WELL_COLUMN;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchGridColumnIds.WELL_CONTENT_MATERIAL;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchGridColumnIds.WELL_CONTENT_MATERIAL_TYPE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchGridColumnIds.WELL_IMAGES;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchGridColumnIds.WELL_ROW;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.IColumn;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.NamedFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;

/**
 * @author Franz-Josef Elmer
 */
public class WellContentProvider extends AbstractTableModelProvider<WellContent>
{
    static final String WELL_CONTENT_PROPERTY_ID_PREFIX = "WELL_CONTENT_PROPERTY-";

    static final String WELL_CONTENT_FEATURE_VECTOR_PREFIX = "WELL_CONTENT_FEATURE_VECTOR-";

    private final IScreeningServer server;

    private final String sessionToken;

    private final WellSearchCriteria materialCriteria;

    WellContentProvider(IScreeningServer server, String sessionToken,
            WellSearchCriteria materialCriteria)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.materialCriteria = materialCriteria;
    }

    @Override
    protected TypedTableModel<WellContent> createTableModel(int maxSize)
    {
        TypedTableModelBuilder<WellContent> builder = new TypedTableModelBuilder<WellContent>();
        addStandardWellColumns(builder);
        List<WellContent> wells = server.listPlateWells(sessionToken, materialCriteria);
        addWellPropertiesColumns(builder, wells);

        for (WellContent well : wells)
        {
            addRow(builder, well);
        }
        return builder.getModel();
    }

    private void addStandardWellColumns(TypedTableModelBuilder<WellContent> builder)
    {
        builder.addColumn(WELL_CONTENT_MATERIAL);
        builder.addColumn(WELL_CONTENT_MATERIAL_TYPE);
        builder.addColumn(EXPERIMENT);
        builder.addColumn(PLATE);
        builder.addColumn(WELL);
        builder.addColumn(WELL_ROW).withDataType(DataTypeCode.INTEGER);
        builder.addColumn(WELL_COLUMN).withDataType(DataTypeCode.INTEGER);
        builder.addColumn(IMAGE_DATA_SET);
        builder.addColumn(IMAGE_ANALYSIS_DATA_SET);
        builder.addColumn(FILE_FORMAT_TYPE);
        builder.addColumn(WELL_IMAGES).withDefaultWidth(500);
    }

    private void addWellPropertiesColumns(TypedTableModelBuilder<WellContent> builder,
            List<WellContent> wells)
    {
        Set<String> columnNames = new HashSet<String>();
        for (WellContent well : wells)
        {
            for (IEntityProperty property : well.getWellProperties())
            {
                String columnName = property.getPropertyType().getCode();
                columnNames.add(columnName);
            }
        }

        for (String columnName : columnNames)
        {
            builder.addColumn(columnName).withTitle(columnName);
        }
    }

    private void addRow(TypedTableModelBuilder<WellContent> builder, WellContent well)
    {
        builder.addRow(well);
        Material material = well.getMaterialContent();
        String value = material.getCode();
        builder.column(WELL_CONTENT_MATERIAL).addString(value);
        builder.column(WELL_CONTENT_MATERIAL_TYPE).addString(material.getEntityType().getCode());
        builder.columnGroup(WELL_CONTENT_PROPERTY_ID_PREFIX)
                .addProperties(material.getProperties());
        NamedFeatureVector featureVector = well.tryGetFeatureVectorValues();
        if (featureVector != null)
        {
            addFeatureColumns(builder, featureVector);
        }
        builder.column(EXPERIMENT).addString(well.getExperiment().toString());
        builder.column(PLATE).addString(well.getPlate().getCode());
        builder.column(WELL).addString(well.getWell().getCode());
        WellLocation location = well.tryGetLocation();
        builder.column(WELL_ROW).addInteger(location == null ? null : new Long(location.getRow()));
        builder.column(WELL_COLUMN).addInteger(
                location == null ? null : new Long(location.getColumn()));
        DatasetImagesReference imageDataset = well.tryGetImageDataset();
        builder.column(IMAGE_DATA_SET).addString(
                imageDataset == null ? null : imageDataset.getDatasetCode());
        DatasetReference dataset = well.tryGetFeatureVectorDataset();
        builder.column(IMAGE_ANALYSIS_DATA_SET).addString(
                dataset == null ? null : dataset.getCode());
        builder.column(FILE_FORMAT_TYPE).addString(
                imageDataset == null ? null : imageDataset.getDatasetReference().getFileTypeCode());
        builder.column(WELL_IMAGES).addString(well.tryGetImageDataset() == null ? "" : "[images]");

        addWellProperties(builder, well);
    }

    private void addFeatureColumns(TypedTableModelBuilder<WellContent> builder,
            NamedFeatureVector featureVector)
    {
        FeatureValue[] values = featureVector.getValues();
        String[] labels = featureVector.getFeatureLabels();
        String[] codes = featureVector.getFeatureCodes();
        for (int i = 0; i < values.length; i++)
        {
            IColumn column =
                    builder.column(WELL_CONTENT_FEATURE_VECTOR_PREFIX + codes[i]).withTitle(
                            labels[i]);
            FeatureValue featureValue = values[i];
            if (featureValue.isFloat())
            {
                column.addDouble(new Double(featureValue.asFloat()));
            } else
            {
                column.addString(featureValue.tryAsVocabularyTerm());
            }
        }
    }

    private void addWellProperties(TypedTableModelBuilder<WellContent> builder, WellContent well)
    {
        for (IEntityProperty property : well.getWellProperties())
        {
            PropertyType propertyType = property.getPropertyType();
            String columnCode = propertyType.getCode();
            String columnLabel = propertyType.getLabel();

            IColumn column = builder.column(columnCode).withTitle(columnLabel);
            DataTypeCode dataTypeCode = propertyType.getDataType().getCode();
            String value = property.tryGetAsString();
            if (dataTypeCode == DataTypeCode.INTEGER)
            {
                try
                {
                    column.addInteger(Long.parseLong(value));
                } catch (NumberFormatException ex)
                {
                    column.addString(value);
                }
            } else if (dataTypeCode == DataTypeCode.REAL)
            {
                try
                {
                    column.addDouble(Double.parseDouble(value));
                } catch (NumberFormatException ex)
                {
                    column.addString(value);
                }
            } else
            {
                column.addString(value);
            }

        }
    }

}
