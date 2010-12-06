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

import java.util.List;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.common.exceptions.InvalidExternalDataException;
import ch.systemsx.cisd.etlserver.HCSImageFileExtractionResult;
import ch.systemsx.cisd.etlserver.IHCSImageFileAccepter;
import ch.systemsx.cisd.etlserver.IHCSImageFileExtractor;
import ch.systemsx.cisd.etlserver.plugins.AbstractHCSImageFileExtractor;
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
public class HCSImageFileExtractor extends AbstractHCSImageFileExtractor implements
        IHCSImageFileExtractor
{
    private static final String TIFF_SUBDIRECTORY = "TIFF";

    static final String IMAGE_FILE_NOT_STANDARDIZABLE =
            "Image file '%s' could not be standardized given following tokens [plateLocation=%s,wellLocation=%s,channel=%s].";

    static final String IMAGE_FILE_NOT_ENOUGH_ENTITIES =
            "Image file '%s' does not have enough entities.";

    static final String IMAGE_FILE_BELONGS_TO_WRONG_SAMPLE =
            "Image file '%s' belongs to the wrong sample [expected=%s,found=%s].";

    static final String IMAGE_FILE_ACCEPTED =
            "Image file '%s' was accepted for channel %d, plate location %s and well location %s.";

    static final char TOKEN_SEPARATOR = '_';

    public HCSImageFileExtractor(final Properties properties)
    {
        super(properties);
    }

    @Private
    HCSImageFileExtractor(Geometry wellGeometry)
    {
        super(wellGeometry);
    }

    /**
     * Extracts the channel from given <var>value</var>, following the convention adopted here.
     * <p>
     * Returns <code>0</code> if the operation fails.
     * </p>
     */
    @Override
    protected final int getChannelWavelength(final String channel)
    {
        final String startsWith = "w";
        if (channel.startsWith(startsWith))
        {
            try
            {
                return Integer.parseInt(channel.substring(startsWith.length()));
            } catch (final NumberFormatException ex)
            {
                // Nothing to do here. Rest of the code can handle this.
            }
        }
        return 0;
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
    @Override
    protected final Location tryGetWellLocation(final String wellLocation)
    {
        return tryGetZigZagWellLocation(wellLocation, wellGeometry);
    }

    public static Location tryGetZigZagWellLocation(String value, Geometry wellGeometry)
    {
        try
        {
            int tileNumber = Integer.parseInt(value);
            Location letterLoc = Location.tryCreateLocationFromRowwisePosition(tileNumber, wellGeometry);
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

    //
    // IHCSImageFileExtractor
    //

    public final HCSImageFileExtractionResult process(final IDirectory incomingDataSetDirectory,
            final DataSetInformation dataSetInformation, final IHCSImageFileAccepter accepter)
    {
        assert incomingDataSetDirectory != null;
        final List<IFile> imageFiles = listImageFiles(incomingDataSetDirectory);
        return process(imageFiles, dataSetInformation, accepter);
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
