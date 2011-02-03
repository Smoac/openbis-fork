/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.collections.ExtendedBlockingQueueFactory;
import ch.systemsx.cisd.common.collections.IExtendedBlockingQueue;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * @author Piotr Buczek
 */
public final class DynamicPropertyEvaluationScheduler implements
        IDynamicPropertyEvaluationSchedulerWithQueue
{
    public final static String DYNAMIC_PROPERTY_EVALUATOR_QUEUE_FILENAME =
            ".dynamic_property_evaluator_queue";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DynamicPropertyEvaluationScheduler.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            DynamicPropertyEvaluationScheduler.class);

    private final IExtendedBlockingQueue<DynamicPropertyEvaluationOperation> evaluatorQueue;

    /** temporary operation queues for every thread */
    private Map<Thread, List<DynamicPropertyEvaluationOperation>> operationsByThread =
            new HashMap<Thread, List<DynamicPropertyEvaluationOperation>>();

    public DynamicPropertyEvaluationScheduler()
    {
        final File queueFile = getEvaluatorQueueFile();
        operationLog.info(String.format("Evaluator queue file: %s.", queueFile.getAbsolutePath()));
        evaluatorQueue = createEvaluatorQueue(queueFile);
    }

    private static IExtendedBlockingQueue<DynamicPropertyEvaluationOperation> createEvaluatorQueue(
            final File queueFile)
    {
        try
        {
            return ExtendedBlockingQueueFactory
                    .<DynamicPropertyEvaluationOperation> createPersistRecordBased(queueFile);
        } catch (RuntimeException e)
        {
            // don't fail if e.g. deserialization of the queue fails (see SE-286)
            String newFileName =
                    DYNAMIC_PROPERTY_EVALUATOR_QUEUE_FILENAME + "_" + System.currentTimeMillis();
            notificationLog.error(String.format("%s.\n "
                    + "Renaming '%s' to '%s' and using an empty queue file. "
                    + "Restart server with the queue that caused the problem or "
                    + "wait for maintenance task to reevaluate all properties.", e.getMessage(),
                    queueFile, newFileName));
            queueFile.renameTo(new File(newFileName));
            return ExtendedBlockingQueueFactory
                    .<DynamicPropertyEvaluationOperation> createPersistRecordBased(queueFile);
        }
    }

    private static File getEvaluatorQueueFile()
    {
        return new File(DYNAMIC_PROPERTY_EVALUATOR_QUEUE_FILENAME);
    }

    public void clear()
    {
        evaluatorQueue.clear();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Cleared evaluator queue.");
        }
    }

    public void scheduleUpdate(DynamicPropertyEvaluationOperation operation)
    {
        threadDebugLog("Scheduling update: " + operation);
        List<DynamicPropertyEvaluationOperation> threadOperations =
                operationsByThread.get(Thread.currentThread());
        if (threadOperations == null)
        {
            threadOperations = new ArrayList<DynamicPropertyEvaluationOperation>();
            operationsByThread.put(Thread.currentThread(), threadOperations);
        }
        threadOperations.add(operation);
    }

    public void synchronize()
    {
        List<DynamicPropertyEvaluationOperation> threadOperations =
                operationsByThread.get(Thread.currentThread());
        if (threadOperations != null && threadOperations.size() > 0)
        {
            threadDebugLog("Synchronizing scheduled operations");
            for (DynamicPropertyEvaluationOperation operation : threadOperations)
            {
                evaluatorQueue.add(operation);
            }
            threadOperations.clear();
        } else
        {
            threadDebugLog("Nothing to synchronize");
        }
    }

    void threadDebugLog(String msg)
    {
        if (operationLog.isDebugEnabled())
        {
            String threadPrefix = "[" + Thread.currentThread().hashCode() + "]: ";
            operationLog.info(threadPrefix + msg);
        }
    }

    public DynamicPropertyEvaluationOperation peekWait() throws InterruptedException
    {
        return evaluatorQueue.peekWait();
    }

    public DynamicPropertyEvaluationOperation take() throws InterruptedException
    {
        return evaluatorQueue.take();
    }

}
