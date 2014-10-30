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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.NotImplementedException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractArchiverProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncArchiveCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncArchiver;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.SshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.IMultiDatasetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverContainerDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDataSetArchiverDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.archiver.dataaccess.MultiDatasetArchiverDBTransaction;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Jakub Straszewski
 */
public class MultiDatasetArchiver extends AbstractArchiverProcessingPlugin
{
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

    private IMultiDatasetArchiverDBTransaction transaction;

    public MultiDatasetArchiver(Properties properties, File storeRoot)
    {
        super(properties, storeRoot, null, null);
        this.minimumContainerSize = PropertyUtils.getLong(properties, MINIMUM_CONTAINER_SIZE_IN_BYTES, DEFAULT_MINIMUM_CONTAINER_SIZE_IN_BYTES);
        this.maximumContainerSize = PropertyUtils.getLong(properties, MAXIMUM_CONTAINER_SIZE_IN_BYTES, DEFAULT_MAXIMUM_CONTAINER_SIZE_IN_BYTES);
        this.fileOperationsFactory = new FileOperationsManagerFactory(properties);
    }

    @Override
    protected DatasetProcessingStatuses doArchive(List<DatasetDescription> paramDataSets, ArchiverTaskContext context)
    {
        LinkedList<DatasetDescription> dataSets = new LinkedList<DatasetDescription>(paramDataSets);
        DatasetProcessingStatuses result = new DatasetProcessingStatuses();

        filterBasedOnArchiveStatus(dataSets, result, FilterOption.FILTER_ARCHIVED, Status.OK, Operation.ARCHIVE);

        if (dataSets.isEmpty())
        {
            return result;
        }

        try
        {
            verifyDataSetsSize(dataSets);

            DatasetProcessingStatuses archiveResult = archiveDataSets(dataSets, context);

            result.addResults(archiveResult);

            getTransaction().commit();
            getTransaction().close();
        } catch (Exception e)
        {
            operationLog.warn("Archiving of " + dataSets.size() + " data sets failed", e);
            try
            {
                getTransaction().rollback();
                getTransaction().close();
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
    private void filterBasedOnArchiveStatus(LinkedList<? extends IDatasetLocation> dataSets, DatasetProcessingStatuses result,
            FilterOption filterOption,
            Status status, Operation operation)
    {

        Iterator<? extends IDatasetLocation> it = dataSets.iterator();
        while (it.hasNext())
        {
            IDatasetLocation dataSet = it.next();
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
                it.remove();
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

    private DatasetProcessingStatuses archiveDataSets(List<DatasetDescription> dataSets, ArchiverTaskContext context) throws Exception
    {
        DatasetProcessingStatuses statuses = new DatasetProcessingStatuses();

        // for sharding we use the location of the first datast

        String containerPath = getFileOperations().generateContainerPath(dataSets);

        MultiDataSetArchiverContainerDTO container = getTransaction().createContainer(containerPath);

        for (DatasetDescription dataSet : dataSets)
        {
            getTransaction().insertDataset(dataSet, container);
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

    private void checkArchivedDataSets(IHierarchicalContent archivedContent, List<DatasetDescription> dataSets,
            ArchiverTaskContext context, DatasetProcessingStatuses statuses)
    {
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
    protected DatasetProcessingStatuses doUnarchive(List<DatasetDescription> datasets, ArchiverTaskContext context)
    {
        if (datasets.size() > 0)
        {
            throw new NotImplementedException("Unarchiving is not yet implemented for multi dataset archiver");
        }
        return new DatasetProcessingStatuses();
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
        MultiDataSetArchiverDataSetDTO dataSetInArchiveDB = getTransaction().getDataSetForCode(dataSetCode);
        return dataSetInArchiveDB != null;
    }

    public IMultiDataSetFileOperationsManager getFileOperations()
    {
        if (fileOperations == null)
        {
            fileOperations = fileOperationsFactory.create();
        }
        return fileOperations;
    }

    @Private IMultiDatasetArchiverDBTransaction getTransaction()
    {
        if (transaction == null)
        {
            transaction = new MultiDatasetArchiverDBTransaction();
        }
        return transaction;
    }
}
