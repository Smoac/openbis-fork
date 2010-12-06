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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
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
import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageFileExtractionResult.Channel;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ChannelDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Abstract superclass for <code>IHCSImageFileExtractor</code> implementations.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractHCSImageFileExtractor implements IHCSImageFileExtractor
{
    abstract protected List<AcquiredPlateImage> getImages(ImageFileInfo imageInfo);

    abstract protected List<Channel> getAllChannels();

    /** Should log all the syntax problems in image names. */
    abstract protected ImageFileInfo tryExtractImageInfo(File imageFile,
            File incomingDataSetDirectory, SampleIdentifier datasetSample);

    /**
     * Extracts the plate location from argument. Returns <code>null</code> if the operation fails.
     */
    protected static Location tryGetPlateLocation(final String plateLocation)
    {
        return Location.tryCreateLocationFromTransposedMatrixCoordinate(plateLocation);
    }

    /**
     * Intermediate structure containing tokens from which image info {@link ImageFileInfo} can be
     * extracted if one finds it useful.
     */
    public static class UnparsedImageFileInfo extends AbstractHashable
    {
        private String plateLocationToken;

        private String wellLocationToken;

        private String channelToken;

        private String timepointToken;

        private String depthToken;

        private String imageRelativePath;

        public String getPlateLocationToken()
        {
            return plateLocationToken;
        }

        public void setPlateLocationToken(String plateLocationToken)
        {
            this.plateLocationToken = plateLocationToken;
        }

        public String getWellLocationToken()
        {
            return wellLocationToken;
        }

        public void setWellLocationToken(String wellLocationToken)
        {
            this.wellLocationToken = wellLocationToken;
        }

        public String getChannelToken()
        {
            return channelToken;
        }

        public void setChannelToken(String channelToken)
        {
            this.channelToken = channelToken;
        }

        public String getTimepointToken()
        {
            return timepointToken;
        }

        public void setTimepointToken(String timepointToken)
        {
            this.timepointToken = timepointToken;
        }

        public String getDepthToken()
        {
            return depthToken;
        }

        public void setDepthToken(String depthToken)
        {
            this.depthToken = depthToken;
        }

        public String getImageRelativePath()
        {
            return imageRelativePath;
        }

        public void setImageRelativePath(String imageRelativePath)
        {
            this.imageRelativePath = imageRelativePath;
        }
    }

    /** Information about one image file */
    public static final class ImageFileInfo
    {
        private final Location wellLocation;

        private final Location tileLocation;

        private String channelCode;

        private final String imageRelativePath;

        private final Float timepointOrNull;

        private final Float depthOrNull;

        public ImageFileInfo(Location wellLocation, String channelCode, Location tileLocation,
                String imageRelativePath, Float timepointOrNull, Float depthOrNull)
        {
            assert wellLocation != null;
            assert channelCode != null;
            assert tileLocation != null;
            assert imageRelativePath != null;

            this.wellLocation = wellLocation;
            this.channelCode = channelCode;
            this.tileLocation = tileLocation;
            this.imageRelativePath = imageRelativePath;
            this.timepointOrNull = timepointOrNull;
            this.depthOrNull = depthOrNull;
        }

        public Location getWellLocation()
        {
            return wellLocation;
        }

        public Location getTileLocation()
        {
            return tileLocation;
        }

        public String getChannelCode()
        {
            return channelCode;
        }

        public String getImageRelativePath()
        {
            return imageRelativePath;
        }

        public Float tryGetTimepoint()
        {
            return timepointOrNull;
        }

        public Float tryGetDepth()
        {
            return depthOrNull;
        }

        public void setChannelCode(String channelCode)
        {
            this.channelCode = channelCode;
        }

        @Override
        public String toString()
        {
            return "ImageFileInfo [well=" + wellLocation + ", tile=" + tileLocation + ", channel="
                    + channelCode + ", path=" + imageRelativePath + ", timepoint="
                    + timepointOrNull + ", depth=" + depthOrNull + "]";
        }

    }

    protected static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            AbstractHCSImageFileExtractor.class);

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

    protected static final char TOKEN_SEPARATOR = '_';

    public AbstractHCSImageFileExtractor(final Properties properties)
    {
    }

    /**
     * Splits specified image file name into at least four tokens. Only the last four tokens will be
     * considered. They are sample code, plate location, well location, and channel. Note, that
     * sample code could be <code>null</code>.
     * 
     * @param shouldValidatePlateName if true it will be checked if the plate code in the file name
     *            matches the datasetSample plate code.
     * @return <code>null</code> if the argument could not be splitted into tokens.
     */
    protected final static UnparsedImageFileInfo tryExtractDefaultImageInfo(File imageFile,
            File incomingDataSetDirectory, SampleIdentifier datasetSample,
            boolean shouldValidatePlateName)
    {
        final String baseName = FilenameUtils.getBaseName(imageFile.getPath());
        final String[] tokens = StringUtils.split(baseName, TOKEN_SEPARATOR);
        if (tokens == null || tokens.length < 4)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format(IMAGE_FILE_NOT_ENOUGH_ENTITIES, imageFile));
            }
            return null;
        }
        final String sampleCode = tokens[tokens.length - 4];
        if (shouldValidatePlateName && sampleCode != null
                && sampleCode.equalsIgnoreCase(datasetSample.getSampleCode()) == false)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format(IMAGE_FILE_BELONGS_TO_WRONG_SAMPLE, imageFile,
                        datasetSample, sampleCode));
            }
            return null;
        }
        String channelToken = tokens[tokens.length - 1];
        if (StringUtils.isBlank(channelToken))
        {
            operationLog.info("Channel token is empty for image: " + imageFile);
            return null;
        }

        UnparsedImageFileInfo info = new UnparsedImageFileInfo();
        info.setPlateLocationToken(tokens[tokens.length - 3]);
        info.setWellLocationToken(tokens[tokens.length - 2]);
        info.setChannelToken(channelToken);
        info.setTimepointToken(null);
        info.setDepthToken(null);

        String imageRelativePath = getRelativeImagePath(incomingDataSetDirectory, imageFile);
        info.setImageRelativePath(imageRelativePath);
        return info;
    }

    public ch.systemsx.cisd.openbis.dss.etl.HCSImageFileExtractionResult extract(
            File incomingDataSetDirectory, DataSetInformation dataSetInformation)
    {
        List<File> invalidFiles = new LinkedList<File>();
        List<AcquiredPlateImage> acquiredImages = new ArrayList<AcquiredPlateImage>();
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
                List<AcquiredPlateImage> newImages = getImages(imageInfo);
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
        return new HCSImageFileExtractionResult(acquiredImages,
                Collections.unmodifiableList(invalidFiles), getAllChannels());

    }

    protected static String getRelativeImagePath(File incomingDataSetDirectory, final File imageFile)
    {
        String imageRelativePath =
                FileUtilities.getRelativeFile(incomingDataSetDirectory,
                        new File(imageFile.getPath()));
        assert imageRelativePath != null : "Image relative path should not be null.";
        return imageRelativePath;
    }

    // ------- static helper methods

    public static List<ColorComponent> tryGetChannelComponents(final Properties properties)
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

    protected final static List<ChannelDescription> tryExtractChannelDescriptions(
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

    protected final static Geometry getWellGeometry(final Properties properties)
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

    protected static List<AcquiredPlateImage> getDefaultImages(ImageFileInfo imageInfo)
    {
        List<AcquiredPlateImage> images = new ArrayList<AcquiredPlateImage>();
        images.add(createImage(imageInfo, null));
        return images;
    }

    protected static final AcquiredPlateImage createImage(ImageFileInfo imageInfo,
            ColorComponent colorComponentOrNull)
    {
        RelativeImageReference relativeImageRef =
                new RelativeImageReference(imageInfo.getImageRelativePath(), null,
                        colorComponentOrNull);
        return new AcquiredPlateImage(imageInfo.getWellLocation(), imageInfo.getTileLocation(),
                imageInfo.getChannelCode(), imageInfo.tryGetTimepoint(), imageInfo.tryGetDepth(),
                relativeImageRef);
    }
}
