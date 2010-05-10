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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import static org.testng.AssertJUnit.assertNotNull;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import cz.startnet.utils.pgdiff.PgDiff;
import cz.startnet.utils.pgdiff.PgDiffArguments;

import ch.systemsx.cisd.common.db.ISqlScriptExecutor;
import ch.systemsx.cisd.common.db.SqlUnitTestRunner;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.IDAOFactory;
import ch.systemsx.cisd.dbmigration.IDataSourceFactory;
import ch.systemsx.cisd.dbmigration.postgresql.DumpPreparator;
import ch.systemsx.cisd.dbmigration.postgresql.PostgreSQLDAOFactory;

/**
 * Test cases for database migration.
 * 
 * @author Franz-Josef Elmer
 */
public final class SqlUnitMigrationTest
{
    private static final String ORIGINAL_SQL_SOURCE = "source/sql";

    private static final String BASE_DATA_FOLDER_TEMPLATE = "sourceTest/sql/%s/";

    private static final File unitTestRootDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    private String getDataFolder(final DatabaseConfigurationContext context)
    {
        return String.format(BASE_DATA_FOLDER_TEMPLATE, context.getDatabaseEngineCode())
                + DatabaseVersionHolder.getDatabaseVersion() + "/";
    }

    private String getTestDataFolder(final DatabaseConfigurationContext context)
    {
        return String.format(BASE_DATA_FOLDER_TEMPLATE, context.getDatabaseEngineCode())
                + "test_database_for_migration/";
    }

    private static final class MyDataSource implements DataSource, DisposableBean
    {
        private final String driver;

        private final String url;

        private final String owner;

        private final String password;

        private Connection connection;

        MyDataSource(final String driver, final String url, final String owner,
                final String password)
        {
            this.driver = driver;
            this.url = url;
            this.owner = owner;
            this.password = password;
        }

        public Connection getConnection() throws SQLException
        {
            if (connection != null && connection.isClosed() || connection == null)
            {
                try
                {
                    Class.forName(driver);
                } catch (final ClassNotFoundException ex)
                {
                    throw new SQLException("Couldn't load driver " + driver);
                }
                final Connection c = DriverManager.getConnection(url, owner, password);
                connection = c;
            }
            return connection;
        }

        public Connection getConnection(final String username, final String pw) throws SQLException
        {
            if (owner.equals(username) && password.equals(pw))
            {
                return getConnection();
            }
            throw new SQLException("Forbidden");
        }

        public int getLoginTimeout() throws SQLException
        {
            return 0;
        }

        public void setLoginTimeout(final int timeout) throws SQLException
        {
            throw new UnsupportedOperationException("setLoginTimeout");
        }

        public PrintWriter getLogWriter()
        {
            throw new UnsupportedOperationException("getLogWriter");
        }

        public void setLogWriter(final PrintWriter pw) throws SQLException
        {
            throw new UnsupportedOperationException("setLogWriter");
        }

        public void destroy() throws SQLException
        {
            if (connection != null)
            {
                connection.close();
                connection = null;
            }
        }

        @Override
        public String toString()
        {
            return "MyDataSource[" + driver + ", " + url + ", " + owner + "]";
        }

    }

    private static final IDataSourceFactory DATA_SOURCE_FACTORY = new IDataSourceFactory()
        {
            public DataSource createDataSource(String driver, String url, String owner,
                    String password)
            {
                return new MyDataSource(driver, url, owner, password);
            }

            public void setMaxActive(int maxActive)
            {
            }

            public void setMaxIdle(int maxIdle)
            {
            }
        };

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws Exception
    {
        LogInitializer.init();
    }

    private final static AbstractApplicationContext createBeanFactoryFrom(
            final String... contextFileNames)
    {
        System.setProperty("database.kind", "test_migration");
        System.setProperty("database.create-from-scratch", "false");
        System.setProperty("authorization-component-factory", "no-authorization");
        System.setProperty("script-folder", "source");
        System.setProperty("hibernate.search.index-mode", "NO_INDEX");
        final ClassPathXmlApplicationContext applicationContext =
                new ClassPathXmlApplicationContext(contextFileNames, true);
        return applicationContext;
    }

    private IDAOFactory createDAOFactory(final DatabaseConfigurationContext configurationContext)
    {
        return new PostgreSQLDAOFactory(configurationContext);
    }

