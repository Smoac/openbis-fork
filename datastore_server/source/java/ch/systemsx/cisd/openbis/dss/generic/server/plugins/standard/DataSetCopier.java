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

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.rsync.RsyncCopier;
import ch.systemsx.cisd.common.highwatermark.HostAwareFile;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetCopier extends AbstractDropboxProcessingPlugin
{
    private static final long serialVersionUID = 1L;

    private static final String RSYNC_EXEC = "rsync";

    private static final String SSH_EXEC = "ssh";

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

        public Copier(Properties properties)
        {
            this.properties = properties;
            rsyncPasswordFile = properties.getProperty("rsync-password-file");
            rsyncExecutable = getExecutable(RSYNC_EXEC);
            sshExecutable = getExecutable(SSH_EXEC);
            HostAwareFile hostAwareFile = HostAwareFileWithHighwaterMark.fromProperties(properties, "destination");
            host = hostAwareFile.tryGetHost();
            rsyncModule = hostAwareFile.tryGetRsyncModule();
            destination = hostAwareFile.getFile();
            getCopier();
        }

        private IPathCopier getCopier()
        {
            if (copier == null)
            {
                copier = new RsyncCopier(rsyncExecutable, sshExecutable, false, false);
                copier.check();
                if (host != null)
                {
                    FileUtilities.checkPathCopier(copier, host, null, rsyncModule, rsyncPasswordFile);
                }
            }
            return copier;
        }

        public void handle(File originalData, DataSetInformation dataSetInformation)
        {
            getCopier().copyToRemote(originalData, destination, host,
                    rsyncModule, rsyncPasswordFile);
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
        super(properties, storeRoot, new Copier(properties));
    }

    public String getDescription()
    {
        return "Copy data sets";
    }

}
