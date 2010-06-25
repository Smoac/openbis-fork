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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import ch.systemsx.cisd.bds.hcs.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSDatasetLoader;

/**
 * Creates {@link PlateImageParameters} using {@link IHCSDatasetLoader}.
 * 
 * @author Piotr Buczek
 */
public class PlateImageParametersFactory
{
    public static PlateImageParameters create(IHCSDatasetLoader loader)
    {
        final String datasetCode = loader.getDatasetPermId();
        final Geometry plateGeometry = loader.getPlateGeometry();
        final Geometry wellGeometry = loader.getWellGeometry();
        final List<String> channelsNames = loader.getChannelsNames();

        PlateImageParameters params = new PlateImageParameters();
        params.setDatasetCode(datasetCode);
        params.setRowsNum(plateGeometry.getRows());
        params.setColsNum(plateGeometry.getColumns());
        params.setTileRowsNum(wellGeometry.getRows());
        params.setTileColsNum(wellGeometry.getColumns());
        List<String> escapedChannelNames = new ArrayList<String>();
        for (String name : channelsNames)
        {
            escapedChannelNames.add(StringEscapeUtils.escapeCsv(name));
        }
        params.setChannelsNames(escapedChannelNames);
        return params;
    }
}
