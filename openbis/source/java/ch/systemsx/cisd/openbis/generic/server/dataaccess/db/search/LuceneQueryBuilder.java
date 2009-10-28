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

import ch.rinn.restrictions.Private;
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

    public static String adaptQuery(String userQuery)
    {
        String result = disableFieldQuery(userQuery);
        result = replaceWordSeparators(result, SeparatorSplitterTokenFilter.WORD_SEPARATORS);
        return result;
    }

    @Private
    static String replaceWordSeparators(String query, char[] wordSeparators)
    {
        if (looksLikeNumber(query))
        {
            return query;
        }
        String queryTrimmed = removeSurroundingWordSeparators(query, wordSeparators);
        String charsRegexp = createAnyWordSeparatorRegexp(wordSeparators);
        String queryWithoutSeparators = queryTrimmed.replaceAll(charsRegexp, " AND ");
        if (queryWithoutSeparators.equals(queryTrimmed))
        {
            return queryTrimmed;
        } else
        {
            return "(" + queryWithoutSeparators + ")";
        }
    }

    private static boolean looksLikeNumber(String query)
    {
        return query.length() > 0 && Character.isDigit(query.charAt(0))
                && Character.isDigit(query.charAt(query.length() - 1));
    }

    private static String createAnyWordSeparatorRegexp(char[] wordSeparators)
    {
        String charsRegexp = "[";
        for (int i = 0; i < wordSeparators.length; i++)
        {
            charsRegexp += "\\" + wordSeparators[i];
        }
        charsRegexp += "]";
        return charsRegexp;
    }

    private static String removeSurroundingWordSeparators(String query, char[] wordSeparators)
    {
        int startIx = 0;
        while (startIx < query.length() && isSeparator(query.charAt(startIx), wordSeparators))
        {
            startIx++;
        }
        int endIx = query.length();
        while (endIx > 0 && isSeparator(query.charAt(endIx - 1), wordSeparators))
        {
            endIx--;
        }
        return query.substring(startIx, endIx);
    }

    private static boolean isSeparator(char ch, char[] wordSeparators)
    {
        for (int i = 0; i < wordSeparators.length; i++)
        {
            if (ch == wordSeparators[i])
            {
                return true;
            }
        }
        return false;
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
