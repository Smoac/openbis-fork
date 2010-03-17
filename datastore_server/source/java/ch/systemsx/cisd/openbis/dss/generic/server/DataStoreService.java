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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.spring.AbstractServiceWithLogger;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IReportingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskProvider;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskProviders;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * Implementation of {@link IDataStoreService} which will be accessed remotely by the opneBIS
 * server.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreService extends AbstractServiceWithLogger<IDataStoreService> implements
        IDataStoreService, InitializingBean
{
    private final SessionTokenManager sessionTokenManager;

    private final IDataSetCommandExecutorFactory commandExecutorFactory;

    private final MailClientParameters mailClientParameters;

    private final PluginTaskProviders pluginTaskParameters;

    private String cifexAdminUserOrNull;

    private String cifexAdminPasswordOrNull;

    private File storeRoot;

    private File commandQueueDirOrNull;

    private IDataSetCommandExecutor commandExecuter;

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
    }

    public final void setStoreRoot(File storeRoot)
    {
        this.storeRoot = storeRoot;
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
        commandExecuter = commandExecutorFactory.create(storeRoot, commandQueueDirOrNull);
        commandExecuter.start();
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

    public List<String> getKnownDataSets(String sessionToken, List<String> dataSetLocations)
            throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        List<String> knownLocations = new ArrayList<String>();
        for (String location : dataSetLocations)
        {
            if (new File(storeRoot, location).exists())
            {
                knownLocations.add(location);
            }
        }
        return knownLocations;
    }

    public void deleteDataSets(String sessionToken, final List<String> dataSetLocations)
            throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        commandExecuter.scheduleDeletionOfDataSets(dataSetLocations);
    }

    public void uploadDataSetsToCIFEX(String sessionToken, List<ExternalData> dataSets,
            DataSetUploadContext context) throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

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
        commandExecuter.scheduleUploadingDataSetsToCIFEX(serviceFactory, mailClientParameters,
                dataSets, context, cifexAdminUserOrNull, cifexAdminPasswordOrNull);
    }

    protected ICIFEXRPCServiceFactory createCIFEXRPCServiceFactory(String cifexURL)
    {
        return new CIFEXRPCServiceFactory(cifexURL);
    }

    public TableModel createReportFromDatasets(String sessionToken, String serviceKey,
            List<DatasetDescription> datasets)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        PluginTaskProvider<IReportingPluginTask> reportingPlugins =
                pluginTaskParameters.getReportingPluginsProvider();
        IReportingPluginTask task = reportingPlugins.createPluginInstance(serviceKey, storeRoot);
        return task.createReport(datasets);
    }

    public void processDatasets(String sessionToken, String serviceKey,
            List<DatasetDescription> datasets, String userEmailOrNull)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        PluginTaskProvider<IProcessingPluginTask> plugins =
                pluginTaskParameters.getProcessingPluginsProvider();

        IProcessingPluginTask task = plugins.createPluginInstance(serviceKey, storeRoot);
        DatastoreServiceDescription pluginDescription = plugins.getPluginDescription(serviceKey);
        commandExecuter.scheduleProcessDatasets(task, datasets, userEmailOrNull, pluginDescription,
                mailClientParameters);
    }

    public void unarchiveDatasets(String sessionToken, List<DatasetDescription> datasets,
            String userEmailOrNull)
    {
        // TODO implement (code below is just for testing)
        System.err.println("Unarchiving:");
        for (DatasetDescription d : datasets)
        {
            System.err.println(d.getDatasetCode() + " " + d.getDataSetLocation());
        }
    }

    public void archiveDatasets(String sessionToken, List<DatasetDescription> datasets,
            String userEmailOrNull)
    {
        // TODO implement (code below is just for testing)
        System.err.println("Archiving:");
        for (DatasetDescription d : datasets)
        {
            System.err.println(d.getDatasetCode() + " " + d.getDataSetLocation());
        }
    }

}
