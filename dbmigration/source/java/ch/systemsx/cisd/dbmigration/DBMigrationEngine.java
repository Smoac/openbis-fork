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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Class for creating and migrating a database.  
 *
 * @author felmer
 */
public class DBMigrationEngine
{
    private static final String INSERT_DB_VERSION = "INSERT INTO DATABASE_VERSION VALUES (1, ?)";
    private static final String CREATE_DB_VERSION_TABLE = "CREATE TABLE DATABASE_VERSION (DB_VERSION SMALLINT NOT NULL,"
                                                          + "DB_INSTALLATION_DATE DATE)";

    private static final class DatabaseVersion
    {
        private final int version;
        private final Date installationDate;
        
        public DatabaseVersion(int version, Date installationDate)
        {
            this.version = version;
            this.installationDate = installationDate;
        }

        Date getInstallationDate()
        {
            return installationDate;
        }
        
        int getVersion()
        {
            return version;
        }
    }
    
    private final DataSource metaDataSource;
    private final DataSource dataSource;
    private final File scriptFolder;
    private final String initialDataScriptFile;
    private final String owner;
    private final String databaseName;

    public DBMigrationEngine(DataSource metaDataSource, DataSource dataSource, String scriptFolder, 
                             String initialDataScript, String owner, String databaseName)
    {
        this.metaDataSource = metaDataSource;
        this.dataSource = dataSource;
        this.initialDataScriptFile = initialDataScript;
        this.owner = owner;
        this.databaseName = databaseName;
        this.scriptFolder = new File(scriptFolder);
    }

    public void migrateTo(int version)
    {
        if (databaseExists())
        {
            migrateOrCreate(version);
        } else
        {
            JdbcTemplate template = new JdbcTemplate(metaDataSource);
            String createUserSQL = createScript("createUser.sql", owner, databaseName);
            String createDatabaseSQL = createScript("createDatabase.sql", owner, databaseName);
            
            try
            {
                template.execute(createUserSQL);
            } catch (BadSqlGrammarException ex)
            {
                // TODO: have better error checking here.
            }
            template.execute(createDatabaseSQL);
            
            migrateOrCreate(version);
            System.out.println("Database created");
        }
    }

    private String createScript(String scriptTemplateFile, String user, String database)
    {
        String script = FileUtilities.loadText(new File(scriptFolder, scriptTemplateFile));
        return script.replace("$USER", user).replace("$DATABASE", database);
    }

    private void migrateOrCreate(int version)
    {
        try
        {
            SimpleJdbcTemplate template = new SimpleJdbcTemplate(dataSource);
            List<DatabaseVersion> list 
                    = template.query("SELECT * FROM DATABASE_VERSION", new ParameterizedRowMapper<DatabaseVersion>()
                            {
                                public DatabaseVersion mapRow(ResultSet rs, int rowNum) throws SQLException
                                {
                                    int dbVersion = rs.getInt("DB_VERSION");
                                    java.sql.Date date = rs.getDate("DB_INSTALLATION_DATE");
                                    return new DatabaseVersion(dbVersion, date);
                                }
                            });
            int size = list.size();
            if (size == 0)
            {
                throw new EnvironmentFailureException("Incompletely initialized database.");
            } else if (size > 1)
            {
                throw new EnvironmentFailureException("To many versions found in DATABASE_VERSION: " + size);
            } else
            {
                DatabaseVersion databaseVersion = list.get(0);
                int dbVersion = databaseVersion.getVersion();
                if (version == dbVersion)
                {
                    return; // no migrate needed
                }
                if (version > dbVersion)
                {
                    System.out.println("migrate from " + dbVersion + " -> " + version);
                } else
                {
                    throw new EnvironmentFailureException("Couldn't revert from version " + dbVersion 
                                                          + " to previous version " + version + ".");
                }
            }
        } catch (BadSqlGrammarException ex)
        {
            String createScript = FileUtilities.loadText(new File(scriptFolder, "initial.sql"));
            String initialDataScript = null;
            if (initialDataScriptFile != null)
            {
                File file = new File(initialDataScriptFile);
                if (file.exists())
                {
                    initialDataScript = FileUtilities.loadText(file);
                }
            }
            JdbcTemplate template = new JdbcTemplate(dataSource);
            // TODO: Should be made transactionally save
            template.execute(CREATE_DB_VERSION_TABLE);
            template.update(INSERT_DB_VERSION, new Object[] {new Date()}); 
            template.execute(createScript);
            if (initialDataScript != null)
            {
                template.execute(initialDataScript);
            }
        }
    }
    
    private boolean databaseExists()
    {
        try
        {
            Connection connection = dataSource.getConnection();
            connection.close();
            return true;
        } catch (SQLException ex)
        {
            Throwable cause = ex.getCause();
            if (cause instanceof SQLException)
            {
                ex = (SQLException) cause;
            }
            if (isDBNotExistException(ex))
            {
                return false;
            }
            throw new EnvironmentFailureException("Couldn't connect database server.", ex);
        }
    }
    
    protected boolean isDBNotExistException(SQLException exception)
    {
        String message = exception.getMessage();
        return message.startsWith("FATAL: database") || message.startsWith("FATAL: password");
    }
}
