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

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A default {@link IFullTextIndexer} which knows how to perform an efficient full text index.
 * <p>
 * Partly taken from <i>Hibernate Search</i> documentation page.
 * </p>
 * 
 * @author Christian Ribeaud
 * @author Piotr Buczek
 */
final class DefaultFullTextIndexer implements IFullTextIndexer
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DefaultFullTextIndexer.class);

    private static String ID_PROPERTY_NAME = "id";

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
        operationLog.info(String.format("Indexing '%s'...", clazz.getSimpleName()));
        final FullTextSession fullTextSession = Search.getFullTextSession(hibernateSession);
        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);

        // we index entities in batches loading them in groups restricted by id:
        // [ ids[index], ids[min(index+batchSize, maxIndex))] )
        final Transaction transaction = fullTextSession.beginTransaction();
        final List<Long> ids = getAllIds(fullTextSession, clazz);
        final int idsSize = ids.size();
        operationLog.info(String.format("... got %d '%s' ids...", idsSize, clazz.getSimpleName()));
        int index = 0;
        final int maxIndex = idsSize - 1;
        // need to increment last id because we use 'lt' condition
        if (maxIndex > -1)
        {
            ids.set(maxIndex, ids.get(maxIndex) + 1);
        }
        while (index < maxIndex)
        {
            final int nextIndex = getNextIndex(index, maxIndex);
            final long minId = ids.get(index);
            final long maxId = ids.get(nextIndex);
            final List<?> results =
                    createCriteriaWithRestrictedId(fullTextSession, clazz, minId, maxId).list();
            for (Object object : results)
            {
                indexEntity(fullTextSession, object);
                index++;
            }
            operationLog.info(String.format("%d %ss have been indexed...", index, clazz
                    .getSimpleName()));
            fullTextSession.flushToIndexes();
            fullTextSession.clear();
        }
        fullTextSession.getSearchFactory().optimize(clazz);
        transaction.commit();
        operationLog.info(String.format("'%s' index complete. %d entities have been indexed.",
                clazz.getSimpleName(), index));
    }

    public <T> void doFullTextIndexUpdate(final Session hibernateSession, final Class<T> clazz,
            final List<Long> ids) throws DataAccessException
    {
        operationLog.info(String.format("Reindexing %s %ss...", ids.size(), clazz.getSimpleName()));
        final FullTextSession fullTextSession = Search.getFullTextSession(hibernateSession);
        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);

        // we index entities in batches loading them in groups by id
        final Transaction transaction = fullTextSession.beginTransaction();
        final int maxIndex = ids.size();
        int index = 0;
        // need to increment last id because we use 'lt' condition
        while (index < maxIndex)
        {
            final int nextIndex = getNextIndex(index, maxIndex);
            List<Long> subList = ids.subList(index, nextIndex);
            final List<?> results =
                    createCriteriaWithRestrictedId(fullTextSession, clazz, subList).list();
            for (Object object : results)
            {
                indexEntity(fullTextSession, object);
                index++;
            }
            operationLog.info(String.format("%d %ss have been reindexed...", index, clazz
                    .getSimpleName()));
            fullTextSession.flushToIndexes();
            fullTextSession.clear();
        }
        fullTextSession.getSearchFactory().optimize(clazz);
        transaction.commit();
        operationLog.info(String.format("'%s' index is updated. %d entities have been reindexed.",
                clazz.getSimpleName(), index));
    }

    private int getNextIndex(int index, int maxIndex)
    {
        int result = index + batchSize;
        if (result < maxIndex)
        {
            return result;
        } else
        {
            return maxIndex;
        }
    }

    @SuppressWarnings(
        { "cast", "unchecked" })
    private <T> List<Long> getAllIds(final FullTextSession fullTextSession, final Class<T> clazz)
    {
        List<Long> result =
                (List<Long>) createCriteria(fullTextSession, clazz).setProjection(
                        Projections.property(ID_PROPERTY_NAME)).addOrder(
                        Order.asc(ID_PROPERTY_NAME)).list();
        return result;
    }

    private <T> Criteria createCriteriaWithRestrictedId(final FullTextSession fullTextSession,
            final Class<T> clazz, final long minId, final long maxId)
    {
        return createCriteria(fullTextSession, clazz).add(Restrictions.ge(ID_PROPERTY_NAME, minId))
                .add(Restrictions.lt(ID_PROPERTY_NAME, maxId));
    }

    private <T> Criteria createCriteriaWithRestrictedId(final FullTextSession fullTextSession,
            final Class<T> clazz, final List<Long> ids)
    {
        return createCriteria(fullTextSession, clazz).add(Restrictions.in(ID_PROPERTY_NAME, ids));
    }

    private <T> Criteria createCriteria(final FullTextSession fullTextSession, final Class<T> clazz)
    {
        return fullTextSession.createCriteria(clazz);
    }

    private <T> void indexEntity(final FullTextSession fullTextSession, T object)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Indexing entity '%s'.", object));
        }
        try
        {
            fullTextSession.index(object);
        } catch (Exception e)
        {
            operationLog.error("Error while indexing the object " + object + ": " + e.getMessage()
                    + ". Indexing will be continued.");
        }
    }

}
