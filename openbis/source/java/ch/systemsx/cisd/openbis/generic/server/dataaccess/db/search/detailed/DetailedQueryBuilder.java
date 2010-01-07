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
import java.util.EnumSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;

import ch.systemsx.cisd.common.exceptions.InternalErr;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.LuceneQueryBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.AttributeSearchFieldKindProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;

/**
 * Builder for detailed lucene queries for different entity kinds.
 * 
 * @author Piotr Buczek
 */
public class DetailedQueryBuilder
{
    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DetailedQueryBuilder.class);

    /** @throws UserFailureException when some search patterns are incorrect */
    public static Query createQuery(DetailedSearchCriteria searchCriteria, EntityKind entityKind)
            throws UserFailureException
    {
        final DetailedQueryBuilder builder = new DetailedQueryBuilder(entityKind);
        final Query resultQuery = builder.createQuery(searchCriteria);
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

    private Query createQuery(DetailedSearchCriteria searchCriteria)
    {
        List<DetailedSearchCriterion> criteria = searchCriteria.getCriteria();
        Occur occureCondition = createOccureCondition(searchCriteria.getConnection());

        Analyzer analyzer = LuceneQueryBuilder.createSearchAnalyzer();
        BooleanQuery resultQuery = new BooleanQuery();
        for (DetailedSearchCriterion criterion : criteria)
        {
            List<String> fieldNames = getIndexFieldNames(criterion.getField());
            // TODO 2010-01-07, PTR: pass search mode
            String searchPattern = LuceneQueryBuilder.adaptQuery(criterion.getValue(), true);
            Query luceneQuery = LuceneQueryBuilder.parseQuery(fieldNames, searchPattern, analyzer);
            resultQuery.add(luceneQuery, occureCondition);
        }
        return resultQuery;
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
            default:
                return Arrays.asList(getSimpleFieldIndexName(searchField));
        }
    }

    private final static EnumSet<DetailedSearchFieldKind> simpleFieldKinds =
            EnumSet.of(DetailedSearchFieldKind.ATTRIBUTE, DetailedSearchFieldKind.PROPERTY);

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
                return IndexFieldNameHelper.getAttributeIndexField(entityKind, searchField
                        .getAttributeCode());
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
