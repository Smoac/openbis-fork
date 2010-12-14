/*
 * Copyright 2008 ETH Zuerich, CISD
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.hcs.WellGeometry;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.etl.ImageFileExtractionResult.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.ImageFileInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ChannelDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Abstract superclass for {@link IImageFileExtractor} implementations.<br>
 * <br>
 * Assumes that images names have a file extension present in
 * {@link ImageFileExtractorUtils#IMAGE_EXTENSIONS} constant. <br>
 * <br>
 * If 'extract-single-image-channels' property is specified for storage processor then the channels
 * are extracted from the color components and the channel in the image file name is ignored.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractImageFileExtractor implements IImageFileExtractor
{
    /**
     * Extracts {@link ImageFileInfo} for a given image file. Should log all the syntax problems in
     * image names.
     */
    abstract protected ImageFileInfo tryExtractImageInfo(File imageFile,
            File incomingDataSetDirectory, SampleIdentifier datasetSample);

    // -----------

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractImageFileExtractor.class);

    protected static final String IMAGE_FILE_NOT_STANDARDIZABLE =
            "Information about the image could not be extracted for the file '%s'.";

    protected static final String IMAGE_FILE_NOT_ENOUGH_ENTITIES =
            "The name of image file '%s' could not be splitted into enough entities.";

    protected static final String IMAGE_FILE_BELONGS_TO_WRONG_SAMPLE =
            "Image file '%s' belongs to the wrong sample [expected=%s,found=%s].";

    protected static final String IMAGE_FILE_ACCEPTED = "Image file '%s' was accepted: %s.";

    // optional, list of the color components (RED, GREEN, BLUE), in the same order as channel names
    protected static final String EXTRACT_SINGLE_IMAGE_CHANNELS_PROPERTY =
            "extract-single-image-channels";

    protected static final String TILE_MAPPING_PROPERTY = "tile_mapping";

    protected static final char TOKEN_SEPARATOR = '_';

    // -----------------------------------------

    private final List<ChannelDescription> channelDescriptions;

    private final List<ColorComponent> channelColorComponentsOrNull;

    protected final TileMapper tileMapperOrNull;

    protected final Geometry wellGeometry;

    protected AbstractImageFileExtractor(Properties properties)
    {
        this(extractChannelDescriptions(properties), getWellGeometry(properties), properties);
    }

    protected AbstractImageFileExtractor(List<ChannelDescription> channelDescriptions,
            Geometry wellGeometry, Properties properties)
    {
        assert wellGeometry != null : "wel geometry is null";
        assert channelDescriptions != null : "channelDescriptions is null";

        this.channelDescriptions = channelDescriptions;
        this.wellGeometry = wellGeometry;
        this.channelColorComponentsOrNull = tryGetChannelComponents(properties);
        checkChannelsAndColorComponents();
        this.tileMapperOrNull =
                TileMapper.tryCreate(properties.getProperty(TILE_MAPPING_PROPERTY), wellGeometry);
    }

    private static Geometry getWellGeometry(final Properties properties)
    {
        final String property = properties.getProperty(WellGeometry.WELL_GEOMETRY);
        if (property == null)
        {
            throw new ConfigurationFailureException(String.format(
                    "No '%s' property has been specified.", WellGeometry.WELL_GEOMETRY));
        }
        final Geometry geometry = WellGeometry.createFromString(property);
        if (geometry == null)
        {
            throw new ConfigurationFailureException(String.format(
                    "Could not create a geometry from property value '%s'.", property));
        }
        return geometry;
    }

    protected final Location tryGetTileLocation(int tileNumber)
    {
        if (tileMapperOrNull != null)
        {
            return tileMapperOrNull.tryGetLocation(tileNumber);
        } else
        {
            return null;
        }
    }

    private void checkChannelsAndColorComponents()
    {
        if (channelColorComponentsOrNull != null
                && channelColorComponentsOrNull.size() != channelDescriptions.size())
        {
            throw ConfigurationFailureException.fromTemplate(
                    "There should be exactly one color component for each channel name."
                            + " Correct the list of values for '%s' property.",
                    AbstractImageFileExtractor.EXTRACT_SINGLE_IMAGE_CHANNELS_PROPERTY);
        }
    }

    public ch.systemsx.cisd.openbis.dss.etl.ImageFileExtractionResult extract(
            File incomingDataSetDirectory, DataSetInformation dataSetInformation)
    {
        List<File> invalidFiles = new LinkedList<File>();
        List<AcquiredSingleImage> acquiredImages = new ArrayList<AcquiredSingleImage>();
        List<File> imageFiles = ImageFileExtractorUtils.listImageFiles(incomingDataSetDirectory);
        for (final File imageFile : imageFiles)
        {
            InterruptedExceptionUnchecked.check();
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Processing image file '%s'", imageFile));
            }
            ImageFileInfo imageInfo =
                    tryExtractImageInfo(imageFile, incomingDataSetDirectory,
                            dataSetInformation.getSampleIdentifier());

            if (imageInfo != null)
            {
                List<AcquiredSingleImage> newImages = getImages(imageInfo);
                acquiredImages.addAll(newImages);
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(IMAGE_FILE_ACCEPTED, imageFile, imageInfo));
                }
            } else
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info(String.format(IMAGE_FILE_NOT_STANDARDIZABLE, imageFile));
                }
                invalidFiles.add(imageFile);
            }
        }
        return new ImageFileExtractionResult(acquiredImages,
                Collections.unmodifiableList(invalidFiles), getAllChannels());

    }

    private List<AcquiredSingleImage> getImages(ImageFileInfo imageInfo)
    {
        checkChannelsAndColorComponents();

        if (channelColorComponentsOrNull != null)
        {
            List<AcquiredSingleImage> images = new ArrayList<AcquiredSingleImage>();
            for (int i = 0; i < channelColorComponentsOrNull.size(); i++)
            {
                ColorComponent colorComponent = channelColorComponentsOrNull.get(i);
                ChannelDescription channelDescription = channelDescriptions.get(i);
                imageInfo.setChannelCode(channelDescription.getCode());
                images.add(createImage(imageInfo, colorComponent));
            }
            return images;
        } else
        {
            ensureChannelExist(channelDescriptions, imageInfo.getChannelCode());
            return createImagesWithNoColorComponent(imageInfo);
        }

    }

    private List<Channel> getAllChannels()
    {
        return createChannels(channelDescriptions);
    }

    // ------- static helper methods

    protected final static String getRelativeImagePath(File incomingDataSetDirectory,
            final File imageFile)
    {
        String imageRelativePath =
                FileUtilities.getRelativeFile(incomingDataSetDirectory,
                        new File(imageFile.getPath()));
        assert imageRelativePath != null : "Image relative path should not be null.";
        return imageRelativePath;
    }

    protected final static List<ColorComponent> tryGetChannelComponents(final Properties properties)
    {
        List<String> componentNames =
                PropertyUtils.tryGetList(properties, EXTRACT_SINGLE_IMAGE_CHANNELS_PROPERTY);
        if (componentNames == null)
        {
            return null;
        }
        List<ColorComponent> components = new ArrayList<ColorComponent>();
        for (String name : componentNames)
        {
            components.add(ColorComponent.valueOf(name));
        }
        return components;
    }

    protected final static List<Channel> createChannels(List<ChannelDescription> channelDescriptions)
    {
        List<Channel> channels = new ArrayList<Channel>();
        for (ChannelDescription channelDescription : channelDescriptions)
        {
            channels.add(new Channel(channelDescription.getCode(), null, null, channelDescription
                    .getLabel()));
        }
        return channels;
    }

    protected final static List<ChannelDescription> extractChannelDescriptions(
            final Properties properties)
    {
        return PlateStorageProcessor.extractChannelDescriptions(properties);
    }

    protected final static void ensureChannelExist(List<ChannelDescription> channelDescriptions,
            String channelCode)
    {
        for (ChannelDescription channelDescription : channelDescriptions)
        {
            if (channelDescription.getCode().equals(channelCode))
            {
                return;
            }
        }
        throw UserFailureException.fromTemplate(
                "Channel '%s' is not one of: %s. Change the configuration.", channelCode,
                channelDescriptions);
    }

    protected final static List<AcquiredSingleImage> createImagesWithNoColorComponent(
            ImageFileInfo imageInfo)
    {
        List<AcquiredSingleImage> images = new ArrayList<AcquiredSingleImage>();
        images.add(createImage(imageInfo, null));
        return images;
    }

    protected final static AcquiredSingleImage createImage(ImageFileInfo imageInfo,
            ColorComponent colorComponentOrNull)
    {
        RelativeImageReference relativeImageRef =
                new RelativeImageReference(imageInfo.getImageRelativePath(), null,
                        colorComponentOrNull);
        return new AcquiredSingleImage(imageInfo.tryGetWellLocation(), imageInfo.getTileLocation(),
                imageInfo.getChannelCode(), imageInfo.tryGetTimepoint(), imageInfo.tryGetDepth(),
                imageInfo.tryGetSeriesNumber(), relativeImageRef);
    }

    protected static Integer tryAsInt(String valueOrNull)
    {
        if (valueOrNull == null)
        {
            return null;
        }
        try
        {
            return Integer.parseInt(valueOrNull);
        } catch (NumberFormatException e)
        {
            return null;
        }
    }

    protected static Float tryAsFloat(String valueOrNull)
    {
        if (valueOrNull == null)
        {
            return null;
        }
        try
        {
            return Float.parseFloat(valueOrNull);
        } catch (NumberFormatException e)
        {
            return null;
        }
    }
}
