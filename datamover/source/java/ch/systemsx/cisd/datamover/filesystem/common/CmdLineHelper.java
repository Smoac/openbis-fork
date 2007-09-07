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

package ch.systemsx.cisd.datamover.filesystem.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.utilities.OSUtilities;

/**
 * Utility to execute a command from a command line and log all events.
 * 
 * @author Tomasz Pylak
 */
public class CmdLineHelper
{
    /**
     * The exit value returned by {@link Process#waitFor()} if the process was terminated by {@link Process#destroy()}
     * on a UNIX machine.
     */
    private static final int EXIT_VALUE_FOR_TERMINATION_UNIX = 143;

    /**
     * The exit value returned by {@link Process#waitFor()} if the process was terminated by {@link Process#destroy()}
     * on a MS Windows machine.
     */
    private static final int EXIT_VALUE_FOR_TERMINATION_WINDOWS = 1;

    private final Logger operationLog;

    private final Logger machineLog;

    private CmdLineHelper(Logger operationLog, Logger machineLog)
    {
        this.operationLog = operationLog;
        this.machineLog = machineLog;
    }

    public static boolean run(List<String> cmd, Logger operationLog, Logger machineLog)
    {
        return new CmdLineHelper(operationLog, machineLog).run(cmd);
    }

    public static void logProcessExitValue(final int exitValue, final Process process, String name,
            Logger operationLog, Logger machineLog)
    {
        new CmdLineHelper(operationLog, machineLog).logProcessExitValue(exitValue, process, name);
    }

    private boolean run(List<String> cmd)
    {
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Executing command: " + cmd);
        }
        Process process;
        try
        {
            process = processBuilder.start();
        } catch (IOException e)
        {
            machineLog.error(String.format("Cannot execute binary %s", cmd), e);
            return false;
        }

        int exitValue;
        try
        {
            exitValue = process.waitFor();
        } catch (InterruptedException e2)
        {
            machineLog.error(String.format("Execution of %s interupted", process), e2);
            return false;
        }
        logProcessExitValue(exitValue, process, cmd.get(0).toString());
        return (exitValue == 0);
    }

    private void logProcessExitValue(final int exitValue, final Process process, String command)
    {
        if (operationLog.isDebugEnabled())
        {
            if (processTerminated(exitValue))
            {
                operationLog.debug(String.format("[%s] process was destroyed.", command));
            } else
            {
                operationLog.debug(String.format("[%s] process returned with exit value %d.", command, exitValue));
            }
            if (exitValue != 0)
            {
                final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                try
                {
                    machineLog.debug(String.format("[%s] output:", command));
                    String ln;
                    while ((ln = reader.readLine()) != null)
                    {
                        if (ln.trim().length() > 0)
                            machineLog.debug(String.format("\"%s\"", ln));
                    }
                } catch (IOException e)
                {
                    operationLog.debug(String.format("IOException when trying to read stderr, msg='%s'.", e
                            .getMessage()));
                } finally
                {
                    try
                    {
                        reader.close();
                    } catch (IOException e)
                    {
                        // Silence this.
                    }
                }
            }
        }
    }

    public static boolean processTerminated(final int exitValue)
    {
        if (OSUtilities.isWindows())
        {
            return exitValue == EXIT_VALUE_FOR_TERMINATION_WINDOWS;
        } else
        {
            return exitValue == EXIT_VALUE_FOR_TERMINATION_UNIX;
        }
    }
}
