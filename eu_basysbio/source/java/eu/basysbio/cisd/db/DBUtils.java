/*
 * Copyright 2010 ETH Zuerich, CISD
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

package eu.basysbio.cisd.db;

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
public class DBUtils
{
    /** Current version of the database. */
    public static final String DATABASE_VERSION = "006";

    public static DatabaseConfigurationContext contextBaSysBio;

    private static DatabaseConfigurationContext contextOpenBISCore;

    static
    {
        createBaSysBioDBContext();
        createOpenBISCoreContext();
        DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(contextBaSysBio,
                DATABASE_VERSION);
    }

    private DBUtils()
    {
        // Not to be called.
    }

    public static Connection getBaSysBioConnection() throws SQLException
    {
        final Connection conn = contextBaSysBio.getDataSource().getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    public static Connection getOpenBISCoreConnection() throws SQLException
    {
        final Connection conn = contextOpenBISCore.getDataSource().getConnection();
        conn.setAutoCommit(false);
        return conn;
    }

    public static IOpenBISDataSetQuery createDataSetQuery() throws SQLException
    {
        return QueryTool.getQuery(getOpenBISCoreConnection(), IOpenBISDataSetQuery.class);
    }

    public static IBaSysBioUpdater createBaSysBioUpdater() throws SQLException
    {
        return QueryTool.getQuery(getBaSysBioConnection(), IBaSysBioUpdater.class);
    }

    private static void createBaSysBioDBContext()
    {
        contextBaSysBio = new DatabaseConfigurationContext();
        contextBaSysBio.setDatabaseEngineCode("postgresql");
        contextBaSysBio.setBasicDatabaseName("basysbio");
        contextBaSysBio.setDatabaseKind("dev");
        contextBaSysBio.setScriptFolder("source/sql");
        contextBaSysBio.setCreateFromScratch(true);
    }

    private static void createOpenBISCoreContext()
    {
        contextOpenBISCore = new DatabaseConfigurationContext();
        contextOpenBISCore.setDatabaseEngineCode("postgresql");
        contextOpenBISCore.setBasicDatabaseName("openbis");
        contextOpenBISCore.setDatabaseKind("basysbio_dev");
        contextOpenBISCore.setScriptFolder("source/sql");
    }

}
