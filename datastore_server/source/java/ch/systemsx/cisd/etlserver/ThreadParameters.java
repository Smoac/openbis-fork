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

import java.io.File;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * <i>ETL</i> thread specific parameters.
 * 
 * @author Tomasz Pylak
 */
public final class ThreadParameters
{
    @Private
    static final String GROUP_CODE_KEY = "group-code";

    @Private
    static final String INCOMING_DATA_COMPLETENESS_CONDITION =
            "incoming-data-completeness-condition";

    @Private
    static final String INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE = "marker-file";

    @Private
    static final String INCOMING_DATA_COMPLETENESS_CONDITION_AUTODETECTION = "auto-detection";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ThreadParameters.class);

    private static final String INCOMING_DIR = "incoming-dir";

    private static final String DELETE_UNIDENTIFIED_KEY = "delete-unidentified";

    /**
     * The (local) directory to monitor for new files and directories to move to the remote side.
     * The directory where data to be processed by the ETL server become available.
     */
    private final File incomingDataDirectory;

    private final IETLServerPlugin plugin;

    private final String threadName;

    private final String groupCode;

    private final boolean useIsFinishedMarkerFile;

    private final boolean deleteUnidentified;

    /**
     * @param threadProperties parameters for one processing thread together with general
     *            parameters.
     */
    public ThreadParameters(final Properties threadProperties, final String threadName)
    {
        this.incomingDataDirectory = extractIncomingDataDir(threadProperties);
        this.plugin = new PropertiesBasedETLServerPlugin(threadProperties);
        this.groupCode = tryGetGroupCode(threadProperties);
        String completenessCondition =
                PropertyUtils.getProperty(threadProperties, INCOMING_DATA_COMPLETENESS_CONDITION,
                        INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE);
        this.useIsFinishedMarkerFile = parseCompletenessCondition(completenessCondition);
        this.deleteUnidentified =
                "true".equals(threadProperties.getProperty(DELETE_UNIDENTIFIED_KEY, "false"));
        this.threadName = threadName;
    }

    // true if marker file should be used, false if autodetection should be used, exceprion when the
    // value is invalid.
    private static boolean parseCompletenessCondition(String completenessCondition)
    {
        if (completenessCondition
                .equalsIgnoreCase(INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE))
        {
            return true;
        } else if (completenessCondition
                .equalsIgnoreCase(INCOMING_DATA_COMPLETENESS_CONDITION_AUTODETECTION))
        {
            return false;
        } else
        {
            throw new ConfigurationFailureException(String.format(
                    "Invalid value '%s' for the option '%s'. Allowed values are: '%s', '%s'.",
                    completenessCondition, INCOMING_DATA_COMPLETENESS_CONDITION,
                    INCOMING_DATA_COMPLETENESS_CONDITION_MARKER_FILE,
                    INCOMING_DATA_COMPLETENESS_CONDITION_AUTODETECTION));
        }
    }

    final void check()
    {
        if (incomingDataDirectory.isDirectory() == false)
        {
            throw new ConfigurationFailureException("Incoming directory '" + incomingDataDirectory
                    + "' is not a directory.");
        }
    }

    @Private
    static File extractIncomingDataDir(final Properties threadProperties)
    {
        final String incomingDir = threadProperties.getProperty(INCOMING_DIR);
        if (StringUtils.isNotBlank(incomingDir))
        {
            return FileUtilities.normalizeFile(new File(incomingDir));
        } else
        {
            throw new ConfigurationFailureException("No '" + INCOMING_DIR + "' defined.");
        }
    }

    @Private
    static final String tryGetGroupCode(final Properties properties)
    {
        return nullIfEmpty(PropertyUtils.getProperty(properties, GROUP_CODE_KEY));
    }

    private static String nullIfEmpty(String value)
    {
        return StringUtils.defaultIfEmpty(value, null);
    }

    /**
     * Returns the <code>group-code</code> property specified for this thread.
     */
    final String tryGetGroupCode()
    {
        return groupCode;
    }

    public boolean useIsFinishedMarkerFile()
    {
        return useIsFinishedMarkerFile;
    }

    /**
     * Returns The directory to monitor for incoming data.
     */
    final File getIncomingDataDirectory()
    {
        return incomingDataDirectory;
    }

    final IETLServerPlugin getPlugin()
    {
        return plugin;
    }

    /**
     * Logs the current parameters to the {@link LogCategory#OPERATION} log.
     */
    final void log()
    {
        if (operationLog.isInfoEnabled())
        {
            logLine("Code extractor: '%s'", plugin.getDataSetInfoExtractor().getClass().getName());
            logLine("Type extractor: '%s'", plugin.getTypeExtractor().getClass().getName());
            logLine("Incoming data directory: '%s'.", getIncomingDataDirectory().getAbsolutePath());
            if (groupCode != null)
            {
                logLine("Group code: '%s'.", groupCode);
            }
            String completenessCond =
                    useIsFinishedMarkerFile ? "marker file exists"
                            : "no write access for some period";
            logLine("Condition of incoming data completeness: %s.", completenessCond);
            logLine("Delete unidentified: '%s'.", deleteUnidentified);
        }
    }

    private void logLine(String format, Object... params)
    {
        Vector<Object> allParams = new Vector<Object>();
        allParams.add(threadName);
        allParams.addAll(Arrays.asList(params));
        operationLog.info(String.format("[%s] " + format, allParams.toArray(new Object[0])));
    }

    public String getThreadName()
    {
        return threadName;
    }

    public boolean deleteUnidentified()
    {
        return deleteUnidentified;
    }
}
