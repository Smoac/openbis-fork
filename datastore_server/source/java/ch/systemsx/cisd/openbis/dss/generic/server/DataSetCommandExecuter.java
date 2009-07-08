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

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.collections.ExtendedBlockingQueueFactory;
import ch.systemsx.cisd.common.collections.IExtendedBlockingQueue;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

/**
 * @author Franz-Josef Elmer
 */
class DataSetCommandExecuter implements IDataSetCommandExecutor
{
    private final File store;

    private final IExtendedBlockingQueue<IDataSetCommand> commandQueue;

    private final Logger operationLog;

    public DataSetCommandExecuter(File store)
    {
        this.store = store;
        File queueFile = new File(store, "commandQueue");
        commandQueue =
                ExtendedBlockingQueueFactory.<IDataSetCommand> createPersistRecordBased(queueFile);
        operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());
    }

    public void start()
    {
        Thread thread = new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        while (true)
                        {
                            IDataSetCommand command = commandQueue.peekWait();
                            if (operationLog.isDebugEnabled())
                            {
                                operationLog.debug("Executing " + command);
                            }
                            StopWatch stopWatch = new StopWatch();
                            command.execute(store);
                            if (operationLog.isDebugEnabled())
                            {
                                operationLog.debug("Finished " + command + " after " + stopWatch);
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
                }
            }, "Data Set Comamand Execution");
        thread.setDaemon(true);
        thread.start();
    }

    public void scheduleDeletionOfDataSets(List<String> locations)
    {
        scheduleCommand(new DeletionCommand(locations));
    }

    public void scheduleUploadingDataSetsToCIFEX(ICIFEXRPCServiceFactory cifexServiceFactory,
            MailClientParameters mailClientParameters, List<ExternalDataPE> dataSets,
            DataSetUploadContext uploadContext)
    {
        scheduleCommand(new UploadingCommand(cifexServiceFactory, mailClientParameters, dataSets,
                uploadContext));
    }

    public void scheduleProcessDatasets(IProcessingPluginTask task,
            List<DatasetDescription> datasets)
    {
        scheduleCommand(new ProcessDatasetsCommand(task, datasets));
    }

    private void scheduleCommand(IDataSetCommand command)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Scheduling " + command);
        }
        commandQueue.add(command);
    }
}
