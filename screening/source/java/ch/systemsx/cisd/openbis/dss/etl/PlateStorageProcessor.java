/*
 * Copyright 2007 ETH Zuerich, CISD
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageZoomLevel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ThumbnailsInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ThumbnailsStorageFormat.FileFormat;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * Storage processor which stores HCS plate images in a special-purpose imaging database.
 * <p>
 * See {@link AbstractImageStorageProcessor} documentation.
 * 
 * @author Tomasz Pylak
 */
public final class PlateStorageProcessor extends AbstractImageStorageProcessor
{
    /**
     * Optional boolean property. Defines if all image datasets in one experiment have the same
     * channels or if each imported dataset can have different channels. By default true.
     */
    static final String CHANNELS_PER_EXPERIMENT_PROPERTY = "define-channels-per-experiment";

    /**
     * Optional boolean property. If true an email is sent if some images for the uploaded plate are
     * missing. True by default.
     */
    protected static final String NOTIFY_IF_INCOMPLETE_PROPERTY = "notify-if-incomplete";

    // ---

    // can be overwritten for each dataset
    private final boolean globalStoreChannelsOnExperimentLevel;

    private final boolean notifyIfPlateIncomplete;

    public PlateStorageProcessor(Properties properties)
    {
        super(properties);
        this.globalStoreChannelsOnExperimentLevel =
                PropertyUtils.getBoolean(properties, CHANNELS_PER_EXPERIMENT_PROPERTY, true);
        this.notifyIfPlateIncomplete =
                PropertyUtils.getBoolean(properties, NOTIFY_IF_INCOMPLETE_PROPERTY, true);
    }

    public static class DatasetOwnerInformation extends AbstractHashable
    {
        public static DatasetOwnerInformation create(DataSetInformation dataSetInformation)
        {
            return new DatasetOwnerInformation(dataSetInformation.getDataSetCode(),
                    dataSetInformation);
        }

        private final Experiment experiment;

        private final SampleIdentifier sampleIdentifier;

        private final ExperimentIdentifier experimentIdentifier;

        private final Sample sample;

        private final String dataSetCode;

        private final IEntityProperty[] sampleProperties;

        protected DatasetOwnerInformation(String dataSetCode, DataSetInformation dataSetOwner)
        {
            this(dataSetCode, dataSetOwner.tryToGetSample(), dataSetOwner.getSampleIdentifier(),
                    dataSetOwner.getProperties(), dataSetOwner.tryToGetExperiment(), dataSetOwner
                            .getExperimentIdentifier());
        }

        private DatasetOwnerInformation(String dataSetCode, Sample sample,
                SampleIdentifier sampleIdentifier, IEntityProperty[] sampleProperties,
                Experiment experiment, ExperimentIdentifier experimentIdentifier)
        {
            this.dataSetCode = dataSetCode;
            this.sample = sample;
            this.sampleIdentifier = sampleIdentifier;
            this.sampleProperties = sampleProperties;
            this.experiment = experiment;
            this.experimentIdentifier = experimentIdentifier;
        }

        public String getDataSetCode()
        {
            return dataSetCode;
        }

        public SampleIdentifier getSampleIdentifier()
        {
            return sampleIdentifier;
        }

        public Sample tryGetSample()
        {
            return sample;
        }

        public IEntityProperty[] getSampleProperties()
        {
            return sampleProperties;
        }

        public Experiment tryGetExperiment()
        {
            return experiment;
        }

        public ExperimentIdentifier getExperimentIdentifier()
        {
            return experimentIdentifier;
        }
    }

    public static class ImageDatasetOwnerInformation extends DatasetOwnerInformation
    {
        public static ImageDatasetOwnerInformation create(String containerDatasetPermId,
                DataSetInformation originalDataset, ThumbnailsInfo thumbnailsInfosOrNull)
        {
            return new ImageDatasetOwnerInformation(containerDatasetPermId, originalDataset,
                    thumbnailsInfosOrNull);
        }

        private final List<ImageZoomLevel> imageZoomLevels;

        private ImageDatasetOwnerInformation(String containerDatasetPermId,
                DataSetInformation originalDataset, ThumbnailsInfo thumbnailsInfosOrNull)
        {
            super(containerDatasetPermId, originalDataset);
            this.imageZoomLevels = createZoomLevels(originalDataset, thumbnailsInfosOrNull);
        }

