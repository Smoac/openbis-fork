/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A parser for SQL92 data definition language scripts. The result are columns lengths and checked constraints.
 * 
 * @author Bernd Rinn
 */
public class DBRestrictions
{
    private static final String CREATE_DOMAIN_PREFIX = "create domain ";

    private static final Pattern VARCHAR_PATTERN = Pattern.compile("varchar\\(([0-9]+)\\)");

    /** The prefix each <code>create table</code> statement starts with. */
    private static final String CREATE_TABLE_PREFIX = "create table ";

    private static final Pattern CREATE_TABLE_PATTERN =
            Pattern.compile(CREATE_TABLE_PREFIX + "([a-z,0-9,_]+) \\((.+)\\)");

    /** The prefix each <code>alter table</code> statement starts with (to add a constraint). */
    private static final String ALTER_TABLE_PREFIX = "alter table ";

    private static final Pattern CHECK_CONSTRAINT_PATTERN =
            Pattern.compile(ALTER_TABLE_PREFIX
                    + "([a-z,0-9,_]+) add constraint [a-z,0-9,_]+ check \\(([a-z,0-9,_]+) in \\((.+)\\)\\)");

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DBRestrictions.class);

    // @Private
    final Map<String, DBTableRestrictions> tableMap = new HashMap<String, DBTableRestrictions>();

    /**
     * A class that holds the restrictions put upon columns in a database table.
     */
    public static class DBTableRestrictions
    {
        final Map<String, Integer> columnLengthMap = new HashMap<String, Integer>();

        final Map<String, Set<String>> checkedConstraintsMap = new HashMap<String, Set<String>>();

        final Set<String> notNullSet = new HashSet<String>();

        public int getLength(String columnName)
        {
            final Integer columnLength = columnLengthMap.get(columnName);
            assert columnLength != null : "Illegal column " + columnName;
            return columnLength;
        }

        public Set<String> getCheckedConstaint(String columnName)
        {
            return checkedConstraintsMap.get(columnName);
        }

        public boolean hasNotNullConstraint(String columnName)
        {
            return notNullSet.contains(columnName);
        }
    }

    public DBRestrictions(String ddlScript)
    {
        final List<String> normalizedDDLScript = normalize(ddlScript);
        final Map<String, Integer> domains = parseDomains(normalizedDDLScript);
        parseColumnLength(normalizedDDLScript, domains);
        parserCheckedConstraints(normalizedDDLScript);
    }

    // @Private
    static List<String> normalize(String ddlScript)
    {
        final List<String> list = new ArrayList<String>();
        final SQLCommandTokenizer normalizer = new SQLCommandTokenizer(ddlScript);
        String command;
        do
        {
            command = normalizer.getNextCommand();
            if (command != null)
            {
                list.add(command);
            }
        } while (command != null);
        return list;
    }

    // @Private
    static Map<String, Integer> parseDomains(List<String> ddlScript)
    {
        final Map<String, Integer> domains = new HashMap<String, Integer>();
        for (String line : ddlScript)
        {
            if (line.startsWith(CREATE_DOMAIN_PREFIX))
            {
                final String[] words = StringUtils.split(line.substring(CREATE_DOMAIN_PREFIX.length()), ' ');
                if (words.length != 3 || "as".equals(words[1]) == false)
                {
                    operationLog.warn("line \"" + line + "\" looks like domain definition, but is ill-formed.");
                    continue;
                }
                final Matcher varCharMatcher = VARCHAR_PATTERN.matcher(words[2]);
                if (varCharMatcher.matches())
                {
                    domains.put(words[0], Integer.parseInt(varCharMatcher.group(1)));
                }
            }
        }

        return domains;
    }

    private void parseColumnLength(List<String> ddlScript, Map<String, Integer> domains)
    {
        for (String line : ddlScript)
        {
            final Matcher createTableMatcher = CREATE_TABLE_PATTERN.matcher(line);
            if (createTableMatcher.matches())
            {
                final String tableName = createTableMatcher.group(1);
                final String tableDefinition = createTableMatcher.group(2);
                final String[] columnDefinitions = StringUtils.split(tableDefinition, ',');
                for (String columnDefinition : columnDefinitions)
                {
                    parseColumnDefinition(columnDefinition, tableName, domains);
                }
            }
        }
    }

    private void parseColumnDefinition(String columnDefinition, final String tableName, Map<String, Integer> domains)
            throws NumberFormatException
    {
        final String[] words = StringUtils.split(columnDefinition, ' ');
        if (words.length < 2)
        {
            operationLog.warn("Invalid column definition \"" + columnDefinition + "\" for table " + tableName);
            return;
        }
        final String columnName = words[0];
        final String type = words[1];
        final Integer domainLength = domains.get(type);
        if (domainLength != null)
        {
            getTableRestrictions(tableName).columnLengthMap.put(columnName, domainLength);
        } else
        {
            final Matcher varCharMatcher = VARCHAR_PATTERN.matcher(type);
            if (varCharMatcher.matches())
            {
                getTableRestrictions(tableName).columnLengthMap.put(columnName, Integer.parseInt(varCharMatcher
                        .group(1)));
            }
        }
        final int nullIdx = ArrayUtils.indexOf(words, "null", 3);
        if (nullIdx != ArrayUtils.INDEX_NOT_FOUND && "not".equals(words[nullIdx - 1]))
        {
            getTableRestrictions(tableName).notNullSet.add(columnName);
        }
    }

    private void parserCheckedConstraints(List<String> ddlScript)
    {
        for (String line : ddlScript)
        {
            final Matcher checkedConstraintMatcher = CHECK_CONSTRAINT_PATTERN.matcher(line);
            if (checkedConstraintMatcher.matches())
            {
                final String tableName = checkedConstraintMatcher.group(1);
                final String columnName = checkedConstraintMatcher.group(2);
                final String alternativesStr = checkedConstraintMatcher.group(3);
                final String[] alternatives = StringUtils.split(alternativesStr, ',');
                final Set<String> alternativeSet = new HashSet<String>();
                for (String alternative : alternatives)
                {
                    if (alternative.charAt(0) != '\'' || alternative.charAt(alternative.length() - 1) != '\'')
                    {
                        operationLog.warn("Invalid alternatives definition \"" + alternative + "\" for column "
                                + columnName + " of table " + tableName);
                        continue;
                    }
                    alternativeSet.add(alternative.substring(1, alternative.length() - 1));
                }
                getTableRestrictions(tableName).checkedConstraintsMap.put(columnName, alternativeSet);
            }
        }
    }

    /**
     * @return The table restrictions of <var>tableName</var>
     */
    // @Private
    DBTableRestrictions getTableRestrictions(String tableName)
    {
        DBTableRestrictions table = tableMap.get(tableName);
        if (table == null)
        {
            table = new DBTableRestrictions();
            tableMap.put(tableName, table);
        }
        return table;
    }

    /**
     * Check <var>value</var> against the restrictions (maximal length and possible check constraints) of column
     * <var>columnName</var> of table <var>tableName</var>.
     * 
     * @throws UserFailureException If the <var>value</var> violates the constraints.
     */
    public void check(String tableName, String columnName, String value) throws UserFailureException
    {
        final DBTableRestrictions restrictions = tableMap.get(tableName);
        assert restrictions != null : "Illegal table " + tableName;
        final int maxLength = restrictions.getLength(columnName);
        if (value == null)
        {
            if (restrictions.hasNotNullConstraint(columnName))
            {
                final String msg = String.format("Value 'NULL' not allowed for column %s.%s.", tableName, columnName);
                operationLog.warn("Violation of database constraints detected: " + msg);
                throw new UserFailureException(msg);
            }
            return;
        }
        final Set<String> checkedConstraint = restrictions.getCheckedConstaint(columnName);
        if (checkedConstraint != null && checkedConstraint.contains(value) == false)
        {
            final String msg =
                    String.format("Value '%s' is not one of the allowed alternatives %s of column %s.%s.", value,
                            toString(checkedConstraint), tableName, columnName);
            operationLog.warn("Violation of database constraints detected: " + msg);
            throw new UserFailureException(msg);
        }
        if (value.length() > maxLength)
        {
            final String msg =
                    String.format("Value '%s' is longer than the maximum length %d of column %s.%s.", value, maxLength,
                            tableName, columnName);
            operationLog.warn("Violation of database constraints detected: " + msg);
            throw new UserFailureException(msg);
        }
    }

    private String toString(Set<String> alternatives)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        for (String alternative : alternatives)
        {
            builder.append(alternative);
            builder.append(", ");
        }
        builder.setLength(builder.length() - 2);
        builder.append(" }");
        return builder.toString();
    }
}
