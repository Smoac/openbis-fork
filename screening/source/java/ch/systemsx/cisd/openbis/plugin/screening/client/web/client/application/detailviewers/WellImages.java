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
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Stores: 1. image dataset reference and metadata 2. well location
 * 
 * @author Tomasz Pylak
 */
public class WellImages
{
    private final PlateImageParameters imageParams;

    private final DatasetReference dataset;

    private final WellLocation wellLocation;

    public WellImages(DatasetImagesReference imageDataset, WellLocation location)
    {
        assert imageDataset != null : "image dataset is null";
        assert location != null : "location is null";
        this.imageParams = imageDataset.getImageParameters();
        this.dataset = imageDataset.getDatasetReference();
        this.wellLocation = location;
    }

    public int getTileRowsNum()
    {
        return imageParams.getTileRowsNum();
    }

    public int getTileColsNum()
    {
        return imageParams.getTileColsNum();
    }

    public List<String> getChannelsCodes()
    {
        return imageParams.getChannelsCodes();
    }

    public boolean isMultidimensional()
    {
        return imageParams.isMultidimensional();
    }

    public String getDatasetCode()
    {
        return dataset.getCode();
    }

    public String getDownloadUrl()
    {
        return dataset.getDownloadUrl();
    }

    public String getDatastoreCode()
    {
        return dataset.getDatastoreCode();
    }

    public WellLocation getWellLocation()
    {
        return wellLocation;
    }
}
