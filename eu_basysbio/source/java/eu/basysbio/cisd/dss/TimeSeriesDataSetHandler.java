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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IOutputStream;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.AbstractPostRegistrationDataSetHandlerForFileBasedUndo;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TabSeparatedValueTable;
import ch.systemsx.cisd.etlserver.utils.TableBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * @author Franz-Josef Elmer
 */
class TimeSeriesDataSetHandler extends AbstractPostRegistrationDataSetHandlerForFileBasedUndo
{
    private static final class DataSetPropertiesBuilder extends TableBuilder
    {
        private final DataSetPropertiesValidator propertiesValidator;

        private final String dataSetType;

        DataSetPropertiesBuilder(DataSetPropertiesValidator propertiesValidator, String dataSetType)
        {
            super("property", "value");
            this.propertiesValidator = propertiesValidator;
            this.dataSetType = dataSetType;
        }

        void addProperty(TimePointPropertyType key, DataColumnHeader header)
        {
            addProperty(key, key.getElement(header));
        }

        void addProperty(TimePointPropertyType key, String value)
        {
            propertiesValidator.assertValidFor(dataSetType, key, value.toUpperCase());
            addRow(key.toString(), value);
        }
    }

    static final String DATA_SET_TYPE = "TIME_SERIES";

    static final String DATA_FILE_TYPE = ".data.txt";

    private static final Pattern DATA_COLUMN_HEADER_PATTERN =
            Pattern.compile(".*(" + DataColumnHeader.SEPARATOR + ".*)+");

    static final String EXPERIMENT_CODE_TEMPLATE_KEY = "experiment-code-template";

    static final String DEFAULT_EXPERIMENT_CODE_TEMPLATE = "{0}_{1}_{2}";

    static final String SAMPLE_CODE_TEMPLATE_KEY = "sample-code-template";

    static final String DEFAULT_SAMPLE_CODE_TEMPLATE = "{0}_{1}_{2}";

    static final String SAMPLE_TYPE_CODE_KEY = "sample-type-code";

    static final String DEFAULT_SAMPLE_TYPE_CODE = "TIME_POINT";

    static final String IGNORE_EMPTY_LINES_KEY = "ignore-empty-lines";

    static final String TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY = "time-point-data-set-drop-box-path";

    static final String TIME_POINT_DATA_SET_FILE_NAME_SEPARATOR_KEY =
            "time-point-data-set-file-name-separator";

    static final String DEFAULT_TIME_POINT_DATA_SET_FILE_NAME_SEPARATOR = ".";

    static final String DATA_SET_PROPERTIES_FILE_NAME_KEY = "data-set-properties-file-name";

    static final String TRANSLATION_KEY = "translation.";

    private final MessageFormat experimentCodeFormat;

    private final MessageFormat sampleCodeFormat;

    private final IEncapsulatedOpenBISService service;

    private final String sampleTypeCode;

    private final File dropBox;

    private final String dataSetPropertiesFileName;

    private final DataSetTypeTranslator translator;

    private final String timePointDataSetFileSeparator;

    private final boolean ignoreEmptyLines;

    private final DataSetPropertiesValidator dataSetPropertiesValidator;

    TimeSeriesDataSetHandler(Properties properties, IEncapsulatedOpenBISService service)
    {
        super(FileOperations.getInstance());
        this.service = service;
        sampleTypeCode = properties.getProperty(SAMPLE_TYPE_CODE_KEY, DEFAULT_SAMPLE_TYPE_CODE);
        ignoreEmptyLines = PropertyUtils.getBoolean(properties, IGNORE_EMPTY_LINES_KEY, true);
        experimentCodeFormat =
                new MessageFormat(properties.getProperty(EXPERIMENT_CODE_TEMPLATE_KEY,
                        DEFAULT_EXPERIMENT_CODE_TEMPLATE));
        sampleCodeFormat =
                new MessageFormat(properties.getProperty(SAMPLE_CODE_TEMPLATE_KEY,
                        DEFAULT_SAMPLE_CODE_TEMPLATE));
        String dropBoxPath =
                PropertyUtils.getMandatoryProperty(properties,
                        TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY);
        dropBox = new File(dropBoxPath);
        if (dropBox.isDirectory() == false)
        {
            throw new ConfigurationFailureException(
                    "Drop box for time-point data sets does not exists or isn't a folder: "
                            + dropBox.getAbsolutePath());
        }
        timePointDataSetFileSeparator =
                properties.getProperty(TIME_POINT_DATA_SET_FILE_NAME_SEPARATOR_KEY,
                        DEFAULT_TIME_POINT_DATA_SET_FILE_NAME_SEPARATOR);
        dataSetPropertiesFileName =
                PropertyUtils.getMandatoryProperty(properties, DATA_SET_PROPERTIES_FILE_NAME_KEY);
        translator =
                new DataSetTypeTranslator(ExtendedProperties.getSubset(properties, TRANSLATION_KEY,
                        true));
        dataSetPropertiesValidator =
                new DataSetPropertiesValidator(translator.getTranslatedDataSetTypes(), service);
    }

