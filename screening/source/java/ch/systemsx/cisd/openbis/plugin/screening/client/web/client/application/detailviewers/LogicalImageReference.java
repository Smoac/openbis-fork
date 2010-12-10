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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Points to one logical image in the image dataset. For HCS it will be all images of the well. For
 * microscopy the whole dataset contains one logical image.
 * 
 * @author Tomasz Pylak
 */
public class LogicalImageReference
{
    private final DatasetImagesReference imageDataset;

    private final WellLocation wellLocationOrNull;

    public LogicalImageReference(DatasetImagesReference imageDataset,
            WellLocation wellLocationOrNull)
    {
        assert imageDataset != null : "image dataset is null";
        this.imageDataset = imageDataset;
        this.wellLocationOrNull = wellLocationOrNull;
    }

    public WellLocation tryGetWellLocation()
    {
        return wellLocationOrNull;
    }

    private ImageDatasetParameters getImageParams()
    {
        return imageDataset.getImageParameters();
    }

    private DatasetReference getDataset()
    {
        return imageDataset.getDatasetReference();
    }

    public int getTileRowsNum()
    {
        return getImageParams().getTileRowsNum();
    }

    public int getTileColsNum()
    {
        return getImageParams().getTileColsNum();
    }

    public List<String> getChannelsCodes()
    {
        return getImageParams().getChannelsCodes();
    }

    public boolean isMultidimensional()
    {
        return getImageParams().isMultidimensional();
    }

    public String getDatasetCode()
    {
        return getDataset().getCode();
    }

    public String getDatastoreHostUrl()
    {
        return getDataset().getDatastoreHostUrl();
    }

    public String getDatastoreCode()
    {
        return getDataset().getDatastoreCode();
    }

    public String getTransformerFactorySignatureOrNull(String channelCode)
    {
        return getImageParams().getTransformerFactorySignatureOrNull(channelCode);
    }

}
