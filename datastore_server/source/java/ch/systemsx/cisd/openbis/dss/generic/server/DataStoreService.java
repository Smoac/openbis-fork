/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.common.spring.AbstractServiceWithLogger;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ArchiverPluginFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskProviders;
import ch.systemsx.cisd.openbis.dss.generic.shared.ArchiverTaskContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.Constants;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IArchiverPlugin;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDeleter;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.util.UuidUtil;

/**
 * Implementation of {@link IDataStoreService} which will be accessed remotely by the openBIS
 * server.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreService extends AbstractServiceWithLogger<IDataStoreService> implements
        IDataStoreServiceInternal, InitializingBean
{
    private final SessionTokenManager sessionTokenManager;

    private final IDataSetCommandExecutorFactory commandExecutorFactory;

    private final MailClientParameters mailClientParameters;

    private final PluginTaskProviders pluginTaskParameters;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    private IShareIdManager shareIdManager;

    private String cifexAdminUserOrNull;

    private String cifexAdminPasswordOrNull;

    private File storeRoot;

    private File commandQueueDirOrNull;

    private IDataSetCommandExecutor commandExecutor;

    public DataStoreService(SessionTokenManager sessionTokenManager,
            MailClientParameters mailClientParameters, PluginTaskProviders pluginTaskParameters)
    {
        this(sessionTokenManager, new IDataSetCommandExecutorFactory()
            {
                public IDataSetCommandExecutor create(File store, File queueDir)
                {
                    return new DataSetCommandExecutor(store, queueDir);
                }
            }, mailClientParameters, pluginTaskParameters);
    }

    DataStoreService(SessionTokenManager sessionTokenManager,
            IDataSetCommandExecutorFactory commandExecutorFactory,
            MailClientParameters mailClientParameters, PluginTaskProviders pluginTaskParameters)
    {
        this.sessionTokenManager = sessionTokenManager;
        this.commandExecutorFactory = commandExecutorFactory;
        this.mailClientParameters = mailClientParameters;
        this.pluginTaskParameters = pluginTaskParameters;
        storeRoot = pluginTaskParameters.getStoreRoot();
    }

    void setShareIdManager(IShareIdManager shareIdManager)
    {
        this.shareIdManager = shareIdManager;
    }

    public final void setCommandQueueDir(String queueDirOrNull)
    {
        if (StringUtils.isBlank(queueDirOrNull))
        {
            this.commandQueueDirOrNull = null;
        } else
        {
            this.commandQueueDirOrNull = new File(queueDirOrNull);
        }
    }

    public void setCifexAdminUserOrNull(String cifexAdminUserOrNull)
    {
        this.cifexAdminUserOrNull = cifexAdminUserOrNull;
    }

    public void setCifexAdminPasswordOrNull(String cifexAdminPasswordOrNull)
    {
        this.cifexAdminPasswordOrNull = cifexAdminPasswordOrNull;
    }

    public void afterPropertiesSet()
    {
        String prefix = "Property 'storeRoot' ";
        if (storeRoot == null)
        {
            throw new IllegalStateException(prefix + "not set.");
        }
        String storeRootPath = storeRoot.getAbsolutePath();
        if (storeRoot.isFile())
        {
            throw new IllegalArgumentException(prefix + "is a file instead of a directory: "
                    + storeRootPath);
        }
        if (storeRoot.exists() == false)
        {
            if (storeRoot.mkdirs() == false)
            {
                throw new IOExceptionUnchecked(new IOException(
                        "Couldn't create root directory of the data store: " + storeRootPath));
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Creates root directory of the data store: " + storeRootPath);
            }
        }
        if (commandQueueDirOrNull == null)
        {
            commandQueueDirOrNull = storeRoot;
        }
        commandExecutor = commandExecutorFactory.create(storeRoot, commandQueueDirOrNull);
        migrateStore();
    }

    public void initialize()
    {
        commandExecutor.start();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Command executor started.");
        }
        getShareIdManager().isKnown(""); // initializes ShareIdManager: reading all share ids from
                                         // the data base
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Initialization finished.");
        }

    }

    private void migrateStore()
    {
        File defaultShare = new File(storeRoot, Constants.DEFAULT_SHARE_ID);
        if (defaultShare.exists() == false)
        {
            if (defaultShare.mkdirs() == false)
            {
                throw new IOExceptionUnchecked(new IOException(
                        "Couldn't create default share in data store: " + defaultShare));
            }
            File[] stores = storeRoot.listFiles(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        return UuidUtil.isValidUUID(name);
                    }
                });
            for (File file : stores)
            {
                if (file.renameTo(new File(defaultShare, file.getName())) == false)
                {
                    throw new IOExceptionUnchecked(new IOException("Couldn't move '" + file
                            + "' into default share '" + defaultShare + "'."));
                }
            }
            operationLog.info("Store migrated to default share");
        }
    }

    public IDataStoreService createLogger(IInvocationLoggerContext context)
    {
        return new DataStoreServiceLogger(operationLog, context);
    }

    public int getVersion(String sessionToken)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        return IDataStoreService.VERSION;
    }

    public List<String> getKnownDataSets(String sessionToken,
            List<? extends IDatasetLocation> dataSets) throws InvalidAuthenticationException
    {
        return getKnownDataSets(sessionToken, dataSets, false);
    }

    public List<String> getKnownDataSets(String sessionToken,
            List<? extends IDatasetLocation> dataSets, boolean ignoreNonExistingLocation)
            throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        List<String> knownLocations = new ArrayList<String>();
        IShareIdManager manager = getShareIdManager();
        for (IDatasetLocation dataSet : dataSets)
        {
            String datasetCode = dataSet.getDataSetCode();
            String location = dataSet.getDataSetLocation();
            manager.lock(datasetCode);
            try
            {
                if (manager.isKnown(datasetCode)
                        && (ignoreNonExistingLocation || new File(new File(storeRoot,
                                manager.getShareId(datasetCode)), location).exists()))
                {
                    knownLocations.add(location);
                }
            } finally
            {
                manager.releaseLock(datasetCode);
            }
        }
        return knownLocations;
    }

    public void deleteDataSets(String sessionToken, final List<? extends IDatasetLocation> dataSets)
            throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        commandExecutor.scheduleDeletionOfDataSets(dataSets);
    }

    public void uploadDataSetsToCIFEX(String sessionToken, List<ExternalData> dataSets,
            DataSetUploadContext context) throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        if (context.getCifexURL() == null)
        {
            throw new ConfigurationFailureException(
                    "Upload of data sets to CIFEX not possible: no CIFEX URL configured.");
        }
        ICIFEXRPCServiceFactory serviceFactory =
                createCIFEXRPCServiceFactory(context.getCifexURL());
        ICIFEXComponent cifex = serviceFactory.createCIFEXComponent();
        String userID = context.getUserID();
        String password = context.getPassword();
        if (UploadingCommand.canLoginToCIFEX(cifex, context.isUserAuthenticated(), userID,
                password, cifexAdminUserOrNull, cifexAdminPasswordOrNull) == false)
        {
            throw new InvalidSessionException("User failed to be authenticated by CIFEX.");
        }
        commandExecutor.scheduleUploadingDataSetsToCIFEX(serviceFactory, mailClientParameters,
                dataSets, context, cifexAdminUserOrNull, cifexAdminPasswordOrNull);
    }

    protected ICIFEXRPCServiceFactory createCIFEXRPCServiceFactory(String cifexURL)
    {
        return new CIFEXRPCServiceFactory(cifexURL);
    }

    public TableModel createReportFromDatasets(String sessionToken, String userSessionToken,
            String serviceKey, List<DatasetDescription> datasets)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        PluginTaskProvider<IReportingPluginTask> reportingPlugins =
                pluginTaskParameters.getReportingPluginsProvider();
        IReportingPluginTask task = reportingPlugins.getPluginInstance(serviceKey);
        IShareIdManager manager = getShareIdManager();
        for (DatasetDescription dataSet : datasets)
        {
            manager.lock(dataSet.getDataSetCode());
        }
        try
        {
            return task.createReport(datasets, new DataSetProcessingContext(
                    getHierarchicalContentProvider(), new DataSetDirectoryProvider(storeRoot,
                            manager), new HashMap<String, String>(), null, null, userSessionToken));

        } finally
        {
            manager.releaseLocks();
        }
    }

    public void processDatasets(String sessionToken, String userSessionToken, String serviceKey,
            List<DatasetDescription> datasets, Map<String, String> parameterBindings,
            String userEmailOrNull)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        PluginTaskProvider<IProcessingPluginTask> plugins =
                pluginTaskParameters.getProcessingPluginsProvider();

        IProcessingPluginTask task = plugins.getPluginInstance(serviceKey);
        DatastoreServiceDescription pluginDescription = plugins.getPluginDescription(serviceKey);
        commandExecutor.scheduleProcessDatasets(task, datasets, parameterBindings, userEmailOrNull,
                userSessionToken, pluginDescription, mailClientParameters);
    }

    public void unarchiveDatasets(String sessionToken, List<DatasetDescription> datasets,
            String userEmailOrNull)
    {
        String description = "Unarchiving";
        IProcessingPluginTask task = new UnarchiveProcessingPluginTask(getArchiverPlugin());

        scheduleTask(sessionToken, description, task, datasets, userEmailOrNull);
    }

    public void archiveDatasets(String sessionToken, List<DatasetDescription> datasets,
            String userEmailOrNull, boolean removeFromDataStore)
    {
        String description = removeFromDataStore ? "Archiving" : "Copying data sets to archive";
        IProcessingPluginTask task =
                new ArchiveProcessingPluginTask(getArchiverPlugin(), removeFromDataStore);

        scheduleTask(sessionToken, description, task, datasets, userEmailOrNull);
    }

    public IArchiverPlugin getArchiverPlugin()
    {
        ArchiverPluginFactory factory = pluginTaskParameters.getArchiverPluginFactory();
        return factory.createInstance(storeRoot);
    }

    public IDataSetDirectoryProvider getDataSetDirectoryProvider()
    {
        return new DataSetDirectoryProvider(storeRoot, getShareIdManager());
    }

    private void scheduleTask(String sessionToken, String description,
            IProcessingPluginTask processingTask, List<DatasetDescription> datasets,
            String userEmailOrNull)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);
        DatastoreServiceDescription pluginDescription =
                DatastoreServiceDescription.processing(description, description, null, null);
        Map<String, String> parameterBindings = Collections.<String, String> emptyMap();
        commandExecutor.scheduleProcessDatasets(processingTask, datasets, parameterBindings,
                userEmailOrNull, sessionToken, pluginDescription, mailClientParameters);
    }

    private static class ArchiveProcessingPluginTask implements IProcessingPluginTask
    {

        private static final long serialVersionUID = 1L;

        private IArchiverPlugin archiverTask;

        private boolean removeFromDataStore;

        public ArchiveProcessingPluginTask(final IArchiverPlugin archiverTask,
                final boolean removeFromDataStore)
        {
            this.archiverTask = archiverTask;
            this.removeFromDataStore = removeFromDataStore;
        }

        public ProcessingStatus process(List<DatasetDescription> datasets,
                DataSetProcessingContext context)
        {
            ArchiverTaskContext archiverContext =
                    new ArchiverTaskContext(context.getDirectoryProvider());
            return archiverTask.archive(datasets, archiverContext, removeFromDataStore);
        }
    }

    private static class UnarchiveProcessingPluginTask implements IProcessingPluginTask
    {

        private static final long serialVersionUID = 1L;

        private IArchiverPlugin archiverTask;

        public UnarchiveProcessingPluginTask(final IArchiverPlugin archiverTask)
        {
            this.archiverTask = archiverTask;
        }

        public ProcessingStatus process(List<DatasetDescription> datasets,
                DataSetProcessingContext context)
        {
            ArchiverTaskContext archiverContext =
                    new ArchiverTaskContext(context.getDirectoryProvider());
            return archiverTask.unarchive(datasets, archiverContext);
        }
    }

    public LinkModel retrieveLinkFromDataSet(String sessionToken, String serviceKey,
            DatasetDescription dataSet)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        PluginTaskProvider<IReportingPluginTask> reportingPlugins =
                pluginTaskParameters.getReportingPluginsProvider();
        IReportingPluginTask task = reportingPlugins.getPluginInstance(serviceKey);
        return task.createLink(dataSet);
    }

    public IDataSetDeleter getDataSetDeleter()
    {
        return commandExecutor;
    }

    private IShareIdManager getShareIdManager()
    {
        if (shareIdManager == null)
        {
            shareIdManager = ServiceProvider.getShareIdManager();
        }
        return shareIdManager;
    }

    private IHierarchicalContentProvider getHierarchicalContentProvider()
    {
        if (hierarchicalContentProvider == null)
        {
            hierarchicalContentProvider = ServiceProvider.getHierarchicalContentProvider();
        }
        return hierarchicalContentProvider;
    }
}
