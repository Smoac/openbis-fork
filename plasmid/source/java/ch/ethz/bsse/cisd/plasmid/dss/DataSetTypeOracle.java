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

package ch.ethz.bsse.cisd.plasmid.dss;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.FileTypeExtractor;

/**
 * This oracle knows the extensions of the different types of data files provided by CSB.
 * 
 * @author Piotr Buczek
 */
class DataSetTypeOracle
{
    public static final String DATASET_TYPES_NAME = "dataset-types";

    /**
     * The different kinds of data set types known to the oracle.
     * 
     * @author Piotr Buczek
     */
    static enum DataSetTypeInfo
    {
        // only SEQ_FILE files are derived, other files are measured
        SEQ_FILE(false), RAW_DATA(true), VERIFICATION(true), UNKNOWN(true);

        private final boolean measured;

        DataSetTypeInfo(boolean measured)
        {
            this.measured = measured;
        }

        public String getDataSetTypeCode()
        {
            return name();
        }

        public boolean isMeasured()
        {
            return measured;
        }
    }

    private static DataSetTypeInfo getType(final String typeName)
    {
        try
        {
            return DataSetTypeInfo.valueOf(typeName);
        } catch (IllegalArgumentException e)
        {
            throw ConfigurationFailureException.fromTemplate(
                    "Wrong dataset type '%s'. Expected one of: '%s'.", typeName, Arrays
                            .toString(DataSetTypeInfo.values()));
        }
    }

    private static Map<String, DataSetTypeInfo> typeInfoByExtension =
            new HashMap<String, DataSetTypeInfo>();

    static void initializeMapping(Properties properties)
    {
        Map<String, String> map =
                FileTypeExtractor.createTypeByFileExtensionMap(properties, DATASET_TYPES_NAME);
        for (Entry<String, String> entry : map.entrySet())
        {
            typeInfoByExtension.put(entry.getKey(), getType(entry.getValue()));
        }
    }

    /**
     * Extracts {@link DataSetTypeInfo} from the name of the dataset file.
     * 
     * @throws UserFailureException if <var>incomingDataSetPath</var> is a path to a directory
     */
    static DataSetTypeInfo extractDataSetTypeInfo(File incomingDataSetPath)
            throws UserFailureException
    {
        if (incomingDataSetPath.isDirectory())
        {
            return DataSetTypeInfo.UNKNOWN;
        }

        final String fileName = incomingDataSetPath.getName().toLowerCase();
        final String fileExtension = FilenameUtils.getExtension(fileName);

        DataSetTypeInfo result = tryGetMappedType(fileExtension);
        return result == null ? DataSetTypeInfo.VERIFICATION : result;
    }

    private static DataSetTypeInfo tryGetMappedType(String fileExtension)
    {
        return typeInfoByExtension.get(normalizeExtension(fileExtension));
    }

    private static String normalizeExtension(String extension)
    {
        return extension.toUpperCase();
    }

}
