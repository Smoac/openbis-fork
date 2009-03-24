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

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collections.ExtendedBlockingQueueFactory;
import ch.systemsx.cisd.common.collections.IExtendedBlockingQueue;
import ch.systemsx.cisd.common.exceptions.StopException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * 
 *
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
                    } catch (StopException ex)
                    {
                        // Exit thread.
                    }
                }
            }, "Data Set Comamand Execution");
        thread.setDaemon(true);
        thread.start();
    }



    public void scheduleCommand(IDataSetCommand command)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Scheduling " + command);
        }
        commandQueue.add(command);
    }
}
