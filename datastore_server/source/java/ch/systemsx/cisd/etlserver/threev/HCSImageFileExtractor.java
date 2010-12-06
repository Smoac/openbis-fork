/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.threev;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.hcs.WellGeometry;
import ch.systemsx.cisd.bds.storage.IDirectory;
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
 * A <code>IHCSImageFileExtractor</code> implementation suitable for <i>3V</i>.
 * <p>
 * This implementation extracts and processes image files having the format
 * 
 * <code>Screening_&lt;well id&gt;_s&lt;tile number&gt;_w&lt;channel number&gt;_[&lt;some UUID that we can just ignore&gt;].tif</code>
 * . An example is <code>Screening_H24_s6_w1_[UUID].tif</code>.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class HCSImageFileExtractor implements IHCSImageFileExtractor
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, HCSImageFileExtractor.class);

    static final String IMAGE_FILE_NOT_STANDARDIZABLE =
            "Image file '%s' could not be standardized given following tokens [plateLocation=%s,wellLocation=%s,channel=%s].";

    static final String IMAGE_FILE_ACCEPTED =
            "Image file '%s' was accepted for channel %d, plate location %s and well location %s.";

    static final String FILE_PREFIX = "Screening_";

    static final int TOKEN_NUMBER = 5;

    static final char TOKEN_SEPARATOR = '_';

    private final Geometry wellGeometry;

    public HCSImageFileExtractor(final Properties properties)
    {
        assert properties != null : "Given properites should not be null";
        wellGeometry = getWellGeometry(properties);
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
        final String startsWith = "s";
        if (value.startsWith(startsWith))
        {
            final String tileNo = value.substring(startsWith.length());
            try
            {
                return Location.tryCreateLocationFromRowwisePosition(Integer.parseInt(tileNo),
                        wellGeometry);
            } catch (NumberFormatException ex)
            {
                // Nothing to do here. Rest of the code can handle this.
            }
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
        return Location.tryCreateLocationFromTransposedMatrixCoordinate(value);
    }

    /**
     * Extracts the wavelength from given <var>value</var>, following the convention adopted here.
     * <p>
     * Returns <code>0</code> if the operation fails.
     * </p>
     */
    private final int getWavelength(final String value)
    {
        final String startsWith = "w";
        if (value.startsWith(startsWith))
        {
            try
            {
                return Integer.parseInt(value.substring(startsWith.length()));
            } catch (NumberFormatException ex)
            {
                // Nothing to do here. Rest of the code can handle this.
            }
        }
        return 0;
    }

    //
    // IHCSImageFileExtractor
    //

    public final HCSImageFileExtractionResult process(final IDirectory incomingDataSetDirectory,
            DataSetInformation dataSetInformation, final IHCSImageFileAccepter accepter)
    {
        assert incomingDataSetDirectory != null;
        final List<IFile> imageFiles = incomingDataSetDirectory.listFiles(new String[]
            { "tif", "tiff" }, true);
        final long start = System.currentTimeMillis();
        final List<IFile> invalidFiles = new LinkedList<IFile>();
        final ChannelSetHelper helper = new ChannelSetHelper();
        for (final IFile imageFile : imageFiles)
        {
            InterruptedExceptionUnchecked.check();
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Processing image file '%s'", imageFile));
            }
            final String baseName = FilenameUtils.getBaseName(imageFile.getPath());
            if (baseName.startsWith(FILE_PREFIX) == false)
            {
                continue;
            }
            final String[] tokens = StringUtils.split(baseName, TOKEN_SEPARATOR);
            if (tokens.length != TOKEN_NUMBER)
            {
                continue;
            }
            final Location plateLocation = tryGetPlateLocation(tokens[1]);
            final Location wellLocation = tryGetWellLocation(tokens[2]);
            final int wavelength = getWavelength(tokens[3]);
            if (wellLocation != null && plateLocation != null && wavelength > 0)
            {
                helper.addWavelength(wavelength);
                accepter.accept(wavelength, plateLocation, wellLocation, imageFile);
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(IMAGE_FILE_ACCEPTED, imageFile, wavelength,
                            plateLocation, wellLocation));
                }
            } else
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(IMAGE_FILE_NOT_STANDARDIZABLE, imageFile,
                            tokens[0], tokens[1], tokens[2]));
                }
                invalidFiles.add(imageFile);
            }
        }
        return new HCSImageFileExtractionResult(System.currentTimeMillis() - start, imageFiles
                .size(), Collections.unmodifiableList(invalidFiles), helper.getChannelSet());
    }

}
