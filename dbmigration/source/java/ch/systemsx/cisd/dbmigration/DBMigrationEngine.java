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

import java.io.File;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.db.ISqlScriptExecutor;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.OSUtilities;

/**
 * Class for creating and migrating a database.
 * 
 * @author Franz-Josef Elmer
 */
public class DBMigrationEngine
{
    //@Private
    static final String CREATE_LOG_SQL = "createLog.sql";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, DBMigrationEngine.class);

    private final boolean shouldCreateFromScratch;

    private final ISqlScriptProvider scriptProvider;

    private final IDatabaseAdminDAO adminDAO;

    private final IDatabaseVersionLogDAO logDAO;
    
    private final IMassUploader massUploader;

    private final ISqlScriptExecutor scriptExecutor;

    /**
     * Creates an instance for the specified DAO factory and SQL script provider.
     *
     * @param shouldCreateFromScratch If <code>true</code> the database should be dropped and created from scratch.
     */
    public DBMigrationEngine(IDAOFactory daoFactory, ISqlScriptProvider scriptProvider, boolean shouldCreateFromScratch)
    {
        adminDAO = daoFactory.getDatabaseDAO();
        logDAO = daoFactory.getDatabaseVersionLogDAO();
        massUploader = daoFactory.getMassUploader();
        scriptExecutor = daoFactory.getSqlScriptExecutor();
        this.scriptProvider = scriptProvider;
        this.shouldCreateFromScratch = shouldCreateFromScratch;
    }
    
    /**
     * Create or migrate database to the specified version.
     * 
     * @throws EnvironmentFailureException if creation/migration failed because of some missing scripts or an
     *          inconsistent database.
     */
    public void migrateTo(String version)
    {
        if (shouldCreateFromScratch)
        {
            adminDAO.dropDatabase();
        }
        if (databaseExists() == false)
        {
            setupDatabase(version);
            return;
        }
        LogEntry entry = getAndCheckLastLogEntry();
        String databaseVersion = entry.getVersion();
        if (version.equals(databaseVersion))
        {
            if (operationLog.isDebugEnabled())
            {
                String databaseName = adminDAO.getDatabaseName();
                operationLog.debug("No migration needed for database '" + databaseName + "'. It has the right version (" 
                                  + version + ").");
            }
        } else if (version.compareTo(databaseVersion) > 0)
        {
            if (operationLog.isInfoEnabled())
            {
                String databaseName = adminDAO.getDatabaseName();
                operationLog.info("Trying to migrate database '" + databaseName + "' from version " + databaseVersion 
                                   + " to " + version + ".");
            }
            migrate(databaseVersion, version);
            if (operationLog.isInfoEnabled())
            {
                String databaseName = adminDAO.getDatabaseName();
                operationLog.info("Database '" + databaseName + "' successfully migrated from version " 
                                  + databaseVersion + " to " + version + ".");
            }
        } else
        {
            String databaseName = adminDAO.getDatabaseName();
            String message = "Cannot revert database '" + databaseName + "' from version " 
                             + databaseVersion + " to earlier version " + version + ".";
            operationLog.error(message);
            throw new EnvironmentFailureException(message);
        }
    }

    private LogEntry getAndCheckLastLogEntry()
    {
        LogEntry entry = logDAO.getLastEntry();
        if (entry == null)
        {
            String message = "Inconsistent database: Empty database version log.";
            operationLog.error(message);
            throw new EnvironmentFailureException(message);
        }
        if (entry.getRunStatus() != LogEntry.RunStatus.SUCCESS)
        {
            String message = "Inconsistent database: Last creation/migration didn't succeed. Last log entry: " + entry;
            operationLog.error(message);
            throw new EnvironmentFailureException(message);
        }
        return entry;
    }

    private void setupDatabase(String version)
    {
        adminDAO.createOwner();
        Script finishScript = scriptProvider.getFinishScript(version);
        if (finishScript == null)
        {
            createEmptyDatabase(version);
            fillWithInitialData(version, true);
        } else
        {
            createDatabaseFromDump(version, finishScript);
        }
        if (operationLog.isInfoEnabled())
        {
            String databaseName = adminDAO.getDatabaseName();
            operationLog.info("Database '" + databaseName + "' version " + version + " has been successfully created.");
        }
    }
    
    private void createDatabaseFromDump(String version, Script finishScript)
    {
        adminDAO.createDatabase();
        executeSchemaScript(version, false);
        fillWithInitialData(version, false);
        executeScript(finishScript, version, false);
    }

    private void createEmptyDatabase(String version)
    {
        adminDAO.createDatabase();
        Script createScript = scriptProvider.getScript(CREATE_LOG_SQL);
        if (createScript == null)
        {
            String message = "Missing script " + CREATE_LOG_SQL;
            operationLog.error(message);
            throw new EnvironmentFailureException(message);
        }
        try
        {
            logDAO.createTable(createScript);
        } catch (RuntimeException e)
        {
            operationLog.error("Script '" + createScript.getName() + "' failed:" + OSUtilities.LINE_SEPARATOR 
                               + createScript.getCode(), e);
            throw e;
        }
        
        executeSchemaScript(version, true);
    }

    private void executeSchemaScript(String version, boolean logEnabled)
    {
        Script script = scriptProvider.getSchemaScript(version);
        if (script == null)
        {
            String message = "No schema script found for version " + version;
            operationLog.error(message);
            throw new EnvironmentFailureException(message);
        }
        executeScript(script, version, logEnabled);
    }

    private void fillWithInitialData(String version, boolean logEnabled)
    {
        Script initialDataScript = scriptProvider.getDataScript(version);
        if (initialDataScript != null)
        {
            executeScript(initialDataScript, version, logEnabled);
        }
        File[] massUploadFiles = scriptProvider.getMassUploadFiles(version);
        for (File f : massUploadFiles)
        {
            massUploader.performMassUpload(f);
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
                final String databaseName = adminDAO.getDatabaseName();
                final String message = "Cannot migrate database '" + databaseName + "' from version " + version + " to " 
                                 + nextVersion + " because of missing migration script.";
                operationLog.error(message);
                throw new EnvironmentFailureException(message);
            }
            executeScript(migrationScript, toVersion);
            version = nextVersion;
        } while (version.equals(toVersion) == false);
    }
    
    // @Private
    static String increment(String version)
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

    private void executeScript(Script script, String version)
    {
        executeScript(script, version, true);
    }
    
    private void executeScript(Script script, String version, boolean logEnabled)
    {
        final String name = script.getName();
        final String code = script.getCode();
        if (logEnabled)
        {
            logDAO.logStart(version, name, code);
        }
        try
        {
            scriptExecutor.execute(code);
            if (logEnabled)
            {
                logDAO.logSuccess(version, name);
            }
        } catch (Throwable t)
        {
            operationLog.error("Executing script '" + name + "' failed.", t);
            if (logEnabled)
            {
                logDAO.logFailure(version, name, t);
            }
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
        boolean result = logDAO.canConnectToDatabase();
        if (result == false && operationLog.isInfoEnabled())
        {
            operationLog.info("Database '" + adminDAO.getDatabaseName() + "' does not exist.");
        }
        return result;
    }
    
}
