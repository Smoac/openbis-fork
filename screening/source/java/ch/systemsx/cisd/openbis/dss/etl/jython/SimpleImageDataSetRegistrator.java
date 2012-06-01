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

package ch.systemsx.cisd.openbis.dss.etl.jython;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.image.IntensityRescaling;
import ch.systemsx.cisd.common.image.IntensityRescaling.Levels;
import ch.systemsx.cisd.common.image.IntensityRescaling.PixelHistogram;
import ch.systemsx.cisd.common.io.FileBasedContentNode;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ImageReaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetStructure;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageIdentifier;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageMetadata;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.Location;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations.ImageTransformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations.ImageTransformationBuffer;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.CodeNormalizer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;

/**
 * Allows to prepare the image dataset which should be registered easily using the specified
 * {@link SimpleImageDataConfig}.
 * 
 * @author Tomasz Pylak
 */
public class SimpleImageDataSetRegistrator
{
    private static final String OPTIMAL_DATASET_INTENSITY_RESCALING_DESCRIPTION =
            "Optimal intensity rescaling for a series of images. "
                    + "It allows to compare images of one plate's dataset to each other."
                    + "At the same time it causes that the conversion to 8 bit color depth looses less information, "
                    + "especially when images use only a small part of available intensities range.";

    @Private
    static interface IImageReaderFactory
    {
        IImageReader tryGetReader(String libraryName, String readerName);

        IImageReader tryGetReaderForFile(String libraryName, String fileName);
    }

    private static class ImageTokensWithPath extends ImageMetadata
    {
        /** path relative to the incoming dataset directory */
        private String imageRelativePath;

        public ImageTokensWithPath(ImageMetadata imageTokens, String imageRelativePath)
        {
            setWell(imageTokens.getWell());
            setChannelCode(imageTokens.getChannelCode());
            setTileNumber(imageTokens.getTileNumber());
            setDepth(imageTokens.tryGetDepth());
            setTimepoint(imageTokens.tryGetTimepoint());
            setSeriesNumber(imageTokens.tryGetSeriesNumber());
            setImageIdentifier(imageTokens.tryGetImageIdentifier());
            this.imageRelativePath = imageRelativePath;
        }

        public String getImagePath()
        {
            return imageRelativePath;
        }
    }

    public static DataSetRegistrationDetails<ImageDataSetInformation> createImageDatasetDetails(
            SimpleImageDataConfig simpleImageConfig, File incoming,
            IDataSetRegistrationDetailsFactory<ImageDataSetInformation> factory)
    {
        return createImageDatasetDetails(simpleImageConfig, incoming, factory,
                new IImageReaderFactory()
                    {
                        @Override
                        public IImageReader tryGetReaderForFile(String libraryName, String fileName)
                        {
                            return ImageReaderFactory.tryGetReaderForFile(libraryName, fileName);
                        }

                        @Override
                        public IImageReader tryGetReader(String libraryName, String readerName)
                        {
                            return ImageReaderFactory.tryGetReader(libraryName, readerName);
                        }
                    });
    }

