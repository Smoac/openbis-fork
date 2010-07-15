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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.hcs.WellGeometry;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.etl.HCSImageFileExtractionResult.Channel;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ColorComponent;

/**
 * Abstract superclass for <code>IHCSImageFileExtractor</code> implementations.
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractHCSImageFileExtractor implements IHCSImageFileExtractor
{
    /**
     * Extracts the well location from given <var>value</var>. Returns <code>null</code> if the
     * operation fails.
     */
    abstract protected Location tryGetWellLocation(final String wellLocation);

    abstract protected List<File> listImageFiles(final File directory);

    abstract protected List<AcquiredPlateImage> getImages(String channelToken,
            Location plateLocation, Location wellLocation, String imageRelativePath);

    abstract protected Set<Channel> getAllChannels();

    /**
     * Extracts the plate location from argument. Returns <code>null</code> if the operation fails.
     * <p>
     * Subclasses may override this method.
     */
    protected Location tryGetPlateLocation(final String plateLocation)
    {
        return Location.tryCreateLocationFromTransposedMatrixCoordinate(plateLocation);
    }

    // -------------------------------

    protected static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractHCSImageFileExtractor.class);

    protected static final String IMAGE_FILE_NOT_STANDARDIZABLE =
            "Image file '%s' could not be standardized given following tokens [plateLocation=%s,wellLocation=%s,channel=%s].";

    protected static final String IMAGE_FILE_NOT_ENOUGH_ENTITIES =
            "The name of image file '%s' could not be splitted into enough entities.";

    protected static final String IMAGE_FILE_BELONGS_TO_WRONG_SAMPLE =
            "Image file '%s' belongs to the wrong sample [expected=%s,found=%s].";

    protected static final String IMAGE_FILE_ACCEPTED =
            "Image file '%s' was accepted for channel %d, plate location %s and well location %s.";

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
     * <p>
     * Subclasses may override this method.
     * 
     * @return <code>null</code> if the argument could not be splitted into tokens.
     */
    private String[] tryToSplitIntoTokens(final String imageFileName)
    {
        return StringUtils.split(imageFileName, TOKEN_SEPARATOR);
    }

    public ch.systemsx.cisd.openbis.dss.etl.HCSImageFileExtractionResult extract(
            File incomingDataSetDirectory, DataSetInformation dataSetInformation)
    {
        List<File> invalidFiles = new LinkedList<File>();
        List<AcquiredPlateImage> acquiredImages = new ArrayList<AcquiredPlateImage>();
        List<File> imageFiles = listImageFiles(incomingDataSetDirectory);
        for (final File imageFile : imageFiles)
        {
            InterruptedExceptionUnchecked.check();
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Processing image file '%s'", imageFile));
            }
            final String baseName = FilenameUtils.getBaseName(imageFile.getPath());
            final String[] tokens = tryToSplitIntoTokens(baseName);
            if (tokens == null || tokens.length < 4)
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(IMAGE_FILE_NOT_ENOUGH_ENTITIES, imageFile));
                }
                invalidFiles.add(imageFile);
                continue;
            }
            final String sampleCode = tokens[tokens.length - 4];
            if (sampleCode != null
                    && sampleCode.equalsIgnoreCase(dataSetInformation.getSampleCode()) == false)
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(IMAGE_FILE_BELONGS_TO_WRONG_SAMPLE, imageFile,
                            dataSetInformation.getSampleIdentifier(), sampleCode));
                }
                invalidFiles.add(imageFile);
                continue;
            }
            final String plateLocationToken = tokens[tokens.length - 3];
            final Location plateLocation = tryGetPlateLocation(plateLocationToken);
            final String wellLocationToken = tokens[tokens.length - 2];
            final Location wellLocation = tryGetWellLocation(wellLocationToken);
            final String channelToken = tokens[tokens.length - 1];
            if (wellLocation != null && plateLocation != null && channelToken != null)
            {
                String imageRelativePath =
                        getRelativeImagePath(incomingDataSetDirectory, imageFile);
                List<AcquiredPlateImage> newImages =
                        getImages(channelToken, plateLocation, wellLocation, imageRelativePath);
                acquiredImages.addAll(newImages);
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(IMAGE_FILE_ACCEPTED, imageFile, channelToken,
                            plateLocation, wellLocation));
                }
            } else
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(IMAGE_FILE_NOT_STANDARDIZABLE, imageFile,
                            plateLocationToken, wellLocationToken, channelToken));
                }
                invalidFiles.add(imageFile);
            }
        }
        return new HCSImageFileExtractionResult(acquiredImages, Collections
                .unmodifiableList(invalidFiles), getAllChannels());

    }

    private String getRelativeImagePath(File incomingDataSetDirectory, final File imageFile)
    {
        String imageRelativePath =
                FileUtilities.getRelativeFile(incomingDataSetDirectory, new File(imageFile
                        .getPath()));
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

    protected static List<String> extractChannelNames(final Properties properties)
    {
        return PropertyUtils.getMandatoryList(properties, PlateStorageProcessor.CHANNEL_NAMES);
    }

    protected static Set<Channel> createChannels(List<String> channelNames)
    {
        Set<Channel> channels = new HashSet<Channel>();
        for (String channelName : channelNames)
        {
            channels.add(new Channel(channelName, null, null));
        }
        return channels;
    }

    protected static Geometry getWellGeometry(final Properties properties)
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

    protected static final AcquiredPlateImage createImage(Location plateLocation,
            Location wellLocation, String imageRelativePath, String channelName,
            ColorComponent colorComponent)
    {
        return new AcquiredPlateImage(plateLocation, wellLocation, channelName, null, null,
                new RelativeImageReference(imageRelativePath, null, colorComponent));
    }
}
