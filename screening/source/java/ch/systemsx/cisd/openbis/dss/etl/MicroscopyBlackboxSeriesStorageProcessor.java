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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ChannelDescription;

/**
 * Storage processor which stores microscopy images in a special-purpose imaging database. Image
 * files do not have to adhere to any naming convention, it's assumed that there is exactly one
 * tile, one channel, no time or depth dimensions. Images are sorted lexicographically and
 * subsequent series numbers are assigned to them.
 * <p>
 * In this storage processor one can set neither well geometry, channels nor image file extractor.<br>
 * See {@link AbstractImageStorageProcessor} documentation to check how to configure thumbnails
 * generation.
 * 
 * @author Tomasz Pylak
 */
public class MicroscopyBlackboxSeriesStorageProcessor extends AbstractImageStorageProcessor
{
    private static final String DEFAULT_CHANNEL_CODE = "DEFAULT";

    private static final String DEFAULT_CHANNEL_LABEL = "Default";

    private static final Location DEFAULT_TILE = new Location(1, 1);

    private static final Geometry DEFAULT_WELL_GEOMETRY = new Geometry(1, 1);

    private static final List<ChannelDescription> DEFAULT_CHANNELS = Arrays
            .asList(new ChannelDescription(DEFAULT_CHANNEL_CODE, DEFAULT_CHANNEL_LABEL));

    public MicroscopyBlackboxSeriesStorageProcessor(Properties properties)
    {
        super(DEFAULT_WELL_GEOMETRY, DEFAULT_CHANNELS, new BlackboxSeriesImageFileExtractor(
                properties), properties);
    }

    private static class BlackboxSeriesImageFileExtractor extends AbstractImageFileExtractor
    {
        protected BlackboxSeriesImageFileExtractor(Properties properties)
        {
            super(DEFAULT_CHANNELS, DEFAULT_WELL_GEOMETRY, true, properties);
        }

        @Override
        protected ImageFileInfo tryExtractImageInfo(File imageFile, File incomingDataSetDirectory,
                SampleIdentifier datasetSample)
        {
            String imageRelativePath = getRelativeImagePath(incomingDataSetDirectory, imageFile);
            // we postpone assigning series numbers until all images are extracted
            return new ImageFileInfo(null, DEFAULT_CHANNEL_CODE, DEFAULT_TILE, imageRelativePath,
                    null, null, null);
        }
    }

    @Override
    protected void storeInDatabase(IImagingQueryDAO dao, DataSetInformation dataSetInformation,
            ImageFileExtractionResult extractedImages)
    {
        List<AcquiredSingleImage> images = extractedImages.getImages();
        setSeriesNumber(images);
        MicroscopyImageDatasetInfo dataset =
                createMicroscopyImageDatasetInfo(dataSetInformation, images);

        MicroscopyImageDatasetUploader.upload(dao, dataset, images, extractedImages.getChannels());
    }

    private void setSeriesNumber(List<AcquiredSingleImage> images)
    {
        Collections.sort(images, createPathComparator());
        int seriesNumber = 1;
        for (AcquiredSingleImage image : images)
        {
            image.setSeriesNumber(seriesNumber++);
        }
    }

    private Comparator<AcquiredSingleImage> createPathComparator()
    {
        return new Comparator<AcquiredSingleImage>()
            {
                public int compare(AcquiredSingleImage o1, AcquiredSingleImage o2)
                {
                    return getPath(o1).compareTo(getPath(o2));
                }

                private String getPath(AcquiredSingleImage o1)
                {
                    return o1.getImageReference().getRelativeImagePath();
                }
            };
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
