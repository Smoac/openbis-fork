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

package eu.basysbio.cisd.dss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.lemnik.eodsql.DataSet;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class DatabaseFeeder
{
    private final ITimeSeriesDAO dao;
    private final IEncapsulatedOpenBISService service;
    private final TimeSeriesDataSetUploaderParameters parameters;

    private final Map<ExperimentIdentifier, Experiment> experimentCache =
        new HashMap<ExperimentIdentifier, Experiment>();

    DatabaseFeeder(ITimeSeriesDAO dao, IEncapsulatedOpenBISService service,
            TimeSeriesDataSetUploaderParameters parameters)
    {
        this.dao = dao;
        this.service = service;
        this.parameters = parameters;
    }
    
    void feedDatabase(DataSetInformation dataSetInformation, List<Column> commonColumns,
            List<Column> dataColumns)
    {
        assertExperiment(dataSetInformation, dataColumns);
        long dataSetID = getOrCreateDataSet(dataSetInformation);
        RowIDManager rowIDManager = createRowsAndCommonColumns(dataSetID, commonColumns);
        Set<DataColumnHeader> headers = new HashSet<DataColumnHeader>();
        for (Column dataColumn : dataColumns)
        {
            createDataColumn(commonColumns, dataColumn, dataSetInformation, headers, dataSetID,
                    rowIDManager);
        }
    }
    
    private void assertExperiment(DataSetInformation dataSetInformation, List<Column> dataColumns)
    {
        String code = dataSetInformation.getExperimentIdentifier().getExperimentCode();
        Set<String> invalidExperimentCodes = new LinkedHashSet<String>();
        Set<String> experimentCodes = new LinkedHashSet<String>();
        for (Column dataColumn : dataColumns)
        {
            DataColumnHeader dataColumnHeader = new DataColumnHeader(dataColumn.getHeader());
            String experimentCode = createExperimentCode(dataColumnHeader);
            experimentCodes.add(experimentCode);
            if (code.equalsIgnoreCase(experimentCode) == false)
            {
                invalidExperimentCodes.add(experimentCode);
            }
        }
        if (invalidExperimentCodes.isEmpty() == false)
        {
            if (experimentCodes.size() == 1 && invalidExperimentCodes.size() == 1)
            {
                throw new UserFailureException("Data should be uploaded for experiment '"
                        + invalidExperimentCodes.iterator().next() + "' instead of '" + code + "'.");
            } else
            {
                throw new UserFailureException("Data columns found for more than one experiment: "
                        + experimentCodes);
            }
        }
    }

    private long getOrCreateDataSet(DataSetInformation dataSetInformation)
    {
        ExperimentIdentifier experimentIdentifier =
                dataSetInformation.getExperimentIdentifier();
        Experiment experiment = tryToGetExperiment(experimentIdentifier);
        if (experiment == null)
        {
            throw new UserFailureException("Unknown experiment: " + experimentIdentifier);
        }
        String experimentPermID = experiment.getPermId();
        Long experimentID = dao.tryToGetExperimentIDByPermID(experimentPermID);
        if (experimentID == null)
        {
            experimentID = dao.createExperiment(experimentPermID);
        }
        String dataSetCode = dataSetInformation.getDataSetCode();
        Long dataSetID = dao.tryToGetDataSetIDByPermID(dataSetCode);
        if (dataSetID == null)
        {
            dataSetID = dao.createDataSet(dataSetCode, experimentID);
        }
        return dataSetID;
    }

    private void createDataColumn(List<Column> commonColumns, Column dataColumn,
            DataSetInformation dataSetInformation, Set<DataColumnHeader> headers, long dataSetID,
            RowIDManager rowIDManager)
    {
        DataColumnHeader dataColumnHeader = new DataColumnHeader(dataColumn.getHeader());
        if (headers.contains(dataColumnHeader))
        {
            throw new UserFailureException("Data column '" + dataColumnHeader + "' appears twice.");
        }
        assertUniqueDataColumnHeader(dataColumnHeader);
        headers.add(dataColumnHeader);

        long columnID = dao.createDataColumn(dataColumnHeader, dataSetID, null);
        createDataValues(dataColumn, rowIDManager, columnID);
    }

    private void createDataValues(Column dataColumn, RowIDManager rowIDManager, long columnID)
    {
        List<String> values = dataColumn.getValues();
        for (int i = 0; i < values.size(); i++)
        {
            Double value;
            try
            {
                value = Double.parseDouble(values.get(i));
            } catch (NumberFormatException ex)
            {
                value = null;
            }
            dao.createDataValue(columnID, rowIDManager.getOrCreateRow(i), value);
        }
    }

    private RowIDManager createRowsAndCommonColumns(long dataSetID, List<Column> commonColumns)
    {
        RowIDManager rowIDManager = new RowIDManager(dao);
        for (Column column : commonColumns)
        {
            column.getHeader();
            long columnID = dao.createColumn(column.getHeader(), dataSetID);
            List<String> values = column.getValues();
            for (int i = 0; i < values.size(); i++)
            {
                dao.createValue(columnID, rowIDManager.getOrCreateRow(i), values.get(i));
            }
        }
        return rowIDManager;
    }

    private void assertUniqueDataColumnHeader(DataColumnHeader dataColumnHeader)
    {
        DataSet<String> permIDs = dao.listDataSetsByDataColumnHeader(dataColumnHeader);
        List<String> dataSets = new ArrayList<String>();
        try
        {
            for (String id : permIDs)
            {
                dataSets.add(id);
            }
        } finally
        {
            permIDs.close();
        }
        if (dataSets.isEmpty() == false)
        {
            throw new UserFailureException("For data column '" + dataColumnHeader
                    + "' following data sets have already been registered: " + dataSets);
        }
    }

    private String createExperimentCode(DataColumnHeader dataColumnHeader)
    {
        return parameters.getExperimentCodeFormat().format(
                new Object[]
                    { dataColumnHeader.getExperimentCode(),
                            dataColumnHeader.getCultivationMethod(),
                            dataColumnHeader.getBiologicalReplicateCode() });
    }

    private Experiment tryToGetExperiment(ExperimentIdentifier experimentIdentifier)
    {
        Experiment experiment = experimentCache.get(experimentIdentifier);
        if (experiment == null)
        {
            experiment = service.tryToGetExperiment(experimentIdentifier);
            experimentCache.put(experimentIdentifier, experiment);
        }
        return experiment;
    }

}
