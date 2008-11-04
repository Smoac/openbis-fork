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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.IMatchingEntity;

/**
 * Implementation of {@link IHibernateSearchDAO} for databases.
 * 
 * @author Christian Ribeaud
 */
final class HibernateSearchDAO extends HibernateDaoSupport implements IHibernateSearchDAO
{
    /**
     * The <code>Logger</code> of this class.
     * <p>
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}.
     * </p>
     */
    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, HibernateSearchDAO.class);

    HibernateSearchDAO(final SessionFactory sessionFactory)
    {
        assert sessionFactory != null : "Unspecified session factory";
        setSessionFactory(sessionFactory);
    }

    private final Analyzer createAnalyzer(final String[] fields)
    {
        final PerFieldAnalyzerWrapper analyzer =
                new PerFieldAnalyzerWrapper(new StandardAnalyzer());
        analyzer.addAnalyzer("code", new KeywordAnalyzer());
        return analyzer;
    }

    //
    // IHibernateSearchDAO
    //

    public final <T extends IMatchingEntity> List<T> searchEntitiesByTerm(
            final Class<T> entityClass, final String[] fields, final String searchTerm)
            throws DataAccessException
    {
        assert entityClass != null : "Unspecified entity class";
        assert fields != null && fields.length > 0 : "Unspecified search fields.";
        assert searchTerm != null : "Unspecified search term.";

        final List<T> list =
                AbstractDAO.cast((List<?>) getHibernateTemplate().execute(new HibernateCallback()
                    {

                        //
                        // HibernateCallback
                        //

                        public final Object doInHibernate(final Session session)
                                throws HibernateException, SQLException
                        {
                            final FullTextSession fullTextSession =
                                    Search.createFullTextSession(session);
                            final MultiFieldQueryParser parser =
                                    new MultiFieldQueryParser(fields, createAnalyzer(fields));
                            try
                            {
                                final Query query = parser.parse(searchTerm);
                                final org.hibernate.Query hibernateQuery =
                                        fullTextSession.createFullTextQuery(query, entityClass);
                                return hibernateQuery.list();
                            } catch (final ParseException ex)
                            {
                                throw new HibernateException(String.format(
                                        "Search term '%s' could not be parsed.", searchTerm), ex);
                            }
                        }
                    }));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d matching entities of type '%s' have been found for search term '%s'.", list
                            .size(), entityClass, searchTerm));
        }
        return list;
    }
}
