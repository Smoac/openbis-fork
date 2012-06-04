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
import java.io.IOException;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.store.DirectoryProvider;
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
    public final static String FULL_TEXT_INDEX_MARKER_FILENAME = Constants.MARKER_PREFIX
            + "full_index";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FullTextIndexerRunnable.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            FullTextIndexerRunnable.class);

    private final HibernateSearchContext context;

    private final IFullTextIndexer fullTextIndexer;

    private final IFullTextIndexUpdater fullTextIndexUpdater;

    private final IIndexedEntityFinder indexedEntityFinder;

    public FullTextIndexerRunnable(final SessionFactory sessionFactory,
            final HibernateSearchContext context, final IFullTextIndexUpdater fullTextIndexUpdater)
    {
        assert context != null : "Unspecified hibernate search context.";
        setSessionFactory(sessionFactory);
        this.context = context;
        this.fullTextIndexUpdater = fullTextIndexUpdater;
        this.fullTextIndexer = new DefaultFullTextIndexer(context.getBatchSize());
        // TODO 2008-11-25, Tomasz Pylak: maybe we could get rid of hardcoding package path by
        // scanning Hibernate mapped entities?
        this.indexedEntityFinder =
                new PackageBasedIndexedEntityFinder("ch.systemsx.cisd.openbis.generic.shared.dto");
        operationLog.debug(String.format("Hibernate search context: %s.", context));
    }

    //
    // Runnable
    //

    @Override
    public final void run()
    {
        final IndexMode indexMode = context.getIndexMode();
        if (indexMode == IndexMode.NO_INDEX)
        {
            operationLog.debug(String.format("Skipping indexing process as "
                    + " '%s' mode was configured.", indexMode));
            return;
        }
        final Set<Class<?>> indexedEntities = indexedEntityFinder.getIndexedEntities();

        // Use code below for debugging if not all entities need to be indexed.
        //
        // final Set<Class<?>> indexedEntities = new HashSet<Class<?>>();
        // indexedEntities.add(DataPE.class);
        // indexedEntities.add(ExperimentPE.class);

        if (indexedEntities.size() == 0)
        {
            operationLog.info(String.format("No entity annotated with '%s' has been found.",
                    Indexed.class.getSimpleName()));
            return;
        }
        Class<?> currentEntity = null;
        try
        {
            // timeout exceptions were observed for the default timeout when database was bigger
            IndexWriterConfig.setDefaultWriteLockTimeout(3000);
            final File indexBase = new File(context.getIndexBase());
            final File markerFile = new File(indexBase, FULL_TEXT_INDEX_MARKER_FILENAME);
            final Session session = getSession();
            writeLockRecovery(session, indexedEntities); // recover even if marker is found
            if (indexMode == IndexMode.SKIP_IF_MARKER_FOUND && markerFile.exists())
            {
                operationLog.debug(String.format("Skipping indexing process as "
                        + "marker file '%s' already exists.", markerFile.getAbsolutePath()));
                return;
            }
            // full text index will be performed so updater queue can be cleared
            fullTextIndexUpdater.clear();
            //
            final StopWatch stopWatch = new StopWatch();
            for (final Class<?> indexedEntity : indexedEntities)
            {
                currentEntity = indexedEntity;
                stopWatch.reset();
                stopWatch.start();
                fullTextIndexer.doFullTextIndex(session, indexedEntity);
                stopWatch.stop();
                operationLog.info(String.format("Indexing entity '%s' took %s.",
                        indexedEntity.getName(), stopWatch));
            }
            FileUtils.touch(markerFile);
            releaseSession(session);
        } catch (final Throwable th)
        {
            notificationLog.error(String.format(
                    "A problem has occurred while indexing entity '%s'.", currentEntity), th);
        } finally
        // when index creation is finished start index updater thread
        {
            fullTextIndexUpdater.start();
        }
    }

    /**
     * Recovery code that removes stale write locks on directories of specified entities (see
     * LMS-2168). It should be run only at the server start when we are sure that no other thread
     * could be writing to the index at the same time. In our case the updater thread shouldn't be
     * started yet.
     * 
     * @see IndexWriter#unlock(org.apache.lucene.store.Directory)
     */
    private void writeLockRecovery(Session session, Set<Class<?>> indexedEntities)
            throws IOException
    {
        final FullTextSession fullTextSession = Search.getFullTextSession(session);
        SearchFactory searchFactory = fullTextSession.getSearchFactory();
        for (Class<?> indexedEntity : indexedEntities)
        {
            DirectoryProvider<?>[] directoryProviders =
                    searchFactory.getDirectoryProviders(indexedEntity);
            for (DirectoryProvider<?> provider : directoryProviders)
            {
                IndexWriter.unlock(provider.getDirectory());
            }
        }
    }
}
