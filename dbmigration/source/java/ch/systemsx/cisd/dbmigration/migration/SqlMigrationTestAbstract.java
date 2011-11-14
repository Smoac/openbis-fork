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

package ch.systemsx.cisd.dbmigration.migration;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

import cz.startnet.utils.pgdiff.PgDiff;
import cz.startnet.utils.pgdiff.PgDiffArguments;

import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.dbmigration.DBMigrationEngine;
import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.dbmigration.postgresql.DumpPreparator;

/**
 * Test cases for database migration.
 * 
 * @author Piotr Kupczyk
 */
public abstract class SqlMigrationTestAbstract
{

    private File sqlScriptOutputDirectory;

    protected abstract String getSqlScriptInputDirectory();

    protected abstract String getSqlScriptOutputDirectory();

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws Exception
    {
        LogInitializer.init();
    }

    @BeforeTest(alwaysRun = true)
    public void beforeTest() throws Exception
    {
        File dir = new File(getSqlScriptOutputDirectory());
        if (!dir.exists())
        {
            dir.mkdir();
        }
    }

    @AfterTest(alwaysRun = true)
    public void afterTest() throws Exception
    {
        File dir = new File(getSqlScriptOutputDirectory());
        if (dir.exists())
        {
            dir.delete();
        }
    }

    public void test_migration(String firstVersion, String newestVersion) throws Exception
    {
        int firstVersionInt = Integer.valueOf(firstVersion);
        int newestVersionInt = Integer.valueOf(newestVersion);

        DatabaseConfigurationContext migrationContext = null;
        DatabaseConfigurationContext scratchContext = null;

        try
        {

            migrationContext = createMigrationDatabaseContext(true);
            scratchContext = createScratchDatabaseContext();

            // create first version of migration database
            DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(migrationContext,
                    firstVersion);
            
            migrationContext.setCreateFromScratch(false);

            for (int version = firstVersionInt + 1; version <= newestVersionInt; version++)
            {
                String versionStr = String.format("%03d", version);

                // migrate to the next version
                DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(migrationContext,
                        versionStr);
                dumpDatabaseSchema(migrationContext, getMigratedDatabaseSchemaFile());

                // create next version from scratch
                DBMigrationEngine.createOrMigrateDatabaseAndGetScriptProvider(scratchContext,
                        versionStr);
                dumpDatabaseSchema(scratchContext, getScratchDatabaseSchemaFile());

                // check whether migrated and scratch version are equal
                assertDatabaseSchemasEqual(getMigratedDatabaseSchemaFile(),
                        getScratchDatabaseSchemaFile());
            }

        } finally
        {
            if (migrationContext != null)
            {
                migrationContext.closeConnections();
            }
            if (scratchContext != null)
            {
                scratchContext.closeConnections();
            }
        }
    }

    private DatabaseConfigurationContext createDatabaseContext(String dbKind,
            boolean createFromScratch)
    {
        DatabaseConfigurationContext context = new DatabaseConfigurationContext();
        context.setDatabaseEngineCode("postgresql");
        context.setBasicDatabaseName("openbis");
        context.setDatabaseKind(dbKind);
        context.setScriptFolder(getSqlScriptInputDirectory());
        context.initDataSourceFactory(new SqlMigrationDataSourceFactory());
        context.setCreateFromScratch(createFromScratch);
        return context;
    }

    private DatabaseConfigurationContext createMigrationDatabaseContext(boolean createFromScratch)
    {
        return createDatabaseContext("test_migration_migrated", createFromScratch);
    }

    private DatabaseConfigurationContext createScratchDatabaseContext()
    {
        return createDatabaseContext("test_migration_scratch", true);
    }

    private File getMigratedDatabaseSchemaFile()
    {
        return new File(sqlScriptOutputDirectory, "migratedDatabaseSchema.sql");
    }

    private File getScratchDatabaseSchemaFile()
    {
        return new File(sqlScriptOutputDirectory, "scratchDatabaseSchema.sql");
    }

    private void dumpDatabaseSchema(final DatabaseConfigurationContext configurationContext,
            final File migratedSchemaFile)
    {
        final boolean dumpSuccessful =
                DumpPreparator.createDatabaseSchemaDump(configurationContext.getDatabaseName(),
                        migratedSchemaFile);
        AssertJUnit.assertTrue("dump of db failed: " + configurationContext.getDatabaseName(),
                dumpSuccessful);
    }

    private void assertDatabaseSchemasEqual(final File currentSchemaFile,
            final File expectedSchemaFile)
    {
        final StringWriter writer = new StringWriter();
        final PgDiffArguments arguments = new PgDiffArguments();
        arguments.setOldDumpFile(currentSchemaFile.getAbsolutePath());
        arguments.setNewDumpFile(expectedSchemaFile.getAbsolutePath());
        arguments.setIgnoreFunctionWhitespace(true);
        arguments.setIgnoreStartWith(true);
        PgDiff.createDiff(new PrintWriter(writer), arguments);

        String delta = writer.toString();
        delta = delta == null ? "" : delta;

        AssertJUnit.assertEquals("The migrated schema is not identical to the scratch one. "
                + "Consider attaching following script to the migration file.", "", delta);
    }

}
