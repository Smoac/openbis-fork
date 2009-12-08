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

package ch.systemsx.cisd.etlserver.imsb;

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
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidExternalDataException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.etlserver.ChannelSetHelper;
import ch.systemsx.cisd.etlserver.HCSImageFileExtractionResult;
import ch.systemsx.cisd.etlserver.IHCSImageFileAccepter;
import ch.systemsx.cisd.etlserver.IHCSImageFileExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A <code>IHCSImageFileExtractor</code> implementation suitable for <i>3V</i>.
 * <p>
 * This implementation extracts and processes image files having the format
 * 
 * <code>Screening_&lt;well id&gt;_s&lt;tile number&gt;_w&lt;channel number&gt;_[&lt;some UUID that we can just ignore&gt;].tif</code>
 * . An example is <code>Screening_H24_s6_w1_[UUID].tif</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 * @author Bernd Rinn
 */
// @Final
public class HCSImageFileExtractor implements IHCSImageFileExtractor
{
    private static final String TIFF_SUBDIRECTORY = "TIFF";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, HCSImageFileExtractor.class);

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

    public HCSImageFileExtractor(final Properties properties)
    {
        assert properties != null : "Given properites should not be null";
        wellGeometry = getWellGeometry(properties);
    }

    @Private
    HCSImageFileExtractor(Geometry wellGeometry)
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
     * here.
     * <p>
     * Returns <code>null</code> if the operation fails.
     * </p>
     */
    @Private
    final Location tryGetWellLocation(final String value)
    {
        try
        {
            // Tiles are numbered in a zig-zag way, starting from left bottom row.
            // For a 3x3 plate it would be:
            // 7 8 9
            // 6 5 4
            // 1 2 3
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
    private final static Location tryGetPlateLocation(final String value)
    {
        return Location.tryCreateLocationFromMatrixCoordinate(value);
    }

    /**
     * Extracts the channel from given <var>value</var>, following the convention adopted here.
     * <p>
     * Returns <code>0</code> if the operation fails.
     * </p>
     */
    private final int getChannelWavelength(final String value)
    {
        final String startsWith = "w";
        if (value.startsWith(startsWith))
        {
            try
            {
                return Integer.parseInt(value.substring(startsWith.length()));
            } catch (final NumberFormatException ex)
            {
                // Nothing to do here. Rest of the code can handle this.
            }
        }
        return 0;
    }

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

    /** Perform channel wavelength sorting on images. */
    private static class ChannelWavelengthSortingHCSImageFileAccepterDecorator implements
            IHCSImageFileAccepter
    {

        private final IHCSImageFileAccepter accepter;

        private final List<ImageFileRecord> images = new ArrayList<ImageFileRecord>();

        private final ChannelSetHelper helper;

        ChannelWavelengthSortingHCSImageFileAccepterDecorator(final IHCSImageFileAccepter accepter)
        {
            this.accepter = accepter;
            helper = new ChannelSetHelper();
        }

        /**
         * Returns the set of <code>Channels</code>.
         */
        final Set<Channel> getChannels()
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
        final void commit()
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

    //
    // IHCSImageFileExtractor
    //

    public final HCSImageFileExtractionResult process(final IDirectory incomingDataSetDirectory,
            final DataSetInformation dataSetInformation, final IHCSImageFileAccepter accepter)
    {
        assert incomingDataSetDirectory != null;
        final List<IFile> imageFiles = listImageFiles(incomingDataSetDirectory);
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
            final Location wellLocation = tryGetWellLocation(wellLocationStr);
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

    private IDirectory getTiffSubDirectory(final IDirectory incomingDataSetDirectory)
    {
        final INode tiffSubDirectoryNodeOrNull =
                incomingDataSetDirectory.tryGetNode(TIFF_SUBDIRECTORY);
        if (tiffSubDirectoryNodeOrNull == null)
        {
            throw InvalidExternalDataException.fromTemplate(
                    "The directory '%s' does not have a sub-directory '%s'.",
                    incomingDataSetDirectory.getPath(), TIFF_SUBDIRECTORY);
        }
        final IDirectory tiffSubDirectoryOrNull = tiffSubDirectoryNodeOrNull.tryAsDirectory();
        if (tiffSubDirectoryOrNull == null)
        {
            throw InvalidExternalDataException.fromTemplate("The file '%s/%s' is not a directory.",
                    incomingDataSetDirectory.getPath(), TIFF_SUBDIRECTORY);
        }
        return tiffSubDirectoryOrNull;
    }

    @Private
    List<IFile> listImageFiles(final IDirectory directory)
    {
        final IDirectory tiffSubDirectory = getTiffSubDirectory(directory);
        return tiffSubDirectory.listFiles(new String[]
            { "tif", "tiff", "png" }, true);
    }

}
