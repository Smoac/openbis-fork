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

package ch.systemsx.cisd.openbis.dss.proteomics.server.plugins;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
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
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.Copier;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IPathCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.ISshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.RSyncConfig;

/**
 * @author Franz-Josef Elmer
 */
class LocalAndRemoteCopier implements Serializable, IPostRegistrationDatasetHandler
{

    @Private
    static final String MARKER_FILE_PREFIX = "marker-file-prefix";

    @Private
    static final String SAMPLE_UNKNOWN = "sample-unknown";

    private static final long serialVersionUID = 1L;

    final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            LocalAndRemoteCopier.class);

    private final Properties properties;

    private final IPathCopierFactory pathCopierFactory;

    private final ISshCommandExecutorFactory sshCommandExecutorFactory;

    private transient IDataSetFileOperationsExecutor executor;

    private transient String destination;

    private String markerFilePrefix;

    LocalAndRemoteCopier(Properties properties, IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory)
    {
        this.properties = properties;
        this.pathCopierFactory = pathCopierFactory;
        this.sshCommandExecutorFactory = sshCommandExecutorFactory;
        markerFilePrefix = properties.getProperty(MARKER_FILE_PREFIX);
        if (markerFilePrefix != null && markerFilePrefix.length() == 0)
        {
            throw new ConfigurationFailureException("marker-file-prefix is an empty string.");
        }
        init();
    }

    private void init()
    {
        String hostFile =
                PropertyUtils.getMandatoryProperty(properties, DataSetCopier.DESTINATION_KEY);
        HostAwareFile hostAwareFile = HostAwareFileWithHighwaterMark.create(hostFile, -1);
        String hostOrNull = hostAwareFile.tryGetHost();
        destination = hostAwareFile.getPath();
        if (hostOrNull == null)
        {
            File sshExecutable = null; // don't use ssh locally
            File rsyncExecutable = Copier.getExecutable(properties, DataSetCopier.RSYNC_EXEC);
            IPathCopier copier =
                    pathCopierFactory.create(rsyncExecutable, sshExecutable,
                            DataSetCopier.SSH_TIMEOUT_MILLIS, RSyncConfig.getInstance().getAdditionalCommandLineOptions());
            copier.check();
            String rsyncModule = hostAwareFile.tryGetRsyncModule();
            String rsyncPasswordFile =
                    properties.getProperty(DataSetCopier.RSYNC_PASSWORD_FILE_KEY);
            executor =
                    new LocalDataSetFileOperationsExcecutor(
                            FileOperations.getMonitoredInstanceForCurrentThread(), copier,
                            rsyncModule, rsyncPasswordFile);
        } else
        {
            File sshExecutable = Copier.getExecutable(properties, DataSetCopier.SSH_EXEC);
            File rsyncExecutable = Copier.getExecutable(properties, DataSetCopier.RSYNC_EXEC);
            File gfindExecutable = Copier.getExecutable(properties, DataSetCopier.GFIND_EXEC);
            IPathCopier copier =
                    pathCopierFactory.create(rsyncExecutable, sshExecutable,
                            DataSetCopier.SSH_TIMEOUT_MILLIS, RSyncConfig.getInstance().getAdditionalCommandLineOptions());
            copier.check();
            String rsyncModule = hostAwareFile.tryGetRsyncModule();
            String rsyncPasswordFile =
                    properties.getProperty(DataSetCopier.RSYNC_PASSWORD_FILE_KEY);
            FileUtilities.checkPathCopier(copier, hostOrNull, null, rsyncModule, rsyncPasswordFile,
                    DataSetCopier.SSH_TIMEOUT_MILLIS);
            ISshCommandExecutor sshCommandExecutor =
                    sshCommandExecutorFactory.create(sshExecutable, hostOrNull);
            executor =
                    new RemoteDataSetFileOperationsExecutor(sshCommandExecutor, copier,
                            gfindExecutable, hostOrNull, rsyncModule, rsyncPasswordFile,
                            DataSetCopier.SSH_TIMEOUT_MILLIS);
        }
    }

    @Override
    public void undoLastOperation()
    {
    }

    /**
     * Copies specified data file/folder to destination specified in constructor. The name of the file/folder at the destination is defined by the
     * data set code.
     */
    @Override
    public Status handle(File originalData, DataSetInformation dataSetInformation,
            Map<String, String> parameterBindings)
    {
        if (destination == null)
        {
            init();
        }
        try
        {
            final File destinationLocalFile = new File(destination);
            String target = dataSetInformation.getDataSetCode();
            File targetFolder = new File(destinationLocalFile, target);
            deleteTargetFolder(targetFolder);
            executor.copyDataSetToDestination(originalData, destinationLocalFile);
            executor.renameTo(targetFolder, new File(destination, originalData.getName()));
            if (markerFilePrefix != null)
            {
                executor.createMarkerFile(new File(destination, markerFilePrefix + target));
            }
            return Status.OK;

        } catch (ExceptionWithStatus ex)
        {
            return ex.getStatus();
        }
    }

    private void deleteTargetFolder(File targetFolder)
    {
        BooleanStatus targetExists = executor.exists(targetFolder);
        if (targetExists.isError())
        {
            operationLog.error("Could not check existence of '" + targetFolder + "': "
                    + targetExists.tryGetMessage());
            throw new ExceptionWithStatus(Status.createError("couldn't check existence"));
        }
        if (targetExists.isSuccess())
        {
            executor.deleteFolder(targetFolder);
        }
    }

}
