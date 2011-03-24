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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.DataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ArchiverPluginFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskProviders;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExternalDataTranslator;

/**
 * An post-registration task that archives datasets.
 * 
 * @author Kaloyan Enimanev
 */
public class ArchivingPostRegistrationTask extends AbstractPostRegistrationTask
{
    private static PluginTaskProviders pluginTaskProviders;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ArchivingPostRegistrationTask.class);


    public ArchivingPostRegistrationTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        super(properties, service);
    }

    /**
     * do not allow concurrent maintenance tasks to run if they alter the data store contents.
     */
    public boolean requiresDataStoreLock()
    {
        return true;
    }

    /**
     * archives the dataset for the specified dataset code.
     */
    public void execute(String dataSetCode)
    {

        IArchiverPlugin archiver = tryCreateArchiver();
        if (archiver == null)
        {
            // no archiver is configured
            operationLog.error("Post-registration archiving cannot be completed, because there is "
                    + "no archiver configured. Please configure an archiver and restart. ");
            return;
        }

        DatasetDescription dataSet = tryGetDatasetDescription(dataSetCode, service);
        if (dataSet == null)
        {
            operationLog.warn("Data set with code " + dataSetCode
                    + " is no longer available in openBIS. Archiving post-registration "
                    + "task will be skipped...");
            return;
        }

        boolean statusUpdated =
                service.compareAndSetDataSetStatus(dataSetCode, AVAILABLE, BACKUP_PENDING, false);
        if (statusUpdated)
        {
            List<DatasetDescription> dataSetAsList = Collections.singletonList(dataSet);
            archiver.archive(dataSetAsList, createArchiverContext(), false);
            service.compareAndSetDataSetStatus(dataSetCode, BACKUP_PENDING, AVAILABLE, true);
        }
    }

    private static DatasetDescription tryGetDatasetDescription(String dataSetCode,
            IEncapsulatedOpenBISService service)
    {
        List<String> codeAsList = Collections.singletonList(dataSetCode);
        List<ExternalData> dataList = service.listDataSetsByCode(codeAsList);
        if (dataList == null || dataList.isEmpty())
        {
            return null;
        }

        ExternalData data = dataList.get(0);
        return ExternalDataTranslator.translateToDescription(data);
    }


    private static ArchiverTaskContext createArchiverContext()
    {
        File storeRoot = getPluginProviders().getStoreRoot();
        IShareIdManager shareIdManager = ServiceProvider.getShareIdManager();
        return new ArchiverTaskContext(new DataSetDirectoryProvider(storeRoot, shareIdManager));
    }

    private static IArchiverPlugin tryCreateArchiver()
    {
        PluginTaskProviders pluginProviders = getPluginProviders();
        ArchiverPluginFactory archiverFactory = pluginProviders.getArchiverPluginFactory();
        if (archiverFactory.isArchiverConfigured() == false)
        {
            return null;
        }

        return archiverFactory.createInstance(pluginProviders.getStoreRoot());
    }

    private static PluginTaskProviders getPluginProviders()
    {
        if (pluginTaskProviders == null)
        {
            pluginTaskProviders = PluginTaskProviders.create();
        }
        return pluginTaskProviders;
    }


    @Override
    public ICleanupTask createCleanupTask(final String dataSetCode)
    {
        return new ArchivingCleanupTask(dataSetCode);
    }

    private static class ArchivingCleanupTask implements ICleanupTask
    {
        private static final long serialVersionUID = 1L;

        private final String dataSetCode;

        ArchivingCleanupTask(String dataSetCode)
        {
            this.dataSetCode = dataSetCode;
        }

        public void cleanup()
        {
            // fetch the service from ServiceProvider, because after the deserialization we
            // we cannot rely on the field coming from the encapsulating class
            IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
            boolean statusUpdated = openBISService
                    .compareAndSetDataSetStatus(dataSetCode, BACKUP_PENDING, AVAILABLE, false);
            
            if (statusUpdated == false) {
                // invalid data set status, do not continue 
                return;
            }
            
            IArchiverPlugin archiver = tryCreateArchiver();
            DatasetDescription dataSet = tryGetDatasetDescription(dataSetCode, openBISService);
            if (archiver != null && dataSet != null)
            {
                // TODO KE: the API here is not optimal. refactor this on Tuesday.
                DeletedDataSet deletedDataset =
                        new DeletedDataSet(dataSetCode, dataSet.getDataSetLocation(), 0);
                List<DeletedDataSet> dataSetAsList = Collections.singletonList(deletedDataset);
                archiver.deleteFromArchive(dataSetAsList);
            }
        }
    }
}
