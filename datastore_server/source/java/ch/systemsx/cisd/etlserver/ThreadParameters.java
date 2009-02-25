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
import java.util.Properties;

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

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ThreadParameters.class);

    private static final String INCOMING_DIR = "incoming-dir";

    /**
     * The (local) directory to monitor for new files and directories to move to the remote side.
     * The directory where data to be processed by the ETL server become available.
     */
    private final File incomingDataDirectory;

    private final IETLServerPlugin plugin;

    private final String threadName;

    private final String groupCode;

    /**
     * @param threadProperties parameters for one processing thread together with general
     *            parameters.
     */
    public ThreadParameters(final Properties threadProperties, final String threadName)
    {
        this.incomingDataDirectory = extractIncomingDataDir(threadProperties);
        this.plugin = new PropertiesBasedETLServerPlugin(threadProperties);
        groupCode = tryGetGroupCode(threadProperties);
        this.threadName = threadName;
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
        return StringUtils.defaultIfEmpty(PropertyUtils.getProperty(properties, GROUP_CODE_KEY),
                null);
    }

    /**
     * Returns the <code>group-code</code> property specified for this thread.
     */
    final String tryGetGroupCode()
    {
        return groupCode;
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
            operationLog.info(String.format("[%s] Code extractor: '%s'", threadName, plugin
                    .getDataSetInfoExtractor().getClass().getName()));
            operationLog.info(String.format("[%s] Type extractor: '%s'", threadName, plugin
                    .getTypeExtractor().getClass().getName()));
            operationLog.info(String.format("[%s] Incoming data directory: '%s'.", threadName,
                    getIncomingDataDirectory().getAbsolutePath()));
            if (groupCode != null)
            {
                operationLog.info(String.format("[%s] Group code: '%s'.", threadName, groupCode));
            }
        }
    }

    public String getThreadName()
    {
        return threadName;
    }

}
