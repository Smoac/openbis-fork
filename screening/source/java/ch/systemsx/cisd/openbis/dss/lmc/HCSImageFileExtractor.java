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

package ch.systemsx.cisd.openbis.dss.lmc;

import java.util.List;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.etlserver.HCSImageFileExtractionResult;
import ch.systemsx.cisd.etlserver.IHCSImageFileAccepter;
import ch.systemsx.cisd.etlserver.IHCSImageFileExtractor;
import ch.systemsx.cisd.etlserver.plugins.AbstractHCSImageFileExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * A <code>IHCSImageFileExtractor</code> implementation suitable for <i>LMC</i>.
 * 
 * @author Tomasz Pylak
 */
public class HCSImageFileExtractor extends AbstractHCSImageFileExtractor implements
        IHCSImageFileExtractor
{
    private static final String[] IMAGE_EXTENSIONS = new String[]
        { "tif", "jpg" };

    private static final String[] CHANNEL_NAMES = new String[]
        { "dapi1", "gfp1" };

    public HCSImageFileExtractor(final Properties properties)
    {
        super(properties);
    }

    @Private
    HCSImageFileExtractor(Geometry wellGeometry)
    {
        super(wellGeometry);
    }

    @Override
    protected final int getChannelWavelength(final String channel)
    {
        for (int channelIndex = 1; channelIndex <= CHANNEL_NAMES.length; channelIndex++)
        {
            if (channel.equalsIgnoreCase(CHANNEL_NAMES[channelIndex - 1]))
            {
                return channelIndex;
            }
        }
        return 0; // unknown channel name
    }

    /**
     * Extracts the well location from given <var>value</var>, following the convention adopted
     * here.<br>
     * Here is a numbering example for a 3x3 plate:<br>
     * 1 4 7<br>
     * 2 5 8<br>
     * 3 6 9<br>
     * <p>
     * Returns <code>null</code> if the operation fails.
     * </p>
     */
    @Override
    protected final Location tryGetWellLocation(final String wellLocation)
    {
        try
        {
            int tileNumber = Integer.parseInt(wellLocation);
            Location letterLoc = Location.tryCreateLocationFromPosition(tileNumber, wellGeometry);
            // transpose rows with columns
            return new Location(letterLoc.getY(), letterLoc.getX());
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

    private List<IFile> listImageFiles(final IDirectory directory)
    {
        return directory.listFiles(IMAGE_EXTENSIONS, false);
    }

}
