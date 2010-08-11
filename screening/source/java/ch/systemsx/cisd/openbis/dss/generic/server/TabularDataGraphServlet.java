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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ITabularData;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.shared.DssScreeningUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.PlateUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;

/**
 * Create a graph from the imaging database.
 * <p>
 * TODO 2010-08-09, CR, LMS-1692, This implementation is inefficient. The better way to implement
 * this would be to lazily get feature vectors necessary for generating the graph, not to egerly get
 * all feature vectors.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class TabularDataGraphServlet extends AbstractTabularDataGraphServlet
{

    private static final long serialVersionUID = 1L;

    private IImagingQueryDAO imagingDbDao;

    private IEncapsulatedOpenBISService openBisService;

    /**
     * An CSV-file-like interface to feature data from the database.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private final class ImagingTabularData implements ITabularData
    {
        private static final String WELL_NAME_COLUMN = "WellName";

        private static final String WELL_ROW_COLUMN = "Row";

        private static final String WELL_COLUMN_COLUMN = "Column";

        private final IImagingQueryDAO dao;

        private final IEncapsulatedOpenBISService service;

        private final String dataSetCode;

        private String[] headerTokens;

        private ArrayList<String[]> lines;

        private ImagingTabularData(IImagingQueryDAO dao, IEncapsulatedOpenBISService service,
                String dataSetCode)
        {
            this.dao = dao;
            this.service = service;
            this.dataSetCode = dataSetCode;
            initialize();
        }

        private void initialize()
        {
            final FeatureTableBuilder tableBuilder = new FeatureTableBuilder(dao, service);
            tableBuilder.addFeatureVectorsOfDataSet(dataSetCode);

            List<String> featureNames = tableBuilder.getFeatureNames();
            int headerTokensLength = featureNames.size() + 3;
            headerTokens = new String[headerTokensLength];
            headerTokens[0] = WELL_NAME_COLUMN;
            headerTokens[1] = WELL_ROW_COLUMN;
            headerTokens[2] = WELL_COLUMN_COLUMN;

            int i = 1;
            for (String name : featureNames)
            {
                headerTokens[i++] = name;
            }

            lines = new ArrayList<String[]>();

            final List<FeatureTableRow> rows = tableBuilder.createFeatureTableRows();
            for (FeatureTableRow row : rows)
            {
                String[] line = new String[headerTokensLength];
                WellPosition pos = row.getWellPosition();
                String rowLetter = PlateUtils.translateRowNumberIntoLetterCode(pos.getWellRow());
                String columnNumber = Integer.toString(row.getWellPosition().getWellColumn());
                line[0] = rowLetter + columnNumber;
                line[1] = rowLetter;
                line[2] = columnNumber;
                i = 1;
                float[] values = row.getFeatureValues();
                for (float value : values)
                {
                    line[i++] = Float.toString(value);
                }
                lines.add(line);
            }
            // final ImgDatasetDTO dataSet = dao.tryGetDatasetByPermId(dataSetCode);
            // if (dataSet == null)
            // {
            // throw new UserFailureException("Unkown data set " + dataSetCode);
            // }
            //
            // final List<ImgFeatureDefDTO> featureDefinitions =
            // dao.listFeatureDefsByDataSetId(dataSet.getId());
            //
            // int headersLength = featureDefinitions.size();
            // headerTokens = new String[headersLength];
            // lines = new ArrayList<String[]>();
            // int featureDefCount = 0;
            // for (ImgFeatureDefDTO featureDefinition : featureDefinitions)
            // {
            // headerTokens[featureDefCount++] = featureDefinition.getName();
            // }
            // int numRows = dataSet.getFieldNumberOfRows();
            // int numCols = dataSet.getFieldNumberOfColumns();
            // for (int row = 0; row < numRows; ++row)
            // {
            // for (int col = 0; col < numCols; ++col)
            // {
            // for (ImgFeatureDefDTO featureDefinition : featureDefinitions)
            // {
            // List<ImgFeatureValuesDTO> featureValues =
            // dao.getFeatureValues(featureDefinition);
            // }
            // }
            // }
        }

        public List<String[]> getDataLines()
        {
            return lines;
        }

        public String[] getHeaderTokens()
        {
            return headerTokens;
        }

    }

    @Override
    protected ITabularData getDatasetLines(String dataSetCode, String filePathOrNull)
            throws IOException
    {
        return new ImagingTabularData(getDAO(), getService(), dataSetCode);
    }

    private IImagingQueryDAO getDAO()
    {
        synchronized (this)
        {
            if (imagingDbDao == null)
            {
                imagingDbDao = DssScreeningUtils.createQuery();
            }
        }
        return imagingDbDao;
    }

    private IEncapsulatedOpenBISService getService()
    {
        synchronized (this)
        {
            if (openBisService == null)
            {
                openBisService = ServiceProvider.getOpenBISService();
            }
        }
        return openBisService;
    }

}
