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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractArchiverProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncArchiveCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncArchiver;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.SshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDataSetArchiverReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSourceUtil;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.IUnarchivingPreparation;
import ch.systemsx.cisd.openbis.dss.generic.shared.IncomingShareIdProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils.FilterOptions;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.Share;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.translator.SimpleDataSetHelper;

/**
 * @author Jakub Straszewski
 */
public class MultiDataSetArchiver extends AbstractArchiverProcessingPlugin
{
    private static final String ARCHIVING_FINALIZER = "Archiving Finalizer";

    private static final long serialVersionUID = 1L;

    private transient IMultiDataSetFileOperationsManager fileOperations;

    private final FileOperationsManagerFactory fileOperationsFactory;

    private static class FileOperationsManagerFactory implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private final Properties properties;

        private FileOperationsManagerFactory(Properties properties)
        {
            this.properties = properties;
        }

        private IMultiDataSetFileOperationsManager create()
        {
            return new MultiDataSetFileOperationsManager(properties, new RsyncArchiveCopierFactory(), new SshCommandExecutorFactory());
        }
    }

    private final long minimumContainerSize;

    private final long maximumContainerSize;

    public static final String MINIMUM_CONTAINER_SIZE_IN_BYTES = "minimum-container-size-in-bytes";

    public static final Long DEFAULT_MINIMUM_CONTAINER_SIZE_IN_BYTES = 10L * 1024 * 1024 * 1024;

    public static final String MAXIMUM_CONTAINER_SIZE_IN_BYTES = "maximum-container-size-in-bytes";

    public static final Long DEFAULT_MAXIMUM_CONTAINER_SIZE_IN_BYTES = 80L * 1024 * 1024 * 1024;
    
    public static final long DEFAULT_FINALIZER_POLLING_TIME = DateUtils.MILLIS_PER_MINUTE;
    
    public static final long DEFAULT_FINALIZER_WAITING_TIME = DateUtils.MILLIS_PER_DAY;

    private transient IMultiDataSetArchiverReadonlyQueryDAO readonlyQuery;
    
    private transient IDataStoreServiceInternal dataStoreService;

    private final long finalizerPollingTime;

    private final long finalizerWaitingTime;

    public MultiDataSetArchiver(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, null, null);
        this.minimumContainerSize = PropertyUtils.getLong(properties, MINIMUM_CONTAINER_SIZE_IN_BYTES, DEFAULT_MINIMUM_CONTAINER_SIZE_IN_BYTES);
        this.maximumContainerSize = PropertyUtils.getLong(properties, MAXIMUM_CONTAINER_SIZE_IN_BYTES, DEFAULT_MAXIMUM_CONTAINER_SIZE_IN_BYTES);
        this.fileOperationsFactory = new FileOperationsManagerFactory(properties);
        finalizerPollingTime = DateTimeUtils.getDurationInMillis(properties, 
                MultiDataSetArchivingFinalizer.FINALIZER_POLLING_TIME_KEY, DEFAULT_FINALIZER_POLLING_TIME);
        finalizerWaitingTime = DateTimeUtils.getDurationInMillis(properties, 
                MultiDataSetArchivingFinalizer.FINALIZER_WAITING_TIME_KEY, DEFAULT_FINALIZER_WAITING_TIME);
    }

    @Override
    protected DatasetProcessingStatuses doArchive(List<DatasetDescription> paramDataSets, 
            ArchiverTaskContext context, boolean removeFromDataStore)
    {
        LinkedList<DatasetDescription> dataSets = new LinkedList<DatasetDescription>(paramDataSets);
        DatasetProcessingStatuses result = new DatasetProcessingStatuses();
        result.setStatusUpdatingSupressed(needsToWaitForReplication());

        filterBasedOnArchiveStatus(dataSets, result, FilterOption.FILTER_ARCHIVED, Status.OK, Operation.ARCHIVE);

        if (dataSets.isEmpty())
        {
            return result;
        }

        IMultiDataSetArchiverDBTransaction transaction = getTransaction();

        try
        {
            verifyDataSetsSize(dataSets);

            DatasetProcessingStatuses archiveResult = archiveDataSets(dataSets, context, removeFromDataStore, transaction);

            result.addResults(archiveResult);

            transaction.commit();
            transaction.close();
        } catch (Exception e)
        {
            operationLog.warn("Archiving of " + dataSets.size() + " data sets failed", e);
            try
            {
                transaction.rollback();
                transaction.close();
            } catch (Exception ex)
            {
                operationLog.warn("Rollback of multi dataset db transaction failed", ex);
            }
            result.addResult(dataSets, Status.createError(e.getMessage()), Operation.ARCHIVE);
        }
        return result;
    }

    private static enum FilterOption
    {
        FILTER_ARCHIVED,
        FILTER_UNARCHIVED
    }

    /**
     * NOTE: This method MODIFIES both arguments. It removes items from dataSets list and adds values to result. Re Iterate over the
     * <code>dataSets</code> and removes those which are present in the archive already (or not present, depending on the <code>filterOption</code>).
     * For those removed data sets it adds entry with <code>status</code> for <code>operation</code> in <code>result</code>
     */
    private void filterBasedOnArchiveStatus(LinkedList<? extends IDatasetLocation> dataSets,
            DatasetProcessingStatuses result, FilterOption filterOption, Status status, Operation operation)
    {
        for (Iterator<? extends IDatasetLocation> iterator = dataSets.iterator(); iterator.hasNext();)
        {
            IDatasetLocation dataSet = iterator.next();
            boolean isPresentInArchive = isDataSetPresentInArchive(dataSet.getDataSetCode());

            boolean isFiltered;

            switch (filterOption)
            {
                case FILTER_ARCHIVED:
                    isFiltered = isPresentInArchive;
                    break;
                case FILTER_UNARCHIVED:
                    isFiltered = (false == isPresentInArchive);
                    break;
                default:
                    throw new IllegalStateException("All cases should be covered");
            }

            if (isFiltered)
            {
                result.addResult(dataSet.getDataSetCode(), status, operation);
                iterator.remove();
            }
        }
    }

    private void verifyDataSetsSize(List<DatasetDescription> dataSets)
    {
        long datasetSize = getDataSetsSize(dataSets);
        if (dataSets.size() == 1)
        {
            if (datasetSize < minimumContainerSize)
            {
                throw new IllegalArgumentException("Dataset " + dataSets.get(0).getDataSetCode()
                        + " is too small (" + FileUtilities.byteCountToDisplaySize(datasetSize)
                        + ") to be archived with multi dataset archiver because minimum size is "
                        + FileUtilities.byteCountToDisplaySize(minimumContainerSize) + ".");
            }
            // if single dataset is bigger than specified maximum, we should still allow it being
        }
        else
        {
            if (datasetSize < minimumContainerSize)
            {
                throw new IllegalArgumentException("Set of data sets specified for archiving is too small ("
                        + FileUtilities.byteCountToDisplaySize(datasetSize)
                        + ") to be archived with multi dataset archiver because minimum size is "
                        + FileUtilities.byteCountToDisplaySize(minimumContainerSize) + ".");
            }
            else if (datasetSize > maximumContainerSize)
            {
                throw new IllegalArgumentException("Set of data sets specified for archiving is too big ("
                        + FileUtilities.byteCountToDisplaySize(datasetSize)
                        + ") to be archived with multi dataset archiver because maximum size is "
                        + FileUtilities.byteCountToDisplaySize(maximumContainerSize) + ".");
            }
        }
    }

    private DatasetProcessingStatuses archiveDataSets(List<DatasetDescription> dataSets, ArchiverTaskContext context,
            boolean removeFromDataStore, IMultiDataSetArchiverDBTransaction transaction) throws Exception
    {
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();

        // for sharding we use the location of the first datast

        String containerPath = getFileOperations().generateContainerPath(dataSets);

        MultiDataSetArchiverContainerDTO container = transaction.createContainer(containerPath);

        for (DatasetDescription dataSet : dataSets)
        {
            transaction.insertDataset(dataSet, container);
        }

        IHierarchicalContent archivedContent = null;

        try
        {

            Status status = getFileOperations().createContainerInStage(containerPath, dataSets);
            if (status.isError())
            {
                throw new Exception("Couldn't create package file in stage archive " + containerPath);
            }

            status = getFileOperations().copyToFinalDestination(containerPath);

            if (status.isError())
            {
                throw new Exception("Couldn't copy container to final store");
            }

            archivedContent = getFileOperations().getContainerAsHierarchicalContent(containerPath);

            checkArchivedDataSets(archivedContent, dataSets, context, statuses);
            scheduleFinalizer(containerPath, dataSets, context, removeFromDataStore);
        } catch (Exception ex)
        {
            getFileOperations().deleteContainerFromFinalDestination(containerPath);
            // In case of error we actually should delete failed container here. If the transaction fail that the AbstractArchiver is unable to locate
            // container file.
            throw ex;
        } finally
        {
            // always delete staging content
            getFileOperations().deleteContainerFromStage(containerPath);

            if (archivedContent != null)
            {
                archivedContent.close();
            }
        }
        return statuses;
    }
    
    private void scheduleFinalizer(String containerPath, List<DatasetDescription> dataSets, 
            ArchiverTaskContext archiverContext, boolean removeFromDataStore)
    {
        if (needsToWaitForReplication() == false)
        {
            return;
        }
        MultiDataSetArchivingFinalizer task = new MultiDataSetArchivingFinalizer(null, null);
        String userId = archiverContext.getUserId();
        String userEmail = archiverContext.getUserEmail();
        String userSessionToken = archiverContext.getUserSessionToken();
        HashMap<String, String> parameterBindings = new HashMap<String, String>();
        IMultiDataSetFileOperationsManager operations = getFileOperations();
        parameterBindings.put(MultiDataSetArchivingFinalizer.ORIGINAL_FILE_PATH_KEY, 
                operations.getOriginalArchiveFilePath(containerPath));
        parameterBindings.put(MultiDataSetArchivingFinalizer.REPLICATED_FILE_PATH_KEY, 
                operations.getReplicatedArchiveFilePath(containerPath));
        parameterBindings.put(MultiDataSetArchivingFinalizer.FINALIZER_POLLING_TIME_KEY, Long.toString(finalizerPollingTime));
        parameterBindings.put(MultiDataSetArchivingFinalizer.FINALIZER_WAITING_TIME_KEY, Long.toString(finalizerWaitingTime));
        DataSetArchivingStatus status = removeFromDataStore ? DataSetArchivingStatus.ARCHIVED : DataSetArchivingStatus.AVAILABLE;
        parameterBindings.put(MultiDataSetArchivingFinalizer.STATUS_KEY, status.toString());
        getDataStoreService().scheduleTask(ARCHIVING_FINALIZER, task, parameterBindings, dataSets, 
                userId, userEmail, userSessionToken);
    }
    
    private boolean needsToWaitForReplication()
    {
        return getFileOperations().isReplicatedArchiveDefined();
    }

    private void checkArchivedDataSets(IHierarchicalContent archivedContent, List<DatasetDescription> dataSets,
            ArchiverTaskContext context, DatasetProcessingStatuses statuses)
    {
        operationLog.info("Start sanity check on " + CollectionUtils.abbreviate(dataSets, 10));
        Status status;
        for (DatasetDescription dataset : dataSets)
        {
            String dataSetCode = dataset.getDataSetCode();
            IHierarchicalContent content = null;
            try
            {
                content = context.getHierarchicalContentProvider().asContentWithoutModifyingAccessTimestamp(dataSetCode);

                IHierarchicalContentNode root = content.getRootNode();
                IHierarchicalContentNode archiveDataSetRoot = archivedContent.getNode(dataset.getDataSetCode());

                status =
                        RsyncArchiver.checkHierarchySizeAndChecksums(root, dataSetCode, archiveDataSetRoot,
                                RsyncArchiver.ChecksumVerificationCondition.IF_AVAILABLE);

                if (status.isError())
                {
                    throw new RuntimeException(status.tryGetErrorMessage());
                }
            } finally
            {
                if (content != null)
                {
                    content.close();
                }
            }
            statuses.addResult(dataSetCode, status, Operation.ARCHIVE);
        }
        operationLog.info("Sanity check finished.");
    }

    private long getDataSetsSize(List<DatasetDescription> ds)
    {
        long result = 0;
        for (DatasetDescription dataset : ds)
        {
            result += dataset.getDataSetSize();
        }
        return result;
    }

    @Override
    public List<String> getDataSetCodesForUnarchiving(List<String> dataSetCodes)
    {
        assertAllDataSetsInTheSameContainer(dataSetCodes);
        return getCodesOfAllDataSetsInContainer(dataSetCodes);
    }

    @Override
    protected IUnarchivingPreparation getUnarchivingPreparation()
    {
        Share scratchShare = findScratchShare();

        IDataSetDirectoryProvider directoryProvider = ServiceProvider.getDataStoreService().getDataSetDirectoryProvider();

        return new MultiDataSetUnarchivingPreparations(scratchShare, getShareIdManager(), getService(), directoryProvider);
    }

    private Share findScratchShare()
    {
        String dataStoreCode = ServiceProvider.getConfigProvider().getDataStoreCode();
        Set<String> incomingShares = IncomingShareIdProvider.getIdsOfIncomingShares();
        IFreeSpaceProvider freeSpaceProvider = createFreeSpaceProvider();
        List<Share> shares =
                SegmentedStoreUtils.getSharesWithDataSets(storeRoot, dataStoreCode, FilterOptions.ARCHIVING_SCRATCH, incomingShares,
                        freeSpaceProvider, getService(), new Log4jSimpleLogger(operationLog));
        if (shares.size() != 1)
        {
            throw new ConfigurationFailureException("There should be exactly one unarchiving scratch share configured!");
        }
        Share scratchShare = shares.get(0);
        return scratchShare;
    }
    
    private static class MultiDataSetUnarchivingPreparations implements IUnarchivingPreparation
    {
        private final Share scratchShare;

        private final IEncapsulatedOpenBISService service;

        private final IShareIdManager shareIdManager;

        private final IDataSetDirectoryProvider directoryProvider;

        MultiDataSetUnarchivingPreparations(Share scratchShare, IShareIdManager shareIdManager, IEncapsulatedOpenBISService service,
                IDataSetDirectoryProvider directoryProvider)
        {
            this.shareIdManager = shareIdManager;
            this.service = service;
            this.scratchShare = scratchShare;
            this.directoryProvider = directoryProvider;
        }

        @Override
        public void prepareForUnarchiving(List<DatasetDescription> dataSets)
        {
            for (DatasetDescription dataSet : dataSets)
            {
                SimpleDataSetInformationDTO translatedDataSet = SimpleDataSetHelper.translate(dataSet);
                String dataSetCode = dataSet.getDataSetCode();
                translatedDataSet.setDataSetShareId(null);
                String oldShareId = shareIdManager.getShareId(dataSetCode);
                String newShareId = scratchShare.getShareId();
                if (newShareId.equals(oldShareId) == false)
                {
                    service.updateShareIdAndSize(dataSetCode, newShareId, dataSet.getDataSetSize());
                    shareIdManager.setShareId(dataSetCode, newShareId);
                }
            }

            SegmentedStoreUtils.freeSpace(scratchShare, service, dataSets, directoryProvider, shareIdManager, new Log4jSimpleLogger(operationLog));

        }
    }

    @Override
    protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> parameterDataSets, ArchiverTaskContext context)
    {
        List<String> dataSetCodes = translateToDataSetCodes(parameterDataSets);
        long containerId = assertAllDataSetsInTheSameContainer(dataSetCodes);
        assertNoAvailableDatasets(dataSetCodes);
        
        context.getUnarchivingPreparation().prepareForUnarchiving(parameterDataSets);

        MultiDataSetArchiverContainerDTO container = getReadonlyQuery().getContainerForId(containerId);

        getFileOperations().restoreDataSetsFromContainerInFinalDestination(container.getPath(), parameterDataSets);

        for (String dataSetCode : dataSetCodes)
        {
            getService().notifyDatasetAccess(dataSetCode);
        }

        DatasetProcessingStatuses result = new DatasetProcessingStatuses();
        result.addResult(parameterDataSets, Status.OK, Operation.UNARCHIVE);
        return result;
    }

    private void assertNoAvailableDatasets(List<String> dataSetCodes)
    {
        List<PhysicalDataSet> dataSets = translateToPhysicalDataSets(dataSetCodes);
        for (PhysicalDataSet physicalDataSet : dataSets)
        {
            if (physicalDataSet.isAvailable())
            {
                throw new UserFailureException("Dataset '" + physicalDataSet.getCode() + "'specified for unarchiving is available");
            }
        }
    }

    private List<String> getCodesOfAllDataSetsInContainer(List<String> dataSetCodes)
    {

        MultiDataSetArchiverDataSetDTO dataset = getReadonlyQuery().getDataSetForCode(dataSetCodes.get(0));
        Long containerId = dataset.getContainerId();
        List<MultiDataSetArchiverDataSetDTO> dbDataSets = getReadonlyQuery().listDataSetsForContainerId(containerId);

        List<String> enhancedDataSetCodes = new LinkedList<String>();
        for (MultiDataSetArchiverDataSetDTO dbDataSet : dbDataSets)
        {
            enhancedDataSetCodes.add(dbDataSet.getCode());
        }
        return enhancedDataSetCodes;
    }

    private List<PhysicalDataSet> translateToPhysicalDataSets(List<String> dataSetCodes)
    {
        List<PhysicalDataSet> result = new LinkedList<PhysicalDataSet>();
        for (AbstractExternalData dataSet : getService().listDataSetsByCode(dataSetCodes))
        {
            if (dataSet.tryGetAsDataSet() != null)
            {
                result.add(dataSet.tryGetAsDataSet());
            }
            else
            {
                throw new IllegalStateException("All data sets in container are expected to be physical datasets, but data set '" + dataSet.getCode()
                        + "' is not ");
            }
        }
        return result;
    }

    private List<String> translateToDataSetCodes(List<? extends IDatasetLocation> dataSets)
    {
        LinkedList<String> result = new LinkedList<String>();
        for (IDatasetLocation dataSet : dataSets)
        {
            result.add(dataSet.getDataSetCode());
        }
        return result;
    }

    /**
     * @return ID of container that groups all listed data sets
     * @throws exception if not all data sets are in the same container
     */
    private long assertAllDataSetsInTheSameContainer(List<String> dataSetCodes)
    {
        Map<Long, List<String>> containers = new LinkedHashMap<Long, List<String>>();
        long containerId = -1;
        for (String code : dataSetCodes)
        {
            MultiDataSetArchiverDataSetDTO dataSet = getReadonlyQuery().getDataSetForCode(code);
            if (dataSet == null)
            {
                throw new UserFailureException("Dataset " + code
                        + " was selected for unarchiving, but is not present in the archive");
            }
            List<String> list = containers.get(dataSet.getContainerId());
            if (list == null)
            {
                list = new ArrayList<String>();
                containers.put(dataSet.getContainerId(), list);
            }
            list.add(dataSet.getCode());
            containerId = dataSet.getContainerId();
        }
        if (containers.size() > 1)
        {
            throw new UserFailureException("Datasets selected for unarchiving do not all belong to one container, "
                    + "but to " + containers.size() + " different containers: " + containers);
        }
        return containerId;
    }

    @Override
    protected DatasetProcessingStatuses doDeleteFromArchive(List<? extends IDatasetLocation> datasets)
    {
        LinkedList<IDatasetLocation> localDataSets = new LinkedList<IDatasetLocation>(datasets);
        DatasetProcessingStatuses results = new DatasetProcessingStatuses();

        filterBasedOnArchiveStatus(localDataSets, results, FilterOption.FILTER_UNARCHIVED, Status.OK, Operation.DELETE_FROM_ARCHIVE);

        if (localDataSets.size() > 0)
        {
            throw new NotImplementedException("Deleting from archive is not yet implemented for multi dataset archiver");
        }
        return results;
    }

    @Override
    // TODO: implement a real check? Run the checkArchivedDataSets logic
    protected BooleanStatus isDataSetSynchronizedWithArchive(DatasetDescription dataset, ArchiverTaskContext context)
    {
        return isDataSetPresentInArchive(dataset);
    }

    @Override
    protected BooleanStatus isDataSetPresentInArchive(DatasetDescription dataSet)
    {
        return BooleanStatus.createFromBoolean(isDataSetPresentInArchive(dataSet.getDataSetCode()));
    }

    protected boolean isDataSetPresentInArchive(String dataSetCode)
    {
        MultiDataSetArchiverDataSetDTO dataSetInArchiveDB = getReadonlyQuery().getDataSetForCode(dataSetCode);
        return dataSetInArchiveDB != null;
    }

    @Private
    IMultiDataSetFileOperationsManager getFileOperations()
    {
        if (fileOperations == null)
        {
            fileOperations = fileOperationsFactory.create();
        }
        return fileOperations;
    }

    @Private
    IMultiDataSetArchiverDBTransaction getTransaction()
    {
        return new MultiDataSetArchiverDBTransaction();
    }

    IMultiDataSetArchiverReadonlyQueryDAO getReadonlyQuery()
    {
        if (readonlyQuery == null)
        {
            readonlyQuery = MultiDataSetArchiverDataSourceUtil.getReadonlyQueryDAO();
        }
        return readonlyQuery;
    }
    
    IDataStoreServiceInternal getDataStoreService()
    {
        if (dataStoreService == null)
        {
            dataStoreService = ServiceProvider.getDataStoreService();
        }
        return dataStoreService;
    }
}
