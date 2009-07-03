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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
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
import ch.systemsx.cisd.etlserver.plugin_tasks.framework.IProcessingPluginTask;
import ch.systemsx.cisd.etlserver.plugin_tasks.framework.IReportingPluginTask;
import ch.systemsx.cisd.etlserver.plugin_tasks.framework.PluginTaskFactories;
import ch.systemsx.cisd.etlserver.plugin_tasks.framework.ProcessingPluginTaskFactory;
import ch.systemsx.cisd.etlserver.plugin_tasks.framework.ReportingPluginTaskFactory;
import ch.systemsx.cisd.etlserver.utils.PropertyParametersUtil;
import ch.systemsx.cisd.etlserver.utils.PropertyParametersUtil.SectionProperties;

/**
 * The class to process the command line parameters and service properties.
 * 
 * @author Bernd Rinn
 */
public class Parameters
{
    private static final String CHECK_INTERVAL_NAME = "check-interval";

    private static final String QUIET_PERIOD_NAME = "quiet-period";

    private static final String REPROCESS_FAULTY_DATASETS_NAME = "reprocess-faulty-datasets";

    private static final String SERVICE_PROPERTIES_FILE = "etc/service.properties";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, Parameters.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, Parameters.class);

    /** property with thread names separated by delimiter */
    private static final String INPUT_THREAD_NAMES = "inputs";

    @Private
    /* * property with repotring plugins names separated by delimiter */
    static final String REPORTING_PLUGIN_NAMES = "reporting-plugins";

    @Private
    /* * property with processing plugins names separated by delimiter */
    static final String PROCESSING_PLUGIN_NAMES = "processing-plugins";

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

    /**
     * The password to access the LIMS server with.
     */
    @Option(longName = REPROCESS_FAULTY_DATASETS_NAME, usage = "Specifies what should happen if an error occurs during dataset processing. "
            + "By default this flag is set to false and user has to modify the 'faulty paths file' each time the faulty dataset should be processed again. "
            + "Set this flag to true if the processing should be repeated after some time without any manual intervention.")
    private boolean reprocessFaultyDatasets;

    /** A subset of <code>service.properties</code> that are reserved for the <i>JavaMail API</i>. */
    private final Properties mailProperties;

    /**
     * The command line parser.
     */
    private final CmdLineParser parser = new CmdLineParser(this);

    private final TimingParameters timingParameters;

    private final Properties serviceProperties;

    private final ThreadParameters[] threads;

    private final PluginTaskFactories<IReportingPluginTask> reportingPlugins;

    private final PluginTaskFactories<IProcessingPluginTask> processingPlugins;

    private final Map<String, Properties> processorProperties;

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
            this.serviceProperties = PropertyUtils.loadProperties(SERVICE_PROPERTIES_FILE);
            PropertyUtils.trimProperties(serviceProperties);
            this.processorProperties = extractProcessorProperties(serviceProperties);
            this.threads = createThreadParameters(serviceProperties);
            this.mailProperties = createMailProperties(serviceProperties);
            this.timingParameters = TimingParameters.create(serviceProperties);
            this.reportingPlugins = createReportingPluginsFactories(serviceProperties);
            this.processingPlugins = createProcessingPluginsFactories(serviceProperties);

            initCommandLineParametersFromProperties();

            parser.parseArgument(args);
            ensureParametersCorrect();
        } catch (final Exception ex)
        {
            outputException(ex);
            systemExitHandler.exit(1);
            // Only reached in unit tests.
            throw new AssertionError(ex.getMessage());
        }
    }

    private void ensureParametersCorrect()
    {
        for (final ThreadParameters thread : threads)
        {
            thread.check();
        }
        reportingPlugins.check();
        processingPlugins.check();
        if (serverURL == null)
        {
            throw new ConfigurationFailureException("No 'server-url' defined.");
        }
    }

    private void initCommandLineParametersFromProperties()
    {
        serverURL = serviceProperties.getProperty("server-url");
        username = serviceProperties.getProperty("username");
        password = serviceProperties.getProperty("password");
        reprocessFaultyDatasets =
                Boolean.parseBoolean(serviceProperties.getProperty(REPROCESS_FAULTY_DATASETS_NAME,
                        "false"));
        checkIntervalSeconds =
                Long.parseLong(serviceProperties.getProperty(CHECK_INTERVAL_NAME, "120"));
        quietPeriodMillis = Long.parseLong(serviceProperties.getProperty(QUIET_PERIOD_NAME, "300"));
        shutdownTimeOutSeconds =
                Long.parseLong(serviceProperties.getProperty("shutdown-timeout", "30"));
    }

    private static Map<String, Properties> extractProcessorProperties(final Properties properties)
    {
        final LinkedHashMap<String, Properties> map = new LinkedHashMap<String, Properties>();
        final String processors = properties.getProperty("processors");
        if (processors != null)
        {
            final String[] procedureTypes =
                    processors.split(PropertyParametersUtil.ITEMS_DELIMITER);
            for (final String procedureType : procedureTypes)
            {
                final String prefix = "processor." + procedureType + ".";
                map.put(procedureType, ExtendedProperties.getSubset(properties, prefix, true));
            }
        }
        return map;
    }

    @Private
    static PluginTaskFactories<IReportingPluginTask> createReportingPluginsFactories(
            Properties serviceProperties)
    {
        SectionProperties[] sectionsProperties =
                extractSectionProperties(serviceProperties, REPORTING_PLUGIN_NAMES);
        ReportingPluginTaskFactory[] factories =
                new ReportingPluginTaskFactory[sectionsProperties.length];
        for (int i = 0; i < factories.length; i++)
        {
            factories[i] = new ReportingPluginTaskFactory(sectionsProperties[i]);
        }
        return new PluginTaskFactories<IReportingPluginTask>(factories);
    }

    @Private
    static PluginTaskFactories<IProcessingPluginTask> createProcessingPluginsFactories(
            Properties serviceProperties)
    {
        SectionProperties[] sectionsProperties =
                extractSectionProperties(serviceProperties, PROCESSING_PLUGIN_NAMES);
        ProcessingPluginTaskFactory[] factories =
                new ProcessingPluginTaskFactory[sectionsProperties.length];
        for (int i = 0; i < factories.length; i++)
        {
            factories[i] = new ProcessingPluginTaskFactory(sectionsProperties[i]);
        }
        return new PluginTaskFactories<IProcessingPluginTask>(factories);
    }

    private static SectionProperties[] extractSectionProperties(Properties serviceProperties,
            String namesListPropertyKey)
    {
        return PropertyParametersUtil.extractSectionProperties(serviceProperties,
                namesListPropertyKey, false);
    }

    private static ThreadParameters[] createThreadParameters(final Properties serviceProperties)
    {
        SectionProperties[] sectionsProperties =
                PropertyParametersUtil.extractSectionProperties(serviceProperties,
                        INPUT_THREAD_NAMES, true);
        if (sectionsProperties.length == 0)
        {
            // backward compatibility mode - no prefixes before thread properties, one thread only
            return new ThreadParameters[]
                { new ThreadParameters(serviceProperties, "default") };
        } else
        {
            return asThreadParameters(sectionsProperties);
        }
    }

    private static ThreadParameters[] asThreadParameters(SectionProperties[] sectionProperties)
    {
        final ThreadParameters[] threadParameters = new ThreadParameters[sectionProperties.length];
        for (int i = 0; i < threadParameters.length; i++)
        {
            SectionProperties section = sectionProperties[i];
            operationLog.info("Create parameters for thread '" + section.getKey() + "'.");
            threadParameters[i] = new ThreadParameters(section.getProperties(), section.getKey());
        }
        return threadParameters;
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

    public boolean reprocessFaultyDatasets()
    {
        return reprocessFaultyDatasets;
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
            processingPlugins.logConfigurations();
            reportingPlugins.logConfigurations();

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
