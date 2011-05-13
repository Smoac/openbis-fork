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
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.SimpleFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.utilities.ClassUtils;
import ch.systemsx.cisd.common.utilities.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDeleter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetStatusUpdater;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareFinder;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IUnarchivingPreparation;
import ch.systemsx.cisd.openbis.dss.generic.shared.IncomingShareIdProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.QueueingDataSetStatusUpdaterService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.SimpleShareFinder;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetCodesWithStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.translator.SimpleDataSetHelper;

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

    @Private
    public static final String SHARE_FINDER_KEY = "share-finder";

    private static final String SYNCHRONIZE_ARCHIVE = "synchronize-archive";

    private final IStatusChecker archivePrerequisiteOrNull;

    private final IStatusChecker unarchivePrerequisiteOrNull;

    private final boolean synchronizeArchive;

    private transient IShareIdManager shareIdManager;

    private transient IEncapsulatedOpenBISService service;

    @Private
    transient IDataSetStatusUpdater statusUpdater;

    public AbstractArchiverProcessingPlugin(Properties properties, File storeRoot,
            IStatusChecker archivePrerequisiteOrNull, IStatusChecker unarchivePrerequisiteOrNull)
    {
        super(properties, storeRoot);
        this.archivePrerequisiteOrNull = archivePrerequisiteOrNull;
        this.unarchivePrerequisiteOrNull = unarchivePrerequisiteOrNull;
        this.synchronizeArchive = PropertyUtils.getBoolean(properties, SYNCHRONIZE_ARCHIVE, true);
    }

    /**
     * NOTE: this method is not allowed to throw exception as this will leave data sets in the
     * openBIS database with an inconsistent status.
     */
    abstract protected DatasetProcessingStatuses doArchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context);

    /**
     * NOTE: this method is not allowed to throw exception as this will leave data sets in the
     * openBIS database with an inconsistent status. Implementations of this method should invoke
     * <code>context.getUnarchivingPreparation().prepareForUnarchiving()</code> for each data set
     * before doing the actual unarchiving.
     */
    abstract protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets,
            ArchiverTaskContext context);

    /**
     * deletes data sets from archive. At the time when this method is invoked the data sets do not
     * exist in the openBIS database.
     */
    abstract protected DatasetProcessingStatuses doDeleteFromArchive(List<DatasetLocation> datasets);

    /**
     * @return <code>true</code> if the dataset is present and synchronized with the archive,
     *         <code>false</code> otherwise.
     */
    abstract protected BooleanStatus isDataSetSynchronizedWithArchive(DatasetDescription dataset,
            ArchiverTaskContext context);

    /**
     * @return <code>true</code> if the dataset is present in the archive, <code>false</code>
     *         otherwise.
     */
    abstract protected BooleanStatus isDataSetPresentInArchive(DatasetDescription dataset);

    public ProcessingStatus archive(List<DatasetDescription> datasets,
            final ArchiverTaskContext context, boolean removeFromDataStore)
    {
        operationLog.info("Archiving of the following datasets has been requested: "
                + CollectionUtils.abbreviate(datasets, 10));

        DatasetProcessingStatuses statuses = safeArchive(datasets, context, removeFromDataStore);

        DataSetArchivingStatus successStatus = (removeFromDataStore) ? ARCHIVED : AVAILABLE;

        asyncUpdateStatuses(statuses.getSuccessfulDatasetCodes(), successStatus, true);
        asyncUpdateStatuses(statuses.getFailedDatasetCodes(), AVAILABLE, false);

        return statuses.getProcessingStatus();
    }

    private void initializeDatasetSizesIfNeeded(List<DatasetDescription> datasets)
    {
        for (DatasetDescription dataset : datasets)
        {
            if (dataset.getDataSetSize() == null)
            {
                String dataSetCode = dataset.getDataSetCode();
                String shareId = getShareIdManager().getShareId(dataSetCode);
                File shareFolder = new File(storeRoot, shareId);
                String dataSetLocation = dataset.getDataSetLocation();
                long size = FileUtils.sizeOfDirectory(new File(shareFolder, dataSetLocation));
                getService().updateShareIdAndSize(dataSetCode, shareId, size);
            }
        }
    }

    /**
     * a 'safe' method that does not throw any exceptions.
     */
    private DatasetProcessingStatuses safeArchive(List<DatasetDescription> datasets,
            final ArchiverTaskContext context, boolean removeFromDataStore)
    {
        Status prerequisiteStatus = checkUnarchivePrerequisite(datasets);
        DatasetProcessingStatuses statuses = null;
        if (prerequisiteStatus.isError())
        {
            statuses = createStatuses(prerequisiteStatus, datasets, Operation.ARCHIVE);
        } else
        {
            try
            {
                initializeDatasetSizesIfNeeded(datasets);
                statuses = unsafeArchive(datasets, context, removeFromDataStore);
            } catch (Throwable t)
            {
                String errorMessage = "Archiving failed :" + t.getMessage();
                operationLog.error(errorMessage, t);
                Status errorStatus = Status.createError(errorMessage);
                statuses = createStatuses(errorStatus, datasets, Operation.ARCHIVE);
            }
        }
        return statuses;
    }

    /**
     * this method does not handle any exceptions coming from the archiver implementation, hence it
     * is 'unsafe'.
     */
    private DatasetProcessingStatuses unsafeArchive(List<DatasetDescription> datasets,
            final ArchiverTaskContext context, boolean removeFromDataStore)
    {

        GroupedDatasets groupedDatasets =
                groupByArchiveDifferencies(datasets, context, synchronizeArchive);

        List<DatasetDescription> toBeArchived = groupedDatasets.getDifferenciesAsList();
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();
        if (toBeArchived.isEmpty() == false)
        {
            // copy data sets in the archive
            statuses = doArchive(toBeArchived, context);

            // paranoid check to make sure everything really got archived
            groupedDatasets = groupByArchiveDifferencies(datasets, context, true);
        }

        if (removeFromDataStore)
        {
            // only remove the when we are sure we have got a backup in the archive
            removeFromDataStore(groupedDatasets.getUpToDateInArchive(), context);
        }

        // merge the archiver statuses with the paranoid check results
        return mergeArchiveStatuses(statuses, groupedDatasets);
    }

    private DatasetProcessingStatuses mergeArchiveStatuses(DatasetProcessingStatuses statuses,
            GroupedDatasets groupedDatasets)
    {
        DatasetProcessingStatuses result = new DatasetProcessingStatuses();
        for (DatasetDescription dataset : groupedDatasets.getUpToDateInArchive())
        {
            String dataSetCode = dataset.getDataSetCode();
            Status status = getStatusForDataset(statuses, dataSetCode, Status.OK);
            result.addResult(dataSetCode, status, Operation.ARCHIVE);
        }
        for (DatasetDescription dataset : groupedDatasets.getDifferenciesAsList())
        {
            String dataSetCode = dataset.getDataSetCode();
            BooleanStatus booleanStatus = groupedDatasets.getDifferencyArchiveStatus(dataset);
            String errorMessage =
                    (booleanStatus.tryGetMessage() != null) ? booleanStatus.tryGetMessage() : "";
            Status status =
                    getStatusForDataset(statuses, dataSetCode, Status.createError(errorMessage));
            result.addResult(dataSetCode, status, Operation.ARCHIVE);
        }

        return result;
    }

    private Status getStatusForDataset(DatasetProcessingStatuses statuses, String dataSetCode,
            Status defaultStatus)
    {
        Status status = statuses.getProcessingStatus().tryGetStatusByDataset(dataSetCode);
        if (status == null)
        {
            status = defaultStatus;
        } else if (status.isError() != defaultStatus.isError())
        {
            // the status returned from the archiver is actually incorrect !
            // our paranoic check showed that the dataset was in fact *not* present in archive
            status = defaultStatus;
        }
        return status;
    }

    protected void removeFromDataStore(List<DatasetDescription> datasets,
            ArchiverTaskContext context)
    {
        // the deletion will happen at a later point in time
        IDataSetDeleter dataSetDeleter = ServiceProvider.getDataStoreService().getDataSetDeleter();
        dataSetDeleter.scheduleDeletionOfDataSets(datasets);
    }

    public ProcessingStatus unarchive(List<DatasetDescription> datasets, ArchiverTaskContext context)
    {
        operationLog.info("Unarchiving of the following datasets has been requested: "
                + CollectionUtils.abbreviate(datasets, 10));
        DatasetProcessingStatuses statuses = safeUnarchive(datasets, context);

        asyncUpdateStatuses(statuses.getSuccessfulDatasetCodes(), AVAILABLE, true);
        asyncUpdateStatuses(statuses.getFailedDatasetCodes(), ARCHIVED, true);

        return statuses.getProcessingStatus();
    }

    private void setUpUnarchivingPreparation(ArchiverTaskContext context)
    {
        String dataStoreCode = ServiceProvider.getConfigProvider().getDataStoreCode();
        Set<String> incomingShares = IncomingShareIdProvider.getIdsOfIncomingShares();
        IFreeSpaceProvider freeSpaceProvider = new SimpleFreeSpaceProvider();
        List<Share> shares =
                SegmentedStoreUtils.getDataSetsPerShare(storeRoot, dataStoreCode, incomingShares,
                        freeSpaceProvider, getService(), new Log4jSimpleLogger(operationLog));
        context.setUnarchivingPreparation(new UnarchivingPreparation(getShareFinder(),
                getShareIdManager(), getService(), shares));
    }

    /**
     * a 'safe' method that never throws any exceptions.
     */
    private DatasetProcessingStatuses safeUnarchive(List<DatasetDescription> datasets,
            final ArchiverTaskContext context)
    {
        Status prerequisiteStatus = checkUnarchivePrerequisite(datasets);
        DatasetProcessingStatuses statuses = null;
        if (prerequisiteStatus.isError())
        {
            statuses = createStatuses(prerequisiteStatus, datasets, Operation.UNARCHIVE);
        } else
        {
            try
            {
                setUpUnarchivingPreparation(context);
                statuses = doUnarchive(datasets, context);
            } catch (Throwable t)
            {
                String errorMessage = "Unarchiving failed: " + t.getMessage();
                operationLog.error(errorMessage, t);
                Status errorStatus = Status.createError(errorMessage);
                statuses = createStatuses(errorStatus, datasets, Operation.UNARCHIVE);
            }
        }
        return statuses;
    }

    public ProcessingStatus deleteFromArchive(List<DatasetLocation> datasets)
    {
        DatasetProcessingStatuses status = doDeleteFromArchive(datasets);
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
                addResult(dataset.getDataSetCode(), status, operation.getDescription());
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
            statuses.addResult(dataset.getDataSetCode(), status, operationDescription);
        }
        return statuses;
    }

    private void asyncUpdateStatuses(List<String> dataSetCodes, DataSetArchivingStatus newStatus,
            boolean presentInArchive)
    {
        if (statusUpdater == null)
        {
            statusUpdater = new IDataSetStatusUpdater()
                {
                    public void update(List<String> codes, DataSetArchivingStatus status,
                            boolean present)
                    {
                        QueueingDataSetStatusUpdaterService.update(new DataSetCodesWithStatus(
                                codes, status, present));
                    }
                };
        }
        statusUpdater.update(dataSetCodes, newStatus, presentInArchive);
    }

    protected static class GroupedDatasets
    {
        private List<DatasetDescription> upToDateInArchive;

        private Map<DatasetDescription, BooleanStatus> differentInArchive;

        GroupedDatasets(List<DatasetDescription> upToDateInArchive,
                Map<DatasetDescription, BooleanStatus> differentInArchive)
        {
            this.upToDateInArchive = upToDateInArchive;
            this.differentInArchive = differentInArchive;
        }

        public List<DatasetDescription> getUpToDateInArchive()
        {
            return upToDateInArchive;
        }

        public Map<DatasetDescription, BooleanStatus> getDifferentInArchive()
        {
            return differentInArchive;
        }

        public List<DatasetDescription> getDifferenciesAsList()
        {

            return new ArrayList<DatasetDescription>(differentInArchive.keySet());
        }

        public BooleanStatus getDifferencyArchiveStatus(DatasetDescription description)
        {
            return differentInArchive.get(description);
        }
    }

    private GroupedDatasets groupByArchiveDifferencies(List<DatasetDescription> datasets,
            ArchiverTaskContext context, boolean checkIfSynchronized)
    {
        List<DatasetDescription> upToDateInArchive = new ArrayList<DatasetDescription>();
        Map<DatasetDescription, BooleanStatus> differentInArchive =
                new HashMap<DatasetDescription, BooleanStatus>();

        for (DatasetDescription dataset : datasets)
        {
            BooleanStatus upToDateStatus =
                    checkIfSynchronized ? isDataSetSynchronizedWithArchive(dataset, context)
                            : isDataSetPresentInArchive(dataset);
            if (upToDateStatus.isSuccess())
            {
                upToDateInArchive.add(dataset);
            } else
            {
                differentInArchive.put(dataset, upToDateStatus);
            }
        }

        return new GroupedDatasets(upToDateInArchive, differentInArchive);
    }

    private IShareIdManager getShareIdManager()
    {
        if (shareIdManager == null)
        {
            shareIdManager = ServiceProvider.getShareIdManager();
        }
        return shareIdManager;
    }

    private IShareFinder getShareFinder()
    {
        Properties props =
                PropertyParametersUtil.extractSingleSectionProperties(properties, SHARE_FINDER_KEY,
                        false).getProperties();

        String className = props.getProperty("class");
        if (StringUtils.isEmpty(className))
        {
            // use simple share finder by default when no share finder is configured
            className = SimpleShareFinder.class.getName();
        }

        IShareFinder shareFinder = ClassUtils.create(IShareFinder.class, className, props);

        return shareFinder;
    }

    private IEncapsulatedOpenBISService getService()
    {
        if (service == null)
        {
            service = ServiceProvider.getOpenBISService();
        }
        return service;
    }

    /**
     * @author Franz-Josef Elmer
     */
    private static final class UnarchivingPreparation implements IUnarchivingPreparation
    {
        private final IShareFinder shareFinder;

        private final IEncapsulatedOpenBISService service;

        private final List<Share> shares;

        private final IShareIdManager shareIdManager;

        UnarchivingPreparation(IShareFinder shareFinder, IShareIdManager shareIdManager,
                IEncapsulatedOpenBISService service, List<Share> shares)
        {
            this.shareFinder = shareFinder;
            this.shareIdManager = shareIdManager;
            this.service = service;
            this.shares = shares;
        }

        public void prepareForUnarchiving(DatasetDescription dataSet)
        {
            SimpleDataSetInformationDTO translatedDataSet = SimpleDataSetHelper.translate(dataSet);
            String dataSetCode = dataSet.getDataSetCode();
            String shareId = shareIdManager.getShareId(dataSetCode);
            translatedDataSet.setDataSetShareId(shareId);
            Share share = shareFinder.tryToFindShare(translatedDataSet, shares);
            if (share != null)
            {
                String newShareId = share.getShareId();
                if (newShareId.equals(shareId) == false)
                {
                    service.updateShareIdAndSize(dataSetCode, newShareId, dataSet.getDataSetSize());
                    shareIdManager.setShareId(dataSetCode, newShareId);
                }
            }
        }

    }

}
