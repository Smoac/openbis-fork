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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.etlserver.IDataSetHandler;
import ch.systemsx.cisd.etlserver.utils.PreprocessingExecutor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.yeastx.etl.DatasetMappingUtil.DataSetMappingInformationFile;

/**
 * {@link IDataSetHandler} implementation which for each dataset directory reads all the files
 * inside that directory and runs the primary dataset handler for it.<br>
 * Following properties can be configured:<br> {@link PreprocessingExecutor#PREPROCESSING_SCRIPT_PATH} -
 * the path to the script which acquires write access.
 * 
 * @author Tomasz Pylak
 */
public class BatchDataSetHandler implements IDataSetHandler
{
    private final IDataSetHandler delegator;

    private final IMailClient mailClient;

    private final DatasetMappingResolver datasetMappingResolver;

    // the script which ensures that we have write access to the datasets
    private final PreprocessingExecutor writeAccessSetter;

    public BatchDataSetHandler(Properties parentProperties, IDataSetHandler delegator,
            IEncapsulatedOpenBISService openbisService)
    {
        this.delegator = delegator;
        this.mailClient = new MailClient(parentProperties);
        Properties specificProperties = getSpecificProperties(parentProperties);
        this.datasetMappingResolver =
                new DatasetMappingResolver(specificProperties, openbisService);
        this.writeAccessSetter = PreprocessingExecutor.create(specificProperties);
    }

    private static Properties getSpecificProperties(Properties properties)
    {
        return ExtendedProperties.getSubset(properties, IDataSetHandler.DATASET_HANDLER_KEY + '.',
                true);
    }

    public List<DataSetInformation> handleDataSet(File batchDir)
    {
        if (canBatchBeProcessed(batchDir) == false)
        {
            return createEmptyResult();
        }
        LogUtils log = new LogUtils(batchDir);
        if (callPreprocessingScript(batchDir, log) == false)
        {
            return flushErrors(batchDir, null, log);
        }
        DataSetMappingInformationFile mappingFile =
                DatasetMappingUtil.tryGetDatasetsMapping(batchDir, log);
        if (mappingFile == null || mappingFile.tryGetMappings() == null)
        {
            return flushErrors(batchDir, mappingFile, log);
        }

        return processDatasets(batchDir, log, mappingFile.tryGetMappings(), mappingFile
                .getNotificationEmail());
    }

    private ArrayList<DataSetInformation> flushErrors(File batchDir,
            DataSetMappingInformationFile datasetMappingFileOrNull, LogUtils log)
    {
        touchErrorMarkerFile(batchDir, log);
        sendNotificationsIfNecessary(log, tryGetEmail(datasetMappingFileOrNull));
        return createEmptyResult();
    }

    // false if script failed
    private boolean callPreprocessingScript(File batchDir, LogUtils log)
    {
        boolean ok = writeAccessSetter.execute(batchDir.getName());
        if (ok == false)
        {
            String errorMsg =
                    String.format("No datasets from '%s' directory can be processed because "
                            + "the try to acquire write access by openBIS has failed.", batchDir
                            .getName());
            log.error(errorMsg + " Try again after some time or contact your administrator.");
            log.adminError(errorMsg);
        }
        return ok;
    }

    private List<DataSetInformation> processDatasets(File batchDir, LogUtils log,
            TableMap<String, DataSetMappingInformation> mappings, String notificationEmail)
    {
        List<DataSetInformation> processedDatasetFiles = createEmptyResult();

        Set<String> unknownMappings = new HashSet<String>(mappings.keySet());
        Set<String> processedFiles = new HashSet<String>();
        List<File> files = listAll(batchDir);
        for (File file : files)
        {
            unknownMappings.remove(file.getName().toLowerCase());
            // we have already tries to acquire write access to all files in batch directory,
            // but some new files may have appeared since that time.
            // We do not retry if the operation fails - it could take hours if the problem cannot be
            // solved by retries (we usually are dealing with hundreds of files)
            boolean isWritable = acquireWriteAccessWithoutRetries(batchDir, file, log);
            if (isWritable == false)
            {
                logNonWritable(file, log);
                continue;
            }
            if (canDatasetBeProcessed(file, mappings, log))
            {
                List<DataSetInformation> processed = delegateProcessing(file, log);
                processedDatasetFiles.addAll(processed);
                if (processed.size() > 0)
                {
                    processedFiles.add(file.getName().toLowerCase());
                }
            }
        }
        if (unknownMappings.size() > 0)
        {
            logUnknownMappings(unknownMappings, log);
        }
        clean(batchDir, processedFiles, log, mappings.values().size());
        sendNotificationsIfNecessary(log, notificationEmail);
        return processedDatasetFiles;
    }

    private List<DataSetInformation> delegateProcessing(File dataset, LogUtils log)
    {
        try
        {
            return delegator.handleDataSet(dataset);
        } catch (UserFailureException e)
        {
            log.datasetFileError(dataset,
                    "unexpected error occured, the dataset has been moved to the error directory. The reason is: "
                            + e.getMessage());
            return createEmptyResult();
        }
    }

