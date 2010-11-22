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

package ch.systemsx.cisd.openbis.generic.server.business.bo.dynamic_property;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.DynamicPropertyEvaluationOperation;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDynamicPropertyEvaluationSchedulerWithQueue;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IFullTextIndexUpdateScheduler;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IndexUpdateOperation;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityInformationWithPropertiesHolder;

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

    public DynamicPropertyEvaluationRunnable(final SessionFactory sessionFactory,
            final IDAOFactory daoFactory,
            final IFullTextIndexUpdateScheduler fullTextIndexUpdateScheduler,
            final IDynamicPropertyEvaluationSchedulerWithQueue evaluationQueue)
    {
        this.fullTextIndexUpdateScheduler = fullTextIndexUpdateScheduler;
        this.evaluationQueue = evaluationQueue;
        setSessionFactory(sessionFactory);
        evaluator = new DefaultBatchDynamicPropertyEvaluator(BATCH_SIZE, daoFactory);
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
                final DynamicPropertyEvaluationOperation operation = evaluationQueue.peekWait();
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
                    session = getSession();

                    if (operation.getIds() == null)
                    {
                        modifiedIds = evaluator.doEvaluateProperties(session, clazz);
                    } else
                    {
                        // new collection is passed because it can be modified inside
                        modifiedIds =
                                evaluator.doEvaluateProperties(session, clazz, new ArrayList<Long>(
                                        operation.getIds()));
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
                                + (modifiedIds == null ? "" : modifiedIds.size() + " ")
                                + operation.getClassName() + "s took " + stopWatch);
                    }
                    if (clazz != null)
                    {
                        List<Long> ids =
                                operation.getIds() == null ? modifiedIds : operation.getIds();
                        if (ids.size() > 0)
                        {
                            IndexUpdateOperation indexUpdateOperation =
                                    IndexUpdateOperation.reindex(clazz, ids);
                            fullTextIndexUpdateScheduler.scheduleUpdate(indexUpdateOperation);
                        }
                    }
                }
                evaluationQueue.take();
            }
        } catch (final Throwable th)
        {
            notificationLog
                    .error("A problem has occurred while evaluating dynamic properties.", th);
        }
    }
}
