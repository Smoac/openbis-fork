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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

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
    private static final String NOT = "NOT";

    private static final String OR = "OR";

    private static final String AND = "AND";

    private static final char STAR = '*';

    private static final char SPACE = ' ';

    /** @throws UserFailureException when some search patterns are incorrect */
    public static Query createDetailedSearchQuery(DetailedSearchCriteria searchCriteria,
            EntityKind entityKind)
    {
        return DetailedQueryBuilder.createQuery(searchCriteria,
                DtoConverters.convertEntityKind(entityKind));
    }

    private static final char FIELD_SEPARATOR = ':';

    //
    // query adaptation
    //

    private static final char[] CHARS_ESCAPED_IN_WILCARD_MODE =
        { FIELD_SEPARATOR };

    // For now both wildcard and basic modes escape the same characters. If we decide
    // to escape wildcard characters in basic mode unescape the following code.
    // private static final char[] CHARS_ESCAPED_IN_BASIC_MODE =
    // { FIELD_SEPARATOR, '*', '?' };
    private static final char[] CHARS_ESCAPED_IN_BASIC_MODE = CHARS_ESCAPED_IN_WILCARD_MODE;

    public static String adaptQuery(String userQuery, boolean useWildcardSearchMode)
    {
        char[] escapedChars =
                (useWildcardSearchMode == true) ? CHARS_ESCAPED_IN_WILCARD_MODE
                        : CHARS_ESCAPED_IN_BASIC_MODE;
        String result = escapeQuery(userQuery, escapedChars);
        // add '*' wildcard at the beginning and at the end of the query if all conditions are met:
        // 1. in basic search mode
        // 2. query is not in quotes
        // 3. query doesn't contain '*'
        if (useWildcardSearchMode == false && isQuoted(result) == false
                && result.contains("*") == false)
        {
            result = addWildcards(result);
        }
        return result;
    }

    private static String addWildcards(String result)
    {
        String[] queryTokens = StringUtils.split(result, SPACE);
        List<String> transformedTokens = new ArrayList<String>();
        for (String qt : queryTokens)
        {
            if (qt.equals(AND) || qt.equals(OR) || qt.equals(NOT))
            {
                transformedTokens.add(qt);
            } else
            {
                transformedTokens.add(addWildcartdsToToken(qt));
            }
        }
        return StringUtils.join(transformedTokens, SPACE);
    }

    private static String addWildcartdsToToken(String token)
    {
        Collection<Character> tokenSeparators = CharacterHelper.getTokenSeparators();
        tokenSeparators.removeAll(new ArrayList<String>());
        String[] miniTokens = StringUtils.split(token, StringUtils.join(tokenSeparators, ""));
        List<String> transformedMiniTokens = new ArrayList<String>();
        for (String qt : miniTokens)
        {
            transformedMiniTokens.add(STAR + qt + STAR);
        }
        return '(' + StringUtils.join(transformedMiniTokens, SPACE + AND + SPACE) + ')';
    }

    private static boolean isQuoted(String result)
    {
        return result.startsWith("\"") && result.endsWith("\"");
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
