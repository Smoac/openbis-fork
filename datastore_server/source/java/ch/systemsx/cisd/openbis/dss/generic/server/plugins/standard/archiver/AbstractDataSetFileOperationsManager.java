/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver;

import java.io.File;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandExecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.IDataSetFileOperationsExecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.LocalDataSetFileOperationsExcecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.RemoteDataSetFileOperationsExecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.Copier;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IPathCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.ISshCommandExecutorFactory;

/**
 * @author Jakub Straszewski
 */
public abstract class AbstractDataSetFileOperationsManager
{

    @Private
    protected static final String DESTINATION_KEY = "destination";

    @Private
    protected static final String TIMEOUT_KEY = "timeout";

    @Private
    protected static final String RSYNC_PASSWORD_FILE_KEY = "rsync-password-file";

    @Private
    protected static final String CHECK_EXISTENCE_FAILED = "couldn't check existence";

    @Private
    protected static final String DESTINATION_DOES_NOT_EXIST = "destination doesn't exist";

    @Private
    protected static final String RSYNC_EXEC = "rsync";

    @Private
    protected static final String SSH_EXEC = "ssh";

    @Private
    protected static final String GFIND_EXEC = "find";

    @Private
    protected static final long DEFAULT_TIMEOUT_SECONDS = 15;

    @Private
    protected static final String FOLDER_OF_AS_DELETED_MARKED_DATA_SETS = "DELETED";

    public static class ArchiveDestination
    {
        private final String destination;

        private final IDataSetFileOperationsExecutor executor;

        private final boolean isHosted;

        private final long timeoutInMillis;

        private ArchiveDestination(String destination, IDataSetFileOperationsExecutor executor, boolean isHosted, long timeoutInMillis)
        {
            this.destination = destination;
            this.executor = executor;
            this.isHosted = isHosted;
            this.timeoutInMillis = timeoutInMillis;
        }

        public String getDestination()
        {
            return destination;
        }

        public IDataSetFileOperationsExecutor getExecutor()
        {
            return executor;
        }

        public boolean isHosted()
        {
            return isHosted;
        }

        public long getTimeoutInMillis()
        {
            return timeoutInMillis;
        }

    }

    protected static ArchiveDestination createArchiveDestinationManager(Properties properties,
            IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory, String archiveDestinationHost, long timeoutInMillis)
    {
        HostAwareFile hostAwareFile = HostAwareFileWithHighwaterMark.create(archiveDestinationHost, -1);
        boolean isHosted = hostAwareFile.tryGetHost() != null;
        String destination = hostAwareFile.getPath();
        IDataSetFileOperationsExecutor executor;

        if (false == isHosted)
        {
            executor = createLocalDataSetFileOperationsExecutor(properties, pathCopierFactory, hostAwareFile, timeoutInMillis);
        } else
        {
            executor =
                    createRemoteDataSetFileOperationsExecutor(properties, pathCopierFactory, sshCommandExecutorFactory, hostAwareFile,
                            timeoutInMillis);
        }
        return new ArchiveDestination(destination, executor, isHosted, timeoutInMillis);
    }

    protected static RemoteDataSetFileOperationsExecutor createRemoteDataSetFileOperationsExecutor(Properties properties,
            IPathCopierFactory pathCopierFactory, ISshCommandExecutorFactory sshCommandExecutorFactory, HostAwareFile hostAwareFile,
            long timeoutInMillis)
    {
        String hostOrNull = hostAwareFile.tryGetHost();
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
        ISshCommandExecutor sshCommandExecutor = sshCommandExecutorFactory.create(sshExecutable, hostOrNull);
        RemoteDataSetFileOperationsExecutor result = new RemoteDataSetFileOperationsExecutor(sshCommandExecutor, copier,
                gfindExecutable, hostOrNull, rsyncModule, rsyncPasswordFile,
                timeoutInMillis);
        return result;
    }

    protected static LocalDataSetFileOperationsExcecutor createLocalDataSetFileOperationsExecutor(Properties properties,
            IPathCopierFactory pathCopierFactory,
            HostAwareFile hostAwareFile, long timeoutInMillis)
    {
        File sshExecutable = null; // don't use ssh locally
        File rsyncExecutable = Copier.getExecutable(properties, RSYNC_EXEC);
        IPathCopier copier =
                pathCopierFactory.create(rsyncExecutable, sshExecutable, timeoutInMillis);
        copier.check();
        String rsyncModule = hostAwareFile.tryGetRsyncModule();
        String rsyncPasswordFile = properties.getProperty(RSYNC_PASSWORD_FILE_KEY);
        LocalDataSetFileOperationsExcecutor result = new LocalDataSetFileOperationsExcecutor(
                FileOperations.getMonitoredInstanceForCurrentThread(), copier,
                rsyncModule, rsyncPasswordFile);
        return result;
    }

}
