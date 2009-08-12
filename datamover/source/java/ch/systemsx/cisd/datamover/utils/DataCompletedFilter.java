/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IStoreItemFilter;
import ch.systemsx.cisd.common.filesystem.StoreItem;
import ch.systemsx.cisd.common.logging.ConditionalNotificationLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.datamover.DatamoverConstants;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.StoreItemLocation;

/**
 * Filter which executes a shell script in order to determine whether a {@link StoreItem} should be
 * passed (i.e. accepted) or not. The script will be provided with one or two arguments: The
 * absolute path to store item and optional the remote host where the store item is located.
 * <p>
 * The filter remembers the last status of script execution. Status changes are logged with log
 * category {@link LogCategory#NOTIFY}.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public class DataCompletedFilter implements IStoreItemFilter
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DataCompletedFilter.class);

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, DataCompletedFilter.class);

    private final static Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, DataCompletedFilter.class);

    private final ConditionalNotificationLogger conditionalNotificationLog =
        new ConditionalNotificationLogger(operationLog, notificationLog,
                DatamoverConstants.IGNORED_ERROR_COUNT_BEFORE_NOTIFICATION);


    private final IFileStore fileStore;

    private final File dataCompletedScript;

    private final long dataCompletedScriptTimeout;

    private final ConditionalNotificationLogger notificationLogger;

    private ProcessResult lastProcessResult;

    /**
     * Creates an instance for the specified file store, data completed script, and script time out.
     */
    public DataCompletedFilter(final IFileStore fileStore, final File dataCompletedScript,
            final long dataCompletedScriptTimeout)
    {
        if (dataCompletedScript == null)
        {
            throw new IllegalArgumentException("Data completed script not specified.");
        }
        this.dataCompletedScript = dataCompletedScript;
        notificationLogger =
                new ConditionalNotificationLogger(operationLog, Level.WARN, notificationLog, 0);
        this.dataCompletedScriptTimeout = dataCompletedScriptTimeout;
        if (fileStore == null)
        {
            throw new IllegalArgumentException("File store not specified.");
        }
        this.fileStore = fileStore;
    }

    private final List<String> createCommand(final StoreItem item)
    {
        final StoreItemLocation storeItemLocation = fileStore.getStoreItemLocation(item);
        final List<String> command = new ArrayList<String>();
        if (OSUtilities.isWindows())
        {
            command.add("sh");
        }
        command.add(FileUtilities.getCanonicalPath(getDataCompletedScript()));
        command.add(storeItemLocation.getAbsolutePath());
        final String host = storeItemLocation.getHost();
        if (host != null)
        {
            command.add(host);
        }
        return command;
    }

    private final File getDataCompletedScript()
    {
        if (OSUtilities.executableExists(dataCompletedScript) == false)
        {
            notificationLogger.log(LogLevel.ERROR, String.format(
                    "Cannot find specified script '%s'.", dataCompletedScript));
        } else
        {
            notificationLogger.reset(String.format("Script '%s' is again accessible.",
                    FileUtilities.getCanonicalPath(dataCompletedScript)));
        }
        return dataCompletedScript;
    }

    private final static String describeProcessResult(final ProcessResult result)
    {
        assert result != null : "Unspecified process result";
        final StringBuilder builder = new StringBuilder("[");
        builder.append("interrupted=").append(result.isInterruped()).append(",");
        builder.append("exitValue=").append(result.getExitValue()).append(",");
        builder.append("ok=").append(result.isOK()).append(",");
        builder.append("terminated=").append(result.isTerminated()).append(",");
        builder.append("timedOut=").append(result.isTimedOut()).append(",");
        builder.append("output=").append(result.getOutput()).append(",");
        builder.append("run=").append(result.isRun()).append(",");
        builder.append("startupFailureMessage=").append(result.getStartupFailureMessage());
        builder.append("]");
        return builder.toString();
    }

    //
    // IStoreItemFilter
    //

    public final boolean accept(final StoreItem item)
    {
        final List<String> commandLine = createCommand(item);
        final ProcessResult result =
                ProcessExecutionHelper.run(commandLine, operationLog, machineLog,
                        dataCompletedScriptTimeout);
        final boolean ok = result.isOK();
        if (result.equals(lastProcessResult) == false)
        {
            final String message =
                    String
                            .format(
                                    "Processing status of data completed script has changed to '%s'. ICommand line: '%s'.",
                                    describeProcessResult(result), commandLine);
            if (ok)
            {
                conditionalNotificationLog.reset(null);
                conditionalNotificationLog.log(LogLevel.INFO, message);
            } else
            {
                conditionalNotificationLog.log(LogLevel.WARN, message);
            }
            result.log();
            lastProcessResult = result;
        }
        return ok;
    }

}
