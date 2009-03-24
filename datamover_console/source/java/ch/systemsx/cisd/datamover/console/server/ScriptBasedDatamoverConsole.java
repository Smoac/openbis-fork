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

package ch.systemsx.cisd.datamover.console.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.datamover.console.client.EnvironmentFailureException;
import ch.systemsx.cisd.datamover.console.client.dto.DatamoverStatus;

/**
 * Implementation based on script <code>datamover.sh</code>.
 * 
 * @author Franz-Josef Elmer
 */
public class ScriptBasedDatamoverConsole implements IDatamoverConsole
{
    private static final String SCRIPT_FILE = "datamover.sh";

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, ScriptBasedDatamoverConsole.class);

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, ScriptBasedDatamoverConsole.class);

    private final String name;

    private final String scriptPath;

    /**
     * Creates an instance for the specified datamover name and its working directory.
     */
    public ScriptBasedDatamoverConsole(String name, String scriptWorkingDirectory)
    {
        this.name = name;
        scriptPath = scriptWorkingDirectory + "/" + SCRIPT_FILE;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Shutdown signal sent to datamover '" + name + "'.");
        }

    }

    public DatamoverStatus obtainStatus()
    {
        final List<String> output = execute("mstatus").getOutput();
        if (output.isEmpty())
        {
            throw new EnvironmentFailureException(
                    "Nothing returned when obtaining status of datamover '" + name + "'.");
        }
        String value = output.get(0);
        DatamoverStatus status = DatamoverStatus.valueOf(value);
        if (status == null)
        {
            throw new EnvironmentFailureException("Unkown status obtained for datamover '" + name
                    + "': " + value);
        }
        return status;
    }

    public String tryObtainErrorMessage()
    {
        final ProcessResult presult = execute("status");
        if (presult.getExitValue() != 1)
        {
            return null;
        } else
        {
            return StringUtils.join(presult.getOutput(), '\n');
        }
    }

    public String tryObtainTarget()
    {
        List<String> output = execute("target").getOutput();
        return output.isEmpty() ? null : output.get(0);
    }

    public void shutdown()
    {
        ProcessResult result = execute("shutdown");
        if (result.isOK())
        {
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Shutdown signal sent to datamover '" + name + "'.");
            }
        } else
        {
            operationLog.error("A problem occured after sending to datamover '" + name
                    + "' a shutdown signale: ");
            List<String> output = result.getOutput();
            for (String line : output)
            {
                operationLog.error("   " + line);
            }
        }
    }

    public void start(String target)
    {
        ProcessResult result = execute("start", "--outgoing-target", target);
        if (result.isOK() == false)
        {
            String message =
                    "Datamover '" + name + "' couldn't be started for target '" + target + "'";
            operationLog.error(message + ": ");
            List<String> output = result.getOutput();
            for (String line : output)
            {
                operationLog.error("   " + line);
            }
            throw new EnvironmentFailureException(message + ".");
        }
    }

    //
    // ISelfTestable
    //

    public void check() throws ConfigurationFailureException
    {
        final File datamoverScriptFile = new File(scriptPath);
        if (datamoverScriptFile.exists() == false)
        {
            throw new ConfigurationFailureException("Cannot find Datamover '"
                    + datamoverScriptFile.getAbsolutePath() + "'");
        }
        final ProcessResult result = execute("mstatus");
        if (result.getExitValue() == ProcessResult.NO_EXIT_VALUE)
        {
            throw new ConfigurationFailureException("Unable to run Datamover '"
                    + datamoverScriptFile.getAbsolutePath() + "'");
        }
        final List<String> output = result.getOutput();
        if (output.size() != 1)
        {
            throw new ConfigurationFailureException("Unexpected output from Datamover '"
                    + datamoverScriptFile.getAbsolutePath() + "':\n"
                    + StringUtils.join(output, '\n'));
        }
    }

    public boolean isRemote()
    {
        return false;
    }

    private ProcessResult execute(String commandName, String... options)
    {
        List<String> command = new ArrayList<String>();
        if (OSUtilities.isWindows())
        {
            command.add("sh");
        }
        command.add(scriptPath);
        command.add(commandName);
        command.addAll(Arrays.asList(options));
        ProcessResult result =
                ProcessExecutionHelper.run(command, operationLog, machineLog,
                        ConcurrencyUtilities.NO_TIMEOUT,
                        ProcessExecutionHelper.OutputReadingStrategy.ALWAYS, true);
        return result;
    }

}
