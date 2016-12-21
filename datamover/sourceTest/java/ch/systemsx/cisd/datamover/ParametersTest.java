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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.SystemExit;
import ch.systemsx.cisd.datamover.Parameters.HostAwareFileWithHighwaterMarkHandler;
import ch.systemsx.cisd.datamover.filesystem.FileStoreFactory;
import ch.systemsx.cisd.datamover.filesystem.FileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.AbstractFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.store.FileStoreRemote;
import ch.systemsx.cisd.datamover.intf.IFileSysParameters;

/**
 * Test cases for the {@link Parameters} class.
 * 
 * @author Bernd Rinn
 */
@Friend(toClasses =
{ SystemExit.class, Parameters.class, FileStoreFactory.class })
public final class ParametersTest extends AbstractFileSystemTestCase
{
    private final Parameters parse(final String... args)
    {
        return new Parameters(args);
    }

    @BeforeClass
    public void init()
    {
        LogInitializer.init();
        SystemExit.setThrowException(true);
    }

    @AfterClass
    public void finish()
    {
        SystemExit.setThrowException(false);
    }

    @Test
    public void testSetRsyncExecutableLong() throws Exception
    {
        final String rsyncExec = "/usr/local/bin/rsync";
        final Parameters parameters = parse("--rsync-executable", rsyncExec);
        assertEquals(rsyncExec, parameters.getRsyncExecutable());
    }

    @Test
    public void testSetSshExecutableLong() throws Exception
    {
        final String sshExec = "/usr/local/bin/ssh";
        final Parameters parameters = parse("--ssh-executable", sshExec);
        assertEquals(sshExec, parameters.getSshExecutable());
    }

