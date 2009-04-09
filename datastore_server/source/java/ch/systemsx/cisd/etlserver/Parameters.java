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

package ch.systemsx.cisd.etlserver;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.base.utilities.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.common.TimingParameters;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.JavaMailProperties;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.common.utilities.SystemExit;

/**
 * The class to process the command line parameters and service properties.
 * 
 * @author Bernd Rinn
 */
public class Parameters
{
    private static final String CHECK_INTERVAL_NAME = "check-interval";

    private static final String QUIET_PERIOD_NAME = "quiet-period";

    private static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, Parameters.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, Parameters.class);

    /** property with thread names separated by {@link #ITEMS_DELIMITER} */
    private static final String INPUT_THREAD_NAMES = "inputs";

    private static final String ITEMS_DELIMITER = ",";

    @Option(name = "s", longName = "server-url", metaVar = "URL", usage = "URL of the server")
    private String serverURL;

    /**
     * The interval to wait between to checks for activity (in milliseconds).
     */
    @Option(name = "c", longName = CHECK_INTERVAL_NAME, usage = "The interval to wait between two checks (in seconds) "
            + "[default: 120]")
    private long checkIntervalSeconds;

    /**
     * Valid only when {@link ThreadParameters#INCOMING_DATA_COMPLETENESS_CONDITION} is false.<br>
     * The period to wait before a file or directory is considered "quiet" (in milliseconds). This
     * setting is used when deciding whether a file or directory is ready to be processed.
     */
    @Option(name = "q", longName = QUIET_PERIOD_NAME, usage = "The period of no write access that needs to pass before an incoming data item is "
            + "considered complete and ready to be processed (in seconds) [default: 300]. "
            + "Valid only when auto-detection method is used to determine if an incoming data are ready to be processed.")
    private long quietPeriodMillis;

    /**
     * The time-out for clean up work in the shutdown sequence (in seconds).
     * <p>
     * Note that that the maximal time for the shutdown sequence to complete can be as large as
     * twice this time.
     */
    @Option(name = "t", longName = "shutdown-timeout", usage = "The time-out for clean up work "
            + "in the shutdown sequence (in seconds) [default: 30]")
    private long shutdownTimeOutSeconds;

    /**
     * The username to access the LIMS server with.
     */
    @Option(name = "u", longName = "username", usage = "User login name")
    private String username;

    /**
     * The password to access the LIMS server with.
     */
    @Option(name = "p", longName = "password", usage = "User login password")
    private String password;

    /** A subset of <code>service.properties</code> that are reserved for the <i>JavaMail API</i>. */
    private Properties mailProperties;

    /**
     * The command line parser.
     */
    private final CmdLineParser parser = new CmdLineParser(this);

    private TimingParameters timingParameters;

    private Properties serviceProperties;

    private ThreadParameters[] threads;

    private Map<String, Properties> processorProperties;

    @Option(longName = "help", skipForExample = true, usage = "Prints out a description of the options.")
    void printHelp(final boolean exit)
    {
        parser.printHelp("etlserver", "<required options> [option [...]]", "", ExampleMode.ALL);
        if (exit)
        {
            System.exit(0);
        }
    }

    @Option(longName = "version", skipForExample = true, usage = "Prints out the version information.")
    void printVersion(final boolean exit)
    {
        System.err
                .println("etlserver version " + BuildAndEnvironmentInfo.INSTANCE.getFullVersion());
        if (exit)
        {
            System.exit(0);
        }
    }

    @Option(longName = "test-notify", skipForExample = true, usage = "Tests the notify log (i.e. that an email is "
            + "sent out).")
    void sendTestNotification(final boolean exit)
    {
        notificationLog
                .error("This is a test notification given due to specifying the --test-notify option.");
        if (exit)
        {
            System.exit(0);
        }
    }

    Parameters(final String[] args)
    {
        this(args, SystemExit.SYSTEM_EXIT);
    }

    Parameters(final String[] args, final IExitHandler systemExitHandler)
    {
        try
        {
            initParametersFromProperties();

            parser.parseArgument(args);
            for (final ThreadParameters thread : threads)
            {
                thread.check();
            }
            if (serverURL == null)
            {
                throw new ConfigurationFailureException("No 'server-url' defined.");
            }
        } catch (final Exception ex)
        {
            outputException(ex);
            systemExitHandler.exit(1);
            // Only reached in unit tests.
            throw new AssertionError(ex.getMessage());
        }
    }

    private void initParametersFromProperties()
    {
        serviceProperties = PropertyUtils.loadProperties(SERVICE_PROPERTIES_FILE);
        PropertyUtils.trimProperties(serviceProperties);
        processorProperties = extractProcessorProperties(serviceProperties);
        threads = createThreadParameters(serviceProperties);
        serverURL = serviceProperties.getProperty("server-url");
        username = serviceProperties.getProperty("username");
        password = serviceProperties.getProperty("password");
        checkIntervalSeconds =
                Long.parseLong(serviceProperties.getProperty(CHECK_INTERVAL_NAME, "120"));
        quietPeriodMillis = Long.parseLong(serviceProperties.getProperty(QUIET_PERIOD_NAME, "300"));
        shutdownTimeOutSeconds =
                Long.parseLong(serviceProperties.getProperty("shutdown-timeout", "30"));
        mailProperties = createMailProperties(serviceProperties);
        timingParameters = TimingParameters.create(serviceProperties);
    }

    private static Map<String, Properties> extractProcessorProperties(final Properties properties)
    {
        final LinkedHashMap<String, Properties> map = new LinkedHashMap<String, Properties>();
        final String processors = properties.getProperty("processors");
        if (processors != null)
        {
            final String[] procedureTypes = processors.split(ITEMS_DELIMITER);
            for (final String procedureType : procedureTypes)
            {
                final String prefix = "processor." + procedureType + ".";
                map.put(procedureType, ExtendedProperties.getSubset(properties, prefix, true));
            }
        }
        return map;
    }

    private static ThreadParameters[] createThreadParameters(final Properties serviceProperties)
    {
        final String threadNames = serviceProperties.getProperty(INPUT_THREAD_NAMES);
        if (threadNames == null)
        {
            // backward compatibility mode - no prefixes before thread properties, one thread only
            return new ThreadParameters[]
                { new ThreadParameters(serviceProperties, "default") };
        } else
        {
            final String[] names = threadNames.split(ITEMS_DELIMITER);
            validateThreadNames(names);
            return createThreadParameters(names, serviceProperties);
        }
    }

    private static ThreadParameters[] createThreadParameters(final String[] names,
            final Properties serviceProperties)
    {
        final ThreadParameters[] threadParameters = new ThreadParameters[names.length];
        final Properties generalProperties =
                removeThreadSpecificProperties(names, serviceProperties);
        for (int i = 0; i < names.length; i++)
        {
            final String name = names[i].trim();
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Create parameters for thread '" + name + "'.");
            }
            // extract thread specific properties, remove prefix
            final ExtendedProperties threadProperties =
                    ExtendedProperties.getSubset(serviceProperties, getPropertyPrefix(name), true);
            threadProperties.putAll(generalProperties); // add all general properties
            threadParameters[i] = new ThreadParameters(threadProperties, name);
        }
        return threadParameters;
    }

    private static Properties removeThreadSpecificProperties(final String[] names,
            final Properties properties)
    {
        final ExtendedProperties generalProperties = ExtendedProperties.createWith(properties);
        for (final String name : names)
        {
            generalProperties.removeSubset(getPropertyPrefix(name));
        }
        return generalProperties;
    }

    private static String getPropertyPrefix(final String name)
    {
        return name + ".";
    }

    private static void validateThreadNames(final String[] names)
    {
        final Set<String> processed = new HashSet<String>();
        for (final String name : names)
        {
            if (processed.contains(name))
            {
                throw new ConfigurationFailureException("Duplicated thread name: " + name);
            }
            if (name.length() == 0)
            {
                throw new ConfigurationFailureException("Thread name:cannot be empty!");
            }
            processed.add(name);
        }
    }

    private final static Properties createMailProperties(final Properties serviceProperties)
    {
        final Properties properties =
                ExtendedProperties.getSubset(serviceProperties, "mail", false);
        if (properties.getProperty(JavaMailProperties.MAIL_SMTP_HOST) == null)
        {
            properties.setProperty(JavaMailProperties.MAIL_SMTP_HOST, "localhost");
        }
        if (properties.getProperty(JavaMailProperties.MAIL_FROM) == null)
        {
            properties.setProperty(JavaMailProperties.MAIL_FROM, "etlserver@localhost");
        }
        return properties;
    }

    private void outputException(final Exception ex)
    {
        if (ex instanceof UserFailureException || ex instanceof CmdLineException)
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

    /**
     * Returns The interval to wait between to checks for activity (in milliseconds).
     */
    public long getCheckIntervalMillis()
    {
        return checkIntervalSeconds * DateUtils.MILLIS_PER_SECOND;
    }

    public long getQuietPeriodMillis()
    {
        return quietPeriodMillis * DateUtils.MILLIS_PER_SECOND;
    }

    /**
     * Returns the time-out for clean up work in the shutdown sequence (in seconds).
     * <p>
     * Note that that the maximal time for the shutdown sequence to complete can be as large as
     * twice this time.
     */
    public long getShutdownTimeOutMillis()
    {
        return shutdownTimeOutSeconds * DateUtils.MILLIS_PER_SECOND;
    }

    /**
     * Returns the password to access the LIMS server with.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Returns the username to access the LIMS server with.
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Returns all properties.
     */
    public final Properties getProperties()
    {
        return serviceProperties;
    }

    /**
     * Returns a map of all processor properties with the procedure type code as the key.
     */
    public final Map<String, Properties> getProcessorProperties()
    {
        return processorProperties;
    }

    /** Returns <code>mailProperties</code>. */
    public final Properties getMailProperties()
    {
        return mailProperties;
    }

    /**
     * Returns the timing parameters for monitored operations.
     */
    public TimingParameters getTimingParameters()
    {
        return timingParameters;
    }

    /**
     * Logs the current parameters to the {@link LogCategory#OPERATION} log.
     */
    public void log()
    {
        if (operationLog.isInfoEnabled())
        {
            for (final ThreadParameters threadParameters : threads)
            {
                threadParameters.log();
            }
            operationLog.info(String.format("Check intervall: %d s.",
                    getCheckIntervalMillis() / 1000));
            operationLog
                    .info(String
                            .format(
                                    "Quiet period (valid if auto-detection method is used to determine incoming data completeness): %d s.",
                                    getQuietPeriodMillis() / 1000));
        }
    }

    /**
     * Returns the URL of the LIMS server.
     */
    public String getServerURL()
    {
        return serverURL;
    }

    public ThreadParameters[] getThreads()
    {
        return threads;
    }

}
