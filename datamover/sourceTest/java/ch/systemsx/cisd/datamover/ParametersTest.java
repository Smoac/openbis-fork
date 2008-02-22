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

package ch.systemsx.cisd.datamover;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.utilities.SystemExit;
import ch.systemsx.cisd.datamover.filesystem.FileStoreFactory;
import ch.systemsx.cisd.datamover.filesystem.FileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.intf.IFileSysParameters;

/**
 * Test cases for the {@link Parameters} class.
 * 
 * @author Bernd Rinn
 */
public class ParametersTest
{

    private Parameters parse(String... args)
    {
        return new Parameters(args, SystemExit.SYSTEM_EXIT);
    }

    @BeforeClass
    public void init()
    {
        SystemExit.throwException = true;
    }

    @AfterClass
    public void finish()
    {
        SystemExit.throwException = false;

    }

    @Test
    public void testSetRsyncExecutableLong() throws Exception
    {
        final String RSYNC_EXEC = "/usr/local/bin/rsync";
        final Parameters parameters = parse("--rsync-executable", RSYNC_EXEC);
        assertEquals(RSYNC_EXEC, parameters.getRsyncExecutable());
    }

    @Test
    public void testSetSshExecutableLong() throws Exception
    {
        final String SSH_EXEC = "/usr/local/bin/ssh";
        final Parameters parameters = parse("--ssh-executable", SSH_EXEC);
        assertEquals(SSH_EXEC, parameters.getSshExecutable());
    }

    @Test
    public void testSetLnExecutableLong() throws Exception
    {
        final String EXEC = "/usr/local/bin/ln";
        final Parameters parameters = parse("--hard-link-executable", EXEC);
        assertEquals(EXEC, parameters.getHardLinkExecutable());
    }

