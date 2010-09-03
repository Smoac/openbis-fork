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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.AbstractColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Column definitions for the PlateMaterialReviewer.<br>
 * 
 * @author Tomasz Pylak
 */
public enum PlateMaterialReviewerColDefKind implements IColumnDefinitionKind<WellContent>
{
    WELL_CONTENT_MATERIAL(new AbstractColumnDefinitionKind<WellContent>(Dict.WELL_CONTENT_MATERIAL,
            true)
        {
            @Override
            public String tryGetValue(WellContent entity)
            {
                return entity.getMaterialContent().getCode();
            }

            @Override
            public String tryGetLink(WellContent entity)
            {
                return LinkExtractor.tryExtract(entity.getMaterialContent());
            }
        }),

    WELL_CONTENT_MATERIAL_TYPE(new AbstractColumnDefinitionKind<WellContent>(
            Dict.WELL_CONTENT_MATERIAL_TYPE, true)
        {
            @Override
            public String tryGetValue(WellContent entity)
            {
                return entity.getMaterialContent().getEntityType().getCode();
            }
        }),

    WELL_CONTENT_PROPERTIES(new AbstractColumnDefinitionKind<WellContent>(
            Dict.WELL_CONTENT_PROPERTIES, true)
        {
            @Override
            public String tryGetValue(WellContent entity)
            {

                Material material = entity.getMaterialContent();
                String separator = " <br/> ";
                StringBuilder sb = new StringBuilder();
                for (IEntityProperty p : material.getProperties())
                {
                    if (sb.length() != 0)
                    {
                        sb.append(separator);
                    }
                    sb.append(p.getPropertyType().getCode());
                    sb.append(": ");
                    sb.append(p.tryGetAsString());
                }
                return sb.toString();
            }
        }),

    EXPERIMENT(new AbstractColumnDefinitionKind<WellContent>(Dict.EXPERIMENT, true)
        {
            @Override
            public String tryGetValue(WellContent entity)
            {
                return entity.getExperiment().toString();
            }

            @Override
            public String tryGetLink(WellContent entity)
            {
                return LinkExtractor.tryExtract(entity.getExperiment());
            }
        }),

    PLATE(new AbstractColumnDefinitionKind<WellContent>(Dict.PLATE)
        {
            @Override
            public String tryGetValue(WellContent entity)
            {
                return entity.getPlate().getCode();
            }

            @Override
            public String tryGetLink(WellContent entity)
            {
                return LinkExtractor.tryExtract(entity.getPlate());
            }
        }),

    WELL(new AbstractColumnDefinitionKind<WellContent>(Dict.WELL)
        {
            @Override
            public String tryGetValue(WellContent entity)
            {
                return entity.getWell().getCode();
            }

            @Override
            public String tryGetLink(WellContent entity)
            {
                return LinkExtractor.tryExtract(entity.getWell());
            }
        }),

    WELL_ROW(new AbstractColumnDefinitionKind<WellContent>(Dict.WELL_ROW, true)
        {
            @Override
            public String tryGetValue(WellContent entity)
            {
                WellLocation location = entity.tryGetLocation();
                return location == null ? null : "" + location.getRow();
            }
        }),

    WELL_COLUMN(new AbstractColumnDefinitionKind<WellContent>(Dict.WELL_COLUMN, true)
        {
            @Override
            public String tryGetValue(WellContent entity)
            {
                WellLocation location = entity.tryGetLocation();
                return location == null ? null : "" + location.getColumn();
            }
        }),

    DATASET(new AbstractColumnDefinitionKind<WellContent>(Dict.DATA_SET)
        {
            @Override
            public String tryGetValue(WellContent entity)
            {
                DatasetImagesReference imageDataset = entity.tryGetImageDataset();
                return imageDataset != null ? imageDataset.getDatasetReference().getCode() : null;
            }

            @Override
            public String tryGetLink(WellContent entity)
            {
                DatasetImagesReference imageDataset = entity.tryGetImageDataset();
                if (imageDataset != null)
                {
                    return LinkExtractor.tryExtract(imageDataset.getDatasetReference());
                } else
                {
                    return null;
                }
            }
        }),

    DATASET_FILE_TYPE(new AbstractColumnDefinitionKind<WellContent>(Dict.FILE_FORMAT_TYPE)
        {
            @Override
            public String tryGetValue(WellContent entity)
            {
                DatasetImagesReference imageDataset = entity.tryGetImageDataset();
                return imageDataset != null ? imageDataset.getDatasetReference().getFileTypeCode()
                        : null;
            }
        }),

    IMAGE(new AbstractColumnDefinitionKind<WellContent>(Dict.WELL_IMAGES, 500)
        {
            @Override
            public String tryGetValue(WellContent entity)
            {
                if (entity != null && entity.tryGetImageDataset() != null)
                {
                    // Used only for export and filtering, renderer will set the image browser
                    // widget as a value of this column in the GUI
                    return "[images]";
                }
                return null;
            }
        }),

    ;

    private final AbstractColumnDefinitionKind<WellContent> columnDefinitionKind;

    private PlateMaterialReviewerColDefKind(
            AbstractColumnDefinitionKind<WellContent> columnDefinitionKind)
    {
        this.columnDefinitionKind = columnDefinitionKind;
    }

    public String id()
    {
        return name();
    }

    public AbstractColumnDefinitionKind<WellContent> getDescriptor()
    {
        return columnDefinitionKind;
    }
}
