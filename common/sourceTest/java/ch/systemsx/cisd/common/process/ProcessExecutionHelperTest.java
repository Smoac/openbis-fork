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

package ch.systemsx.cisd.common.process;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.collections.CollectionIO;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.StopException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper.OutputReadingStrategy;

/**
 * Test cases for the {@link ProcessExecutionHelper}.
 * 
 * @author Bernd Rinn
 */
public class ProcessExecutionHelperTest
{

    private static final long WATCHDOG_WAIT_MILLIS = 1000L;

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, ProcessExecutionHelperTest.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ProcessExecutionHelperTest.class);

    private static final File unitTestRootDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory =
            new File(unitTestRootDirectory, "ProcessExecutionHelperTest");

    private File createExecutable(String name, String... lines) throws IOException,
            InterruptedException
    {
        final File executable = new File(workingDirectory, name);
        executable.delete();
        CollectionIO.writeIterable(executable, Arrays.asList(lines));
        Runtime.getRuntime().exec(String.format("/bin/chmod +x %s", executable.getPath()))
                .waitFor();
        executable.deleteOnExit();
        return executable;
    }

    private File createExecutable(String name, int exitValue) throws IOException,
            InterruptedException
    {
        return createExecutable(name, "#! /bin/sh", "exit " + exitValue);
    }

    private final String sleepyMessage = "I am feeling sooo sleepy...";

    private File createSleepingExecutable(String name, long millisToSleep) throws IOException,
            InterruptedException
    {
        return createExecutable(name, "#! /bin/sh", "echo " + sleepyMessage, "sleep "
                + (millisToSleep / 1000.0f), "exit 0");
    }

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
        unitTestRootDirectory.mkdirs();
        assert unitTestRootDirectory.isDirectory();
    }

    @BeforeMethod
    public void setUp() throws IOException
    {
        workingDirectory.delete();
        workingDirectory.mkdirs();
        workingDirectory.deleteOnExit();
    }

    @Test(groups =
        { "requires_unix" })
    public void testExecutionOKWithoutTimeOut() throws Exception
    {
        final File dummyExec = createExecutable("dummyOKWithoutTimeOut.sh", 0);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog);
        assertTrue(ok);
    }

    @Test(groups =
        { "requires_unix" })
    public void testExecutionFailedWithoutTimeOut() throws Exception
    {
        final File dummyExec = createExecutable("dummyFailingWithoutTimeOut.sh", 1);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog);
        assertFalse(ok);
    }

    @Test(groups =
        { "requires_unix" })
    public void testExecutionOKWithTimeOut() throws Exception
    {
        final File dummyExec = createExecutable("dummyOKWithTimeOut.sh", 0);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, WATCHDOG_WAIT_MILLIS);
        assertTrue(ok);
    }

    @Test(groups =
        { "requires_unix" })
    public void testExecutionFailedWithTimeOut() throws Exception
    {
        final File dummyExec = createExecutable("dummyFailingWithTimeOut.sh", 1);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, WATCHDOG_WAIT_MILLIS);
        assertFalse(ok);
    }

    @Test(groups =
        { "requires_unix", "slow" })
    public void testSleepyExecutionOKWithTimeOut() throws Exception
    {
        final File dummyExec =
                createSleepingExecutable("dummySleepyOKWithTimeOut.sh", WATCHDOG_WAIT_MILLIS / 2);
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, WATCHDOG_WAIT_MILLIS);
        assertTrue(result.isOK());
        assertEquals(0, result.getOutput().size());
    }

    @Test(groups =
        { "requires_unix", "slow" }, expectedExceptions =
        { StopException.class })
    public void testSleepyExecutionGetsStopped() throws Exception
    {
        final Thread thisThread = Thread.currentThread();
        final Timer timer = new Timer();
        try
        {
            timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        thisThread.interrupt();
                    }
                }, WATCHDOG_WAIT_MILLIS / 10);
            final File dummyExec =
                    createSleepingExecutable("dummySleepyFailedWithTimeOut.sh",
                            2 * WATCHDOG_WAIT_MILLIS);
            ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()), operationLog,
                    machineLog, WATCHDOG_WAIT_MILLIS);
        } finally
        {
            timer.cancel();
        }
    }

    @Test(groups =
        { "requires_unix", "slow" })
    public void testSleepyExecutionGetsInterrupted() throws Exception
    {
        final Thread thisThread = Thread.currentThread();
        final Timer timer = new Timer();
        try
        {
            timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        thisThread.interrupt();
                    }
                }, WATCHDOG_WAIT_MILLIS / 10);
            final File dummyExec =
                    createSleepingExecutable("dummySleepyFailedWithTimeOut.sh",
                            2 * WATCHDOG_WAIT_MILLIS);
            final ProcessResult result =
                    ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                            operationLog, machineLog, WATCHDOG_WAIT_MILLIS,
                            ProcessExecutionHelper.DEFAULT_OUTPUT_READING_STRATEGY, false);
            assertTrue(result.isInterruped());
        } finally
        {
            timer.cancel();
        }
    }

    @Test(groups =
        { "requires_unix", "slow" })
    public void testSleepyExecutionFailedWithTimeOut() throws Exception
    {
        final File dummyExec =
                createSleepingExecutable("dummySleepyFailedWithTimeOut.sh",
                        2 * WATCHDOG_WAIT_MILLIS);
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, WATCHDOG_WAIT_MILLIS);
        assertTrue(result.isTimedOut());
        assertFalse(result.isOK());
        assertEquals(1, result.getOutput().size());
        assertEquals(sleepyMessage, result.getOutput().get(0));
    }

    @Test(groups =
        { "requires_unix", "slow" })
    public void testTryExecutionReadProcessOutput() throws Exception
    {
        final String stdout1 = "This goes to stdout, 1";
        final String stdout2 = "This goes to stdout, 2";
        final String stderr1 = "This goes to stderr, 1";
        final String stderr2 = "This goes to stderr, 2";
        final File dummyExec =
                createExecutable("dummy.sh", "echo " + stdout1, "echo " + stderr1, "echo "
                        + stdout2, "echo " + stderr2);
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()),
                        operationLog, machineLog, ConcurrencyUtilities.NO_TIMEOUT,
                        OutputReadingStrategy.ALWAYS, false);
        final int exitValue = result.getExitValue();
        assertEquals(0, exitValue);
        result.log();
        assertTrue(result.isOutputAvailable());
        assertEquals(4, result.getOutput().size());
        assertEquals(stdout1, result.getOutput().get(0));
        assertEquals(stderr1, result.getOutput().get(1));
        assertEquals(stdout2, result.getOutput().get(2));
        assertEquals(stderr2, result.getOutput().get(3));
    }

    @Test(groups = "requires_unix")
    public void testStartupFailed()
    {
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList("some_non_existent_executable"),
                        operationLog, machineLog);
        result.log();
        assertFalse(result.isRun());
        assertTrue(result.getStartupFailureMessage().indexOf(
                "some_non_existent_executable: not found") >= 0);
        System.out.println("Startup failure: " + result.getStartupFailureMessage());
    }

}
