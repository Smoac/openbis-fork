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
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.LongOptionHandler;
import org.kohsuke.args4j.spi.Setter;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.SystemExit;

/**
 * The class to process the command line parameters.
 * 
 * @author Bernd Rinn
 */
public class Parameters implements ITimingParameters
{

    private static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";

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
     * Default interval to wait between two checks for activity (in seconds)
     */
    static final int DEFAULT_CHECK_INTERVAL = 120;

    /**
     * The interval to wait between two checks for activity (in milliseconds).
     */
    @Option(name = "c", longName = "check-interval", usage = "The interval to wait between two checks (in seconds) "
            + "[default: 120]", handler = MillisecondConversionOptionHandler.class)
    private long checkIntervalMillis;

    /**
     * Default period to wait before a file or directory is considered "inactive" or "stalled" (in seconds).
     */
    static final int DEFAULT_INACTIVITY_PERIOD = 600;

    /**
     * The period to wait before a file or directory is considered "inactive" or "stalled" (in milliseconds). This
     * setting is used when deciding whether a copy operation of a file or directory is "stalled".
     */
    @Option(name = "i", longName = "inactivity-period", usage = "The period to wait before a file or directory is "
            + "considered \"inactive\" or \"stalled\" (in seconds) [default: 600].", handler = MillisecondConversionOptionHandler.class)
    private long inactivityPeriodMillis;

    /**
     * Default period to wait before a file or directory is considered "quiet" (in seconds).
     */
    static final int DEFAULT_QUIET_PERIOD = 300;

    /**
     * The period to wait before a file or directory is considered "quiet" (in milliseconds). This setting is used when
     * deciding whether a file or directory is ready to be moved to the remote side.
     */
    @Option(name = "q", longName = "quiet-period", usage = "The period that needs to pass before a path item is "
            + "considered quiet (in seconds) [default: 300].", handler = MillisecondConversionOptionHandler.class)
    private long quietPeriodMillis;

    /**
     * Default period to wait before a file or directory is considered "quiet" (in seconds).
     */
    static final int DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURES = 1800;

    /**
     * The time interval to wait after a failure has occurred before the operation is retried (in milliseconds).
     */
    @Option(name = "f", longName = "failure-interval", usage = "The interval to wait after a failure has occurred "
            + "before retrying the operation (in seconds) [default: 1800].", handler = MillisecondConversionOptionHandler.class)
    private long intervalToWaitAfterFailureMillis;

    /**
     * Default number of retries after a failure has occurred.
     */
    static final int DEFAULT_MAXIMAL_NUMBER_OF_RETRIES = 10;

    /**
     * The number of times a failed operation is retried (note that this means that the total number that the operation
     * is tried is one more).
     */
    @Option(name = "m", longName = "max-retries", usage = "The number of retries of a failed operation before the "
            + "datamover gives up on it. [default: 10].")
    private int maximalNumberOfRetries;

    /**
     * The directory to monitor for new files and directories to move to outgoing.
     */
    @Option(longName = "incoming-dir", metaVar = "DIR", usage = "The local directory where "
            + "the data producer writes to.")
    private File incomingDirectory = null;

    /**
     * The directory to move files and directories to that have been quiet in the incoming directory for long enough and
     * thus are considered to be ready to be moved to outgoing. Note that this directory needs to be on the same file
     * system than {@link #incomingDirectory}.
     */
    @Option(longName = "buffer-dir", metaVar = "DIR", usage = "The local directory to "
            + "store the paths to be transfered temporarily.")
    private File bufferDirectory = null;

    /**
     * The directory to move files and directories to that have been quiet in the incoming directory for long enough and
     * that need manual intervention. Note that this directory needs to be on the same file system than
     * {@link #incomingDirectory}.
     */
    @Option(longName = "manual-intervention-dir", metaVar = "DIR", usage = "The local directory to "
            + "store paths that need manual intervention.")
    private File manualInterventionDirectory = null;