        private static List<ImageZoomLevel> createZoomLevels(DataSetInformation originalDataset,
                ThumbnailsInfo thumbnailsInfosOrNull)
        {
            List<ImageZoomLevel> zoomLevels = new ArrayList<ImageZoomLevel>();

            ImageZoomLevel originalZoomLevel =
                    new ImageZoomLevel(originalDataset.getDataSetCode(), true,
                            StringUtils.EMPTY_STRING, null, null, null, null);
            zoomLevels.add(originalZoomLevel);
            if (thumbnailsInfosOrNull != null)
            {
                for (String permId : thumbnailsInfosOrNull.getThumbnailPhysicalDatasetsPermIds())
                {
                    Integer width = null, height = null;
                    Size dimension = thumbnailsInfosOrNull.tryGetDimension(permId);
                    if (dimension != null)
                    {
                        width = dimension.getWidth();
                        height = dimension.getHeight();
                    }
                    String fileTypeString = null;
                    FileFormat fileType = thumbnailsInfosOrNull.getFileType(permId);
                    if (fileType != null)
                    {
                        fileTypeString = fileType.getFileExtension();
                    }
                    String rootPath = thumbnailsInfosOrNull.getRootPath(permId);

                    // TODO add color depth
                    ImageZoomLevel thumbnailZoomLevel =
                            new ImageZoomLevel(permId, false, rootPath, width, height, null,
                                    fileTypeString);
                    zoomLevels.add(thumbnailZoomLevel);
                }
            }
            return zoomLevels;
        }

        public List<ImageZoomLevel> getImageZoomLevels()
        {
            return imageZoomLevels;
        }
    }

    @Override
    protected boolean validateImages(DatasetOwnerInformation dataSetInformation,
            IMailClient mailClient, File incomingDataSetDirectory,
            ImageFileExtractionResult extractionResult)
    {
        ImageValidator validator =
                new ImageValidator(dataSetInformation, mailClient, incomingDataSetDirectory,
                        extractionResult, operationLog, notificationLog, notifyIfPlateIncomplete);
        return validator.validateImages();
    }

    private void checkDataSetInformation(final DatasetOwnerInformation dataSetInformation)
    {
        assert dataSetInformation != null : "Unspecified data set information";
        assert dataSetInformation.getSampleIdentifier() != null : "Unspecified sample identifier";

        final ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier experimentIdentifier =
                dataSetInformation.getExperimentIdentifier();
        assert experimentIdentifier != null : "Unspecified experiment identifier";
        assert dataSetInformation.tryGetExperiment() != null : "experiment not set";
        checkExperimentIdentifier(experimentIdentifier);
    }

    private final static void checkExperimentIdentifier(
            final ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier experimentIdentifier)
    {
        assert experimentIdentifier.getSpaceCode() != null : "Space code is null";
        assert experimentIdentifier.getExperimentCode() != null : "Experiment code is null";
        assert experimentIdentifier.getProjectCode() != null : "Project code is null";
    }

    @Override
    protected void storeInDatabase(IImagingQueryDAO dao,
            ImageDatasetOwnerInformation dataSetInformation,
            ImageFileExtractionResult extractedImages)
    {
        checkDataSetInformation(dataSetInformation);

        Experiment experiment = dataSetInformation.tryGetExperiment();
        assert experiment != null : "experiment is null";
        List<AcquiredSingleImage> images = extractedImages.getImages();
        boolean storeChannelsOnExperimentLevel = globalStoreChannelsOnExperimentLevel;
        if (extractedImages.tryStoreChannelsOnExperimentLevel() != null)
        {
            storeChannelsOnExperimentLevel = extractedImages.tryStoreChannelsOnExperimentLevel();
        }
        HCSImageDatasetInfo info =
                createImageDatasetInfo(experiment, dataSetInformation, images,
                        extractedImages.getTileGeometry(), extractedImages.tryGetImageLibrary(),
                        storeChannelsOnExperimentLevel);

        HCSImageDatasetUploader.upload(dao, info, images, extractedImages.getChannels());
    }

    private HCSImageDatasetInfo createImageDatasetInfo(Experiment experiment,
            ImageDatasetOwnerInformation dataSetInformation,
            List<AcquiredSingleImage> acquiredImages, Geometry tileGeometry,
            ImageLibraryInfo imageLibraryInfoOrNull, boolean storeChannelsOnExperimentLevel)
    {
        HCSContainerDatasetInfo info =
                HCSContainerDatasetInfo.createScreeningDatasetInfo(dataSetInformation);
        boolean hasImageSeries = hasImageSeries(acquiredImages);
        ImageDatasetInfo imageDatasetInfo =
                new ImageDatasetInfo(tileGeometry.getRows(), tileGeometry.getColumns(),
                        hasImageSeries, imageLibraryInfoOrNull,
                        dataSetInformation.getImageZoomLevels());
        return new HCSImageDatasetInfo(info, imageDatasetInfo, storeChannelsOnExperimentLevel);
    }

}
