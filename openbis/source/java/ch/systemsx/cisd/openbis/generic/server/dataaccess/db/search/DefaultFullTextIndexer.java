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
 * Taken from <i>Hibernate Search</i> documentation page.
 * </p>
 * 
 * @author Christian Ribeaud
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
        final Transaction transaction = hibernateSession.beginTransaction();
        // we index entities in batches loading them in groups restricted by id:
        // (currentMinId,currentMinId+batchSize]
        long minId = 0;
        long maxId = getMaxId(fullTextSession, clazz);
        long currentMinId = minId;
        int index = 0;
        while (currentMinId < maxId)
        {
            final List<?> results =
                    createCriteriaWithRestrictedId(fullTextSession, clazz, currentMinId).list();
            for (Object object : results)
            {
                indexEntity(hibernateSession, fullTextSession, object);
                index++;
            }
            operationLog.info(String.format("%d '%s' have been indexed...", index, clazz
                    .getSimpleName()));
            fullTextSession.flushToIndexes();
            hibernateSession.clear();
            currentMinId += batchSize;
        }
        // TODO 2009-08-12, Piotr Buczek: check whether optimize improves search perfomance
        // fullTextSession.getSearchFactory().optimize(clazz);
        transaction.commit();
        operationLog.info(String.format("'%s' index complete. %d entities have been indexed.",
                clazz.getSimpleName(), index));
    }

    private <T> Long getMaxId(final FullTextSession fullTextSession, final Class<T> clazz)
    {
        Object result =
                createCriteria(fullTextSession, clazz).setProjection(
                        Projections.max(ID_PROPERTY_NAME)).uniqueResult();
        return (result == null) ? 0 : (Long) result;
    }

    private <T> Criteria createCriteria(final FullTextSession fullTextSession, final Class<T> clazz)
    {
        return fullTextSession.createCriteria(clazz);
    }

    private <T> Criteria createCriteriaWithRestrictedId(final FullTextSession fullTextSession,
            final Class<T> clazz, final long minId)
    {
        return createCriteria(fullTextSession, clazz).add(Restrictions.gt(ID_PROPERTY_NAME, minId))
                .add(Restrictions.le(ID_PROPERTY_NAME, minId + batchSize));
    }

    private <T> void indexEntity(final Session hibernateSession,
            final FullTextSession fullTextSession, T object)
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
