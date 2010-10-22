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

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.IFullTextIndexUpdateScheduler;

/**
 * @author Piotr Buczek
 */
public final class DummyDynamicPropertyEvaluationRunnable extends HibernateDaoSupport implements
        IDynamicPropertyEvaluationScheduler, Runnable
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DummyDynamicPropertyEvaluationRunnable.class);

    public DummyDynamicPropertyEvaluationRunnable(final SessionFactory sessionFactory,
            final IFullTextIndexUpdateScheduler fullTextIFullTextIndexUpdateScheduler)
    {
        setSessionFactory(sessionFactory);
        operationLog.debug("dummy property evaluator created");
    }

    public void clear()
    {
        operationLog.debug("clear");
    }

    public void scheduleUpdate(DynamicPropertyEvaluationOperation operation)
    {
        operationLog.debug("scheduling " + operation);
    }

    //
    // Runnable
    //

    public final void run()
    {
        operationLog.debug("started");
    }

}
