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

package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.util.Date;

/**
 * Description of one feature vector dataset.
 * 
 * @author Tomasz Pylak
 */
public class FeatureVectorDatasetReference extends DatasetReference implements
        IFeatureVectorDatasetIdentifier
{
    private static final long serialVersionUID = 1L;

    private final IImageDatasetIdentifier imageDatasetIdentifier;

    public FeatureVectorDatasetReference(String datasetCode, String datastoreServerUrl,
            PlateIdentifier plate, Geometry plateGeometry, Date registrationDate,
            IImageDatasetIdentifier imageDatasetIdentifier)
    {
        super(datasetCode, datastoreServerUrl, plate, plateGeometry, registrationDate);
        this.imageDatasetIdentifier = imageDatasetIdentifier;
    }

    /** parent image dataset which has been analyzed to obtain the feature vectors */
    public IImageDatasetIdentifier getParentImageDataset()
    {
        return imageDatasetIdentifier;
    }

    @Override
    public String toString()
    {
        return super.toString() + " from image dataset " + imageDatasetIdentifier.getDatasetCode();
    }
}
