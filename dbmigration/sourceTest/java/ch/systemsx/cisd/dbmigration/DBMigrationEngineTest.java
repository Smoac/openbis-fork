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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.jdbc.BadSqlGrammarException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.Script;
import ch.systemsx.cisd.common.db.ISqlScriptExecutor;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.utilities.OSUtilities;

/**
 * Tests of {@link DBMigrationEngine} using mocks for database and {@link SqlScriptProvider}.
 * 
 * @author Franz-Josef Elmer
 */
public class DBMigrationEngineTest
{
    private class MyExpectations extends Expectations
    {
        protected void expectSuccessfulExecution(Script script, boolean honorSingleStepMode)
        {
            will(returnValue(script));
            one(scriptExecutor).execute(script, honorSingleStepMode, logDAO);
        }
    }

    private Mockery context;

    private ISqlScriptProvider scriptProvider;

    private IDAOFactory daoFactory;

    private IDatabaseAdminDAO adminDAO;

    private IDatabaseVersionLogDAO logDAO;

    private ISqlScriptExecutor scriptExecutor;

    private BufferedAppender logRecorder;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        scriptProvider = context.mock(ISqlScriptProvider.class);
        daoFactory = context.mock(IDAOFactory.class);
        adminDAO = context.mock(IDatabaseAdminDAO.class);
        logDAO = context.mock(IDatabaseVersionLogDAO.class);
        scriptExecutor = context.mock(ISqlScriptExecutor.class);
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testIncrement()
    {
        assertEquals("001", DBMigrationEngine.increment("000"));
        assertEquals("009", DBMigrationEngine.increment("008"));
        assertEquals("010", DBMigrationEngine.increment("009"));
        assertEquals("100", DBMigrationEngine.increment("099"));
    }

    @Test
    public void testCreateFromScratch()
    {
        final String version = "042";
        context.checking(new MyExpectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));

                    one(adminDAO).dropDatabase();
                    one(logDAO).canConnectToDatabase();
                    will(returnValue(false));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(adminDAO).createOwner();
                    one(scriptProvider).isDumpRestore(version);
                    will(returnValue(false));
                    one(adminDAO).createDatabase();

