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

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.TokenGroup;
import org.apache.lucene.search.highlight.TokenSources;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
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

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.LuceneQueryBuilder;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.SearchAnalyzer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IMatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchHit;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

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
                        public final Object doInHibernate(final Session session)
                                throws HibernateException, SQLException
                        {
                            return doSearchEntitiesByTerm(session, entityClass, searchTerm);
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
            throws DataAccessException, UserFailureException
    {
        final FullTextSession fullTextSession = Search.getFullTextSession(session);
        Analyzer analyzer = LuceneQueryBuilder.createSearchAnalyzer();

        MyIndexReaderProvider<T> indexProvider =
                new MyIndexReaderProvider<T>(fullTextSession, entityClass);
        String searchQuery = LuceneQueryBuilder.disableFieldQuery(userQuery);

        try
        {
            List<SearchHit> result = new ArrayList<SearchHit>();
            String[] fields = indexProvider.getIndexedFields();
            for (String fieldName : fields)
            {
                List<SearchHit> hits =
                        searchTermInField(fullTextSession, fieldName, searchQuery, entityClass,
                                analyzer, indexProvider.getReader());
                result.addAll(hits);
            }
            return result;
        } finally
        {
            indexProvider.close();
        }
    }

    private final <T extends IMatchingEntity> List<SearchHit> searchTermInField(
            final FullTextSession fullTextSession, final String fieldName, final String searchTerm,
            final Class<T> entityClass, Analyzer analyzer, IndexReader indexReader)
            throws DataAccessException, UserFailureException
    {
        Query query = LuceneQueryBuilder.parseQuery(fieldName, searchTerm, analyzer);
        query = rewriteQuery(indexReader, query);
        final FullTextQuery hibernateQuery =
                fullTextSession.createFullTextQuery(query, entityClass);
        hibernateQuery.setProjection(FullTextQuery.THIS, FullTextQuery.DOCUMENT_ID,
                FullTextQuery.DOCUMENT);

        MyHighlighter highlighter = new MyHighlighter(query, indexReader, analyzer);
        hibernateQuery.setResultTransformer(createResultTransformer(fieldName, highlighter));
        List<SearchHit> result = AbstractDAO.cast(hibernateQuery.list());
        return filterNulls(result);
    }

    private static <T> List<T> filterNulls(List<T> list)
    {
        List<T> result = new ArrayList<T>();
        for (T elem : list)
        {
            if (elem != null)
            {
                result.add(elem);
            }
        }
        return result;
    }

    // we need this for higlighter when wildcards are used
    private static Query rewriteQuery(IndexReader indexReader, Query query)
    {
        try
        {
            return query.rewrite(indexReader);
        } catch (IOException ex)
        {
            logSearchHighlightingError(ex);
            return query;
        }
    }

    private static ResultTransformer createResultTransformer(final String fieldName,
            final MyHighlighter highlighter)
    {
        return new ResultTransformer()
            {
                private static final long serialVersionUID = 1L;

                @SuppressWarnings("unchecked")
                public List transformList(List collection)
                {
                    throw new IllegalStateException("This method should not be called");
                }

                public Object transformTuple(Object[] tuple, String[] aliases)
                {
                    IMatchingEntity entity = (IMatchingEntity) tuple[0];
                    int documentId = (Integer) tuple[1];
                    Document doc = (Document) tuple[2];
                    if (entity == null)
                    {
                        return null;
                    }

                    String matchingText = null;
                    try
                    {
                        String content = doc.get(fieldName);
                        if (content != null)
                        {
                            // NOTE: this may be imprecise if there are multiple fields with the
                            // same code. The first value will be taken.
                            matchingText =
                                    highlighter.getBestFragment(content, fieldName, documentId);
                        } else
                        {
                            // we do not store file content in the index
                            matchingText = "file content";
                        }
                    } catch (IOException ex)
                    {
                        logSearchHighlightingError(ex);
                    }
                    return new SearchHit(entity, fieldName, matchingText);
                }
            };
    }

    private static void logSearchHighlightingError(IOException ex)
    {
        operationLog.error("error during search result highlighting: " + ex.getMessage());
    }

    private static final class MyHighlighter
    {
        private final IndexReader indexReader;

        private final Analyzer analyzer;

        private final Highlighter highlighter;

        public MyHighlighter(Query query, IndexReader indexReader, Analyzer analyzer)
        {
            this.highlighter = createHighlighter(query);
            this.indexReader = indexReader;
            this.analyzer = analyzer;
        }

        private static Highlighter createHighlighter(Query query)
        {
            Formatter htmlFormatter = createFormatter();
            return new Highlighter(htmlFormatter, new QueryScorer(query));
        }

        private static Formatter createFormatter()
        {
            // NOTE: we do not use SimpleHTMLFormatter because we want to escape html in the search
            // results.
            return new Formatter()
                {
                    public String highlightTerm(String text, TokenGroup tokenGroup)
                    {
                        return text; // no highlight at all
                    }
                };
        }

        public String getBestFragment(String fieldContent, String fieldName, int documentId)
                throws IOException
        {
            TokenStream tokenStream =
                    TokenSources.getAnyTokenStream(indexReader, documentId, fieldName, analyzer);
            return highlighter.getBestFragment(tokenStream, fieldContent);
        }
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
            // TODO 2008-11-27, Tomasz Pylak: get rid of hard-coded name. Specify the same name
            // attribute for all DocumentId fields.
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

    public List<ExternalDataPE> searchForDataSets(final DataSetSearchCriteria criteria)
    {
        final List<ExternalDataPE> list =
                AbstractDAO.cast((List<?>) getHibernateTemplate().execute(new HibernateCallback()
                    {
                        public final Object doInHibernate(final Session session)
                                throws HibernateException, SQLException
                        {
                            return searchForDataSets(session, criteria);
                        }
                    }));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d matching datasets have been found for search criteria '%s'.", list.size(),
                    criteria.toString()));
        }
        return list;
    }

    private List<ExternalDataPE> searchForDataSets(Session session,
            DataSetSearchCriteria datasetSearchCriteria)
    {
        BooleanQuery query = new BooleanQuery();
        query.add(LuceneQueryBuilder.createQuery(datasetSearchCriteria), Occur.MUST);
        SearchAnalyzer searchAnalyzer = LuceneQueryBuilder.createSearchAnalyzer();
        query.add(LuceneQueryBuilder.parseQuery(SearchFieldConstants.DELETED, "false",
                searchAnalyzer), Occur.MUST);
        final FullTextSession fullTextSession = Search.getFullTextSession(session);
        final FullTextQuery hibernateQuery =
                fullTextSession.createFullTextQuery(query, ExternalDataPE.class);

        Criteria criteria = getSession().createCriteria(ExternalDataPE.class);
        criteria.setFetchMode("dataSetProperties", FetchMode.JOIN);
        criteria.setFetchMode("parents", FetchMode.JOIN);
        criteria.setFetchMode("procedure", FetchMode.JOIN);
        criteria.setFetchMode("procedure.experimentInternal", FetchMode.JOIN);
        criteria.setFetchMode("procedure.experimentInternal.experimentProperties", FetchMode.JOIN);
        hibernateQuery.setCriteriaQuery(criteria);

        List<ExternalDataPE> datasets = AbstractDAO.cast(hibernateQuery.list());
        datasets = filterNulls(datasets);
        // NOTE: there is a limit on the number of JOINs, so we have to initialize sample properties
        // manually
        initSamplesWithProperties(datasets);
        return datasets;
    }

    private void initSamplesWithProperties(List<ExternalDataPE> datasets)
    {
        for (ExternalDataPE dataset : datasets)
        {
            initSamplesWithProperties(dataset.getSampleAcquiredFrom());
            initSamplesWithProperties(dataset.getSampleDerivedFrom());
        }
    }

    private void initSamplesWithProperties(SamplePE sampleOrNull)
    {
        if (sampleOrNull != null)
        {
            HibernateUtils.initialize(sampleOrNull);
            HibernateUtils.initialize(sampleOrNull.getProperties());
        }
    }

}
