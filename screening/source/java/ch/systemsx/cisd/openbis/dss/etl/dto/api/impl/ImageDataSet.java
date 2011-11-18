/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.impl;

import java.io.File;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IImageDataSet;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.IExperimentImmutable;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v1.ISampleImmutable;

/**
 * Represents an image data set for the registration API.
 * 
 * @author Tomasz Pylak
 */
public class ImageDataSet extends DataSet<ImageDataSetInformation> implements IImageDataSet
{
    private DataSet<ImageDataSetInformation> originalDataset;

    private List<IDataSet> thumbnailDatasets = Collections.emptyList();

    public ImageDataSet(
            DataSetRegistrationDetails<? extends ImageDataSetInformation> registrationDetails,
            File dataSetFolder)
    {
        super(registrationDetails, dataSetFolder);
    }

    public DataSet<ImageDataSetInformation> getOriginalDataset()
    {
        return originalDataset;
    }

    public void setOriginalDataset(DataSet<ImageDataSetInformation> originalDataset)
    {
        this.originalDataset = originalDataset;
    }

    public List<IDataSet> getThumbnailDatasets()
    {
        return thumbnailDatasets;
    }

    public void setThumbnailDatasets(List<IDataSet> thumbnailDatasets)
    {
        this.thumbnailDatasets = thumbnailDatasets;
    }

    @Override
    public void setSample(ISampleImmutable sampleOrNull)
    {
        super.setSample(sampleOrNull);
        if (originalDataset != null)
        {
            originalDataset.setSample(sampleOrNull);
        }
        for (IDataSet thumbnailDataset : thumbnailDatasets)
        {
            thumbnailDataset.setSample(sampleOrNull);
        }
    }

    @Override
    public void setExperiment(IExperimentImmutable experimentOrNull)
    {
        super.setExperiment(experimentOrNull);
        if (originalDataset != null)
        {
            originalDataset.setExperiment(experimentOrNull);
        }
        for (IDataSet thumbnailDataset : thumbnailDatasets)
        {
            thumbnailDataset.setExperiment(experimentOrNull);
        }
    }

}
