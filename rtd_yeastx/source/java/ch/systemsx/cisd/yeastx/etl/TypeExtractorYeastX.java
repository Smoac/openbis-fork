/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * Implementation of {@link ITypeExtractor} which gets the types from the file extension. Some of
 * the types are hard-coded.
 * 
 * @author Tomasz Pylak
 */
public class TypeExtractorYeastX implements ITypeExtractor
{
    private static final String FILE_TYPE_MAPPING_SEPARATOR = " ";

    @Private
    // File format type which is used when the user has not configured it otherwise
    static final String UNRECOGNIZED_FILE_FORMAT_TYPE = "UNKNOWN";

    @Private
    // Dataset type which is used when the user has not configured it otherwise
    static final String UNRECOGNIZED_DATA_SET_TYPE = "UNKNOWN";

    private static final String LOCATOR_TYPE_CODE = LocatorType.DEFAULT_LOCATOR_TYPE_CODE;

    @Private
    static final String FILE_TYPES_NAME = "file-types";

    private final Map<String/* file extension */, String/* file type */> fileTypeMapping;

    public TypeExtractorYeastX(final Properties properties)
    {
        String typesMapping = PropertyUtils.getProperty(properties, FILE_TYPES_NAME);
        this.fileTypeMapping = createFileTypeMap(typesMapping);
    }

    private static Map<String, String> createFileTypeMap(String typesMapping)
    {
        Map<String, String> result = new HashMap<String, String>();
        if (StringUtils.isBlank(typesMapping))
        {
            return result;
        }
        String[] mappings =
                PropertyParametersUtil.parseItemisedProperty(typesMapping, FILE_TYPES_NAME);
        for (int i = 0; i < mappings.length; i++)
        {
            String mapping = mappings[i];
            String[] tokens = mapping.split(FILE_TYPE_MAPPING_SEPARATOR);
            if (tokens.length != 2 || StringUtils.isBlank(tokens[0])
                    || StringUtils.isBlank(tokens[1]))
            {
                throw ConfigurationFailureException
                        .fromTemplate(
                                "Wrong value of property '%s = %s'. The item '%s' has incorrect format.\n"
                                        + "The value should be a comma separated list of pairs: file-extension file-type.\n"
                                        + "It is assumed that for each file extension a dataset type with the same name is defined in openBIS.\n"
                                        + "The corresponding file types have to be defined in openBIS as well.\n"
                                        + "Files with unspecified extensions will have the file type and dataset type UNKNOWN in openBIS.",
                                FILE_TYPES_NAME, typesMapping, mapping);
            }
            String fileExtension = normalizeExtension(tokens[0]);
            String fileType = tokens[1].toUpperCase();
            result.put(fileExtension, fileType);
        }
        return result;
    }

    public final FileFormatType getFileFormatType(final File incomingDataSetPath)
    {
        String fileExtension = getExtension(incomingDataSetPath);
        String fileType = getFileTypeCode(fileExtension);
        return new FileFormatType(fileType);
    }

    private String getFileTypeCode(String fileExtension)
    {
        String fileType = fileTypeMapping.get(fileExtension);
        if (fileType == null)
        {
            return UNRECOGNIZED_DATA_SET_TYPE;
        } else
        {
            return fileType;
        }
    }

    public final LocatorType getLocatorType(final File incomingDataSetPath)
    {
        return new LocatorType(LOCATOR_TYPE_CODE);
    }

    public final DataSetType getDataSetType(final File incomingDataSetPath)
    {
        String fileExtension = getExtension(incomingDataSetPath);
        String datasetType;
        if (fileTypeMapping.get(fileExtension) == null)
        {
            datasetType = UNRECOGNIZED_FILE_FORMAT_TYPE;
        } else
        {
            datasetType = fileExtension;
        }
        return new DataSetType(datasetType);
    }

    public String getProcessorType(File incomingDataSetPath)
    {
        return null;
    }

    public boolean isMeasuredData(File incomingDataSetPath)
    {
        return getExtension(incomingDataSetPath).equalsIgnoreCase(ConstantsYeastX.MZXML_EXT);
    }

    private static String getExtension(final File incomingDataSetPath)
    {
        String extension = FilenameUtils.getExtension(incomingDataSetPath.getName());
        return normalizeExtension(extension);
    }

    private static String normalizeExtension(String extension)
    {
        return extension.toUpperCase();
    }
}
