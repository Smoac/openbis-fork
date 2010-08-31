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
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

import eu.basysbio.cisd.db.TimeSeriesColumnDescriptor;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class DatabaseFeeder
{
    static final String UPLOADER_EMAIL_KEY = "UPLOADER_EMAIL";
    private static final int POSITION_COLUMN_INDEX = 5;
    private static final int HEIGHT_COLUMN_INDEX = 6;
    private static final int SCORE_COLUMN_INDEX = 7;
    
    private static interface IDataSetProvider
    {
        DataSet<String> getDataSetsByDataColumnHeader(DataColumnHeader dataColumnHeader);
    }
    
    private final class DataSetProviderForTimeSeriesData implements IDataSetProvider
    {
        public DataSet<String> getDataSetsByDataColumnHeader(DataColumnHeader dataColumnHeader)
        {
            return dao.listDataSetsByTimeSeriesDataColumnHeader(dataColumnHeader);
        }
    }
    
    private final class DataSetProviderForChipChipData implements IDataSetProvider
    {
        public DataSet<String> getDataSetsByDataColumnHeader(DataColumnHeader dataColumnHeader)
        {
            return dao.listDataSetsByChipChipDataColumnHeader(dataColumnHeader);
        }
    }
    
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
    
    void feedDatabase(DataSetInformation dataSetInformation, List<Column> columns)
    {
        assertExperiment(dataSetInformation, columns);
        long dataSetID = getOrCreateDataSet(dataSetInformation);
        String dataSetType = dataSetInformation.getDataSetType().getCode();
        if (dataSetType.equals("CHIP_CHIP"))
        {
            feedDatabaseWithChipChipData(columns, dataSetID);
        } else
        {
            feedDatabaseWithTimeSeriesData(columns, dataSetID);
        }
    }

    private void feedDatabaseWithTimeSeriesData(List<Column> columns, long dataSetID)
    {
        List<IColumnInjection<TimeSeriesValue>> columnInjections =
                createInjections(columns, TimeSeriesInjectionFactory.values());
        List<TimeSeriesValue> dataValues = new ArrayList<TimeSeriesValue>();
        Set<DataColumnHeader> headers = new HashSet<DataColumnHeader>();
        ValueGroupIdGenerator valueGroupIdGenerator = new ValueGroupIdGenerator(dao);
        for (int colIndex = 0; colIndex < columns.size(); colIndex++)
        {
            Column column = columns.get(colIndex);
            String header = column.getHeader();
            if (HeaderUtils.isDataColumnHeader(header))
            {
                TimeSeriesValue timeSeriesValue = new TimeSeriesValue();
                timeSeriesValue.setColumnIndex(colIndex);
                DataColumnHeader dataColumnHeader = new DataColumnHeader(header);
                assertUniqueDataColumnHeader(dataColumnHeader, headers,
                        new DataSetProviderForTimeSeriesData());
                ValueGroupDescriptor valueGroupDescriptor =
                        new ValueGroupDescriptor(dataColumnHeader);
                timeSeriesValue.setValueGroupId(valueGroupIdGenerator
                        .getValueGroupIdFor(valueGroupDescriptor));
                timeSeriesValue.setDescriptor(new TimeSeriesColumnDescriptor(valueGroupDescriptor,
                        dataColumnHeader));
                for (int i = 0, n = column.getValues().size(); i < n; i++)
                {
                    Double value = Util.parseDouble(column, i);
                    dataValues.add(timeSeriesValue.createFor(i, value, columnInjections));
                }
            }
        }
        String identifierType = columns.get(0).getHeader();
        dao.insertTimeSeriesValues(dataSetID, identifierType, dataValues);
    }

    private void feedDatabaseWithChipChipData(List<Column> columns, long dataSetID)
    {
        List<IColumnInjection<ChipChipData>> columnInjections =
                createInjections(columns, ChipChipInjectionFactory.values());
        List<ChipChipData> dataValues = new ArrayList<ChipChipData>();
        ChipChipData chipChipData = new ChipChipData();
        TimeSeriesColumnDescriptor peakColumnDescriptor =
                createDataColumnDescriptor(columns, POSITION_COLUMN_INDEX);
        chipChipData.setDescriptor(peakColumnDescriptor);
        chipChipData.setChipPeakPositionScale(peakColumnDescriptor.getScale());
        chipChipData.setChipLocalHeightScale(createDataColumnDescriptor(columns,
                HEIGHT_COLUMN_INDEX).getScale());
        chipChipData.setChipScoreScale(createDataColumnDescriptor(columns, SCORE_COLUMN_INDEX)
                .getScale());
        Column positions = columns.get(POSITION_COLUMN_INDEX);
        Column heights = columns.get(HEIGHT_COLUMN_INDEX);
        Column scores = columns.get(SCORE_COLUMN_INDEX);
        for (int rowIndex = 0, n = positions.getValues().size(); rowIndex < n; rowIndex++)
        {
            Integer position = Util.parseInteger(positions, rowIndex);
            Double height = Util.parseDouble(heights, rowIndex);
            Double score = Util.parseDouble(scores, rowIndex);
            dataValues.add(chipChipData.createFor(rowIndex, position, height, score,
                    columnInjections));
        }
        dao.insertChipChipValues(dataSetID, dataValues);
    }

    private TimeSeriesColumnDescriptor createDataColumnDescriptor(List<Column> columns,
            int columnIndex)
    {
        Column column = columns.get(columnIndex);
        DataColumnHeader dataColumnHeader = new DataColumnHeader(column.getHeader());
        assertUniqueDataColumnHeader(dataColumnHeader, new HashSet<DataColumnHeader>(),
                new DataSetProviderForChipChipData());
        ValueGroupDescriptor valueGroupDescriptor = new ValueGroupDescriptor(dataColumnHeader);
        return new TimeSeriesColumnDescriptor(valueGroupDescriptor, dataColumnHeader);
    }

    private <T extends AbstractDataValue> List<IColumnInjection<T>> createInjections(List<Column> columns,
            IInjectionFactory<T>[] enums)
    {
        List<IColumnInjection<T>> columnInjections = new ArrayList<IColumnInjection<T>>();
        for (IInjectionFactory<T> factory : enums)
        {
            IColumnInjection<T> injection = factory.tryToCreate(columns);
            if (injection != null)
            {
                columnInjections.add(injection);
            }
        }
        return columnInjections;
    }
    
    private void assertUniqueDataColumnHeader(DataColumnHeader dataColumnHeader,
            Set<DataColumnHeader> headers, IDataSetProvider dataSetProvider)
    {
    if (headers.contains(dataColumnHeader))
        {
            throw new UserFailureException("Data column '" + dataColumnHeader + "' appears twice.");
        }
        DataSet<String> permIDs = dataSetProvider.getDataSetsByDataColumnHeader(dataColumnHeader);
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
        headers.add(dataColumnHeader);
    }

    private void assertExperiment(DataSetInformation dataSetInformation, List<Column> columns)
    {
        String code = dataSetInformation.getExperimentIdentifier().getExperimentCode();
        Set<String> invalidExperimentCodes = new LinkedHashSet<String>();
        Set<String> experimentCodes = new LinkedHashSet<String>();
        for (Column column : columns)
        {
            String header = column.getHeader();
            if (HeaderUtils.isDataColumnHeader(header))
            {
                DataColumnHeader dataColumnHeader = new DataColumnHeader(header);
                String experimentCode = createExperimentCode(dataColumnHeader);
                experimentCodes.add(experimentCode);
                if (code.equalsIgnoreCase(experimentCode) == false)
                {
                    invalidExperimentCodes.add(experimentCode);
                }
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
        String dataSetCode = dataSetInformation.getDataSetCode();
        Long dataSetID = dao.tryToGetDataSetIDByPermID(dataSetCode);
        if (dataSetID == null)
        {
            String eMailAddress = getUploaderEMailAddress(dataSetInformation);
            dataSetID = dao.createDataSet(dataSetCode, eMailAddress, experiment);
        }
        return dataSetID;
    }
    
    private String getUploaderEMailAddress(DataSetInformation dataSetInformation)
    {
        List<NewProperty> properties = dataSetInformation.getDataSetProperties();
        for (NewProperty property : properties)
        {
            if (property.getPropertyCode().equals(UPLOADER_EMAIL_KEY))
            {
                return property.getValue();
            }
        }
        throw new IllegalArgumentException("No uploader email address specified: " + dataSetInformation);
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
