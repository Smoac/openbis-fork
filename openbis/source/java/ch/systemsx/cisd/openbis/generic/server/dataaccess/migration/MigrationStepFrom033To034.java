/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.migration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.java.IMigrationStep;

/**
 * @author Bernd Rinn
 */
public class MigrationStepFrom033To034 implements IMigrationStep
{

    private final DatabaseConfigurationContext dbConfigurationContext;

    public MigrationStepFrom033To034(DatabaseConfigurationContext dbConfigurationContext)
    {
        assert dbConfigurationContext != null;

        this.dbConfigurationContext = dbConfigurationContext;
    }

    public void performPostMigration(SimpleJdbcTemplate simpleJdbcTemplate)
            throws DataAccessException
    {
    }

    public void performPreMigration(SimpleJdbcTemplate simpleJdbcTemplate)
            throws DataAccessException
    {
        final String newDatabaseName = dbConfigurationContext.getDatabaseName();
        final String oldDatabaseName = "lims_" + newDatabaseName.substring("openbis_".length());
        if (databaseExists(simpleJdbcTemplate, oldDatabaseName) == false)
        {
            return;
        }
        simpleJdbcTemplate.getJdbcOperations().execute(
                "alter database " + oldDatabaseName + " rename to " + newDatabaseName);
    }

    private boolean databaseExists(SimpleJdbcTemplate simpleJdbcTemplate, String dbName)
    {
        final List<String> databases =
            simpleJdbcTemplate.query("select datname from pg_database",
                    new ParameterizedRowMapper<String>()
                        {
                            public String mapRow(ResultSet rs, int rowNum) throws SQLException
                            {
                                return rs.getString("datname");
                            }
                        });
    for (String database : databases)
    {
        if (dbName.equalsIgnoreCase(database))
        {
            return true;
        }
    }
    return false;
        
    }
}
