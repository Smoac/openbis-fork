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
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class Cleaver extends AbstractPostRegistrationDataSetHandlerForFileBasedUndo
{
    private static final Pattern DATA_COLUMN_HEADER_PATTERN = Pattern.compile(".*(::.*)+");

    static final String EXPERIMENT_CODE_TEMPLATE_KEY = "experiment-code-template";
    static final String DEFAULT_EXPERIMENT_CODE_TEMPLATE = "{0}_{1}_{2}";
    
    static final String SAMPLE_CODE_TEMPLATE_KEY = "sample-code-template";
    static final String DEFAULT_SAMPLE_CODE_TEMPLATE = "{0}_{1}";
    
    static final String SAMPLE_TYPE_CODE_KEY = "sample-type-code";
    static final String DEFAULT_SAMPLE_TYPE_CODE = "TIME_SERIES";
    
    static final String TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY = "time-point-data-set-drop-box-path";
    
    static final String DATA_SET_PROPERTIES_FILE_NAME_KEY = "data-set-properties-file-name";

    static final String TRANSLATION_KEY = "translation.";
    
    private static final class DataColumnHeader
    {
        private static final int HEADER_PARTS = 12;
        
        private final String experimentCode;
        private final String cultivationMethod;
        private final String biologicalReplicateCode;
        private final int timePoint;
        private final String timePointType;
        private final String technicalReplicateCode;
        private final String celLoc;
        private final String dataSetType;
        private final String valueType;
        private final String scale;
        private final String biID;
        private final String controlledGene;

        DataColumnHeader(String header)
        {
            String[] parts = header.split("::");
            if (parts.length < HEADER_PARTS)
            {
                throw new IllegalArgumentException(HEADER_PARTS
                        + " parts of the following header separated by '::' expected: " + header);
            }
            experimentCode = parts[0];
            cultivationMethod = parts[1];
            biologicalReplicateCode = parts[2];
            timePoint = parseTimePoint(parts[3], header);
            timePointType = parts[4];
            technicalReplicateCode = parts[5];
            celLoc = parts[6];
            dataSetType = parts[7];
            valueType = parts[8];
            scale = parts[9];
            biID = parts[10];
            controlledGene = parts[11];
        }

        private int parseTimePoint(String value, String header)
        {
            try
            {
                return Integer.parseInt(value.startsWith("+") ? value.substring(1) : value);
            } catch (NumberFormatException ex)
            {
                throw new UserFailureException(
                        "4. part [" + value + "] of the following header isn't an integer number: " + header);
            }
        }
        
        String createExperimentCode(MessageFormat format)
        {
            return format.format(new Object[]
                { experimentCode, cultivationMethod, biologicalReplicateCode });
        }
        
        String createSampleCode(MessageFormat format)
        {
            return format.format(new Object[] {timePointType, Integer.toString(timePoint)} );
        }
    }
    
    private final MessageFormat experimentCodeFormat;
    private final MessageFormat sampleCodeFormat;
    private final IEncapsulatedOpenBISService service;
    private final String sampleTypeCode;
    private final File dropBox;
    private final String dataSetPropertiesFileName;
    private final DataSetTypeTranslator translation;

    Cleaver(Properties properties, IEncapsulatedOpenBISService service)
    {
        super(FileOperations.getInstance());
        this.service = service;
        sampleTypeCode = properties.getProperty(SAMPLE_TYPE_CODE_KEY, DEFAULT_SAMPLE_TYPE_CODE);
        experimentCodeFormat =
                new MessageFormat(properties.getProperty(EXPERIMENT_CODE_TEMPLATE_KEY,
                        DEFAULT_EXPERIMENT_CODE_TEMPLATE));
        sampleCodeFormat =
                new MessageFormat(properties.getProperty(SAMPLE_CODE_TEMPLATE_KEY,
                        DEFAULT_SAMPLE_CODE_TEMPLATE));
        String dropBoxPath = PropertyUtils.getMandatoryProperty(properties, TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY);
        dropBox = new File(dropBoxPath);
        if (dropBox.isDirectory() == false)
        {
            throw new ConfigurationFailureException(
                    "Drop box for time-point data sets does not exists or isn't a folder: "
                            + dropBox.getAbsolutePath());
        }
        dataSetPropertiesFileName = PropertyUtils.getMandatoryProperty(properties, DATA_SET_PROPERTIES_FILE_NAME_KEY);
        translation = new DataSetTypeTranslator(ExtendedProperties.getSubset(properties, TRANSLATION_KEY, true));
    }

    public void handle(File originalData, DataSetInformation dataSetInformation)
    {
        if (originalData.isFile())
        {
            cleaveFileIntoDataSets(originalData, dataSetInformation);
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
            for (File tsvFile : tsvFiles)
            {
                cleaveFileIntoDataSets(tsvFile, dataSetInformation);
            }
        }
    }

    private void cleaveFileIntoDataSets(File tsvFile, DataSetInformation dataSetInformation)
    {
        FileReader reader = null;
        try
        {
            reader = new FileReader(tsvFile);
            String fileName = tsvFile.toString();
            TabSeparatedValueTable table = new TabSeparatedValueTable(reader, fileName);
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
            for (Column dataColumn : dataColumns)
            {
                createDataSet(commonColumns, dataColumn, dataSetInformation);
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

    private void createDataSet(List<Column> commonColumns, Column dataColumn,
            DataSetInformation dataSetInformation)
    {
        DataColumnHeader dataColumnHeader = new DataColumnHeader(dataColumn.getHeader());
        Experiment experiment = getExperiment(dataColumnHeader, dataSetInformation);
        String sampleCode = dataColumnHeader.createSampleCode(sampleCodeFormat).toUpperCase();
        createSampleIfNecessary(sampleCode, experiment);
        
        File dataSetFolder = new File(dropBox, sampleCode);
        boolean success = getFileOperations().mkdirs(dataSetFolder);
        if (success == false)
        {
            throw new EnvironmentFailureException("Folder '" + dataSetFolder.getAbsolutePath()
                    + "' couldn't be created.");
        }
        addFileForUndo(dataSetFolder);
        String dataFileName = translation.translate(dataColumnHeader.dataSetType) + ".data.txt";
        File dataFile = new File(dataSetFolder, dataFileName);
        List<Column> columns = new ArrayList<Column>(commonColumns);
        columns.add(dataColumn);
        writeAsTSVFile(dataFile, columns);
        writeDataSetProperties(dataSetFolder, dataColumnHeader);
        File markerFile = new File(dropBox, Constants.IS_FINISHED_PREFIX + sampleCode);
        success = getFileOperations().createNewFile(markerFile);
        if (success == false)
        {
            throw new EnvironmentFailureException("Marker file '" + markerFile.getAbsolutePath()
                    + "' couldn't be created.");
        }
    }

    private void writeDataSetProperties(File dataSetFolder, DataColumnHeader dataColumnHeader)
    {
        File dataSetPropertiesFile = new File(dataSetFolder, dataSetPropertiesFileName);
        TableBuilder builder = new TableBuilder("property", "value");
        builder.addRow("TECHNICAL_REPLICATE_CODE", dataColumnHeader.technicalReplicateCode);
        builder.addRow("CEL_LOC", dataColumnHeader.celLoc);
        builder.addRow("VALUE_TYPE", dataColumnHeader.valueType);
        builder.addRow("SCALE", dataColumnHeader.scale);
        builder.addRow("BI_ID", dataColumnHeader.biID);
        builder.addRow("CG", dataColumnHeader.controlledGene);
        writeAsTSVFile(dataSetPropertiesFile, builder.getColumns());
    }
    
    private void writeAsTSVFile(File tsvFile, List<Column> columns)
    {
        IOutputStream outputStream = getFileOperations().getIOutputStream(tsvFile);
        try
        {
            Printer printer = new Printer(outputStream);
            List<List<String>> cols = new ArrayList<List<String>>();
            int numberOfRows = Integer.MAX_VALUE;
            String delim = "";
            for (Column column : columns)
            {
                printer.print(delim + column.getHeader());
                delim = "\t";
                List<String> values = column.getValues();
                numberOfRows = Math.min(numberOfRows, values.size());
                cols.add(values);
            }
            printer.println("");
            for (int i = 0; i < numberOfRows; i++)
            {
                delim = "";
                for (List<String> col : cols)
                {
                    printer.print(delim + col.get(i));
                    delim = "\t";
                }
                printer.println("");
            }
            outputStream.flush();
        } finally
        {
            outputStream.close();
        }
    }

    private static final class Printer
    {
        private final IOutputStream outputStream;

        public Printer(IOutputStream outputStream)
        {
            this.outputStream = outputStream;
        }
        
        public void println(Object object)
        {
            print(object + OSUtilities.LINE_SEPARATOR);
        }
        
        public void print(Object object)
        {
            outputStream.write(String.valueOf(object).getBytes());
        }
    }

    private void createSampleIfNecessary(String sampleCode, Experiment experiment)
    {
        ListSampleCriteria criteria = ListSampleCriteria.createForExperiment(new TechId(experiment.getId()));
        List<Sample> samples = service.listSamples(criteria);
        System.out.println(samples);
        for (Sample sample : samples)
        {
            if (sample.getCode().equals(sampleCode))
            {
                return;
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
        service.registerSample(sample);
    }

    private Experiment getExperiment(DataColumnHeader dataColumnHeader,
            DataSetInformation dataSetInformation)
    {
        ExperimentIdentifier experimentIdentifier = createExperimentIdentifier(dataColumnHeader, dataSetInformation);
        Experiment experiment = service.tryToGetExperiment(experimentIdentifier);
        if (experiment == null)
        {
            throw new UserFailureException("No experiment found for experiment identifier " + experimentIdentifier);
        }
        return experiment;
    }

    private ExperimentIdentifier createExperimentIdentifier(DataColumnHeader dataColumnHeader,
            DataSetInformation dataSetInformation)
    {
        String experimentCode = dataColumnHeader.createExperimentCode(experimentCodeFormat);
        ExperimentIdentifier experimentIdentifier = dataSetInformation.getExperimentIdentifier();
        if (experimentIdentifier == null)
        {
            throw new UserFailureException(
                    "Data set should be registered for an experiment and not for a sample.");
        }
        return new ExperimentIdentifier(experimentIdentifier, experimentCode);
    }


}
