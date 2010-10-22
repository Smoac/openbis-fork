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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property;

import java.io.File;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ch.systemsx.cisd.common.collections.ExtendedBlockingQueueFactory;
import ch.systemsx.cisd.common.collections.IExtendedBlockingQueue;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;

/**
 * @author Piotr Buczek
 */
public final class DynamicPropertyEvaluationRunnable extends HibernateDaoSupport implements
        IDynamicPropertyEvaluationScheduler, Runnable
{

    private static final int BATCH_SIZE = 1000;

    public final static String DYNAMIC_PROPERTY_EVALUATOR_QUEUE_FILENAME =
            ".dynamic_property_evaluator_queue";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DynamicPropertyEvaluationRunnable.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            DynamicPropertyEvaluationRunnable.class);

    private final IDynamicPropertyEvaluator evaluator;

    private final IExtendedBlockingQueue<DynamicPropertyEvaluationOperation> evaluatorQueue;

    public DynamicPropertyEvaluationRunnable(final SessionFactory sessionFactory)
    {
        setSessionFactory(sessionFactory);
        evaluator = new DefaultDynamicPropertyEvaluator(BATCH_SIZE);

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
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Scheduling update: " + operation);
        }
        evaluatorQueue.add(operation);
    }

    //
    // Runnable
    //

    @SuppressWarnings("unchecked")
    public final void run()
    {
        try
        {
            while (true)
            {
                final DynamicPropertyEvaluationOperation operation = evaluatorQueue.peekWait();
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Update: " + operation);
                }
                final StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                Session session = null;
                try
                {
                    final Class<IEntityInformationWithPropertiesHolder> clazz =
                            (Class<IEntityInformationWithPropertiesHolder>) Class.forName(operation
                                    .getClassName());
                    session = getSession();

                    if (operation.getIds() == null)
                    {
                        evaluator.doEvaluateProperties(session, clazz);
                    } else
                    {
                        evaluator.doEvaluateProperties(session, clazz, operation.getIds());
                    }
                    stopWatch.stop();
                } catch (RuntimeException e)
                {
                    notificationLog.error("Error: " + operation + ".", e);
                } finally
                {
                    if (session != null)
                    {
                        releaseSession(session);
                    }
                    if (operationLog.isInfoEnabled())
                    {
                        operationLog.info("Update of "
                                + (operation.getIds() == null ? "" : operation.getIds().size()
                                        + " ") + operation.getClassName() + "s took " + stopWatch);
                    }
                }
                evaluatorQueue.take();
            }
        } catch (final Throwable th)
        {
            notificationLog
                    .error("A problem has occurred while evaluating dynamic properties.", th);
        }
    }

}
