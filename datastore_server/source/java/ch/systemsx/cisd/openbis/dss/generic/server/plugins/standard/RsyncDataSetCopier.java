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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ExceptionWithStatus;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.IDataSetFileOperationsExecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.LocalDataSetFileOperationsExcecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.RemoteDataSetFileOperationsExecutor;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Code based on LocalAndRemoteCopier, able to copy dataset files both ways: to and from
 * destination.
 * 
 * @author Piotr Buczek
 */
public class RsyncDataSetCopier
{

    private static final String DESTINATION_KEY = "destination";

    private static final String RSYNC_PASSWORD_FILE_KEY = "rsync-password-file";

    private static final String CHECK_EXISTENCE_FAILED = "couldn't check existence";

    private static final String DESTINATION_DOES_NOT_EXIST = "destination doesn't exist";

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            RsyncDataSetCopier.class);

    private static final String RSYNC_EXEC = "rsync";

    private static final String SSH_EXEC = "ssh";

    private static final long SSH_TIMEOUT_MILLIS = 15 * 1000; // 15s

    private final IDataSetFileOperationsExecutor executor;

    private final File destination;

    public RsyncDataSetCopier(Properties properties, IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory)
    {
        String hostFile = PropertyUtils.getMandatoryProperty(properties, DESTINATION_KEY);
        HostAwareFile hostAwareFile = HostAwareFileWithHighwaterMark.create(hostFile, -1);
        String hostOrNull = hostAwareFile.tryGetHost();

        this.destination = hostAwareFile.getFile();

        if (hostOrNull == null)
        {
            this.executor =
                    new LocalDataSetFileOperationsExcecutor(
                            FileOperations.getMonitoredInstanceForCurrentThread());
        } else
        {
            File sshExecutable = Copier.getExecutable(properties, SSH_EXEC);
            File rsyncExecutable = Copier.getExecutable(properties, RSYNC_EXEC);
            IPathCopier copier = pathCopierFactory.create(rsyncExecutable, sshExecutable);
            copier.check();
            String rsyncModule = hostAwareFile.tryGetRsyncModule();
            String rsyncPasswordFile = properties.getProperty(RSYNC_PASSWORD_FILE_KEY);
            FileUtilities.checkPathCopier(copier, hostOrNull, null, rsyncModule, rsyncPasswordFile,
                    SSH_TIMEOUT_MILLIS);
            ISshCommandExecutor sshCommandExecutor =
                    sshCommandExecutorFactory.create(sshExecutable, hostOrNull);
            this.executor =
                    new RemoteDataSetFileOperationsExecutor(sshCommandExecutor, copier, hostOrNull,
                            rsyncModule, rsyncPasswordFile);
        }
    }

    /**
     * Copies specified data file/folder to destination specified in constructor. The name of the
     * file/folder at the destination is defined by the given data set code.
     */
    public Status copyToDestination(File originalData, DatasetDescription dataset)
    {
        try
        {
            File destinationFolder = new File(destination, dataset.getDatasetCode());
            deleteDestinationFolder(destinationFolder); // cleanup needed for local executor
            operationLog.info("Copy dataset '" + dataset.getDatasetCode() + "' from '"
                    + originalData.getPath() + "' to '" + destination.getPath());
            executor.copyDataSetToDestination(originalData, destination);
            return Status.OK;
        } catch (ExceptionWithStatus ex)
        {
            return ex.getStatus();
        }
    }

    /**
     * Retrieves specified data file/folder from the destination specified in constructor. The name
     * of the file/folder at the destination is defined by the given data set code.
     */
    public Status retrieveFromDestination(File originalData, DatasetDescription dataset)
    {
        try
        {
            File destinationFolder = new File(destination, dataset.getDatasetCode());
            checkDestinationExists(destinationFolder);
            operationLog.info("Retrieve data set '" + dataset.getDatasetCode() + "' from '"
                    + destinationFolder.getPath() + "' to '"
                    + originalData.getParentFile().getPath());
            executor.retrieveDataSetFromDestination(originalData.getParentFile(), destinationFolder);
            return Status.OK;
        } catch (ExceptionWithStatus ex)
        {
            return ex.getStatus();
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

    private void deleteDestinationFolder(File destinationFolder)
    {
        BooleanStatus destinationExists = destinationExists(destinationFolder);
        if (destinationExists.isSuccess())
        {
            executor.deleteFolder(destinationFolder);
        }
    }

}