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

package ch.systemsx.cisd.common.filesystem.rsync;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.collections.CollectionIO;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.StoringUncaughtExceptionHandler;

/**
 * Test cases for the {@link RsyncCopier} class.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses = RsyncCopier.class)
public final class RsyncCopierTest
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            RsyncCopierTest.class);

    private static final long SLEEP_MILLIS = 1000L;

    private static final File unitTestRootDirectory = new File("targets" + File.separator
            + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "RsyncCopierTest");

    private final File sourceFile = new File(workingDirectory, "a");

    private final File sourceDirectory = new File(workingDirectory, "aa");

    private final File destinationDirectory = new File(workingDirectory, "b");

    private final StoringUncaughtExceptionHandler exceptionHandler =
            new StoringUncaughtExceptionHandler();

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

    private final String sleepyMessage = "I am feeling sooo sleepy...";

    private File createSleepingRsyncExecutable(String name, long millisToSleep) throws IOException,
            InterruptedException
    {
        return createExecutable(name, "#! /bin/sh",
                "if [ \"$1\" = \"--version\" ]; then echo \"rsync  version 3.0.3\"; exit 0; fi",
                "echo " + sleepyMessage, "sleep " + (millisToSleep / 1000.0f), "exit 0");
    }

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
        unitTestRootDirectory.mkdirs();
        assert unitTestRootDirectory.isDirectory();
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
    }

    @BeforeMethod
    public void setUp() throws IOException
    {
        workingDirectory.delete();
        workingDirectory.mkdirs();
        workingDirectory.deleteOnExit();
        exceptionHandler.reset();
        sourceFile.delete();
        sourceFile.createNewFile();

        sourceFile.deleteOnExit();
        sourceDirectory.delete();
        assertTrue(sourceDirectory.mkdir());
        sourceDirectory.deleteOnExit();
        destinationDirectory.delete();
        assertTrue(destinationDirectory.mkdir());
        destinationDirectory.deleteOnExit();
    }

    private File createRsync(final String rsyncVersion, final String... additionalLines)
            throws IOException, InterruptedException
    {
        final File rsyncBinary = new File(workingDirectory, "rsync");
        rsyncBinary.delete();
        final List<String> lines = new ArrayList<String>();
        lines.addAll(Arrays.asList("#! /bin/sh", "if [ \"$1\" = \"--version\" ]; then ",
                String.format("  echo \"rsync  version %s\"", rsyncVersion), "exit 0", "fi"));
        lines.addAll(Arrays.asList(additionalLines));
        CollectionIO.writeIterable(rsyncBinary, lines);
        Runtime.getRuntime().exec(String.format("/bin/chmod +x %s", rsyncBinary.getPath()))
                .waitFor();
        rsyncBinary.deleteOnExit();
        return rsyncBinary;
    }

    private File createRsync(final int exitValue) throws IOException, InterruptedException
    {
        return createRsync("2.6.9", "exit " + exitValue);
    }

    @Test
    public void testCommandLineForMutableCopyLocal() throws IOException, InterruptedException
    {
        final File rsyncBinary = createRsync(0);
        final RsyncCopier copier = new RsyncCopier(rsyncBinary, rsyncBinary, false, false);
        final List<String> cmdLine =
                copier.createCommandLineForMutableCopy(sourceDirectory, null, destinationDirectory,
                        null, null, null, false);
        assertEquals(rsyncBinary.getAbsolutePath(), cmdLine.get(0));
        assertEquals(sourceDirectory.getAbsolutePath(), cmdLine.get(cmdLine.size() - 2));
        assertEquals(destinationDirectory.getAbsolutePath() + "/", cmdLine.get(cmdLine.size() - 1));
    }

    @Test
    public void testCommandLineForMutableCopyLocalNoOwnerNoGroup() throws IOException,
            InterruptedException
    {
        final File rsyncBinary = createRsync(0);
        final RsyncCopier copier =
                new RsyncCopier(rsyncBinary, rsyncBinary, false, false, "--no-owner", "--no-group");
        final List<String> cmdLine =
                copier.createCommandLineForMutableCopy(sourceDirectory, null, destinationDirectory,
                        null, null, null, false);
        assertEquals(rsyncBinary.getAbsolutePath(), cmdLine.get(0));
        assertEquals("--no-owner", cmdLine.get(cmdLine.size() - 4));
        assertEquals("--no-group", cmdLine.get(cmdLine.size() - 3));
        assertEquals(sourceDirectory.getAbsolutePath(), cmdLine.get(cmdLine.size() - 2));
        assertEquals(destinationDirectory.getAbsolutePath() + "/", cmdLine.get(cmdLine.size() - 1));
    }

    @Test
    public void testCommandLineForMutableCopyCustomParameters() throws IOException,
            InterruptedException
    {
        final File rsyncBinary = createRsync(0);
        final RsyncCopier copier =
                new RsyncCopier(rsyncBinary, rsyncBinary, "--archive", "--no-perms");
        final List<String> cmdLine =
                copier.createCommandLineForMutableCopy(sourceDirectory, null, destinationDirectory,
                        null, null, null, false);
        assertEquals(rsyncBinary.getAbsolutePath(), cmdLine.get(0));
        assertEquals("--archive", cmdLine.get(1));
        assertEquals("--no-perms", cmdLine.get(2));
        assertEquals(sourceDirectory.getAbsolutePath(), cmdLine.get(3));
        assertEquals(destinationDirectory.getAbsolutePath() + "/", cmdLine.get(4));
    }

    @Test
    public void testCommandLineForMutableCopyLocalContentRatherThanDirectory() throws IOException,
            InterruptedException
    {
        final File rsyncBinary = createRsync(0);
        final RsyncCopier copier = new RsyncCopier(rsyncBinary, rsyncBinary, false, false);
        final List<String> cmdLine =
                copier.createCommandLineForMutableCopy(sourceDirectory, null, destinationDirectory,
                        null, null, null, true);
        assertEquals(rsyncBinary.getAbsolutePath(), cmdLine.get(0));
        assertEquals(sourceDirectory.getAbsolutePath() + "/", cmdLine.get(cmdLine.size() - 2));
        assertEquals(destinationDirectory.getAbsolutePath() + "/", cmdLine.get(cmdLine.size() - 1));
    }

    @Test
    public void testCommandLineForMutableCopyToRemoteSSHTunnel() throws IOException,
            InterruptedException
    {
        final File rsyncBinary = createRsync(0);
        final File dstPath = new File("dst");
        final String host = "hst";
        final RsyncCopier copier = new RsyncCopier(rsyncBinary, rsyncBinary, false, false);
        final List<String> cmdLine =
                copier.createCommandLineForMutableCopy(sourceDirectory, null, dstPath, host, null,
                        null, false);
        assertEquals(rsyncBinary.getAbsolutePath(), cmdLine.get(0));
        assertEquals(sourceDirectory.getAbsolutePath(), cmdLine.get(cmdLine.size() - 2));
        assertEquals(host + ":dst/", cmdLine.get(cmdLine.size() - 1));
    }

    @Test
    public void testCommandLineForMutableCopyToRemoteSSHTunnelContentRatherThanDirectory()
            throws IOException, InterruptedException
    {
        final File rsyncBinary = createRsync(0);
        final File dstPath = new File("dst");
        final String host = "hst";
        final RsyncCopier copier = new RsyncCopier(rsyncBinary, rsyncBinary, false, false);
        final List<String> cmdLine =
                copier.createCommandLineForMutableCopy(sourceDirectory, null, dstPath, host, null,
                        null, true);
        assertEquals(rsyncBinary.getAbsolutePath(), cmdLine.get(0));
        assertEquals(sourceDirectory.getAbsolutePath() + "/", cmdLine.get(cmdLine.size() - 2));
        assertEquals(host + ":dst/", cmdLine.get(cmdLine.size() - 1));
    }

    @Test
    public void testCommandLineForMutableCopyToRemoteRsyncModule() throws IOException,
            InterruptedException
    {
        final File rsyncBinary = createRsync(0);
        final File dstPath = new File("dst");
        final String host = "hst";
        final String rsyncModule = "rsmod";
        final RsyncCopier copier = new RsyncCopier(rsyncBinary, rsyncBinary, false, false);
        final List<String> cmdLine =
                copier.createCommandLineForMutableCopy(sourceDirectory, null, dstPath, host,
                        rsyncModule, null, false);
        assertEquals(rsyncBinary.getAbsolutePath(), cmdLine.get(0));
        assertEquals(sourceDirectory.getAbsolutePath(), cmdLine.get(cmdLine.size() - 2));
        assertEquals(host + "::" + rsyncModule + "/", cmdLine.get(cmdLine.size() - 1));
    }

    @Test
    public void testCommandLineForMutableCopyToRemoteRsyncModuleContentRatherThanDirectory()
            throws IOException, InterruptedException
    {
        final File rsyncBinary = createRsync(0);
        final File dstPath = new File("dst");
        final String host = "hst";
        final String rsyncModule = "rsmod";
        final RsyncCopier copier = new RsyncCopier(rsyncBinary, rsyncBinary, false, false);
        final List<String> cmdLine =
                copier.createCommandLineForMutableCopy(sourceDirectory, null, dstPath, host,
                        rsyncModule, null, true);
        assertEquals(rsyncBinary.getAbsolutePath(), cmdLine.get(0));
        assertEquals(sourceDirectory.getAbsolutePath() + "/", cmdLine.get(cmdLine.size() - 2));
        assertEquals(host + "::" + rsyncModule + "/", cmdLine.get(cmdLine.size() - 1));
    }

    @Test
    public void testCommandLineForMutableCopyToRemoteRsyncModuleWithPwFile() throws IOException,
            InterruptedException
    {
        final File rsyncBinary = createRsync(0);
        final File dstPath = new File("dst");
        final String host = "hst";
        final String rsyncModule = "rsmod";
        final File pwFile = new File(workingDirectory, "rsync.pwd");
        FileUtils.touch(pwFile);
        pwFile.deleteOnExit();
        final RsyncCopier copier = new RsyncCopier(rsyncBinary, rsyncBinary, false, false);
        final List<String> cmdLine =
                copier.createCommandLineForMutableCopy(sourceDirectory, null, dstPath, host,
                        rsyncModule, pwFile.getPath(), false);
        assertEquals(rsyncBinary.getAbsolutePath(), cmdLine.get(0));
        assertEquals("--password-file", cmdLine.get(cmdLine.size() - 4));
        assertEquals(workingDirectory + "/rsync.pwd", cmdLine.get(cmdLine.size() - 3));
        assertEquals(sourceDirectory.getAbsolutePath(), cmdLine.get(cmdLine.size() - 2));
        assertEquals(host + "::" + rsyncModule + "/", cmdLine.get(cmdLine.size() - 1));
    }

    @Test
    public void testCommandLineForMutableCopyToRemoteRsyncModuleWithNonExistentPwFile()
            throws IOException, InterruptedException
    {
        final File rsyncBinary = createRsync(0);
        final File dstPath = new File("dst");
        final String host = "hst";
        final String rsyncModule = "rsmod";
        final File pwFile = new File(workingDirectory, "rsync.pwd");
        pwFile.delete();
        assertFalse(pwFile.exists());
        final RsyncCopier copier = new RsyncCopier(rsyncBinary, rsyncBinary, false, false);
        final List<String> cmdLine =
                copier.createCommandLineForMutableCopy(sourceDirectory, null, dstPath, host,
                        rsyncModule, pwFile.getPath(), false);
        assertEquals(rsyncBinary.getAbsolutePath(), cmdLine.get(0));
        assertFalse("--password-file".equals(cmdLine.get(cmdLine.size() - 4)));
        assertFalse((workingDirectory + "/rsync.pwd").equals(cmdLine.get(cmdLine.size() - 3)));
        assertEquals(sourceDirectory.getAbsolutePath(), cmdLine.get(cmdLine.size() - 2));
        assertEquals(host + "::" + rsyncModule + "/", cmdLine.get(cmdLine.size() - 1));
    }

    @Test
    public void testCommandLineForMutableCopyFromRemoteSSHTunnel() throws IOException,
            InterruptedException
    {
        final File rsyncBinary = createRsync(0);
        final String host = "hst";
        final RsyncCopier copier = new RsyncCopier(rsyncBinary, rsyncBinary, false, false);
        final List<String> cmdLine =
                copier.createCommandLineForMutableCopy(sourceDirectory, host, destinationDirectory,
                        null, null, null, false);
        assertEquals(rsyncBinary.getAbsolutePath(), cmdLine.get(0));
        assertEquals(host + ":" + sourceDirectory.getPath(), cmdLine.get(cmdLine.size() - 2));
        assertEquals(destinationDirectory.getAbsolutePath() + "/", cmdLine.get(cmdLine.size() - 1));
    }

    @Test
    public void testCommandLineForMutableCopyFromRemoteRsyncModule() throws IOException,
            InterruptedException
    {
        final File rsyncBinary = createRsync(0);
        final String host = "hst";
        final String rsyncModule = "rsmod";
        final File pwFile = new File(workingDirectory, "rsync.pwd");
        FileUtils.touch(pwFile);
        pwFile.deleteOnExit();
        final RsyncCopier copier = new RsyncCopier(rsyncBinary, rsyncBinary, false, false);
        final List<String> cmdLine =
                copier.createCommandLineForMutableCopy(sourceDirectory, host, destinationDirectory,
                        null, rsyncModule, pwFile.getPath(), false);
        assertEquals(rsyncBinary.getAbsolutePath(), cmdLine.get(0));
        assertEquals("--password-file", cmdLine.get(cmdLine.size() - 4));
        assertEquals(workingDirectory + "/rsync.pwd", cmdLine.get(cmdLine.size() - 3));
        assertEquals(host + "::" + rsyncModule, cmdLine.get(cmdLine.size() - 2));
        assertEquals(destinationDirectory.getAbsolutePath() + "/", cmdLine.get(cmdLine.size() - 1));
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncFileOK() throws IOException, InterruptedException
    {
        final File rsyncBinary = createRsync(0);
        final RsyncCopier copier = new RsyncCopier(rsyncBinary, null, false, false);
        final Status status = copier.copy(sourceFile, destinationDirectory);
        assertEquals(Status.OK, status);
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncDirectoryOK() throws IOException, InterruptedException
    {
        final File parametersLogFile = new File(workingDirectory, "parameters.log");
        final File loggingRsyncBinary =
                createRsync("2.6.7",
                        String.format("echo \"$@\" > %s", parametersLogFile.getAbsolutePath()));
        final RsyncCopier copier = new RsyncCopier(loggingRsyncBinary, null, false, false);
        final Status status = copier.copy(sourceDirectory, destinationDirectory);
        assertEquals(Status.OK, status);
        final String expectedRsyncCmdLine =
                String.format("--archive --delete --inplace --append %s %s/\n",
                        sourceDirectory.getAbsolutePath(), destinationDirectory.getAbsolutePath());
        final String observedRsyncCmdLine = FileUtilities.loadToString(parametersLogFile);
        assertEquals(expectedRsyncCmdLine, observedRsyncCmdLine);
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncDirectoryContentOK() throws IOException, InterruptedException
    {
        final File parametersLogFile = new File(workingDirectory, "parameters.log");
        final File loggingRsyncBinary =
                createRsync("2.6.7",
                        String.format("echo \"$@\" > %s", parametersLogFile.getAbsolutePath()));
        final RsyncCopier copier = new RsyncCopier(loggingRsyncBinary, null, false, false);
        final Status status = copier.copyContent(sourceDirectory, destinationDirectory);
        assertEquals(Status.OK, status);
        final String expectedRsyncCmdLine =
                String.format("--archive --delete --inplace --append %s/ %s/\n",
                        sourceDirectory.getAbsolutePath(), destinationDirectory.getAbsolutePath());
        final String observedRsyncCmdLine = FileUtilities.loadToString(parametersLogFile);
        assertEquals(expectedRsyncCmdLine, observedRsyncCmdLine);
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncImmutableCopyImplicitNameOK() throws IOException, InterruptedException
    {
        final File parametersLogFile = new File(workingDirectory, "parameters.log");
        final File loggingRsyncBinary =
                createRsync("2.6.7",
                        String.format("echo \"$@\" > %s", parametersLogFile.getAbsolutePath()));
        final RsyncCopier copier = new RsyncCopier(loggingRsyncBinary, null, false, false);
        assertTrue(copier.copyDirectoryImmutably(sourceDirectory, destinationDirectory, null));
        final String absWd = workingDirectory.getAbsolutePath();
        final String expectedRsyncCmdLine =
                String.format("--archive --link-dest=%s/aa %s/aa/ %s/b/aa\n", absWd, absWd, absWd);
        final String observedRsyncCmdLine = FileUtilities.loadToString(parametersLogFile);
        assertEquals(expectedRsyncCmdLine, observedRsyncCmdLine);
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncImmutableCopyExplicitNameOK() throws IOException, InterruptedException
    {
        final File parametersLogFile = new File(workingDirectory, "parameters.log");
        final File loggingRsyncBinary =
                createRsync("2.6.7",
                        String.format("echo \"$@\" > %s", parametersLogFile.getAbsolutePath()));
        final RsyncCopier copier = new RsyncCopier(loggingRsyncBinary, null, false, false);
        final String name = "xxx";
        assertTrue(copier.copyDirectoryImmutably(sourceDirectory, destinationDirectory, name));
        final String absWd = workingDirectory.getAbsolutePath();
        final String expectedRsyncCmdLine =
                String.format("--archive --link-dest=%s/aa %s/aa/ %s/b/%s\n", absWd, absWd, absWd,
                        name);
        final String observedRsyncCmdLine = FileUtilities.loadToString(parametersLogFile);
        assertEquals(expectedRsyncCmdLine, observedRsyncCmdLine);
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncImmutableCopyFailed() throws IOException, InterruptedException
    {
        final File failingRsyncBinary = createRsync(1);
        final RsyncCopier copier = new RsyncCopier(failingRsyncBinary, null, false, false);
        assertFalse(copier.copyDirectoryImmutably(sourceDirectory, destinationDirectory, null));
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncRetriableFailure() throws IOException, InterruptedException
    {
        final int exitValue = 11;
        StatusFlag expectedStatus = StatusFlag.RETRIABLE_ERROR;

        testRsyncFailure(exitValue, expectedStatus);
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncFatalFailure() throws IOException, InterruptedException
    {
        final int exitValue = 1;
        StatusFlag expectedStatus = StatusFlag.ERROR;

        testRsyncFailure(exitValue, expectedStatus);
    }

    private void testRsyncFailure(final int exitValue, StatusFlag expectedStatus)
            throws IOException, InterruptedException
    {
        final File buggyRsyncBinary = createRsync(exitValue);
        final RsyncCopier copier = new RsyncCopier(buggyRsyncBinary, null, false, false);
        final Status status = copier.copy(sourceFile, destinationDirectory);
        assertEquals(expectedStatus, status.getFlag());
        assertEquals(RsyncExitValueTranslator.getMessage(exitValue), status.tryGetErrorMessage());
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncAppendMode() throws IOException, InterruptedException
    {
        final File parametersLogFile = new File(workingDirectory, "parameters.log");
        final File loggingRsyncBinary =
                createRsync("2.6.7",
                        String.format("echo \"$@\" > %s", parametersLogFile.getAbsolutePath()));
        final RsyncCopier copier = new RsyncCopier(loggingRsyncBinary, null, false, false);
        copier.copy(sourceFile, destinationDirectory);
        final String rsyncParameters = FileUtilities.loadToString(parametersLogFile);
        assertFalse(rsyncParameters.indexOf("--whole-file") >= 0);
        assertTrue(rsyncParameters.indexOf("--append") >= 0);
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncAppendModeWhenNotSupported() throws IOException, InterruptedException
    {
        final File parametersLogFile = new File(workingDirectory, "parameters.log");
        final File loggingRsyncBinary =
                createRsync("2.6.6",
                        String.format("echo \"$@\" > %s", parametersLogFile.getAbsolutePath()));
        final RsyncCopier copier = new RsyncCopier(loggingRsyncBinary, null, false, false);
        copier.copy(sourceFile, destinationDirectory);
        final String rsyncParameters = FileUtilities.loadToString(parametersLogFile);
        assertTrue(rsyncParameters.indexOf("--whole-file") >= 0);
        assertFalse(rsyncParameters.indexOf("--append") >= 0);
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncOverwriteMode() throws IOException, InterruptedException
    {
        final File parametersLogFile = new File(workingDirectory, "parameters.log");
        final File loggingRsyncBinary =
                createRsync("2.6.7",
                        String.format("echo \"$@\" > %s", parametersLogFile.getAbsolutePath()));
        final RsyncCopier copier = new RsyncCopier(loggingRsyncBinary, null, false, true);
        copier.copy(sourceFile, destinationDirectory);
        final String rsyncParameters = FileUtilities.loadToString(parametersLogFile);
        assertTrue(rsyncParameters.indexOf("--whole-file") >= 0);
        assertFalse(rsyncParameters.indexOf("--append") >= 0);
    }

    @Test(groups =
        { "requires_unix", "slow" })
    public void testRsyncTermination() throws IOException, InterruptedException
    {
        final File sleepyRsyncBinary = createSleepProcess(100);
        final RsyncCopier copier = new RsyncCopier(sleepyRsyncBinary, null, false, false);
        final Thread thread = new Thread(new Runnable()
            {
                //
                // Runnable
                //

                public final void run()
                {
                    final long sleepMillis = 20;
                    final int maxCount = 50;
                    boolean ok = false;
                    int count = 0;
                    while (ok == false && count < maxCount)
                    {
                        ++count;
                        try
                        {
                            Thread.sleep(sleepMillis);
                        } catch (final InterruptedException e)
                        {
                            // Can't happen.
                        }
                        ok = copier.terminate();
                    }
                }
            });
        thread.start();
        final Status status = copier.copy(sourceFile, destinationDirectory);
        assertEquals(RsyncCopier.TERMINATED_STATUS, status);
    }

    private File createSleepProcess(int seconds) throws IOException, InterruptedException
    {
        return createRsync("2.6.9", "/bin/sleep " + seconds);
    }

    @Test(groups =
        { "slow", "requires_unix" }, expectedExceptions = InterruptedExceptionUnchecked.class)
    public void testStopRsyncCopierCopyImmutably() throws Exception
    {
        final File rsyncExecutable = createSleepingRsyncExecutable("rsync", SLEEP_MILLIS);
        assertTrue(rsyncExecutable.exists());
        final File source = new File(unitTestRootDirectory, "a");
        source.mkdir();
        source.deleteOnExit();
        assertTrue(source.isDirectory());
        final File destination = new File(unitTestRootDirectory, "b");
        destination.mkdir();
        destination.deleteOnExit();
        assertTrue(destination.isDirectory());
        final RsyncCopier copier = new RsyncCopier(rsyncExecutable);
        final Thread thisThread = Thread.currentThread();
        final Timer timer = new Timer();
        try
        {
            timer.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        operationLog.info("Interrupting thread");
                        thisThread.interrupt();
                    }
                }, SLEEP_MILLIS / 10);
            copier.copyDirectoryImmutably(source, destination, null);
        } finally
        {
            timer.cancel();
        }
    }

}
