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

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import net.lemnik.eodsql.DataSet;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TabSeparatedValueTable;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
class TimeSeriesDataSetUploader extends AbstractDataSetUploader
{
    static final IDataSetUploaderFactory FACTORY = new IDataSetUploaderFactory()
        {

            public IDataSetUploader create(DataSetInformation dataSetInformation,
                    DataSource dataSource, IEncapsulatedOpenBISService service,
                    TimeSeriesDataSetUploaderParameters parameters)
            {
                return new TimeSeriesDataSetUploader(dataSource, service, parameters, false);
            }

            public IDataSetUploader create(DataSetInformation dataSetInformation,
                    ITimeSeriesDAO dao, IEncapsulatedOpenBISService service,
                    TimeSeriesDataSetUploaderParameters parameters)
            {
                return new TimeSeriesDataSetUploader(dao, service, parameters, false);
            }
        };

    static final IDataSetUploaderFactory FACTORY_WO_TIME_POINT = new IDataSetUploaderFactory()
        {

            public IDataSetUploader create(DataSetInformation dataSetInformation,
                    DataSource dataSource, IEncapsulatedOpenBISService service,
                    TimeSeriesDataSetUploaderParameters parameters)
            {
                return new TimeSeriesDataSetUploader(dataSource, service, parameters, true);
            }

            public IDataSetUploader create(DataSetInformation dataSetInformation,
                    ITimeSeriesDAO dao, IEncapsulatedOpenBISService service,
                    TimeSeriesDataSetUploaderParameters parameters)
            {
                return new TimeSeriesDataSetUploader(dao, service, parameters, true);
            }
        };

    private final Map<ExperimentIdentifier, Experiment> experimentCache =
            new HashMap<ExperimentIdentifier, Experiment>();

    private final boolean ignoringTimePointDataSetCreation;

    TimeSeriesDataSetUploader(DataSource dataSource, IEncapsulatedOpenBISService service,
            TimeSeriesDataSetUploaderParameters parameters, boolean ignoringTimePointDataSetCreation)
    {
        super(dataSource, service, parameters);
        this.ignoringTimePointDataSetCreation = ignoringTimePointDataSetCreation;
    }

    TimeSeriesDataSetUploader(ITimeSeriesDAO dao, IEncapsulatedOpenBISService service,
            TimeSeriesDataSetUploaderParameters parameters, boolean ignoringTimePointDataSetCreation)
    {
        super(dao, service, parameters);
        this.ignoringTimePointDataSetCreation = ignoringTimePointDataSetCreation;
    }

