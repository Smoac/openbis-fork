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
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.collections.TableMap.UniqueKeyViolationException;
import ch.systemsx.cisd.common.collections.TableMap.UniqueKeyViolationStrategy;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.parser.TabFileLoader;

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
            List<DataSetMappingInformation> list, LogUtils log, File mappingFile)
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
            log.mappingFileError(mappingFile, "the file '%s' appears more than once.", e
                    .getInvalidKey());
            return null;
        }
    }

    public static DataSetMappingInformation tryGetDatasetMapping(File datasetFile, LogUtils log)
    {
        DataSetMappingInformationFile datasetsMappings =
                tryGetDatasetsMapping(datasetFile.getParentFile(), log);
        if (datasetsMappings == null || datasetsMappings.tryGetMappings() == null)
        {
            return null;
        }
        return tryGetDatasetMapping(datasetFile, datasetsMappings.tryGetMappings());
    }

    public static DataSetMappingInformation tryGetDatasetMapping(File datasetFile,
            TableMap<String, DataSetMappingInformation> datasetsMapping)
    {
        String datasetFileName = datasetFile.getName();
        return datasetsMapping.tryGet(datasetFileName.toLowerCase());
    }

    // returns the content of the first line comment or null if there is no comment or it is empty
    private static String tryGetFirstLineCommentContent(File mappingFile, LogUtils log)
    {
        List<String> lines;
        try
        {
            lines = readLines(mappingFile);
        } catch (IOException e)
        {
            Object[] arguments = {};
            log.mappingFileError(mappingFile, e.getMessage(), arguments);
            return null;
        }
        if (lines.size() == 0)
        {
            return null;
        }
        String firstLine = lines.get(0);
        if (StringUtils.isBlank(firstLine))
        {
            return null;
        }
        firstLine = firstLine.trim();
        if (firstLine.startsWith(TabFileLoader.COMMENT_PREFIX) == false)
        {
            return null;
        }
        firstLine = firstLine.substring(1).trim();
        return firstLine;
    }

    /**
     * @return email address from the first line of the mapping file or null if there is no emai or
     *         it is invalid.
     */
    private static String tryGetEmail(File mappingFile, LogUtils log)
    {
        String email = tryGetFirstLineCommentContent(mappingFile, log);
        if (email == null)
        {
            log.mappingFileError(mappingFile,
                    "There should be a '%s' character followed by an email address "
                            + "in the first line of the file. "
                            + "The email is needed to send messages about errors.",
                    TabFileLoader.COMMENT_PREFIX);
            return null;
        }
        if (email.contains("@") == false || email.contains(".") == false)
        {
            log.mappingFileError(mappingFile,
                    "The text '%s' does not seem to be an email address.", email);
            return null;
        }
        return email;
    }

    static class DataSetMappingInformationFile
    {
        private final TableMap<String/* file name in lowercase */, DataSetMappingInformation> mappingsOrNull;

        private final String notificationEmail;

        public DataSetMappingInformationFile(
                TableMap<String, DataSetMappingInformation> mappingsOrNull, String notificationEmail)
        {
            this.mappingsOrNull = mappingsOrNull;
            this.notificationEmail = notificationEmail;
        }

        public TableMap<String, DataSetMappingInformation> tryGetMappings()
        {
            return mappingsOrNull;
        }

        public String getNotificationEmail()
        {
            return notificationEmail;
        }
    }

    public static DataSetMappingInformationFile tryGetDatasetsMapping(File batchDir, LogUtils log)
    {
        File mappingFile = tryGetMappingFile(batchDir, log);
        if (mappingFile == null)
        {
            return null;
        }
        String notificationEmail = tryGetEmail(mappingFile, log);
        if (notificationEmail == null)
        {
            return null; // email has to be provided always
        }
        TableMap<String, DataSetMappingInformation> mappingsOrNull = null;
        List<DataSetMappingInformation> list =
                DataSetMappingInformationParser.tryParse(mappingFile, log);
        if (list != null)
        {
            mappingsOrNull = tryAsFileMap(list, log, mappingFile);
        }
        return new DataSetMappingInformationFile(mappingsOrNull, notificationEmail);
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

    /** @return false if no potential mapping files exist in the specified directory */
    public static boolean isMappingFilePresent(File batchDir)
    {
        List<File> potentialMappingFiles = listPotentialMappingFiles(batchDir);
        return potentialMappingFiles.size() > 0;
    }

    private static File tryGetMappingFile(File batchDir, LogUtils log)
    {
        List<File> potentialMappingFiles = listPotentialMappingFiles(batchDir);
        String errorMsgPrefix = "No datasets from the directory '%s' can be processed because ";
        String batchDirName = batchDir.getName();
        if (potentialMappingFiles.size() == 0)
        {
            log.error(errorMsgPrefix
                    + "there is no file with extension '%s' which contains dataset descriptions.",
                    batchDirName, CollectionUtils.abbreviate(MAPPING_FILE_EXTENSIONS, -1));
            return null;
        }

        if (potentialMappingFiles.size() > 1)
        {
            log.error(errorMsgPrefix + "there is more than one file with extension '%s'.",
                    batchDirName, CollectionUtils.abbreviate(MAPPING_FILE_EXTENSIONS, -1));
            return null;
        }
        File indexFile = potentialMappingFiles.get(0);
        if (indexFile.isFile() == false)
        {
            log.error(errorMsgPrefix + "'%s' is not a file.", batchDirName, indexFile.getName());
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

    public static boolean deleteMappingFile(File batchDir, LogUtils log)
    {
        File mappingFile = tryGetMappingFile(batchDir, log);
        if (mappingFile != null && mappingFile.isFile())
        {
            return mappingFile.delete();
        }
        return true;
    }

    /**
     * Removes from the specified file these lines which describe the already processed files.
     * 
     * @param processedFiles files which should be removed from the mapping file
     */
    public static void cleanMappingFile(File batchDir, Set<String> processedFiles, LogUtils log)
    {
        File mappingFile = tryGetMappingFile(batchDir, log);
        if (mappingFile == null)
        {
            return;
        }
        try
        {
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
            writeLines(mappingFile, unprocessedLines);
        } catch (IOException ex)
        {
            log.mappingFileError(mappingFile, "cannot clean dataset mappings file: "
                    + ex.getMessage());
        }
    }

    private static void writeLines(File file, List<String> lines) throws IOException,
            FileNotFoundException
    {
        FileOutputStream stream = new FileOutputStream(file);
        IOUtils.writeLines(lines, "\n", stream);
        IOUtils.closeQuietly(stream);
    }

    @SuppressWarnings("unchecked")
    private static List<String> readLines(File mappingFile) throws IOException,
            FileNotFoundException
    {
        FileInputStream stream = new FileInputStream(mappingFile);
        List lines = IOUtils.readLines(stream);
        IOUtils.closeQuietly(stream);
        return lines;
    }
}
