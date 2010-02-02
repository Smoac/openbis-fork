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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.IOutputStream;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.utils.Column;
import ch.systemsx.cisd.etlserver.utils.TableBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class TimePointDataDropBoxFeeder implements IDropBoxFeeder
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

    static final String DATA_FILE_TYPE = ".data.txt";

    static final String TRANSLATION_KEY = "translation.";

    static final String DATA_SET_PROPERTIES_FILE_NAME_KEY = "data-set-properties-file-name";

    static final String DEFAULT_TIME_POINT_DATA_SET_FILE_NAME_SEPARATOR = ".";

    static final String TIME_POINT_DATA_SET_FILE_NAME_SEPARATOR_KEY =
            "time-point-data-set-file-name-separator";

    static final String TIME_POINT_DATA_SET_DROP_BOX_PATH_KEY = "time-point-data-set-drop-box-path";

    private final String timePointDataSetFileSeparator;
    private final File dropBox;
    private final DataSetPropertiesValidator dataSetPropertiesValidator;
    private final String dataSetPropertiesFileName;
    private final DataSetTypeTranslator translator;
    private final IFileManager fileManager;

    TimePointDataDropBoxFeeder(Properties properties, IFileManager fileManager,
            IEncapsulatedOpenBISService service)
    {
        this.fileManager = fileManager;
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
    
    public void feed(String userEmail, String sampleCode, List<Column> commonColumns,
            Column dataColumn)
    {
        DataColumnHeader dataColumnHeader = new DataColumnHeader(dataColumn.getHeader());
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
        boolean success = fileManager.getFileOperations().mkdirs(dataSetFolder);
        if (success == false)
        {
            HashSet<String> filesInDropBox =
                new HashSet<String>(Arrays.asList(fileManager.getFileOperations().list(dropBox)));
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
        fileManager.addFileForUndo(dataSetFolder);
        String dataSetType = translator.translate(dataColumnHeader.getTimeSeriesDataSetType());
        File dataFile = new File(dataSetFolder, dataSetType + DATA_FILE_TYPE);
        List<Column> columns = new ArrayList<Column>(commonColumns);
        columns.add(dataColumn);
        writeAsTSVFile(dataFile, columns);
        writeDataSetProperties(dataSetFolder, dataColumnHeader, dataSetType, userEmail);
        File markerFile = new File(dropBox, Constants.IS_FINISHED_PREFIX + dataSetFolderName);
        success = fileManager.getFileOperations().createNewFile(markerFile);
        if (success == false)
        {
            throw new EnvironmentFailureException("Marker file '" + markerFile.getAbsolutePath()
                    + "' couldn't be created.");
        }
    }
    private void writeDataSetProperties(File dataSetFolder, DataColumnHeader dataColumnHeader,
            String dataSetType, String userEmailOrNull)
    {
        File dataSetPropertiesFile = new File(dataSetFolder, dataSetPropertiesFileName);
        DataSetPropertiesBuilder builder =
                new DataSetPropertiesBuilder(dataSetPropertiesValidator, dataSetType);
        try
        {
            if (userEmailOrNull != null)
            {
                builder.addProperty(TimePointPropertyType.UPLOADER_EMAIL, userEmailOrNull);
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
        IOutputStream outputStream = fileManager.getFileOperations().getIOutputStream(tsvFile);
        TSVOutputWriter writer = new TSVOutputWriter(outputStream);
        try
        {
            writer.write(columns);
        } finally
        {
            writer.close();
        }
    }

}