    private void logUnknownMappings(Set<String> unknownMappings, LogUtils log)
    {
        String unknownFiles = CollectionUtils.abbreviate(unknownMappings, -1);
        log
                .error("There are following files mentioned in the mapping file which do not exist:\n"
                        + unknownFiles
                        + "\nBrowse the mapping file and check if you have not misspelled some file names.");
    }

    private void logNonWritable(File file, LogUtils log)
    {
        log.error("Could not acquire write access to '%s'. "
                + "Try again or contact your administrator.", file.getPath());
    }

    // Acquires write access if the file is not writable. Does not retry if the operation fails.
    // Returns true if file is writable afterwards.
    private boolean acquireWriteAccessWithoutRetries(File batchDir, File file, LogUtils log)
    {
        if (file.exists() == false)
        {
            log.error("File '%s' does not exist.", file.getPath());
            return false; // someone could deleted the file in the meantime
        }
        if (isWritable(file) == false)
        {
            String path =
                    batchDir.getName() + System.getProperty("file.separator") + file.getName();
            boolean ok = writeAccessSetter.executeOnce(path);
            if (ok == false)
            {
                log.adminError("Cannot acquire write access to '%s' "
                        + "because write access setter failed", path);
            }
            return isWritable(file);
        } else
        {
            return true;
        }
    }

    private static boolean isWritable(File file)
    {
        return file.canWrite();
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

    // true if we deal with a directory which contains no error marker file and is not empty
    private static boolean canBatchBeProcessed(File batchDir)
    {
        if (batchDir.isDirectory() == false)
        {
            return false;
        }
        if (errorMarkerFileExists(batchDir))
        {
            return false;
        }
        if (DatasetMappingUtil.isMappingFilePresent(batchDir) == false)
        {
            return false;
        }
        List<File> files = listAll(batchDir);
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

    private static boolean errorMarkerFileExists(File batchDir)
    {
        return new File(batchDir, ERROR_MARKER_FILE).isFile();
    }

    private static void cleanMappingFile(File batchDir, Set<String> processedFiles, LogUtils log)
    {
        DatasetMappingUtil.cleanMappingFile(batchDir, processedFiles, log);
    }

    private static void clean(File batchDir, Set<String> processedFiles, LogUtils log,
            int datasetMappingsNumber)
    {
        cleanMappingFile(batchDir, processedFiles, log);

        int unprocessedDatasetsCounter = datasetMappingsNumber - processedFiles.size();
        boolean hasNoPotentialDatasetFiles = hasNoPotentialDatasetFiles(batchDir);
        if (unprocessedDatasetsCounter == 0 && hasNoPotentialDatasetFiles)
        {
            cleanDatasetsDir(batchDir, log);
        } else
        {
            touchErrorMarkerFile(batchDir, log);
        }
    }

    private static void touchErrorMarkerFile(File batchDir, LogUtils log)
    {
        File errorMarkerFile = new File(batchDir, ERROR_MARKER_FILE);
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
            log
                    .adminError("Could not create an error marker file '%s'.", errorMarkerFile
                            .getPath());
        } else
        {
            log.warning("Correct the errors and delete the '%s' file to start processing again.",
                    ERROR_MARKER_FILE);
        }
    }

    private static void cleanDatasetsDir(File batchDir, LogUtils log)
    {
        LogUtils.deleteUserLog(batchDir);
        DatasetMappingUtil.deleteMappingFile(batchDir, log);
        deleteEmptyDir(batchDir);
    }

    // Checks that the sample from the mapping exists and is assigned to the experiment - we do not
    // want to move datasets to unidentified directory in this case.
    private boolean canDatasetBeProcessed(File file,
            TableMap<String, DataSetMappingInformation> datasetsMapping, LogUtils log)
    {
        if (isPotentialDatasetFile(file) == false)
        {
            return false;
        }
        DataSetMappingInformation mapping =
                DatasetMappingUtil.tryGetDatasetMapping(file, datasetsMapping);
        if (mapping == null)
        {
            log.error(file.getName() + " - no mapping could be found for this dataset");
            return false;
        }
        return datasetMappingResolver.isMappingCorrect(mapping, log);
    }

    private static boolean deleteEmptyDir(File dir)
    {
        boolean ok = dir.delete();
        if (ok == false)
        {
            LogUtils.adminWarn(
                    "The directory '%s' cannot be deleted although it seems to be empty.", dir
                            .getPath());
        }
        return ok;
    }

    private static boolean hasNoPotentialDatasetFiles(File batchDir)
    {
        List<File> files = listAll(batchDir);
        int datasetsCounter = files.size();
        for (File file : files)
        {
            if (isPotentialDatasetFile(file) == false)
            {
                datasetsCounter--;
            }
        }
        return datasetsCounter == 0;
    }

    private static boolean isPotentialDatasetFile(File file)
    {
        return LogUtils.isUserLog(file) == false && DatasetMappingUtil.isMappingFile(file) == false;
    }

    private static List<File> listAll(File dataSet)
    {
        List<File> files = FileUtilities.listFilesAndDirectories(dataSet, false, null);
        Collections.sort(files);
        return files;
    }
}
