/*
 * Copyright 2009 ETH Zuerich, CISD
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
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSDatasetLoader;

/**
 * Loads content of datasets by contacting DSS.
 * 
 * @author Tomasz Pylak
 */
public class DatasetReportsLoader
{
    public static TableModel loadAnalysisResults(List<String> datasets, String datastoreCode,
            IExternalDataTable externalDataTable)
    {
        return externalDataTable.createReportFromDatasets(
                ScreeningConstants.PLATE_IMAGE_ANALYSIS_REPORT_KEY, datastoreCode, datasets);
    }

    public static List<PlateImageParameters> loadPlateImageParameters(List<String> datasetCodes,
            IScreeningBusinessObjectFactory boFactory)
    {
        List<PlateImageParameters> paramsList = new ArrayList<PlateImageParameters>();

        for (String datasetCode : datasetCodes)
        {
            final IHCSDatasetLoader loader = boFactory.createHCSDatasetLoader(datasetCode);
            paramsList.add(createImageParameters(loader));
        }
        return paramsList;
    }

    // TODO 2010-06-25, Piotr Buczek: move
    public static PlateImageParameters createImageParameters(IHCSDatasetLoader loader)
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