    /**
     * The directory on the remote side to move the paths to from the buffer directory.
     */
    @Option(longName = "outgoing-dir", metaVar = "DIR", usage = "The remote directory to move the data to.")
    private File outgoingDirectory = null;

    /**
     * The remote host to copy the data to (only with rsync, will use an ssh tunnel).
     */
    @Option(longName = "outgoing-host", metaVar = "HOST", usage = "The remote host to move the data to (only "
            + "with rsync).")
    private String outgoingHost = null;

    /**
     * The regular expression to use for cleansing on the incoming directory before moving it to the buffer.
     */
    @Option(longName = "cleansing-regex", usage = "The regular expression to use for cleansing before "
            + "moving to outgoing.")
    private Pattern cleansingRegex = null;

    /**
     * The regular expression to use for deciding whether a path in the incoming directory needs manual intervetion.
     */
    @Option(longName = "manual-intervention-regex", usage = "The regular expression to use for deciding whether an "
            + "incoming paths needs manual intervention. ")
    private Pattern manualInterventionRegex = null;

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
        this(args, SystemExit.SYSTEM_EXIT);
    }

    Parameters(String[] args, IExitHandler systemExitHandler)
    {
        initParametersFromProperties();
        try
        {
            parser.parseArgument(args);
            if (incomingDirectory == null)
            {
                throw new ConfigurationFailureException("No 'incoming-dir' defined.");
            }
            if (bufferDirectory == null)
            {
                throw new ConfigurationFailureException("No 'buffer-dir' defined.");
            }
            if (outgoingDirectory == null)
            {
                throw new ConfigurationFailureException("No 'outgoing-dir' defined.");
            }
            if (manualInterventionDirectory == null && manualInterventionRegex != null)
            {
                throw new ConfigurationFailureException(
                        "No 'manual-intervention-dir' defined, but 'manual-intervention-regex'.");
            }
        } catch (Exception ex)
        {
            outputException(ex);
            systemExitHandler.exit(1);
            // Only reached in unit tests.
            throw new AssertionError(ex.getMessage());
        }
    }

    private void outputException(Exception ex)
    {
        if (ex instanceof HighLevelException || ex instanceof CmdLineException)
        {
            System.err.println(ex.getMessage());
        } else
        {
            System.err.println("An exception occurred.");
            ex.printStackTrace();
        }
        if (ex instanceof CmdLineException)
        {
            printHelp(false);
        }
    }

    private void initParametersFromProperties()
    {
        final Properties serviceProperties = loadServiceProperties();
        rsyncExecutable = serviceProperties.getProperty("rsync-executable");
        sshExecutable = serviceProperties.getProperty("ssh-executable");
        checkIntervalMillis =
                Integer.parseInt(serviceProperties.getProperty("check-interval", Integer
                        .toString(DEFAULT_CHECK_INTERVAL))) * 1000;
        inactivityPeriodMillis =
                Integer.parseInt(serviceProperties.getProperty("inactivity-period", Integer
                        .toString(DEFAULT_INACTIVITY_PERIOD))) * 1000;
        quietPeriodMillis =
                Integer.parseInt(serviceProperties.getProperty("quiet-period", Integer.toString(DEFAULT_QUIET_PERIOD))) * 1000;
        intervalToWaitAfterFailureMillis =
                Integer.parseInt(serviceProperties.getProperty("failure-interval", Integer
                        .toString(DEFAULT_INTERVAL_TO_WAIT_AFTER_FAILURES))) * 1000;
        maximalNumberOfRetries =
                Integer.parseInt(serviceProperties.getProperty("max-retries", Integer
                        .toString(DEFAULT_MAXIMAL_NUMBER_OF_RETRIES)));
        if (serviceProperties.getProperty("incoming-dir") != null)
        {
            incomingDirectory = new File(serviceProperties.getProperty("incoming-dir"));
        }
        if (serviceProperties.getProperty("buffer-dir") != null)
        {
            bufferDirectory = new File(serviceProperties.getProperty("buffer-dir"));
        }
        if (serviceProperties.getProperty("manual-intervention-dir") != null)
        {
            manualInterventionDirectory = new File(serviceProperties.getProperty("manual-intervention-dir"));
        }
        if (serviceProperties.getProperty("outgoing-dir") != null)
        {
            outgoingDirectory = new File(serviceProperties.getProperty("outgoing-dir"));
        }
        outgoingHost = serviceProperties.getProperty("outgoing-host");
        if (serviceProperties.getProperty("cleansing-regex") != null)
        {
            cleansingRegex = Pattern.compile(serviceProperties.getProperty("cleansing-regex"));
        }
        if (serviceProperties.getProperty("manual-intervention-regex") != null)
        {
            manualInterventionRegex = Pattern.compile(serviceProperties.getProperty("manual-intervention-regex"));
        }
    }

    /**
     * Returns the service property.
     * 
     * @throws ConfigurationFailureException If an exception occurs when loading the service properties.
     */
    private Properties loadServiceProperties()
    {
        final Properties properties = new Properties();
        try
        {
            final InputStream is = new FileInputStream(SERVICE_PROPERTIES_FILE);
            try
            {
                properties.load(is);
                return properties;
            } finally
            {
                IOUtils.closeQuietly(is);
            }
        } catch (Exception ex)
        {
            final String msg = "Could not load the service properties from resource '" + SERVICE_PROPERTIES_FILE + "'.";
            operationLog.warn(msg, ex);
            throw new ConfigurationFailureException(msg, ex);
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
     * @return The (local) directory to monitor for new files and directories to move to outgoing.
     */
    public File getIncomingDirectory()
    {
        return incomingDirectory;
    }

    /**
     * @return The directory to move files and directories to that have been quiet in the local data directory for long
     *         enough and thus are considered to be ready to be moved to remote. Note that this directory needs to be on
     *         the same file system than {@link #getIncomingDirectory}.
     */
    public File getBufferDirectory()
    {
        return bufferDirectory;
    }

    /**
     * @return The directory to move files and directories to that have been quiet in the local data directory for long
     *         enough and that need manual intervention. Note that this directory needs to be on the same file system
     *         than {@link #getIncomingDirectory}.
     */
    public File getManualInterventionDirectory()
    {
        return manualInterventionDirectory;
    }

    /**
     * @return The directory on the remote side to move the local files to once they are quiet.
     */
    public File getOutgoingDirectory()
    {
        return outgoingDirectory;
    }

    /**
     * @return The remote host to copy the data to (only with rsync, will use an ssh tunnel).
     */
    public String getOutgoingHost()
    {
        return outgoingHost;
    }

    /**
     * @return The regular expression to use for cleansing on the incoming directory before moving it to the buffer or
     *         <code>null</code>, if no regular expression for cleansing has been provided.
     */
    public Pattern getCleansingRegex()
    {
        return cleansingRegex;
    }

    /**
     * @return The regular expression to use for deciding whether a path in the incoming directory requires manual
     *         intervention or <code>null</code>, if no regular expression for manual interventionpaths has been
     *         provided.
     */
    public Pattern getManualInterventionRegex()
    {
        return manualInterventionRegex;
    }

    /**
     * Logs the current parameters to the {@link LogCategory#OPERATION} log.
     */
    public void log()
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Incoming directory: '%s'.", getIncomingDirectory().getAbsolutePath()));
            operationLog.info(String.format("Buffer directory: '%s'.", getBufferDirectory().getAbsolutePath()));
            operationLog.info(String.format("Outgoing directory: '%s'.", getOutgoingDirectory().getAbsolutePath()));
            if (null != getOutgoingHost())
            {
                operationLog.info(String.format("Outgoing host: '%s'.", getOutgoingHost()));
            }
            if (null != getManualInterventionDirectory())
            {
                operationLog.info(String.format("Manual interventions directory: '%s'.",
                        getManualInterventionDirectory().getAbsolutePath()));
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
            if (getManualInterventionRegex() != null)
            {
                operationLog.info(String.format(
                        "Regular expression used for deciding whether a path needs manual intervention: '%s'",
                        getManualInterventionRegex().pattern()));
            }
        }
    }

}
