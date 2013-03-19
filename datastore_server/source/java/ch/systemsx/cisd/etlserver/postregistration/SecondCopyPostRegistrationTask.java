/*
 * Copyright 2013 ETH Zuerich, CISD
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

import java.io.File;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.string.Template;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.AbstractArchiverProcessingPlugin;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.DataSetFileOperationsManager;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.IDataSetFileOperationsManager;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncArchiveCopierFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.RsyncArchiver;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.standard.SshCommandExecutorFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetStatusUpdater;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;

/**
 * Creates a second copy of the data set.
 *
 * @author Franz-Josef Elmer
 */
public class SecondCopyPostRegistrationTask extends AbstractPostRegistrationTaskForPhysicalDataSets
{

    private final DataSetFileOperationsManager fileOperationManager;
    private final IHierarchicalContentProvider hierarchicalContentProvider;
    private final IDataSetDirectoryProvider dataSetDirectoryProvider;
    private final IArchiverPlugin archiver;
    private final Template notificationTemplate;

    public SecondCopyPostRegistrationTask(Properties properties, IEncapsulatedOpenBISService service)
    {
        this(properties, service, ServiceProvider.getDataStoreService(), ServiceProvider
                .getHierarchicalContentProvider());
    }

    SecondCopyPostRegistrationTask(Properties properties, IEncapsulatedOpenBISService service,
            IDataStoreServiceInternal dataStoreService,
            IHierarchicalContentProvider hierarchicalContentProvider)
    {
        super(properties, service);
        this.hierarchicalContentProvider = hierarchicalContentProvider;
        fileOperationManager = new DataSetFileOperationsManager(properties,
                new RsyncArchiveCopierFactory(), new SshCommandExecutorFactory());
        if (fileOperationManager.isHosted())
        {
            throw new ConfigurationFailureException(
                    "Destination should be on a local or mounted drive.");
        }
        dataSetDirectoryProvider = dataStoreService.getDataSetDirectoryProvider();
        File storeRoot = dataSetDirectoryProvider.getStoreRoot();
        properties.setProperty(AbstractArchiverProcessingPlugin.SYNCHRONIZE_ARCHIVE, "false");
        notificationTemplate =
                new Template(
                        "Creating a second copy of dataset '${dataSet}' has failed.\n${errors}");
        archiver =
                new Archiver(properties, storeRoot, service, fileOperationManager, dataStoreService
                        .getDataSetDirectoryProvider().getShareIdManager());
    }

    @Override
    public IPostRegistrationTaskExecutor createExecutor(String dataSetCode)
    {
        return new ArchivingExecutor(dataSetCode, false, notificationTemplate, service, archiver, dataSetDirectoryProvider,
                hierarchicalContentProvider);
    }
    
    private static final class Archiver extends RsyncArchiver
    {

        private static final long serialVersionUID = 1L;

        Archiver(Properties properties, File storeRoot, IEncapsulatedOpenBISService service,
                IDataSetFileOperationsManager fileOperationsManager, IShareIdManager shareIdManager)
        {
            super(properties, storeRoot, fileOperationsManager, RsyncArchiver.DeleteAction.DELETE,
                    ChecksumVerificationCondition.IF_AVAILABLE);
            setService(service);
            setStatusUpdater(new IDataSetStatusUpdater()
                {
                    @Override
                    public void update(List<String> dataSetCodes, DataSetArchivingStatus status,
                            boolean presentInArchive)
                    {
                    }
                });
            setShareIdManager(shareIdManager);
        }

    }

}
