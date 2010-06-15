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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityReference;
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
    WELL_NESTED_MATERIAL(new AbstractColumnDefinitionKind<WellContent>(Dict.WELL_NESTED_MATERIAL)
        {
            @Override
            public String tryGetValue(WellContent entity)
            {
                EntityReference nestedMaterial = entity.tryGetNestedMaterialContent();
                return nestedMaterial != null ? nestedMaterial.getCode() : null;
            }

            @Override
            public String tryGetLink(WellContent entity)
            {
                return LinkExtractor.tryExtract(entity.tryGetNestedMaterialContent());
            }
        }),

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
                DatasetImagesReference imageDataset = entity.tryGetImages();
                return imageDataset != null ? imageDataset.getDatasetReference().getCode() : null;
            }

            @Override
            public String tryGetLink(WellContent entity)
            {
                DatasetImagesReference imageDataset = entity.tryGetImages();
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
                DatasetImagesReference imageDataset = entity.tryGetImages();
                return imageDataset != null ? imageDataset.getDatasetReference().getFileTypeCode()
                        : null;
            }
        }),

    IMAGE(new AbstractColumnDefinitionKind<WellContent>(Dict.WELL_IMAGES)
        {
            @Override
            public String tryGetValue(WellContent entity)
            {
                return "Show";
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
