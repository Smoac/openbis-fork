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

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.CollectionIO;

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

    private static final File unitTestRootDirectory = new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "ProcessExecutionHelperTest");

    private File createExecutable(String name, String... lines) throws IOException, InterruptedException
    {
        final File executable = new File(workingDirectory, name);
        executable.delete();
        CollectionIO.writeIterable(executable, Arrays.asList(lines));
        Runtime.getRuntime().exec(String.format("/bin/chmod +x %s", executable.getPath())).waitFor();
        executable.deleteOnExit();
        return executable;
    }

    private File createExecutable(String name, int exitValue) throws IOException, InterruptedException
    {
        return createExecutable(name, "#! /bin/sh", "exit " + exitValue);
    }

    private File createSleepingExecutable(String name, long millisToSleep) throws IOException, InterruptedException
    {
        return createExecutable(name, "#! /bin/sh", "sleep " + (millisToSleep / 1000.0f), "exit 0");
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
    public void testExecutionOKWithoutWatchDog() throws Exception
    {
        final File dummyExec = createExecutable("dummy.sh", 0);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()), operationLog, machineLog);
        assertTrue(ok);
    }

    @Test(groups =
        { "requires_unix" })
    public void testExecutionFailedWithoutWatchDog() throws Exception
    {
        final File dummyExec = createExecutable("dummy.sh", 1);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()), operationLog, machineLog);
        assertFalse(ok);
    }

    @Test(groups =
        { "requires_unix" })
    public void testExecutionOKWithWatchDog() throws Exception
    {
        final File dummyExec = createExecutable("dummy.sh", 0);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()), WATCHDOG_WAIT_MILLIS,
                        operationLog, machineLog);
        assertTrue(ok);
    }

    @Test(groups =
        { "requires_unix" })
    public void testExecutionFailedWithWatchDog() throws Exception
    {
        final File dummyExec = createExecutable("dummy.sh", 1);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()), WATCHDOG_WAIT_MILLIS,
                        operationLog, machineLog);
        assertFalse(ok);
    }

    @Test(groups =
        { "requires_unix", "slow" })
    public void testExecutionOKWithWatchDogWaiting() throws Exception
    {
        final File dummyExec = createSleepingExecutable("dummy.sh", WATCHDOG_WAIT_MILLIS / 2);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()), WATCHDOG_WAIT_MILLIS,
                        operationLog, machineLog);
        assertTrue(ok);
    }

    @Test(groups =
        { "requires_unix", "slow" })
    public void testExecutionFailedWithWatchDogHitting() throws Exception
    {
        final File dummyExec = createSleepingExecutable("dummy.sh", 2 * WATCHDOG_WAIT_MILLIS);
        final boolean ok =
                ProcessExecutionHelper.runAndLog(Arrays.asList(dummyExec.getAbsolutePath()), WATCHDOG_WAIT_MILLIS,
                        operationLog, machineLog);
        assertFalse(ok);
    }

    @Test(groups =
        { "requires_unix", "slow" })
    public void testTryExecutionFailedWithWatchDogHitting() throws Exception
    {
        final File dummyExec = createSleepingExecutable("dummy.sh", 2 * WATCHDOG_WAIT_MILLIS);
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()), WATCHDOG_WAIT_MILLIS,
                        operationLog, machineLog);
        assertTrue(result.hasBlocked() || ProcessExecutionHelper.isProcessTerminated(result.exitValue()));
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
                createExecutable("dummy.sh", "echo " + stdout1, "echo " + stderr1, "echo " + stdout2, "echo " + stderr2);
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(dummyExec.getAbsolutePath()), operationLog, machineLog);
        final int exitValue = result.exitValue();
        assertEquals(0, exitValue);
        result.log();
        assertEquals(4, result.getProcessOutput().size());
        assertEquals(stdout1, result.getProcessOutput().get(0));
        assertEquals(stderr1, result.getProcessOutput().get(1));
        assertEquals(stdout2, result.getProcessOutput().get(2));
        assertEquals(stderr2, result.getProcessOutput().get(3));
    }

}
