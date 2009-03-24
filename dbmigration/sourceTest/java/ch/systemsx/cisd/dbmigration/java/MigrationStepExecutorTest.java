/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.dbmigration.java;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.fail;

import javax.sql.DataSource;

import org.apache.log4j.Level;
import org.jmock.Mockery;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.Script;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * Test cases for the {@link MigrationStepExecutor}.
 * 
 * @author Izabela Adamczyk
 */
public final class MigrationStepExecutorTest
{
    private Mockery context;

    private DataSource dataSource;

    private BufferedAppender logRecorder;

    @BeforeMethod
    public final void setUp()
    {
        context = new Mockery();
        dataSource = context.mock(DataSource.class);
        logRecorder = new BufferedAppender("%m%n", Level.DEBUG);
    }

    @BeforeClass(alwaysRun = true)
    public final void beforeClass() throws Exception
    {
        LogInitializer.init();
    }

    @Test
    public final void testHappyCase()
    {
        final MigrationStepExecutor migrationStepExecutor = new MigrationStepExecutor(dataSource);
        Script script =
                new Script("001To002.sql",
                        "-- JAVA ch.systemsx.cisd.dbmigration.java.MigrationStepFrom001To002");
        migrationStepExecutor.init(script);
        migrationStepExecutor.performPreMigration();
        migrationStepExecutor.performPostMigration();
        migrationStepExecutor.finish();
        assertEquals("Migration step class 'MigrationStepFrom001To002' found for "
                + "migration script '001To002.sql'.", logRecorder.getLogContent());
        logRecorder.resetLogContent();
        script =
                new Script("001To002.sql", "\n\n  \n"
                        + "--JAVA ch.systemsx.cisd.dbmigration.java.MigrationStepFrom001To002");
        migrationStepExecutor.init(script);
        assertEquals("Migration step class 'MigrationStepFrom001To002' found for "
                + "migration script '001To002.sql'.", logRecorder.getLogContent());
    }

    @Test
    public final void testFinish()
    {
        final MigrationStepExecutor migrationStepExecutor = new MigrationStepExecutor(dataSource);
        final Script script =
                new Script("001To002.sql",
                        "--   JAVA ch.systemsx.cisd.dbmigration.java.MigrationStepFrom001To002");
        migrationStepExecutor.init(script);
        migrationStepExecutor.performPreMigration();
        migrationStepExecutor.performPostMigration();
        migrationStepExecutor.finish();
        boolean fail = true;
        try
        {
            migrationStepExecutor.performPreMigration();
        } catch (final AssertionError ex)
        {
            fail = false;
        }
        assertFalse(fail);
    }

    @Test
    public final void testUnhappyCase()
    {
        final MigrationStepExecutor migrationStepExecutor = new MigrationStepExecutor(dataSource);
        Script script =
                new Script("002To003.sql", "\n-- This is a comment\n"
                        + "-- JAVA ch.systemsx.cisd.dbmigration.java.MigrationStepFrom002To003");
        migrationStepExecutor.init(script);
        assertEquals("No migration step class found for migration script '002To003.sql'.",
                logRecorder.getLogContent());
        logRecorder.resetLogContent();
        script =
                new Script("002To003.sql",
                        "-- JAVA ch.systemsx.cisd.dbmigration.java.MigrationStepFrom002To003");
        migrationStepExecutor.init(script);
        assertEquals("Migration step class 'MigrationStepFrom002To003' found for "
                + "migration script '002To003.sql'.", logRecorder.getLogContent());
        try
        {
            migrationStepExecutor.performPreMigration();
            fail();
        } catch (final DataIntegrityViolationException ex)
        {
            // Nothing to do here.
        }
        try
        {
            migrationStepExecutor.performPostMigration();
            fail();
        } catch (final EmptyResultDataAccessException ex)
        {
            // Nothing to do here.
        }
    }

    @Test
    public final void testClassNotFound()
    {
        final MigrationStepExecutor migrationStepExecutor = new MigrationStepExecutor(dataSource);
        final Script script =
                new Script("003To004.sql",
                        "-- JAVA ch.systemsx.cisd.dbmigration.java.MigrationStepFrom003To003");
        try
        {
            migrationStepExecutor.init(script);
            fail();
        } catch (final CheckedExceptionTunnel e)
        {
            // Nothing to do here.
        }
    }
}
