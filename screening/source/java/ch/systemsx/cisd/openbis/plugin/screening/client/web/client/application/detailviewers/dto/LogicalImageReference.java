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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.dto;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetImagesReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
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
    private final String datasetCode;

    private final String datastoreCode;

    private final String datastoreHostUrl;

    private final ImageDatasetParameters imageParameters;

    private final WellLocation wellLocationOrNull;

    private final List<DatasetImagesReference> overlayDatasets;

    public LogicalImageReference(ImageDatasetEnrichedReference imageEnrichedDataset,
            WellLocation wellLocationOrNull)
    {
        this(imageEnrichedDataset.getImageDataset(), imageEnrichedDataset.getOverlayDatasets(),
                wellLocationOrNull);
    }

    private LogicalImageReference(DatasetImagesReference imageDataset,
            List<DatasetImagesReference> overlayDatasets, WellLocation wellLocationOrNull)
    {
        assert imageDataset != null : "image dataset is null";
        this.datasetCode = imageDataset.getDatasetCode();
        this.datastoreCode = imageDataset.getDatastoreCode();
        this.datastoreHostUrl = imageDataset.getDatastoreHostUrl();
        this.imageParameters = imageDataset.getImageParameters();
        this.wellLocationOrNull = wellLocationOrNull;
        this.overlayDatasets = overlayDatasets;
    }

    public LogicalImageReference(String datasetCode, String datastoreCode, String datastoreHostUrl,
            ImageDatasetParameters imageParameters)
    {
        this.datasetCode = datasetCode;
        this.datastoreCode = datastoreCode;
        this.datastoreHostUrl = datastoreHostUrl;
        this.imageParameters = imageParameters;
        this.wellLocationOrNull = null;
        this.overlayDatasets = new ArrayList<DatasetImagesReference>();
    }

    /**
     * Creates a new instance with refreshed dataset references, does not change the current
     * instance.
     */
    public LogicalImageReference updateDatasets(ImageDatasetEnrichedReference refreshedDataset)
    {
        return new LogicalImageReference(refreshedDataset, wellLocationOrNull);
    }

    public WellLocation tryGetWellLocation()
    {
        return wellLocationOrNull;
    }

    public int getTileRowsNum()
    {
        return imageParameters.getTileRowsNum();
    }

    public int getTileColsNum()
    {
        return imageParameters.getTileColsNum();
    }

    public List<String> getChannelsCodes()
    {
        return imageParameters.getChannelsCodes();
    }

    public boolean isMultidimensional()
    {
        return imageParameters.isMultidimensional();
    }

    public String getDatasetCode()
    {
        return datasetCode;
    }

    public String getDatastoreHostUrl()
    {
        return datastoreHostUrl;
    }

    public String getDatastoreCode()
    {
        return datastoreCode;
    }

    public String getTransformerFactorySignatureOrNull(String channelCode)
    {
        return imageParameters.getTransformerFactorySignatureOrNull(channelCode);
    }

    public List<DatasetImagesReference> getOverlayDatasets()
    {
        return overlayDatasets;
    }

    public DatasetReference getDatasetReference()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
