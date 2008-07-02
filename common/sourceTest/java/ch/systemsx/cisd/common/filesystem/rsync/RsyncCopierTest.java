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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.collections.CollectionIO;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.filesystem.rsync.RsyncCopier;
import ch.systemsx.cisd.common.filesystem.rsync.RsyncExitValueTranslator;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.StoringUncaughtExceptionHandler;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * Test cases for the {@link RsyncCopier} class.
 * 
 * @author Bernd Rinn
 */
public final class RsyncCopierTest
{

    private static final File unitTestRootDirectory =
            new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "RsyncCopierTest");

    private final File sourceFile = new File(workingDirectory, "a");

    private final File sourceDirectory = new File(workingDirectory, "aa");

    private final File destinationDirectory = new File(workingDirectory, "b");

    private final StoringUncaughtExceptionHandler exceptionHandler =
            new StoringUncaughtExceptionHandler();

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
        lines.addAll(Arrays.asList("#! /bin/sh", "if [ \"$1\" = \"--version\" ]; then ", String
                .format("  echo \"rsync  version %s\"", rsyncVersion), "exit 0", "fi"));
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

    @Test(groups =
        { "requires_unix" })
    public void testRsyncOK() throws IOException, InterruptedException
    {
        final File rsyncBinary = createRsync(0);
        final RsyncCopier copier = new RsyncCopier(rsyncBinary, null, false, false);
        final Status status = copier.copy(sourceFile, destinationDirectory);
        assert Status.OK == status;
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncImmutableCopyImplicitNameOK() throws IOException, InterruptedException
    {
        final File parametersLogFile = new File(workingDirectory, "parameters.log");
        final File loggingRsyncBinary =
                createRsync("2.6.7", String.format("echo \"$@\" > %s", parametersLogFile
                        .getAbsolutePath()));
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
                createRsync("2.6.7", String.format("echo \"$@\" > %s", parametersLogFile
                        .getAbsolutePath()));
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
        StatusFlag expectedStatus = StatusFlag.FATAL_ERROR;

        testRsyncFailure(exitValue, expectedStatus);
    }

    private void testRsyncFailure(final int exitValue, StatusFlag expectedStatus)
            throws IOException, InterruptedException
    {
        final File buggyRsyncBinary = createRsync(exitValue);
        final RsyncCopier copier = new RsyncCopier(buggyRsyncBinary, null, false, false);
        final Status status = copier.copy(sourceFile, destinationDirectory);
        assertEquals(expectedStatus, status.getFlag());
        assertEquals(RsyncExitValueTranslator.getMessage(exitValue), status.getMessage());
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncAppendMode() throws IOException, InterruptedException
    {
        final File parametersLogFile = new File(workingDirectory, "parameters.log");
        final File loggingRsyncBinary =
                createRsync("2.6.7", String.format("echo \"$@\" > %s", parametersLogFile
                        .getAbsolutePath()));
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
                createRsync("2.6.6", String.format("echo \"$@\" > %s", parametersLogFile
                        .getAbsolutePath()));
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
                createRsync("2.6.7", String.format("echo \"$@\" > %s", parametersLogFile
                        .getAbsolutePath()));
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

}
