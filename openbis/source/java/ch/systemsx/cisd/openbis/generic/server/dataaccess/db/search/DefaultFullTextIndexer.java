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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
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
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

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

    private Map<Class<?>, String[]> joinedProperties;

    DefaultFullTextIndexer(final int batchSize)
    {
        assert batchSize > -1 : "Batch size can not be negative.";
        this.batchSize = batchSize;
        initializeJoinedProperties();
    }

    private void initializeJoinedProperties()
    {
        joinedProperties = new HashMap<Class<?>, String[]>();
        joinedProperties.put(SamplePE.class, new String[]
            { "sampleProperties", "internalAttachments" });
        joinedProperties.put(MaterialPE.class, new String[]
            { "materialProperties" });
        joinedProperties.put(ExperimentPE.class,
                new String[]
                    { "experimentProperties", "internalAttachments",
                            "projectInternal.internalAttachments" });
        joinedProperties.put(ExternalDataPE.class, new String[]
            { "dataSetProperties" });
    }

    //
    // IFullTextIndexer
    //

    public final <T> void doFullTextIndex(final Session hibernateSession, final Class<T> clazz)
            throws DataAccessException
    {
        final FullTextSession fullTextSession = Search.getFullTextSession(hibernateSession);
        fullTextSession.setFlushMode(FlushMode.MANUAL);
        fullTextSession.setCacheMode(CacheMode.IGNORE);
        final Transaction transaction = hibernateSession.beginTransaction();
        final ScrollableResults results =
                createCriteria(fullTextSession, clazz).scroll(ScrollMode.FORWARD_ONLY);
        operationLog.info(String.format("Indexing '%s'...", clazz.getSimpleName()));
        int index = 0;
        // need to distinct manually from outer join because we scroll through results
        Set<Long> indexedIds = new LongOpenHashSet();
        while (results.next())
        {
            final Object object = results.get(0);
            Long id = HibernateUtils.getId((IIdHolder) object);
            if (indexedIds.add(id))
            {
                index++;
                indexEntity(hibernateSession, fullTextSession, index, object);
            }
        }
        // TODO 2009-08-12, Piotr Buczek: check whether optimize improves search perfomance
        // fullTextSession.getSearchFactory().optimize(clazz);
        transaction.commit();
        operationLog.info(String.format("'%s' index complete. %d entities have been indexed.",
                clazz.getSimpleName(), index));
    }

    private <T> Criteria createCriteria(final FullTextSession fullTextSession, final Class<T> clazz)
    {
        final Criteria criteria = fullTextSession.createCriteria(clazz);

        // TODO 2009-08-09, Piotr Buczek: uncomment when fixed loading all properties
        // criteria.setFetchSize(batchSize);

        // if fetch size is not set we get OutOfMemory with big DB
        // fetching properties in JOIN mode improves performance by a factor of ~10
        String[] properties = joinedProperties.get(clazz);
        if (properties != null)
        {
            // TODO 2009-08-09, Piotr Buczek: uncomment when fixed loading all properties
            // for (String property : properties)
            // {
            // criteria.setFetchMode(property, FetchMode.JOIN);
            // }
        }
        return criteria;
    }

    private <T> void indexEntity(final Session hibernateSession,
            final FullTextSession fullTextSession, int index, T object)
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
        if (batchSize > 0 && index % batchSize == 0)
        {
            operationLog.info(String.format("%d '%s' have been indexed...", index, object
                    .getClass().getSimpleName()));
            fullTextSession.flushToIndexes();
            hibernateSession.clear();
        }
    }

}
