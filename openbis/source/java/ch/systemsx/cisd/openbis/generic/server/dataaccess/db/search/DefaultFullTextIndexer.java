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

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A default {@link IFullTextIndexer} which knows how to perform an efficient full text index.
 * <p>
 * Taken from <i>Hibernate Search</i> documentation page.
 * </p>
 * 
 * @author Christian Ribeaud
 */
final class DefaultFullTextIndexer implements IFullTextIndexer
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DefaultFullTextIndexer.class);

    /**
     * It is critical that <code>batchSize</code> matches
     * <code>hibernate.search.worker.batch_size</code>.
     * <p>
     * Default value (meaning <i>unspecified</i>) is <code>0</code>.
     * </p>
     */
    private final int batchSize;

    DefaultFullTextIndexer(final int batchSize)
    {
        assert batchSize > -1 : "Batch size can not be negative.";
        this.batchSize = batchSize;
    }

    //
    // IFullTextIndexer
    //

    public final <T> void doFullTextIndex(final Session hibernateSession, final Class<T> clazz)
            throws DataAccessException
    {
        final FullTextSession fullTextSession = Search.createFullTextSession(hibernateSession);
        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);
        final Transaction transaction = hibernateSession.beginTransaction();
        final ScrollableResults results =
                fullTextSession.createCriteria(clazz).scroll(ScrollMode.FORWARD_ONLY);
        int index = 0;
        while (results.next())
        {
            index++;
            final Object object = results.get(0);
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Indexing entity '%s'."));
            }
            fullTextSession.index(object);
            if (batchSize > 0 && index % batchSize == 0)
            {
                hibernateSession.clear();
            }
        }
        transaction.commit();
        operationLog
                .info(String.format("%d '%s' have been indexed.", index, clazz.getSimpleName()));
    }
}
