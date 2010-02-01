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

package ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard;

import java.io.File;
import java.io.Serializable;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.rsync.RsyncCopier;
import ch.systemsx.cisd.common.highwatermark.HostAwareFile;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Processing plugin which copies data sets to a destination folder by using rsync. The destination
 * can be
 * <ul>
 * <li>on the local file system,
 * <li>a mounted remote folder,
 * <li>a remote folder accessible via SSH,
 * <li>a remote folder accessible via an rsync server.
 * </ul>
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetCopier extends AbstractDropboxProcessingPlugin
{
    private static final long serialVersionUID = 1L;

    @Private
    static final String DESTINATION_KEY = "destination";

    @Private
    static final String RSYNC_PASSWORD_FILE_KEY = "rsync-password-file";

    private static final String ALREADY_EXIST_MSG = "already exist";

    private static final String COPYING_FAILED_MSG = "copying failed";

    private static final String RSYNC_EXEC = "rsync";

    private static final String SSH_EXEC = "ssh";

    @Private
    static interface IPathCopierFactory
    {
        IPathCopier create(File rsyncExecutable, File sshExecutableOrNull);
    }

    private static final class RsyncCopierFactory implements Serializable, IPathCopierFactory
    {
        private static final long serialVersionUID = 1L;

        public IPathCopier create(File rsyncExecutable, File sshExecutableOrNull)
        {
            return new RsyncCopier(rsyncExecutable, sshExecutableOrNull, false, false);
        }
    }

    private static final class Copier implements Serializable, IPostRegistrationDatasetHandler
    {

        private static final long serialVersionUID = 1L;

        private transient IPathCopier copier;

        private final File rsyncExecutable;

        private final File sshExecutable;

        private final Properties properties;

        private final String host;

        private final String rsyncModule;

        private final File destination;

        private final String rsyncPasswordFile;

        private final IPathCopierFactory factory;

        public Copier(Properties properties, IPathCopierFactory factory)
        {
            this.properties = properties;
            this.factory = factory;
            rsyncPasswordFile = properties.getProperty(RSYNC_PASSWORD_FILE_KEY);
            rsyncExecutable = getExecutable(RSYNC_EXEC);
            sshExecutable = getExecutable(SSH_EXEC);
            HostAwareFile hostAwareFile =
                    HostAwareFileWithHighwaterMark.fromProperties(properties, DESTINATION_KEY);
            host = hostAwareFile.tryGetHost();
            rsyncModule = hostAwareFile.tryGetRsyncModule();
            destination = hostAwareFile.getFile();
            getCopier();
        }

        private IPathCopier getCopier()
        {
            if (copier == null)
            {
                copier = factory.create(rsyncExecutable, sshExecutable);
                copier.check();
                if (host != null)
                {
                    FileUtilities.checkPathCopier(copier, host, null, rsyncModule,
                            rsyncPasswordFile);
                }
            }
            return copier;
        }

        public Status handle(File originalData, DataSetInformation dataSetInformation)
        {
            if (checkDestinationExists())
            {
                operationLog.error("Destination directory '" + destination.getPath()
                        + "' already exists - dataset files will not be copied.");
                return Status.createError(ALREADY_EXIST_MSG);
            }
            Status status =
                    getCopier().copyToRemote(originalData, destination, host, rsyncModule,
                            rsyncPasswordFile);
            if (status.isError())
            {
                operationLog.error("Could not copy data set " + dataSetInformation.getDataSetCode()
                        + " to destination folder '" + destination + "'"
                        + (host != null ? " on host '" + host + "'" : "")
                        + (rsyncModule != null ? " for rsync module '" + rsyncModule + "'" : "")
                        + ": " + status.tryGetErrorMessage());
                return Status.createError(COPYING_FAILED_MSG);
            }
            return status;
        }

        private boolean checkDestinationExists()
        {
            if (host != null)
            {
                // TODO 2010-01-29, Piotr Buczek: check if directory already exists
                // check remotely using ssh
                return false;
            } else
            {
                // check locally
                return destination.exists();
            }
        }

        public void undoLastOperation()
        {
        }

        private File getExecutable(String executable)
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

    public DataSetCopier(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, new RsyncCopierFactory());
    }

    @Private
    DataSetCopier(Properties properties, File storeRoot, IPathCopierFactory factory)
    {
        super(properties, storeRoot, new Copier(properties, factory));
    }

}
