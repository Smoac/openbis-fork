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
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
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

    private transient IDataSetFileOperationsManager fileOperationsManager;

    public RsyncArchiver(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, new DataSetFileOperationsManager(properties,
                new RsyncArchiveCopierFactory(), new SshCommandExecutorFactory()));
    }

    @Private
    RsyncArchiver(Properties properties, File storeRoot,
            IDataSetFileOperationsManager fileOperationsManager)
    {
        super(properties, storeRoot, null, null);
        this.fileOperationsManager = fileOperationsManager;
    }

    @Override
    protected DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context) throws UserFailureException
    {
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (DatasetDescription dataset : datasets)
        {
            File originalData = getDatasetDirectory(context, dataset);
            Status status = doArchive(dataset, originalData);
            statuses.addResult(dataset.getDataSetCode(), status, Operation.ARCHIVE);
        }

        return statuses;
    }

    @Override
    protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context) throws UserFailureException
    {
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (DatasetDescription dataset : datasets)
        {
            context.getUnarchivingPreparation().prepareForUnarchiving(dataset);
            File originalData = getDatasetDirectory(context, dataset);
            Status status = doUnarchive(dataset, originalData);
            statuses.addResult(dataset.getDataSetCode(), status, Operation.UNARCHIVE);
        }

        return statuses;
    }

    @Override
    protected DatasetProcessingStatuses doDeleteFromArchive(List<DatasetLocation> datasets)
    {
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (DatasetLocation dataset : datasets)
        {
            Status status = doDeleteFromArchive(dataset);
            statuses.addResult(dataset.getDataSetCode(), status, Operation.DELETE_FROM_ARCHIVE);
        }

        return statuses;
    }

    @Override
    protected BooleanStatus isDataSetSynchronizedWithArchive(DatasetDescription dataset,
            ArchiverTaskContext context)
    {
        File originalData = getDatasetDirectory(context, dataset);
        return fileOperationsManager.isSynchronizedWithDestination(originalData, dataset);
    }

    @Override
    protected BooleanStatus isDataSetPresentInArchive(DatasetDescription dataset)
    {
        return fileOperationsManager.isPresentInDestination(dataset);
    }

    private Status doArchive(DatasetDescription dataset, File originalData)
    {
        return fileOperationsManager.copyToDestination(originalData, dataset);
    }

    private Status doUnarchive(DatasetDescription dataset, File originalData)
    {
        return fileOperationsManager.retrieveFromDestination(originalData, dataset);
    }

    private Status doDeleteFromArchive(DatasetLocation dataset)
    {
        return fileOperationsManager.deleteFromDestination(dataset);
    }

    private File getDatasetDirectory(ArchiverTaskContext context, DatasetDescription dataset)
    {
        return context.getDirectoryProvider().getDataSetDirectory(dataset);
    }

}
