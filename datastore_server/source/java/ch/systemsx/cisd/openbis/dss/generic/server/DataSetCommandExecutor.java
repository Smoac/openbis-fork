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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.collection.IExtendedBlockingQueue;
import ch.systemsx.cisd.common.io.PersistentExtendedBlockingQueueFactory;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IHierarchicalContentProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;

/**
 * @author Franz-Josef Elmer
 */
class DataSetCommandExecutor implements IDataSetCommandExecutor
{
    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetCommandExecutor.class);

    private final static Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            DataSetCommandExecutor.class);

    private final File store;

    private final IExtendedBlockingQueue<IDataSetCommand> commandQueue;

    private IShareIdManager shareIdManager;

    private IHierarchicalContentProvider hierarchicalContentProvider;

    public DataSetCommandExecutor(File store, File queueDir)
    {
        this.store = store;
        File queueFile = getCommandQueueFile(queueDir);
        commandQueue = PersistentExtendedBlockingQueueFactory.<IDataSetCommand> createSmartPersist(queueFile);
    }

    void setShareIdManager(IShareIdManager shareIdManager)
    {
        this.shareIdManager = shareIdManager;
    }

    private static File getCommandQueueFile(File store)
    {
        return new File(store, "commandQueue");
    }

    @Override
    public void start()
    {
        Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    String description = "?";
                    try
                    {
                        while (true)
                        {
                            final IDataSetCommand command = commandQueue.peekWait();
                            description = command.getDescription();
                            if (operationLog.isInfoEnabled())
                            {
                                operationLog.info("Executing " + description);
                            }
                            final StopWatch stopWatch = new StopWatch();
                            stopWatch.start();
                            try
                            {
                                IShareIdManager manager = getShareIdManager();
                                manager.lock(command.getDataSetCodes());
                                command.execute(getHierarchicalContentProvider(),
                                        new DataSetDirectoryProvider(store, manager));
                            } catch (RuntimeException e)
                            {
                                notificationLog.error("Error executing command '" + description
                                        + "'.", e);
                            } finally
                            {
                                getShareIdManager().releaseLocks();
                            }
                            if (operationLog.isInfoEnabled())
                            {
                                operationLog.info("Finished executing " + description + " after "
                                        + stopWatch);
                            }
                            commandQueue.take();
                        }
                    } catch (InterruptedException ex)
                    {
                        // Exit thread.
                    } catch (InterruptedExceptionUnchecked ex)
                    {
                        // Exit thread.
                    }
                    operationLog.info("Executor stopped");
                }
            }, "Data Set Command Execution");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void scheduleDeletionOfDataSets(List<? extends IDatasetLocation> dataSets,
            int maxNumberOfRetries, long waitingTimeBetweenRetries)
    {
        scheduleCommand(new DeletionCommand(dataSets, maxNumberOfRetries, waitingTimeBetweenRetries));
    }

    @Override
    public void scheduleUploadingDataSetsToCIFEX(ICIFEXRPCServiceFactory cifexServiceFactory,
            MailClientParameters mailClientParameters, List<AbstractExternalData> dataSets,
            DataSetUploadContext uploadContext, String cifexAdminUserOrNull,
            String cifexAdminPasswordOrNull)
    {
        scheduleCommand(new UploadingCommand(cifexServiceFactory, mailClientParameters, dataSets,
                uploadContext, cifexAdminUserOrNull, cifexAdminPasswordOrNull));
    }

    @Override
    public void scheduleProcessDatasets(IProcessingPluginTask task,
            List<DatasetDescription> datasets, Map<String, String> parameterBindings,
            String userId, String userEmailOrNull, String sessionTokenOrNull,
            DatastoreServiceDescription serviceDescription,
            MailClientParameters mailClientParameters)
    {
        scheduleCommand(new ProcessDatasetsCommand(task, datasets, parameterBindings, userId,
                userEmailOrNull, sessionTokenOrNull, serviceDescription, mailClientParameters));
    }

    private void scheduleCommand(IDataSetCommand command)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Scheduling " + command);
        }
        commandQueue.add(command);
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

    /**
     * Writes the list of items in the command store of the given <var>store</var> directory to
     * stdout.
     */
    public static void listQueuedCommands(File store)
    {
        final File queueFile = getCommandQueueFile(store);
        final IExtendedBlockingQueue<IDataSetCommand> commandQueue =
                PersistentExtendedBlockingQueueFactory.<IDataSetCommand> createSmartPersist(queueFile);
        if (commandQueue.isEmpty())
        {
            System.out.println("Command queue is empty.");
        } else
        {
            System.out.println("Found " + commandQueue.size() + " items in command queue:");
            for (final IDataSetCommand cmd : commandQueue)
            {
                try
                {
                    System.out.println(cmd.getDescription());
                } catch (RuntimeException ex)
                {
                    System.err.printf("Error showing description of command '%s':\n", cmd
                            .getClass().getSimpleName());
                    ex.printStackTrace();
                }
            }
        }
    }

}
