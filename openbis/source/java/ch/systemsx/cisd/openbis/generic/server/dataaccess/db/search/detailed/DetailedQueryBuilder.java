/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.detailed;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.bridge.builtin.DateBridge;

import ch.systemsx.cisd.common.exception.InternalErr;
import ch.systemsx.cisd.common.exception.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.MetaprojectSearch;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.LuceneQueryBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.AttributeSearchFieldKindProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.search.IgnoreCaseAnalyzer;

/**
 * Builder for detailed lucene queries for different entity kinds.
 * 
 * @author Piotr Buczek
 */
public class DetailedQueryBuilder
{
    private static final String[] DATE_FORMATS =
        { "y-M-d HH:mm:ss", "y-M-d HH:mm", "y-M-d" };

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DetailedQueryBuilder.class);

    /**
     * @param associations // TODO move to DetailedSearchCriteria
     * @throws UserFailureException when some search patterns are incorrect
     */
    public static Query createQuery(String userId, DetailedSearchCriteria searchCriteria,
            EntityKind entityKind, List<DetailedSearchAssociationCriteria> associations)
            throws UserFailureException
    {
        final DetailedQueryBuilder builder = new DetailedQueryBuilder(entityKind);
        final Query resultQuery = builder.createQuery(userId, searchCriteria, associations);
        operationLog.debug("Lucene detailed query: " + resultQuery.toString());
        return resultQuery;
    }

    //
    // LuceneDetailedQueryBuilder
    //

    private final EntityKind entityKind;

    private DetailedQueryBuilder(EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    private Query createQuery(String userId, DetailedSearchCriteria searchCriteria,
            List<DetailedSearchAssociationCriteria> associations)
    {
        boolean useWildcardSearchMode = searchCriteria.isUseWildcardSearchMode();
        List<DetailedSearchCriterion> criteria = searchCriteria.getCriteria();
        Occur occureCondition = createOccureCondition(searchCriteria.getConnection());

        Analyzer searchAnalyzer = LuceneQueryBuilder.createSearchAnalyzer();
        BooleanQuery resultQuery = new BooleanQuery();
        for (DetailedSearchCriterion criterion : criteria)
        {
            List<String> fieldNames = getIndexFieldNames(criterion.getField());

            if (criterion.getTimeZone() == null)
            {
                List<String> fieldPatterns = new ArrayList<String>(fieldNames.size());
                List<Analyzer> fieldAnalyzers = new ArrayList<Analyzer>(fieldNames.size());

                for (String fieldName : fieldNames)
                {
                    String fieldPattern = null;
                    Analyzer fieldAnalyzer = null;

                    if (MetaprojectSearch.isMetaprojectField(fieldName))
                    {
                        String fieldUserQuery =
                                MetaprojectSearch.getMetaprojectUserQuery(criterion.getValue(),
                                        userId);
                        fieldPattern =
                                LuceneQueryBuilder.adaptQuery(fieldUserQuery,
                                        useWildcardSearchMode, false);
                        fieldAnalyzer = new IgnoreCaseAnalyzer();
                    } else
                    {
                        fieldPattern =
                                LuceneQueryBuilder.adaptQuery(criterion.getValue(),
                                        useWildcardSearchMode);
                        fieldAnalyzer = searchAnalyzer;
                    }

                    fieldPatterns.add(fieldPattern);
                    fieldAnalyzers.add(fieldAnalyzer);
                }

                Query luceneQuery =
                        LuceneQueryBuilder.parseQuery(fieldNames, fieldPatterns, fieldAnalyzers);
                resultQuery.add(luceneQuery, occureCondition);
            } else
            {
                Date lower = parseDate(criterion.getValue());
                int offset = getTimeZoneOffset(criterion, lower);

                lower.setTime(lower.getTime() + offset);
                Date upper = new Date(lower.getTime() + 24 * 3600 * 1000);

                switch (criterion.getType())
                {
                    case EQUALS:
                        break;
                    case LESS_THAN_OR_EQUAL:
                        lower = new Date(0);
                        break;
                    case MORE_THAN_OR_EQUAL:
                        upper = new Date(Long.MAX_VALUE);
                        break;
                }

                String fieldName = fieldNames.get(0);
                DateBridge bridge = new DateBridge(Resolution.SECOND);
                TermRangeQuery q =
                        new TermRangeQuery(fieldName, bridge.objectToString(lower),
                                bridge.objectToString(upper), true, true);
                resultQuery.add(q, occureCondition);
            }
        }
        for (DetailedSearchAssociationCriteria association : associations)
        {
            String fieldName = getIndexFieldName(association);
            List<String> searchPatterns = extractAssociationPatterns(association);
            Query luceneQuery =
                    LuceneQueryBuilder.parseQuery(fieldName, searchPatterns, searchAnalyzer);
            resultQuery.add(luceneQuery, occureCondition);
        }
        return resultQuery;
    }

    private int getTimeZoneOffset(DetailedSearchCriterion criterion, Date lower)
    {
        String tzs = criterion.getTimeZone();
        if (tzs.equals(DetailedSearchCriterion.SERVER_TIMEZONE))
        {
            return -TimeZone.getDefault().getOffset(lower.getTime());
        }

        if (tzs.startsWith("+"))
        {
            tzs = tzs.substring(1);
        } else if (tzs.equals("Z"))
        {
            tzs = "0";
        }

        int offset;
        try
        {
            offset = (int) (-Double.parseDouble(tzs) * 3600000);
        } catch (NumberFormatException e)
        {
            offset = 0;
        }
        return offset;
    }

    private Date parseDate(String dateAsString)
    {
        for (String format : DATE_FORMATS)
        {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

            try
            {
                return sdf.parse(dateAsString);
            } catch (ParseException ex)
            {
                // ignore, try next format
            }
        }
        throw new UserFailureException("Couldn't parse date '" + dateAsString
                + "'. It has to match one of the following formats: " + Arrays.asList(DATE_FORMATS));
    }

    private List<String> extractAssociationPatterns(DetailedSearchAssociationCriteria association)
    {
        List<String> result = new ArrayList<String>();
        for (Long id : association.getIds())
        {
            result.add(id.toString());
        }
        return result;
    }

    private Occur createOccureCondition(SearchCriteriaConnection connection)
    {
        switch (connection)
        {
            case MATCH_ALL:
                return Occur.MUST;
            case MATCH_ANY:
                return Occur.SHOULD;
            default:
                throw InternalErr.error("unknown enum " + connection);
        }
    }

    private List<String> getIndexFieldNames(DetailedSearchField searchField)
    {
        DetailedSearchFieldKind fieldKind = searchField.getKind();
        switch (fieldKind)
        {
            case ANY_PROPERTY:
                return getPropertyIndexFields(searchField);
            case ANY_FIELD:
                List<String> fields = new ArrayList<String>();
                fields.addAll(getPropertyIndexFields(searchField));
                fields.addAll(getAllAttributeIndexFieldNames());
                return fields;
            case REGISTRATOR:
                return Arrays.asList(SearchFieldConstants.PREFIX_REGISTRATOR
                        + SearchFieldConstants.PERSON_USER_ID);
            default:
                return Arrays.asList(getSimpleFieldIndexName(searchField));
        }
    }

    private String getIndexFieldName(DetailedSearchAssociationCriteria association)
    {
        return IndexFieldNameHelper.getAssociationIndexField(entityKind,
                association.getEntityKind());
    }

    private final static EnumSet<DetailedSearchFieldKind> simpleFieldKinds = EnumSet.of(
            DetailedSearchFieldKind.ATTRIBUTE, DetailedSearchFieldKind.PROPERTY);

    private String getSimpleFieldIndexName(DetailedSearchField searchField)
    {
        assert simpleFieldKinds.contains(searchField.getKind()) : "simple field kind required";
        String indexFieldName = tryGetIndexFieldName(searchField);
        assert indexFieldName != null;
        return indexFieldName;
    }

    private List<String> getAllAttributeIndexFieldNames()
    {
        List<String> indexFieldNames = new ArrayList<String>();
        IAttributeSearchFieldKind[] attributeFieldKinds = getAllAttributeFieldKinds(entityKind);
        for (IAttributeSearchFieldKind attributeFieldKind : attributeFieldKinds)
        {
            DetailedSearchField attributeField =
                    DetailedSearchField.createAttributeField(attributeFieldKind);
            indexFieldNames.add(getSimpleFieldIndexName(attributeField));
        }
        return indexFieldNames;
    }

    private static IAttributeSearchFieldKind[] getAllAttributeFieldKinds(EntityKind entityKind)
    {
        return AttributeSearchFieldKindProvider.getAllAttributeFieldKinds(entityKind);
    }

    private List<String> getPropertyIndexFields(DetailedSearchField searchField)
    {
        assert searchField.getKind() != DetailedSearchFieldKind.ATTRIBUTE : "attribute field kind not allowed";
        return getPropertyIndexFieldNames(searchField.getAllEntityPropertyCodesOrNull());
    }

    private List<String> getPropertyIndexFieldNames(List<String> allPropertyCodes)
    {
        List<String> indexFieldNames = new ArrayList<String>();
        assert allPropertyCodes != null;
        for (String propertyCode : allPropertyCodes)
        {
            final DetailedSearchField searchField =
                    DetailedSearchField.createPropertyField(propertyCode);
            final String indexFieldName = tryGetIndexFieldName(searchField);
            assert indexFieldName != null;
            indexFieldNames.add(indexFieldName);
        }
        return indexFieldNames;
    }

    // returns the field name in the index for the specified query field
    private String tryGetIndexFieldName(DetailedSearchField searchField)
    {
        DetailedSearchFieldKind fieldKind = searchField.getKind();
        switch (fieldKind)
        {
            case ATTRIBUTE:
                return IndexFieldNameHelper.getAttributeIndexField(entityKind,
                        searchField.getAttributeCode());
            case PROPERTY:
                return IndexFieldNameHelper.getPropertyIndexField(searchField.getPropertyCode());
            case ANY_PROPERTY:
            case ANY_FIELD:
                return null;
            default:
                throw InternalErr.error("unknown enum " + fieldKind);
        }
    }

}
