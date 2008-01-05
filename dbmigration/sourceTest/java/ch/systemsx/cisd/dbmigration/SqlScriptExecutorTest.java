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

package ch.systemsx.cisd.dbmigration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.Script;
import ch.systemsx.cisd.common.db.ISqlScriptExecutionLogger;
import ch.systemsx.cisd.common.logging.LogInitializer;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for the {@link SqlScriptExecutor}.
 * 
 * @author Bernd Rinn
 */
public class SqlScriptExecutorTest
{

    private class TestSqlScriptExecutor extends SqlScriptExecutor
    {
        private final List<String> executions;
        
        private final Error th;

        public TestSqlScriptExecutor(boolean singleStepMode)
        {
            this(null, singleStepMode);
        }
        public TestSqlScriptExecutor(Error th, boolean singleStepMode)
        {
            super(new DelegatingDataSource(), singleStepMode);
            this.th = th;
            this.executions = new ArrayList<String>();
        }

        @Override
        void execute(String script)
        {
            if (th != null)
            {
                throw th;
            }
            executions.add(script);
        }

        List<String> getExecutions()
        {
            return executions;
        }
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws Exception
    {
        LogInitializer.init();
    }

    @Test
    public void testHappyCase()
    {
        final TestSqlScriptExecutor scriptExecutor = new TestSqlScriptExecutor(false);
        final String simpleScriptContent1 = "statement1; statement2;";
        final Script simpleScript1 = new Script("script1", simpleScriptContent1, "VERSION");
        final String simpleScriptContent2 = "statement3; statement4;";
        final Script simpleScript2 = new Script("script2", simpleScriptContent2, "VERSION");
        scriptExecutor.execute(simpleScript1, true, null);
        scriptExecutor.execute(simpleScript2, true, null);
        assertEquals(2, scriptExecutor.getExecutions().size());
        assertEquals(Arrays.asList(simpleScriptContent1, simpleScriptContent2), scriptExecutor.getExecutions());
    }

    @Test
    public void testSingleStepMode()
    {
        final TestSqlScriptExecutor scriptExecutor = new TestSqlScriptExecutor(true);
        final String simpleScriptContent1 = "statement1; statement2;";
        final Script simpleScript1 = new Script("script1", simpleScriptContent1, "VERSION");
        final String simpleScriptContent2 = "statement3.1 -- some coment\n statement3.2 ; ";
        final Script simpleScript2 = new Script("script2", simpleScriptContent2, "VERSION");
        scriptExecutor.execute(simpleScript1, true, null);
        scriptExecutor.execute(simpleScript2, true, null);
        assertEquals(3, scriptExecutor.getExecutions().size());
        assertEquals(Arrays.asList("statement1;", "statement2;", "statement3.1 statement3.2;"), scriptExecutor
                .getExecutions());
    }

    @Test
    public void testLoggingHappyCase()
    {
        final Mockery context = new Mockery();
        try
        {
            final TestSqlScriptExecutor scriptExecutor = new TestSqlScriptExecutor(false);
            final String simpleScriptContent = "statement1; statement2;";
            final Script simpleScript = new Script("script1", simpleScriptContent, "VERSION");
            final ISqlScriptExecutionLogger logger = context.mock(ISqlScriptExecutionLogger.class);
            context.checking(new Expectations()
            {
                {
                    one(logger).logStart(simpleScript);
                    one(logger).logSuccess(simpleScript);
                }
                
            });
            scriptExecutor.execute(simpleScript, true, logger);
        } finally
        {
            context.assertIsSatisfied();
        }
    }

    class MyError extends Error
    {
        private static final long serialVersionUID = 1L;
    }
    
    @Test(expectedExceptions=MyError.class)
    public void testLoggingScriptThrowsException()
    {
        final Mockery context = new Mockery();
        try
        {
            final MyError error = new MyError();
            final TestSqlScriptExecutor scriptExecutor = new TestSqlScriptExecutor(error, false);
            final String simpleScriptContent = "statement1; statement2;";
            final Script simpleScript = new Script("script1", simpleScriptContent, "VERSION");
            final ISqlScriptExecutionLogger logger = context.mock(ISqlScriptExecutionLogger.class);
            context.checking(new Expectations()
            {
                {
                    one(logger).logStart(simpleScript);
                    one(logger).logFailure(simpleScript, error);
                }
                
            });
            scriptExecutor.execute(simpleScript, true, logger);
        } finally
        {
            context.assertIsSatisfied();
        }
    }

}
