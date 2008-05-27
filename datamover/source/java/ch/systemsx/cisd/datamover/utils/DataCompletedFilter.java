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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.ConditionalNotificationLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.utilities.AbstractHashable;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.StoreItemLocation;

/**
 * Filter which executes a shell script in order to determine whether a {@link StoreItem} should be
 * passed (i.e. accepted) or not. The script will be provided with one or two arguments: The
 * absolute path to store item and optional the remote host where the store item is located.
 * <p>
 * The filter remembers the last status of script execution. Status changes are logged with log
 * category {@link LogCategory#NOTIFY}.
 * 
 * @author Franz-Josef Elmer
 */
public class DataCompletedFilter implements IStoreItemFilter
{
    private static final class Status extends AbstractHashable
    {
        static final Status NULL = new Status();

        private final boolean ok;

        private final boolean run;

        private final boolean terminated;

        private final int exitValue;

        private final boolean blocked;

        private Status()
        {
            ok = true;
            run = false;
            terminated = false;
            blocked = false;
            exitValue = Integer.MAX_VALUE;
        }

        Status(final ProcessResult processResult)
        {
            ok = processResult.isOK();
            run = processResult.isRun();
            terminated = processResult.isTerminated();
            blocked = processResult.hasBlocked();
            exitValue = processResult.exitValue();
        }

        public final boolean isOk()
        {
            return ok;
        }

        public final boolean isRun()
        {
            return run;
        }

        public final boolean isTerminated()
        {
            return terminated;
        }

        public final int getExitValue()
        {
            return exitValue;
        }

        public final boolean isBlocked()
        {
            return blocked;
        }
    }

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DataCompletedFilter.class);

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, DataCompletedFilter.class);

    private final static Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, DataCompletedFilter.class);

    private final IFileStore fileStore;

    private final String dataCompletedScript;

    private final long dataCompletedScriptTimeout;

    private final ConditionalNotificationLogger notificationLogger;

    private Status lastStatus = Status.NULL;

    /**
     * Creates an instance for the specified file store, data completed script, and script time out.
     */
    public DataCompletedFilter(final IFileStore fileStore, final String dataCompletedScript,
            final long dataCompletedScriptTimeout)
    {
        if (dataCompletedScript == null)
        {
            throw new IllegalArgumentException("Data completed script not specified.");
        }
        this.dataCompletedScript = dataCompletedScript;
        notificationLogger = new ConditionalNotificationLogger(getClass(), 0);
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
        command.add("sh");
        command.add(getDataCompletedScript());
        command.add(storeItemLocation.getAbsolutePath());
        final String host = storeItemLocation.getHost();
        if (host != null)
        {
            command.add(host);
        }
        return command;
    }

    private final String getDataCompletedScript()
    {
        if (OSUtilities.executableExists(dataCompletedScript) == false)
        {
            notificationLogger.log(LogLevel.ERROR, String.format(
                    "Cannot find specified script '%s'.", dataCompletedScript));
        } else
        {
            notificationLogger.reset(String.format("Script '%s' is again accessible.",
                    dataCompletedScript));
        }
        return dataCompletedScript;
    }

    //
    // IStoreItemFilter
    //

    public final boolean accept(final StoreItem item)
    {
        final List<String> commandLine = createCommand(item);
        final ProcessResult result =
                ProcessExecutionHelper.run(commandLine, dataCompletedScriptTimeout, operationLog,
                        machineLog);
        final Status status = new Status(result);
        final boolean ok = status.isOk();
        if (status.equals(lastStatus) == false)
        {
            final String message =
                    "Processing status of data completed script has changed to " + status
                            + ". Command line: " + commandLine;
            if (ok)
            {
                if (notificationLog.isInfoEnabled())
                {
                    notificationLog.info(message);
                }
            } else
            {
                notificationLog.error(message);
            }
            result.log();
            lastStatus = status;
        }
        return ok;
    }

}
