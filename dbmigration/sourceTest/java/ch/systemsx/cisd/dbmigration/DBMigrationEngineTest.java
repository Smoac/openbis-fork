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
import static org.testng.AssertJUnit.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
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
    }
    
    private Mockery context;
    private ISqlScriptProvider scriptProvider;
    private IDAOFactory daoFactory;
    private IDatabaseAdminDAO adminDAO;
    private IDatabaseVersionLogDAO logDAO;
    private ISqlScriptExecutor scriptExecutor;
    private PrintStream systemOut;
    private PrintStream systemErr;
    private ByteArrayOutputStream logRecorder;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        scriptProvider = context.mock(ISqlScriptProvider.class);
        daoFactory = context.mock(IDAOFactory.class);
        adminDAO = context.mock(IDatabaseAdminDAO.class);
        logDAO = context.mock(IDatabaseVersionLogDAO.class);
        scriptExecutor = context.mock(ISqlScriptExecutor.class);
        logRecorder = new ByteArrayOutputStream();
        systemOut = System.out;
        systemErr = System.err;
        System.setErr(new PrintStream(logRecorder));
        System.setOut(new PrintStream(logRecorder));
        Properties properties = new Properties();
        properties.setProperty("log4j.rootLogger", "DEBUG, TestAppender");
        properties.setProperty("log4j.appender.TestAppender", ConsoleAppender.class.getName());
        properties.setProperty("log4j.appender.TestAppender.layout", PatternLayout.class.getName());
        properties.setProperty("log4j.appender.TestAppender.layout.ConversionPattern", "%-5p %c - %m%n");
        PropertyConfigurator.configure(properties);
    }
    
    @AfterMethod
    public void tearDown()
    {
        if (systemOut != null)
        {
            System.setOut(systemOut);
        }
        if (systemErr != null)
        {
            System.setErr(systemErr);
        }
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
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
                one(adminDAO).createDatabase();
                one(logDAO).createTable();
                one(scriptProvider).getSchemaScript(version);
                expectSuccessfulExecution(new Script("schema", "schema code"), version);
                one(scriptProvider).getDataScript(version);
                expectSuccessfulExecution(new Script("data", "data code"), version);
                one(adminDAO).getDatabaseName();
                will(returnValue("my 2. database"));
            }
        });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, true);
        
        logRecorder.reset();
        migrationEngine.migrateTo(version);
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 1. database' does not exist." + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 2. database' version 042 has been successfully created.", getLogContent());
        
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
                one(adminDAO).createDatabase();
                one(logDAO).createTable();
                one(scriptProvider).getSchemaScript(version);
                expectSuccessfulExecution(new Script("schema", "schema code"), version);
                one(scriptProvider).getDataScript(version);
                expectSuccessfulExecution(new Script("data", "data code"), version);
                one(adminDAO).getDatabaseName();
                will(returnValue("my 2. database"));
            }

        });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);
        
        logRecorder.reset();
        migrationEngine.migrateTo(version);
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 1. database' does not exist." + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my 2. database' version 042 has been successfully created.", getLogContent());
        
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
                logEntry.setVersion(version);
                will(returnValue(logEntry));
                one(adminDAO).getDatabaseName();
                will(returnValue("my database"));
            }
        });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, false);
        
        logRecorder.reset();
        migrationEngine.migrateTo(version);
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "No migration needed for database 'my database'. Current version: 042.", getLogContent());
        
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
        
        logRecorder.reset();
        migrationEngine.migrateTo(toVersion);
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Migrating database 'my 1. database' from version '099' to '101'." + OSUtilities.LINE_SEPARATOR
                + "INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - " 
                + "Database 'my 2. database' successfully migrated from version '099' to '101'.", getLogContent());
        
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
        
        logRecorder.reset();
        String errorMessage = "Cannot migrate database 'my 2. database' from version 099 to 100 because of "
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
                + "Migrating database 'my 1. database' from version '099' to '101'." + OSUtilities.LINE_SEPARATOR
                + "ERROR OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - " + errorMessage, getLogContent());
        
        context.assertIsSatisfied();
    }
    
    private String getLogContent()
    {
        return new String(logRecorder.toByteArray()).trim();
    }
    
}
