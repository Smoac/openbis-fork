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
import java.util.List;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Archiver plugin which copies data sets to a destination folder using rsync (if it is remote). 
 * The destination can be
 * <ul>
 * <li>on the local file system,
 * <li>a mounted remote folder,
 * <li>a remote folder accessible via SSH,
 * <li>a remote folder accessible via an rsync server.
 * </ul>
 * 
 * @author Piotr Buczek
 */
public class RsyncArchiver extends AbstractArchiverProcessingPlugin
{
    private static final long serialVersionUID = 1L;

    private transient RsyncDataSetCopier copier;

    private final IPathCopierFactory pathCopierFactory;

    private final ISshCommandExecutorFactory sshCommandExecutorFactory;

    public RsyncArchiver(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, new RsyncCopierFactory(), new SshCommandExecutorFactory());
    }

    @Private
    RsyncArchiver(Properties properties, File storeRoot, IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory)
    {
        super(properties, storeRoot, null, null);
        this.pathCopierFactory = pathCopierFactory;
        this.sshCommandExecutorFactory = sshCommandExecutorFactory;
    }

    private boolean isInitialized()
    {
        return copier != null;
    }

    private void init()
    {
        this.copier =
                new RsyncDataSetCopier(properties, pathCopierFactory, sshCommandExecutorFactory);
    }

    private Status doArchive(DatasetDescription dataset, File originalData)
    {
        return copier.copyToDestination(originalData, dataset);
    }

    private Status doUnarchive(DatasetDescription dataset, File originalData)
    {
        return copier.retrieveFromDestination(originalData, dataset);
    }

    @Override
    protected DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context) throws UserFailureException
    {
        if (isInitialized() == false)
        {
            init();
        }
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (DatasetDescription dataset : datasets)
        {
            File originalData = context.getDirectoryProvider().getDataSetDirectory(dataset);
            Status status = doArchive(dataset, originalData);
            statuses.addResult(dataset.getDatasetCode(), status, true);
        }

        return statuses;
    }

    @Override
    protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context) throws UserFailureException
    {
        if (isInitialized() == false)
        {
            init();
        }

        // no need to lock - this is processing task
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (DatasetDescription dataset : datasets)
        {
            File originalData = context.getDirectoryProvider().getDataSetDirectory(dataset);
            Status status = doUnarchive(dataset, originalData);
            statuses.addResult(dataset.getDatasetCode(), status, false);
        }

        return createStatusesFrom(Status.OK, datasets, false);
    }

    protected boolean isProperlyArchived() throws UserFailureException
    {
        return true;
    }

    protected void doDeleteFromArchive() throws UserFailureException
    {

    }

    protected void doDeleteFromDss() throws UserFailureException
    {

    }

}
