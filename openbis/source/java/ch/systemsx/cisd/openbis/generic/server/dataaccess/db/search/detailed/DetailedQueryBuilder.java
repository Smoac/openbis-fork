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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.FieldInfo.DocValuesType;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.hibernate.search.annotations.Resolution;
import org.hibernate.search.bridge.builtin.StringEncodingDateBridge;
import org.hibernate.search.util.impl.PassThroughAnalyzer;

import ch.systemsx.cisd.common.exceptions.InternalErr;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.MetaprojectSearch;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.LuceneQueryBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.AttributeSearchFieldKindProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CompareType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SortableNumberBridgeUtils;
import ch.systemsx.cisd.openbis.generic.shared.search.IgnoreCaseAnalyzer;

/**
 * Builder for detailed lucene queries for different entity kinds.
 * 
 * @author Piotr Buczek
 */
public class DetailedQueryBuilder
{

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DetailedQueryBuilder.class);

    /**
     * @param associations // TODO move to DetailedSearchCriteria
     * @throws UserFailureException when some search patterns are incorrect
     */
    public static Query createQuery(String userId, DetailedSearchCriteria searchCriteria,
            EntityKind entityKind, List<IAssociationCriteria> associations, Map<String, DocValuesType> fieldTypes)
            throws UserFailureException
    {
        final DetailedQueryBuilder builder = new DetailedQueryBuilder(entityKind);
        final Query resultQuery = builder.createQuery(userId, searchCriteria, associations, fieldTypes);
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
            List<IAssociationCriteria> associations, Map<String, DocValuesType> fieldTypes)
    {
        boolean useWildcardSearchMode = searchCriteria.isUseWildcardSearchMode();
        List<DetailedSearchCriterion> criteria = searchCriteria.getCriteria();
        Occur occureCondition = createOccureCondition(searchCriteria.getConnection());

        Analyzer searchAnalyzer = LuceneQueryBuilder.createSearchAnalyzer();
        BooleanQuery resultQuery = new BooleanQuery();
        for (DetailedSearchCriterion criterion : criteria)
        {
            List<String> fieldNames = getIndexFieldNames(criterion.getField(), entityKind);
            Collection<String> values = criterion.getValues();

            if (criterion.getTimeZone() == null)
            {
                List<String> fieldPatterns = new ArrayList<String>();
                List<Analyzer> fieldAnalyzers = new ArrayList<Analyzer>();
                List<Occur> fieldOccur = new ArrayList<Occur>();
                List<String> fieldValueNames = new ArrayList<String>();

                if (values != null)
                {
                    for (String value : values)
                    {
                        for (String fieldName : fieldNames)
                        {
                            String fieldPattern = null;
                            Analyzer fieldAnalyzer = null;
                            boolean isNumeric = fieldTypes.get(fieldName) == DocValuesType.SORTED_NUMERIC;
                            if (MetaprojectSearch.isMetaprojectField(fieldName))
                            {
                                String fieldUserQuery =
                                        MetaprojectSearch.getMetaprojectUserQuery(value, userId);
                                fieldPattern = LuceneQueryBuilder.adaptQuery(fieldUserQuery,
                                        useWildcardSearchMode, false);
                                fieldAnalyzer = new IgnoreCaseAnalyzer();
                            } else if (isNumeric && criterion.getType() != null && SortableNumberBridgeUtils.isValidNumber(value))
                            {
                                fieldPattern = getRangeNumberQuery(criterion.getType(), value, fieldPattern);
                                fieldAnalyzer = PassThroughAnalyzer.INSTANCE;
                            } else
                            {
                                fieldPattern = LuceneQueryBuilder.adaptQuery(value, useWildcardSearchMode);
                                fieldAnalyzer = getFieldAnalyzer(entityKind, fieldName, searchAnalyzer);
                            }

                            fieldPatterns.add(fieldPattern);
                            fieldAnalyzers.add(fieldAnalyzer);
                            fieldOccur.add(criterion.isNegated() ? Occur.MUST_NOT : Occur.SHOULD);
                            fieldValueNames.add(fieldName);
                        }
                    }
                }

                Query luceneQuery = LuceneQueryBuilder.parseQuery(criterion.getType(), fieldValueNames,
                        fieldPatterns, fieldAnalyzers, fieldOccur);
                resultQuery.add(luceneQuery, occureCondition);
            } else
            {
                if (false == StringUtils.isEmpty(criterion.getValue()))
                {
                    DateRangeCalculator rangeCalculator = new DateRangeCalculator(criterion.getValue(), criterion.getTimeZone(), criterion.getType());
                    String fieldName = fieldNames.get(0);
                    StringEncodingDateBridge bridge = new StringEncodingDateBridge(Resolution.SECOND);
                    TermRangeQuery q =
                            TermRangeQuery.newStringRange(fieldName, bridge.objectToString(rangeCalculator.getLowerDate()),
                                    bridge.objectToString(rangeCalculator.getUpperDate()), true, true);
                    resultQuery.add(q, occureCondition);
                }
            }
        }

        for (IAssociationCriteria association : associations)
        {
            String fieldName = getIndexFieldName(association);
            Query luceneQuery = LuceneQueryBuilder.parseQuery(fieldName, association.getSearchPatterns(), searchAnalyzer);
            resultQuery.add(luceneQuery, occureCondition);
        }

        return resultQuery;
    }

    private String getRangeNumberQuery(CompareType compareType, String value, String fieldPattern)
    {
        String parsedNumberValue = SortableNumberBridgeUtils.getNumberForLucene(value);
        switch (compareType)
        {
            case LESS_THAN:
                return "{* TO " + parsedNumberValue + "}";
            case LESS_THAN_OR_EQUAL:
                return "[* TO " + parsedNumberValue + "]";
            case EQUALS:
                return "[" + parsedNumberValue + " TO " + parsedNumberValue + "]";
            case MORE_THAN_OR_EQUAL:
                return "[" + parsedNumberValue + " TO *]";
            case MORE_THAN:
                return "{" + parsedNumberValue + " TO *}";
        }
        return fieldPattern;
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

    public static List<String> getIndexFieldNames(List<DetailedSearchCriterion> criteria, EntityKind entityKind)
    {
        List<String> fieldNames = new ArrayList<String>();
        for (DetailedSearchCriterion criterion : criteria)
        {
            fieldNames.addAll(getIndexFieldNames(criterion.getField(), entityKind));
        }
        return fieldNames;
    }

    private static List<String> getIndexFieldNames(DetailedSearchField searchField, EntityKind entityKind)
    {
        DetailedSearchFieldKind fieldKind = searchField.getKind();
        switch (fieldKind)
        {
            case ANY_PROPERTY:
                return getPropertyIndexFields(searchField, entityKind);
            case ANY_FIELD:
                List<String> fields = new ArrayList<String>();
                fields.addAll(getPropertyIndexFields(searchField, entityKind));
                fields.addAll(getAllAttributeIndexFieldNames(entityKind));
                return fields;
            case REGISTRATOR:
                return Arrays.asList(SearchFieldConstants.PREFIX_REGISTRATOR
                        + SearchFieldConstants.PERSON_USER_ID);
            default:
                return Arrays.asList(getSimpleFieldIndexName(searchField, entityKind));
        }
    }

    private String getIndexFieldName(IAssociationCriteria association)
    {
        return IndexFieldNameHelper.getAssociationIndexField(entityKind,
                association.getEntityKind());
    }

    private final static EnumSet<DetailedSearchFieldKind> simpleFieldKinds = EnumSet.of(
            DetailedSearchFieldKind.ATTRIBUTE, DetailedSearchFieldKind.PROPERTY);

    private static String getSimpleFieldIndexName(DetailedSearchField searchField, EntityKind entityKind)
    {
        assert simpleFieldKinds.contains(searchField.getKind()) : "simple field kind required";
        String indexFieldName = tryGetIndexFieldName(searchField, entityKind);
        assert indexFieldName != null;
        return indexFieldName;
    }

    private static List<String> getAllAttributeIndexFieldNames(EntityKind entityKind)
    {
        List<String> indexFieldNames = new ArrayList<String>();
        IAttributeSearchFieldKind[] attributeFieldKinds = getAllAttributeFieldKinds(entityKind);
        for (IAttributeSearchFieldKind attributeFieldKind : attributeFieldKinds)
        {
            DetailedSearchField attributeField =
                    DetailedSearchField.createAttributeField(attributeFieldKind);
            indexFieldNames.add(getSimpleFieldIndexName(attributeField, entityKind));
        }
        return indexFieldNames;
    }

    private static IAttributeSearchFieldKind[] getAllAttributeFieldKinds(EntityKind entityKind)
    {
        return AttributeSearchFieldKindProvider.getAllAttributeFieldKinds(entityKind);
    }

    private static List<String> getPropertyIndexFields(DetailedSearchField searchField, EntityKind entityKind)
    {
        assert searchField.getKind() != DetailedSearchFieldKind.ATTRIBUTE : "attribute field kind not allowed";
        return getPropertyIndexFieldNames(searchField.getAllEntityPropertyCodesOrNull(), entityKind);
    }

    private static List<String> getPropertyIndexFieldNames(List<String> allPropertyCodes, EntityKind entityKind)
    {
        List<String> indexFieldNames = new ArrayList<String>();
        assert allPropertyCodes != null;
        for (String propertyCode : allPropertyCodes)
        {
            final DetailedSearchField searchField =
                    DetailedSearchField.createPropertyField(propertyCode);
            final String indexFieldName = tryGetIndexFieldName(searchField, entityKind);
            assert indexFieldName != null;
            indexFieldNames.add(indexFieldName);
        }
        return indexFieldNames;
    }

    // returns the field name in the index for the specified query field
    private static String tryGetIndexFieldName(DetailedSearchField searchField, EntityKind entityKind)
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

    private static Analyzer getFieldAnalyzer(EntityKind entityKind, String fieldName, Analyzer defaultAnalyzer)
    {
        if ((EntityKind.SAMPLE.equals(entityKind) || EntityKind.EXPERIMENT.equals(entityKind)) && SearchFieldConstants.IDENTIFIER.equals(fieldName))
        {
            return new IgnoreCaseAnalyzer();
        } else
        {
            return defaultAnalyzer;
        }
    }

}
