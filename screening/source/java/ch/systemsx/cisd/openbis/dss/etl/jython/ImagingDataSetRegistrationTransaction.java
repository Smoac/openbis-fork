/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.jython;

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.MICROSCOPY_CONTAINER_TYPE_SUBSTRING;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants.MICROSCOPY_IMAGE_TYPE_SUBSTRING;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.io.FileBasedContentNode;
import ch.systemsx.cisd.etlserver.registrator.AutoRecoverySettings;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationService;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.etlserver.registrator.api.v1.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.Constants;
import ch.systemsx.cisd.openbis.dss.etl.Hdf5ThumbnailGenerator;
import ch.systemsx.cisd.openbis.dss.etl.Utils;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.FeatureVectorDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageContainerDataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ThumbnailsInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IImageDataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.IImagingDataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ThumbnailsStorageFormat;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.Size;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * 
 *
 * @author jakubs
 */

/**
 * Imaging-specific transaction. Handles image datasets in a special way, other datasets are
 * registered using a standard procedure.
 * <p>
 * Note that this transaction is not parametrized by a concrete {@link DataSetInformation} subclass.
 * It has to deal with {@link ImageDataSetInformation}, {@link FeatureVectorDataSetInformation} and
 * {@link DataSetInformation} at the same time.
 */
