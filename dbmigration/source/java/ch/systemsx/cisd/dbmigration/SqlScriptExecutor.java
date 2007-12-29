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

package ch.systemsx.cisd.dbmigration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import ch.systemsx.cisd.common.db.ISqlScriptExecutor;

/**
 * Implementation of {@link ISqlScriptExecutor}.
 * 
 * @author Franz-Josef Elmer
 */
public class SqlScriptExecutor extends JdbcDaoSupport implements ISqlScriptExecutor
{
    /** Gives better error messages, but is a lot slower. */
    private final boolean singleStepMode;
    
    public SqlScriptExecutor(DataSource dataSource, boolean singleStepMode)
    {
        setDataSource(dataSource);
        this.singleStepMode = singleStepMode;
    }

    public void execute(String sqlScript)
    {
        if (singleStepMode)
        {
            String lastSqlStatement = "";
            for (String sqlStatement : splitStatements(sqlScript))
            {
                try
                {
                    getJdbcTemplate().execute(sqlStatement);
                } catch (BadSqlGrammarException ex2)
                {
                    throw new BadSqlGrammarException(getTask(ex2), lastSqlStatement + ">-->" + sqlStatement + "<--<",
                            getCause(ex2));
                }
                lastSqlStatement = sqlStatement;
            }
        } else
        {
            getJdbcTemplate().execute(sqlScript);
        }
    }

    private String getTask(BadSqlGrammarException ex)
    {
        final String msg = ex.getMessage();
        final int endIdx = msg.indexOf("; bad SQL grammar [");
        if (endIdx > 0)
        {
            return msg.substring(0, endIdx);
        } else
        {
            return msg;
        }
    }

    private SQLException getCause(BadSqlGrammarException ex)
    {
        final Throwable cause = ex.getCause();
        if (cause instanceof SQLException)
        {
            return (SQLException) cause;
        } else
        {
            throw new Error("Cause of BadSqlGrammarException needs to be a SQLException.", cause);
        }
    }

    private List<String> splitStatements(String sqlScript)
    {
        final String[] lines = StringUtils.split(sqlScript, '\n');
        final List<String> statements = new ArrayList<String>(lines.length);
        StringBuilder statement = new StringBuilder();
        for (int i = 0; i < lines.length; ++i)
        {
            String line = lines[i];
            final int commentStart = line.indexOf("--");
            if (commentStart >= 0)
            {
                line = line.substring(0, commentStart);
            }
            statement.append(line.trim());
            statement.append(' ');
            if (statement.length() > 1 && statement.charAt(statement.length() - 2) == ';')
            {
                statements.add(statement.toString());
                statement.setLength(0);
            }
        }
        return statements;
    }

}
