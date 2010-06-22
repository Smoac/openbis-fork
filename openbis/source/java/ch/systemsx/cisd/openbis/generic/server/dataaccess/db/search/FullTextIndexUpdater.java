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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

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

/**
 * A <i>full-text</i> index updater.
 * 
 * @author Piotr Buczek
 */
public final class FullTextIndexUpdater extends HibernateDaoSupport implements
        IFullTextIndexUpdater
{
    public final static String FULL_TEXT_INDEX_UPDATER_QUEUE_FILENAME = ".index_updater_queue";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FullTextIndexUpdater.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, FullTextIndexUpdater.class);

    private final HibernateSearchContext context;

    private final IFullTextIndexer fullTextIndexer;

    private final IExtendedBlockingQueue<IndexUpdateOperation> updaterQueue;

    public FullTextIndexUpdater(final SessionFactory sessionFactory,
            final HibernateSearchContext context)
    {
        assert context != null : "Unspecified hibernate search context.";
        setSessionFactory(sessionFactory);
        this.context = context;
        operationLog.debug(String.format("Hibernate search context: %s.", context));
        fullTextIndexer = new DefaultFullTextIndexer(context.getBatchSize());
        File queueFile = getUpdaterQueueFile(context);
        operationLog.debug(String.format("Updater queue file: %s.", queueFile));
        updaterQueue =
                ExtendedBlockingQueueFactory
                        .<IndexUpdateOperation> createPersistRecordBased(queueFile);
    }

    private static File getUpdaterQueueFile(HibernateSearchContext context)
    {
        final File indexBase = new File(context.getIndexBase());
        final File queueFile = new File(indexBase, FULL_TEXT_INDEX_UPDATER_QUEUE_FILENAME);
        return queueFile;
    }

    public void start()
    {
        Thread thread = new Thread(new FullTextIndexUpdaterRunnable(), "Full Text Index Updater");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    public void clear()
    {
        updaterQueue.clear();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Cleared updater queue.");
        }
    }

    public void scheduleUpdate(IndexUpdateOperation operation)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Scheduling update: " + operation);
        }
        updaterQueue.add(operation);
    }

    /**
     * {@link Runnable} performing updates in a loop.
     * 
     * @author Piotr Buczek
     */
    private class FullTextIndexUpdaterRunnable implements Runnable
    {
        public final void run()
        {
            final IndexMode indexMode = context.getIndexMode();
            if (indexMode == IndexMode.NO_INDEX) // sanity check
            {
                operationLog.debug(String.format("Stopping index updater process as "
                        + " '%s' mode was configured.", indexMode));
                return;
            }
            try
            {
                while (true)
                {
                    final IndexUpdateOperation operation = updaterQueue.peekWait();
                    if (operationLog.isInfoEnabled())
                    {
                        operationLog.info("Update: " + operation);
                    }
                    final StopWatch stopWatch = new StopWatch();
                    stopWatch.start();
                    Session session = null;
                    try
                    {
                        session = getSession();
                        switch (operation.getOperationKind())
                        {
                            case REINDEX:
                                fullTextIndexer.doFullTextIndexUpdate(getSession(), operation
                                        .getClazz(), operation.getIds());
                                break;
                            case REMOVE:
                                fullTextIndexer.removeFromIndex(getSession(), operation.getClazz(),
                                        operation.getIds());
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
                    }
                    if (operationLog.isInfoEnabled())
                    {
                        operationLog.info(operation.getOperationKind() + " of "
                                + operation.getIds().size() + " "
                                + operation.getClazz().getSimpleName() + "s took " + stopWatch);
                    }
                    updaterQueue.take();
                }
            } catch (final Throwable th)
            {
                notificationLog.error("A problem has occurred while updating index.", th);
            }
        }
    }
}
