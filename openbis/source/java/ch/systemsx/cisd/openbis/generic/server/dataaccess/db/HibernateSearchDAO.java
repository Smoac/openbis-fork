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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.TokenGroup;
import org.apache.lucene.search.highlight.TokenSources;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.engine.ProjectionConstants;
import org.hibernate.search.indexes.spi.DirectoryBasedIndexManager;
import org.hibernate.search.indexes.spi.ReaderProvider;
import org.hibernate.search.metadata.IndexedTypeDescriptor;
import org.hibernate.search.spi.SearchIntegrator;
import org.hibernate.transform.BasicTransformerAdapter;
import org.hibernate.transform.ResultTransformer;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.HibernateSearchContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.LuceneQueryBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.search.IgnoreCaseAnalyzer;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;

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
     * This logger does not output any SQL statement. If you want to do so, you had better set an appropriate debugging level for class
     * {@link JdbcAccessor}.
     * </p>
     */
    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            HibernateSearchDAO.class);

    private final HibernateSearchContext hibernateSearchContext;

    HibernateSearchDAO(final SessionFactory sessionFactory,
            HibernateSearchContext hibernateSearchContext)
    {
        assert sessionFactory != null : "Unspecified session factory";
        this.hibernateSearchContext = hibernateSearchContext;
        setSessionFactory(sessionFactory);
    }

    //
    // IHibernateSearchDAO
    //

    @Override
    public int getResultSetSizeLimit()
    {
        return hibernateSearchContext.getMaxResults();
    }

    // simple search for MatchingEntities

    @Override
    public List<MatchingEntity> searchEntitiesByTerm(final String userId,
            final SearchableEntity searchableEntity, final String searchTerm,
            final HibernateSearchDataProvider dataProvider, final boolean useWildcardSearchMode,
            final int alreadyFoundEntities, final int maxSize) throws DataAccessException
    {
        assert searchableEntity != null : "Unspecified searchable entity";
        assert StringUtils.isBlank(searchTerm) == false : "Unspecified search term.";
        assert dataProvider != null : "Unspecified data provider";

        final List<MatchingEntity> list =
                AbstractDAO.cast((List<?>) getHibernateTemplate().executeWithNativeSession(new HibernateCallback()
                    {
                        @Override
                        public final List<MatchingEntity> doInHibernate(final Session session)
                                throws HibernateException
                        {
                            return doSearchEntitiesByTerm(userId, session, searchableEntity,
                                    searchTerm, dataProvider, useWildcardSearchMode,
                                    alreadyFoundEntities, maxSize);
                        }
                    }));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d matching entities of type '%s' have been found for search term '%s'.",
                    list.size(), searchableEntity.getMatchingEntityClass(), searchTerm));
        }
        return list;
    }

    private final List<MatchingEntity> doSearchEntitiesByTerm(final String userId,
            final Session session, final SearchableEntity searchableEntity, final String userQuery,
            final HibernateSearchDataProvider dataProvider, final boolean useWildcardSearchMode,
            int alreadyFoundEntities, int maxSize) throws DataAccessException, UserFailureException
    {
        final FullTextSession fullTextSession = Search.getFullTextSession(session);
        Analyzer analyzer = LuceneQueryBuilder.createSearchAnalyzer();

        MyIndexReaderProvider indexProvider =
                new MyIndexReaderProvider(fullTextSession, searchableEntity);

        try
        {
            List<MatchingEntity> result = new ArrayList<MatchingEntity>();
            String[] fields = indexProvider.getIndexedFields();
            for (String fieldName : fields)
            {
                List<MatchingEntity> hits =
                        searchTermInField(userId, fullTextSession, fieldName, userQuery,
                                searchableEntity, analyzer, indexProvider.getReader(),
                                dataProvider, useWildcardSearchMode, result.size()
                                        + alreadyFoundEntities, maxSize);
                result.addAll(hits);
            }
            return result;
        } finally
        {
            indexProvider.close();
        }
    }

    private final List<MatchingEntity> searchTermInField(final String userId,
            final FullTextSession fullTextSession, final String fieldName, final String userQuery,
            final SearchableEntity searchableEntity, Analyzer analyzer, IndexReader indexReader,
            final HibernateSearchDataProvider dataProvider, final boolean useWildcardSearchMode,
            int alreadyFoundResults, int maxSize) throws DataAccessException, UserFailureException
    {
        int maxResults =
                Math.max(0, Math.min(maxSize, hibernateSearchContext.getMaxResults())
                        - alreadyFoundResults);
        if (maxResults == 0)
        {
            return new ArrayList<MatchingEntity>();
        }

        Query query = null;

        Analyzer chosenAnalyzer = analyzer;
        if (MetaprojectSearch.isMetaprojectField(fieldName))
        {
            String searchTerm =
                    LuceneQueryBuilder.adaptQuery(
                            MetaprojectSearch.getMetaprojectUserQuery(userQuery, userId),
                            useWildcardSearchMode, false);

            searchTerm = searchTerm.replaceAll("\\/", "\\\\/");
            query =
                    LuceneQueryBuilder.parseQuery(fieldName, searchTerm, (chosenAnalyzer =
                            new IgnoreCaseAnalyzer()));
        } else
        {
            String searchTerm = LuceneQueryBuilder.adaptQuery(userQuery, useWildcardSearchMode);
            query = LuceneQueryBuilder.parseQuery(fieldName, searchTerm, analyzer);
        }

        query = rewriteQuery(indexReader, query);
        final FullTextQuery hibernateQuery =
                fullTextSession.createFullTextQuery(query,
                        searchableEntity.getMatchingEntityClass());

        // takes data only from Lucene index without hitting DB
        hibernateQuery.setProjection(ProjectionConstants.DOCUMENT_ID, ProjectionConstants.DOCUMENT);
        hibernateQuery.setReadOnly(true);
        hibernateQuery.setFirstResult(0);
        hibernateQuery.setMaxResults(maxResults);

        MyHighlighter highlighter = new MyHighlighter(query, indexReader, chosenAnalyzer);
        hibernateQuery.setResultTransformer(new MatchingEntityResultTransformer(searchableEntity,
                fieldName, highlighter, dataProvider));
        List<?> list = hibernateQuery.list();
        final List<MatchingEntity> result = AbstractDAO.cast(list);
        return filterNulls(result);
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

    // detailed search

    @Override
    public List<Long> searchForEntityIds(final String userId,
            final DetailedSearchCriteria criteria, final EntityKind entityKind,
            final List<IAssociationCriteria> associations)
    {
        final List<Long> list =
                AbstractDAO.cast((List<?>) getHibernateTemplate().executeWithNativeSession(new HibernateCallback()
                    {
                        @Override
                        public final Object doInHibernate(final Session session)
                                throws HibernateException
                        {
                            return searchForEntityIds(userId, session, criteria, entityKind,
                                    associations);
                        }
                    }));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format(
                    "%d matching samples have been found for search criteria '%s'.", list.size(),
                    criteria.toString()));
        }
        return list;
    }

    /**
     * Returns a list ids of entities of given kind that match given criteria.<br>
     * <br>
     * Takes data only from Lucene index without hitting DB.
     */
    private List<Long> searchForEntityIds(String userId, Session session,
            DetailedSearchCriteria searchCriteria, EntityKind entityKind,
            List<IAssociationCriteria> associations)
    {
        Query query =
                LuceneQueryBuilder.createDetailedSearchQuery(userId, searchCriteria, associations,
                        entityKind);
        final FullTextSession fullTextSession = Search.getFullTextSession(session);
        final FullTextQuery hibernateQuery =
                fullTextSession.createFullTextQuery(query, entityKind.getEntityClass());

        hibernateQuery.setProjection(ProjectionConstants.ID);
        hibernateQuery.setReadOnly(true);
        hibernateQuery.setResultTransformer(new PassThroughOneObjectTupleResultTransformer());

        List<Long> entityIds = AbstractDAO.cast(hibernateQuery.list());
        entityIds = filterNulls(entityIds);
        return entityIds;
    }

    //
    // Helpers
    //

    private static class PassThroughOneObjectTupleResultTransformer extends BasicTransformerAdapter
    {
        private static final long serialVersionUID = 1L;

        @Override
        public Object transformTuple(Object[] tuple, String[] aliases)
        {
            assert tuple.length == 1 : "tuple should consist of exactly one object";
            return tuple[0];
        }

    }

    private static class MatchingEntityResultTransformer implements ResultTransformer
    {
        private final SearchableEntity searchableEntity;

        private final String fieldName;

        private final MyHighlighter highlighter;

        private final HibernateSearchDataProvider dataProvider;

        private static final long serialVersionUID = 1L;

        public MatchingEntityResultTransformer(final SearchableEntity searchableEntity,
                final String fieldName, final MyHighlighter highlighter,
                final HibernateSearchDataProvider dataProvider)
        {
            this.searchableEntity = searchableEntity;
            this.fieldName = fieldName;
            this.highlighter = highlighter;
            this.dataProvider = dataProvider;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public List transformList(List collection)
        {
            throw new IllegalStateException("This method should not be called");
        }

        @Override
        public Object transformTuple(Object[] tuple, String[] aliases)
        {
            final int documentId = (Integer) tuple[0];
            final Document doc = (Document) tuple[1];

            String matchingText = null;
            try
            {
                String content = doc.get(fieldName);
                if (content != null)
                {
                    // NOTE: this may be imprecise if there are multiple fields with the
                    // same code. The first value will be taken.
                    matchingText = highlighter.getBestFragment(content, fieldName, documentId);
                } else

                {
                    // in some cases (e.g. attachments) we do not store content in the index
                    matchingText = "[content]";
                }
            } catch (IOException ex)
            {
                logSearchHighlightingError(ex);
            } catch (InvalidTokenOffsetsException ex)
            {
                logSearchHighlightingError(ex);
            }
            return createMatchingEntity(doc, matchingText);
        }

        private MatchingEntity createMatchingEntity(final Document doc, final String matchingText)
        {
            final MatchingEntity result = new MatchingEntity();

            // search properties
            result.setFieldDescription(fieldName);
            result.setTextFragment(matchingText);

            // IIdentifiable properties
            // NOTE: for contained sample this code is full code with container code part
            result.setCode(getFieldValue(doc, SearchFieldConstants.CODE));
            result.setId(Long.parseLong(getFieldValue(doc, SearchFieldConstants.ID)));
            result.setIdentifier(getFieldValue(doc, SearchFieldConstants.IDENTIFIER));
            result.setPermId(tryGetFieldValue(doc, SearchFieldConstants.PERM_ID));

            // entity kind
            result.setEntityKind(DtoConverters.convertEntityKind(searchableEntity.getEntityKind()));

            // entity type
            BasicEntityType entityType = new BasicEntityType();
            entityType.setCode(getFieldValue(doc, SearchFieldConstants.PREFIX_ENTITY_TYPE
                    + SearchFieldConstants.CODE));
            result.setEntityType(entityType);

            // group
            Map<String, Space> spacesById = dataProvider.getGroupsById();
            IndexableField spaceFieldOrNull = doc.getField(getSpaceIdFieldName());
            if (spaceFieldOrNull != null)
            {
                Space space = spacesById.get(spaceFieldOrNull.stringValue());
                result.setSpace(space);
            }

            // registrator
            final String registratorIdOrNull =
                    tryGetFieldValue(doc, SearchFieldConstants.PREFIX_REGISTRATOR
                            + SearchFieldConstants.PERSON_USER_ID);
            final String firstNameOrNull =
                    tryGetFieldValue(doc, SearchFieldConstants.PREFIX_REGISTRATOR
                            + SearchFieldConstants.PERSON_FIRST_NAME);
            if (registratorIdOrNull != null || firstNameOrNull != null)
            {
                Person registrator = new Person();
                registrator.setUserId(registratorIdOrNull);
                registrator.setFirstName(firstNameOrNull);
                registrator.setLastName(tryGetFieldValue(doc,
                        SearchFieldConstants.PREFIX_REGISTRATOR
                                + SearchFieldConstants.PERSON_LAST_NAME));
                registrator.setEmail(tryGetFieldValue(doc, SearchFieldConstants.PREFIX_REGISTRATOR
                        + SearchFieldConstants.PERSON_EMAIL));
                result.setRegistrator(registrator);
            }

            return result;
        }

        private String getFieldValue(final Document document, final String searchFieldName)
        {
            return document.getField(searchFieldName).stringValue();
        }

        private String tryGetFieldValue(final Document document, final String searchFieldName)
        {
            IndexableField fieldOrNull = document.getField(searchFieldName);
            return fieldOrNull == null ? null : fieldOrNull.stringValue();
        }

        private String getSpaceIdFieldName()
        {
            String groupId = SearchFieldConstants.PREFIX_SPACE + SearchFieldConstants.ID;
            if (searchableEntity.equals(SearchableEntity.EXPERIMENT))
            {
                return SearchFieldConstants.PREFIX_PROJECT + groupId;
            } else
            {
                return groupId;
            }
        }
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

    private static void logSearchHighlightingError(Exception ex)
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
                    @Override
                    public String highlightTerm(String text, TokenGroup tokenGroup)
                    {
                        return text; // no highlight at all
                    }
                };
        }

        public String getBestFragment(String fieldContent, String fieldName, int documentId)
                throws IOException, InvalidTokenOffsetsException

        {
            TokenStream tokenStream =
                    TokenSources.getAnyTokenStream(indexReader, documentId, fieldName, analyzer);
            return highlighter.getBestFragment(tokenStream, fieldContent);
        }
    }

    private static final class MyIndexReaderProvider
    {
        private final ReaderProvider readerProvider;

        private final IndexReader indexReader;

        private final IndexedTypeDescriptor indexedTypeDescriptor;

        /** opens the index reader. Closing the index after usage must be done with #close() method. */
        public MyIndexReaderProvider(final FullTextSession fullTextSession,
                final SearchableEntity searchableEntity)
        {
            SearchFactory searchFactory = fullTextSession.getSearchFactory();

            SearchIntegrator searchIntegrator = searchFactory.unwrap(SearchIntegrator.class);
            DirectoryBasedIndexManager indexManager =
                    (DirectoryBasedIndexManager) searchIntegrator.getIndexManager(searchableEntity.getMatchingEntityClass().getSimpleName());
            this.readerProvider = indexManager.getReaderProvider();
            this.indexReader = readerProvider.openIndexReader();
            this.indexedTypeDescriptor = searchIntegrator.getIndexedTypeDescriptor(searchableEntity.getMatchingEntityClass());
        }

        public IndexReader getReader()
        {
            return indexReader;
        }

        private void collectFields(IndexReaderContext context, Set<String> fields)
        {
            if (context == null)
            {
                return;
            }
            if (context instanceof AtomicReaderContext)
            {
                try
                {
                    for (String fieldName : ((AtomicReaderContext) context).reader().fields())
                    {
                        fields.add(fieldName);
                    }
                } catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (context.children() == null)
            {
                return;
            }
            for (IndexReaderContext child : context.children())
            {
                collectFields(child, fields);
            }

        }

        public String[] getIndexedFields()
        {
            Set<String> fieldNames = new HashSet<String>();
            collectFields(indexReader.getContext(), fieldNames);
            fieldNames.remove(SearchFieldConstants.ID);
            for (String prefix : SearchFieldConstants.PREFIXES)
            {
                fieldNames.remove(prefix + SearchFieldConstants.ID);
            }
            return fieldNames.toArray(new String[fieldNames.size()]);
        }

        /** must be called to close the index reader when it is not needed anymore */
        public void close()
        {
            readerProvider.closeIndexReader(indexReader);
        }
    }
}
