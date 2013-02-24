/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.postregistration;

import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.AVAILABLE;
import static ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus.BACKUP_PENDING;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.etlserver.plugins.AutoArchiverTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.translator.DataSetTranslator;

/**
 * A post-registration task that archives datasets.
 * 
 * @author Kaloyan Enimanev
 */
public class ArchivingPostRegistrationTask extends AbstractPostRegistrationTask
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ArchivingPostRegistrationTask.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            ArchivingPostRegistrationTask.class);

    public ArchivingPostRegistrationTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        super(properties, service);
    }

    /**
     * do not allow concurrent maintenance tasks to run if they alter the data store contents.
     */
    @Override
    public boolean requiresDataStoreLock()
    {
        return true;
    }

    @Override
    public IPostRegistrationTaskExecutor createExecutor(String dataSetCode, boolean container)
    {
        if (container)
        {
            return DummyPostRegistrationTaskExecutor.INSTANCE;
        }
        return new Executor(dataSetCode);
    }
    
    private final class Executor implements IPostRegistrationTaskExecutor
    {
        private final String dataSetCode;

        Executor(String dataSetCode)
        {
            this.dataSetCode = dataSetCode;

        }

        /**
         * archives the dataset for the specified dataset code.
         */
        @Override
        public void execute()
        {

            IArchiverPlugin archiver = ServiceProvider.getDataStoreService().getArchiverPlugin();
            if (archiver == null)
            {
                // no archiver is configured
                operationLog
                        .error("Post-registration archiving cannot be completed, because there is "
                                + "no archiver configured. Please configure an archiver and restart. ");
                return;
            }

            AbstractExternalData dataSet = tryGetExternalData(dataSetCode, service);
            if (dataSet == null)
            {
                operationLog.warn("Data set '" + dataSetCode
                        + "' is no longer available in openBIS."
                        + "Archiving post-registration task will be skipped...");
                return;
            }

            boolean statusUpdated =
                    service.compareAndSetDataSetStatus(dataSetCode, AVAILABLE, BACKUP_PENDING,
                            false);
            if (statusUpdated)
            {
                DatasetDescription dataSetDescription =
                        DataSetTranslator.translateToDescription(dataSet);
                List<DatasetDescription> dataSetAsList =
                        Collections.singletonList(dataSetDescription);
                ProcessingStatus processingStatus =
                        archiver.archive(dataSetAsList, createArchiverContext(), false);
                if (false == processingStatus.getErrorStatuses().isEmpty())
                {
                    notifyAdministrator(processingStatus);
                }
                service.compareAndSetDataSetStatus(dataSetCode, BACKUP_PENDING, AVAILABLE, true);
            }
        }

        private void notifyAdministrator(ProcessingStatus processingStatus)
        {
            StringBuilder message = new StringBuilder();
            String failedMessage =
                    String.format("Eager archiving of dataset '%s' has failed. \n", dataSetCode);
            message.append(failedMessage);
            for (Status status : processingStatus.getErrorStatuses()) 
            {
                if (status.tryGetErrorMessage() != null)
                {
                    message.append("Error encountered : " + status.tryGetErrorMessage());
                    message.append("\n");
                }
            }
            String footer =
                    String.format("If you wish to archive the dataset in the future, "
                            + "you can configure an '%s'.", AutoArchiverTask.class.getSimpleName());
            message.append(footer);
            
            notificationLog.error(message);
        }

        @Override
        public ICleanupTask createCleanupTask()
        {
            return new ArchivingCleanupTask(dataSetCode);
        }
    }

    private static AbstractExternalData tryGetExternalData(String dataSetCode,
            IEncapsulatedOpenBISService service)
    {
        List<String> codeAsList = Collections.singletonList(dataSetCode);
        List<AbstractExternalData> dataList = service.listDataSetsByCode(codeAsList);
        if (dataList == null || dataList.isEmpty())
        {
            return null;
        }

        AbstractExternalData data = dataList.get(0);
        return data;
    }

    private static DatasetDescription tryGetDatasetWithLocation(String dataSetCode,
            IEncapsulatedOpenBISService service)
    {
        AbstractExternalData data = tryGetExternalData(dataSetCode, service);
        return (data != null) ? DataSetTranslator.translateToDescription(data) : null;
    }

    private static ArchiverTaskContext createArchiverContext()
    {
        return new ArchiverTaskContext(ServiceProvider.getDataStoreService()
                .getDataSetDirectoryProvider(), ServiceProvider.getHierarchicalContentProvider());
    }

    private static class ArchivingCleanupTask implements ICleanupTask
    {
        private static final long serialVersionUID = 1L;

        private final String dataSetCode;

        ArchivingCleanupTask(String dataSetCode)
        {
            this.dataSetCode = dataSetCode;
        }

        @Override
        public void cleanup(ISimpleLogger logger)
        {
            IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
            boolean statusUpdated = openBISService
                    .compareAndSetDataSetStatus(dataSetCode, BACKUP_PENDING, AVAILABLE, false);
            
            if (statusUpdated == false) {
                // invalid data set status, do not continue 
                return;
            }
            
            IArchiverPlugin archiver = ServiceProvider.getDataStoreService().getArchiverPlugin();
            DatasetDescription dataSet = tryGetDatasetWithLocation(dataSetCode, openBISService);
            if (archiver != null && dataSet != null && dataSet.getDataSetLocation() != null)
            {
                DatasetLocation dataset = new DatasetLocation();
                dataset.setDatasetCode(dataSetCode);
                dataset.setDataSetLocation(dataSet.getDataSetLocation());

                List<DatasetLocation> dataSetAsList = Collections.singletonList(dataset);
                archiver.deleteFromArchive(dataSetAsList);
                logger.log(LogLevel.INFO, "Successfully cleaned up leftovers from incomplete "
                        + "archiving of dataset '" + dataSetCode + "'.");
            }
        }
    }
}
