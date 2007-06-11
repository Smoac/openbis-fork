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

package ch.systemsx.cisd.datamover.rsync;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.CollectionIO;
import ch.systemsx.cisd.common.utilities.StoringUncaughtExceptionHandler;

/**
 * Test cases for the {@link RsyncCopier} class.
 * 
 * @author Bernd Rinn
 */
public class RsyncCopierTest
{

    private static final File unitTestRootDirectory = new File("targets" + File.separator + "unit-test-wd");

    private static final File workingDirectory = new File(unitTestRootDirectory, "RsyncCopierTest");

    private final File sourceFile = new File(workingDirectory, "a");

    private final File destinationDirectory = new File(workingDirectory, "b");

    private final StoringUncaughtExceptionHandler exceptionHandler = new StoringUncaughtExceptionHandler();

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
        destinationDirectory.delete();
        assert destinationDirectory.mkdir();
        destinationDirectory.deleteOnExit();
    }

    private File createRsync(String... lines) throws IOException, InterruptedException
    {
        final File rsyncBinary = new File(workingDirectory, "rsync");
        rsyncBinary.delete();
        CollectionIO.writeIterable(rsyncBinary, Arrays.asList(lines));
        Runtime.getRuntime().exec(String.format("/bin/chmod +x %s", rsyncBinary.getPath())).waitFor();
        rsyncBinary.deleteOnExit();
        return rsyncBinary;
    }

    private File createRsync(int exitValue) throws IOException, InterruptedException
    {
        return createRsync("#! /bin/sh", "exit " + exitValue);
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncOK() throws IOException, InterruptedException
    {
        final File buggyRsyncBinary = createRsync(0);
        final RsyncCopier copier = new RsyncCopier(buggyRsyncBinary, null, false);
        Status status = copier.copy(sourceFile, destinationDirectory);
        assert Status.OK == status;
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncRetriableFailure() throws IOException, InterruptedException
    {
        final int exitValue = 11;
        final File buggyRsyncBinary = createRsync(exitValue);
        final RsyncCopier copier = new RsyncCopier(buggyRsyncBinary, null, false);
        Status status = copier.copy(sourceFile, destinationDirectory);
        assertEquals(StatusFlag.RETRIABLE_ERROR, status.getFlag());
        assertEquals(RsyncExitValueTranslator.getMessage(exitValue), status.getMessage());
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncFatalFailure() throws IOException, InterruptedException
    {
        final int exitValue = 1;
        final File buggyRsyncBinary = createRsync(exitValue);
        final RsyncCopier copier = new RsyncCopier(buggyRsyncBinary, null, false);
        Status status = copier.copy(sourceFile, destinationDirectory);
        assertEquals(StatusFlag.FATAL_ERROR, status.getFlag());
        assertEquals(RsyncExitValueTranslator.getMessage(exitValue), status.getMessage());
    }

    @Test(groups =
        { "requires_unix" })
    public void testRsyncTermination() throws IOException, InterruptedException
    {
        final File sleepyRsyncBinary = createRsync("#! /bin/sh", "/bin/sleep 100");
        final RsyncCopier copier = new RsyncCopier(sleepyRsyncBinary, null, false);
        (new Thread(new Runnable()
            {
                public void run()
                {
                    final long SLEEP_MILLIS = 20;
                    final int MAX_COUNT = 50;
                    boolean OK = false;
                    int count = 0;
                    while (OK == false && count < MAX_COUNT)
                    {
                        ++count;
                        try
                        {
                            Thread.sleep(SLEEP_MILLIS);
                        } catch (InterruptedException e)
                        {
                            // Can't happen.
                        }
                        OK = copier.terminate();
                    }
                }
            })).start();
        Status status = copier.copy(sourceFile, destinationDirectory);
        assertEquals(RsyncCopier.TERMINATED_STATUS, status);
    }

}
