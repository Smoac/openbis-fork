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
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;

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

    @Private
    static final String ALREADY_EXIST_MSG = "already exist";

    @Private
    static final String COPYING_FAILED_MSG = "copying failed";

    static final String RSYNC_EXEC = "rsync";

    static final String SSH_EXEC = "ssh";

    @Private
    static final long SSH_TIMEOUT_MILLIS = 15 * 1000; // 15s

    public DataSetCopier(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, new RsyncCopierFactory(), new SshCommandExecutorFactory());
    }

    @Private DataSetCopier(Properties properties, File storeRoot, IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory)
    {
        super(properties, storeRoot, new Copier(properties, pathCopierFactory,
                sshCommandExecutorFactory));
    }

    public DataSetCopier(Properties properties, File storeRoot,
            IPostRegistrationDatasetHandler dropboxHandler)
    {
        super(properties, storeRoot, dropboxHandler);
    }

}
