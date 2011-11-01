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
import java.util.List;

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

    /** temporary operation queues for every thread */
    private static final ThreadLocal<List<DynamicPropertyEvaluationOperation>> threadQueue =
            new ThreadLocal<List<DynamicPropertyEvaluationOperation>>()
                {
                    @Override
                    protected List<DynamicPropertyEvaluationOperation> initialValue()
                    {
                        return new ArrayList<DynamicPropertyEvaluationOperation>();
                    }
                };

    /* private - exposed for tests */
    public static List<DynamicPropertyEvaluationOperation> getThreadOperations()
    {
        return threadQueue.get();
    }

    private final IExtendedBlockingQueue<DynamicPropertyEvaluationOperation> evaluatorQueue;

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
                    .<DynamicPropertyEvaluationOperation> createSmartPersist(queueFile);
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
                    .<DynamicPropertyEvaluationOperation> createSmartPersist(queueFile);
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
        List<DynamicPropertyEvaluationOperation> threadOperations = getThreadOperations();
        threadOperations.add(operation);
    }

    public void synchronizeThreadQueue()
    {
        List<DynamicPropertyEvaluationOperation> threadOperations = getThreadOperations();
        if (threadOperations.size() > 0)
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

    public void clearThreadQueue()
    {
        threadDebugLog("Clearing scheduled operations");
        List<DynamicPropertyEvaluationOperation> threadOperations = getThreadOperations();
        threadOperations.clear();
    }

    void threadDebugLog(String msg)
    {
        if (operationLog.isDebugEnabled())
        {
            String threadPrefix = "[" + Thread.currentThread().hashCode() + "]: ";
            operationLog.debug(threadPrefix + msg);
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