    @Private
    static DataSetRegistrationDetails<ImageDataSetInformation> createImageDatasetDetails(
            SimpleImageDataConfig simpleImageConfig, File incoming,
            IDataSetRegistrationDetailsFactory<ImageDataSetInformation> factory,
            IImageReaderFactory readerFactory)
    {
        SimpleImageDataSetRegistrator registrator =
                new SimpleImageDataSetRegistrator(simpleImageConfig, readerFactory);
        return registrator.createImageDatasetDetails(incoming, factory);
    }

    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            SimpleImageDataSetRegistrator.class);

    private final SimpleImageDataConfig simpleImageConfig;

    private final IImageReaderFactory readerFactory;

    private SimpleImageDataSetRegistrator(SimpleImageDataConfig simpleImageConfig,
            IImageReaderFactory readerFactory)
    {
        this.simpleImageConfig = simpleImageConfig;
        this.readerFactory = readerFactory;
    }

    private DataSetRegistrationDetails<ImageDataSetInformation> createImageDatasetDetails(
            File incoming,
            IDataSetRegistrationDetailsFactory<ImageDataSetInformation> imageDatasetFactory)
    {
        DataSetRegistrationDetails<ImageDataSetInformation> registrationDetails =
                imageDatasetFactory.createDataSetRegistrationDetails();
        ImageDataSetInformation imageDataset = registrationDetails.getDataSetInformation();
        setImageDataset(incoming, imageDataset);
        setRegistrationDetails(registrationDetails, imageDataset);
        return registrationDetails;
    }

    /**
     * Finds all images in the directory.
     */
    private List<File> listImageFiles(final File incoming)
    {
        String[] extensions = simpleImageConfig.getRecognizedImageExtensions();
        if (incoming.isFile())
        {
            List<File> list = new ArrayList<File>();
            if (extensionMatches(incoming, extensions))
            {
                list.add(incoming);
            }
            return list;
        } else
        {
            return FileOperations.getInstance().listFiles(incoming, extensions, true);
        }
    }

    private boolean extensionMatches(final File incoming, String[] extensions)
    {
        if (extensions == null || extensions.length == 0)
        {
            return true;
        }
        String fileExt = FilenameUtils.getExtension(incoming.getName());
        if (fileExt == null)
        {
            fileExt = "";
        }
        for (String ext : extensions)
        {
            if (ext.equalsIgnoreCase(fileExt))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Tokenizes file names of all images in the directory.
     */
    protected List<ImageTokensWithPath> parseImageTokens(List<File> imageFiles,
            File incomingDirectory, IImageReader imageReaderOrNull)
    {
        List<ImageTokensWithPath> imageTokensList = new ArrayList<ImageTokensWithPath>();

        for (File imageFile : imageFiles)
        {
            File file = new File(imageFile.getPath());
            List<ImageIdentifier> identifiers = getImageIdentifiers(imageReaderOrNull, file);
            String imageRelativePath = FileUtilities.getRelativeFilePath(incomingDirectory, file);
            ImageMetadata[] imageTokens =
                    simpleImageConfig.extractImagesMetadata(imageRelativePath, identifiers);
            for (ImageMetadata imageToken : imageTokens)
            {
                imageToken.ensureValid(simpleImageConfig.isMicroscopyData());
                imageTokensList.add(new ImageTokensWithPath(imageToken, imageRelativePath));
            }
        }
        if (imageTokensList.isEmpty())
        {
            throw UserFailureException
                    .fromTemplate(
                            "No image tokens could be parsed from incoming directory '%s' for extensions %s!",
                            incomingDirectory.getPath(),
                            CollectionUtils.abbreviate(
                                    simpleImageConfig.getRecognizedImageExtensions(), -1));
        }
        return imageTokensList;
    }

    private List<File> extractImageFiles(File incoming)
    {
        List<File> imageFiles = listImageFiles(incoming);
        if (imageFiles.isEmpty())
        {
            throw UserFailureException.fromTemplate(
                    "Incoming directory '%s' contains no images with extensions %s!", incoming
                            .getPath(), CollectionUtils.abbreviate(
                            simpleImageConfig.getRecognizedImageExtensions(), -1));
        }
        return imageFiles;
    }

    // this method can have a side effect - it autodetects the image reader if only the library is
    // specified
    private IImageReader tryCreateAndSaveImageReader(List<File> imageFiles)
    {
        IImageReader readerOrNull = null;
        ImageLibraryInfo imageLibraryInfoOrNull = tryGetImageLibrary();
        if (imageLibraryInfoOrNull != null)
        {
            readerOrNull = tryCreateImageReader(imageFiles, imageLibraryInfoOrNull);
            if (readerOrNull != null)
            {
                // NOTE: ugly side effect which is used later on
            //    if (null == imageLibraryInfoOrNull.getReaderName())
            //    {
                    imageLibraryInfoOrNull.setReaderName(readerOrNull.getName());
            //    }
            } else
            {
                throw ConfigurationFailureException.fromTemplate(
                        "Cannot find any reader for '%s' library.", imageLibraryInfoOrNull);
            }
        }
        return readerOrNull;
    }

    private IImageReader tryCreateImageReader(List<File> imageFiles,
            ImageLibraryInfo imageLibraryInfo)
    {
        if (imageFiles.isEmpty())
        {
            return null;
        }
        File imageFile = imageFiles.get(0);
        String libraryName = imageLibraryInfo.getName();
        String readerNameOrNull = imageLibraryInfo.getReaderName();
        if (readerNameOrNull != null)
        {
            return readerFactory.tryGetReader(libraryName, readerNameOrNull);
        } else
        {
            return readerFactory.tryGetReaderForFile(libraryName, imageFile.getPath());
        }
    }

    private ImageLibraryInfo tryGetImageLibrary()
    {
        return simpleImageConfig.getImageStorageConfiguration().tryGetImageLibrary();
    }

    private static List<ImageIdentifier> getImageIdentifiers(IImageReader readerOrNull,
            File imageFile)
    {
        List<ImageIdentifier> ids = new ArrayList<ImageIdentifier>();
        if (readerOrNull == null)
        {
            ids.add(ImageIdentifier.NULL);
        } else
        {
            List<ImageID> imageIDs = readerOrNull.getImageIDs(imageFile);
            for (ImageID imageID : imageIDs)
            {
                ids.add(new ImageIdentifier(imageID.getSeriesIndex(), imageID.getTimeSeriesIndex(),
                        imageID.getFocalPlaneIndex(), imageID.getColorChannelIndex()));
            }
        }
        Collections.sort(ids);
        return ids;
    }

    /**
     * Creates ImageFileInfo for a given path to an image.
     */
    protected ImageFileInfo createImageInfo(ImageTokensWithPath imageTokens, Geometry tileGeometry)
    {
        Location tileCoords =
                simpleImageConfig.getTileCoordinates(imageTokens.getTileNumber(), tileGeometry);
        ImageFileInfo img =
                new ImageFileInfo(imageTokens.getChannelCode(), tileCoords.getRow(),
                        tileCoords.getColumn(), imageTokens.getImagePath());
        img.setTimepoint(imageTokens.tryGetTimepoint());
        img.setDepth(imageTokens.tryGetDepth());
        img.setSeriesNumber(imageTokens.tryGetSeriesNumber());
        img.setWell(imageTokens.getWell());
        img.setImageIdentifier(imageTokens.tryGetImageIdentifier());
        return img;
    }

    /**
     * @param imageTokensList list of ImageTokens for each image
     * @param tileGeometry describes the matrix of tiles (aka fields or sides) in the well
     */
    protected List<ImageFileInfo> createImageInfos(List<ImageTokensWithPath> imageTokensList,
            Geometry tileGeometry)
    {
        List<ImageFileInfo> images = new ArrayList<ImageFileInfo>();
        for (ImageTokensWithPath imageTokens : imageTokensList)
        {
            ImageFileInfo image = createImageInfo(imageTokens, tileGeometry);
            images.add(image);
        }
        return images;
    }

    private List<Channel> getAvailableChannels(List<ImageFileInfo> images)
    {
        Set<String> channelCodes = new LinkedHashSet<String>();
        for (ImageFileInfo image : images)
        {
            channelCodes.add(image.getChannelCode());
        }
        List<Channel> channels = new ArrayList<Channel>();
        for (String channelCode : channelCodes)
        {
            Channel channel = simpleImageConfig.createChannel(channelCode);
            channels.add(channel);
        }
        return channels;
    }

    private static int getMaxTileNumber(List<ImageTokensWithPath> imageTokensList)
    {
        int max = 0;
        for (ImageMetadata imageTokens : imageTokensList)
        {
            max = Math.max(max, imageTokens.getTileNumber());
        }
        return max;
    }

    // -------------------

    private void computeAndAppendCommonIntensityRangeTransformation(List<ImageFileInfo> images,
            File incomingDir, List<Channel> channels, IImageReader readerOrNull)
    {
        List<Channel> channelsForComputation =
                tryFindChannelsToComputeCommonIntensityRange(channels);
        if (channelsForComputation == null)
        {
            return;
        }
        for (Channel channel : channelsForComputation)
        {
            String channelCode = channel.getCode();
            List<File> imagePaths = chooseChannelImages(images, incomingDir, channelCode);
            operationLog.info(String.format("Computing intensity range for channel '%s'. "
                    + "Found %d images for the channel in incoming directory '%s'.", channelCode,
                    imagePaths.size(), incomingDir.getName()));
            Levels intensityRange =
                    tryComputeCommonIntensityRange(readerOrNull, imagePaths,
                            simpleImageConfig.getComputeCommonIntensityRangeOfAllImagesThreshold());
            if (intensityRange != null)
            {
                operationLog.info(String.format(
                        "Computed intensity range for channel '%s': %s (incoming directory '%s').",
                        channelCode, intensityRange.toString(), incomingDir.getName()));
                appendCommonIntensityRangeTransformation(channel, intensityRange);
            } else
            {
                operationLog
                        .warn(String
                                .format("Transformation cannot be generated for channel '%s' (incoming directory '%s').",
                                        channelCode, incomingDir.getName()));
            }
        }
    }

    private void appendCommonIntensityRangeTransformation(Channel channel, Levels intensityRange)
    {
        ImageTransformationBuffer buffer = new ImageTransformationBuffer();
        // append first
        String label = simpleImageConfig.getComputeCommonIntensityRangeOfAllImagesLabel();
        ImageTransformation imageTransformation =
                buffer.appendRescaleGrayscaleIntensity(intensityRange.getMinLevel(),
                        intensityRange.getMaxLevel(), label);
        imageTransformation.setDescription(OPTIMAL_DATASET_INTENSITY_RESCALING_DESCRIPTION);
        boolean isDefault = simpleImageConfig.isComputeCommonIntensityRangeOfAllImagesDefault();
        imageTransformation.setDefault(isDefault);
        buffer.appendAll(channel.getAvailableTransformations());

        channel.setAvailableTransformations(buffer.getTransformations());
    }

    private static List<File> chooseChannelImages(List<ImageFileInfo> images, File incomingDir,
            String channelCode)
    {
        String normalizedChannelCode = CodeNormalizer.normalize(channelCode);
        List<File> channelImages = new ArrayList<File>();
        for (ImageFileInfo imageFileInfo : images)
        {
            String imageChannelCode = CodeNormalizer.normalize(imageFileInfo.getChannelCode());
            if (imageChannelCode.equals(normalizedChannelCode))
            {
                channelImages.add(new File(incomingDir, imageFileInfo.getImageRelativePath()));
            }
        }
        return channelImages;
    }

    private List<Channel> tryFindChannelsToComputeCommonIntensityRange(List<Channel> channels)
    {
        List<String> channelCodes =
                simpleImageConfig.getComputeCommonIntensityRangeOfAllImagesForChannels();
        if (channelCodes == null)
        {
            return null;
        }
        if (channelCodes.isEmpty())
        {
            return channels;
        }

        Set<String> channelCodesSet = createNormalizedCodesSet(channelCodes);
        List<Channel> chosenChannels = new ArrayList<Channel>();
        for (Channel channel : channels)
        {
            if (channelCodesSet.contains(channel.getCode()))
            {
                chosenChannels.add(channel);
            }
        }
        return chosenChannels;
    }

    private static Set<String> createNormalizedCodesSet(List<String> channelCodes)
    {
        Set<String> normalizedCodes = new HashSet<String>();
        for (String code : channelCodes)
        {
            normalizedCodes.add(CodeNormalizer.normalize(code));
        }
        return normalizedCodes;
    }

    private static Levels tryComputeCommonIntensityRange(IImageReader readerOrNull,
            List<File> imageFiles, float threshold)
    {
        String libraryName = (readerOrNull == null) ? null : readerOrNull.getLibraryName();
        String readerName = (readerOrNull == null) ? null : readerOrNull.getName();
        PixelHistogram histogram = new PixelHistogram();

        for (File imageFile : imageFiles)
        {
            List<ImageIdentifier> imageIdentifiers = getImageIdentifiers(readerOrNull, imageFile);
            for (ImageIdentifier imageIdentifier : imageIdentifiers)
            {
                BufferedImage image =
                        loadUnchangedImage(imageFile, imageIdentifier, libraryName, readerName);
                if (IntensityRescaling.isNotGrayscale(image))
                {
                    operationLog
                            .warn(String
                                    .format("Intensity range cannot be computed because image '%s' is not in grayscale.",
                                            imageFile.getPath()));
                    return null;
                }
                IntensityRescaling.addToLevelStats(histogram, image);
            }
        }
        return IntensityRescaling.computeLevels(histogram, threshold);
    }

    private static BufferedImage loadUnchangedImage(File imageFile,
            ImageIdentifier imageIdentifier, String libraryName, String readerName)
    {
        String imageStringIdentifier = imageIdentifier.getUniqueStringIdentifier();
        return ImageUtil.loadUnchangedImage(new FileBasedContentNode(imageFile),
                imageStringIdentifier, libraryName, readerName, null);
    }

    // -------------------
    /**
     * Extracts all images from the incoming directory.
     * 
     * @param incoming - folder with images
     * @param dataset - here the result will be stored
     */
    protected void setImageDataset(File incoming, ImageDataSetInformation dataset)
    {
        dataset.setDatasetTypeCode(simpleImageConfig.getDataSetType());
        dataset.setFileFormatCode(simpleImageConfig.getFileFormatType());
        dataset.setMeasured(simpleImageConfig.isMeasuredData());

        dataset.setSample(simpleImageConfig.getPlateSpace(), simpleImageConfig.getPlateCode());
        dataset.setIncomingDirectory(incoming);

        ImageDataSetStructure imageStruct = createImageDataSetStructure(incoming);
        dataset.setImageDataSetStructure(imageStruct);
    }

    private ImageDataSetStructure createImageDataSetStructure(File incoming)
    {
        List<File> imageFiles = extractImageFiles(incoming);
        IImageReader imageReaderOrNull = tryCreateAndSaveImageReader(imageFiles);
        List<ImageTokensWithPath> imageTokensList =
                parseImageTokens(imageFiles, incoming, imageReaderOrNull);

        int maxTileNumber = getMaxTileNumber(imageTokensList);
        Geometry tileGeometry = simpleImageConfig.getTileGeometry(imageTokensList, maxTileNumber);
        List<ImageFileInfo> images = createImageInfos(imageTokensList, tileGeometry);
        List<Channel> channels = getAvailableChannels(images);

        computeAndAppendCommonIntensityRangeTransformation(images, incoming, channels,
                imageReaderOrNull);

        ImageDataSetStructure imageStruct = new ImageDataSetStructure();
        imageStruct.setImages(images);
        imageStruct.setChannels(channels);
        imageStruct.setTileGeometry(tileGeometry.getNumberOfRows(),
                tileGeometry.getNumberOfColumns());

        imageStruct.setImageStorageConfiguraton(simpleImageConfig.getImageStorageConfiguration());
        return imageStruct;
    }

    private <T extends DataSetInformation> void setRegistrationDetails(
            DataSetRegistrationDetails<T> registrationDetails, T dataset)
    {
        registrationDetails.setDataSetInformation(dataset);
        registrationDetails.setFileFormatType(new FileFormatType(simpleImageConfig
                .getFileFormatType()));
        registrationDetails.setDataSetType(new DataSetType(simpleImageConfig.getDataSetType()));
        registrationDetails.setMeasuredData(simpleImageConfig.isMeasuredData());

    }

}
