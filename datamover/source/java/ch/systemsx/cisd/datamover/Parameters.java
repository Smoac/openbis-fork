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
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.LongOptionHandler;
import org.kohsuke.args4j.spi.Setter;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;

/**
 * The class to process the command line parameters.
 * 
 * @author Bernd Rinn
 */
public class Parameters implements ITimingParameters
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, Parameters.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, Parameters.class);

    /**
     * The name of the <code>rsync</code> executable to use for copy operations.
     */
    @Option(longName = "rsync-executable", metaVar = "EXEC", usage = "The rsync executable to use for "
            + "copy operations.")
    private String rsyncExecutable = null;

    /**
     * The name of the <code>ssh</code> executable to use for creating tunnels.
     */
    @Option(longName = "ssh-executable", metaVar = "EXEC", usage = "The ssh executable to use for creating tunnels.")
    private String sshExecutable = null;

    /**
     * The interval to wait beween to checks for activity (in milliseconds).
     */
    @Option(name = "c", longName = "check-interval", usage = "The interval to wait between two checks (in seconds) "
            + "[default: 120]", handler = MillisecondConversionOptionHandler.class)
    private long checkIntervalMillis = 120 * 1000;

    /**
     * The period to wait before a file or directory is considered "inactive" or "stalled" (in milliseconds). This
     * setting is used when deciding whether a copy operation of a file or directory is "stalled".
     */
    @Option(name = "i", longName = "inactivity-period", usage = "The period to wait before a file or directory is "
            + "considered \"inactive\" or \"stalled\" (in seconds) [default: 600].", handler = MillisecondConversionOptionHandler.class)
    private long inactivityPeriodMillis = 600 * 1000;

    /**
     * The period to wait before a file or directory is considered "quiet" (in milliseconds). This setting is used when
     * deciding whether a file or directory is ready to be moved to the remote side.
     */
    @Option(name = "q", longName = "quiet-period", usage = "The period that needs to pass before a path item is "
            + "considered quiet (in seconds) [default: 300].", handler = MillisecondConversionOptionHandler.class)
    private long quietPeriodMillis = 300 * 1000;

    /**
     * The time intervall to wait after a failure has occurred before the operation is retried (in milliseconds).
     */
    @Option(name = "f", longName = "failure-interval", usage = "The interval to wait after a failure has occurred "
            + "before retrying the operation (in seconds) [default: 120].", handler = MillisecondConversionOptionHandler.class)
    private long intervalToWaitAfterFailureMillis = 120 * 1000;

    /**
     * The number of times a failed operation is retried (note that this means that the total number that the operation
     * is tried is one more).
     */
    @Option(name = "m", longName = "max-retries", usage = "The number of retries of a failed operation before the "
            + "datamover gives up on it. [default: 2].")
    private int maximalNumberOfRetries = 2;

    /**
     * The (local) directory to monitor for new files and directories to move to the remote side.
     */
    @Option(name = "d", longName = "local-datadir", metaVar = "DIR", required = true, usage = "The local directory where "
            + "the data producer writes to.")
    private File localDataDirectory = null;

    /**
     * The directory to move files and directories to that have been quiet in the local data directory for long enough
     * and thus are considered to be ready to be moved to remote. Note that this directory needs to be on the same file
     * system than {@link #localDataDirectory}.
     */
    @Option(name = "t", longName = "local-tempdir", metaVar = "DIR", required = true, usage = "The local directory to "
            + "store the paths to be transfered temporarily.")
    private File localTemporaryDirectory = null;

    /**
     * The directory on the remote side to move the local files to once they are quiet.
     */
    @Option(name = "r", longName = "remotedir", metaVar = "DIR", required = true, usage = "The remote directory to "
            + "move the data to.")
    private File remoteDataDirectory = null;

    /**
     * The remote host to copy the data to (only with rsync, will use an ssh tunnel).
     */
    @Option(name = "h", longName = "remotehost", metaVar = "HOST", usage = "The remote host to move the data to (only "
            + "with rsync).")
    private String remoteHost = null;

    /**
     * The regular expression to use for cleansing on the local path before moving it to remote.
     */
    @Option(longName = "cleansing-regex", usage = "The regular expression to use for cleansing before "
            + "moving to remote.")
    private Pattern cleansingRegex = null;

    /**
     * The command line parser.
     */
    private final CmdLineParser parser = new CmdLineParser(this);

    @Option(longName = "help", skipForExample = true, usage = "Prints out a description of the options.")
    void printHelp(boolean exit)
    {
        parser.printHelp("datamover", "<required options> [option [...]]", "", ExampleMode.ALL);
        if (exit)
        {
            System.exit(0);
        }
    }

    @Option(longName = "version", skipForExample = true, usage = "Prints out the version information.")
    void printVersion(boolean exit)
    {
        System.err.println("datamover version " + BuildAndEnvironmentInfo.INSTANCE.getBuildNumber());
        if (exit)
        {
            System.exit(0);
        }
    }

    @Option(longName = "test-notify", skipForExample = true, usage = "Tests the notify log (i.e. that an email is "
            + "sent out).")
    void sendTestNotification(boolean exit)
    {
        notificationLog.error("This is a test notification given due to specifying the --test-notify option.");
        if (exit)
        {
            System.exit(0);
        }
    }

    /**
     * A class which converts <code>long</code> options given in seconds to milli-seconds.
     */
    public static class MillisecondConversionOptionHandler extends LongOptionHandler
    {
        public MillisecondConversionOptionHandler(Option option, Setter<? super Long> setter)
        {
            super(option, setter);
        }

        @Override
        public void set(long value) throws CmdLineException
        {
            setter.addValue(value * 1000);
        }

    }

    Parameters(String[] args)
    {
        this(args, false, false);
    }

    Parameters(String[] args, boolean unitTest, boolean suppressMissingMandatoryOptions)
    {
        try
        {
            parser.parseArgument(args);
        } catch (CmdLineException ex)
        {
            if (unitTest == false)
            {
                System.err.println(ex.getMessage());
                printHelp(false);
                System.exit(1);
            } else
            // Suppress exception due to missing mandatory options.
            {
                if (false == suppressMissingMandatoryOptions
                        || ex.getMessage().indexOf("Required option(s) are missing") == -1)
                {
                    throw new UserFailureException("Error parsing command line: " + ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * @return The name of the <code>rsync</code> executable to use for copy operations.
     */
    public String getRsyncExecutable()
    {
        return rsyncExecutable;
    }

    /**
     * @return The name of the <code>ssh</code> executable to use for creating tunnels.
     */
    public String getSshExecutable()
    {
        return sshExecutable;
    }

    /**
     * @return The interval to wait beween to checks for activity (in milliseconds).
     */
    public long getCheckIntervalMillis()
    {
        return checkIntervalMillis;
    }

    /**
     * @return The period to wait before a file or directory is considered "inactive" (in milliseconds). This setting is
     *         used when deciding whether a copy operation of a file or directory is "stalled".
     */
    public long getInactivityPeriodMillis()
    {
        return inactivityPeriodMillis;
    }

    /**
     * @return The period to wait before a file or directory is considered "quiet" (in milliseconds). This setting is
     *         used when deciding whether a file or directory is ready to be moved to the remote side.
     */
    public long getQuietPeriodMillis()
    {
        return quietPeriodMillis;
    }

    /**
     * @return The time interval to wait after a failure has occurred before the operation is retried.
     */
    public long getIntervalToWaitAfterFailure()
    {
        return intervalToWaitAfterFailureMillis;
    }

    /**
     * @return The number of times a failed operation is retried (note that this means that the total number that the
     *         operation is tried is one more).
     */
    public int getMaximalNumberOfRetries()
    {
        return maximalNumberOfRetries;
    }

    /**
     * @return The (local) directory to monitor for new files and directories to move to the remote side.
     */
    public File getLocalDataDirectory()
    {
        return localDataDirectory;
    }

    /**
     * @return The directory to move files and directories to that have been quiet in the local data directory for long
     *         enough and thus are considered to be ready to be moved to remote. Note that this directory needs to be on
     *         the same file system than {@link #getLocalDataDirectory}.
     */
    public File getLocalTemporaryDirectory()
    {
        return localTemporaryDirectory;
    }

    /**
     * @return The directory on the remote side to move the local files to once they are quiet.
     */
    public File getRemoteDataDirectory()
    {
        return remoteDataDirectory;
    }

    /**
     * @return The remote host to copy the data to (only with rsync, will use an ssh tunnel).
     */
    public String getRemoteHost()
    {
        return remoteHost;
    }

    /**
     * @return The regular expression to use for cleansing on the local path before moving it to remote or
     *         <code>null</code>, if no regular expression for cleansing has been provided.
     */
    public Pattern getCleansingRegex()
    {
        return cleansingRegex;
    }

    /**
     * Logs the current parameters to the {@link LogCategory#OPERATION} log.
     */
    public void log()
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Local data directory: '%s'.", getLocalDataDirectory().getAbsolutePath()));
            operationLog.info(String.format("Local temporary directory: '%s'.", getLocalTemporaryDirectory()
                    .getAbsolutePath()));
            operationLog.info(String.format("Remote directory: '%s'.", getRemoteDataDirectory().getAbsolutePath()));
            if (null != getRemoteHost())
            {
                operationLog.info(String.format("Remote host: '%s'.", getRemoteHost()));
            }
            operationLog.info(String.format("Check intervall: %d s.", getCheckIntervalMillis() / 1000));
            operationLog.info(String.format("Quiet period: %d s.", getQuietPeriodMillis() / 1000));
            operationLog.info(String.format("Inactivity (stall) period: %d s.", getInactivityPeriodMillis() / 1000));
            operationLog.info(String.format("Intervall to wait after failure: %d s.",
                    getIntervalToWaitAfterFailure() / 1000));
            operationLog.info(String.format("Maximum number of retries: %d.", getMaximalNumberOfRetries()));
            if (getCleansingRegex() != null)
            {
                operationLog.info(String.format("Regular expression used for cleansing before moving: '%s'",
                        getCleansingRegex().pattern()));
            }
        }
    }

}