    @Test(groups =
        { "slow" })
    /* runs unit tests written in SQL. The tests should check if the migration went fine. */
    public final void testMigration() throws Exception
    {
        final DatabaseConfigurationContext configurationContext =
                DatabaseCreationUtil.createDatabaseConfigurationContext("test_migration");
        final File migrationFolder =
                new File(getDataFolder(configurationContext) + SqlUnitTestRunner.MIGRATION_FOLDER);
        final File[] folders = migrationFolder.listFiles(new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return pathname.isDirectory() && pathname.getName().startsWith(".") == false;
                }
            });
        if (folders != null && folders.length != 0)
        {
            for (final File folder : folders)
            {
                final String databaseVersion = folder.getName();
                System.out.println("Test migration from database version " + databaseVersion
                        + " to " + DatabaseVersionHolder.getDatabaseVersion());
                try
                {
                    configurationContext.setCreateFromScratch(true);
                    configurationContext.initDataSourceFactory(DATA_SOURCE_FACTORY);
                    configurationContext.setScriptFolder(getTestDataFolder(configurationContext));
                    DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(
                            configurationContext, databaseVersion);
                    configurationContext.closeConnections();
                    configurationContext.setCreateFromScratch(false);
                    configurationContext.setScriptFolder(ORIGINAL_SQL_SOURCE);
                    DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(
                            configurationContext, DatabaseVersionHolder.getDatabaseVersion());
                    final ISqlScriptExecutor executor =
                            createDAOFactory(configurationContext).getSqlScriptExecutor();
                    new SqlUnitTestRunner(executor, new PrintWriter(System.out, true)).run(folder);
                } finally
                {
                    configurationContext.closeConnections();
                }
            }
        }
    }

    @Test(groups =
        { "slow" })
    /*
     * This test checks if the schema of a current database version is equal to the one which was
     * migrated from the version 18 to the current version with existing migration scripts. If it is
     * not a case, then the test will fail and will show what is missing in the last migration
     * script. Note that this test can be used to generate new migration scripts! When you introduce
     * a new database version, just create an empty migration script and run this test.
     */
    public final void testMigrationFrom034() throws Exception
    {
        final String databaseKind = "test_migration";
        final DatabaseConfigurationContext configurationContext =
                createDatabaseContext(databaseKind);
        AbstractApplicationContext applicationContext = null;
        try
        {
            final String initialVersion = "034";
            DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(configurationContext,
                    initialVersion);
            applicationContext = createBeanFactoryFrom("applicationContext.xml");
            final SessionFactory sessionFactory =
                    (SessionFactory) applicationContext.getBean("hibernate-session-factory");
            assertNotNull(sessionFactory);
            // dump the migrated database schema to the file
            final File migratedSchemaFile =
                    new File(unitTestRootDirectory, "migratedDatabaseSchema.sql");
            dumpSchema(configurationContext, migratedSchemaFile);

            final File originalSchemaFile = dumpOriginalSchema();

            final String schemasDelta = compareSchemas(migratedSchemaFile, originalSchemaFile);
            final String errorMsg =
                    "The migrated schema is not identical to the original one. "
                            + "Consider attaching following script to the migration file.";
            AssertJUnit.assertEquals(errorMsg, "", schemasDelta);
        } finally
        {
            configurationContext.closeConnections();
            if (applicationContext != null)
            {
                applicationContext.close();
            }
        }
    }

    // create an original database from scratch and dump its schema to the file
    private static File dumpOriginalSchema()
    {
        final DatabaseConfigurationContext originalDatabaseContext =
                createDatabaseContext("test_migration_original");
        originalDatabaseContext.setScriptFolder(ORIGINAL_SQL_SOURCE);
        DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(originalDatabaseContext,
                DatabaseVersionHolder.getDatabaseVersion());
        final File originalSchemaFile =
                new File(unitTestRootDirectory, "currentDatabaseSchema.sql");
        dumpSchema(originalDatabaseContext, originalSchemaFile);
        return originalSchemaFile;
    }

    private static String compareSchemas(final File currentSchemaFile, final File expectedSchemaFile)
    {
        final StringWriter writer = new StringWriter();
        final PgDiffArguments arguments = new PgDiffArguments();
        arguments.setOldDumpFile(currentSchemaFile.getAbsolutePath());
        arguments.setNewDumpFile(expectedSchemaFile.getAbsolutePath());
        arguments.setIgnoreFunctionWhitespace(true);
        arguments.setIgnoreStartWith(true);
        PgDiff.createDiff(new PrintWriter(writer), arguments);
        final String delta = writer.toString();
        return delta == null ? "" : delta;
    }

    private static DatabaseConfigurationContext createDatabaseContext(final String databaseKind)
    {
        final DatabaseConfigurationContext configurationContext =
                DatabaseCreationUtil.createDatabaseConfigurationContext(databaseKind);
        configurationContext.setCreateFromScratch(true);
        configurationContext.initDataSourceFactory(DATA_SOURCE_FACTORY);
        return configurationContext;
    }

    private static void dumpSchema(final DatabaseConfigurationContext configurationContext,
            final File migratedSchemaFile)
    {
        final boolean ok =
                DumpPreparator.createDatabaseSchemaDump(configurationContext.getDatabaseName(),
                        migratedSchemaFile);
        AssertJUnit.assertTrue("dump of db failed: " + configurationContext.getDatabaseName(), ok);
    }
}
