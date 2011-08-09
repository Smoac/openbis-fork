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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ImageReaderFactory;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageLibraryInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageIdentifier;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageMetadata;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.Location;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.SimpleImageDataConfig;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
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
    @Private static interface IImageReaderFactory 
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
                        public IImageReader tryGetReaderForFile(String libraryName, String fileName)
                        {
                            return ImageReaderFactory.tryGetReaderForFile(libraryName, fileName);
                        }

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

    private final SimpleImageDataConfig simpleImageConfig;
    private final IImageReaderFactory readerFactory;

    private SimpleImageDataSetRegistrator(SimpleImageDataConfig simpleImageConfig, IImageReaderFactory readerFactory)
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
    private List<File> listImageFiles(final File incomingDirectory)
    {
        return FileOperations.getInstance().listFiles(incomingDirectory,
                simpleImageConfig.getRecognizedImageExtensions(), true);
    }

    /**
     * Tokenizes file names of all images in the directory.
     */
    protected List<ImageTokensWithPath> parseImageTokens(File incomingDirectory)
    {
        List<File> imageFiles = listImageFiles(incomingDirectory);
        if (imageFiles.isEmpty())
        {
            throw UserFailureException.fromTemplate(
                    "Incoming directory '%s' contains no images with extensions %s!",
                    incomingDirectory.getPath(), CollectionUtils.abbreviate(
                            simpleImageConfig.getRecognizedImageExtensions(), -1));
        }

        List<ImageTokensWithPath> imageTokensList = new ArrayList<ImageTokensWithPath>();
        ImageLibraryInfo imageLibraryInfoOrNull =
                simpleImageConfig.getImageStorageConfiguration().tryGetImageLibrary();
        for (File imageFile : imageFiles)
        {
            File file = new File(imageFile.getPath());
            IImageReader readerOrNull = null;
            if (imageLibraryInfoOrNull != null)
            {
                String libraryName = imageLibraryInfoOrNull.getName();
                String readerNameOrNull = imageLibraryInfoOrNull.getReaderName();
                if (readerNameOrNull != null)
                {
                    readerOrNull = readerFactory.tryGetReader(libraryName, readerNameOrNull);
                } else
                {
                    readerOrNull =
                            readerFactory.tryGetReaderForFile(libraryName, imageFile.getPath());
                    if (readerOrNull != null)
                    {
                        imageLibraryInfoOrNull.setReaderName(readerOrNull.getName());
                    }
                }
            }
            List<ImageIdentifier> identifiers = getImageIdentifiers(readerOrNull, file);
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
            throw UserFailureException.fromTemplate(
                            "No image tokens could be parsed from incoming directory '%s' for extensions %s!",
                    incomingDirectory.getPath(), CollectionUtils.abbreviate(
                            simpleImageConfig.getRecognizedImageExtensions(), -1));
        }
        return imageTokensList;
    }

    private List<ImageIdentifier> getImageIdentifiers(IImageReader readerOrNull, File imageFile)
    {
        List<ImageIdentifier> ids = new ArrayList<ImageIdentifier>();
        if (readerOrNull == null)
        {
            ids.add(ImageIdentifier.NULL);
        } else
        {
            List<ImageID> imageIDs =
                readerOrNull.getImageIDs(imageFile);
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

        String sampleCode = simpleImageConfig.getPlateCode();
        String spaceCode = simpleImageConfig.getPlateSpace();
        dataset.setSample(spaceCode, sampleCode);
        dataset.setMeasured(true);

        List<ImageTokensWithPath> imageTokensList = parseImageTokens(incoming);
        int maxTileNumber = getMaxTileNumber(imageTokensList);
        Geometry tileGeometry = simpleImageConfig.getTileGeometry(imageTokensList, maxTileNumber);
        List<ImageFileInfo> images = createImageInfos(imageTokensList, tileGeometry);
        List<Channel> channels = getAvailableChannels(images);

        dataset.setImages(images);
        dataset.setChannels(channels);
        dataset.setTileGeometry(tileGeometry.getNumberOfRows(), tileGeometry.getNumberOfColumns());

        dataset.setImageStorageConfiguraton(simpleImageConfig.getImageStorageConfiguration());
    }

    private <T extends DataSetInformation> void setRegistrationDetails(
            DataSetRegistrationDetails<T> registrationDetails, T dataset)
    {
        registrationDetails.setDataSetInformation(dataset);
        registrationDetails.setFileFormatType(new FileFormatType(simpleImageConfig.getFileFormatType()));
        registrationDetails.setDataSetType(new DataSetType(simpleImageConfig.getDataSetType()));
        registrationDetails.setMeasuredData(simpleImageConfig.isMeasuredData());

    }

}
