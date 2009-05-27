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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;

/**
 * @author Tomasz Pylak
 */
public class DatasetMappingUtil
{
    private static final String MAPPING_FILE_NAME = "index.tsv";

    private static TableMap<String, DataSetMappingInformation> asFileMap(
            List<DataSetMappingInformation> list)
    {
        return new TableMap<String, DataSetMappingInformation>(list,
                new IKeyExtractor<String, DataSetMappingInformation>()
                    {
                        public String getKey(DataSetMappingInformation dataset)
                        {
                            return dataset.getFileName().toLowerCase();
                        }
                    });
    }

    public static DataSetMappingInformation tryGetPlainDatasetInfo(File incomingDataSetPath)
    {
        TableMap<String, DataSetMappingInformation> datasetsMapping =
                tryGetDatasetsMapping(incomingDataSetPath.getParentFile());
        if (datasetsMapping == null)
        {
            return null;
        }
        return tryGetPlainDatasetInfo(incomingDataSetPath, datasetsMapping);
    }

    public static boolean hasMapping(File incomingDataSetPath,
            TableMap<String, DataSetMappingInformation> datasetsMapping)
    {
        return tryGetPlainDatasetInfo(incomingDataSetPath, datasetsMapping) != null;
    }

    private static DataSetMappingInformation tryGetPlainDatasetInfo(File incomingDataSetPath,
            TableMap<String, DataSetMappingInformation> datasetsMapping)
    {
        String datasetFileName = incomingDataSetPath.getName();
        return datasetsMapping.tryGet(datasetFileName.toLowerCase());
    }

    public static TableMap<String/* file name in lowercase */, DataSetMappingInformation> tryGetDatasetsMapping(
            File parentDir)
    {
        File mappingFile = tryGetMappingFile(parentDir);
        if (mappingFile == null)
        {
            LogUtils.warn(parentDir, "Cannot process the directory '%s' "
                    + "because the file '%s' with datasets descriptions does not exist.", parentDir
                    .getPath(), MAPPING_FILE_NAME);
            return null;
        }
        List<DataSetMappingInformation> list =
                DataSetMappingInformationParser.tryParse(mappingFile);
        if (list == null)
        {
            return null;
        }
        return asFileMap(list);
    }

    public static boolean isMappingFile(File incomingDataSetPath)
    {
        return incomingDataSetPath.getName().equals(MAPPING_FILE_NAME);
    }

    private static File tryGetMappingFile(File parentDir)
    {
        File indexFile = new File(parentDir, MAPPING_FILE_NAME);
        if (indexFile.isFile() == false)
        {
            return null;
        } else if (indexFile.canWrite() == false)
        {
            return null;
        } else
        {
            return indexFile;
        }
    }

    public static void deleteMappingFile(File parentDir)
    {
        File mappingFile = tryGetMappingFile(parentDir);
        if (mappingFile != null && mappingFile.isFile())
        {
            mappingFile.delete();
        }
    }

    /**
     * Removes from the specified file these lines which describe the already processed files.
     * 
     * @param processedFiles files which should be removed from the mapping file
     */
    public static void cleanMappingFile(File parentDir, Set<String> processedFiles)
            throws IOException
    {
        File mappingFile = tryGetMappingFile(parentDir);
        if (mappingFile == null)
        {
            return;
        }
        List<String> lines = readLines(mappingFile);
        List<String> unprocessedLines = new ArrayList<String>();
        for (String line : lines)
        {
            String tokens[] = line.trim().split("\t");
            if (tokens.length == 0 || processedFiles.contains(tokens[0].toLowerCase()) == false)
            {
                unprocessedLines.add(line);
            }
        }
        IOUtils.writeLines(unprocessedLines, "\n", new FileOutputStream(mappingFile));
    }

    @SuppressWarnings("unchecked")
    private static List<String> readLines(File mappingFile) throws IOException,
            FileNotFoundException
    {
        return IOUtils.readLines(new FileInputStream(mappingFile));
    }
}
