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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.FieldInfo.DocValuesType;
import org.apache.lucene.index.FieldInfos;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.TokenGroup;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
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
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.detailed.DetailedQueryBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Person;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.SearchableEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
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

    private Map<String, DocValuesType> fieldTypesCache;

    HibernateSearchDAO(final SessionFactory sessionFactory,
            HibernateSearchContext hibernateSearchContext)
    {
        assert sessionFactory != null : "Unspecified session factory";
        this.hibernateSearchContext = hibernateSearchContext;
        setSessionFactory(sessionFactory);
    }

    private boolean containsKeys(Map<String, DocValuesType> map, List<String> keys)
    {
        for (String key : keys)
        {
            if (map == null || map.containsKey(key) == false)
            {
                return false;
            }
        }
        return true;
    }

    private Map<String, DocValuesType> getFieldTypes(List<String> fields)
    {
        // Initialize field types
        if (fieldTypesCache == null || containsKeys(fieldTypesCache, fields) == false)
        {
            synchronized (this)
            {
                fieldTypesCache = Collections.synchronizedMap(new HashMap<String, DocValuesType>());
                for (SearchableEntity searchableEntity : SearchableEntity.values())
                {
                    MyIndexReaderProvider indexProvider = null;
                    try
                    {
                        final FullTextSession fullTextSession = Search.getFullTextSession(this.currentSession());
                        indexProvider = new MyIndexReaderProvider(fullTextSession, searchableEntity);
                        IndexReader indexReader = indexProvider.getReader();
                        for (AtomicReaderContext rc : indexReader.leaves())
                        {
                            AtomicReader ar = rc.reader();
                            FieldInfos fis = ar.getFieldInfos();
                            for (Iterator<FieldInfo> iter = fis.iterator(); iter.hasNext();)
                            {
                                FieldInfo fi = iter.next();
                                fieldTypesCache.put(fi.name, fi.getDocValuesType());
                            }
                        }
                    } finally
                    {
                        indexProvider.close();
                    }
                }
            }
        }
        return fieldTypesCache;
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

        @SuppressWarnings({ "unchecked", "rawtypes" })
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
            List<MatchingEntity> result = searchTermInField(userId, fullTextSession, "global_search", userQuery,
                    searchableEntity, analyzer, indexProvider.getReader(),
                    dataProvider, useWildcardSearchMode, alreadyFoundEntities,
                    maxSize);
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

        if (useWildcardSearchMode)
        {
            String q = QueryParser.escape(userQuery.toLowerCase());
            q = q.replace("\\*", "*");
            q = q.replace("\\?", "?");

            Query globalQuery = new WildcardQuery(new Term("global_search", q));
            Query metaprojectQuery = new WildcardQuery(new Term("global_search_metaprojects", "/" + userId + "/" + q));
            BooleanQuery bq = new BooleanQuery();
            bq.add(globalQuery, Occur.SHOULD);
            bq.add(metaprojectQuery, Occur.SHOULD);
            query = bq;
        } else
        {
            if (userQuery.startsWith("\"") && userQuery.endsWith("\"") && userQuery.length() > 2)
            {

                String[] parts = userQuery.toLowerCase().substring(1, userQuery.length() - 1).split("\\s+");

                SpanQuery[] queryParts = new SpanQuery[parts.length];
                for (int i = 0; i < parts.length; i++)
                {
                    String term = QueryParser.escape(parts[i]);
                    if (i == 0)
                    {
                        term = "*" + term;
                    } else if (i == parts.length - 1)
                    {
                        term = term + "*";
                    }
                    queryParts[i] = new SpanMultiTermQueryWrapper<WildcardQuery>(
                            new WildcardQuery(new Term("global_search", term)));
                }

                query = new SpanNearQuery(queryParts, 0, true);
            } else
            {
                String[] parts = userQuery.toLowerCase().split("\\s+");
                if (parts.length == 1)
                {
                    BooleanQuery bq = new BooleanQuery();

                    bq.add(new WildcardQuery(new Term("global_search", "*" + QueryParser.escape(parts[0]) + "*")), Occur.SHOULD);
                    bq.add(new WildcardQuery(new Term("global_search_metaprojects", "/" + userId + "/*" + QueryParser.escape(parts[0]) + "*")),
                            Occur.SHOULD);

                    query = bq;
                } else
                {
                    BooleanQuery bq = new BooleanQuery();
                    for (int i = 0; i < parts.length; i++)
                    {
                        String term = "*" + QueryParser.escape(parts[i]) + "*";
                        bq.add(new WildcardQuery(new Term("global_search", term)), Occur.SHOULD);
                        bq.add(new WildcardQuery(new Term("global_search_metaprojects", "/" + userId + "/" + term)),
                                Occur.SHOULD);
                    }
                    query = bq;
                }
            }
        }

        final FullTextQuery hibernateQuery =
                fullTextSession.createFullTextQuery(query,
                        searchableEntity.getMatchingEntityClass());

        // takes data only from Lucene index without hitting DB
        hibernateQuery.setProjection(ProjectionConstants.DOCUMENT_ID, ProjectionConstants.DOCUMENT);
        hibernateQuery.setReadOnly(true);
        hibernateQuery.setFirstResult(0);
        hibernateQuery.setMaxResults(maxResults);

        hibernateQuery.setResultTransformer(new ResultTransformer()
            {
                private static final long serialVersionUID = 1L;

                @Override
                public Object transformTuple(Object[] tuple, String[] aliases)
                {
                    IndexableField dataField = ((Document) tuple[1]).getField("global_search");
                    IndexableField headerField = ((Document) tuple[1]).getField("global_search_fields");
                    IndexableField metaprojectField = ((Document) tuple[1]).getField("global_search_metaprojects");

                    String splitString = "\\ \\|\\|\\|\\ ";

                    String[] fields = headerField.stringValue().split(splitString);
                    String[] content = dataField.stringValue().split(splitString);
                    String[] metaprojects = new String[0];

                    String mpfs = metaprojectField.stringValue();
                    if (mpfs.trim().length() > 0)
                    {
                        metaprojects = metaprojectField.stringValue().split(" ");
                    }

                    List<String> query = new ArrayList<>();
                    if (useWildcardSearchMode)
                    {
                        query.addAll(Arrays.asList(userQuery.split(" ")));
                    } else
                    {
                        if (userQuery.startsWith("\"") && userQuery.endsWith("\""))
                        {
                            query.add(userQuery.substring(1, userQuery.length() - 1));
                        } else if (userQuery.contains(" "))
                        {
                            query.addAll(Arrays.asList(userQuery.split(" ")));
                        } else
                        {
                            query.add(userQuery);
                        }
                    }

                    Map<String, String> matchingFields = new HashMap<>();
                    double score = 0.0;

                    for (int i = 0; i < content.length; i++)
                    {
                        for (String q : query)
                        {
                            double currentScore = score;

                            if (useWildcardSearchMode)
                            {
                                Pattern pattern = Pattern.compile("(?s)" + q.toLowerCase().replace("*", ".*").replace("?", ".?"));
                                Matcher matcher = pattern.matcher(content[i].toLowerCase());

                                while (matcher.find())
                                {
                                    int start = matcher.start();
                                    int end = matcher.end();
                                    score += getScore(content[i].toLowerCase(), start, end, fields[i], useWildcardSearchMode);
                                }
                            } else
                            {
                                String rest = content[i].toLowerCase();
                                while (rest.length() > 0)
                                {
                                    int start = rest.indexOf(q.toLowerCase());
                                    if (start == -1)
                                    {
                                        rest = "";
                                    } else
                                    {
                                        int end = start + q.length();
                                        rest = rest.substring(end);
                                        score += getScore(content[i].toLowerCase(), start, end, fields[i], useWildcardSearchMode);
                                    }
                                }
                            }

                            if (score > currentScore)
                            {
                                matchingFields.put(fields[i], content[i]);
                            }
                        }
                    }

                    Set<String> matchingMetaprojects = new HashSet<>();
                    for (int i = 0; i < metaprojects.length; i++)
                    {
                        String mp = metaprojects[i].substring(1);
                        String user = mp.substring(0, mp.indexOf("/"));
                        if (!user.equals(userId))
                        {
                            continue;
                        }

                        String value = metaprojects[i].substring(metaprojects[i].lastIndexOf("/") + 1);
                        for (String q : query)
                        {
                            if (useWildcardSearchMode)
                            {
                                Pattern pattern = Pattern.compile(q.toLowerCase().replace("*", ".*").replace("?", ".?"));
                                Matcher matcher = pattern.matcher(value.toLowerCase());

                                while (matcher.find())
                                {
                                    matchingMetaprojects.add(value);
                                    int start = matcher.start();
                                    int end = matcher.end();
                                    score += getScore(value, start, end, "Tag", useWildcardSearchMode) * 5;
                                }
                            } else
                            {
                                String rest = value.toLowerCase();
                                while (rest.length() > 0)
                                {
                                    int start = rest.indexOf(q.toLowerCase());
                                    if (start == -1)
                                    {
                                        rest = "";
                                    } else
                                    {
                                        matchingMetaprojects.add(value);
                                        int end = start + q.length();
                                        rest = rest.substring(end);
                                        score += getScore(value, start, end, "Tag", useWildcardSearchMode) * 5;
                                    }
                                }
                            }
                        }
                    }

                    if (matchingMetaprojects.size() > 0)
                    {
                        List<String> mps = new ArrayList<>(matchingMetaprojects);
                        Collections.sort(mps);
                        matchingFields.put("Tags", StringUtils.join(mps, ","));
                    }

                    if (matchingFields.containsKey("Code") ||
                            matchingFields.containsKey("Project code") ||
                            matchingFields.containsKey("Space code"))
                    {
                        matchingFields.remove("Identifier");
                    }

                    List<String> keys = new ArrayList<String>(matchingFields.keySet());
                    Collections.sort(keys);

                    String text = "";
                    for (String key : keys)
                    {
                        text = text + key + ": " + matchingFields.get(key) + "\n";
                    }

                    return createMatchingEntity((Document) tuple[1], text, score);
                }

                private double getScore(String text, int start, int end, String field, boolean wildcard)
                {
                    boolean fullmatch = (start == 0 || !StringUtils.isAlphanumeric(text.substring(start - 1, start))) &&
                            (end == text.length() || !StringUtils.isAlphanumeric(text.substring(end, end + 1))) &&
                            !wildcard; // full/partial matching not relevant for wildcard mode
                    if (field.equals("Perm ID") || field.equals("Identifier") || field.equals("Code"))
                    {
                        if (fullmatch)
                        {
                            return 200;
                        } else
                        {
                            return 100;
                        }
                    } else if (fullmatch)
                    {
                        return 10;
                    } else
                    {
                        return 1;
                    }
                }

                @Override
                public List transformList(List collection)
                {
                    List result = new ArrayList();
                    for (Object o : collection)
                    {
                        result.add(transformTuple(new Object[] { o }, new String[0]));
                    }
                    return result;
                }

                private MatchingEntity createMatchingEntity(final Document doc, final String matchingText, double score)
                {
                    final MatchingEntity result = new MatchingEntity();

                    // search properties
                    result.setMatch(matchingText);
                    result.setScore(score);

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

            });

        List<?> list = hibernateQuery.list();
        List<MatchingEntity> result = AbstractDAO.cast(list);

        return filterNulls(result);
    }

    // detailed search

    @Override
    public List<Long> searchForEntityIds(final String userId,
            final DetailedSearchCriteria criteria, final EntityKind entityKind,
            final List<IAssociationCriteria> associations)
    {
        @SuppressWarnings({ "unchecked", "rawtypes" })
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
        List<String> fieldNames = DetailedQueryBuilder.getIndexFieldNames(searchCriteria.getCriteria(), DtoConverters.convertEntityKind(entityKind));

        Query query = LuceneQueryBuilder.createDetailedSearchQuery(userId, searchCriteria, associations, entityKind, getFieldTypes(fieldNames));
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

        private final Highlighter highlighter;

        public MyHighlighter(Query query, IndexReader indexReader, Analyzer analyzer)
        {
            this.highlighter = createHighlighter(query);
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

        public Highlighter getRawHighlighter()
        {
            return highlighter;
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
