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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.Script;
import ch.systemsx.cisd.common.db.ISqlScriptExecutor;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.java.IMigrationStepExecutor;

/**
 * Class for creating and migrating a database.
 * 
 * @author Franz-Josef Elmer
 */
public final class DBMigrationEngine
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DBMigrationEngine.class);

    /**
     * Creates or migrates a database specified in the context for/to the specified version.
     * 
     * @return the SQL script provider.
     */
    public final static ISqlScriptProvider createOrMigrateDatabaseAndGetScriptProvider(
            final DatabaseConfigurationContext context, final String databaseVersion)
    {
        assert context != null : "Unspecified database configuration context.";
        assert StringUtils.isNotBlank(databaseVersion) : "Unspecified database version.";

        final ch.systemsx.cisd.dbmigration.IDAOFactory migrationDAOFactory =
                context.createDAOFactory();
        final String scriptFolder = context.getScriptFolder();
        if (scriptFolder == null)
        {
            operationLog.warn("Script folder is null");
        }
        final String databaseEngineCode = context.getDatabaseEngineCode();
        final ISqlScriptProvider sqlScriptProvider =
                new SqlScriptProvider(scriptFolder, databaseEngineCode);
        final DBMigrationEngine migrationEngine =
                new DBMigrationEngine(migrationDAOFactory, sqlScriptProvider, context
                        .isCreateFromScratch());
        migrationEngine.migrateTo(databaseVersion);
        return sqlScriptProvider;
    }

    private final boolean shouldCreateFromScratch;

    private final ISqlScriptProvider scriptProvider;

    private final IDatabaseAdminDAO adminDAO;

    private final IDatabaseVersionLogDAO logDAO;

    private final ISqlScriptExecutor scriptExecutor;

    private final IMigrationStepExecutor migrationStepExecutor;

    private final IMigrationStepExecutor migrationStepExecutorAdmin;

    /**
     * Creates an instance for the specified DAO factory and SQL script provider.
     * 
     * @param shouldCreateFromScratch If <code>true</code> the database should be dropped and
     *            created from scratch.
     */
    public DBMigrationEngine(final IDAOFactory daoFactory, final ISqlScriptProvider scriptProvider,
            final boolean shouldCreateFromScratch)
    {
        adminDAO = daoFactory.getDatabaseDAO();
        logDAO = daoFactory.getDatabaseVersionLogDAO();
        scriptExecutor = daoFactory.getSqlScriptExecutor();
        migrationStepExecutor = daoFactory.getMigrationStepExecutor();
        migrationStepExecutorAdmin = daoFactory.getMigrationStepExecutorAdmin();
        this.scriptProvider = scriptProvider;
        this.shouldCreateFromScratch = shouldCreateFromScratch;
    }

    /**
     * Create or migrate database to the specified version.
     * 
     * @throws ConfigurationFailureException If creation/migration fails due to a missing script
     * @throws EnvironmentFailureException If creation/migration fails due to an inconsistent
     *             database.
     */
    public final void migrateTo(final String version)
    {
        if (shouldCreateFromScratch)
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Dropping database.");
            }
            adminDAO.dropDatabase();
        }
        if (databaseExists() == false)
        {
            setupDatabase(version);
            return;
        }
        final LogEntry entry = getAndCheckLastLogEntry();
        final String databaseVersion = entry.getVersion();
        if (version.equals(databaseVersion))
        {
            if (operationLog.isDebugEnabled())
            {
                final String databaseName = adminDAO.getDatabaseName();
                if (operationLog.isInfoEnabled())
                {
                    operationLog.debug("No migration needed for database '" + databaseName
                            + "'. It has the right version (" + version + ").");
                }
            }
        } else if (version.compareTo(databaseVersion) > 0)
        {
            adminDAO.createGroups();
            if (operationLog.isInfoEnabled())
            {
                final String databaseName = adminDAO.getDatabaseName();
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Trying to migrate database '" + databaseName
                            + "' from version " + databaseVersion + " to " + version + ".");
                }
            }
            migrate(databaseVersion, version);
            if (operationLog.isInfoEnabled())
            {
                final String databaseName = adminDAO.getDatabaseName();
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Database '" + databaseName
                            + "' successfully migrated from version " + databaseVersion + " to "
                            + version + ".");
                }
            }
        } else
        {
            final String databaseName = adminDAO.getDatabaseName();
            final String message =
                    "Cannot revert database '" + databaseName + "' from version " + databaseVersion
                            + " to earlier version " + version + ".";
            operationLog.error(message);
            throw new EnvironmentFailureException(message);
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Using database '%s'", adminDAO.getDatabaseURL()));
        }
    }

    private final LogEntry getAndCheckLastLogEntry()
    {
        final LogEntry entry = logDAO.getLastEntry();
        if (entry == null)
        {
            final String message = "Inconsistent database: Empty database version log.";
            operationLog.error(message);
            throw new EnvironmentFailureException(message);
        }
        if (entry.getRunStatus() != LogEntry.RunStatus.SUCCESS)
        {
            final String message =
                    "Inconsistent database: Last creation/migration didn't succeed. Last log entry: "
                            + entry;
            operationLog.error(message);
            throw new EnvironmentFailureException(message);
        }
        return entry;
    }

    private final void setupDatabase(final String version)
    {
        adminDAO.createOwner();
        adminDAO.createGroups();
        if (scriptProvider.isDumpRestore(version))
        {
            adminDAO.restoreDatabaseFromDump(scriptProvider.getDumpFolder(version), version);
        } else
        {
            createEmptyDatabase(version);
            fillWithInitialData(version);
        }
        if (operationLog.isInfoEnabled())
        {
            final String databaseName = adminDAO.getDatabaseName();
            operationLog.info("Database '" + databaseName + "' version " + version
                    + " has been successfully created.");
        }
    }

    private final void createEmptyDatabase(final String version)
    {
        adminDAO.createDatabase();
        executeSchemaScript(version);
    }

    private final void executeSchemaScript(final String version)
    {
        final Script domainsScript = scriptProvider.tryGetDomainsScript(version);
        if (domainsScript == null)
        {
            operationLog.debug("No domains script found for version " + version);
        } else
        {
            scriptExecutor.execute(domainsScript, true, logDAO);
        }
        final Script schemaScript = scriptProvider.tryGetSchemaScript(version);
        if (schemaScript == null)
        {
            final String message = "No schema script found for version " + version;
            operationLog.error(message);
            throw new ConfigurationFailureException(message);
        }
        scriptExecutor.execute(schemaScript, true, logDAO);
        final Script functionScript = scriptProvider.tryGetFunctionScript(version);
        if (functionScript == null)
        {
            operationLog.debug("No function script found for version " + version);
        } else
        {
            scriptExecutor.execute(functionScript, false, logDAO);
        }
        final Script grantsScript = scriptProvider.tryGetGrantsScript(version);
        if (grantsScript == null)
        {
            operationLog.debug("No grants script found for version " + version);
        } else
        {
            scriptExecutor.execute(grantsScript, true, logDAO);
        }
    }

    private final void fillWithInitialData(final String version)
    {
        final Script initialDataScript = scriptProvider.tryGetDataScript(version);
        if (initialDataScript != null)
        {
            scriptExecutor.execute(initialDataScript, true, logDAO);
        }
    }

    private final void migrate(final String fromVersion, final String toVersion)
    {
        String version = fromVersion;
        do
        {
            final String nextVersion = increment(version);
            final Script functionMigrationScriptOrNull =
                    scriptProvider.tryGetFunctionMigrationScript(version, nextVersion);
            final Script migrationScript =
                    scriptProvider.tryGetMigrationScript(version, nextVersion);
            if (migrationScript == null)
            {
                final String databaseName = adminDAO.getDatabaseName();
                final String message =
                        "Cannot migrate database '" + databaseName + "' from version " + version
                                + " to " + nextVersion + " because of missing migration script.";
                operationLog.error(message);
                throw new EnvironmentFailureException(message);
            }
            final long time = System.currentTimeMillis();
            migrationStepExecutorAdmin.init(migrationScript);
            migrationStepExecutor.init(migrationScript);
            migrationStepExecutorAdmin.performPreMigration();
            migrationStepExecutor.performPreMigration();
            scriptExecutor.execute(migrationScript, true, logDAO);
            if (functionMigrationScriptOrNull != null)
            {
                scriptExecutor.execute(functionMigrationScriptOrNull, false, logDAO);
            }
            migrationStepExecutor.performPostMigration();
            migrationStepExecutorAdmin.performPostMigration();
            migrationStepExecutor.finish();
            migrationStepExecutorAdmin.finish();
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Successfully migrated from version " + version + " to "
                        + nextVersion + " in " + (System.currentTimeMillis() - time) + " msec");
            }
            version = nextVersion;
        } while (version.equals(toVersion) == false);
    }

    @Private
    final static String increment(final String version)
    {
        final char[] characters = new char[version.length()];
        version.getChars(0, characters.length, characters, 0);
        for (int i = characters.length - 1; i >= 0; i--)
        {
            final char c = characters[i];
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

    /** Checks whether database already exists. */
    private final boolean databaseExists()
    {
        final boolean result = logDAO.canConnectToDatabase();
        if (result == false && operationLog.isInfoEnabled())
        {
            operationLog.info("Database '" + adminDAO.getDatabaseName() + "' does not exist.");
        }
        return result;
    }

}
