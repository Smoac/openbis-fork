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

package ch.systemsx.cisd.dbmigration.postgresql;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.Script;
import ch.systemsx.cisd.common.db.ISqlScriptExecutor;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.AbstractDatabaseAdminDAO;
import ch.systemsx.cisd.dbmigration.DBUtilities;
import ch.systemsx.cisd.dbmigration.DatabaseVersionLogDAO;
import ch.systemsx.cisd.dbmigration.IDatabaseAdminDAO;
import ch.systemsx.cisd.dbmigration.IMassUploader;
import ch.systemsx.cisd.dbmigration.MassUploadFileType;

/**
 * Implementation of {@link IDatabaseAdminDAO} for PostgreSQL.
 * 
 * @author Franz-Josef Elmer
 */
public class PostgreSQLAdminDAO extends AbstractDatabaseAdminDAO
{
    private static final String SQL_FILE_TYPE = ".sql";

    private static final String CREATE_DATABASE_SQL_TEMPLATE =
            "create database %1$s with owner = \"%2$s\" encoding = 'utf8' template = template0 tablespace = pg_default; "
                    + "alter database %1$s set default_with_oids = off;";

    private static final String CREATE_PLPGSQL = "create language plpgsql;";

    private static final String CREATE_TABLE_DATABASE_VERSION_LOGS_SQL =
            "create table "
                    + DatabaseVersionLogDAO.DB_VERSION_LOG
                    + " (db_version varchar(4) not null, "
                    + "module_name varchar(250), run_status varchar(10), run_status_timestamp timestamp, "
                    + "module_code bytea, run_exception bytea);";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PostgreSQLAdminDAO.class);

    /**
     * Creates an instance.
     * 
     * @param dataSource Data source able to create/drop the specified database.
     * @param scriptExecutor An executor for SQL scripts.
     * @param massUploader A class that can perform mass (batch) uploads into database tables.
     * @param owner Owner to be created if it doesn't exist.
     * @param readOnlyGroup Group that should be granted read-only access.
     * @param readWriteGroup Group that should be granted read-write access.
     * @param databaseName Name of the database.
     * @param databaseURL URL of the database.
     */
    public PostgreSQLAdminDAO(DataSource dataSource, ISqlScriptExecutor scriptExecutor,
            IMassUploader massUploader, String owner, String readOnlyGroup, String readWriteGroup,
            String databaseName, String databaseURL)
    {
        super(dataSource, scriptExecutor, massUploader, owner, readOnlyGroup, readWriteGroup,
                databaseName, databaseURL);
    }

    public void createOwner()
    {
        try
        {
            getJdbcTemplate().execute("create user \"" + owner + "\"");
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Created role '" + owner + "'.");
            }
        } catch (DataAccessException ex)
        {
            if (DBUtilities.isDuplicateObjectException(ex))
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Role '" + owner + "' already exists.");
                }
            } else
            {
                operationLog.error("Database role '" + owner + "' couldn't be created:", ex);
                throw ex;
            }
        }
    }

    public void createGroups()
    {
        if (StringUtils.isNotBlank(readOnlyGroupOrNull))
        {
            createRole(readOnlyGroupOrNull);
        }
        if (StringUtils.isNotBlank(readWriteGroupOrNull))
        {
            createRole(readWriteGroupOrNull);
        }
    }

    private void createRole(String role)
    {
        try
        {
            getJdbcTemplate().execute("create role \"" + role + "\"");
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Created role '" + role + "'.");
            }
        } catch (DataAccessException ex)
        {
            if (DBUtilities.isDuplicateObjectException(ex))
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Role '" + role + "' already exists.");
                }
            } else
            {
                operationLog.error("Database role '" + role
                        + "' couldn't be created:", ex);
                throw ex;
            }
        }
    }

    public void createDatabase()
    {
        createEmptyDatabase();
        createLanguagePlPgSQLIfNecessary();
        createDatabaseVersionLogsTable();
    }

    private void createDatabaseVersionLogsTable()
    {
        try
        {
            scriptExecutor.execute(new Script("create database_version_logs table",
                    CREATE_TABLE_DATABASE_VERSION_LOGS_SQL), true, null);
        } catch (RuntimeException ex)
        {
            operationLog.error("Failed to create database_version_logs table.", ex);
            throw ex;
        }
    }

    private void createEmptyDatabase()
    {
        operationLog.info("Try to create empty database '" + databaseName + "' with owner '"
                + owner + "'.");
        try
        {
            getJdbcTemplate().execute(
                    String.format(CREATE_DATABASE_SQL_TEMPLATE, databaseName, owner));
        } catch (RuntimeException ex)
        {
            if (ex instanceof DataAccessException
                    && DBUtilities.isDuplicateDatabaseException((DataAccessException) ex))
            {
                operationLog.warn("Cannot create database '" + databaseName
                        + "' since it already exists.");
            } else
            {
                operationLog.error("Failed to create database '" + databaseName + "'.", ex);
                throw ex;
            }
        }
    }

    private void createLanguagePlPgSQLIfNecessary()
    {
        operationLog.info("Try to create PL/PgSQL language.");
        try
        {
            scriptExecutor.execute(new Script("create language PL/PgSQL", CREATE_PLPGSQL), true,
                    null);
            getJdbcTemplate().execute(CREATE_PLPGSQL);
        } catch (RuntimeException ex)
        {
            if (ex instanceof DataAccessException
                    && DBUtilities.isDuplicateObjectException((DataAccessException) ex))
            {
                operationLog.warn("Cannot create language PL/PgSQL since it already exists.");
            } else
            {
                operationLog.error("Failed to create language PL/PgSQL.", ex);
                throw ex;
            }
        }
    }

    public void dropDatabase()
    {
        try
        {
            getJdbcTemplate().execute("drop database " + databaseName);
        } catch (DataAccessException ex)
        {
            if (DBUtilities.isDBNotExistException(ex) == false)
            {
                throw ex;
            }
        }
    }

    public void restoreDatabaseFromDump(File dumpFolder, String version)
    {
        createEmptyDatabase();
        createLanguagePlPgSQLIfNecessary();

        final Script schemaScript = tryLoadScript(dumpFolder, "schema", version);
        scriptExecutor.execute(schemaScript, false, null);
        final File[] massUploadFiles = getMassUploadFiles(dumpFolder);
        massUploader.performMassUpload(massUploadFiles);
        final Script finishScript = tryLoadScript(dumpFolder, "finish", version);
        scriptExecutor.execute(finishScript, false, null);
    }

    private Script tryLoadScript(final File dumpFolder, String prefix, String version)
            throws ConfigurationFailureException
    {
        final File scriptFile = new File(dumpFolder, prefix + "-" + version + SQL_FILE_TYPE);
        if (scriptFile.canRead() == false)
        {
            final String message = "No " + prefix + " script found for version " + version;
            operationLog.error(message);
            throw new ConfigurationFailureException(message);
        }
        final Script script =
                new Script(scriptFile.getPath(), FileUtilities.loadToString(scriptFile), version);
        return script;
    }

    /**
     * Returns the files determined for mass uploading.
     */
    private File[] getMassUploadFiles(File dumpFolder)
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Searching for mass upload files in directory '"
                    + dumpFolder.getAbsolutePath() + "'."); 
        }
        String[] csvFiles = dumpFolder.list(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return MassUploadFileType.CSV.isOfType(name)
                            || MassUploadFileType.TSV.isOfType(name);
                }
            });
        if (csvFiles == null)
        {
            operationLog.warn("Path '" + dumpFolder.getAbsolutePath() + "' is not a directory.");
            return new File[0];
        }
        Arrays.sort(csvFiles);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Found " + csvFiles.length + " files for mass uploading.");
        }
        final File[] csvPaths = new File[csvFiles.length];
        for (int i = 0; i < csvFiles.length; ++i)
        {
            csvPaths[i] = new File(dumpFolder, csvFiles[i]);
        }
        return csvPaths;
    }

}
