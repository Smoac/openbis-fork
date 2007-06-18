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

/**
 * Tests of {@link DBMigrationEngine} using mocks for database and {@link SqlScriptProvider}.
 *
 * @author Franz-Josef Elmer
 */
public class DBMigrationEngineTest
{
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
        context.checking(new Expectations()
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
                one(adminDAO).createOwner();
                one(adminDAO).createDatabase();
                one(logDAO).createTable();
                one(scriptProvider).getSchemaScript(version);
                Script schemaScript = new Script("schema", "schema code");
                will(returnValue(schemaScript));
                one(logDAO).logStart(version, schemaScript.getName(), schemaScript.getCode());
                one(scriptExecutor).execute(schemaScript.getCode());
                one(logDAO).logSuccess(version, schemaScript.getName());
                one(scriptProvider).getDataScript(version);
                Script dataScript = new Script("data", "data code");
                will(returnValue(dataScript));
                one(logDAO).logStart(version, dataScript.getName(), dataScript.getCode());
                one(scriptExecutor).execute(dataScript.getCode());
                one(logDAO).logSuccess(version, dataScript.getName());
                one(adminDAO).getDatabaseName();
                will(returnValue("my database"));
            }
        });
        DBMigrationEngine migrationEngine = new DBMigrationEngine(daoFactory, scriptProvider, true);
        
        logRecorder.reset();
        migrationEngine.migrateTo(version);
        assertEquals("INFO  OPERATION.ch.systemsx.cisd.dbmigration.DBMigrationEngine - "
                + "Database 'my database' version 042 has been successfully created.", getLogContent());
        
        context.assertIsSatisfied();
    }
    
    private String getLogContent()
    {
        return new String(logRecorder.toByteArray()).trim();
    }
}
