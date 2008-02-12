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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A parser for SQL92 data definition language scripts. The result are columns lengths and checked constraints.
 * 
 * @author Bernd Rinn
 */
public class DBRestrictionParser
{
    private static final String CREATE_DOMAIN_PREFIX = "create domain ";

    private static final Pattern VARCHAR_PATTERN = Pattern.compile("(varchar|character varying)\\(([0-9]+)\\).*");

    /** The prefix each <code>create table</code> statement starts with. */
    private static final String CREATE_TABLE_PREFIX = "create table ";

    private static final Pattern CREATE_TABLE_PATTERN =
            Pattern.compile(CREATE_TABLE_PREFIX + "([a-z,0-9,_]+) \\((.+)\\)");

    private static final Pattern NOT_NULL_TABLE_PATTERN =
            Pattern.compile("\\w+ ((default .+ not null)|(not null)|(not null .+ default.+))");
    
    /** The prefix each <code>alter table</code> statement starts with (to add a constraint). */
    private static final String ALTER_TABLE_PREFIX = "alter table ";

    private static final Pattern CHECK_CONSTRAINT_PATTERN =
            Pattern.compile(ALTER_TABLE_PREFIX
                    + "([a-z,0-9,_]+) add constraint [a-z,0-9,_]+ check \\(([a-z,0-9,_]+) in \\((.+)\\)\\)");

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DBRestrictionParser.class);

    // @Private
    final Map<String, DBTableRestrictions> tableRestrictionMap = new HashMap<String, DBTableRestrictions>();

    public DBRestrictionParser(String ddlScript)
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
                String domainDefinition = line.substring(CREATE_DOMAIN_PREFIX.length());
                int indexOfAS = domainDefinition.indexOf("as");
                if (indexOfAS < 0)
                {
                    operationLog.warn("line \"" + line
                            + "\" starts like a domain definition, but key word 'AS' is missing.");
                    continue;
                }
                String domainName = domainDefinition.substring(0, indexOfAS).trim();
                domainDefinition = domainDefinition.substring(indexOfAS + 2).trim();
                final Matcher varCharMatcher = VARCHAR_PATTERN.matcher(domainDefinition);
                if (varCharMatcher.matches())
                {
                    domains.put(domainName, Integer.parseInt(varCharMatcher.group(2)));
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
        if (columnDefinition.startsWith("constraint "))
        {
            return;
        }
        int indexOfFirstSpace = columnDefinition.indexOf(' ');
        if (indexOfFirstSpace < 0)
        {
            operationLog.warn("Invalid column definition \"" + columnDefinition + "\" for table " + tableName);
            return;
        }
        String columnName = columnDefinition.substring(0, indexOfFirstSpace).trim();
        if (columnName.startsWith("\"") && columnName.endsWith("\""))
        {
            columnName = columnName.substring(1, columnName.length() - 1);
        }
        final String typeDefinition = columnDefinition.substring(indexOfFirstSpace).trim();
        final Matcher varCharMatcher = VARCHAR_PATTERN.matcher(typeDefinition);
        if (varCharMatcher.matches())
        {
            getTableRestrictions(tableName).columnLengthMap.put(columnName, Integer.parseInt(varCharMatcher
                    .group(2)));
        } else
        {
            final Integer domainLength = domains.get(StringUtils.split(typeDefinition, ' ')[0]);
            if (domainLength != null)
            {
                getTableRestrictions(tableName).columnLengthMap.put(columnName, domainLength);
            }
        }
        
        if (NOT_NULL_TABLE_PATTERN.matcher(typeDefinition).matches())
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
        DBTableRestrictions table = tableRestrictionMap.get(tableName);
        if (table == null)
        {
            table = new DBTableRestrictions();
            tableRestrictionMap.put(tableName, table);
        }
        return table;
    }

    public Map<String, DBTableRestrictions> getDBRestrictions()
    {
        return Collections.unmodifiableMap(tableRestrictionMap);
    }
    
}
