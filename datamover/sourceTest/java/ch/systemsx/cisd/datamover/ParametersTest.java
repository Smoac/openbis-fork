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
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for the {@link Parameters} class.
 * 
 * @author Bernd Rinn
 */
public class ParametersTest
{

    private Parameters parse(String... args)
    {
        return parse(true, args);
    }

    private Parameters parse(boolean suppressMissingMandatoryOptions, String... args)
    {
        return new Parameters(args, true, suppressMissingMandatoryOptions);
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
    public void testSetCleansingRegexLong() throws Exception
    {
        final String CLEANSING_REGEX = "[0-9]+";
        final Parameters parameters = parse("--cleansing-regex", CLEANSING_REGEX);
        assertEquals(CLEANSING_REGEX, parameters.getCleansingRegex().pattern());
    }

    @Test(expectedExceptions = UserFailureException.class)
    public void testSetInvalidCleansingRegex() throws Exception
    {
        final String CLEANSING_REGEX = "[0-9}+";
        parse("--cleansing-regex", CLEANSING_REGEX);
    }

    @Test
    public void testSetLocalDataDirLong() throws Exception
    {
        final String LOCAL_DATADIR = ".." + File.separator + "test_it_data";
        final Parameters parameters = parse("--local-datadir", LOCAL_DATADIR);
        assertEquals(LOCAL_DATADIR, parameters.getLocalDataDirectory().getPath());
    }

    @Test
    public void testSetLocalDataDirShort() throws Exception
    {
        final String LOCAL_DATADIR = ".." + File.separator + "test_it_data2";
        final Parameters parameters = parse("-d", LOCAL_DATADIR);
        assertEquals(LOCAL_DATADIR, parameters.getLocalDataDirectory().getPath());
    }

    @Test
    public void testSetLocalTempDirLong() throws Exception
    {
        final String LOCAL_TEMPDIR = "test_it_tmp";
        final Parameters parameters = parse("--local-tempdir", LOCAL_TEMPDIR);
        assertEquals(LOCAL_TEMPDIR, parameters.getLocalTemporaryDirectory().getPath());
    }

    @Test
    public void testSetLocalTempDirShort() throws Exception
    {
        final String LOCAL_TEMPDIR = "test_it_tmp3";
        final Parameters parameters = parse("-t", LOCAL_TEMPDIR);
        assertEquals(LOCAL_TEMPDIR, parameters.getLocalTemporaryDirectory().getPath());
    }

    @Test
    public void testSetRemoteDirLong() throws Exception
    {
        final String REMOTE_DATADIR = "test_it_remote";
        final Parameters parameters = parse("--remotedir", REMOTE_DATADIR);
        assertEquals(REMOTE_DATADIR, parameters.getRemoteDataDirectory().getPath());
    }

    @Test
    public void testSetRemoteDirShort() throws Exception
    {
        final String REMOTE_DATADIR = "test_it_remote4";
        final Parameters parameters = parse("-r", REMOTE_DATADIR);
        assertEquals(REMOTE_DATADIR, parameters.getRemoteDataDirectory().getPath());
    }

    @Test
    public void testSetRemoteHostLong() throws Exception
    {
        final String REMOTE_HOST = "test_it_remote";
        final Parameters parameters = parse("--remotehost", REMOTE_HOST);
        assertEquals(REMOTE_HOST, parameters.getRemoteHost());
    }

    @Test
    public void testSetRemoteHostShort() throws Exception
    {
        final String REMOTE_HOST = "test_it_remote4";
        final Parameters parameters = parse("-h", REMOTE_HOST);
        assertEquals(REMOTE_HOST, parameters.getRemoteHost());
    }

    @Test
    public void testSetCheckIntervalLong() throws Exception
    {
        final int CHECK_INTERVAL = 5;
        final Parameters parameters = parse("--check-interval", Integer.toString(CHECK_INTERVAL));
        assertEquals(1000 * CHECK_INTERVAL, parameters.getCheckIntervalMillis());
    }

    @Test(expectedExceptions = UserFailureException.class)
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

    @Test(expectedExceptions = UserFailureException.class)
    public void testMissingMandatoryOptions() throws Exception
    {
        parse(false);
    }

    @Test
    public void testSetMandatoryOptions() throws Exception
    {
        final String LOCAL_DATADIR = ".." + File.separator + "ldata";
        final String LOCAL_TEMPDIR = "l" + File.separator + "tmp";
        final String REMOTE_DATADIR = "rrr";
        final Parameters parameters =
                parse(false, "--local-datadir", LOCAL_DATADIR, "--local-tempdir", LOCAL_TEMPDIR, "--remotedir",
                        REMOTE_DATADIR);
        assertEquals(LOCAL_DATADIR, parameters.getLocalDataDirectory().getPath());
        assertEquals(LOCAL_TEMPDIR, parameters.getLocalTemporaryDirectory().getPath());
        assertEquals(REMOTE_DATADIR, parameters.getRemoteDataDirectory().getPath());
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
        final Parameters parameters =
                parse(false, "--local-datadir", LOCAL_DATADIR, "--local-tempdir", LOCAL_TEMPDIR, "--remotedir",
                        REMOTE_DATADIR, "--remotehost", REMOTE_HOST, "--check-interval", Integer
                                .toString(CHECK_INTERVAL), "--quiet-period", Integer.toString(QUIET_PERIOD));
        assertEquals(LOCAL_DATADIR, parameters.getLocalDataDirectory().getPath());
        assertEquals(LOCAL_TEMPDIR, parameters.getLocalTemporaryDirectory().getPath());
        assertEquals(REMOTE_DATADIR, parameters.getRemoteDataDirectory().getPath());
        assertEquals(REMOTE_HOST, parameters.getRemoteHost());
        assertEquals(1000 * CHECK_INTERVAL, parameters.getCheckIntervalMillis());
        assertEquals(1000 * QUIET_PERIOD, parameters.getQuietPeriodMillis());
    }

}
