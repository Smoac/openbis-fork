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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

import static ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants.CODE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants.PREFIX_EXPERIMENT;
import static ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants.PREFIX_EXPERIMENT_TYPE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants.PREFIX_GROUP;
import static ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants.PREFIX_PROJECT;
import static ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants.PREFIX_SAMPLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants.PREFIX_SAMPLE_TYPE;

import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;

import ch.systemsx.cisd.common.exceptions.InternalErr;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriterion.DataSetSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetSearchCriterion.DataSetSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteria.CriteriaConnection;
import ch.systemsx.cisd.openbis.generic.shared.dto.hibernate.SearchFieldConstants;

/**
 * @author Tomasz Pylak
 */
public class LuceneQueryBuilder
{
    /** @throws UserFailureException when some search patterns are incorrect */
    public static Query createQuery(SearchCriteria dataSetCriteria) throws UserFailureException
    {
        List<DataSetSearchCriterion> criteria = dataSetCriteria.getCriteria();
        Occur occureCondition = createOccureCondition(dataSetCriteria.getConnection());

        SearchAnalyzer analyzer = createSearchAnalyzer();
        BooleanQuery resultQuery = new BooleanQuery();

        for (DataSetSearchCriterion criterion : criteria)
        {
            String fieldName = getIndexFieldName(criterion.getField());
            String searchPattern = LuceneQueryBuilder.disableFieldQuery(criterion.getValue());

            Query luceneQuery = parseQuery(fieldName, searchPattern, analyzer);
            resultQuery.add(luceneQuery, occureCondition);
        }
        return resultQuery;
    }

    private static Occur createOccureCondition(CriteriaConnection connection)
    {
        switch (connection)
        {
            case AND:
                return Occur.MUST;
            case OR:
                return Occur.SHOULD;
            default:
                throw InternalErr.error("unknown enum " + connection);
        }
    }

    // returns the field name in the index for the specified dataset query field
    private static String getIndexFieldName(DataSetSearchField searchField)
    {
        DataSetSearchFieldKind fieldKind = searchField.getKind();
        switch (fieldKind)
        {
            case DATA_SET_TYPE:
                return SearchFieldConstants.PREFIX_DATASET_TYPE + CODE;
            case FILE_TYPE:
                return SearchFieldConstants.PREFIX_FILE_FORMAT_TYPE + CODE;
            case GROUP:
                return PREFIX_EXPERIMENT + PREFIX_PROJECT + PREFIX_GROUP + CODE;
            case PROJECT:
                return PREFIX_EXPERIMENT + PREFIX_PROJECT + CODE;
            case EXPERIMENT:
                return PREFIX_EXPERIMENT + CODE;
            case EXPERIMENT_TYPE:
                return PREFIX_EXPERIMENT + PREFIX_EXPERIMENT_TYPE + CODE;
            case EXPERIMENT_PROPERTY:
                return PREFIX_EXPERIMENT + getPropertyIndexField(searchField.tryGetPropertyCode());
            case SAMPLE:
                return PREFIX_SAMPLE + CODE;
            case SAMPLE_TYPE:
                return PREFIX_SAMPLE + PREFIX_SAMPLE_TYPE + CODE;
            case SAMPLE_PROPERTY:
                return PREFIX_SAMPLE + getPropertyIndexField(searchField.tryGetPropertyCode());
            default:
                throw InternalErr.error("unknown enum " + fieldKind);
        }
    }

    private static String getPropertyIndexField(String propertyCode)
    {
        assert propertyCode != null : "property code is null";
        return SearchFieldConstants.PREFIX_PROPERTIES + propertyCode;
    }

    // disables field query by escaping all field separator characters.
    public static String disableFieldQuery(String userQuery)
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

    /**
     * All the search query parsers should use this method to get the analyzer, because this is the
     * one which is used to build the index.
     */
    public static SearchAnalyzer createSearchAnalyzer()
    {
        return new SearchAnalyzer();
    }

    public static Query parseQuery(final String indexFieldName, final String searchPattern,
            Analyzer analyzer) throws UserFailureException
    {
        final QueryParser parser = new QueryParser(indexFieldName, analyzer);
        parser.setAllowLeadingWildcard(true);
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        try
        {
            return parser.parse(searchPattern);
        } catch (ParseException ex)
        {
            throw new UserFailureException(String.format("Search pattern '%s' is invalid.",
                    searchPattern), ex);
        }
    }
}
