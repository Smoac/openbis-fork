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

package ch.systemsx.cisd.openbis.dss.phosphonetx.server.plugins;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.common.highwatermark.HostAwareFile;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.StringUtilities;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.Copier;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetCopier;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IPathCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.ISshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class MsInjectionCopier implements Serializable, IPostRegistrationDatasetHandler
{
    
    @Private static final String SAMPLE_UNKNOWN = "sample-unknown";

    private static final class ExceptionWithStatus extends RuntimeException
    {
        private static final long serialVersionUID = 1L;
        private final Status status;

        ExceptionWithStatus(Status status)
        {
            this.status = status;
        }

        ExceptionWithStatus(Status status, Exception ex)
        {
            super(ex);
            this.status = status;
        }

        public Status getStatus()
        {
            return status;
        }
    }
    
    private static interface IExecutor
    {
        BooleanStatus exists(File file);
        void deleteFolder(File folder);
        void copyDataSet(File dataSet, File destination);
        void renameTo(File newFile, File oldFile);
    }
    
    private static final class LocalExcecutor implements IExecutor
    {
        private final IFileOperations fileOperations;

        LocalExcecutor(IFileOperations fileOperations)
        {
            this.fileOperations = fileOperations;
        }
        
        public BooleanStatus exists(File file)
        {
            return BooleanStatus.createFromBoolean(fileOperations.exists(file));
        }

        public void deleteFolder(File folder)
        {
            try
            {
                fileOperations.deleteRecursively(folder);
            } catch (Exception ex)
            {
                operationLog.error("Deletion of '" + folder + "' failed.", ex);
                throw new ExceptionWithStatus(Status.createError("couldn't delete"));
            }
        }

        public void copyDataSet(File dataSet, File destination)
        {
            try
            {
                if (dataSet.isFile())
                {
                    fileOperations.copyFileToDirectory(dataSet, destination);
                } else
                {
                    fileOperations.copyDirectoryToDirectory(dataSet, destination);
                }
                new File(destination, dataSet.getName()).setLastModified(dataSet.lastModified());
            } catch (Exception ex)
            {
                operationLog.error("Couldn't copy '" + dataSet + "' to '" + destination + "'", ex);
                throw new ExceptionWithStatus(Status.createError("copy failed"), ex);
            }
        }

        public void renameTo(File newFile, File oldFile)
        {
            boolean result = oldFile.renameTo(newFile);
            if (result == false)
            {
                operationLog.error("Couldn't rename '" + oldFile + "' to '" + newFile + "'.");
                throw new ExceptionWithStatus(Status.createError("rename failed"));
            }
        }
        
    }
    
    private static final class RemoteExecutor implements IExecutor
    {
        private final ISshCommandExecutor executor;
        private final IPathCopier copier;
        private final String host;
        private final String rsyncModuleNameOrNull;
        private final String rsyncPasswordFileOrNull;

        public RemoteExecutor(ISshCommandExecutor executor, IPathCopier copier, String host,
                String rsyncModuleNameOrNull, String rsyncPasswordFileOrNull)
        {
            this.executor = executor;
            this.copier = copier;
            this.host = host;
            this.rsyncModuleNameOrNull = rsyncModuleNameOrNull;
            this.rsyncPasswordFileOrNull = rsyncPasswordFileOrNull;
        }
        
        public BooleanStatus exists(File file)
        {
            return executor.exists(file, DataSetCopier.SSH_TIMEOUT_MILLIS);
        }

        public void deleteFolder(File folder)
        {
            ProcessResult result =
                executor.tryExecuteCommandRemotely("rm -rf " + folder.getPath(),
                        DataSetCopier.SSH_TIMEOUT_MILLIS);
            if (result.isOK() == false)
            {
                operationLog.error("Remote deletion of '" + folder
                        + "' failed with exit value: " + result.getExitValue());
                throw new ExceptionWithStatus(Status.createError("couldn't delete"));
            }
            List<String> output = result.getOutput();
            if (output.isEmpty() == false)
            {
                operationLog.error("Remote deletion of '" + folder
                        + "' seemed to be successful but produced following output:\n"
                        + StringUtilities.concatenateWithNewLine(output));
                throw new ExceptionWithStatus(Status.createError("deletion leads to a problem"));
            }
        }
        
        public void copyDataSet(File dataSet, File destination)
        {
            Status result =
                    copier.copyToRemote(dataSet, destination, host, rsyncModuleNameOrNull,
                            rsyncPasswordFileOrNull);
            if (result.isError())
            {
                throw new ExceptionWithStatus(result);
            }
        }

        public void renameTo(File newFile, File oldFile)
        {
            ProcessResult result =
                    executor.tryExecuteCommandRemotely("mv " + oldFile.getPath() + " "
                            + newFile.getPath(), DataSetCopier.SSH_TIMEOUT_MILLIS);
            if (result.isOK() == false)
            {
                operationLog.error("Remote move of '" + oldFile
                        + "' to '" + newFile + "' failed with exit value: " + result.getExitValue());
                throw new ExceptionWithStatus(Status.createError("couldn't move"));
            }
            List<String> output = result.getOutput();
            if (output.isEmpty() == false)
            {
                operationLog.error("Remote move of '" + oldFile
                        + "' to '" + newFile 
                        + "' seemed to be successful but produced following output:\n"
                        + StringUtilities.concatenateWithNewLine(output));
                throw new ExceptionWithStatus(Status.createError("moving leads to a problem"));
            }
        }
    }

    private static final long serialVersionUID = 1L;

    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, MsInjectionCopier.class);

    private final Properties properties;

    private final IPathCopierFactory pathCopierFactory;

    private final ISshCommandExecutorFactory sshCommandExecutorFactory;

    private transient IExecutor executor;
    
    private transient File destination;
    
    MsInjectionCopier(Properties properties, IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory)
    {
        this.properties = properties;
        this.pathCopierFactory = pathCopierFactory;
        this.sshCommandExecutorFactory = sshCommandExecutorFactory;
        init();
    }

    private void init()
    {
        String hostFile =
                PropertyUtils.getMandatoryProperty(properties, DataSetCopier.DESTINATION_KEY);
        HostAwareFile hostAwareFile = HostAwareFileWithHighwaterMark.create(hostFile, -1);
        String hostOrNull = hostAwareFile.tryGetHost();
        destination = hostAwareFile.getFile();
        if (hostOrNull == null)
        {
            executor = new LocalExcecutor(FileOperations.getMonitoredInstanceForCurrentThread());
        } else
        {
            File sshExecutable = Copier.getExecutable(properties, DataSetCopier.SSH_EXEC);
            File rsyncExecutable = Copier.getExecutable(properties, DataSetCopier.RSYNC_EXEC);
            IPathCopier copier = pathCopierFactory.create(rsyncExecutable, sshExecutable);
            copier.check();
            String rsyncModule = hostAwareFile.tryGetRsyncModule();
            String rsyncPasswordFile =
                    properties.getProperty(DataSetCopier.RSYNC_PASSWORD_FILE_KEY);
            FileUtilities.checkPathCopier(copier, hostOrNull, null, rsyncModule, rsyncPasswordFile);
            ISshCommandExecutor sshCommandExecutor =
                    sshCommandExecutorFactory.create(sshExecutable, hostOrNull);
            executor =
                    new RemoteExecutor(sshCommandExecutor, copier, hostOrNull, rsyncModule,
                            rsyncPasswordFile);
        }
    }

    public void undoLastOperation()
    {
    }

    public Status handle(File originalData, DataSetInformation dataSetInformation,
            Map<String, String> parameterBindings)
    {
        if (destination == null)
        {
            init();
        }
        try
        {
            String target = createTargetFolderName(dataSetInformation, parameterBindings);
            File targetFolder = new File(destination, target);
            deleteTargetFolder(targetFolder);
            executor.copyDataSet(originalData, destination);
            executor.renameTo(targetFolder, new File(destination, originalData.getName()));
            return Status.OK;
            
        } catch (ExceptionWithStatus ex)
        {
            return ex.getStatus();
        }
    }

    private String createTargetFolderName(DataSetInformation dataSetInformation,
            Map<String, String> parameterBindings)
    {
        String dataSetTypeCode = dataSetInformation.getDataSetType().getCode();
        String sampleCode = parameterBindings.get(dataSetInformation.getDataSetCode());
        return (sampleCode == null ? SAMPLE_UNKNOWN : sampleCode) + "_" + dataSetTypeCode;
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

