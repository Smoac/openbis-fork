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

package ch.systemsx.cisd.etlserver.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.bds.hcs.Channel;
import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.hcs.WellGeometry;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.ChannelSetHelper;
import ch.systemsx.cisd.etlserver.HCSImageFileExtractionResult;
import ch.systemsx.cisd.etlserver.IHCSImageFileAccepter;
import ch.systemsx.cisd.etlserver.IHCSImageFileExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Abstract class for {@link IHCSImageFileExtractor}.
 * <p>
 * 
 * @author Tomasz Pylak
 */
abstract public class AbstractHCSImageFileExtractor
{
    /**
     * Extracts the channel from given <var>value</var>, following the convention adopted here.
     * <p>
     * Returns <code>0</code> if the operation fails.
     * </p>
     */
    abstract protected int getChannelWavelength(final String value);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractHCSImageFileExtractor.class);

    static final String IMAGE_FILE_NOT_STANDARDIZABLE =
            "Image file '%s' could not be standardized given following tokens [plateLocation=%s,wellLocation=%s,channel=%s].";

    static final String IMAGE_FILE_NOT_ENOUGH_ENTITIES =
            "Image file '%s' does not have enough entities.";

    static final String IMAGE_FILE_BELONGS_TO_WRONG_SAMPLE =
            "Image file '%s' belongs to the wrong sample [expected=%s,found=%s].";

    static final String IMAGE_FILE_ACCEPTED =
            "Image file '%s' was accepted for channel %d, plate location %s and well location %s.";

    static final char TOKEN_SEPARATOR = '_';

    private final Geometry wellGeometry;

    protected AbstractHCSImageFileExtractor(final Properties properties)
    {
        assert properties != null : "Given properites should not be null";
        wellGeometry = getWellGeometry(properties);
    }

    protected AbstractHCSImageFileExtractor(Geometry wellGeometry)
    {
        this.wellGeometry = wellGeometry;
    }

    private final static Geometry getWellGeometry(final Properties properties)
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

    /**
     * Extracts the well location from given <var>value</var>, following the convention adopted
     * here.<br>
     * Tiles are numbered in a zig-zag way, starting from left bottom row. For a 3x3 plate it would
     * be:<br>
     * 7 8 9<br>
     * 6 5 4<br>
     * 1 2 3<br>
     * <p>
     * Returns <code>null</code> if the operation fails.
     * </p>
     */
    protected final Location tryGetZigZagWellLocation(final String value)
    {
        return tryGetZigZagWellLocation(value, wellGeometry);
    }

    @Private
    public static final Location tryGetZigZagWellLocation(final String value, Geometry wellGeometry)
    {
        try
        {
            int tileNumber = Integer.parseInt(value);
            Location letterLoc = Location.tryCreateLocationFromPosition(tileNumber, wellGeometry);
            int row = letterLoc.getY();
            int col = letterLoc.getX();
            // microscops starts at the last row, so let's make a mirror
            row = wellGeometry.getRows() - row + 1;
            // even rows counting from the bottom have reverted columns
            if (letterLoc.getY() % 2 == 0)
            {
                col = wellGeometry.getColumns() - col + 1;
            }
            return new Location(col, row);
        } catch (final NumberFormatException ex)
        {
            // Nothing to do here. Rest of the code can handle this.
        }
        return null;
    }

    /**
     * Extracts the plate location from given <var>value</var>, following the convention adopted
     * here.
     * <p>
     * Returns <code>null</code> if the operation fails.
     * </p>
     */
    protected final static Location tryGetPlateLocation(final String value)
    {
        return Location.tryCreateLocationFromMatrixCoordinate(value);
    }

    /** Perform channel wavelength sorting on images. */
    protected static class ChannelWavelengthSortingHCSImageFileAccepterDecorator implements
            IHCSImageFileAccepter
    {
        private static class ImageFileRecord
        {
            final IFile imageFile;

            final Location plateLocation;

            final Location wellLocation;

            final int channelWavelength;

            ImageFileRecord(final IFile imageFile, final Location plateLocation,
                    final Location wellLocation, final int channelWavelength)
            {
                this.imageFile = imageFile;
                this.plateLocation = plateLocation;
                this.wellLocation = wellLocation;
                this.channelWavelength = channelWavelength;
            }
        }

        private final IHCSImageFileAccepter accepter;

        private final List<ImageFileRecord> images = new ArrayList<ImageFileRecord>();

        private final ChannelSetHelper helper;

        public ChannelWavelengthSortingHCSImageFileAccepterDecorator(
                final IHCSImageFileAccepter accepter)
        {
            this.accepter = accepter;
            helper = new ChannelSetHelper();
        }

        /**
         * Returns the set of <code>Channels</code>.
         */
        public final Set<Channel> getChannels()
        {
            return helper.getChannelSet();
        }

        /**
         * Informs that {@link #accept(int, Location, Location, IFile)} will no longer get called.
         * <p>
         * We are now ready to construct the channels and to commit the images to the encapsulated
         * <code>IHCSImageFileAccepter</code>.
         * </p>
         */
        public final void commit()
        {
            for (final ImageFileRecord image : images)
            {
                accepter.accept(helper.getChannelForWavelength(image.channelWavelength)
                        .getCounter(), image.plateLocation, image.wellLocation, image.imageFile);
            }
        }

        //
        // IHCSImageFileAccepter
        //

        public final void accept(final int channelWavelength, final Location wellLocation,
                final Location tileLocation, final IFile imageFile)
        {
            images
                    .add(new ImageFileRecord(imageFile, wellLocation, tileLocation,
                            channelWavelength));
            helper.addWavelength(channelWavelength);
        }

    }

    public final HCSImageFileExtractionResult process(final List<IFile> imageFiles,
            final DataSetInformation dataSetInformation, final IHCSImageFileAccepter accepter)
    {
        final long start = System.currentTimeMillis();
        final List<IFile> invalidFiles = new LinkedList<IFile>();
        final ChannelWavelengthSortingHCSImageFileAccepterDecorator accepterDecorator =
                new ChannelWavelengthSortingHCSImageFileAccepterDecorator(accepter);
        for (final IFile imageFile : imageFiles)
        {
            InterruptedExceptionUnchecked.check();
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Processing image file '%s'", imageFile));
            }
            final String baseName = FilenameUtils.getBaseName(imageFile.getPath());
            final String[] tokens = StringUtils.split(baseName, TOKEN_SEPARATOR);
            if (tokens.length < 4)
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(IMAGE_FILE_NOT_ENOUGH_ENTITIES, imageFile));
                }
                invalidFiles.add(imageFile);
                continue;
            }
            final String sampleCode = tokens[tokens.length - 4];
            if (sampleCode.equals(dataSetInformation.getSampleCode()) == false)
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(IMAGE_FILE_BELONGS_TO_WRONG_SAMPLE, imageFile,
                            dataSetInformation.getSampleIdentifier(), sampleCode));
                }
                invalidFiles.add(imageFile);
                continue;
            }
            final String plateLocationStr = tokens[tokens.length - 3];
            final Location plateLocation = tryGetPlateLocation(plateLocationStr);
            final String wellLocationStr = tokens[tokens.length - 2];
            final Location wellLocation = tryGetZigZagWellLocation(wellLocationStr);
            final String channelStr = tokens[tokens.length - 1];
            final int channelWavelength = getChannelWavelength(channelStr);
            if (wellLocation != null && plateLocation != null && channelWavelength > 0)
            {
                accepterDecorator.accept(channelWavelength, plateLocation, wellLocation, imageFile);
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(IMAGE_FILE_ACCEPTED, imageFile,
                            channelWavelength, plateLocation, wellLocation));
                }
            } else
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(IMAGE_FILE_NOT_STANDARDIZABLE, imageFile,
                            plateLocationStr, wellLocationStr, channelStr));
                }
                invalidFiles.add(imageFile);
            }
        }
        accepterDecorator.commit();
        return new HCSImageFileExtractionResult(System.currentTimeMillis() - start, imageFiles
                .size(), Collections.unmodifiableList(invalidFiles), accepterDecorator
                .getChannels());
    }
}
