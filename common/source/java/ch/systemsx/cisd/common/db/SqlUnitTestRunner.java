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

package ch.systemsx.cisd.common.db;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.OSUtilities;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class SqlUnitTestRunner
{
    private static final class TestResult
    {
        private final boolean ok;
        private final Throwable throwable;
        private final File testScript;
        
        public TestResult(File testScript)
        {
            this(testScript, true, null);
        }
        
        public TestResult(File testScript, Throwable throwable)
        {
            this(testScript, false, throwable);
        }

        public TestResult(File testScript, boolean ok, Throwable throwable)
        {
            this.testScript = testScript;
            this.ok = ok;
            this.throwable = throwable;
        }
        
        public final File getTestScript()
        {
            return testScript;
        }

        public final boolean isOK()
        {
            return ok;
        }

        public final Throwable getThrowable()
        {
            return throwable;
        }
    }
    
    private final ISqlScriptExecutor executor;
    private final PrintWriter writer;
    
    public SqlUnitTestRunner(ISqlScriptExecutor executor, PrintWriter writer)
    {
        this.executor = executor;
        this.writer = writer;
    }

    public void run(File testScriptsFolder)
    {
        if (testScriptsFolder.exists() == false)
        {
            return; // no tests
        }
        File[] testCases = testScriptsFolder.listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.startsWith(".") == false;
                }
            });
        Arrays.sort(testCases);
        List<TestResult> results = new ArrayList<TestResult>();
        for (File file : testCases)
        {
            runTestCase(file, results);
        }
        StringBuilder builder = new StringBuilder();
        for (TestResult result : results)
        {
            if (result.isOK() == false)
            {
                builder.append("Test script ").append(getName(result.getTestScript())).append(" failed because of ");
                builder.append(result.getThrowable()).append(OSUtilities.LINE_SEPARATOR);
            }
        }
        if (builder.length() > 0)
        {
            throw new AssertionError(builder.toString());
        }
       
    }

    private void runTestCase(File testCaseFolder, List<TestResult> results)
    {
        writer.println("====== Test case " + testCaseFolder.getName() + " ======");
        File buildupFile = new File(testCaseFolder, "buildup.sql");
        if (buildupFile.exists())
        {
            results.add(runScript(buildupFile));
        }
        File[] testScripts = testCaseFolder.listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.length() > 1 && name.charAt(1) == '=';
                }
            });
        Arrays.sort(testScripts);
        for (File testScript : testScripts)
        {
            results.add(runScript(testScript));
        }
        File teardownFile = new File(testCaseFolder, "teardown.sql");
        if (teardownFile.exists())
        {
            results.add(runScript(teardownFile));
        }
    }
    
    private TestResult runScript(File scriptFile)
    {
        writer.println("     execute script " + scriptFile.getName());
        try
        {
            executor.execute(FileUtilities.loadToString(scriptFile));
            return new TestResult(scriptFile);
        } catch (Throwable t)
        {
            while (t.getCause() != null)
            {
                t = t.getCause();
            }
            return new TestResult(scriptFile, t);
        }
    }

    private String getName(File testScript)
    {
        return testScript.getParentFile().getName() + File.separatorChar + testScript.getName();
    }
    
}