@SuppressWarnings("rawtypes")
public class ImagingDataSetRegistrationTransaction extends DataSetRegistrationTransaction implements
        IImagingDataSetRegistrationTransaction
{
    private final IDataSetRegistrationDetailsFactory<ImageDataSetInformation> imageDatasetFactory;

    private final IDataSetRegistrationDetailsFactory<DataSetInformation> imageContainerDatasetFactory;

    private final String originalDirName;

    @SuppressWarnings("unchecked")
    public ImagingDataSetRegistrationTransaction(File rollBackStackParentFolder,
            File workingDirectory, File stagingDirectory,
            DataSetRegistrationService<DataSetInformation> registrationService,
            IDataSetRegistrationDetailsFactory<DataSetInformation> registrationDetailsFactory,
            String originalDirName, AutoRecoverySettings autoRecoverySettings)
    {
        super(rollBackStackParentFolder, workingDirectory, stagingDirectory, registrationService,
                registrationDetailsFactory, autoRecoverySettings);

        assert registrationDetailsFactory instanceof JythonPlateDatasetFactory : "JythonPlateDatasetFactory expected, but got: "
                + registrationDetailsFactory.getClass().getCanonicalName();

        JythonPlateDatasetFactory factory = (JythonPlateDatasetFactory) registrationDetailsFactory;
        this.imageDatasetFactory = factory.imageDatasetFactory;
        this.imageContainerDatasetFactory = factory.imageContainerDatasetFactory;
        this.originalDirName = originalDirName;
    }

    public IImageDataSet createNewImageDataSet(SimpleImageDataConfig imageDataSet,
            File incomingFolderWithImages)
    {
        DataSetRegistrationDetails<ImageDataSetInformation> details =
                SimpleImageDataSetRegistrator.createImageDatasetDetails(imageDataSet,
                        incomingFolderWithImages, imageDatasetFactory);
        return createNewImageDataSet(details);
    }

    /**
     * Creates container dataset which contains dataset with original images (created on the fly).
     * If thumbnails are required they are generated and moved to a thumbnail dataset which becomes
     * a part of the container as well.
     * <p>
     * The original images dataset is special - it contains description of what should be saved in
     * imaging database by the storage processor.
     * 
     * @return container dataset.
     */
    public IImageDataSet createNewImageDataSet(
            DataSetRegistrationDetails<ImageDataSetInformation> imageRegistrationDetails)
    {
        ImageDataSetInformation imageDataSetInformation =
                imageRegistrationDetails.getDataSetInformation();
        ImageDataSetStructure imageDataSetStructure =
                imageDataSetInformation.getImageDataSetStructure();
        File incomingDirectory = imageDataSetInformation.getIncomingDirectory();
        List<String> containedDataSetCodes = new ArrayList<String>();

        // Compute the bounding box of the images -- needs to happen before thumbnail
        // generation, since thumbnails may want to know the bounding box
        calculateBoundingBox(imageDataSetInformation, imageDataSetStructure, incomingDirectory);

        // create thumbnails dataset if needed
        List<IDataSet> thumbnailDatasets = new ArrayList<IDataSet>();
        boolean generateThumbnails = imageDataSetStructure.areThumbnailsGenerated();
        if (generateThumbnails)
        {
            imageDataSetStructure
                    .validateImageRepresentationGenerationParameters(imageDataSetInformation);

            List<ThumbnailsStorageFormat> thumbnailsStorageFormatList =
                    imageDataSetStructure.getImageStorageConfiguraton()
                            .getThumbnailsStorageFormat();

            ThumbnailsInfo thumbnailsInfo = new ThumbnailsInfo();
            for (ThumbnailsStorageFormat thumbnailsStorageFormat : thumbnailsStorageFormatList)
            {
                IDataSet thumbnailDataset =
                        createThumbnailDataset(imageDataSetInformation, thumbnailsStorageFormat);
                thumbnailDatasets.add(thumbnailDataset);

                generateThumbnails(imageDataSetStructure, incomingDirectory, thumbnailDataset,
                        thumbnailsStorageFormat, thumbnailsInfo);
                containedDataSetCodes.add(thumbnailDataset.getDataSetCode());
            }
            imageDataSetInformation.setThumbnailsInfo(thumbnailsInfo);
        }

        // create main dataset (with original images)
        @SuppressWarnings("unchecked")
        DataSet<ImageDataSetInformation> mainDataset =
                (DataSet<ImageDataSetInformation>) super.createNewDataSet(imageRegistrationDetails);
        containedDataSetCodes.add(mainDataset.getDataSetCode());

        for (IDataSet thumbnailDataset : thumbnailDatasets)
        {
            setSameDatasetOwner(mainDataset, thumbnailDataset);
        }
        ImageContainerDataSet containerDataset =
                createImageContainerDataset(mainDataset, imageDataSetInformation,
                        containedDataSetCodes);
        containerDataset.setOriginalDataset(mainDataset);
        for (IDataSet thumbnailDataset : thumbnailDatasets)
        {
            containerDataset.setThumbnailDatasets(Arrays.asList(thumbnailDataset));
        }
        imageDataSetInformation.setContainerDatasetPermId(containerDataset.getDataSetCode());

        return containerDataset;
    }

    private void calculateBoundingBox(ImageDataSetInformation imageDataSetInformation,
            ImageDataSetStructure imageDataSetStructure, File incomingDirectory)
    {
        ImageLibraryInfo imageLibrary =
                imageDataSetStructure.getImageStorageConfiguraton().tryGetImageLibrary();
        List<ImageFileInfo> images = imageDataSetStructure.getImages();
        for (ImageFileInfo imageFileInfo : images)
        {
            File file = new File(incomingDirectory, imageFileInfo.getImageRelativePath());
            Size size =
                    Utils.loadUnchangedImageSize(new FileBasedContentNode(file), null, imageLibrary);
            imageDataSetInformation.setMaximumImageWidth(Math.max(
                    imageDataSetInformation.getMaximumImageWidth(), size.getWidth()));
            imageDataSetInformation.setMaximumImageHeight(Math.max(
                    imageDataSetInformation.getMaximumImageHeight(), size.getHeight()));
        }
    }

    private File prependOriginalDirectory(String directoryPath)
    {
        return new File(originalDirName + File.separator + directoryPath);
    }

    private void generateThumbnails(ImageDataSetStructure imageDataSetStructure,
            File incomingDirectory, IDataSet thumbnailDataset,
            ThumbnailsStorageFormat thumbnailsStorageFormatOrNull, ThumbnailsInfo thumbnailPaths)
    {
        String thumbnailFile;
        if (thumbnailsStorageFormatOrNull == null)
        {
            thumbnailFile =
                    createNewFile(thumbnailDataset, Constants.HDF5_CONTAINER_THUMBNAILS_FILE_NAME);
        } else
        {
            thumbnailFile =
                    createNewFile(thumbnailDataset,
                            thumbnailsStorageFormatOrNull.getThumbnailsFileName());
        }

        Hdf5ThumbnailGenerator.tryGenerateThumbnails(imageDataSetStructure, incomingDirectory,
                thumbnailFile, imageDataSetStructure.getImageStorageConfiguraton(),
                thumbnailDataset.getDataSetCode(), thumbnailsStorageFormatOrNull, thumbnailPaths);
        enhanceWithResolution(thumbnailDataset, thumbnailPaths);
    }

    private static void enhanceWithResolution(IDataSet thumbnailDataset,
            ThumbnailsInfo thumbnailPaths)
    {
        Size size = thumbnailPaths.tryGetDimension(thumbnailDataset.getDataSetCode());
        if (size != null)
        {
            thumbnailDataset.setPropertyValue(ScreeningConstants.RESOLUTION, size.getWidth() + "x"
                    + size.getHeight());
        }
    }

    private IDataSet createThumbnailDataset(ImageDataSetInformation imageDataSetInformation,
            ThumbnailsStorageFormat thumbnailsStorageFormat)
    {
        String thumbnailsDatasetTypeCode = findThumbnailsDatasetTypeCode(imageDataSetInformation);
        IDataSet thumbnailDataset = createNewDataSet(thumbnailsDatasetTypeCode);
        thumbnailDataset.setFileFormatType(thumbnailsStorageFormat.getFileFormat()
                .getOpenBISFileType());
        thumbnailDataset.setMeasuredData(false);

        return thumbnailDataset;
    }

    private ImageContainerDataSet createImageContainerDataset(IDataSet mainDataset,
            ImageDataSetInformation imageDataSetInformation, List<String> containedDataSetCodes)
    {
        String containerDatasetTypeCode = findContainerDatasetTypeCode(imageDataSetInformation);
        @SuppressWarnings("unchecked")
        ImageContainerDataSet containerDataset =
                (ImageContainerDataSet) createNewDataSet(imageContainerDatasetFactory,
                        containerDatasetTypeCode);
        setSameDatasetOwner(mainDataset, containerDataset);
        moveDatasetRelations(mainDataset, containerDataset);

        containerDataset.setContainedDataSetCodes(containedDataSetCodes);
        return containerDataset;
    }

    // Copies properties and relations to datasets from the main dataset to the container and
    // resets them in the main dataset.
    private static void moveDatasetRelations(IDataSet mainDataset, IDataSet containerDataset)
    {
        containerDataset.setParentDatasets(mainDataset.getParentDatasets());
        mainDataset.setParentDatasets(Collections.<String> emptyList());

        for (String propertyCode : mainDataset.getAllPropertyCodes())
        {
            containerDataset.setPropertyValue(propertyCode,
                    mainDataset.getPropertyValue(propertyCode));
            mainDataset.setPropertyValue(propertyCode, null);
        }
    }

    private static boolean isHCSImageDataSetType(String mainDatasetTypeCode)
    {
        String prefix = ScreeningConstants.HCS_IMAGE_DATASET_TYPE_PREFIX;
        if (mainDatasetTypeCode.startsWith(prefix))
        {
            if (mainDatasetTypeCode
                    .contains(ScreeningConstants.IMAGE_CONTAINER_DATASET_TYPE_MARKER))
            {
                throw UserFailureException
                        .fromTemplate(
                                "The specified image dataset type '%s' should not be of container type, but contains '%s' in the type code.",
                                mainDatasetTypeCode,
                                ScreeningConstants.IMAGE_CONTAINER_DATASET_TYPE_MARKER);
            }
            return true;
        } else
        {
            return false;
        }
    }

    private static boolean isMicroscopyImageDataSetType(String dataSetTypeCode)
    {
        return dataSetTypeCode.contains(MICROSCOPY_IMAGE_TYPE_SUBSTRING)
                && false == dataSetTypeCode.contains(MICROSCOPY_CONTAINER_TYPE_SUBSTRING);
    }

    private static String findContainerDatasetTypeCode(
            ImageDataSetInformation imageDataSetInformation)
    {
        String dataSetTypeCode = imageDataSetInformation.getDataSetType().getCode().toUpperCase();
        String prefix = ScreeningConstants.HCS_IMAGE_DATASET_TYPE_PREFIX;
        if (isHCSImageDataSetType(dataSetTypeCode))
        {
            return prefix + ScreeningConstants.IMAGE_CONTAINER_DATASET_TYPE_MARKER
                    + dataSetTypeCode.substring(prefix.length());
        } else if (isMicroscopyImageDataSetType(dataSetTypeCode))
        {
            return dataSetTypeCode.replace(MICROSCOPY_IMAGE_TYPE_SUBSTRING,
                    MICROSCOPY_CONTAINER_TYPE_SUBSTRING);
        } else
        {
            throw UserFailureException
                    .fromTemplate(
                            "The image dataset type '%s' is neither standard HCS type (starts with '%s') nor a microscopy type (contains '%s').",
                            dataSetTypeCode, prefix,
                            ScreeningConstants.MICROSCOPY_IMAGE_SAMPLE_TYPE_PATTERN);
        }
    }

    private static String findThumbnailsDatasetTypeCode(
            ImageDataSetInformation imageDataSetInformation)
    {
        String dataSetTypeCode = imageDataSetInformation.getDataSetType().getCode().toUpperCase();

        if (isHCSImageDataSetType(dataSetTypeCode))
        {
            return ScreeningConstants.HCS_IMAGE_DATASET_TYPE_PREFIX
                    + ScreeningConstants.IMAGE_THUMBNAIL_DATASET_TYPE_MARKER;
        } else if (isMicroscopyImageDataSetType(dataSetTypeCode))
        {
            return dataSetTypeCode.replace(ScreeningConstants.MICROSCOPY_IMAGE_TYPE_SUBSTRING,
                    ScreeningConstants.MICROSCOPY_THUMBNAIL_TYPE_SUBSTRING);
        } else
        {
            throw UserFailureException
                    .fromTemplate(
                            "The image dataset type '%s' is neither standard HCS type (starts with '%s') nor a microscopy type (contains '%s').",
                            dataSetTypeCode, ScreeningConstants.HCS_IMAGE_DATASET_TYPE_PREFIX,
                            ScreeningConstants.MICROSCOPY_IMAGE_SAMPLE_TYPE_PATTERN);
        }
    }

    private static void setSameDatasetOwner(IDataSet templateDataset, IDataSet destinationDataset)
    {
        destinationDataset.setExperiment(templateDataset.getExperiment());
        destinationDataset.setSample(templateDataset.getSample());

    }

    @SuppressWarnings(
        { "cast", "unchecked" })
    @Override
    public IDataSet createNewDataSet(DataSetRegistrationDetails registrationDetails)
    {
        if (registrationDetails.getDataSetInformation() instanceof ImageDataSetInformation)
        {
            DataSetRegistrationDetails<ImageDataSetInformation> imageRegistrationDetails =
                    (DataSetRegistrationDetails<ImageDataSetInformation>) registrationDetails;
            return createNewImageDataSet(imageRegistrationDetails);
        } else
        {
            return super.createNewDataSet(registrationDetails);
        }
    }

    /**
     * If we are dealing with the image dataset container then the move operation is delegated to
     * the original dataset. Otherwise a default implementation is used.
     */
    @Override
    public String moveFile(String src, IDataSet dst)
    {
        return moveFile(src, dst, new File(src).getName());
    }

    /**
     * If we are dealing with the image dataset container then the move operation is delegated to
     * the original dataset. Otherwise a default implementation is used.
     */
    @Override
    public String moveFile(String src, IDataSet dst, String dstInDataset)
    {
        ImageContainerDataSet imageContainerDataset = tryAsImageContainerDataset(dst);
        if (imageContainerDataset != null)
        {
            String destination = dstInDataset;
            if (destination.startsWith(originalDirName) == false)
            {
                destination = prependOriginalDirectory(destination).getPath();
            }
            DataSet<ImageDataSetInformation> originalDataset =
                    imageContainerDataset.getOriginalDataset();
            if (originalDataset == null)
            {
                throw new UserFailureException(
                        "Cannot move the files because the original dataset is missing: " + src);
            }
            originalDataset.getRegistrationDetails().getDataSetInformation()
                    .setDatasetRelativeImagesFolderPath(new File(destination));

            return super.moveFile(src, originalDataset, destination);
        } else
        {
            return super.moveFile(src, dst, dstInDataset);
        }
    }

    private static ImageContainerDataSet tryAsImageContainerDataset(IDataSet dataset)
    {
        if (dataset instanceof ImageContainerDataSet)
        {
            return (ImageContainerDataSet) dataset;
        } else
        {
            return null;
        }
    }

}
