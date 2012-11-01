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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.util.Properties;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ExceptionWithStatus;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.IDataSetFileOperationsExecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.LocalDataSetFileOperationsExcecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.RemoteDataSetFileOperationsExecutor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Code based on LocalAndRemoteCopier, able to copy dataset files both ways: to and from
 * destination.
 * 
 * @author Piotr Buczek
 */
public class DataSetFileOperationsManager implements IDataSetFileOperationsManager
{
    private static interface IDeleteAction
    {
        String getName();
        void delete(File dataSetFolder, String dataSetCode);
    }


    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetFileOperationsManager.class);

    @Private
    static final String DESTINATION_KEY = "destination";

    @Private
    static final String TIMEOUT_KEY = "timeout";

    @Private
    static final String RSYNC_PASSWORD_FILE_KEY = "rsync-password-file";

    @Private
    static final String CHECK_EXISTENCE_FAILED = "couldn't check existence";

    @Private
    static final String DESTINATION_DOES_NOT_EXIST = "destination doesn't exist";

    @Private
    static final String RSYNC_EXEC = "rsync";

    @Private
    static final String SSH_EXEC = "ssh";

    @Private
    static final String GFIND_EXEC = "find";

    @Private
    static final long DEFAULT_TIMEOUT_SECONDS = 15;

    @Private static final String FOLDER_OF_AS_DELETED_MARKED_DATA_SETS = "DELETED";

    private final IDataSetFileOperationsExecutor executor;

    private final String destination;

    private final long timeoutInMillis;

    private final boolean isHosted;

    public DataSetFileOperationsManager(Properties properties,
            IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory)
    {
        String hostFile = PropertyUtils.getMandatoryProperty(properties, DESTINATION_KEY);
        HostAwareFile hostAwareFile = HostAwareFileWithHighwaterMark.create(hostFile, -1);
        String hostOrNull = hostAwareFile.tryGetHost();

        this.isHosted = hostOrNull != null;

        this.destination = hostAwareFile.getPath();
        long timeoutInSeconds =
                PropertyUtils.getLong(properties, TIMEOUT_KEY, DEFAULT_TIMEOUT_SECONDS);
        this.timeoutInMillis = timeoutInSeconds * DateUtils.MILLIS_PER_SECOND;

        if (hostOrNull == null)
        {
            File sshExecutable = null; // don't use ssh locally
            File rsyncExecutable = Copier.getExecutable(properties, RSYNC_EXEC);
            IPathCopier copier =
                    pathCopierFactory.create(rsyncExecutable, sshExecutable, timeoutInMillis);
            copier.check();
            String rsyncModule = hostAwareFile.tryGetRsyncModule();
            String rsyncPasswordFile = properties.getProperty(RSYNC_PASSWORD_FILE_KEY);
            this.executor =
                    new LocalDataSetFileOperationsExcecutor(
                            FileOperations.getMonitoredInstanceForCurrentThread(), copier,
                            rsyncModule, rsyncPasswordFile);
        } else
        {
            File sshExecutable = Copier.getExecutable(properties, SSH_EXEC);
            File rsyncExecutable = Copier.getExecutable(properties, RSYNC_EXEC);
            File gfindExecutable = Copier.getExecutable(properties, GFIND_EXEC);

            IPathCopier copier =
                    pathCopierFactory.create(rsyncExecutable, sshExecutable, timeoutInMillis);
            copier.check();
            String rsyncModule = hostAwareFile.tryGetRsyncModule();
            String rsyncPasswordFile = properties.getProperty(RSYNC_PASSWORD_FILE_KEY);
            FileUtilities.checkPathCopier(copier, hostOrNull, null, rsyncModule, rsyncPasswordFile,
                    timeoutInMillis);
            ISshCommandExecutor sshCommandExecutor =
                    sshCommandExecutorFactory.create(sshExecutable, hostOrNull);
            this.executor =
                    new RemoteDataSetFileOperationsExecutor(sshCommandExecutor, copier,
                            gfindExecutable, hostOrNull, rsyncModule, rsyncPasswordFile,
                            timeoutInMillis);

        }
    }

    /**
     * Copies specified dataset's data to destination specified in constructor. The path at the
     * destination is defined by the original location of the data set.
     */
    @Override
    public Status copyToDestination(File originalData, DatasetDescription dataset)
    {
        try
        {
            File destinationFolder = new File(destination, dataset.getDataSetLocation());
            if (createFolderIfNotExists(destinationFolder.getParentFile())
                    || destinationExists(destinationFolder).isSuccess() == false)
            {
                operationLog.info("Copy dataset '" + dataset.getDataSetCode() + "' from '"
                        + originalData.getPath() + "' to '" + destinationFolder.getParentFile());
                executor.copyDataSetToDestination(originalData, destinationFolder.getParentFile());
            } else
            {
                operationLog.info("Update dataset '" + dataset.getDataSetCode() + "' from '"
                        + originalData.getPath() + "' to '" + destinationFolder.getParentFile());
                executor.syncDataSetWithDestination(originalData, destinationFolder.getParentFile());
            }
            return Status.OK;
        } catch (ExceptionWithStatus ex)
        {
            return ex.getStatus();
        }
    }

    /**
     * Retrieves specified datases's data from the destination specified in constructor. The path at
     * the destination is defined by original location of the data set.
     */
    @Override
    public Status retrieveFromDestination(File originalData, DatasetDescription dataset)
    {
        try
        {
            File destinationFolder = new File(destination, dataset.getDataSetLocation());
            checkDestinationExists(destinationFolder);
            File folder = originalData.getParentFile();
            operationLog.info("Retrieve data set '" + dataset.getDataSetCode() + "' from '"
                    + destinationFolder.getPath() + "' to '" + folder);
            folder.mkdirs();
            executor.retrieveDataSetFromDestination(folder, destinationFolder);
            return Status.OK;
        } catch (ExceptionWithStatus ex)
        {
            return ex.getStatus();
        }
    }

    /**
     * Deletes specified datases's data from the destination specified in constructor. The path at
     * the destination is defined by original location of the data set.
     */
    @Override
    public Status deleteFromDestination(IDatasetLocation dataset)
    {
        return delete(dataset, new IDeleteAction()
            {
                @Override
                public String getName()
                {
                    return "delete";
                }

                @Override
                public void delete(File dataSetFolder, String dataSetCode)
                {
                    executor.deleteFolder(dataSetFolder);
                }
            });
    }

    @Override
    public Status markAsDeleted(IDatasetLocation dataset)
    {
        return delete(dataset, new IDeleteAction()
            {
                @Override
                public String getName()
                {
                    return "mark as deleted";
                }

                @Override
                public void delete(File dataSetFolder, String dataSetCode)
                {
                    File deletedFolder =
                            new File(destination, FOLDER_OF_AS_DELETED_MARKED_DATA_SETS);
                    executor.createFolder(deletedFolder);
                    File markerFile = new File(deletedFolder, dataSetCode);
                    executor.createMarkerFile(markerFile);
                }
            });
    }
    
    private Status delete(IDatasetLocation dataset, IDeleteAction action)
    {
        try
        {
            File destinationFolder = new File(destination, dataset.getDataSetLocation());
            BooleanStatus destinationExists = destinationExists(destinationFolder);
            if (destinationExists.isSuccess())
            {
                action.delete(destinationFolder, dataset.getDataSetCode());
            } else
            {
                operationLog.info("Data of data set '" + dataset.getDataSetCode()
                        + "' don't exist in the destination '" + destinationFolder.getPath()
                        + "'. There is nothing to " + action.getName() + ".");
            }
            return Status.OK;
        } catch (ExceptionWithStatus ex)
        {
            return ex.getStatus();
        }
    }
    
    /**
     * Checks if specified dataset's data are present and synchronized in the destination specified
     * in constructor. The path at the destination is defined by original location of the data set.
     */
    @Override
    public BooleanStatus isSynchronizedWithDestination(File originalData, DatasetDescription dataset)
    {
        try
        {
            File destinationFolder = new File(destination, dataset.getDataSetLocation());
            BooleanStatus resultStatus = executor.checkSame(originalData, destinationFolder);
            String message = resultStatus.tryGetMessage();
            if (message != null) // if there is a message something went wrong
            {
                operationLog.error(message);
            }
            return resultStatus;
        } catch (ExceptionWithStatus ex)
        {
            return BooleanStatus.createError(ex.getStatus().toString());
        }
    }

    /**
     * Checks if specified dataset's data are present in the destination specified in constructor.
     * The path at the destination is defined by original location of the data set.
     */
    @Override
    public BooleanStatus isPresentInDestination(DatasetDescription dataset)
    {
        try
        {
            File destinationFolder = new File(destination, dataset.getDataSetLocation());
            BooleanStatus resultStatus = executor.exists(destinationFolder);
            String message = resultStatus.tryGetMessage();
            if (message != null) // if there is a message something went wrong
            {
                operationLog.error(message);
            }
            return resultStatus;
        } catch (ExceptionWithStatus ex)
        {
            return BooleanStatus.createError(ex.getStatus().toString());
        }
    }

    private void checkDestinationExists(File destinationFolder)
    {
        BooleanStatus destinationExists = destinationExists(destinationFolder);
        if (destinationExists.isSuccess() == false)
        {
            operationLog.error("Destination folder '" + destinationFolder + "' doesn't exist");
            throw new ExceptionWithStatus(Status.createError(DESTINATION_DOES_NOT_EXIST));
        }
    }

    private boolean createFolderIfNotExists(File destinationFolder)
    {
        BooleanStatus destinationExists = destinationExists(destinationFolder);
        if (destinationExists.isSuccess() == false)
        {
            executor.createFolder(destinationFolder);
            return true;
        }
        return false;
    }

    private BooleanStatus destinationExists(File destinationFolder)
    {
        BooleanStatus destinationExists = executor.exists(destinationFolder);
        if (destinationExists.isError())
        {
            operationLog.error("Could not check existence of '" + destinationFolder + "': "
                    + destinationExists.tryGetMessage());
            throw new ExceptionWithStatus(Status.createError(CHECK_EXISTENCE_FAILED));
        }
        return destinationExists;
    }

    @Override
    public boolean isHosted()
    {
        return isHosted;
    }

    @Override
    public File getDestinationFile(DatasetDescription dataset)
    {
        return new File(destination, dataset.getDataSetLocation());
    }
}