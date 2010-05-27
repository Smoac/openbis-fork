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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Utility class for loading image datasets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class ImageDatasetLoader extends PlateDatasetLoader
{
    // TODO 2010-05-27, CR : See PlateDatasetLoader todo comment

    ImageDatasetLoader(Session session, IScreeningBusinessObjectFactory businessObjectFactory,
            String dataStoreBaseURL, List<? extends PlateIdentifier> plates)
    {
        super(session, businessObjectFactory, dataStoreBaseURL, plates,
                ScreeningConstants.IMAGE_DATASET_TYPE);
    }

    /**
     * Return the image datasets for the specified plates.
     */
    public List<ImageDatasetReference> getImageDatasets()
    {
        load();
        return asImageDatasets();
    }

    private List<ImageDatasetReference> asImageDatasets()
    {
        List<ImageDatasetReference> result = new ArrayList<ImageDatasetReference>();
        for (ExternalData externalData : getDatasets())
        {
            result.add(asImageDataset(externalData));
        }
        return result;
    }

    protected ImageDatasetReference asImageDataset(ExternalData externalData)
    {
        DataStore dataStore = externalData.getDataStore();
        return new ImageDatasetReference(externalData.getCode(), dataStore.getDownloadUrl(),
                createPlateIdentifier(externalData), extractPlateGeometry(externalData),
                externalData.getRegistrationDate());
    }
}