                    one(scriptProvider).tryGetSchemaScript(version);
                    expectSuccessfulExecution(new Script("schema", "schema code", version), true);
                    one(scriptProvider).tryGetFunctionScript(version);
                    expectSuccessfulExecution(new Script("function", "db function code", version), false);
                    one(scriptProvider).tryGetDataScript(version);
                    expectSuccessfulExecution(new Script("data", "data code", version), true);
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 2. database"));
                }
            });
        final DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, true);
        migrationEngine.migrateTo(version);
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - Dropping database."
                + OSUtilities.LINE_SEPARATOR + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 1. database' does not exist." + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 2. database' version 042 has been successfully created.", logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testDumpRestore()
    {
        final String version = "042";
        context.checking(new MyExpectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));

                    one(adminDAO).dropDatabase();
                    one(logDAO).canConnectToDatabase();
                    will(returnValue(false));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(adminDAO).createOwner();
                    one(scriptProvider).isDumpRestore(version);
                    will(returnValue(true));

                    one(scriptProvider).getDumpFolder(version);
                    final File dumpFolder = new File("The Dump Folder");
                    will(returnValue(dumpFolder));
                    one(adminDAO).restoreDatabaseFromDump(dumpFolder, version);
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 2. database"));
                }
            });
        final DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, true);
        migrationEngine.migrateTo(version);
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - Dropping database."
                + OSUtilities.LINE_SEPARATOR + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 1. database' does not exist." + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 2. database' version 042 has been successfully created.", logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testCreateFromScratchButCouldNotCreateOwner()
    {
        final String version = "042";
        final BadSqlGrammarException exception = new BadSqlGrammarException("", "", new SQLException("owner"));
        context.checking(new MyExpectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));

                    one(adminDAO).dropDatabase();
                    one(logDAO).canConnectToDatabase();
                    will(returnValue(false));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(adminDAO).createOwner();
                    will(throwException(exception));
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, true);
        try
        {
            migrationEngine.migrateTo(version);
            fail("BadSqlGrammarException expected because owner couldn't be created");
        } catch (BadSqlGrammarException e)
        {
            assertSame(exception, e);
        }
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - Dropping database."
                + OSUtilities.LINE_SEPARATOR + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 1. database' does not exist.", logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testCreateFromScratchWithFromScratchFlagFalse()
    {
        final String version = "042";
        context.checking(new MyExpectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(false));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(adminDAO).createOwner();
                    one(scriptProvider).isDumpRestore(version);
                    will(returnValue(false));
                    one(adminDAO).createDatabase();
                    one(scriptProvider).tryGetSchemaScript(version);
                    expectSuccessfulExecution(new Script("schema", "schema code"), true);
                    one(scriptProvider).tryGetFunctionScript(version);
                    expectSuccessfulExecution(new Script("function", "db function code"), false);
                    one(scriptProvider).tryGetDataScript(version);
                    expectSuccessfulExecution(new Script("data", "data code"), true);
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 2. database"));
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);
        migrationEngine.migrateTo(version);
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 1. database' does not exist." + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 2. database' version 042 has been successfully created.", logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testCreateFromScratchWithMissingDataScript()
    {
        final String version = "042";
        context.checking(new MyExpectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(false));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(adminDAO).createOwner();
                    one(scriptProvider).isDumpRestore(version);
                    will(returnValue(false));
                    one(adminDAO).createDatabase();

                    one(scriptProvider).tryGetSchemaScript(version);
                    expectSuccessfulExecution(new Script("schema", "schema code", version), true);
                    one(scriptProvider).tryGetFunctionScript(version);
                    expectSuccessfulExecution(new Script("function", "db function code", version), false);
                    one(scriptProvider).tryGetDataScript(version);
                    will(returnValue(null));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 2. database"));
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);
        migrationEngine.migrateTo(version);
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 1. database' does not exist." + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 2. database' version 042 has been successfully created.", logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testCreateFromScratchWithMissingSchemaScript()
    {
        final String version = "042";
        context.checking(new MyExpectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(false));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(adminDAO).createOwner();
                    one(scriptProvider).isDumpRestore(version);
                    will(returnValue(false));
                    one(adminDAO).createDatabase();
                    one(scriptProvider).tryGetSchemaScript(version);
                    will(returnValue(null));
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);
        String message = "No schema script found for version 042";
        try
        {
            migrationEngine.migrateTo(version);
            fail("EnvironmentFailureException expected because of missing schema script");
        } catch (ConfigurationFailureException e)
        {
            assertEquals(message, e.getMessage());
        }
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 1. database' does not exist." + OSUtilities.LINE_SEPARATOR
                + "ERROR OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - " + message, logRecorder
                .getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testCaseNoMigrationNeeded()
    {
        final String version = "042";
        context.checking(new Expectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(true));
                    one(logDAO).getLastEntry();
                    LogEntry logEntry = new LogEntry();
                    logEntry.setRunStatus(LogEntry.RunStatus.SUCCESS);
                    logEntry.setVersion(version);
                    will(returnValue(logEntry));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my database"));
                    one(adminDAO).getDatabaseURL();
                    will(returnValue("my database URL"));
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);

        migrationEngine.migrateTo(version);
        assertEquals("DEBUG OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "No migration needed for database 'my database'. It has the right version (042)."
                + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - Using database 'my database URL'",
                logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testMigration()
    {
        final String fromVersion = "099";
        final String toVersion = "101";
        context.checking(new MyExpectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(true));
                    one(logDAO).getLastEntry();
                    LogEntry logEntry = new LogEntry();
                    logEntry.setRunStatus(LogEntry.RunStatus.SUCCESS);
                    logEntry.setVersion(fromVersion);
                    will(returnValue(logEntry));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(adminDAO).getDatabaseURL();
                    will(returnValue("my database URL"));
                    one(scriptProvider).tryGetMigrationScript(fromVersion, "100");
                    expectSuccessfulExecution(new Script("m-099-100", "code 099 100", toVersion), true);
                    one(scriptProvider).tryGetMigrationScript("100", toVersion);
                    expectSuccessfulExecution(new Script("m-100-101", "code 100 101", toVersion), true);
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 2. database"));
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);

        migrationEngine.migrateTo(toVersion);
        String logContent = logRecorder.getLogContent();
        logContent = logContent.replaceAll("\\d+ msec", "0 msec");
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Trying to migrate database 'my 1. database' from version 099 to 101." + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Successfully migrated from version 099 to 100 in 0 msec" + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Successfully migrated from version 100 to 101 in 0 msec" + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 2. database' successfully migrated from version 099 to 101."
                + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - Using database 'my database URL'",
                logContent);

        context.assertIsSatisfied();
    }

    @Test
    public void testMigrationScriptNotFound()
    {
        final String fromVersion = "099";
        final String toVersion = "101";
        context.checking(new MyExpectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(true));
                    one(logDAO).getLastEntry();
                    LogEntry logEntry = new LogEntry();
                    logEntry.setRunStatus(LogEntry.RunStatus.SUCCESS);
                    logEntry.setVersion(fromVersion);
                    will(returnValue(logEntry));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(scriptProvider).tryGetMigrationScript(fromVersion, "100");
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 2. database"));
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);

        String errorMessage =
                "Cannot migrate database 'my 2. database' from version 099 to 100 because of "
                        + "missing migration script.";
        try
        {
            migrationEngine.migrateTo(toVersion);
            fail("EnvironmentFailureException expected because of missing migration step");
        } catch (EnvironmentFailureException e)
        {
            assertEquals(errorMessage, e.getMessage());
        }
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Trying to migrate database 'my 1. database' from version 099 to 101." + OSUtilities.LINE_SEPARATOR
                + "ERROR OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - " + errorMessage, logRecorder
                .getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testRevertMigration()
    {
        final String fromVersion = "101";
        final String toVersion = "099";
        context.checking(new MyExpectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(true));
                    one(logDAO).getLastEntry();
                    LogEntry logEntry = new LogEntry();
                    logEntry.setRunStatus(LogEntry.RunStatus.SUCCESS);
                    logEntry.setVersion(fromVersion);
                    will(returnValue(logEntry));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my database"));
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);

        String errorMessage = "Cannot revert database 'my database' from version 101 to earlier version 099.";
        try
        {
            migrationEngine.migrateTo(toVersion);
            fail("EnvironmentFailureException expected because migration couldn't be reverted");
        } catch (EnvironmentFailureException e)
        {
            assertEquals(errorMessage, e.getMessage());
        }
        assertEquals("ERROR OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - " + errorMessage, logRecorder
                .getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testNullLogEntry()
    {
        context.checking(new MyExpectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));

                    one(adminDAO).dropDatabase();
                    one(logDAO).canConnectToDatabase();
                    will(returnValue(true));
                    one(logDAO).getLastEntry();
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, true);

        String message = "Inconsistent database: Empty database version log.";
        try
        {
            migrationEngine.migrateTo("001");
            fail("EnvironmentFailureException expected because of empty database version log.");
        } catch (EnvironmentFailureException e)
        {
            assertEquals(message, e.getMessage());
        }
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - Dropping database."
                + OSUtilities.LINE_SEPARATOR + "ERROR OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - " + message, logRecorder
                .getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testLastLogEntryIsSTART()
    {
        final LogEntry logEntry = new LogEntry();
        logEntry.setRunStatus(LogEntry.RunStatus.START);
        logEntry.setModuleName("module");
        logEntry.setVersion("042");
        logEntry.setRunStatusTimestamp(new Date(420));
        context.checking(new MyExpectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));

                    one(adminDAO).dropDatabase();
                    one(logDAO).canConnectToDatabase();
                    will(returnValue(true));
                    one(logDAO).getLastEntry();
                    will(returnValue(logEntry));
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, true);

        String message = "Inconsistent database: Last creation/migration didn't succeed. Last log entry: " + logEntry;
        try
        {
            migrationEngine.migrateTo("001");
            fail("EnvironmentFailureException expected because of empty database version log.");
        } catch (EnvironmentFailureException e)
        {
            assertEquals(message, e.getMessage());
        }
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - Dropping database."
                + OSUtilities.LINE_SEPARATOR + "ERROR OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - " + message, logRecorder
                .getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testLastLogEntryIsFAILED()
    {
        final LogEntry logEntry = new LogEntry();
        logEntry.setRunStatus(LogEntry.RunStatus.FAILED);
        logEntry.setModuleName("module");
        logEntry.setVersion("042");
        logEntry.setRunException("exception");
        logEntry.setRunStatusTimestamp(new Date(420));
        context.checking(new MyExpectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));

                    one(adminDAO).dropDatabase();
                    one(logDAO).canConnectToDatabase();
                    will(returnValue(true));
                    one(logDAO).getLastEntry();
                    will(returnValue(logEntry));
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, true);

        String message = "Inconsistent database: Last creation/migration didn't succeed. Last log entry: " + logEntry;
        try
        {
            migrationEngine.migrateTo("001");
            fail("EnvironmentFailureException expected because of empty database version log.");
        } catch (EnvironmentFailureException e)
        {
            assertEquals(message, e.getMessage());
        }
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - Dropping database."
                + OSUtilities.LINE_SEPARATOR + "ERROR OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - " + message, logRecorder
                .getLogContent());

        context.assertIsSatisfied();
    }

    @Test
    public void testFailureOfScriptExecution()
    {
        final String fromVersion = "001";
        final String toVersion = "002";
        final RuntimeException exception = new RuntimeException("execution of script failed");
        context.checking(new MyExpectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(true));
                    one(logDAO).getLastEntry();
                    final LogEntry logEntry = new LogEntry();
                    logEntry.setRunStatus(LogEntry.RunStatus.SUCCESS);
                    logEntry.setVersion(fromVersion);
                    will(returnValue(logEntry));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my database"));
                    one(scriptProvider).tryGetMigrationScript(fromVersion, toVersion);
                    final Script script = new Script("m-1-2", "code", toVersion);
                    will(returnValue(script));
                    one(scriptExecutor).execute(script, true, logDAO);
                    will(throwException(exception));
                }
            });
        final DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);

        try
        {
            migrationEngine.migrateTo(toVersion);
            fail("Exception expected");
        } catch (RuntimeException e)
        {
            assertSame(exception, e);
        }
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Trying to migrate database 'my database' from version 001 to 002.", logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

}
