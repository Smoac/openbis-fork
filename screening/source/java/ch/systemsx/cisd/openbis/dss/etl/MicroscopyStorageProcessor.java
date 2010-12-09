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

package ch.systemsx.cisd.openbis.dss.etl;

import java.io.File;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Storage processor which stores microscopy images in a special-purpose imaging database.
 * <p>
 * See {@link AbstractImageStorageProcessor} documentation.
 * 
 * @author Tomasz Pylak
 */
public class MicroscopyStorageProcessor extends AbstractImageStorageProcessor
{

    public MicroscopyStorageProcessor(Properties properties)
    {
        super(properties);
        if (imageFileExtractor == null)
        {
            throw ConfigurationFailureException
                    .fromTemplate("Image file extractor property is not configured: "
                            + FILE_EXTRACTOR_PROPERTY);
        }
    }

    @Override
    protected void storeInDatabase(IImagingQueryDAO dao, DataSetInformation dataSetInformation,
            ImageFileExtractionResult extractedImages)
    {
        List<AcquiredSingleImage> images = extractedImages.getImages();
        MicroscopyImageDatasetInfo dataset =
                createMicroscopyImageDatasetInfo(dataSetInformation, images);

        MicroscopyImageDatasetUploader.upload(dao, dataset, images, extractedImages.getChannels());
    }

    private MicroscopyImageDatasetInfo createMicroscopyImageDatasetInfo(
            DataSetInformation dataSetInformation, List<AcquiredSingleImage> images)
    {
        boolean hasImageSeries = hasImageSeries(images);
        return new MicroscopyImageDatasetInfo(dataSetInformation.getDataSetCode(),
                spotGeometry.getRows(), spotGeometry.getColumns(), hasImageSeries);
    }

    @Override
    protected void validateImages(DataSetInformation dataSetInformation, IMailClient mailClient,
            File incomingDataSetDirectory, ImageFileExtractionResult extractionResult)
    {
        // do nothing - for now we do not have good examples of real data
    }

}
