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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.jdbc.BadSqlGrammarException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.db.ISqlScriptExecutor;
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
        protected void expectSuccessfulExecution(Script script, final String version)
        {
            will(returnValue(script));
            one(logDAO).logStart(version, script.getName(), script.getCode());
            one(scriptExecutor).execute(script.getCode());
            one(logDAO).logSuccess(version, script.getName());
        }

        protected void expectCreateLogDAOTable()
        {
            one(scriptProvider).getScript(DBMigrationEngine.CREATE_LOG_SQL);
            Script script = new Script(DBMigrationEngine.CREATE_LOG_SQL, "create log");
            will(returnValue(script));
            one(logDAO).createTable(with(same(script)));
        }
    }

    private Mockery context;

    private ISqlScriptProvider scriptProvider;

    private IDAOFactory daoFactory;

    private IDatabaseAdminDAO adminDAO;

    private IDatabaseVersionLogDAO logDAO;

    private ISqlScriptExecutor scriptExecutor;

    private IMassUploader massUploader;

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
        massUploader = context.mock(IMassUploader.class);
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
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

                    one(adminDAO).dropDatabase();
                    one(logDAO).canConnectToDatabase();
                    will(returnValue(false));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(adminDAO).createOwner();
                    one(adminDAO).createDatabase();

                    expectCreateLogDAOTable();
                    one(scriptProvider).getSchemaScript(version);
                    expectSuccessfulExecution(new Script("schema", "schema code"), version);
                    one(scriptProvider).getMassUploadFiles(version);
                    final File massUploadFile = new File("1=materials.csv");
                    will(returnValue(new File[]
                        { massUploadFile }));
                    one(scriptProvider).getDataScript(version);
                    expectSuccessfulExecution(new Script("data", "data code"), version);
                    one(massUploader).performMassUpload(massUploadFile);
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 2. database"));
                    one(scriptProvider).getFinishScript(version);
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, true);
        migrationEngine.migrateTo(version);
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
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
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

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
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
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
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(false));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(adminDAO).createOwner();
                    one(adminDAO).createDatabase();
                    expectCreateLogDAOTable();
                    one(scriptProvider).getSchemaScript(version);
                    expectSuccessfulExecution(new Script("schema", "schema code"), version);
                    one(scriptProvider).getDataScript(version);
                    expectSuccessfulExecution(new Script("data", "data code"), version);
                    one(scriptProvider).getMassUploadFiles(version);
                    will(returnValue(new File[0]));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 2. database"));
                    one(scriptProvider).getFinishScript(version);
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
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(false));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(adminDAO).createOwner();
                    one(adminDAO).createDatabase();
                    expectCreateLogDAOTable();
                    one(scriptProvider).getSchemaScript(version);
                    expectSuccessfulExecution(new Script("schema", "schema code"), version);
                    one(scriptProvider).getDataScript(version);
                    one(scriptProvider).getMassUploadFiles(version);
                    will(returnValue(new File[0]));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 2. database"));
                    one(scriptProvider).getFinishScript(version);
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
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(false));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(adminDAO).createOwner();
                    one(adminDAO).createDatabase();
                    expectCreateLogDAOTable();
                    one(scriptProvider).getSchemaScript(version);
                    one(scriptProvider).getFinishScript(version);
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);
        String message = "No schema script found for version 042";
        try
        {
            migrationEngine.migrateTo(version);
            fail("EnvironmentFailureException expected because of missing schema script");
        } catch (EnvironmentFailureException e)
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
    public void testCreateFromScratchWithMissingCreateLogScript()
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
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(false));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(adminDAO).createOwner();
                    one(adminDAO).createDatabase();
                    one(scriptProvider).getScript(DBMigrationEngine.CREATE_LOG_SQL);
                    one(scriptProvider).getFinishScript(version);
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);
        String message = "Missing script createLog.sql";
        try
        {
            migrationEngine.migrateTo(version);
            fail("EnvironmentFailureException expected because of missing log creation script");
        } catch (EnvironmentFailureException e)
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
    public void testCreateFromScratchWithCreateLogScriptWhichFails()
    {
        final String version = "042";
        final RuntimeException exception = new RuntimeException();
        context.checking(new MyExpectations()
            {
                {
                    one(daoFactory).getDatabaseDAO();
                    will(returnValue(adminDAO));
                    one(daoFactory).getDatabaseVersionLogDAO();
                    will(returnValue(logDAO));
                    one(daoFactory).getSqlScriptExecutor();
                    will(returnValue(scriptExecutor));
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(false));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my database"));
                    one(adminDAO).createOwner();
                    one(adminDAO).createDatabase();
                    expectCreateLogDAOTable();
                    will(throwException(exception));
                    one(scriptProvider).getFinishScript(version);
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);
        try
        {
            migrationEngine.migrateTo(version);
            fail("RuntimeException expected because of failing log creation script");
        } catch (RuntimeException e)
        {
            assertSame(exception, e);
        }
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my database' does not exist." + OSUtilities.LINE_SEPARATOR
                + "ERROR OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - Script 'createLog.sql' failed:"
                + OSUtilities.LINE_SEPARATOR + "create log" + OSUtilities.LINE_SEPARATOR + getStackTrace(exception),
                logRecorder.getLogContent());

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
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(true));
                    one(logDAO).getLastEntry();
                    LogEntry logEntry = new LogEntry();
                    logEntry.setRunStatus(LogEntry.RunStatus.SUCCESS);
                    logEntry.setVersion(version);
                    will(returnValue(logEntry));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my database"));
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);

        migrationEngine.migrateTo(version);
        assertEquals("DEBUG OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "No migration needed for database 'my database'. It has the right version (042).", logRecorder
                .getLogContent());

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
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(true));
                    one(logDAO).getLastEntry();
                    LogEntry logEntry = new LogEntry();
                    logEntry.setRunStatus(LogEntry.RunStatus.SUCCESS);
                    logEntry.setVersion(fromVersion);
                    will(returnValue(logEntry));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(scriptProvider).getMigrationScript(fromVersion, "100");
                    expectSuccessfulExecution(new Script("m-099-100", "code 099 100"), toVersion);
                    one(scriptProvider).getMigrationScript("100", toVersion);
                    expectSuccessfulExecution(new Script("m-100-101", "code 100 101"), toVersion);
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 2. database"));
                }
            });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);

        migrationEngine.migrateTo(toVersion);
        String logContent = logRecorder.getLogContent();
        logContent.replaceAll("\\d* msec", "0 msec");
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Trying to migrate database 'my 1. database' from version 099 to 101." + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Successfully migrated from version 099 to 100 in 0 msec" + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Successfully migrated from version 100 to 101 in 0 msec" + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 2. database' successfully migrated from version 099 to 101.", logContent);

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
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(true));
                    one(logDAO).getLastEntry();
                    LogEntry logEntry = new LogEntry();
                    logEntry.setRunStatus(LogEntry.RunStatus.SUCCESS);
                    logEntry.setVersion(fromVersion);
                    will(returnValue(logEntry));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my 1. database"));
                    one(scriptProvider).getMigrationScript(fromVersion, "100");
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
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

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
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

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
        assertEquals("ERROR OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - " + message, logRecorder
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
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

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
        assertEquals("ERROR OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - " + message, logRecorder
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
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

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
        assertEquals("ERROR OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - " + message, logRecorder
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
                    one(daoFactory).getMassUploader();
                    will(returnValue(massUploader));

                    one(logDAO).canConnectToDatabase();
                    will(returnValue(true));
                    one(logDAO).getLastEntry();
                    LogEntry logEntry = new LogEntry();
                    logEntry.setRunStatus(LogEntry.RunStatus.SUCCESS);
                    logEntry.setVersion(fromVersion);
                    will(returnValue(logEntry));
                    one(adminDAO).getDatabaseName();
                    will(returnValue("my database"));
                    one(scriptProvider).getMigrationScript(fromVersion, toVersion);
                    Script script = new Script("m-1-2", "code");
                    will(returnValue(script));
                    one(logDAO).logStart(toVersion, script.getName(), script.getCode());
                    one(scriptExecutor).execute(script.getCode());
                    will(throwException(exception));
                    one(logDAO)
                            .logFailure(with(equal(toVersion)), with(equal(script.getName())), with(same(exception)));
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
                + "Trying to migrate database 'my database' from version 001 to 002." + OSUtilities.LINE_SEPARATOR
                + "ERROR OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - Executing script 'm-1-2' failed."
                + OSUtilities.LINE_SEPARATOR + getStackTrace(exception), logRecorder.getLogContent());

        context.assertIsSatisfied();
    }

    private String getStackTrace(final Throwable throwable)
    {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString().trim();
    }

}
