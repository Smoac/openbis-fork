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

import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.detailed.DetailedQueryBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;

/**
 * @author Tomasz Pylak
 */
public class LuceneQueryBuilder
{
    /** @throws UserFailureException when some search patterns are incorrect */
    public static Query createDetailedSearchQuery(DetailedSearchCriteria searchCriteria,
            EntityKind entityKind)
    {
        return DetailedQueryBuilder.createQuery(searchCriteria, DtoConverters
                .convertEntityKind(entityKind));
    }

    private static final char FIELD_SEPARATOR = ':';

    //
    // query adaptation
    //

    // In wildcard mode field separator character needs to be escaped to disable field query.
    // In basic mode wildcard characters in the query need to be escaped too.

    private static final char[] CHARS_ESCAPED_IN_WILCARD_MODE =
        { FIELD_SEPARATOR };

    private static final char[] CHARS_ESCAPED_IN_BASIC_MODE =
        { FIELD_SEPARATOR, '*', '?' };

    public static String adaptQuery(String userQuery, boolean useWildcardSearchMode)
    {
        char[] escapedChars =
                (useWildcardSearchMode == true) ? CHARS_ESCAPED_IN_WILCARD_MODE
                        : CHARS_ESCAPED_IN_BASIC_MODE;
        String result = escapeQuery(userQuery, escapedChars);
        // add '*' wildcard at the beginning and at the end of the query in basic search mode
        if (useWildcardSearchMode == false)
        {
            result = '*' + result + '*';
        }
        return result;
    }

    /**
     * Escapes <var>escapedChars</var> characters in the query.
     */
    private static String escapeQuery(String userQuery, char... escapedChars)
    {
        char escapeChar = '\\';
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < userQuery.length(); i++)
        {
            char ch = userQuery.charAt(i);
            for (char escapedChar : escapedChars)
            {
                if (ch == escapedChar && (i == 0 || userQuery.charAt(i - 1) != escapeChar))
                {
                    // add escape character if there is none
                    sb.append(escapeChar);
                }
            }
            sb.append(ch);
        }
        return sb.toString();
    }
    
    //

    /**
     * All the search query parsers should use this method to get the analyzer, because this is the
     * one which is used to build the index.
     */
    public static Analyzer createSearchAnalyzer()
    {
        return new SearchAnalyzer();
    }

    public static Query parseQuery(final String fieldName, final String searchPattern,
            Analyzer analyzer) throws UserFailureException
    {
        final QueryParser parser = new QueryParser(fieldName, analyzer);
        return parseQuery(searchPattern, searchPattern, parser);
    }

    // creates a query where any field matches the given pattern
    public static Query parseQuery(final List<String> fieldNames, final String searchPattern,
            Analyzer analyzer) throws UserFailureException
    {
        BooleanQuery resultQuery = new BooleanQuery();
        for (String fieldName : fieldNames)
        {
            Query query = parseQuery(fieldName, searchPattern, analyzer);
            resultQuery.add(query, Occur.SHOULD);
        }
        return resultQuery;
    }

    private static Query parseQuery(final String searchPattern, String wholeQuery,
            final QueryParser parser)
    {
        parser.setAllowLeadingWildcard(true);
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        try
        {
            return parser.parse(wholeQuery);
        } catch (ParseException ex)
        {
            throw new UserFailureException(String.format("Search pattern '%s' is invalid.",
                    searchPattern), ex);
        }
    }
}
