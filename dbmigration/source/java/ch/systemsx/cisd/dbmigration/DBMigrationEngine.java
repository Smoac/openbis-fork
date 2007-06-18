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

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.db.SQLStateUtils;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Class for creating and migrating a database.
 * 
 * @author Franz-Josef Elmer
 */
public class DBMigrationEngine
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DBMigrationEngine.class);

    private final boolean shouldCreateFromScratch;

    private final IDAOFactory daoFactory;
    
    private final ISqlScriptProvider scriptProvider;


    public DBMigrationEngine(IDAOFactory daoFactory, ISqlScriptProvider scriptProvider, boolean shouldCreateFromScratch)
    {
        this.daoFactory = daoFactory;
        this.scriptProvider = scriptProvider;
        this.shouldCreateFromScratch = shouldCreateFromScratch;
    }
    
    public void migrateTo(String version)
    {
        if (shouldCreateFromScratch)
        {
            dropDatabase();
        }
        if (databaseExists() == false)
        {
            setupDatabase(version);
            return;
        }
        LogEntry entry = daoFactory.getDatabaseVersionLogDAO().getLastEntry();
        String databaseVersion = entry.getVersion();
        if (version.equals(databaseVersion))
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("No migration needed. Database version " + version + ".");
            }
            return;
        }
        if (version.compareTo(databaseVersion) > 0)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Migrating database from version '" + databaseVersion + "' to '" + version + "'.");
            }
            migrate(databaseVersion, version);
        } else
        {
            throw new EnvironmentFailureException("Couldn't revert from version " + databaseVersion
                    + " to previous version " + version + ".");
        }
    }

    private void dropDatabase()
    {
        daoFactory.getDatabaseDAO().dropDatabase();
    }
    
    private void setupDatabase(String version)
    {
        createOwner();
        createEmptyDatabase(version);
        fillWithInitialData(version);
        if (operationLog.isInfoEnabled())
        {
            String databaseName = daoFactory.getDatabaseDAO().getDatabaseName();
            operationLog.info("Database '" + databaseName + "' version " + version + " has been successfully created.");
        }
    }

    private void createOwner()
    {
        IDatabaseAdminDAO databaseDAO = daoFactory.getDatabaseDAO();
        try
        {
            databaseDAO.createOwner();
        } catch (DataAccessException ex)
        {
            if (userAlreadyExists(ex))
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Owner '" + databaseDAO.getOwner() + "' already exists.");
                }
            } else {
                operationLog.error("Database owner couldn't be created:", ex);
            }
        }
    }

    private void createEmptyDatabase(String version)
    {
        daoFactory.getDatabaseDAO().createDatabase();
        IDatabaseVersionLogDAO logDAO = daoFactory.getDatabaseVersionLogDAO();
        logDAO.createTable();
        
        Script script = scriptProvider.getSchemaScript(version);
        if (script == null)
        {
            throw new EnvironmentFailureException("No schema script found for version " + version);
        }
        executeScript(script, version);
    }

    private void fillWithInitialData(String version)
    {
        Script initialDataScript = scriptProvider.getDataScript(version);
        if (initialDataScript != null)
        {
            executeScript(initialDataScript, version);
        }
    }

    private void migrate(String fromVersion, String toVersion)
    {
        String version = fromVersion;
        do
        {
            String nextVersion = increment(version);
            Script migrationScript = scriptProvider.getMigrationScript(version, nextVersion);
            if (migrationScript == null)
            {
                String message = "Missing migration script from version " + version + " to " + nextVersion;
                operationLog.error(message);
                throw new EnvironmentFailureException(message);
            }
            executeScript(migrationScript, toVersion);
            version = nextVersion;
        } while (version.equals(toVersion) == false);
    }
    
    private String increment(String version)
    {
        char[] characters = new char[version.length()];
        version.getChars(0, characters.length, characters, 0);
        for (int i = characters.length - 1; i >= 0; i--)
        {
            char c = characters[i];
            if (c == '9')
            {
                characters[i] = '0';
            } else
            {
                characters[i] = (char) (c + 1);
                break;
            }
        }
        return new String(characters);
    }

    private void executeScript(Script script, String version) throws Error
    {
        IDatabaseVersionLogDAO logDAO = daoFactory.getDatabaseVersionLogDAO();
        String code = script.getCode();
        String name = script.getName();
        logDAO.logStart(version, name, code);
        try
        {
            daoFactory.getSqlScriptExecutor().execute(code);
            logDAO.logSuccess(version, name);
        } catch (Throwable t)
        {
            operationLog.error("Executing script '" + name + "' failed", t);
            logDAO.logFailure(version, name, t);
            if (t instanceof RuntimeException)
            {
                RuntimeException re = (RuntimeException) t;
                throw re;
            }
            if (t instanceof Error)
            {
                Error error = (Error) t;
                throw error;
            }
            throw new CheckedExceptionTunnel((Exception) t);
        }
    }


    /** Checks whether database already exists. */
    private final boolean databaseExists()
    {
        boolean result = daoFactory.getDatabaseVersionLogDAO().canConnectToDatabase();
        if (result && operationLog.isInfoEnabled())
        {
            operationLog.info("Database '" + daoFactory.getDatabaseDAO().getDatabaseName() + "' does not exist.");
        }
        return result;
    }
    
    /**
     * Checks whether given <code>DataAccessException</code> is caused by a "user already exists" exception.
     * <p>
     * This is database specific.
     * </p>
     */
    protected boolean userAlreadyExists(DataAccessException ex) {
        // 42710 DUPLICATE OBJECT
        return SQLStateUtils.isDuplicateObject(SQLStateUtils.getSqlState(ex));
    }
}