    @Test
    public void testSetCleansingRegexLong() throws Exception
    {
        final String cleansingRegex = "[0-9]+";
        final Parameters parameters = parse("--cleansing-regex", cleansingRegex);
        assertEquals(cleansingRegex, parameters.tryGetCleansingRegex().pattern());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testSetInvalidCleansingRegex() throws Exception
    {
        final String cleansingRegex = "[0-9}+";
        parse("--cleansing-regex", cleansingRegex);
    }

    @Test
    public void testIncomingDirectory() throws Exception
    {
        final String localDataDir = ".." + File.separator + "test_it_data";
        final Parameters parameters = parse("--" + PropertyNames.INCOMING_TARGET, localDataDir);
        assertEquals(createIncomingStore(localDataDir, null, parameters),
                getIncomingStore(parameters));
    }

    @DataProvider(name = "directoryWithHighwaterMark")
    public final Object[][] getDirectoryWithHighwaterMark()
    {
        return new Object[][]
        {
                { PropertyNames.BUFFER_DIR, 100 },
                { PropertyNames.OUTGOING_TARGET, 200 } };
    }

    @Test(dataProvider = "directoryWithHighwaterMark")
    public final void testDirectoryWithHighwaterMark(final String optionName,
            final long highwaterMark) throws Exception
    {
        final String localTempDir = "test_it_tmp";
        // Without highwater mark
        Parameters parameters = parse("--" + optionName, localTempDir);
        HostAwareFileWithHighwaterMark hostAwareFileWithHighwaterMark =
                getFileWithHighwaterMark(optionName, parameters);
        assertEquals(localTempDir, hostAwareFileWithHighwaterMark.getLocalFile().getPath());
        assertEquals(-1, hostAwareFileWithHighwaterMark.getHighwaterMark());
        // With a highwater mark value
        parameters =
                parse("--" + optionName, localTempDir
                        + HostAwareFileWithHighwaterMarkHandler.DIRECTORY_HIGHWATERMARK_SEP
                        + highwaterMark);
        hostAwareFileWithHighwaterMark = getFileWithHighwaterMark(optionName, parameters);
        assertEquals(localTempDir, hostAwareFileWithHighwaterMark.getLocalFile().getPath());
        assertEquals(highwaterMark, hostAwareFileWithHighwaterMark.getHighwaterMark());
    }

    public final void testTarget() throws Exception
    {
        Parameters parameters = parse("--" + PropertyNames.OUTGOING_TARGET, "dir");
        HostAwareFileWithHighwaterMark hostAwareFileWithHighwaterMark =
                getFileWithHighwaterMark(PropertyNames.OUTGOING_TARGET, parameters);
        assertEquals("dir", hostAwareFileWithHighwaterMark.getLocalFile().getPath());
        assertNull(hostAwareFileWithHighwaterMark.tryGetHost());
        assertNull(hostAwareFileWithHighwaterMark.tryGetRsyncModule());

        parameters = parse("--" + PropertyNames.OUTGOING_TARGET, "host:dir");
        hostAwareFileWithHighwaterMark =
                getFileWithHighwaterMark(PropertyNames.OUTGOING_TARGET, parameters);
        assertEquals("dir", hostAwareFileWithHighwaterMark.getLocalFile().getPath());
        assertEquals("host", hostAwareFileWithHighwaterMark.tryGetHost());
        assertNull(hostAwareFileWithHighwaterMark.tryGetRsyncModule());

        parameters = parse("--" + PropertyNames.OUTGOING_TARGET, "host:mod:dir");
        hostAwareFileWithHighwaterMark =
                getFileWithHighwaterMark(PropertyNames.OUTGOING_TARGET, parameters);
        assertEquals("dir", hostAwareFileWithHighwaterMark.getLocalFile().getPath());
        assertEquals("host", hostAwareFileWithHighwaterMark.tryGetHost());
        assertEquals("mod", hostAwareFileWithHighwaterMark.tryGetRsyncModule());

        parameters = parse("--" + PropertyNames.OUTGOING_TARGET, "host:mod:dir>42");
        hostAwareFileWithHighwaterMark =
                getFileWithHighwaterMark(PropertyNames.OUTGOING_TARGET, parameters);
        assertEquals("dir", hostAwareFileWithHighwaterMark.getLocalFile().getPath());
        assertEquals("host", hostAwareFileWithHighwaterMark.tryGetHost());
        assertEquals("mod", hostAwareFileWithHighwaterMark.tryGetRsyncModule());
        assertEquals(42L, hostAwareFileWithHighwaterMark.getHighwaterMark());
    }

    private final HostAwareFileWithHighwaterMark getFileWithHighwaterMark(final String optionName,
            final Parameters parameters)
    {
        if (optionName.equals(PropertyNames.BUFFER_DIR))
        {
            return parameters.getBufferDirectoryPath();
        } else if (optionName.equals(PropertyNames.OUTGOING_TARGET))
        {
            return parameters.outgoingTarget;
        }
        throw new AssertionError();
    }

    @Test
    public void testDefaultCheckInterval() throws Exception
    {
        final Parameters parameters = parse();
        assertEquals(1000 * Parameters.DEFAULT_CHECK_INTERVAL, parameters.getCheckIntervalMillis());
    }

    @Test
    public final void testDataCompletedScript() throws Exception
    {
        Parameters parameters = parse();
        assertNull(parameters.getDataCompletedScript());
        assertEquals(
                Parameters.DEFAULT_DATA_COMPLETED_SCRIPT_TIMEOUT * DateUtils.MILLIS_PER_SECOND,
                parameters.getDataCompletedScriptTimeout());
        final String scriptName = "run.sh";
        try
        {
            parameters = parse("--" + PropertyNames.DATA_COMPLETED_SCRIPT, scriptName);
        } catch (final ConfigurationFailureException ex)
        {
            assertEquals(
                    String.format(Parameters.DATA_COMPLETED_SCRIPT_NOT_FOUND_TEMPLATE, scriptName),
                    ex.getMessage());
        }
        final File scriptFile = new File(workingDirectory, scriptName);
        FileUtils.touch(scriptFile);
        parameters = parse("--" + PropertyNames.DATA_COMPLETED_SCRIPT, scriptFile.getPath());
        assertEquals(scriptFile, parameters.getDataCompletedScript());
    }

    @Test
    public void testDefaultCheckIntervalInternal() throws Exception
    {
        final Parameters parameters = parse();
        assertEquals(1000 * Parameters.DEFAULT_CHECK_INTERVAL_INTERNAL,
                parameters.getCheckIntervalInternalMillis());
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
        assertEquals(1000 * Parameters.DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURES,
                parameters.getIntervalToWaitAfterFailure());
    }

    @Test
    public void testDefaultMaximalNumberOfRetries() throws Exception
    {
        final Parameters parameters = parse();
        assertEquals(Parameters.DEFAULT_MAXIMAL_NUMBER_OF_RETRIES,
                parameters.getMaximalNumberOfRetries());
    }

    @Test
    public void testDefaultInactivityPeriod() throws Exception
    {
        final Parameters parameters = parse();
        assertEquals(1000 * Parameters.DEFAULT_INACTIVITY_PERIOD,
                parameters.getInactivityPeriodMillis());
    }

    @Test
    public void testSetCheckIntervalLong() throws Exception
    {
        final int checkInterval = 5;
        final Parameters parameters = parse("--check-interval", Integer.toString(checkInterval));
        assertEquals(1000 * checkInterval, parameters.getCheckIntervalMillis());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testSetInvalidCheckInterval() throws Exception
    {
        parse("--check-interval", "5x");
    }

    @Test
    public void testSetCheckIntervalShort() throws Exception
    {
        final int checkInterval = 11;
        final Parameters parameters = parse("-c", Integer.toString(checkInterval));
        assertEquals(1000 * checkInterval, parameters.getCheckIntervalMillis());
    }

    @Test
    public void testSetCheckIntervalInternalLong() throws Exception
    {
        final int checkInterval = 1;
        final Parameters parameters =
                parse("--check-interval-internal", Integer.toString(checkInterval));
        assertEquals(1000 * checkInterval, parameters.getCheckIntervalInternalMillis());
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
        final int quietPeriod = 6;
        final Parameters parameters = parse("--quiet-period", Integer.toString(quietPeriod));
        assertEquals(1000 * quietPeriod, parameters.getQuietPeriodMillis());
    }

    @Test
    public void testSetQuietPeriodShort() throws Exception
    {
        final int quietPeriod = 17;
        final Parameters parameters = parse("-q", Integer.toString(quietPeriod));
        assertEquals(1000 * quietPeriod, parameters.getQuietPeriodMillis());
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
        final String localDataDir = ".." + File.separator + "ldata";
        final String localTempDir = "l" + File.separator + "tmp";
        final String remoteDataDir = "rrr";
        final Parameters parameters =
                parse("--" + PropertyNames.INCOMING_TARGET, localDataDir, "--"
                        + PropertyNames.BUFFER_DIR, localTempDir, "--"
                        + PropertyNames.OUTGOING_TARGET, remoteDataDir);
        assertEquals(createIncomingStore(localDataDir, null, parameters),
                getIncomingStore(parameters));
        assertEquals(localTempDir, parameters.getBufferDirectoryPath().getLocalFile().getPath());
        assertEquals(createOutgoingStore(remoteDataDir, null, parameters),
                getOutgoingStore(parameters));
    }

    @Test
    public void testSetLocalBufferDir() throws IOException
    {
        final Parameters params = parse("--buffer-dir", "data/buffer");
        assertNull(params.getBufferDirectoryPath().tryGetHost());
        assertNull(params.getBufferDirectoryPath().tryGetRsyncModule());
        assertEquals(new File("data/buffer").getCanonicalPath(), params.getBufferDirectoryPath()
                .getCanonicalPath());
    }

    @Test(expectedExceptions = ConfigurationFailureException.class)
    public void testSetRemoteBufferDir() throws IOException
    {
        parse("--buffer-dir", "myserver:data/buffer");
    }

    @Test
    public void testSetLocalBufferDirWindows() throws IOException
    {
        Parameters params = parse("--buffer-dir", "C:\\data\\buffer");
        assertEquals(new File("C:\\data\\buffer"), params.getBufferDirectoryPath().getLocalFile());
        params = parse("--buffer-dir", "c:\\data\\buffer");
        assertEquals(new File("c:\\data\\buffer"), params.getBufferDirectoryPath().getLocalFile());
        params = parse("--buffer-dir", "\\data\\buffer");
        assertEquals(new File("\\data\\buffer"), params.getBufferDirectoryPath().getLocalFile());
        params = parse("--buffer-dir", "c:/data/buffer");
        assertEquals(new File("c:/data/buffer"), params.getBufferDirectoryPath().getLocalFile());
    }

    @Test
    public void testSetEverything() throws Exception
    {
        final String localDataDir = ".." + File.separator + "ldata";
        final String localTempDir = "l" + File.separator + "tmp";
        final String remoteDataDir = "rrr";
        final String remoteHost = "myremotehost";
        final int checkIntervall = 22;
        final int quietPeriod = 33;
        final String remoteIncomingHost = "my-remote-incoming-host";
        final String extraCopyDir = "xxx";

        final Parameters parameters =
                parse("--" + PropertyNames.INCOMING_TARGET,
                        remoteIncomingHost + ":" + localDataDir, "--" + PropertyNames.BUFFER_DIR,
                        localTempDir, "--" + PropertyNames.OUTGOING_TARGET, remoteHost + ":"
                                + remoteDataDir, "--check-interval",
                        Integer.toString(checkIntervall), "--quiet-period",
                        Integer.toString(quietPeriod), "--treat-incoming-as-remote",
                        "--extra-copy-dir", extraCopyDir, "--rsync-overwrite");
        final IFileStore incomingStoreExpected =
                createIncomingStore(localDataDir, remoteIncomingHost, parameters);
        final IFileStore incomingStore = getIncomingStore(parameters);
        final IFileStore outgoingStoreExpected =
                createOutgoingStore(remoteDataDir, remoteHost, parameters);
        final IFileStore outgoingStore = getOutgoingStore(parameters);

        assertEquals(incomingStoreExpected, incomingStore);
        assertEquals(localTempDir, parameters.getBufferDirectoryPath().getLocalFile().getPath());
        assertEquals(outgoingStoreExpected, outgoingStore);
        assertEquals(extraCopyDir, parameters.tryGetExtraCopyDir().getPath());
        assertEquals(1000 * checkIntervall, parameters.getCheckIntervalMillis());
        assertEquals(1000 * quietPeriod, parameters.getQuietPeriodMillis());
        assertTrue(parameters.isRsyncOverwrite());
    }

    private IFileStore getIncomingStore(final Parameters parameters)
    {
        final IFileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
        return parameters.getIncomingStore(factory);
    }

    private IFileStore getOutgoingStore(final Parameters parameters)
    {
        final IFileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
        return parameters.getOutgoingStore(factory);
    }

    private static IFileStore createIncomingStore(final String path, final String hostOrNull,
            final IFileSysParameters parameters)
    {
        return createStore(path, hostOrNull, Parameters.INCOMING_KIND_DESC, parameters);
    }

    private static IFileStore createOutgoingStore(final String path, final String hostOrNull,
            final IFileSysParameters parameters)
    {
        return createStore(path, hostOrNull, Parameters.OUTGOING_KIND_DESC, parameters);
    }

    private static IFileStore createStore(final String path, final String hostOrNull,
            final String kind, final IFileSysParameters parameters)
    {
        final IFileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
        if (hostOrNull == null)
        {
            return FileStoreFactory.createLocal(path, kind, factory, false,
                    AbstractFileStore.DEFAULT_REMOTE_CONNECTION_TIMEOUT_MILLIS);
        } else
        {
            return FileStoreFactory.createRemoteHost(path, hostOrNull, null, kind, factory,
                    AbstractFileStore.DEFAULT_REMOTE_CONNECTION_TIMEOUT_MILLIS,
                    FileStoreRemote.DEFAULT_REMOTE_OPERATION_TIMEOUT_MILLIS);
        }
    }
}
