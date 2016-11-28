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

package ch.systemsx.cisd.datamover;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessIOStrategy;
import ch.systemsx.cisd.common.process.ProcessResult;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DataMover.class)
public class ShellScriptTest
{
    static final String PID_FILE_NAME = "datamover.pid";

    static final String SCRIPT_FILE_NAME = "datamover.sh";

    private static final File ORIGINAL_SCRIPT_FILE = new File(new File("dist"), SCRIPT_FILE_NAME);

    private static final File WORKING_DIRECTORY = new File("targets/shell-script-test");

    private static final File SCRIPT_FILE = new File(WORKING_DIRECTORY, SCRIPT_FILE_NAME);

    private static final File PID_FILE = new File(WORKING_DIRECTORY, PID_FILE_NAME);

    private static final File MARKER_FILE_INCOMING_PROCESSING = new File(WORKING_DIRECTORY,
            DataMover.INCOMING_PROCESS_MARKER_FILENAME);

    private static final File MARKER_FILE_OUTGOING_PROCESSING = new File(WORKING_DIRECTORY,
            DataMover.OUTGOING_PROCESS_MARKER_FILENAME);

    private static final File MARKER_FILE_SHUTDOWN = new File(WORKING_DIRECTORY,
            DataMover.SHUTDOWN_MARKER_FILENAME);

    private static final File TARGET_LOCATION_FILE = new File(WORKING_DIRECTORY,
            DataMover.OUTGOING_TARGET_LOCATION_FILE);

    private static final File MARKER_FILE_INCOMING_ERROR = new File(WORKING_DIRECTORY,
            DataMover.INCOMING_ERROR_MARKER_FILENAME);

    private static final File MARKER_FILE_OUTGOING_ERROR = new File(WORKING_DIRECTORY,
            DataMover.OUTGOING_ERROR_MARKER_FILENAME);

    private Logger operationLog;

    private Logger machineLog;

    private BufferedAppender logRecorder;

    @BeforeClass(alwaysRun = true)
    public void init()
    {
        LogInitializer.init();
        operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());
        machineLog = LogFactory.getLogger(LogCategory.MACHINE, getClass());
    }

    @BeforeMethod
    public void setUp()
    {
        FileUtilities.deleteRecursively(WORKING_DIRECTORY);
        assertEquals(true, WORKING_DIRECTORY.mkdirs());
        FileOperations.getInstance().copyFile(ORIGINAL_SCRIPT_FILE, SCRIPT_FILE);
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        FileUtilities.deleteRecursively(WORKING_DIRECTORY);
    }

    @Test
    public void testStatusDown()
    {
        checkStatus(3, "DOWN", false);
    }

    @Test
    public void testStatusStale() throws IOException
    {
        FileUtilities.writeToFile(PID_FILE, "-1");
        checkStatus(4, "STALE", false);
        checkStatus(4, "Datamover is dead (stale pid -1)", true);
    }

    @Test
    public void testStatusIdle() throws IOException
    {
        // Convention for unit tests to get PID accepted as "running".
        FileUtils.writeStringToFile(PID_FILE, "fake");
        checkStatus(0, "IDLE", false);
        checkStatus(0, "Datamover (pid fake) is running and in idle state", true);
    }

    @Test
    public void testStatusProcessing() throws IOException
    {
        // Convention for unit tests to get PID accepted as "running".
        FileUtils.writeStringToFile(PID_FILE, "fake");
        FileUtils.touch(MARKER_FILE_INCOMING_PROCESSING);
        checkStatus(0, "PROCESSING", false);
        checkStatus(0, "Datamover (pid fake) is running and in processing state", true);
        FileUtils.touch(MARKER_FILE_OUTGOING_PROCESSING);
        checkStatus(0, "PROCESSING", false);
    }

    @Test
    public void testStatusError() throws IOException
    {
        // Convention for unit tests to get PID accepted as "running".
        FileUtils.writeStringToFile(PID_FILE, "fake");
        FileUtils.touch(MARKER_FILE_INCOMING_ERROR);
        checkStatus(1, "ERROR", false);
        checkStatus(1, "Datamover (pid fake) is running and in error state:", true, 2);
        FileUtils.touch(MARKER_FILE_OUTGOING_ERROR);
        checkStatus(1, "ERROR", false);
    }

    @Test
    public void testStatusShutdown() throws IOException
    {
        // Convention for unit tests to get PID accepted as "running".
        FileUtils.writeStringToFile(PID_FILE, "fake");
        FileUtils.touch(MARKER_FILE_SHUTDOWN);
        checkStatus(2, "SHUTDOWN", false);
        checkStatus(2, "Datamover (pid fake) is in shutdown mode", true);
        FileUtils.touch(MARKER_FILE_INCOMING_PROCESSING);
        checkStatus(2, "SHUTDOWN", false);
    }

    @Test
    public void testUnknownTarget()
    {
        ProcessResult result = executeShellScript("target");
        assertEquals(1, result.getExitValue());
        assertEquals(0, result.getOutput().size());
    }

    @Test
    public void testTarget()
    {
        String targetlocation = "target-location";
        FileUtilities.writeToFile(TARGET_LOCATION_FILE, targetlocation);
        ProcessResult result = executeShellScript("target");
        assertEquals(0, result.getExitValue());
        List<String> output = result.getOutput();
        assertEquals(targetlocation, output.get(0));
        assertEquals(1, output.size());
    }

    private void checkStatus(int expectedExitValue, String expectedStatus, boolean pretty)
    {
        checkStatus(expectedExitValue, expectedStatus, pretty, 1);
    }

    private void checkStatus(int expectedExitValue, String expectedStatus, boolean pretty,
            int numberOfOutputLines)
    {
        ProcessResult result = executeShellScript(pretty ? "status" : "mstatus");
        assertEquals(expectedExitValue, result.getExitValue());
        List<String> output = result.getOutput();
        assertEquals(expectedStatus, output.get(0));
        assertEquals(numberOfOutputLines, output.size());
    }

    private ProcessResult executeShellScript(String... arguments)
    {
        List<String> command = new ArrayList<String>();
        command.add("bash");
        command.add(SCRIPT_FILE.getAbsolutePath());
        command.addAll(Arrays.asList(arguments));
        @SuppressWarnings("deprecation")
        ProcessResult result =
                ProcessExecutionHelper.run(command, operationLog, machineLog,
                        ConcurrencyUtilities.NO_TIMEOUT,
                        ProcessIOStrategy.TEXT_IO_STRATEGY, true);
        return result;
    }
}
