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
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.IMatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;

/**
 * Implementation of {@link IHibernateSearchDAO} for databases.
 * 
 * @author Christian Ribeaud
 */
final class HibernateSearchDAO extends HibernateDaoSupport implements IHibernateSearchDAO
{

    HibernateSearchDAO(final SessionFactory sessionFactory)
    {
        assert sessionFactory != null : "Unspecified session factory";
        setSessionFactory(sessionFactory);
    }

    //
    // IHibernateSearchDAO
    //

    public final <T extends IMatchingEntity> List<T> searchEntity(
            final SearchableEntity searchableEntity, final String searchTerm)
            throws DataAccessException
    {
        return AbstractDAO.cast((List<?>) getHibernateTemplate().execute(new HibernateCallback()
            {

                //
                // HibernateCallback
                //

                public final Object doInHibernate(final Session session) throws HibernateException,
                        SQLException
                {
                    final FullTextSession fullTextSession = Search.createFullTextSession(session);
                    final MultiFieldQueryParser parser =
                            new MultiFieldQueryParser(searchableEntity.getFields(),
                                    new StandardAnalyzer());
                    try
                    {
                        final Query query = parser.parse(searchTerm);
                        final org.hibernate.Query hibernateQuery =
                                fullTextSession.createFullTextQuery(query, searchableEntity
                                        .getEntityClass());
                        return hibernateQuery.list();
                    } catch (final ParseException ex)
                    {
                        throw new HibernateException(String.format(
                                "Search term '%s' could not be parsed.", searchTerm), ex);
                    }
                }
            }));
    }
}
