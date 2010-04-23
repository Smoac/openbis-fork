/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.genedata;

import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.etlserver.HCSImageFileExtractionResult;
import ch.systemsx.cisd.etlserver.IHCSImageFileAccepter;
import ch.systemsx.cisd.etlserver.IHCSImageFileExtractor;
import ch.systemsx.cisd.etlserver.plugins.AbstractHCSImageFileExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Franz-Josef Elmer
 */
public class HCSImageFileExtractor extends AbstractHCSImageFileExtractor implements
        IHCSImageFileExtractor
{
    public HCSImageFileExtractor(final Properties properties)
    {
        super(properties);
    }

    @Private
    HCSImageFileExtractor(Geometry wellGeometry)
    {
        super(wellGeometry);
    }

    public HCSImageFileExtractionResult process(IDirectory incomingDataSetDirectory,
            DataSetInformation dataSetInformation, IHCSImageFileAccepter accepter)
    {
        assert incomingDataSetDirectory != null;
        return process(incomingDataSetDirectory.listFiles(null, false), dataSetInformation,
                accepter);
    }

    @Override
    protected String[] tryToSplitIntoTokens(String imageFileName)
    {
        String[] tokens = new String[4];
        int indexOfChannelSeparator = imageFileName.indexOf('-');
        if (indexOfChannelSeparator < 0)
        {
            return null;
        }
        tokens[3] = imageFileName.substring(indexOfChannelSeparator + 1);
        String wellAndTileCoordinates = imageFileName.substring(0, indexOfChannelSeparator);
        if (wellAndTileCoordinates.length() != 9)
        {
            return null;
        }
        tokens[1] = wellAndTileCoordinates.substring(0, 6);
        tokens[2] = wellAndTileCoordinates.substring(6);
        return tokens;
    }

    @Override
    protected int getChannelWavelength(String channel)
    {
        try
        {
            return Integer.parseInt(channel) + 1;
        } catch (NumberFormatException ex)
        {
            return 0;
        }
    }

    @Override
    protected Location tryGetWellLocation(String wellLocation)
    {
        return new Location(1, 1);
    }

    @Override
    protected Location tryGetPlateLocation(String plateLocation)
    {
        try
        {
            int row = Integer.parseInt(plateLocation.substring(0, 3));
            int column = Integer.parseInt(plateLocation.substring(3));
            return new Location(column, row);
        } catch (NumberFormatException ex)
        {
            return null;
        }
    }

}