    @Test
    public void testSetCleansingRegexLong() throws Exception
    {
        final String CLEANSING_REGEX = "[0-9]+";
        final Parameters parameters = parse("--cleansing-regex", CLEANSING_REGEX);
        assertEquals(CLEANSING_REGEX, parameters.tryGetCleansingRegex().pattern());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testSetInvalidCleansingRegex() throws Exception
    {
        final String CLEANSING_REGEX = "[0-9}+";
        parse("--cleansing-regex", CLEANSING_REGEX);
    }

    @Test
    public void testSetLocalDataDirLong() throws Exception
    {
        final String LOCAL_DATADIR = ".." + File.separator + "test_it_data";
        final Parameters parameters = parse("--incoming-dir", LOCAL_DATADIR);
        assertEquals(createIncomingStore(LOCAL_DATADIR, null, parameters), getIncomingStore(parameters));
    }

    @Test
    public void testSetLocalTempDirLong() throws Exception
    {
        final String LOCAL_TEMPDIR = "test_it_tmp";
        final Parameters parameters = parse("--buffer-dir", LOCAL_TEMPDIR);
        assertEquals(LOCAL_TEMPDIR, parameters.getBufferDirectoryPath().getPath());
    }

    @Test
    public void testDefaultCheckInterval() throws Exception
    {
        final Parameters parameters = parse();
        assertEquals(1000 * Parameters.DEFAULT_CHECK_INTERVAL, parameters.getCheckIntervalMillis());
    }

    @Test
    public void testDefaultCheckIntervalInternal() throws Exception
    {
        final Parameters parameters = parse();
        assertEquals(1000 * Parameters.DEFAULT_CHECK_INTERVAL_INTERNAL, parameters.getCheckIntervalInternalMillis());
    }

    @Test
    public void testDefaultQuietPeriod() throws Exception
    {
        final Parameters parameters = parse();
        assertEquals(1000 * Parameters.DEFAULT_QUIET_PERIOD, parameters.getQuietPeriodMillis());
    }

    @Test
    public void testDefaultIntervalToWaitAfterFailureMillis() throws Exception
    {
        final Parameters parameters = parse();
        assertEquals(1000 * Parameters.DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURES, parameters
                .getIntervalToWaitAfterFailure());
    }

    @Test
    public void testDefaultMaximalNumberOfRetries() throws Exception
    {
        final Parameters parameters = parse();
        assertEquals(Parameters.DEFAULT_MAXIMAL_NUMBER_OF_RETRIES, parameters.getMaximalNumberOfRetries());
    }

    @Test
    public void testDefaultInactivityPeriod() throws Exception
    {
        final Parameters parameters = parse();
        assertEquals(1000 * Parameters.DEFAULT_INACTIVITY_PERIOD, parameters.getInactivityPeriodMillis());
    }

    @Test
    public void testSetCheckIntervalLong() throws Exception
    {
        final int CHECK_INTERVAL = 5;
        final Parameters parameters = parse("--check-interval", Integer.toString(CHECK_INTERVAL));
        assertEquals(1000 * CHECK_INTERVAL, parameters.getCheckIntervalMillis());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testSetInvalidCheckInterval() throws Exception
    {
        parse("--check-interval", "5x");
    }

    @Test
    public void testSetCheckIntervalShort() throws Exception
    {
        final int CHECK_INTERVAL = 11;
        final Parameters parameters = parse("-c", Integer.toString(CHECK_INTERVAL));
        assertEquals(1000 * CHECK_INTERVAL, parameters.getCheckIntervalMillis());
    }

    @Test
    public void testSetCheckIntervalInternalLong() throws Exception
    {
        final int CHECK_INTERVAL = 1;
        final Parameters parameters = parse("--check-interval-internal", Integer.toString(CHECK_INTERVAL));
        assertEquals(1000 * CHECK_INTERVAL, parameters.getCheckIntervalInternalMillis());
    }

    @Test
    public void testDefaultQuietInterval() throws Exception
    {
        final Parameters parameters = parse();
        assertEquals(1000 * Parameters.DEFAULT_QUIET_PERIOD, parameters.getQuietPeriodMillis());
    }

    @Test
    public void testSetQuietPeriodLong() throws Exception
    {
        final int QUIET_PERIOD = 6;
        final Parameters parameters = parse("--quiet-period", Integer.toString(QUIET_PERIOD));
        assertEquals(1000 * QUIET_PERIOD, parameters.getQuietPeriodMillis());
    }

    @Test
    public void testSetQuietPeriodShort() throws Exception
    {
        final int QUIET_PERIOD = 17;
        final Parameters parameters = parse("-q", Integer.toString(QUIET_PERIOD));
        assertEquals(1000 * QUIET_PERIOD, parameters.getQuietPeriodMillis());
    }

    @Test
    public void testDefaultRsyncOverwrite()
    {
        final Parameters parameters = parse();
        assertFalse(parameters.isRsyncOverwrite());
    }

    @Test
    public void testSetRsyncOverwrite()
    {
        final Parameters parameters = parse("--rsync-overwrite");
        assertTrue(parameters.isRsyncOverwrite());
    }

    @Test
    public void testSetMandatoryOptions() throws Exception
    {
        final String LOCAL_DATADIR = ".." + File.separator + "ldata";
        final String LOCAL_TEMPDIR = "l" + File.separator + "tmp";
        final String REMOTE_DATADIR = "rrr";
        final Parameters parameters =
                parse("--incoming-dir", LOCAL_DATADIR, "--buffer-dir", LOCAL_TEMPDIR, "--outgoing-dir", REMOTE_DATADIR);
        assertEquals(createIncomingStore(LOCAL_DATADIR, null, parameters), getIncomingStore(parameters));
        assertEquals(LOCAL_TEMPDIR, parameters.getBufferDirectoryPath().getPath());
        assertEquals(createOutgoingStore(REMOTE_DATADIR, null, parameters), getOutgoingStore(parameters));
    }

    @Test
    public void testSetEverything() throws Exception
    {
        final String LOCAL_DATADIR = ".." + File.separator + "ldata";
        final String LOCAL_TEMPDIR = "l" + File.separator + "tmp";
        final String REMOTE_DATADIR = "rrr";
        final String REMOTE_HOST = "myremotehost";
        final int CHECK_INTERVAL = 22;
        final int QUIET_PERIOD = 33;
        final String REMOTE_INCOMING_HOST = "my-remote-incoming-host";
        final String EXTRA_COPY_DIR = "xxx";

        final Parameters parameters =
                parse("--incoming-dir", LOCAL_DATADIR, "--buffer-dir", LOCAL_TEMPDIR, "--outgoing-dir", REMOTE_DATADIR,
                        "--outgoing-host", REMOTE_HOST, "--check-interval", Integer.toString(CHECK_INTERVAL),
                        "--quiet-period", Integer.toString(QUIET_PERIOD), "--treat-incoming-as-remote",
                        "--incoming-host", REMOTE_INCOMING_HOST, "--extra-copy-dir", EXTRA_COPY_DIR,
                        "--rsync-overwrite");
        IFileStore incomingStoreExpected = createIncomingStore(LOCAL_DATADIR, REMOTE_INCOMING_HOST, parameters);
        IFileStore incomingStore = getIncomingStore(parameters);
        IFileStore outgoingStoreExpected = createOutgoingStore(REMOTE_DATADIR, REMOTE_HOST, parameters);
        IFileStore outgoingStore = getOutgoingStore(parameters);

        assertEquals(incomingStoreExpected, incomingStore);
        assertEquals(LOCAL_TEMPDIR, parameters.getBufferDirectoryPath().getPath());
        assertEquals(outgoingStoreExpected, outgoingStore);
        assertEquals(EXTRA_COPY_DIR, parameters.tryGetExtraCopyDir().getPath());
        assertEquals(1000 * CHECK_INTERVAL, parameters.getCheckIntervalMillis());
        assertEquals(1000 * QUIET_PERIOD, parameters.getQuietPeriodMillis());
        assertTrue(incomingStore.isRemote());
        assertTrue(parameters.isRsyncOverwrite());
    }

    private IFileStore getIncomingStore(Parameters parameters)
    {
        IFileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
        return parameters.getIncomingStore(factory);
    }

    private IFileStore getOutgoingStore(Parameters parameters)
    {
        IFileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
        return parameters.getOutgoingStore(factory);
    }

    private static IFileStore createIncomingStore(final String path, final String hostOrNull,
            IFileSysParameters parameters)
    {
        return createStore(path, hostOrNull, Parameters.INCOMING_KIND_DESC, parameters);
    }

    private static IFileStore createOutgoingStore(final String path, final String hostOrNull,
            IFileSysParameters parameters)
    {
        return createStore(path, hostOrNull, Parameters.OUTGOING_KIND_DESC, parameters);
    }

    private static IFileStore createStore(final String path, final String hostOrNull, String kind,
            IFileSysParameters parameters)
    {
        IFileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
        File file = new File(path);
        if (hostOrNull == null)
        {
            return FileStoreFactory.createLocal(file, kind, factory);
        } else
        {
            return FileStoreFactory.createRemoteHost(file, hostOrNull, kind, factory);
        }
    }
}
