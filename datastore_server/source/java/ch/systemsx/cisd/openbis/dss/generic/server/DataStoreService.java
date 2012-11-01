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
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.etlserver.api.v1.PutDataSetService;
import ch.systemsx.cisd.openbis.common.conversation.context.ServiceConversationsThreadContext;
import ch.systemsx.cisd.openbis.common.conversation.progress.IServiceConversationProgressListener;
import ch.systemsx.cisd.openbis.common.spring.AbstractServiceWithLogger;
import ch.systemsx.cisd.openbis.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ArchiverPluginFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IPluginTaskInfoProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskProvider;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CustomImportFile;
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

    private final IPluginTaskInfoProvider pluginTaskInfoProvider;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    private IShareIdManager shareIdManager;

    private String cifexAdminUserOrNull;

    private String cifexAdminPasswordOrNull;

    private File storeRoot;

    private File commandQueueDirOrNull;

    private IDataSetCommandExecutor commandExecutor;

    private PutDataSetService putService;

    public DataStoreService(SessionTokenManager sessionTokenManager,
            MailClientParameters mailClientParameters, IPluginTaskInfoProvider pluginTaskParameters)
    {
        this(sessionTokenManager, new IDataSetCommandExecutorFactory()
            {
                @Override
                public IDataSetCommandExecutor create(File store, File queueDir)
                {
                    return new DataSetCommandExecutor(store, queueDir);
                }
            }, mailClientParameters, pluginTaskParameters);
    }

    DataStoreService(SessionTokenManager sessionTokenManager,
            IDataSetCommandExecutorFactory commandExecutorFactory,
            MailClientParameters mailClientParameters, IPluginTaskInfoProvider pluginTaskParameters)
    {
        this.sessionTokenManager = sessionTokenManager;
        this.commandExecutorFactory = commandExecutorFactory;
        this.mailClientParameters = mailClientParameters;
        this.pluginTaskInfoProvider = pluginTaskParameters;
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

    @Override
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

    @Override
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
                    @Override
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

    @Override
    public IDataStoreService createLogger(IInvocationLoggerContext context)
    {
        return new DataStoreServiceLogger(operationLog, context);
    }

    @Override
    public int getVersion(String sessionToken)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        return IDataStoreService.VERSION;
    }

    @Override
    public List<String> getKnownDataSets(String sessionToken,
            List<? extends IDatasetLocation> dataSets, boolean ignoreNonExistingLocation)
            throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        IServiceConversationProgressListener listener =
                ServiceConversationsThreadContext.getProgressListener();

        List<String> knownLocations = new ArrayList<String>();
        IShareIdManager manager = getShareIdManager();
        int index = 0;

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

            listener.update("getKnownDataSets", dataSets.size(), ++index);
        }
        return knownLocations;
    }

    @Override
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

    @Override
    public TableModel createReportFromDatasets(String sessionToken, String userSessionToken,
            String serviceKey, List<DatasetDescription> datasets, String userId,
            String userEmailOrNull)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        PluginTaskProvider<IReportingPluginTask> reportingPlugins =
                pluginTaskInfoProvider.getReportingPluginsProvider();
        IReportingPluginTask task = reportingPlugins.getPluginInstance(serviceKey);
        IShareIdManager manager = getShareIdManager();
        try
        {
            for (DatasetDescription dataSet : datasets)
            {
                manager.lock(dataSet.getDataSetCode());
            }
            IMailClient mailClient = createEMailClient();
            return task.createReport(
                    datasets,
                    new DataSetProcessingContext(getHierarchicalContentProvider(),
                            new DataSetDirectoryProvider(storeRoot, manager),
                            new SessionWorkspaceProvider(pluginTaskInfoProvider
                                    .getSessionWorkspaceRootDir(), userSessionToken),
                            new HashMap<String, String>(), mailClient, userId, userEmailOrNull,
                            userSessionToken));

        } finally
        {
            manager.releaseLocks();
        }
    }

    @Override
    public void processDatasets(String sessionToken, String userSessionToken, String serviceKey,
            List<DatasetDescription> datasets, Map<String, String> parameterBindings,
            String userId, String userEmailOrNull)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        PluginTaskProvider<IProcessingPluginTask> plugins =
                pluginTaskInfoProvider.getProcessingPluginsProvider();

        IProcessingPluginTask task = plugins.getPluginInstance(serviceKey);
        DatastoreServiceDescription pluginDescription = plugins.getPluginDescription(serviceKey);
        commandExecutor.scheduleProcessDatasets(task, datasets, parameterBindings, userId,
                userEmailOrNull, userSessionToken, pluginDescription, mailClientParameters);
    }

    @Override
    public void unarchiveDatasets(String sessionToken, String userSessionToken, List<DatasetDescription> datasets,
            String userId, String userEmailOrNull)
    {
        String description = "Unarchiving";
        IProcessingPluginTask task = new UnarchiveProcessingPluginTask(getArchiverPlugin());

        scheduleTask(sessionToken, userSessionToken, description, task, datasets, userId, userEmailOrNull);
    }

    @Override
    public void archiveDatasets(String sessionToken, String userSessionToken, List<DatasetDescription> datasets,
            String userId, String userEmailOrNull, boolean removeFromDataStore)
    {
        String description = removeFromDataStore ? "Archiving" : "Copying data sets to archive";
        IProcessingPluginTask task =
                new ArchiveProcessingPluginTask(getArchiverPlugin(), removeFromDataStore);

        scheduleTask(sessionToken, userSessionToken, description, task, datasets, userId, userEmailOrNull);
    }

    @Override
    public IArchiverPlugin getArchiverPlugin()
    {
        ArchiverPluginFactory factory = pluginTaskInfoProvider.getArchiverPluginFactory();
        return factory.createInstance(storeRoot);
    }

    @Override
    public IDataSetDirectoryProvider getDataSetDirectoryProvider()
    {
        return new DataSetDirectoryProvider(storeRoot, getShareIdManager());
    }

    @Override
    public TableModel createReportFromAggregationService(String sessionToken,
            String userSessionToken, String serviceKey, Map<String, Object> parameters,
            String userId, String userEmailOrNull)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);
        PluginTaskProvider<IReportingPluginTask> reportingPlugins =
                pluginTaskInfoProvider.getReportingPluginsProvider();
        IReportingPluginTask task = reportingPlugins.getPluginInstance(serviceKey);
        IShareIdManager manager = getShareIdManager();
        try
        {
            IMailClient mailClient = createEMailClient();
            return task.createAggregationReport(
                    parameters,
                    new DataSetProcessingContext(getHierarchicalContentProvider(),
                            new DataSetDirectoryProvider(storeRoot, manager),
                            new SessionWorkspaceProvider(pluginTaskInfoProvider
                                    .getSessionWorkspaceRootDir(), userSessionToken),
                            new HashMap<String, String>(), mailClient, userId, userEmailOrNull,
                            userSessionToken));

        } finally
        {
            manager.releaseLocks();
        }
    }

    private IMailClient createEMailClient()
    {
        return new MailClient(mailClientParameters);
    }

    private void scheduleTask(String sessionToken, String userSessionToken, String description,
            IProcessingPluginTask processingTask, List<DatasetDescription> datasets, String userId,
            String userEmailOrNull)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);
        DatastoreServiceDescription pluginDescription =
                DatastoreServiceDescription.processing(description, description, null, null);
        Map<String, String> parameterBindings = Collections.<String, String> emptyMap();
        commandExecutor.scheduleProcessDatasets(processingTask, datasets, parameterBindings,
                userId, userEmailOrNull, userSessionToken, pluginDescription, mailClientParameters);
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

        @Override
        public ProcessingStatus process(List<DatasetDescription> datasets,
                DataSetProcessingContext context)
        {
            ArchiverTaskContext archiverContext = createContext(context);
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

        @Override
        public ProcessingStatus process(List<DatasetDescription> datasets,
                DataSetProcessingContext context)
        {
            ArchiverTaskContext archiverContext = createContext(context);
            return archiverTask.unarchive(datasets, archiverContext);
        }
    }

    private static ArchiverTaskContext createContext(DataSetProcessingContext context)
    {
        return new ArchiverTaskContext(context.getDirectoryProvider(),
                context.getHierarchicalContentProvider());
    }

    @Override
    public LinkModel retrieveLinkFromDataSet(String sessionToken, String serviceKey,
            DatasetDescription dataSet)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        PluginTaskProvider<IReportingPluginTask> reportingPlugins =
                pluginTaskInfoProvider.getReportingPluginsProvider();
        IReportingPluginTask task = reportingPlugins.getPluginInstance(serviceKey);
        return task.createLink(dataSet);
    }

    @Override
    public IDataSetDeleter getDataSetDeleter()
    {
        return commandExecutor;
    }

    @Override
    public String putDataSet(String sessionToken, String dropboxName,
            CustomImportFile customImportFile)
    {
        PutDataSetService service = getPutDataSetService();
        return service.putDataSet(sessionToken, dropboxName, customImportFile);
    }

    @Override
    public void cleanupSession(String userSessionToken)
    {
        final File sessionWorkspace =
                new File(pluginTaskInfoProvider.getSessionWorkspaceRootDir(), userSessionToken);
        if (sessionWorkspace.exists())
        {
            QueueingPathRemoverService.removeRecursively(sessionWorkspace);
        }
    }

    private PutDataSetService getPutDataSetService()
    {
        if (putService == null)
        {
            this.putService =
                    new PutDataSetService(ServiceProvider.getOpenBISService(), operationLog);
            putService.setStoreDirectory(storeRoot);
        }
        return putService;
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
