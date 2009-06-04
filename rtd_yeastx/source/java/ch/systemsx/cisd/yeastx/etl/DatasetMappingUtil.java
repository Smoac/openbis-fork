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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.collections.TableMap.UniqueKeyViolationException;
import ch.systemsx.cisd.common.collections.TableMap.UniqueKeyViolationStrategy;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Tomasz Pylak
 */
class DatasetMappingUtil
{
    // extensions of the file which contains dataset mapping. It is assumed that exactly one such a
    // file will exist in the directory.
    private static final String[] MAPPING_FILE_EXTENSIONS = new String[]
        { "tsv" };

    private static TableMap<String, DataSetMappingInformation> tryAsFileMap(
            List<DataSetMappingInformation> list, File logDir)
    {
        IKeyExtractor<String, DataSetMappingInformation> extractor =
                new IKeyExtractor<String, DataSetMappingInformation>()
                    {
                        public String getKey(DataSetMappingInformation dataset)
                        {
                            return dataset.getFileName().toLowerCase();
                        }
                    };
        try
        {
            return new TableMap<String, DataSetMappingInformation>(list, extractor,
                    UniqueKeyViolationStrategy.ERROR);
        } catch (UniqueKeyViolationException e)
        {
            LogUtils.error(logDir,
                    "The file '%s' appears more than once. No datasets will be processed.", e
                            .getInvalidKey());
            return null;
        }
    }

    public static DataSetMappingInformation tryGetDatasetMapping(File datasetFile)
    {
        TableMap<String, DataSetMappingInformation> datasetsMapping =
                tryGetDatasetsMapping(datasetFile.getParentFile());
        if (datasetsMapping == null)
        {
            return null;
        }
        return tryGetDatasetMapping(datasetFile, datasetsMapping);
    }

    public static DataSetMappingInformation tryGetDatasetMapping(File datasetFile,
            TableMap<String, DataSetMappingInformation> datasetsMapping)
    {
        String datasetFileName = datasetFile.getName();
        return datasetsMapping.tryGet(datasetFileName.toLowerCase());
    }

    public static TableMap<String/* file name in lowercase */, DataSetMappingInformation> tryGetDatasetsMapping(
            File parentDir)
    {
        File mappingFile = tryGetMappingFile(parentDir);
        if (mappingFile == null)
        {
            LogUtils.warn(parentDir, "Cannot process the directory '%s' "
                    + "because a file with extension '%s' which contains dataset descriptions "
                    + "does not exist or there is more than one.", parentDir.getPath(),
                    CollectionUtils.abbreviate(MAPPING_FILE_EXTENSIONS, -1));
            return null;
        }
        List<DataSetMappingInformation> list =
                DataSetMappingInformationParser.tryParse(mappingFile);
        if (list == null)
        {
            return null;
        }
        DatasetMappingResolver.adaptPropertyCodes(list);
        return tryAsFileMap(list, parentDir);
    }

    public static boolean isMappingFile(File file)
    {
        String ext = FilenameUtils.getExtension(file.getName());
        for (String mappingExt : MAPPING_FILE_EXTENSIONS)
        {
            if (mappingExt.equalsIgnoreCase(ext))
            {
                return true;
            }
        }
        return false;
    }

    private static File tryGetMappingFile(File parentDir)
    {
        List<File> potentialMappingFiles = listPotentialMappingFiles(parentDir);
        if (potentialMappingFiles.size() != 1)
        {
            return null;
        }
        File indexFile = potentialMappingFiles.get(0);
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

    private static List<File> listPotentialMappingFiles(File dataSet)
    {
        return FileUtilities.listFiles(dataSet, MAPPING_FILE_EXTENSIONS, false, null);
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
