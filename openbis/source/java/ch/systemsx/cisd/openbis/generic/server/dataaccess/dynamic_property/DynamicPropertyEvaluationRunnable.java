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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.dynamic_property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationSchedulerWithQueue;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IFullTextIndexUpdateScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexUpdateOperation;
import ch.systemsx.cisd.openbis.generic.server.util.ShutdownManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;

/**
 * @author Piotr Buczek
 */
public final class DynamicPropertyEvaluationRunnable extends HibernateDaoSupport implements
        Runnable
{

    private static final int BATCH_SIZE = 1000;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DynamicPropertyEvaluationRunnable.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            DynamicPropertyEvaluationRunnable.class);

    private final IFullTextIndexUpdateScheduler fullTextIndexUpdateScheduler;

    private final IDynamicPropertyEvaluationSchedulerWithQueue evaluationQueue;

    private final IBatchDynamicPropertyEvaluator evaluator;

    private final ShutdownManager shutdownManager;

    public DynamicPropertyEvaluationRunnable(final SessionFactory sessionFactory,
            final IDAOFactory daoFactory,
            final IFullTextIndexUpdateScheduler fullTextIndexUpdateScheduler,
            final IDynamicPropertyEvaluationSchedulerWithQueue evaluationQueue,
            final IDynamicPropertyCalculatorFactory dynamicPropertyCalculatorFactory,
            final IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        this.fullTextIndexUpdateScheduler = fullTextIndexUpdateScheduler;
        this.evaluationQueue = evaluationQueue;
        setSessionFactory(sessionFactory);
        evaluator =
                new DefaultBatchDynamicPropertyEvaluator(BATCH_SIZE, daoFactory,
                        dynamicPropertyCalculatorFactory, managedPropertyEvaluatorFactory);
        shutdownManager = new ShutdownManager(operationLog);
    }

    //
    // Runnable
    //

    @Override
    @SuppressWarnings("unchecked")
    public final synchronized void run()
    {
        operationLog.info("Start dynamic properties evaluator queue.");
        try
        {
            while (shutdownManager.isShutdown() == false)
            {
                final DynamicPropertyEvaluationOperation operation = evaluationQueue.peekWait();
                shutdownManager.notReadyForShutdown();
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Update: " + operation);
                }
                final StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                Session session = null;
                Class<IEntityInformationWithPropertiesHolder> clazz = null;
                List<Long> modifiedIds = null;
                try
                {
                    clazz =
                            (Class<IEntityInformationWithPropertiesHolder>) Class.forName(operation
                                    .getClassName());
                    if (operation.isDeletion() == false)
                    {

                        session = getSessionFactory().openSession();

                        if (operation.getIds() == null)
                        {
                            modifiedIds = evaluator.doEvaluateProperties(session, clazz);
                        } else
                        {
                            List<Long> ids = new ArrayList<Long>(operation.getIds());
                            if (ids.isEmpty() == false)
                            {
                                fullTextIndexUpdateScheduler.scheduleUpdate(
                                        IndexUpdateOperation.reindex(clazz, ids));
                            }
                            // new collection is passed because it can be modified inside
                            modifiedIds = evaluator.doEvaluateProperties(session, clazz, ids);
                        }
                    }
                    stopWatch.stop();
                } catch (RuntimeException e)
                {
                    notificationLog.error("Error: " + operation + ".", e);
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                    if (operationLog.isInfoEnabled())
                    {
                        operationLog.info("Update of "
                                + (modifiedIds == null ? "" : modifiedIds.size() + " ")
                                + operation.getClassName() + "s took " + stopWatch);
                    }
                    if (clazz != null)
                    {
                        Collection<Long> ids =
                                operation.getIds() == null ? modifiedIds : operation.getIds();
                        if (ids.size() > 0)
                        {
                            IndexUpdateOperation indexUpdateOperation;
                            if (operation.isDeletion())
                            {
                                indexUpdateOperation =
                                        IndexUpdateOperation.remove(clazz, new LinkedList<Long>(ids));
                            } else
                            {
                                indexUpdateOperation =
                                        IndexUpdateOperation.reindex(clazz, new LinkedList<Long>(ids));
                            }
                            fullTextIndexUpdateScheduler.scheduleUpdate(indexUpdateOperation);
                        }
                    }
                }
                evaluationQueue.take();
                shutdownManager.readyForShutdown();
            }
        } catch (final InterruptedException e)
        {
            operationLog.warn(e);
        } catch (final Throwable th)
        {
            notificationLog
                    .error("A problem has occurred while evaluating dynamic properties.", th);
        } finally
        {
            operationLog.info("Evaluation closed");
        }
    }
}
