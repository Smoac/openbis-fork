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

import static ch.systemsx.cisd.yeastx.etl.ConstantsYeastX.ERROR_MARKER_FILE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.yeastx.etl.DatasetMappingUtil.DataSetMappingInformationFile;

/**
 * {@link IDataSetHandler} implementation which for each dataset directory reads all the files
 * inside that directory and runs the primary dataset handler for it.
 * 
 * @author Tomasz Pylak
 */
public class BatchDataSetHandler implements IDataSetHandler
{
    private final IDataSetHandler delegator;

    private final IMailClient mailClient;

    private final DatasetMappingResolver datasetMappingResolver;

    public BatchDataSetHandler(Properties parentProperties, IDataSetHandler delegator,
            IEncapsulatedOpenBISService openbisService)
    {
        this.delegator = delegator;
        this.mailClient = new MailClient(parentProperties);
        this.datasetMappingResolver =
                new DatasetMappingResolver(getSpecificProperties(parentProperties), openbisService);
    }

    private static Properties getSpecificProperties(Properties properties)
    {
        return ExtendedProperties.getSubset(properties, IDataSetHandler.DATASET_HANDLER_KEY + '.',
                true);
    }

    public List<DataSetInformation> handleDataSet(File datasetsParentDir)
    {
        if (canBatchBeProcessed(datasetsParentDir) == false)
        {
            return createEmptyResult();
        }
        LogUtils log = new LogUtils(datasetsParentDir);
        DataSetMappingInformationFile datasetMappingFile =
                DatasetMappingUtil.tryGetDatasetsMapping(datasetsParentDir, log);
        if (datasetMappingFile == null || datasetMappingFile.tryGetMappings() == null)
        {
            touchErrorMarkerFile(datasetsParentDir, log);
            sendNotificationsIfNecessary(log, tryGetEmail(datasetMappingFile));
            return createEmptyResult();
        }
        return processDatasets(datasetsParentDir, log, datasetMappingFile.tryGetMappings(),
                datasetMappingFile.getNotificationEmail());
    }

    private List<DataSetInformation> processDatasets(File datasetsParentDir, LogUtils log,
            TableMap<String, DataSetMappingInformation> mappings, String notificationEmail)
    {
        List<DataSetInformation> processedDatasetFiles = createEmptyResult();

        Set<String> processedFiles = new HashSet<String>();
        List<File> files = listAll(datasetsParentDir);
        for (File file : files)
        {
            if (canDatasetBeProcessed(file, mappings, log))
            {
                processedDatasetFiles.addAll(delegator.handleDataSet(file));
                processedFiles.add(file.getName().toLowerCase());
            }
        }
        clean(datasetsParentDir, processedFiles, log, mappings.values().size());
        sendNotificationsIfNecessary(log, notificationEmail);
        return processedDatasetFiles;
    }

    private void sendNotificationsIfNecessary(LogUtils log, String email)
    {
        log.sendNotificationsIfNecessary(mailClient, email);
    }

    private static String tryGetEmail(DataSetMappingInformationFile datasetMappingFileOrNull)
    {
        return datasetMappingFileOrNull == null ? null : datasetMappingFileOrNull
                .getNotificationEmail();
    }

    private static ArrayList<DataSetInformation> createEmptyResult()
    {
        return new ArrayList<DataSetInformation>();
    }

    private static boolean canBatchBeProcessed(File parentDir)
    {
        if (parentDir.isDirectory() == false)
        {
            return false;
        }
        if (errorMarkerFileExists(parentDir))
        {
            return false;
        }
        List<File> files = listAll(parentDir);
        // Do not treat empty directories as faulty.

        // The other reason of this check is that this handler is sometimes no able to delete
        // processed directories. It happens when they are mounted on NAS and there are some
        // hidden .nfs* files.
        if (files.size() == 0)
        {
            return false;
        }
        return true;
    }

    private static boolean errorMarkerFileExists(File datasetsParentDir)
    {
        return new File(datasetsParentDir, ERROR_MARKER_FILE).isFile();
    }

    private static void cleanMappingFile(File datasetsParentDir, Set<String> processedFiles,
            LogUtils log)
    {
        DatasetMappingUtil.cleanMappingFile(datasetsParentDir, processedFiles, log);
    }

    private static void clean(File datasetsParentDir, Set<String> processedFiles, LogUtils log,
            int datasetMappingsNumber)
    {
        cleanMappingFile(datasetsParentDir, processedFiles, log);

        int unprocessedDatasetsCounter = datasetMappingsNumber - processedFiles.size();
        if (unprocessedDatasetsCounter == 0 && hasNoPotentialDatasetFiles(datasetsParentDir))
        {
            cleanDatasetsDir(datasetsParentDir);
        } else
        {
            touchErrorMarkerFile(datasetsParentDir, log);
        }
    }

    private static void touchErrorMarkerFile(File parentDir, LogUtils log)
    {
        File errorMarkerFile = new File(parentDir, ERROR_MARKER_FILE);
        if (errorMarkerFile.isFile())
        {
            return;
        }
        boolean ok = false;
        try
        {
            ok = errorMarkerFile.createNewFile();
        } catch (IOException ex)
        {
        }
        if (ok == false)
        {
            LogUtils.adminError("Could not create an error marker file '%s'.", errorMarkerFile
                    .getPath());
        } else
        {
            log.warning(
                    "Correct the errors and delete the '%s' file to start processing again.",
                    ERROR_MARKER_FILE);
        }
    }

    private static void cleanDatasetsDir(File datasetsParentDir)
    {
        LogUtils.deleteUserLog(datasetsParentDir);
        DatasetMappingUtil.deleteMappingFile(datasetsParentDir);
        deleteEmptyDir(datasetsParentDir);
    }

    // Checks that the sample from the mapping exists and is assigned to the experiment - we do not
    // want to move datasets to unidentified directory in this case.
    private boolean canDatasetBeProcessed(File file,
            TableMap<String, DataSetMappingInformation> datasetsMapping, LogUtils log)
    {
        if (DatasetMappingUtil.isMappingFile(file))
        {
            return false;
        }
        DataSetMappingInformation mapping =
                DatasetMappingUtil.tryGetDatasetMapping(file, datasetsMapping);
        if (mapping == null)
        {
            return false;
        }
        return datasetMappingResolver.isMappingCorrect(mapping, log);
    }

    private static void deleteEmptyDir(File dir)
    {
        boolean ok = dir.delete();
        if (ok == false)
        {
            LogUtils.adminError(
                    "The directory '%s' cannot be deleted although it seems to be empty.", dir
                            .getPath());
        }
    }

    private static boolean hasNoPotentialDatasetFiles(File dir)
    {
        List<File> files = listAll(dir);
        int datasetsCounter = files.size();
        for (File file : files)
        {
            if (LogUtils.isUserLog(file) || DatasetMappingUtil.isMappingFile(file))
            {
                datasetsCounter--;
            }
        }
        return datasetsCounter == 0;
    }

    private static List<File> listAll(File dataSet)
    {
        return FileUtilities.listFilesAndDirectories(dataSet, false, null);
    }
}
