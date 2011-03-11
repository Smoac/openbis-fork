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
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.openbis.dss.generic.server.IDataSetCommandExecutor;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Archiver plugin which copies data sets to a destination folder using rsync (if it is remote). The
 * destination can be
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

    public RsyncArchiver(Properties properties, File storeRoot,
            IDataSetCommandExecutor commandExecutor)
    {
        this(properties, storeRoot, commandExecutor, new RsyncCopierFactory(),
                new SshCommandExecutorFactory());
    }

    @Private
    RsyncArchiver(Properties properties, File storeRoot, IDataSetCommandExecutor commandExecutor,
            IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory)
    {
        super(properties, storeRoot, commandExecutor, null, null);
        this.pathCopierFactory = pathCopierFactory;
        this.sshCommandExecutorFactory = sshCommandExecutorFactory;
    }

    @Override
    protected DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context) throws UserFailureException
    {
        initIfNecessary();

        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (DatasetDescription dataset : datasets)
        {
            File originalData = getDatasetDirectory(context, dataset);
            Status status = doArchive(dataset, originalData);
            statuses.addResult(dataset.getDatasetCode(), status, Operation.ARCHIVE);
        }

        return statuses;
    }

    @Override
    protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context) throws UserFailureException
    {
        initIfNecessary();

        // no need to lock - this is processing task
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (DatasetDescription dataset : datasets)
        {
            File originalData = getDatasetDirectory(context, dataset);
            Status status = doUnarchive(dataset, originalData);
            statuses.addResult(dataset.getDatasetCode(), status, Operation.UNARCHIVE);
        }

        return statuses;
    }

    @Override
    protected DatasetProcessingStatuses doDeleteFromArchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context)
    {
        initIfNecessary();

        // no need to lock - this is processing task
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (DatasetDescription dataset : datasets)
        {
            Status status = doDeleteFromArchive(dataset);
            statuses.addResult(dataset.getDatasetCode(), status, Operation.DELETE_FROM_ARCHIVE);
        }

        return statuses;
    }

    @Override
    protected BooleanStatus isDataSetPresentInArchive(DatasetDescription dataset,
            ArchiverTaskContext context)
    {
        initIfNecessary();

        File originalData = getDatasetDirectory(context, dataset);
        return copier.isPresentInDestination(originalData, dataset);
    }

    private void initIfNecessary()
    {
        if (copier == null)
        {
            this.copier =
                    new RsyncDataSetCopier(properties, pathCopierFactory, sshCommandExecutorFactory);
        }
    }

    private Status doArchive(DatasetDescription dataset, File originalData)
    {
        return copier.copyToDestination(originalData, dataset);
    }

    private Status doUnarchive(DatasetDescription dataset, File originalData)
    {
        return copier.retrieveFromDestination(originalData, dataset);
    }

    private Status doDeleteFromArchive(DatasetDescription dataset)
    {
        return copier.deleteFromDestination(dataset);
    }

    private File getDatasetDirectory(ArchiverTaskContext context, DatasetDescription dataset)
    {
        return context.getDirectoryProvider().getDataSetDirectory(dataset);
    }
}
