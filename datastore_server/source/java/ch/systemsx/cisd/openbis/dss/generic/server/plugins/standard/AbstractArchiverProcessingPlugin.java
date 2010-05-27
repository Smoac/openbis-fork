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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IArchiverTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.QueueingDataSetStatusUpdaterService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetCodesWithStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * The base class for archiving.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractArchiverProcessingPlugin extends AbstractDatastorePlugin implements
        IArchiverTask
{

    protected static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractArchiverProcessingPlugin.class);

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

    abstract protected DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets)
            throws UserFailureException;

    abstract protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets)
            throws UserFailureException;

    public ProcessingStatus archive(List<DatasetDescription> datasets)
    {
        operationLog.info("Archiving of the following datasets has been requested: "
                + CollectionUtils.abbreviate(datasets, 10));
        return handleDatasets(datasets, DataSetArchivingStatus.ARCHIVED,
                DataSetArchivingStatus.AVAILABLE, new IDatasetDescriptionHandler()
                    {
                        public DatasetProcessingStatuses handle(List<DatasetDescription> allDatasets)
                        {
                            Status prerequisiteStatus = checkArchivePrerequisite(allDatasets);
                            if (prerequisiteStatus.isError())
                            {
                                return createStatusesFrom(prerequisiteStatus, allDatasets, true);
                            } else
                            {
                                return doArchive(allDatasets);
                            }
                        }
                    });
    }

    public ProcessingStatus unarchive(List<DatasetDescription> datasets)
    {
        operationLog.info("Unarchiving of the following datasets has been requested: "
                + CollectionUtils.abbreviate(datasets, 10));
        return handleDatasets(datasets, DataSetArchivingStatus.AVAILABLE,
                DataSetArchivingStatus.ARCHIVED, new IDatasetDescriptionHandler()
                    {
                        public DatasetProcessingStatuses handle(List<DatasetDescription> allDatasets)
                        {
                            Status prerequisiteStatus = checkUnarchivePrerequisite(allDatasets);
                            if (prerequisiteStatus.isError())
                            {
                                return createStatusesFrom(prerequisiteStatus, allDatasets, false);
                            } else
                            {
                                return doUnarchive(allDatasets);
                            }
                        }
                    });
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

        public void addResult(String datasetCode, Status status, boolean isArchiving)
        {
            String logMessage = createLogMessage(datasetCode, status, isArchiving);
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

        private String createLogMessage(String datasetCode, Status status, boolean isArchiving)
        {
            String operationDescription = isArchiving ? "Archiving" : "Unarchiving";
            return String.format("%s for dataset %s finished with the status: %s.",
                    operationDescription, datasetCode, status);
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

    private ProcessingStatus handleDatasets(List<DatasetDescription> datasets,
            DataSetArchivingStatus success, DataSetArchivingStatus failure,
            IDatasetDescriptionHandler handler)
    {
        DatasetProcessingStatuses statuses = handler.handle(datasets);
        asyncUpdateStatuses(statuses, success, failure);
        return statuses.getProcessingStatus();
    }

    // creates the same status for all datasets
    protected final static DatasetProcessingStatuses createStatusesFrom(Status status,
            List<DatasetDescription> datasets, boolean isArchiving)
    {
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        for (DatasetDescription dataset : datasets)
        {
            statuses.addResult(dataset.getDatasetCode(), status, isArchiving);
        }
        return statuses;
    }

    private void asyncUpdateStatuses(DatasetProcessingStatuses statuses,
            DataSetArchivingStatus success, DataSetArchivingStatus failure)
    {
        asyncUpdateStatuses(statuses.getSuccessfulDatasetCodes(), success);
        asyncUpdateStatuses(statuses.getFailedDatasetCodes(), failure);

    }

    private static void asyncUpdateStatuses(List<String> dataSetCodes,
            DataSetArchivingStatus newStatus)
    {
        QueueingDataSetStatusUpdaterService.update(new DataSetCodesWithStatus(dataSetCodes,
                newStatus));
    }

    private interface IDatasetDescriptionHandler
    {
        public DatasetProcessingStatuses handle(List<DatasetDescription> datasets);
    }

}
