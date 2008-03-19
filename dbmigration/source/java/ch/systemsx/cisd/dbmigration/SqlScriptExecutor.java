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

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import ch.systemsx.cisd.common.Script;
import ch.systemsx.cisd.common.db.ISqlScriptExecutionLogger;
import ch.systemsx.cisd.common.db.ISqlScriptExecutor;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Implementation of {@link ISqlScriptExecutor}.
 * 
 * @author Franz-Josef Elmer
 */
public class SqlScriptExecutor extends JdbcDaoSupport implements ISqlScriptExecutor
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, SqlScriptExecutor.class);

    /** Gives better error messages, but is a lot slower. */
    private final boolean singleStepMode;

    public SqlScriptExecutor(DataSource dataSource, boolean singleStepMode)
    {
        setDataSource(dataSource);
        this.singleStepMode = singleStepMode;
    }

    public void execute(Script sqlScript, boolean honorSingleStepMode,
            ISqlScriptExecutionLogger loggerOrNull)
    {
        if (loggerOrNull != null)
        {
            loggerOrNull.logStart(sqlScript);
        }
        try
        {
            final String sqlScriptCode = sqlScript.getCode();
            if (singleStepMode && honorSingleStepMode)
            {
                String lastSqlStatement = "";
                for (String sqlStatement : DBUtilities.splitSqlStatements(sqlScriptCode))
                {
                    try
                    {
                        execute(sqlStatement);
                    } catch (BadSqlGrammarException ex2)
                    {
                        throw new BadSqlGrammarException(getTask(ex2), lastSqlStatement + ">-->"
                                + sqlStatement + "<--<", getCause(ex2));
                    } catch (UncategorizedSQLException ex2)
                    {
                        throw new UncategorizedSQLException(getTask(ex2), lastSqlStatement + ">-->"
                                + sqlStatement + "<--<", getCause(ex2));
                    }
                    lastSqlStatement = sqlStatement;
                }
            } else
            {
                execute(sqlScriptCode);
            }
            if (loggerOrNull != null)
            {
                loggerOrNull.logSuccess(sqlScript);
            }
        } catch (Throwable t)
        {
            operationLog.error("Executing script '" + sqlScript.getName() + "', version "
                    + sqlScript.getVersion() + " failed.", t);
            if (loggerOrNull != null)
            {
                loggerOrNull.logFailure(sqlScript, t);
            }
            if (t instanceof Error)
            {
                Error error = (Error) t;
                throw error;
            }
            throw CheckedExceptionTunnel.wrapIfNecessary((Exception) t);
        }
    }

    // @Private
    void execute(String script)
    {
        getJdbcTemplate().execute(script);
    }

    private String getTask(BadSqlGrammarException ex)
    {
        final String marker = "; bad SQL grammar [";
        return getTask(ex, marker);
    }

    private String getTask(UncategorizedSQLException ex)
    {
        final String marker = "; uncategorized SQLException for SQL [";
        return getTask(ex, marker);
    }

    private String getTask(RuntimeException ex, final String marker)
    {
        final String msg = ex.getMessage();
        final int endIdx = msg.indexOf(marker);
        if (endIdx > 0)
        {
            return msg.substring(0, endIdx);
        } else
        {
            return msg;
        }
    }

    private SQLException getCause(DataAccessException ex)
    {
        final Throwable cause = ex.getCause();
        if (cause instanceof SQLException)
        {
            return (SQLException) cause;
        } else
        {
            throw new Error("Cause of DataAccessException needs to be a SQLException.", cause);
        }
    }

}