    public Status handle(File originalData, DataSetInformation dataSetInformation)
    {
        ExperimentIdentifier experimentIdentifier = dataSetInformation.getExperimentIdentifier();
        if (experimentIdentifier == null)
        {
            throw new UserFailureException(
                    "Data set should be registered for an experiment and not for a sample.");
        }
        DataSetType dataSetType = dataSetInformation.getDataSetType();
        if (dataSetType == null || dataSetType.getCode().equals(DATA_SET_TYPE) == false)
        {
            throw new UserFailureException("Data has to be uploaded for data set type "
                    + DATA_SET_TYPE + " instead of " + dataSetType + ".");
        }
        Set<DataColumnHeader> headers = new HashSet<DataColumnHeader>();
        if (originalData.isFile())
        {
            cleaveFileIntoDataSets(originalData, dataSetInformation, headers);
        } else
        {
            File[] tsvFiles = originalData.listFiles(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        String lowerCaseName = name.toLowerCase();
                        return lowerCaseName.endsWith(".txt") || lowerCaseName.endsWith(".tsv");
                    }
                });
            if (tsvFiles == null || tsvFiles.length == 0)
            {
                throw new UserFailureException("Data set file(s) to be uploaded must be of type "
                        + "'.txt', '.TXT', '.tsv', or '.TSV'.");
            }
            for (File tsvFile : tsvFiles)
            {
                cleaveFileIntoDataSets(tsvFile, dataSetInformation, headers);
            }
        }
        return Status.OK;
    }

    private void cleaveFileIntoDataSets(File tsvFile, DataSetInformation dataSetInformation,
            Set<DataColumnHeader> headers)
    {
        FileReader reader = null;
        try
        {
            reader = new FileReader(tsvFile);
            String fileName = tsvFile.toString();
            TabSeparatedValueTable table =
                    new TabSeparatedValueTable(reader, fileName, ignoreEmptyLines);
            List<Column> columns = table.getColumns();
            List<Column> commonColumns = new ArrayList<Column>();
            List<Column> dataColumns = new ArrayList<Column>();
            for (Column column : columns)
            {
                String header = column.getHeader();
                if (DATA_COLUMN_HEADER_PATTERN.matcher(header).matches())
                {
                    dataColumns.add(column);
                } else
                {
                    commonColumns.add(column);
                }
            }
            assertExperiment(dataSetInformation, dataColumns);
            for (Column dataColumn : dataColumns)
            {
                createDataSet(commonColumns, dataColumn, dataSetInformation, headers);
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
            DataSetInformation dataSetInformation, Set<DataColumnHeader> headers)
    {
        DataColumnHeader dataColumnHeader = new DataColumnHeader(dataColumn.getHeader());
        if (headers.contains(dataColumnHeader))
        {
            throw new UserFailureException("Data column '" + dataColumnHeader + "' appears twice.");
        }
        Experiment experiment = getExperiment(dataColumnHeader, dataSetInformation);
        String sampleCode = createSampleCode(dataColumnHeader).toUpperCase();
        long sampleID =
                createSampleIfNecessary(sampleCode, dataColumnHeader.getTimePoint(), experiment);
        List<ExternalData> dataSets = service.listDataSetsBySampleID(sampleID, true);
        for (ExternalData dataSet : dataSets)
        {
            DataColumnHeader header = new DataColumnHeader(dataColumnHeader, dataSet);
            if (dataColumnHeader.equals(header))
            {
                throw new UserFailureException("For data column '" + dataColumnHeader
                        + "' the data set '" + dataSet.getCode() + "' has already been registered.");
            }
        }
        headers.add(dataColumnHeader);

        String dataSetFolderName =
                sampleCode + timePointDataSetFileSeparator
                        + dataColumnHeader.getTechnicalReplicateCode()
                        + timePointDataSetFileSeparator + dataColumnHeader.getCelLoc()
                        + timePointDataSetFileSeparator
                        + dataColumnHeader.getTimeSeriesDataSetType()
                        + timePointDataSetFileSeparator + dataColumnHeader.getValueType()
                        + timePointDataSetFileSeparator + dataColumnHeader.getScale()
                        + timePointDataSetFileSeparator + dataColumnHeader.getBiID()
                        + timePointDataSetFileSeparator + dataColumnHeader.getControlledGene();
        File dataSetFolder = new File(dropBox, dataSetFolderName);
        boolean success = getFileOperations().mkdirs(dataSetFolder);
        if (success == false)
        {
            HashSet<String> filesInDropBox =
                    new HashSet<String>(Arrays.asList(getFileOperations().list(dropBox)));
            if (filesInDropBox.contains(dataSetFolder.getName()))
            {
                throw new UserFailureException("There exists already a folder '"
                        + dataSetFolder.getAbsolutePath() + "'.");
            } else
            {
                throw new EnvironmentFailureException("Folder '" + dataSetFolder.getAbsolutePath()
                        + "' couldn't be created.");
            }
        }
        addFileForUndo(dataSetFolder);
        String dataSetType = translator.translate(dataColumnHeader.getTimeSeriesDataSetType());
        File dataFile = new File(dataSetFolder, dataSetType + DATA_FILE_TYPE);
        List<Column> columns = new ArrayList<Column>(commonColumns);
        columns.add(dataColumn);
        writeAsTSVFile(dataFile, columns);
        writeDataSetProperties(dataSetFolder, dataColumnHeader, dataSetType, dataSetInformation
                .tryGetUploadingUserEmail());
        File markerFile = new File(dropBox, Constants.IS_FINISHED_PREFIX + dataSetFolderName);
        success = getFileOperations().createNewFile(markerFile);
        if (success == false)
        {
            throw new EnvironmentFailureException("Marker file '" + markerFile.getAbsolutePath()
                    + "' couldn't be created.");
        }
    }

    private void writeDataSetProperties(File dataSetFolder, DataColumnHeader dataColumnHeader,
            String dataSetType, String userEmail)
    {
        File dataSetPropertiesFile = new File(dataSetFolder, dataSetPropertiesFileName);
        DataSetPropertiesBuilder builder =
                new DataSetPropertiesBuilder(dataSetPropertiesValidator, dataSetType);
        try
        {
            if (userEmail != null)
            {
                builder.addProperty(TimePointPropertyType.UPLOADER_EMAIL, userEmail);
            }
            for (TimePointPropertyType type : DataColumnHeader.HEADER_ELEMENTS)
            {
                builder.addProperty(type, dataColumnHeader);
            }
        } catch (IllegalArgumentException ex)
        {
            throw new UserFailureException("Invalid data column header '" + dataColumnHeader
                    + "': " + ex.getMessage());
        }
        writeAsTSVFile(dataSetPropertiesFile, builder.getColumns());
    }

    private void writeAsTSVFile(File tsvFile, List<Column> columns)
    {
        IOutputStream outputStream = getFileOperations().getIOutputStream(tsvFile);
        TSVOutputWriter writer = new TSVOutputWriter(outputStream);
        try
        {
            writer.write(columns);
        } finally
        {
            writer.close();
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
        sampleType.setCode(sampleTypeCode);
        sample.setSampleType(sampleType);
        sample.setExperimentIdentifier(experiment.getIdentifier());
        String groupIdentifier = experiment.getProject().getGroup().getIdentifier();
        sample.setIdentifier(groupIdentifier
                + DatabaseInstanceIdentifier.Constants.IDENTIFIER_SEPARATOR + sampleCode);
        EntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode("TIME_POINT");
        property.setPropertyType(propertyType);
        property.setValue(Integer.toString(timePoint));
        sample.setProperties(new EntityProperty[]
            { property });
        return service.registerSample(sample);
    }

    private Experiment getExperiment(DataColumnHeader dataColumnHeader,
            DataSetInformation dataSetInformation)
    {
        ExperimentIdentifier experimentIdentifier =
                createExperimentIdentifier(dataColumnHeader, dataSetInformation);
        Experiment experiment = service.tryToGetExperiment(experimentIdentifier);
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
        return experimentCodeFormat.format(new Object[]
            { dataColumnHeader.getExperimentCode(), dataColumnHeader.getCultivationMethod(),
                    dataColumnHeader.getBiologicalReplicateCode() });
    }

    private String createSampleCode(DataColumnHeader dataColumnHeader)
    {
        return sampleCodeFormat.format(new Object[]
            { createExperimentCode(dataColumnHeader), dataColumnHeader.getTimePointType(),
                    Integer.toString(dataColumnHeader.getTimePoint()) });
    }

}
