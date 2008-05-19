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
import java.util.TimerTask;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.highwatermark.FileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathMover;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathRemover;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class IncomingProcessorTest
{
    private static final String LOG_DEBUG_MACHINE_PREFIX = "DEBUG MACHINE.ch.systemsx.cisd.datamover.utils.DataCompletedFilter - ";

    private static final String LOG_DEBUG_PREFIX =
            "DEBUG OPERATION.ch.systemsx.cisd.datamover.utils.DataCompletedFilter - ";
    
    private static final File TEST_FOLDER = new File("targets/unit-test/IncomingProcessorTest");
    private static final String INCOMING_DIR = "incoming";
    private static final String COPY_IN_PROGRESS_DIR = "copy-in-progress";
    private static final String COPY_COMPLETE_DIR = "copy-complete";
    private static final String READY_TO_MOVE_DIR = "ready-to-move";
    private static final String TEMP_DIR = "temp";
    private static final String EXAMPLE_SCRIPT_NAME = "example-script.sh";
    private static final String EXAMPLE_SCRIPT = "echo hello world";
    private static final File TEST_FILE = new File(TEST_FOLDER, "blabla.txt");

    private BufferedAppender logRecorder;
    private Mockery context;
    private IFileSysOperationsFactory fileSysOpertationFactory;
    private IPathMover mover;
    private IPathRemover remover;
    private File incomingDir;
    private IExitHandler exitHandler;
    private File copyInProgressDir;
    private File copyCompleteDir;
    private File exampleScript;

    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        fileSysOpertationFactory = context.mock(IFileSysOperationsFactory.class);
        mover = context.mock(IPathMover.class);
        remover = context.mock(IPathRemover.class);
        exitHandler = context.mock(IExitHandler.class);
        
        FileUtilities.deleteRecursively(TEST_FOLDER);
        TEST_FOLDER.mkdirs();
        exampleScript = new File(TEST_FOLDER, EXAMPLE_SCRIPT_NAME);
        incomingDir = new File(TEST_FOLDER, INCOMING_DIR);
        incomingDir.mkdir();
        copyInProgressDir = new File(TEST_FOLDER, COPY_IN_PROGRESS_DIR);
        copyInProgressDir.mkdir();
        copyCompleteDir = new File(TEST_FOLDER, COPY_COMPLETE_DIR);
        copyCompleteDir.mkdir();
        new File(TEST_FOLDER, READY_TO_MOVE_DIR).mkdir();
        new File(TEST_FOLDER, TEMP_DIR).mkdir();
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
    public void testWithoutDataCompletedScript() throws IOException
    {
        final File testDataFile = new File(incomingDir, "test-data.txt");
        testDataFile.createNewFile();
        context.checking(new Expectations()
            {
                {
                    one(mover).tryMove(testDataFile, copyCompleteDir, "");
                    will(returnValue(new File(copyCompleteDir, testDataFile.getName())));
                }
            });
        
        DataMoverProcess process =
                createProcess("--" + PropertyNames.INCOMING_DIR, incomingDir.toString(), "-q", "1");
        TimerTask dataMoverTimerTask = process.getDataMoverTimerTask();
        dataMoverTimerTask.run(); // 1. round finds a file to process
        dataMoverTimerTask.run(); // 2. round finds that quiet period is over
        
        assertEquals("", getNormalizedLogContent());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testWithDataCompletedScript() throws IOException
    {
        FileUtilities.writeToFile(exampleScript, EXAMPLE_SCRIPT);
        final File testDataFile = new File(incomingDir, "test-data.txt");
        testDataFile.createNewFile();
        context.checking(new Expectations()
        {
            {
                one(mover).tryMove(testDataFile, copyCompleteDir, "");
                will(returnValue(new File(copyCompleteDir, testDataFile.getName())));
            }
        });
        
        DataMoverProcess process =
                createProcess("--" + PropertyNames.INCOMING_DIR, incomingDir.toString(), "-q", "1",
                        "--" + PropertyNames.DATA_COMPLETED_SCRIPT, exampleScript.toString());
        TimerTask dataMoverTimerTask = process.getDataMoverTimerTask();
        dataMoverTimerTask.run(); // 1. round finds a file to process
        dataMoverTimerTask.run(); // 2. round finds that quiet period is over
        dataMoverTimerTask.run(); // 3. round does not change status, thus no log
        
        assertEquals(
                LOG_DEBUG_PREFIX
                        + "Executing command: [sh, targets/unit-test/IncomingProcessorTest/example-script.sh, "
                        + "<wd>/targets/unit-test/IncomingProcessorTest/incoming/test-data.txt]"
                        + OSUtilities.LINE_SEPARATOR
                        + "INFO  NOTIFY.ch.systemsx.cisd.datamover.utils.DataCompletedFilter - "
                        + "Processing status of data completed script has changed to "
                        + "DataCompletedFilter.Status{ok=true,run=true,terminated=false,exitValue=0,blocked=false}. "
                        + "Command line: [sh, targets/unit-test/IncomingProcessorTest/example-script.sh, "
                        + "<wd>/targets/unit-test/IncomingProcessorTest/incoming/test-data.txt]"
                        + OSUtilities.LINE_SEPARATOR + LOG_DEBUG_PREFIX
                        + "[sh] process returned with exit value 0." + OSUtilities.LINE_SEPARATOR
                        + LOG_DEBUG_MACHINE_PREFIX + "[sh] output:" + OSUtilities.LINE_SEPARATOR
                        + LOG_DEBUG_MACHINE_PREFIX + "\"hello world\"", getNormalizedLogContent());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testWithDataCompletedScriptWhichFailsInitially() throws IOException
    {
        FileUtilities.writeToFile(exampleScript, EXAMPLE_SCRIPT + "\nrm -v "
                + TEST_FILE.toString().replace('\\', '/'));
        final File testDataFile = new File(incomingDir, "test-data.txt");
        testDataFile.createNewFile();
        context.checking(new Expectations()
        {
            {
                one(mover).tryMove(testDataFile, copyCompleteDir, "");
                will(returnValue(new File(copyCompleteDir, testDataFile.getName())));
            }
        });
        
        DataMoverProcess process =
            createProcess("--" + PropertyNames.INCOMING_DIR, incomingDir.toString(), "-q", "1",
                    "--" + PropertyNames.DATA_COMPLETED_SCRIPT, exampleScript.toString());
        TimerTask dataMoverTimerTask = process.getDataMoverTimerTask();
        dataMoverTimerTask.run(); // 1. round finds a file to process
        dataMoverTimerTask.run(); // 2. round finds that quiet period is over
        dataMoverTimerTask.run(); // 3. round does not change status, thus no log
        TEST_FILE.createNewFile();
        dataMoverTimerTask.run(); // 4. round finds changed status, thus log
        
        boolean terminated = OSUtilities.isWindows();
        assertEquals(
                LOG_DEBUG_PREFIX
                        + "Executing command: [sh, targets/unit-test/IncomingProcessorTest/example-script.sh, "
                        + "<wd>/targets/unit-test/IncomingProcessorTest/incoming/test-data.txt]"
                        + OSUtilities.LINE_SEPARATOR
                        + "ERROR NOTIFY.ch.systemsx.cisd.datamover.utils.DataCompletedFilter - "
                        + "Processing status of data completed script has changed to "
                        + "DataCompletedFilter.Status{ok=false,run=true,terminated=" + terminated
                                + ",exitValue=1,blocked=false}. "
                        + "Command line: [sh, targets/unit-test/IncomingProcessorTest/example-script.sh, "
                        + "<wd>/targets/unit-test/IncomingProcessorTest/incoming/test-data.txt]"
                        + OSUtilities.LINE_SEPARATOR
                        + "WARN  OPERATION.ch.systemsx.cisd.datamover.utils.DataCompletedFilter - "
                        + "[sh] process " + (terminated ? "was destroyed." : "returned with exit value 1.")
                        + OSUtilities.LINE_SEPARATOR
                        + "WARN  MACHINE.ch.systemsx.cisd.datamover.utils.DataCompletedFilter - "
                        + "[sh] output:"
                        + OSUtilities.LINE_SEPARATOR
                        + "WARN  MACHINE.ch.systemsx.cisd.datamover.utils.DataCompletedFilter - "
                        + "\"hello world\""
                        + OSUtilities.LINE_SEPARATOR
                        + "WARN  MACHINE.ch.systemsx.cisd.datamover.utils.DataCompletedFilter - "
                        + "\"rm: cannot remove `targets/unit-test/IncomingProcessorTest/blabla.txt': "
                        + "No such file or directory\""
                        + OSUtilities.LINE_SEPARATOR
                        + LOG_DEBUG_PREFIX
                        + "Executing command: [sh, targets/unit-test/IncomingProcessorTest/example-script.sh, "
                        + "<wd>/targets/unit-test/IncomingProcessorTest/incoming/test-data.txt]"
                        + OSUtilities.LINE_SEPARATOR
                        + "INFO  NOTIFY.ch.systemsx.cisd.datamover.utils.DataCompletedFilter - "
                        + "Processing status of data completed script has changed to "
                        + "DataCompletedFilter.Status{ok=true,run=true,terminated=false,exitValue=0,blocked=false}. "
                        + "Command line: [sh, targets/unit-test/IncomingProcessorTest/example-script.sh, "
                        + "<wd>/targets/unit-test/IncomingProcessorTest/incoming/test-data.txt]"
                        + OSUtilities.LINE_SEPARATOR + LOG_DEBUG_PREFIX
                        + "[sh] process returned with exit value 0." + OSUtilities.LINE_SEPARATOR
                        + LOG_DEBUG_MACHINE_PREFIX + "[sh] output:" + OSUtilities.LINE_SEPARATOR
                        + LOG_DEBUG_MACHINE_PREFIX + "\"hello world\"" + OSUtilities.LINE_SEPARATOR
                        + LOG_DEBUG_MACHINE_PREFIX
                        + "\"removed `targets/unit-test/IncomingProcessorTest/blabla.txt'\"",
                getNormalizedLogContent());
        
        context.assertIsSatisfied();
    }

    private String getNormalizedLogContent()
    {
        String content = logRecorder.getLogContent();
        content = content.replace(new File(System.getProperty("user.dir")).getAbsolutePath(), "<wd>");
        content = content.replace('\\', '/');
        return content;
    }
    
    private DataMoverProcess createProcess(String... args)
    {
        Parameters parameters = new Parameters(args, exitHandler);
        LocalBufferDirs localBufferDirs =
                new LocalBufferDirs(new FileWithHighwaterMark(TEST_FOLDER), COPY_IN_PROGRESS_DIR,
                        COPY_COMPLETE_DIR, READY_TO_MOVE_DIR, TEMP_DIR);
        context.checking(new Expectations()
            {
                {
                    allowing(fileSysOpertationFactory).getMover();
                    will(returnValue(mover));
                    
                    allowing(fileSysOpertationFactory).getRemover();
                    will(returnValue(remover));
                }
            });
        return IncomingProcessor.createMovingProcess(parameters, fileSysOpertationFactory,
                        new MockTimeProvider(), localBufferDirs);
        
    }
}
