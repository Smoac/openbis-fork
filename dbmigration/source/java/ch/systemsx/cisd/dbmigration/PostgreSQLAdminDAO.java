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

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcDaoSupport;

/**
 * Implementation of {@link IDatabaseAdminDAO} for PostgreSQL.
 *
 * @author Franz-Josef Elmer
 */
public class PostgreSQLAdminDAO extends SimpleJdbcDaoSupport implements IDatabaseAdminDAO
{
    private final String owner;
    private final String database;

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

    public String getOwner()
    {
        return owner;
    }

    public void createOwner()
    {
        getJdbcTemplate().execute("create user " + owner);
    }

    public void createDatabase()
    {
        JdbcTemplate template = getJdbcTemplate();
        template.execute("create database " + database + " with owner = " + owner 
                         + " encoding = 'utf8' tablespace = pg_default;"
                         + "alter database " + database + " set default_with_oids = off;");
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
