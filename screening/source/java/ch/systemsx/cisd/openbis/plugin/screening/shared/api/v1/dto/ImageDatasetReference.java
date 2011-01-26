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
import java.util.Map;

/**
 * Description of one image dataset.
 * 
 * @author Tomasz Pylak
 */
public class ImageDatasetReference extends DatasetReference implements IImageDatasetIdentifier
{
    private static final long serialVersionUID = 1L;

    @Deprecated
    public ImageDatasetReference(String datasetCode, String datastoreServerUrl,
            PlateIdentifier plate)
    {
        super(datasetCode, datastoreServerUrl, plate);
    }

    @Deprecated
    public ImageDatasetReference(String datasetCode, String datastoreServerUrl,
            PlateIdentifier plate, Geometry plateGemoetry, Date registrationDate)
    {
        super(datasetCode, datastoreServerUrl, plate, plateGemoetry, registrationDate);
    }

    public ImageDatasetReference(String datasetCode, String datastoreServerUrl,
            PlateIdentifier plate, ExperimentIdentifier experimentIdentifier,
            Geometry plateGemoetry, Date registrationDate, Map<String, String> propertiesOrNull)
    {
        super(datasetCode, datastoreServerUrl, plate, experimentIdentifier, plateGemoetry,
                registrationDate, propertiesOrNull);
    }
}
