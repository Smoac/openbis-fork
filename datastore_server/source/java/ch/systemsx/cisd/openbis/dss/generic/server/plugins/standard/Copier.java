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
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.common.highwatermark.HostAwareFile;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

public class Copier implements Serializable, IPostRegistrationDatasetHandler
{
    private static final long serialVersionUID = 1L;

    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, Copier.class);

    private final File rsyncExecutable;

    private final File sshExecutable;

    private final String hostFile;

    private final String rsyncPasswordFile;

    private final IPathCopierFactory pathCopierFactory;

    private final ISshCommandExecutorFactory sshCommandExecutorFactory;


    public Copier(Properties properties, IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory)
    {
        this.pathCopierFactory = pathCopierFactory;
        this.sshCommandExecutorFactory = sshCommandExecutorFactory;
        rsyncPasswordFile = properties.getProperty(DataSetCopier.RSYNC_PASSWORD_FILE_KEY);
        rsyncExecutable = getExecutable(properties, DataSetCopier.RSYNC_EXEC);
        sshExecutable = getExecutable(properties, DataSetCopier.SSH_EXEC);
        hostFile = PropertyUtils.getMandatoryProperty(properties, DataSetCopier.DESTINATION_KEY);
    }
    
    protected String transformHostFile(String originalHostFile, Map<String, String> parameterBindings)
    {
        return originalHostFile;
    }

    public Status handle(File originalData, DataSetInformation dataSetInformation, Map<String, String> parameterBindings)
    {
        HostAwareFile hostAwareFile =
                HostAwareFileWithHighwaterMark.create(transformHostFile(hostFile, parameterBindings), -1);
        String host = hostAwareFile.tryGetHost();
        ISshCommandExecutor sshCommandExecutor = sshCommandExecutorFactory.create(sshExecutable, host);
        String rsyncModule = hostAwareFile.tryGetRsyncModule();
        IPathCopier copier = pathCopierFactory.create(rsyncExecutable, sshExecutable);
        copier.check();
        if (host != null)
        {
            FileUtilities.checkPathCopier(copier, host, null, rsyncModule,
                    rsyncPasswordFile);
        }
        File destination = hostAwareFile.getFile();
        File destinationFile = new File(destination, originalData.getName());
        BooleanStatus destinationExists =
                checkDestinationFileExistence(destinationFile, host, sshCommandExecutor);

        if (destinationExists.isSuccess())
        {
            operationLog.error("Destination file/directory '" + destinationFile.getPath()
                    + "' already exists - dataset files will not be copied.");
            return Status.createError(DataSetCopier.ALREADY_EXIST_MSG);
        } else if (destinationExists.isError())
        {
            operationLog.error("Could not test destination file/directory '" + destinationFile
                    + "' existence" + (host != null ? " on host '" + host + "'" : "") + ": "
                    + destinationExists.tryGetMessage());
            return Status.createError(DataSetCopier.COPYING_FAILED_MSG);
        }
        Status status =
                copier.copyToRemote(originalData, destination, host, rsyncModule,
                        rsyncPasswordFile);
        if (status.isError())
        {
            operationLog.error("Could not copy data set " + dataSetInformation.getDataSetCode()
                    + " to destination folder '" + destination + "'"
                    + (host != null ? " on host '" + host + "'" : "")
                    + (rsyncModule != null ? " for rsync module '" + rsyncModule + "'" : "")
                    + ": " + status.tryGetErrorMessage());
            return Status.createError(DataSetCopier.COPYING_FAILED_MSG);
        }
        return status;
    }

    private BooleanStatus checkDestinationFileExistence(File destinationFile,
            String hostOrNull, ISshCommandExecutor sshCommandExecutor)
    {
        if (hostOrNull != null)
        {
            // check remotely using ssh
            return sshCommandExecutor.exists(destinationFile, DataSetCopier.SSH_TIMEOUT_MILLIS);
        } else
        {
            // check locally
            return BooleanStatus.createFromBoolean(destinationFile.exists());
        }
    }

    public void undoLastOperation()
    {
    }

    public static File getExecutable(Properties properties, String executable)
    {
        String executableProperty = properties.getProperty(executable + "-executable");
        File executableFile;
        if (executableProperty != null)
        {
            executableFile = new File(executableProperty);
        } else
        {
            executableFile = OSUtilities.findExecutable(executable);
            if (executableFile == null)
            {
                throw new ConfigurationFailureException("Could not found path to executable '"
                        + executable + "'.");
            }
        }
        if (executableFile.isFile() == false)
        {
            throw new ConfigurationFailureException("Path to executable '" + executable
                    + "' is not a file: " + executableFile.getAbsolutePath());
        }
        return executableFile;
    }

}