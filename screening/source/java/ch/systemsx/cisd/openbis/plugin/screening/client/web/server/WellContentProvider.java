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

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialReviewerColumnIds.EXPERIMENT;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialReviewerColumnIds.FILE_FORMAT_TYPE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialReviewerColumnIds.IMAGE_ANALYSIS_DATA_SET;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialReviewerColumnIds.IMAGE_DATA_SET;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialReviewerColumnIds.PLATE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialReviewerColumnIds.WELL;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialReviewerColumnIds.WELL_COLUMN;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialReviewerColumnIds.WELL_CONTENT_MATERIAL;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialReviewerColumnIds.WELL_CONTENT_MATERIAL_TYPE;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialReviewerColumnIds.WELL_IMAGES;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialReviewerColumnIds.WELL_ROW;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.ITableModelProvider;
import ch.systemsx.cisd.openbis.generic.server.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.NamedFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class WellContentProvider implements ITableModelProvider<WellContent>
{   
    static final String WELL_CONTENT_PROPERTY_ID_PREFIX = "WELL_CONTENT_PROPERTY-";
    static final String WELL_CONTENT_FEATURE_VECTOR_PREFIX = "WELL_CONTENT_FEATURE_VECTOR-";
    
    private final IScreeningServer server;
    private final String sessionToken;
    private final PlateMaterialsSearchCriteria materialCriteria;

    WellContentProvider(IScreeningServer server, String sessionToken, PlateMaterialsSearchCriteria materialCriteria)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.materialCriteria = materialCriteria;
    }

    public TypedTableModel<WellContent> getTableModel()
    {
        TypedTableModelBuilder<WellContent> builder = new TypedTableModelBuilder<WellContent>();
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
        List<WellContent> wells = server.listPlateWells(sessionToken, materialCriteria);
        for (WellContent well : wells)
        {
            builder.addRow(well);
            Material material = well.getMaterialContent();
            String value = material.getCode();
            builder.addStringValueToColumn(WELL_CONTENT_MATERIAL, value);
            builder.addStringValueToColumn(WELL_CONTENT_MATERIAL_TYPE, material.getEntityType()
                    .getCode());
            List<IEntityProperty> properties = material.getProperties();
            for (IEntityProperty property : properties)
            {
                PropertyType propertyType = property.getPropertyType();
                String code = propertyType.getCode();
                builder.addStringValueToColumn(propertyType.getLabel(),
                        WELL_CONTENT_PROPERTY_ID_PREFIX + code, property.tryGetAsString());
            }
            NamedFeatureVector featureVector = well.tryGetFeatureVectorValues();
            if (featureVector != null)
            {
                float[] values = featureVector.getValues();
                String[] labels = featureVector.getFeatureLabels();
                String[] codes = featureVector.getFeatureCodes();
                for (int i = 0; i < values.length; i++)
                {
                    builder.addDoubleValueToColumn(labels[i], WELL_CONTENT_FEATURE_VECTOR_PREFIX
                            + codes[i], new Double(values[i]));
                }
            }
            builder.addStringValueToColumn(EXPERIMENT, well.getExperiment().toString());
            builder.addStringValueToColumn(PLATE, well.getPlate().getCode());
            builder.addStringValueToColumn(WELL, well.getWell().getCode());
            WellLocation location = well.tryGetLocation();
            builder.addIntegerValueToColumn(WELL_ROW, location == null ? null : new Long(location
                    .getRow()));
            builder.addIntegerValueToColumn(WELL_COLUMN, location == null ? null : new Long(
                    location.getColumn()));
            DatasetImagesReference imageDataset = well.tryGetImageDataset();
            builder.addStringValueToColumn(IMAGE_DATA_SET, imageDataset == null ? null
                    : imageDataset.getDatasetCode());
            DatasetReference dataset = well.tryGetFeatureVectorDataset();
            builder.addStringValueToColumn(IMAGE_ANALYSIS_DATA_SET, dataset == null ? null
                    : dataset.getCode());
            builder.addStringValueToColumn(FILE_FORMAT_TYPE, imageDataset == null ? null
                    : imageDataset.getDatasetReference().getFileTypeCode());
            builder.addStringValueToColumn(WELL_IMAGES, well.tryGetImageDataset() == null ? ""
                    : "[images]");
        }
        return builder.getModel();
    }
}
