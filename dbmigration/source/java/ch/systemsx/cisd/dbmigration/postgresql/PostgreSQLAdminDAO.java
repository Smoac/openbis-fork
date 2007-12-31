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

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.dbmigration.DBUtilities;
import ch.systemsx.cisd.dbmigration.IDatabaseAdminDAO;

/**
 * Implementation of {@link IDatabaseAdminDAO} for PostgreSQL.
 * 
 * @author Franz-Josef Elmer
 */
public class PostgreSQLAdminDAO extends SimpleJdbcDaoSupport implements IDatabaseAdminDAO
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, PostgreSQLAdminDAO.class);

    private final String owner;

    private final String database;

    /**
     * Creates an instance.
     * 
     * @param dataSource Data source able to create/drop the specified database.
     * @param owner Owner to be created if it doesn't exist.
     * @param database Name of the database.
     */
    public PostgreSQLAdminDAO(DataSource dataSource, String owner, String database)
    {
        this.owner = owner;
        this.database = database;
        setDataSource(dataSource);
    }

    public String getDatabaseName()
    {
        return database;
    }

    public void createOwner()
    {
        try
        {
            getJdbcTemplate().execute("create user " + owner);
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

    public void createDatabase()
    {
        operationLog.info("Try to create empty database '" + database + "' with owner '" + owner + "'.");
        try
        {
            getJdbcTemplate().execute("create database " + database + " with owner = " + owner
                    + " encoding = 'utf8' tablespace = pg_default;" 
                    + "alter database " + database + " set default_with_oids = off;");
        } catch (BadSqlGrammarException ex)
        {
            if (DBUtilities.isDuplicateDatabaseException(ex) == false)
            {
                throw ex;
            }
            operationLog.warn("Cannot create database '" + database + "' since it already exists.");
        }
    }

    public void dropDatabase()
    {
        try
        {
            getJdbcTemplate().execute("drop database " + database);
        } catch (DataAccessException ex)
        {
            if (DBUtilities.isDBNotExistException(ex) == false)
            {
                throw ex;
            }
        }
    }

}
