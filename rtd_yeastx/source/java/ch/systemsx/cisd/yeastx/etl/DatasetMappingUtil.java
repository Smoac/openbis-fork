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

package ch.systemsx.cisd.yeastx.etl;

import java.io.File;
import java.util.List;

import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author Tomasz Pylak
 */
public class DatasetMappingUtil
{
    private static final String MAPPING_FILE_NAME = "index.tsv";

    private static TableMap<String, PlainDataSetInformation> asFileMap(
            List<PlainDataSetInformation> load)
    {
        return new TableMap<String, PlainDataSetInformation>(
                new IKeyExtractor<String, PlainDataSetInformation>()
                    {

                        public String getKey(PlainDataSetInformation dataset)
                        {
                            return dataset.getFileName().toLowerCase();
                        }
                    });
    }

    public static PlainDataSetInformation tryGetPlainDatasetInfo(File incomingDataSetPath)
    {
        TableMap<String, PlainDataSetInformation> datasetsMapping =
                getDatasetsMapping(incomingDataSetPath.getParentFile());
        return tryGetPlainDatasetInfo(incomingDataSetPath, datasetsMapping);
    }

    public static boolean hasMapping(File incomingDataSetPath,
            TableMap<String, PlainDataSetInformation> datasetsMapping)
    {
        return tryGetPlainDatasetInfo(incomingDataSetPath, datasetsMapping) != null;
    }

    private static PlainDataSetInformation tryGetPlainDatasetInfo(File incomingDataSetPath,
            TableMap<String, PlainDataSetInformation> datasetsMapping)
    {
        String datasetFileName = incomingDataSetPath.getName();
        return datasetsMapping.tryGet(datasetFileName.toLowerCase());
    }

    public static TableMap<String/* file name in lowercase */, PlainDataSetInformation> getDatasetsMapping(
            File parentDir)
    {
        File mappingFile = getMappingFile(parentDir);
        List<PlainDataSetInformation> list = DataSetInformationParser.parse(mappingFile);
        return asFileMap(list);
    }

    public static boolean isMappingFile(File incomingDataSetPath)
    {
        return incomingDataSetPath.getName().equals(MAPPING_FILE_NAME);
    }

    private static File getMappingFile(File parentDir)
    {
        File indexFile = new File(parentDir, MAPPING_FILE_NAME);
        if (indexFile.isFile() == false)
        {
            throw UserFailureException.fromTemplate(
                    "The file '%s' with datasets descriptions does not exist.", indexFile
                            .getAbsolutePath());
        }
        return indexFile;
    }
}
