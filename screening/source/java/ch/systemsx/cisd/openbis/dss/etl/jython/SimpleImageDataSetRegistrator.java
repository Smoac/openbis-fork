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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.registrator.DataSetRegistrationDetails;
import ch.systemsx.cisd.etlserver.registrator.IDataSetRegistrationDetailsFactory;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ImageFileInfo;
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
    private static class ImageTokensWithPath extends ImageMetadata
    {
        /** path relative to the incoming dataset directory */
        private String imageRelativePath;

        public ImageTokensWithPath(ImageMetadata imageTokens, String imageRelativePath)
        {
            setWell(imageTokens.getWell());
            setChannelCode(imageTokens.getChannelCode());
            setTileNumber(imageTokens.getTileNumber());
            this.imageRelativePath = imageRelativePath;
        }

        public String getImagePath()
        {
            return imageRelativePath;
        }
    }

    public static DataSetRegistrationDetails<ImageDataSetInformation> createImageDatasetDetails(
            SimpleImageDataConfig imageDataSet, File incoming,
            IDataSetRegistrationDetailsFactory<ImageDataSetInformation> factory)
    {
        return new SimpleImageDataSetRegistrator(imageDataSet).createImageDatasetDetails(incoming,
                factory);
    }

    private final SimpleImageDataConfig imageDataSet;

    private SimpleImageDataSetRegistrator(SimpleImageDataConfig imageDataSet)
    {
        this.imageDataSet = imageDataSet;
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
                imageDataSet.getRecognizedImageExtensions(), true);
    }

    /**
     * Tokenizes file names of all images in the directory.
     */
    protected List<ImageTokensWithPath> parseImageTokens(File incomingDirectory)
    {
        List<ImageTokensWithPath> imageTokensList = new ArrayList<ImageTokensWithPath>();
        List<File> imageFiles = listImageFiles(incomingDirectory);
        for (File imageFile : imageFiles)
        {
            String imageRelativePath =
                    FileUtilities.getRelativeFile(incomingDirectory, new File(imageFile.getPath()));
            ImageMetadata imageTokens = imageDataSet.extractImageMetadata(imageRelativePath);
            imageTokens.ensureValid();
            imageTokensList.add(new ImageTokensWithPath(imageTokens, imageRelativePath));
        }
        return imageTokensList;
    }

    /**
     * Creates ImageFileInfo for a given path to an image.
     */
    protected ImageFileInfo createImageInfo(ImageTokensWithPath imageTokens, Geometry tileGeometry)
    {
        Location tileCoords =
                imageDataSet.getTileCoordinates(imageTokens.getTileNumber(), tileGeometry);
        ImageFileInfo img =
                new ImageFileInfo(imageTokens.getChannelCode(), tileCoords.getRow(),
                        tileCoords.getColumn(), imageTokens.getImagePath());
        img.setTimepoint(imageTokens.tryGetTimepoint());
        img.setDepth(imageTokens.tryGetDepth());
        img.setSeriesNumber(imageTokens.tryGetSeriesNumber());
        img.setWell(imageTokens.getWell());
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
        Set<String> channelCodes = new HashSet<String>();
        for (ImageFileInfo image : images)
        {
            channelCodes.add(image.getChannelCode());
        }
        List<Channel> channels = new ArrayList<Channel>();
        for (String channelCode : channelCodes)
        {
            channels.add(imageDataSet.createChannel(channelCode));
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
        dataset.setDatasetTypeCode(imageDataSet.getDataSetType());
        dataset.setFileFormatCode(imageDataSet.getFileFormatType());
        dataset.setMeasured(imageDataSet.isMeasuredData());

        String sampleCode = imageDataSet.getPlateCode();
        String spaceCode = imageDataSet.getPlateSpace();
        dataset.setSample(spaceCode, sampleCode);
        dataset.setMeasured(true);

        List<ImageTokensWithPath> imageTokensList = parseImageTokens(incoming);
        int maxTileNumber = getMaxTileNumber(imageTokensList);
        Geometry tileGeometry = imageDataSet.getTileGeometry(imageTokensList, maxTileNumber);
        List<ImageFileInfo> images = createImageInfos(imageTokensList, tileGeometry);
        List<Channel> channels = getAvailableChannels(images);

        dataset.setImages(images);
        dataset.setChannels(channels);
        dataset.setTileGeometry(tileGeometry.getNumberOfRows(), tileGeometry.getNumberOfColumns());

        dataset.setImageStorageConfiguraton(imageDataSet.getImageStorageConfiguration());
    }

    private <T extends DataSetInformation> void setRegistrationDetails(
            DataSetRegistrationDetails<T> registrationDetails, T dataset)
    {
        registrationDetails.setDataSetInformation(dataset);
        registrationDetails.setFileFormatType(new FileFormatType(imageDataSet.getFileFormatType()));
        registrationDetails.setDataSetType(new DataSetType(imageDataSet.getDataSetType()));
        registrationDetails.setMeasuredData(imageDataSet.isMeasuredData());

    }

}
