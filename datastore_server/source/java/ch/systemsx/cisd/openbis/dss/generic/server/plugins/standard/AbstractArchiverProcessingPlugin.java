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
import ch.systemsx.cisd.openbis.dss.generic.server.ProcessDatasetsCommand;
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

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ProcessDatasetsCommand.class);

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

    abstract protected void archive(DatasetDescription dataset) throws UserFailureException;

    abstract protected void unarchive(DatasetDescription dataset) throws UserFailureException;

    public ProcessingStatus archive(List<DatasetDescription> datasets)
    {
        operationLog.info("Archiving of the following datasets has been requested: "
                + CollectionUtils.abbreviate(datasets, 10));
        return handleDatasets(datasets, DataSetArchivingStatus.ARCHIVED,
                DataSetArchivingStatus.AVAILABLE, new IDatasetDescriptionHandler()
                    {

                        public Status handle(DatasetDescription dataset)
                        {
                            if (archivePrerequisiteOrNull != null)
                            {
                                Status status = archivePrerequisiteOrNull.check();
                                if (status.isError())
                                {
                                    return status;
                                }
                            }
                            try
                            {
                                archive(dataset);
                            } catch (UserFailureException ex)
                            {
                                return Status.createError(ex.getMessage());
                            }
                            return Status.OK;
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

                        public Status handle(DatasetDescription dataset)
                        {
                            if (unarchivePrerequisiteOrNull != null)
                            {
                                Status status = unarchivePrerequisiteOrNull.check();
                                if (status.isError())
                                {
                                    return status;
                                }
                            }
                            try
                            {
                                unarchive(dataset);
                            } catch (UserFailureException ex)
                            {
                                return Status.createError(ex.getMessage());
                            }
                            return Status.OK;
                        }

                    });
    }

    private ProcessingStatus handleDatasets(List<DatasetDescription> datasets,
            DataSetArchivingStatus success, DataSetArchivingStatus failure,
            IDatasetDescriptionHandler handler)
    {
        final ProcessingStatus result = new ProcessingStatus();
        List<String> successful = new ArrayList<String>();
        List<String> failed = new ArrayList<String>();
        for (DatasetDescription dataset : datasets)
        {
            Status status = handler.handle(dataset);
            List<String> codes = status.isError() ? failed : successful;
            codes.add(dataset.getDatasetCode());
            result.addDatasetStatus(dataset, status);
        }
        asyncUpdateStatuses(successful, success);
        asyncUpdateStatuses(failed, failure);
        return result;
    }

    private static void asyncUpdateStatuses(List<String> dataSetCodes,
            DataSetArchivingStatus newStatus)
    {
        QueueingDataSetStatusUpdaterService.update(new DataSetCodesWithStatus(dataSetCodes,
                newStatus));
    }

    private interface IDatasetDescriptionHandler
    {
        public Status handle(DatasetDescription dataset);
    }

}
