/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.etlserver.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.fasta.SequenceType;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;

/**
 * This maintenance task creates a BLAST database for all files with defined file types of data set with 
 * matching data set types. 
 *
 * @author Franz-Josef Elmer
 */
public class BlastDatabaseCreationMaintenanceTask implements IMaintenanceTask
{
    static final String DATASET_TYPES_PROPERTY = "dataset-types";
    static final String BLAST_TOOLS_DIRECTORY_PROPERTY = "blast-tools-directory";
    static final String BLAST_DATABASES_FOLDER_PROPERTY = "blast-databases-folder";
    static final String BLAST_TEMP_FOLDER_PROPERTY = "blast-temp-folder";
    static final String LAST_SEEN_DATA_SET_FILE_PROPERTY = "last-seen-data-set-file";
    static final String FILE_TYPES_PROPERTY = "file-types";
    
    private static final String DEFAULT_LAST_SEEN_DATA_SET_FILE = "last-seen-data-set-for-BLAST-database-creation";
    private static final String DEFAULT_FILE_TYPES = ".fasta .fa .fsa .fastq";
    private static final String DEFAULT_BLAST_DATABASES_FOLDER = "blast-databases";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, BlastDatabaseCreationMaintenanceTask.class);
    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, BlastDatabaseCreationMaintenanceTask.class);
    
    private File lastSeenDataSetFile;

    private List<Pattern> dataSetTypePatterns;
    private List<String> fileTypes;
    private File blastDatabasesFolder;
    private File tmpFolder;
    private String makeblastdb;
    private String makembindex;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        List<String> dataSetTypeRegexs = PropertyUtils.getMandatoryList(properties, DATASET_TYPES_PROPERTY);
        dataSetTypePatterns = new ArrayList<Pattern>();
        for (String regex : dataSetTypeRegexs)
        {
            try
            {
                dataSetTypePatterns.add(Pattern.compile(regex));
            } catch (PatternSyntaxException ex)
            {
                throw new ConfigurationFailureException("Property '" + DATASET_TYPES_PROPERTY 
                        + "' has invalid regular expression '" + regex + "': " + ex.getMessage());
            }
        }
        fileTypes = Arrays.asList(properties.getProperty(FILE_TYPES_PROPERTY, DEFAULT_FILE_TYPES).split(" +"));
        operationLog.info("File types: " + fileTypes);
        lastSeenDataSetFile = getFile(properties, LAST_SEEN_DATA_SET_FILE_PROPERTY, DEFAULT_LAST_SEEN_DATA_SET_FILE);
        setUpBlastDatabasesFolder(properties);
        setUpBlastTempFolder(properties);
        String blastToolDirectory = getBLASTToolDirectory(properties);
        makeblastdb = blastToolDirectory + "makeblastdb";
        if (process(makeblastdb, "-version") == false)
        {
            operationLog.error("BLAST isn't installed or property '" + BLAST_TOOLS_DIRECTORY_PROPERTY 
                    + "' hasn't been correctly specified.");
        }
        makembindex = blastToolDirectory + "makembindex";
        
    }

    private void setUpBlastDatabasesFolder(Properties properties)
    {
        blastDatabasesFolder = getFile(properties, BLAST_DATABASES_FOLDER_PROPERTY, DEFAULT_BLAST_DATABASES_FOLDER);
        operationLog.info("BLAST databases folder: " + blastDatabasesFolder);
        if (blastDatabasesFolder.exists())
        {
            if (blastDatabasesFolder.isFile())
            {
                throw new ConfigurationFailureException("BLAST databases folder '" + blastDatabasesFolder 
                        + "' is an existing file.");
            }
        } else
        {
            if (blastDatabasesFolder.mkdirs() == false)
            {
                throw new ConfigurationFailureException("Couldn't create BLAST databases folder '" 
                        + blastDatabasesFolder + "'.");
            }
        }
    }
    
    private void setUpBlastTempFolder(Properties properties)
    {
        String tempFolderProperty = properties.getProperty(BLAST_TEMP_FOLDER_PROPERTY);
        if (tempFolderProperty == null)
        {
            tmpFolder = new File(blastDatabasesFolder, "tmp");
        } else
        {
            tmpFolder = new File(tempFolderProperty);
        }
        if (tmpFolder.exists())
        {
            boolean success = FileUtilities.deleteRecursively(tmpFolder);
            if (success == false)
            {
                operationLog.warn("Couldn't delete temp folder '" + tmpFolder + "'.");
            }
        }
        if (tmpFolder.mkdirs() == false)
        {
            throw new ConfigurationFailureException("Couldn't create temp folder '" + tmpFolder + "'.");
        }
        operationLog.info("Temp folder '" + tmpFolder + "' created.");
    }

    private File getFile(Properties properties, String pathProperty, String defaultPath)
    {
        String path = properties.getProperty(pathProperty);
        return path == null ? new File(getConfigProvider().getStoreRoot(), defaultPath) : new File(path);
    }

    private String getBLASTToolDirectory(Properties properties)
    {
        String blastToolsDirectory = properties.getProperty(BLAST_TOOLS_DIRECTORY_PROPERTY, "");
        if (blastToolsDirectory.endsWith("/") || blastToolsDirectory.isEmpty())
        {
            return blastToolsDirectory;
        }
        return blastToolsDirectory + "/";
    }

    @Override
    public void execute()
    {
        IHierarchicalContentProvider contentProvider = getContentProvider();
        IEncapsulatedOpenBISService service = getOpenBISService();
        List<AbstractExternalData> dataSets = getDataSets(service);
        if (dataSets.isEmpty() == false)
        {
            operationLog.info("Scan " + dataSets.size() + " data sets for creating BLAST databases.");
        }
        for (AbstractExternalData dataSet : dataSets)
        {
            if (dataSet.tryGetAsDataSet() != null && dataSet.isAvailable() && dataSetTypeMatches(dataSet))
            {
                try
                {
                    createBlastDatabase(dataSet, contentProvider);
                } catch (Exception ex)
                {
                    operationLog.error("Error caused by creating BLAST database for data set " + dataSet.getCode() 
                            + ": " + ex.getMessage(), ex);
                }
            }
            updateLastSeenEventId(dataSet.getId());
        }
    }
    
    private boolean dataSetTypeMatches(AbstractExternalData dataSet)
    {
        String dataSetType = dataSet.getDataSetType().getCode();
        for (Pattern pattern : dataSetTypePatterns)
        {
            if (pattern.matcher(dataSetType).matches())
            {
                return true;
            }
        }
        return false;
    }

    private void createBlastDatabase(AbstractExternalData dataSet, IHierarchicalContentProvider contentProvider)
    {
        String dataSetCode = dataSet.getCode();
        FastaFileBuilder builder = new FastaFileBuilder(tmpFolder, dataSetCode);
        IHierarchicalContent content = contentProvider.asContent(dataSet);
        IHierarchicalContentNode rootNode = content.getRootNode();
        handle(rootNode, builder);
        builder.finish();
        SequenceType[] values = SequenceType.values();
        for (SequenceType sequenceType : values)
        {
            File fastaFile = builder.getTemporaryFastaFileOrNull(sequenceType);
            if (fastaFile == null)
            {
                continue;
            }
            String fastaFilePath = fastaFile.getAbsolutePath();
            String databaseName = FilenameUtils.removeExtension(fastaFile.getName());
            String databaseFile = new File(blastDatabasesFolder, databaseName).getAbsolutePath();
            String dbtype = sequenceType.toString().toLowerCase();
            boolean success = process(makeblastdb, "-in", fastaFilePath, "-dbtype", dbtype, 
                    "-title", databaseName, "-out", databaseFile);
            if (success == false)
            {
                operationLog.error("Creation of BLAST database failed for data set '" + dataSetCode 
                        + "'. Temporary fasta file: " + fastaFile);
                break;
            }
            File databaseSeqFile = new File(databaseFile + ".nsq");
            if (databaseSeqFile.exists() && databaseSeqFile.length() > 1000000)
            {
                process(makembindex, "-iformat", "blastdb", "-input", databaseFile, "-old_style_index", "false");
            }
            File allDatabaseFile = new File(blastDatabasesFolder, "all-" + dbtype + ".nal");
            if (allDatabaseFile.exists() == false)
            {
                FileUtilities.writeToFile(allDatabaseFile, "TITLE all-" + dbtype + "\nDBLIST");
            }
            FileUtilities.appendToFile(allDatabaseFile, " " + databaseName, false);
        }
        builder.cleanUp();
    }
    
    private boolean process(String... command)
    {
        return process(Arrays.asList(command));
    }
    
    boolean process(List<String> command)
    {
        ProcessResult processResult = ProcessExecutionHelper.run(command, operationLog, machineLog);
        if (processResult.isOK())
        {
            processResult.logAsInfo();
        } else
        {
            processResult.log();
        }
        return processResult.isOK();
    }
    
    private void handle(IHierarchicalContentNode node, FastaFileBuilder builder)
    {
        if (node.isDirectory())
        {
            for (IHierarchicalContentNode childNode : node.getChildNodes())
            {
                handle(childNode, builder);
            }
        } else
        {
            String nodeName = node.getName();
            for (String fileType : fileTypes)
            {
                if (nodeName.endsWith(fileType))
                {
                    appendTo(builder, node);
                    break;
                }
            }
        }
    }

    private void appendTo(FastaFileBuilder builder, IHierarchicalContentNode node)
    {
        InputStream inputStream = node.getInputStream();
        BufferedReader bufferedReader = null;
        String relativePath = node.getRelativePath();
        builder.setFilePath(relativePath);
        try
        {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null)
            {
                builder.handle(line);
            }
        } catch (IOException e)
        {
            throw new EnvironmentFailureException("Error while reading data from '" + relativePath 
                    + "': " + e.getMessage(), e);
        } finally 
        {
            IOUtils.closeQuietly(bufferedReader);
        }
    }

    private List<AbstractExternalData> getDataSets(IEncapsulatedOpenBISService service)
    {
        Long lastSeenEventId = getLastSeenEventId();
        if (lastSeenEventId == null)
        {
            lastSeenEventId = 0L;
        }
        TrackingDataSetCriteria criteria = new TrackingDataSetCriteria(lastSeenEventId);
        List<AbstractExternalData> dataSets = service.listNewerDataSets(criteria);
        Collections.sort(dataSets, new Comparator<AbstractExternalData>()
            {
                @Override
                public int compare(AbstractExternalData d0, AbstractExternalData d1)
                {
                    long id0 = d0.getId();
                    long id1 = d1.getId();
                    return id0 > id1 ? 1 : (id0 < id1 ? -1 : 0);
                }
            });
        return dataSets;
    }

    private Long getLastSeenEventId()
    {
        Long result = null;
        if (lastSeenDataSetFile.exists())
        {
            try
            {
                result = Long.parseLong(FileUtilities.loadToString(lastSeenDataSetFile).trim());
            } catch (Exception ex)
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("Cannot load last seen event id from file :"
                            + lastSeenDataSetFile, ex);
                }
            }
        }
        return result;
    }
    
    private void updateLastSeenEventId(Long eventId)
    {
        FileUtilities.writeToFile(lastSeenDataSetFile, String.valueOf(eventId) + "\n");
    }
    
    IConfigProvider getConfigProvider()
    {
        return ServiceProvider.getConfigProvider();
    }
    
    IEncapsulatedOpenBISService getOpenBISService()
    {
        return ServiceProvider.getOpenBISService();
    }
    
    IHierarchicalContentProvider getContentProvider()
    {
        return ServiceProvider.getHierarchicalContentProvider();
    }

}
