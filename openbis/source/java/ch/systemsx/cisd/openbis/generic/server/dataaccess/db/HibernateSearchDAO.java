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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.engine.DocumentBuilder;
import org.hibernate.search.reader.ReaderProvider;
import org.hibernate.search.store.DirectoryProvider;
import org.hibernate.transform.ResultTransformer;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.IMatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;

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

    //
    // IHibernateSearchDAO
    //

    public final <T extends IMatchingEntity> List<SearchHit> searchEntitiesByTerm(
            final Class<T> entityClass, final String searchTerm) throws DataAccessException
    {
        assert entityClass != null : "Unspecified entity class";
        assert StringUtils.isBlank(searchTerm) == false : "Unspecified search term.";

        final List<SearchHit> list =
                AbstractDAO.cast((List<?>) getHibernateTemplate().execute(new HibernateCallback()
                    {

                        //
                        // HibernateCallback
                        //

                        public final Object doInHibernate(final Session session)
                                throws HibernateException, SQLException
                        {
                            try
                            {
                                return doSearchEntitiesByTerm(session, entityClass, searchTerm);
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

    private final <T extends IMatchingEntity> List<SearchHit> doSearchEntitiesByTerm(
            final Session session, final Class<T> entityClass, final String userQuery)
            throws DataAccessException, ParseException
    {
        final FullTextSession fullTextSession = Search.createFullTextSession(session);
        StandardAnalyzer analyzer = new StandardAnalyzer();

        MyIndexReaderProvider<T> indexProvider =
                new MyIndexReaderProvider<T>(fullTextSession, entityClass);
        String searchQuery = disableFieldQuery(userQuery);

        try
        {
            List<SearchHit> result = new ArrayList<SearchHit>();
            String[] fields = indexProvider.getIndexedFields();
            for (String fieldName : fields)
            {
                List<SearchHit> hits =
                        searchTermInField(fullTextSession, analyzer, entityClass, fieldName,
                                searchQuery);
                result.addAll(hits);
            }
            return result;
        } finally
        {
            indexProvider.close();
        }
    }

    // disables field query by escaping all field separator characters.
    @Private
    static String disableFieldQuery(String userQuery)
    {
        char fieldSep = ':';
        char escapeChar = '\\';
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < userQuery.length(); i++)
        {
            char ch = userQuery.charAt(i);
            if (ch == fieldSep && (i == 0 || userQuery.charAt(i - 1) != escapeChar))
            {
                // add escape character to an unescaped field separator
                sb.append(escapeChar);
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    private final <T extends IMatchingEntity> List<SearchHit> searchTermInField(
            final FullTextSession fullTextSession, Analyzer analyzer, final Class<T> entityClass,
            final String fieldName, final String searchTerm) throws DataAccessException,
            ParseException
    {
        Query query = createLuceneQuery(fieldName, searchTerm, analyzer);
        final FullTextQuery hibernateQuery =
                fullTextSession.createFullTextQuery(query, entityClass);
        hibernateQuery.setResultTransformer(createResultTransformer(fieldName));
        return AbstractDAO.cast(hibernateQuery.list());
    }

    private static <T extends IMatchingEntity> ResultTransformer createResultTransformer(
            final String fieldName)
    {
        return new ResultTransformer()
            {
                private static final long serialVersionUID = 1L;

                @SuppressWarnings("unchecked")
                public List transformList(List collection)
                {
                    List<SearchHit> result = new ArrayList<SearchHit>();
                    List<T> originalList = collection;
                    for (T elem : originalList)
                    {
                        // TODO 2008-11-27, Tomasz Pylak: find the text which is matching
                        String matchingText = "?";
                        result.add(new SearchHit(elem, fieldName, matchingText));
                    }
                    return result;
                }

                public Object transformTuple(Object[] tuple, String[] aliases)
                {
                    return new IllegalStateException("This method should not be called");
                }

            };
    }

    private Query createLuceneQuery(final String fieldName, final String searchTerm,
            Analyzer analyzer) throws ParseException
    {
        final QueryParser parser = new QueryParser(fieldName, analyzer);
        parser.setAllowLeadingWildcard(true);
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        return parser.parse(searchTerm);
    }

    private static final class MyIndexReaderProvider<T>
    {
        private final ReaderProvider readerProvider;

        private final IndexReader indexReader;

        /** opens the index reader. Closing the index after usage must be done with #close() method. */
        public MyIndexReaderProvider(final FullTextSession fullTextSession,
                final Class<T> entityClass)
        {
            SearchFactory searchFactory = fullTextSession.getSearchFactory();
            DirectoryProvider<?>[] directoryProviders =
                    searchFactory.getDirectoryProviders(entityClass);
            this.readerProvider = searchFactory.getReaderProvider();
            this.indexReader = readerProvider.openReader(directoryProviders);
        }

        public IndexReader getReader()
        {
            return indexReader;
        }

        public String[] getIndexedFields()
        {
            Collection<?> fieldNames = indexReader.getFieldNames(FieldOption.INDEXED);
            // TODO 2008-11-27, Tomasz Pylak: is there a nicer way to remove id field without
            // hardcoding its name?
            fieldNames.remove("id");
            fieldNames.remove(DocumentBuilder.CLASS_FIELDNAME);
            return fieldNames.toArray(new String[fieldNames.size()]);
        }

        /** must be called to close the index reader when it is not needed anymore */
        public void close()
        {
            readerProvider.closeReader(indexReader);
        }
    }
}