    @Override
    protected void handleTSVFile(File tsvFile, DataSetInformation dataSetInformation,
            IDropBoxFeeder feeder)
    {
        // TODO 2010-08-08, IA: Workaround enabling upload of LCA_MIC data
        // ("dataset for header ... already registered"). Remove after problem is fixed.
        if (dataSetInformation.getDataSetType().getCode().startsWith("LCA_MIC"))
        {
            return;
        }
        FileReader reader = null;
        try
        {
            reader = new FileReader(tsvFile);
            String fileName = tsvFile.toString();
            TabSeparatedValueTable table =
                    new TabSeparatedValueTable(reader, fileName, parameters.isIgnoreEmptyLines(),
                            true, false);
            List<Column> columns = table.getColumns();
            List<Column> commonColumns = new ArrayList<Column>();
            List<Column> dataColumns = new ArrayList<Column>();
            for (Column column : columns)
            {
                String header = column.getHeader();
                if (HeaderUtils.isDataColumnHeader(header))
                {
                    dataColumns.add(column);
                } else
                {
                    commonColumns.add(column);
                }
            }
            assertExperiment(dataSetInformation, dataColumns);
            ExperimentIdentifier experimentIdentifier =
                    dataSetInformation.getExperimentIdentifier();
            long dataSetID = getOrCreateDataSet(dataSetInformation, experimentIdentifier);
            RowIDManager rowIDManager = createRowsAndCommonColumns(dataSetID, commonColumns);
            Set<DataColumnHeader> headers = new HashSet<DataColumnHeader>();
            for (Column dataColumn : dataColumns)
            {
                createDataSet(commonColumns, dataColumn, dataSetInformation, headers, dataSetID,
                        rowIDManager, feeder);
            }
        } catch (RuntimeException ex)
        {
            throw ex;
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(reader);
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

    private void createDataSet(List<Column> commonColumns, Column dataColumn,
            DataSetInformation dataSetInformation, Set<DataColumnHeader> headers, long dataSetID,
            RowIDManager rowIDManager, IDropBoxFeeder feeder)
    {
        DataColumnHeader dataColumnHeader = new DataColumnHeader(dataColumn.getHeader());
        if (headers.contains(dataColumnHeader))
        {
            throw new UserFailureException("Data column '" + dataColumnHeader + "' appears twice.");
        }
        assertUniqueDataColumnHeader(dataColumnHeader);
        Experiment experiment = getExperiment(dataColumnHeader, dataSetInformation);
        String sampleCode = createSampleCode(dataColumnHeader).toUpperCase();
        if (ignoringTimePointDataSetCreation == false)
        {
            createSampleIfNecessary(sampleCode, dataColumnHeader.getTimePoint(), experiment);
        }
        headers.add(dataColumnHeader);

        createSampleAndDataColumn(dataColumn, dataSetID, rowIDManager, dataColumnHeader,
                experiment, sampleCode);

        if (ignoringTimePointDataSetCreation == false)
        {
            feeder.feed(dataSetInformation.tryGetUploadingUserEmail(), sampleCode, commonColumns,
                    dataColumn);
        }
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

    private long createSampleIfNecessary(String sampleCode, int timePoint, Experiment experiment)
    {
        ListSampleCriteria criteria =
                ListSampleCriteria.createForExperiment(new TechId(experiment.getId()));
        List<Sample> samples = service.listSamples(criteria);
        for (Sample sample : samples)
        {
            if (sample.getCode().equals(sampleCode))
            {
                return sample.getId();
            }
        }
        NewSample sample = new NewSample();
        SampleType sampleType = new SampleType();
        sampleType.setCode(parameters.getSampleTypeCode());
        sample.setSampleType(sampleType);
        sample.setExperimentIdentifier(experiment.getIdentifier());
        String sampleIdentifier = Util.createSampleIdentifier(experiment, sampleCode);
        sample.setIdentifier(sampleIdentifier);
        EntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("TIME_POINT");
        property.setPropertyType(propertyType);
        property.setValue(Integer.toString(timePoint));
        sample.setProperties(new EntityProperty[]
            { property });
        return service.registerSample(sample, null);
    }

    private Experiment getExperiment(DataColumnHeader dataColumnHeader,
            DataSetInformation dataSetInformation)
    {
        ExperimentIdentifier experimentIdentifier =
                createExperimentIdentifier(dataColumnHeader, dataSetInformation);
        Experiment experiment = tryToGetExperiment(experimentIdentifier);
        if (experiment == null)
        {
            throw new UserFailureException("No experiment found for experiment identifier "
                    + experimentIdentifier);
        }
        return experiment;
    }

    private ExperimentIdentifier createExperimentIdentifier(DataColumnHeader dataColumnHeader,
            DataSetInformation dataSetInformation)
    {
        String experimentCode = createExperimentCode(dataColumnHeader);
        ExperimentIdentifier experimentIdentifier = dataSetInformation.getExperimentIdentifier();
        if (experimentIdentifier == null)
        {
            throw new UserFailureException(
                    "Data set should be registered for an experiment and not for a sample.");
        }
        return new ExperimentIdentifier(experimentIdentifier, experimentCode);
    }

    private String createExperimentCode(DataColumnHeader dataColumnHeader)
    {
        return parameters.getExperimentCodeFormat().format(
                new Object[]
                    { dataColumnHeader.getExperimentCode(),
                            dataColumnHeader.getCultivationMethod(),
                            dataColumnHeader.getBiologicalReplicateCode() });
    }

    private String createSampleCode(DataColumnHeader dataColumnHeader)
    {
        return parameters.getSampleCodeFormat().format(
                new Object[]
                    { createExperimentCode(dataColumnHeader), dataColumnHeader.getTimePointType(),
                            Integer.toString(dataColumnHeader.getTimePoint()) });
    }

    private long getOrCreateDataSet(DataSetInformation dataSetInformation,
            ExperimentIdentifier experimentIdentifier)
    {
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

    private void createSampleAndDataColumn(Column dataColumn, long dataSetID,
            RowIDManager rowIDManager, DataColumnHeader dataColumnHeader, Experiment experiment,
            String sampleCode)
    {
        Long sampleID = null;
        if (ignoringTimePointDataSetCreation == false)
        {
            sampleID = getOrCreateSample(experiment, sampleCode);
        }
        long columnID = dao.createDataColumn(dataColumnHeader, dataSetID, sampleID);
        createDataValues(dataColumn, rowIDManager, columnID);
    }

    private long getOrCreateSample(Experiment experiment, String sampleCode)
    {
        String sampleIdentifier = Util.createSampleIdentifier(experiment, sampleCode);
        Sample sample =
                service.tryGetSampleWithExperiment(SampleIdentifierFactory.parse(sampleIdentifier));
        String samplePermID = sample.getPermId();
        Long sampleId = dao.tryToGetSampleIDByPermID(samplePermID);
        if (sampleId == null)
        {
            sampleId = dao.createSample(samplePermID);
        }
        return sampleId;
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
