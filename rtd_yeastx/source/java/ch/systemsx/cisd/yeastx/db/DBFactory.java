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

package ch.systemsx.cisd.yeastx.db;

import java.sql.Connection;
import java.sql.SQLException;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;

/**
 * Factory for database connections.
 *
 * @author Bernd Rinn
 */
public class DBFactory
{
    /** Current version of the database. */
    public static final String DATABASE_VERSION = "001";
    
    static
    {
        QueryTool.getTypeMap().put(float[].class, new FloatArrayMapper());
    }

    private final DatabaseConfigurationContext context;

    public DBFactory(DatabaseConfigurationContext context)
    {
        this.context = context;
        DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(context, DATABASE_VERSION);
    }
    
    public Connection getConnection() throws SQLException
    {
        final Connection conn = context.getDataSource().getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    public static DatabaseConfigurationContext createDefaultDBContext()
    {
        final DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setDatabaseEngineCode("postgresql");
        context.setBasicDatabaseName("metabol");
        context.setDatabaseKind("dev");
        context.setReadOnlyGroup("metabol_readonly");
        context.setReadWriteGroup("metabol_readwrite");
        context.setScriptFolder("source/sql");
        return context;
    }

}
