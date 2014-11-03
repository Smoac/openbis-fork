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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.ExceptionWithStatus;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.FilteredHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.IHierarchicalContentNodeFilter;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContent;
import ch.systemsx.cisd.openbis.common.io.hierarchical_content.api.IHierarchicalContentNode;
import ch.systemsx.cisd.openbis.dss.archiveverifier.batch.VerificationError;
import ch.systemsx.cisd.openbis.dss.generic.server.AbstractDataSetPackager;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IPathCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.ISshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Jakub Straszewski
 */
public class MultiDataSetFileOperationsManager extends AbstractDataSetFileOperationsManager implements IMultiDataSetFileOperationsManager,
        Serializable
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, MultiDataSetFileOperationsManager.class);

    private static final long serialVersionUID = 1L;

    public static final String STAGING_DESTINATION_KEY = "staging-destination";

    public static final String FINAL_DESTINATION_KEY = "final-destination";

    public static final String WITH_SHARDING_KEY = "with-sharding";

    private transient ArchiveDestination stageArchive;

    private transient ArchiveDestination finalArchive;

    private final ArchiveDestinationFactory stageArchivefactory;

    private final ArchiveDestinationFactory finalArchivefactory;

    private final boolean withSharding;

    protected IMultiDataSetPackageManager packageManager;

    // TODO: some features existing in rsync archiver:
    // - ignore existing

    public MultiDataSetFileOperationsManager(Properties properties, IPathCopierFactory pathCopierFactory,
            ISshCommandExecutorFactory sshCommandExecutorFactory)
    {
        this.packageManager = new MultiDataSetPackageManager(properties);

        this.withSharding = PropertyUtils.getBoolean(properties, WITH_SHARDING_KEY, false);

        long timeoutInSeconds =
                PropertyUtils.getLong(properties, TIMEOUT_KEY, DEFAULT_TIMEOUT_SECONDS);
        long timeoutInMillis = timeoutInSeconds * DateUtils.MILLIS_PER_SECOND;

        String stagingHostFile = PropertyUtils.getMandatoryProperty(properties, STAGING_DESTINATION_KEY);

        String finalHostFile = PropertyUtils.getMandatoryProperty(properties, FINAL_DESTINATION_KEY);

        if (false == new File(stagingHostFile).isDirectory())
        {
            throw new ConfigurationFailureException("Archiving stage area '" + stagingHostFile + "' is not an existing directory");
        }

        if (false == new File(finalHostFile).isDirectory())
        {
            throw new ConfigurationFailureException("Archiving final destination '" + finalHostFile + "' is not an existing directory");
        }

        this.stageArchivefactory =
                new ArchiveDestinationFactory(properties, pathCopierFactory, sshCommandExecutorFactory, stagingHostFile, timeoutInMillis);
        this.finalArchivefactory =
                new ArchiveDestinationFactory(properties, pathCopierFactory, sshCommandExecutorFactory, finalHostFile, timeoutInMillis);

    }

    private ArchiveDestination getStageArchive()
    {
        if (stageArchive == null)
        {
            stageArchive = stageArchivefactory.createArchiveDestination();
        }
        return stageArchive;
    }

    private ArchiveDestination getFinalArchive()
    {
        if (finalArchive == null)
        {
            finalArchive = finalArchivefactory.createArchiveDestination();
        }
        return finalArchive;
    }

    @Override
    public Status deleteContainerFromStage(String containerPath)
    {
        File stageArchiveContainerFile = new File(getStageArchive().getDestination(), containerPath);

        if (false == stageArchiveContainerFile.isFile())
        {
            operationLog.warn("Archive container '" + containerPath + "' doesn't exist.");
            return Status.OK;
        }
        boolean success = stageArchiveContainerFile.delete();
        return success ? Status.OK : Status.createError("Couldn't delete archive container '" + containerPath);
    }

    public Status restoreDataSetsFromContainerInFinalDestination(String containerPath, String unarchivingShareId,
            List<DatasetDescription> dataSetDescriptions)
    {
        HashMap<String, File> dataSetToLocation = new HashMap<String, File>();
        for (DatasetDescription datasetDescription : dataSetDescriptions)
        {
            File location = getDirectoryProvider().getDataSetDirectory(unarchivingShareId, datasetDescription.getDataSetLocation());
            dataSetToLocation.put(datasetDescription.getDataSetCode(), location);
        }

        File stageArchiveContainerFile = new File(getFinalArchive().getDestination(), containerPath);
        packageManager.extractMultiDataSets(stageArchiveContainerFile, dataSetToLocation);

        return Status.OK;
    }

    @Override
    public Status createContainerInStage(String containerPath, List<DatasetDescription> datasetDescriptions)
    {
        File stageArchiveContainerFile = new File(getStageArchive().getDestination(), containerPath);
        IShareIdManager shareIdManager = getDirectoryProvider().getShareIdManager();
        Status status = Status.OK;
        try
        {
            List<AbstractExternalData> dataSets = new LinkedList<AbstractExternalData>();
            for (DatasetDescription datasetDescription : datasetDescriptions)
            {
                AbstractExternalData dataSet = getDataSetWithAllMetaData(datasetDescription);
                dataSets.add(dataSet);
                shareIdManager.lock(dataSet.getCode());
                operationLog.info("Archive dataset " + dataSet.getCode() + " in " + containerPath);
            }

            boolean result = createFolderIfNotExists(stageArchive, stageArchiveContainerFile.getParentFile());

            // TODO: react somehow?
            if (result)
            {
                operationLog.warn("File already exists in archive " + stageArchiveContainerFile.getParentFile());
            }

            packageManager.create(stageArchiveContainerFile, dataSets);
        } catch (Exception ex)
        {
            status = Status.createError(ex.toString());
            operationLog.error("Couldn't create package file: " + containerPath, ex);
        } finally
        {
            try
            {
                if (Status.OK.equals(status))
                {
                    Collection<VerificationError> errors = packageManager.verify(stageArchiveContainerFile);

                    if (errors.size() > 0)
                    {
                        status = Status.createError(errors.toString());
                        throw new RuntimeException(errors.toString());
                    }
                }

                operationLog.info("Data sets archived: " + containerPath);
            } catch (Exception ex)
            {
                operationLog.error("Couldn't create package file: " + containerPath, ex);
            }
            for (DatasetDescription datasetDescription : datasetDescriptions)
            {
                shareIdManager.releaseLock(datasetDescription.getDataSetCode());
            }
        }
        return status;
    }

    /**
     * Returns container path local to the archive root.
     */
    @Override
    public String generateContainerPath(List<DatasetDescription> dataSets)
    {
        String name = packageManager.getName(dataSets.get(0).getDataSetCode());

        if (withSharding)
        {
            return dataSets.get(0).getDataSetLocation() + "/" + name;
        }
        else
        {
            return name;
        }

    }

    /**
     * Copies specified dataset's data to destination specified in constructor. The path at the destination is defined by the original location of the
     * data set.
     */
    @Override
    public Status copyToFinalDestination(String containerLocalPath)
    {
        ArchiveDestination stageDestination = getStageArchive();
        File containerFile = new File(stageDestination.getDestination(), containerLocalPath);

        ArchiveDestination finalDestination = getFinalArchive();
        try
        {
            File destinationFolder = new File(finalDestination.getDestination(), containerLocalPath);
            if (createFolderIfNotExists(finalDestination, destinationFolder.getParentFile())
                    || destinationExists(finalDestination, destinationFolder).isSuccess() == false)
            {
                operationLog.info("Copy archive container from '"
                        + containerFile + "' to '" + destinationFolder.getParentFile());
                finalDestination.getExecutor().copyDataSetToDestination(containerFile, destinationFolder.getParentFile());
            } else
            {
                operationLog.info("Update archive container from '"
                        + containerFile + "' to '" + destinationFolder.getParentFile());
                finalDestination.getExecutor().syncDataSetWithDestination(containerFile, destinationFolder.getParentFile());
            }

            return Status.OK;
        } catch (ExceptionWithStatus ex)
        {
            return ex.getStatus();
        }
    }

    @Override
    public Status deleteContainerFromFinalDestination(String containerLocalPath)
    {
        try
        {
            ArchiveDestination finalDestination = getFinalArchive();
            File containerInFinalDestination = new File(finalDestination.getDestination(), containerLocalPath);
            finalDestination.getExecutor().deleteFolder(containerInFinalDestination);
            return Status.OK;
        } catch (ExceptionWithStatus ex)
        {
            return ex.getStatus();
        }
    }

    private static final IHierarchicalContentNodeFilter METADATA_IN_CONTAINER_FILTER = new IHierarchicalContentNodeFilter()
        {
            @Override
            public boolean accept(IHierarchicalContentNode node)
            {
                return AbstractDataSetPackager.META_DATA_FILE_NAME.equals(node.getName()) == false;
            }
        };

    @Override
    public IHierarchicalContent getContainerAsHierarchicalContent(String containerPath)
    {
        ArchiveDestination archiveDestination = getFinalArchive();
        String destinationRoot = archiveDestination.getDestination();
        File containerInDestination = new File(destinationRoot, containerPath);

        return new FilteredHierarchicalContent(packageManager.asHierarchialContent(containerInDestination), METADATA_IN_CONTAINER_FILTER);

    }

    private boolean createFolderIfNotExists(ArchiveDestination archiveDestination, File destinationFolder)
    {
        BooleanStatus destinationExists = destinationExists(archiveDestination, destinationFolder);
        if (destinationExists.isSuccess() == false)
        {
            archiveDestination.getExecutor().createFolder(destinationFolder);
            return true;
        }
        return false;
    }

    private BooleanStatus destinationExists(ArchiveDestination archiveDestination, File destinationFolder)
    {
        BooleanStatus destinationExists = archiveDestination.getExecutor().exists(destinationFolder);
        if (destinationExists.isError())
        {
            // operationLog.error("Could not check existence of '" + destinationFolder + "': "
            // + destinationExists.tryGetMessage());
            throw new ExceptionWithStatus(Status.createError("CHECK_EXISTENCE_FAILED"));
        }
        return destinationExists;
    }

}
