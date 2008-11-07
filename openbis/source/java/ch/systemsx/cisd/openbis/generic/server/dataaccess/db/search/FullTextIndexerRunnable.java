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
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.annotations.Indexed;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A <i>full-text</i> indexer.
 * 
 * @author Christian Ribeaud
 */
public final class FullTextIndexerRunnable extends HibernateDaoSupport implements Runnable
{
    final static String FULL_TEXT_INDEX_MARKER_FILENAME = Constants.MARKER_PREFIX + "full_index";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FullTextIndexerRunnable.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, FullTextIndexerRunnable.class);

    private final HibernateSearchContext context;

    private final IFullTextIndexer fullTextIndexer;

    private final IIndexedEntityFinder indexedEntityFinder;

    public FullTextIndexerRunnable(final SessionFactory sessionFactory,
            final HibernateSearchContext context)
    {
        assert context != null : "Unspecified hibernate search context.";
        setSessionFactory(sessionFactory);
        this.context = context;
        operationLog.debug(String.format("Hibernate search context: %s.", context));
        fullTextIndexer = new DefaultFullTextIndexer(context.getBatchSize());
        indexedEntityFinder =
                new PackageBasedIndexedEntityFinder("ch.systemsx.cisd.openbis.generic.shared.dto");
    }

    //
    // Runnable
    //

    public final void run()
    {
        final IndexMode indexMode = context.getIndexMode();
        if (indexMode == IndexMode.NO_INDEX)
        {
            return;
        }
        final Set<Class<?>> indexedEntities = indexedEntityFinder.getIndexedEntities();
        if (indexedEntities.size() == 0)
        {
            operationLog.info(String.format("No entity annotated with '%s' has been found.",
                    Indexed.class.getSimpleName()));
            return;
        }
        Class<?> currentEntity = null;
        try
        {
            final File indexBase = new File(context.getIndexBase());
            final File markerFile = new File(indexBase, FULL_TEXT_INDEX_MARKER_FILENAME);
            if (indexMode == IndexMode.SKIP_IF_MARKER_FOUND && markerFile.exists())
            {
                operationLog.debug(String.format("Skipping indexing process as "
                        + " marker file '%s' already exists.", markerFile.getAbsolutePath()));
                return;
            }
            final Session session = getSession();
            final StopWatch stopWatch = new StopWatch();
            for (final Class<?> indexedEntity : indexedEntities)
            {
                currentEntity = indexedEntity;
                stopWatch.reset();
                stopWatch.start();
                fullTextIndexer.doFullTextIndex(session, indexedEntity);
                stopWatch.stop();
                operationLog.info(String.format("Indexing entity '%s' took %s.", indexedEntity
                        .getName(), stopWatch));
            }
            FileUtils.touch(markerFile);
            releaseSession(session);
        } catch (final Throwable th)
        {
            notificationLog.error(String.format(
                    "A problem has occurred while indexing entity '%s'.", currentEntity), th);
        }
    }
}
