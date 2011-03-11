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

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.ARCHIVED;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.AVAILABLE;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.QueueingDataSetStatusUpdaterService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetCodesWithStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * The base class for archiving.
 * 
 * @author Piotr Buczek
 * @author Kaloyan Enimanev
 */
public abstract class AbstractArchiverProcessingPlugin extends AbstractDatastorePlugin implements
        IArchiverPlugin
{

    private static final long serialVersionUID = 1L;

    private final IStatusChecker archivePrerequisiteOrNull;

    private final IStatusChecker unarchivePrerequisiteOrNull;

    public AbstractArchiverProcessingPlugin(Properties properties, File storeRoot,
            IStatusChecker archivePrerequisiteOrNull, IStatusChecker unarchivePrerequisiteOrNull)
    {
        super(properties, storeRoot);
        this.archivePrerequisiteOrNull = archivePrerequisiteOrNull;
        this.unarchivePrerequisiteOrNull = unarchivePrerequisiteOrNull;
    }

    /**
     * NOTE: this method is not allowed to throw exception as this will leave data sets in the
     * openBIS database with an inconsistent status.
     */
    abstract protected DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context);

    /**
     * NOTE: this method is not allowed to throw exception as this will leave data sets in the
     * openBIS database with an inconsistent status.
     */
    abstract protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context);
    
    /**
     * NOTE: this method is not allowed to throw exception as this will leave data sets in the
     * openBIS database with an inconsistent status.
     */
    abstract protected DatasetProcessingStatuses doDeleteFromArchive(
            List<DatasetDescription> datasets, ArchiverTaskContext context);
    
    /**
     * @return <code>true</code> if the dataset is present in the archive, <code>false</code>
     *         otherwise.
     */
    abstract protected BooleanStatus isDataSetPresentInArchive(DatasetDescription dataset,
            ArchiverTaskContext context);

    public ProcessingStatus archive(List<DatasetDescription> datasets,
            final ArchiverTaskContext context, boolean removeFromDataStore)
    {
        operationLog.info("Archiving of the following datasets has been requested: "
                + CollectionUtils.abbreviate(datasets, 10));

        Status prerequisiteStatus = checkUnarchivePrerequisite(datasets);
        DatasetProcessingStatuses statuses = null;
        if (prerequisiteStatus.isError())
        {
            statuses = createStatuses(prerequisiteStatus, datasets, Operation.ARCHIVE);
        } else
        {
            statuses = archiveInternal(datasets, context, removeFromDataStore);
        }

        DataSetArchivingStatus successStatus = (removeFromDataStore) ? ARCHIVED : AVAILABLE;

        asyncUpdateStatuses(statuses.getSuccessfulDatasetCodes(), successStatus, true);
        asyncUpdateStatuses(statuses.getFailedDatasetCodes(), AVAILABLE, false);

        return statuses.getProcessingStatus();
    }

    private DatasetProcessingStatuses archiveInternal(List<DatasetDescription> datasets,
            final ArchiverTaskContext context, boolean removeFromDataStore)
    {

        GroupedDatasets groupedDatasets = groupByPresenceInArchive(datasets, context);
        List<DatasetDescription> notPresentInArchive = groupedDatasets.getNotPresentAsList();
        if (notPresentInArchive.isEmpty() == false)
        {
            // copy data sets in the archive
            // TODO KE: try to keep the error messages from the returned statuses
            doArchive(notPresentInArchive, context);

            // paranoid check to make sure everything really got archived
            groupedDatasets = groupByPresenceInArchive(datasets, context);
        }

        if (removeFromDataStore)
        {
            // only remove the when we are sure we have got a backup in the archive
            removeFromDataStore(groupedDatasets.getPresentInArchive(), context);
        }
        
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        statuses.addResult(groupedDatasets.getPresentInArchive(), Status.OK, Operation.ARCHIVE);
        statuses.addResult(groupedDatasets.getNotPresentInArchive().keySet(), Status.createError(),
                Operation.ARCHIVE);

        return statuses;
    }

    protected void removeFromDataStore(List<DatasetDescription> datasets,
            ArchiverTaskContext context)
    {
        // the deletion will happen at a later point in time
        ServiceProvider.getDataSetDeleter().scheduleDeletionOfDataSets(datasets);
    }

    public ProcessingStatus unarchive(List<DatasetDescription> datasets,
            final ArchiverTaskContext context)
    {
        operationLog.info("Unarchiving of the following datasets has been requested: "
                + CollectionUtils.abbreviate(datasets, 10));

        Status prerequisiteStatus = checkUnarchivePrerequisite(datasets);
        DatasetProcessingStatuses statuses = null;
        if (prerequisiteStatus.isError())
        {
            statuses = createStatuses(prerequisiteStatus, datasets, Operation.UNARCHIVE);
        } else
        {
            statuses = doUnarchive(datasets, context);
        }

        asyncUpdateStatuses(statuses.getSuccessfulDatasetCodes(), AVAILABLE, true);
        asyncUpdateStatuses(statuses.getFailedDatasetCodes(), ARCHIVED, true);

        return statuses.getProcessingStatus();
    }

    public ProcessingStatus deleteFromArchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context)
    {
        DatasetProcessingStatuses status = doDeleteFromArchive(datasets, context);
        return status != null ? status.getProcessingStatus() : null;
    }

    protected final Status checkUnarchivePrerequisite(List<DatasetDescription> datasets)
    {
        if (unarchivePrerequisiteOrNull != null)
        {
            return unarchivePrerequisiteOrNull.check(datasets.size());
        } else
        {
            return Status.OK;
        }
    }

    protected final Status checkArchivePrerequisite(List<DatasetDescription> datasets)
    {
        if (archivePrerequisiteOrNull != null)
        {
            return archivePrerequisiteOrNull.check(datasets.size());
        } else
        {
            return Status.OK;
        }
    }

    protected static enum Operation
    {
        ARCHIVE("Archiving"), UNARCHIVE("Unarchiving"),
        DELETE_FROM_ARCHIVE("Deleting from archive");

        private final String description;

        Operation(String description)
        {
            this.description = description;
        }

        public String getDescription()
        {
            return description;
        }
    }

    protected static class DatasetProcessingStatuses
    {
        private final List<String> successfulDatasetCodes;

        private final List<String> failedDatasetCodes;

        private final ProcessingStatus processingStatus;

        public DatasetProcessingStatuses()
        {
            this.successfulDatasetCodes = new ArrayList<String>();
            this.failedDatasetCodes = new ArrayList<String>();
            this.processingStatus = new ProcessingStatus();
        }

        public void addResult(Collection<DatasetDescription> datasets, Status status,
                Operation operation)
        {
            for (DatasetDescription dataset : datasets)
            {
                addResult(dataset.getDatasetCode(), status, operation.getDescription());
            }
        }

        public void addResult(String datasetCode, Status status, Operation operation)
        {
            addResult(datasetCode, status, operation.getDescription());
        }

        public void addResult(String datasetCode, Status status, String operationDescription)
        {
            String logMessage = createLogMessage(datasetCode, status, operationDescription);
            if (status.isError())
            {
                operationLog.error(logMessage);
                failedDatasetCodes.add(datasetCode);
            } else
            {
                operationLog.debug(logMessage);
                successfulDatasetCodes.add(datasetCode);
            }
            processingStatus.addDatasetStatus(datasetCode, status);
        }

        private String createLogMessage(String datasetCode, Status status, String operation)
        {
            return String.format("%s for dataset %s finished with the status: %s.", operation,
                    datasetCode, status);
        }

        public List<String> getSuccessfulDatasetCodes()
        {
            return successfulDatasetCodes;
        }

        public List<String> getFailedDatasetCodes()
        {
            return failedDatasetCodes;
        }

        public ProcessingStatus getProcessingStatus()
        {
            return processingStatus;
        }
    }

    protected final static DatasetProcessingStatuses createStatuses(Status status,
            List<DatasetDescription> datasets, Operation operation)
    {
        return createStatuses(status, datasets, operation.getDescription());
    }

    // creates the same status for all datasets
    protected final static DatasetProcessingStatuses createStatuses(Status status,
            List<DatasetDescription> datasets, String operationDescription)
    {
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (DatasetDescription dataset : datasets)
        {
            statuses.addResult(dataset.getDatasetCode(), status, operationDescription);
        }
        return statuses;
    }

    protected List<String> extractCodes(List<DatasetDescription> dataSets)
    {
        List<String> result = new ArrayList<String>();
        for (DatasetDescription description : dataSets)
        {
            result.add(description.getDatasetCode());
        }
        return result;
    }

    private static void asyncUpdateStatuses(List<String> dataSetCodes,
            DataSetArchivingStatus newStatus, boolean presentInArchive)
    {
        QueueingDataSetStatusUpdaterService.update(new DataSetCodesWithStatus(dataSetCodes,
                newStatus, presentInArchive));
    }

    protected static class GroupedDatasets
    {
        private List<DatasetDescription> presentInArchive;

        private Map<DatasetDescription, BooleanStatus> notPresentInArchive;

        GroupedDatasets(List<DatasetDescription> presentInArchive,
                Map<DatasetDescription, BooleanStatus> notPresentInArchive)
        {
            this.presentInArchive = presentInArchive;
            this.notPresentInArchive = notPresentInArchive;
        }

        public List<DatasetDescription> getPresentInArchive()
        {
            return presentInArchive;
        }

        public Map<DatasetDescription, BooleanStatus> getNotPresentInArchive()
        {
            return notPresentInArchive;
        }

        public List<DatasetDescription> getNotPresentAsList()
        {

            return new ArrayList<DatasetDescription>(notPresentInArchive.keySet());
        }
    }

    protected GroupedDatasets groupByPresenceInArchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context)
    {
        List<DatasetDescription> present = new ArrayList<DatasetDescription>();
        Map<DatasetDescription, BooleanStatus> notPresent = new HashMap<DatasetDescription, BooleanStatus>();
        
        for (DatasetDescription dataset : datasets) {
            BooleanStatus presentStatus = isDataSetPresentInArchive(dataset, context);
            if (presentStatus.isSuccess())
            {
                present.add(dataset);
            } else {
                notPresent.put(dataset, presentStatus);
            }
        }
        
        return new GroupedDatasets(present, notPresent);
    }


}
