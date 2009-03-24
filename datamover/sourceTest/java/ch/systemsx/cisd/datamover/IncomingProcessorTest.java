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
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.concurrent.ITimerTaskListener;
import ch.systemsx.cisd.common.concurrent.TimerTaskWithListeners;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;
import ch.systemsx.cisd.common.utilities.ITimerTaskStatusProvider;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.datamover.common.MarkerFile;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathMover;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathRemover;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;

/**
 * Test cases for the {@link IncomingProcessor} class.
 * 
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DataMoverProcess.class)
public final class IncomingProcessorTest
{

    private static final String MARKER_FILE = ".marker";

    private static final String ERROR_MARKER_FILE = ".error";

    private static final File TEST_FOLDER = new File("targets/unit-test/IncomingProcessorTest");

    private static final String INCOMING_DIR = "incoming";

    private static final String COPY_IN_PROGRESS_DIR = "copy-in-progress";

    private static final String COPY_COMPLETE_DIR = "copy-complete";

    private static final String READY_TO_MOVE_DIR = "ready-to-move";

    private static final String TEMP_DIR = "temp";

    private static final String EXAMPLE_SCRIPT_NAME = "example-script.sh";

    private static final String EXAMPLE_SCRIPT = "echo hello world";

    private BufferedAppender logRecorder;

    private Mockery context;

    private IFileSysOperationsFactory fileSysOperationFactory;

    private IPathMover mover;

    private IPathCopier copier;

    private IPathRemover remover;

    private File incomingDir;

    private File copyInProgressDir;

    private File copyCompleteDir;

    private File exampleScript;

    private ITimerTaskListener timerTaskListener = new ITimerTaskListener()
        {
            public void startRunning()
            {
                File markerFile = new File(MARKER_FILE);
                assertEquals("Missing marker file " + markerFile, true, markerFile.exists());
            }

            public void finishRunning(ITimerTaskStatusProvider statusProviderOrNull)
            {
                File markerFile = new File(MARKER_FILE);
                assertEquals("Marker file " + markerFile + " still there", false, markerFile
                        .exists());
            }

            public void canceling()
            {
                fail("Invocation of 'canceling()' not expected.");
            }

        };

    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
        logRecorder = new BufferedAppender("%m%n", Level.DEBUG);
        context = new Mockery();
        fileSysOperationFactory = context.mock(IFileSysOperationsFactory.class);
        mover = context.mock(IPathMover.class);
        copier = context.mock(IPathCopier.class);
        remover = context.mock(IPathRemover.class);

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
        final File markerFile = new File(incomingDir, MarkerFile.createRequiresDeletionBeforeCreationMarker().getName());
        testDataFile.createNewFile();
        context.checking(new Expectations()
            {
                {
                    one(mover).tryMove(markerFile, copyCompleteDir, "");
                    will(returnValue(new File(copyCompleteDir, markerFile.getName())));

                    one(mover).tryMove(testDataFile, copyCompleteDir, "");
                    will(returnValue(new File(copyCompleteDir, testDataFile.getName())));
                }
            });

        final DataMoverProcess process =
                createProcess("--" + PropertyNames.INCOMING_TARGET, incomingDir.toString(), "-q",
                        "1");
        final TimerTask dataMoverTimerTask = getInstrumentedTimerTaskFrom(process);

        final LogMonitoringAppender operationAppender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION,
                        "has been added to faulty paths file");
        dataMoverTimerTask.run(); // 1. round finds a file to process
        dataMoverTimerTask.run(); // 2. round finds that quiet period is over
        operationAppender.verifyLogHasHappened();
        LogMonitoringAppender.removeAppender(operationAppender);
        context.assertIsSatisfied();
    }

    @Test
    public void testFailureMarker() throws IOException
    {
        final File testDataFile = new File(incomingDir, "test-data.txt");
        final File markerFile = new File(incomingDir, MarkerFile.createRequiresDeletionBeforeCreationMarker().getName());
        final File errorMarker = new File(ERROR_MARKER_FILE);
        errorMarker.delete();
        assertFalse(errorMarker.exists());
        testDataFile.createNewFile();
        context.checking(new Expectations()
            {
                {
                    one(mover).tryMove(markerFile, copyCompleteDir, "");
                    will(returnValue(new File(copyCompleteDir, markerFile.getName())));

                    one(mover).tryMove(testDataFile, copyCompleteDir, "");
                    will(returnValue(new File(copyCompleteDir, testDataFile.getName())));
                }
            });

        final DataMoverProcess process =
                createProcess("--" + PropertyNames.INCOMING_TARGET, incomingDir.toString(), "-q",
                        "1");
        final TimerTask dataMoverTimerTask = getInstrumentedTimerTaskFrom(process);

        final LogMonitoringAppender operationAppender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION,
                        "has been added to faulty paths file");
        dataMoverTimerTask.run(); // 1. round finds a file to process
        assertFalse(errorMarker.exists());
        dataMoverTimerTask.run(); // 2. round finds that quiet period is over
        assertTrue(errorMarker.exists());
        operationAppender.verifyLogHasHappened();
        LogMonitoringAppender.removeAppender(operationAppender);
        context.assertIsSatisfied();
    }

    @Test
    public void testWithDataCompletedScript() throws IOException
    {
        createExampleScript(EXAMPLE_SCRIPT);
        final File testDataFile = new File(incomingDir, "test-data.txt");
        final File markerFile = new File(incomingDir, MarkerFile.createRequiresDeletionBeforeCreationMarker().getName());
        testDataFile.createNewFile();
        final File errorMarker = new File(ERROR_MARKER_FILE);
        errorMarker.delete();
        assertFalse(errorMarker.exists());
        context.checking(new Expectations()
            {
                {
                    one(mover).tryMove(markerFile, copyCompleteDir, "");
                    will(new CustomAction("move file")
                    {
                        public Object invoke(Invocation invocation) throws Throwable
                        {
                            final File result =
                                    new File(copyCompleteDir, markerFile.getName());
                            markerFile.renameTo(result);
                            return result;
                        }
                    });

                    one(mover).tryMove(testDataFile, copyCompleteDir, "");
                    will(new CustomAction("move file")
                        {
                            public Object invoke(Invocation invocation) throws Throwable
                            {
                                final File result =
                                        new File(copyCompleteDir, testDataFile.getName());
                                testDataFile.renameTo(result);
                                return result;
                            }
                        });
                }
            });

        final DataMoverProcess process =
                createProcess("--" + PropertyNames.INCOMING_TARGET, incomingDir.toString(), "-q",
                        "1", "--" + PropertyNames.DATA_COMPLETED_SCRIPT, exampleScript.toString());
        final LogMonitoringAppender operationAppender1 =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION,
                        "Processing status of data completed script has changed");
        final LogMonitoringAppender operationAppender2 =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, "Running command",
                        "process returned with exit value 0");

        final TimerTask dataMoverTimerTask = getInstrumentedTimerTaskFrom(process);
        dataMoverTimerTask.run(); // 1. round finds a file to process
        assertFalse(errorMarker.exists());
        dataMoverTimerTask.run(); // 2. round finds that quiet period is over
        assertFalse(errorMarker.exists());
        operationAppender1.verifyLogHasHappened();
        operationAppender2.verifyLogHasHappened();

        logRecorder.resetLogContent();
        dataMoverTimerTask.run(); // 3. round does not change status, thus no log
        assertFalse(errorMarker.exists());
        assertEquals("", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testWithDataCompletedScriptWhichFailsInitially() throws IOException
    {
        createExampleScript("exit 1");
        final File testDataFile = new File(incomingDir, "test-data.txt");
        testDataFile.createNewFile();
        final File markerFile = new File(incomingDir, MarkerFile.createRequiresDeletionBeforeCreationMarker().getName());
        context.checking(new Expectations()
            {
                {
                    one(mover).tryMove(markerFile, copyCompleteDir, "");
                    will(new CustomAction("move file")
                    {
                        public Object invoke(Invocation invocation) throws Throwable
                        {
                            final File result =
                                    new File(copyCompleteDir, markerFile.getName());
                            markerFile.renameTo(result);
                            return result;
                        }

                    });

                    one(mover).tryMove(testDataFile, copyCompleteDir, "");
                    will(returnValue(new File(copyCompleteDir, testDataFile.getName())));
                }
            });

        final DataMoverProcess process =
                createProcess("--" + PropertyNames.INCOMING_TARGET, incomingDir.toString(), "-q",
                        "1", "--" + PropertyNames.DATA_COMPLETED_SCRIPT, exampleScript.toString());
        final TimerTask dataMoverTimerTask = getInstrumentedTimerTaskFrom(process);
        dataMoverTimerTask.run(); // 1. round finds a file to process
        dataMoverTimerTask.run(); // 2. round finds that quiet period is over
        final LogMonitoringAppender operationAppender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION,
                        "Processing status of data completed script has changed");

        logRecorder.resetLogContent();
        dataMoverTimerTask.run(); // 3. round does not change status, thus no log
        assertEquals("", logRecorder.getLogContent());
        operationAppender.verifyLogHasNotHappened();

        logRecorder.resetLogContent();
        operationAppender.reset();
        createExampleScript("exit 0"); // now the script will run fine
        dataMoverTimerTask.run(); // 4. round finds changed status, thus log
        assertTrue(logRecorder.getLogContent().length() > 0);
        operationAppender.verifyLogHasHappened();

        context.assertIsSatisfied();
    }

    private void createExampleScript(String text)
    {
        FileUtilities.writeToFile(exampleScript, text);
        if (OSUtilities.isWindows() == false)
        {
            Logger logger = LogFactory.getLogger(LogCategory.OPERATION, getClass());
            List<String> cmd = Arrays.asList("chmod", "755", exampleScript.getAbsolutePath());
            ProcessExecutionHelper.run(cmd, logger, logger);
        }
    }

    private TimerTask getInstrumentedTimerTaskFrom(final DataMoverProcess process)
    {
        TimerTask timerTask = process.getTimerTask();
        if (timerTask instanceof TimerTaskWithListeners)
        {
            ((TimerTaskWithListeners) timerTask).addListener(timerTaskListener);
        } else
        {
            fail("Timer task is not an instance of " + TimerTaskWithListeners.class + ": "
                    + timerTask.getClass().getName());
        }
        return timerTask;
    }

    private DataMoverProcess createProcess(final String... args)
    {
        final Parameters parameters = new Parameters(args);
        final LocalBufferDirs localBufferDirs =
                new LocalBufferDirs(new HostAwareFileWithHighwaterMark(TEST_FOLDER),
                        COPY_IN_PROGRESS_DIR, COPY_COMPLETE_DIR, READY_TO_MOVE_DIR, TEMP_DIR);
        final File incomingDeletionCheckFile =
                new File(new File(TEST_FOLDER, INCOMING_DIR), MarkerFile
                        .createRequiresDeletionBeforeCreationMarker().getName());
        final File inProgressDeletionCheckFile =
            new File(new File(TEST_FOLDER, COPY_IN_PROGRESS_DIR), MarkerFile
                    .createRequiresDeletionBeforeCreationMarker().getName());
        context.checking(new Expectations()
            {
                {
                    allowing(fileSysOperationFactory).getMover();
                    will(returnValue(mover));

                    allowing(fileSysOperationFactory).getRemover();
                    will(returnValue(remover));

                    one(fileSysOperationFactory).getCopier(false);
                    will(returnValue(copier));

                    allowing(copier).copy(incomingDeletionCheckFile, copyInProgressDir);
                    will(returnValue(Status.OK));
                    
                    one(remover).remove(incomingDeletionCheckFile);
                    one(remover).remove(inProgressDeletionCheckFile);
                }
            });
        return IncomingProcessor.createMovingProcess(parameters, MARKER_FILE, ERROR_MARKER_FILE,
                null, fileSysOperationFactory, new MockTimeProvider(), localBufferDirs);

    }
}
