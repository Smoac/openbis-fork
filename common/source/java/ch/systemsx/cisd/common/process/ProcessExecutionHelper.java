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

package ch.systemsx.cisd.common.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.utilities.OSUtilities;

/**
 * Utility to execute a command from a command line and log all events.
 * 
 * @author Tomasz Pylak
 * @author Bernd Rinn
 */
public final class ProcessExecutionHelper
{

    /**
     * The value indicating that there is no exit value available for a process execution.
     */
    public static final int NO_EXIT_VALUE = -1;

    /**
     * The value indicating the process execution went OK.
     */
    public static final int EXIT_VALUE_OK = 0;

    /**
     * The exit value returned by {@link Process#waitFor()} if the process was terminated by
     * {@link Process#destroy()} on a UNIX machine.
     */
    private static final int EXIT_VALUE_FOR_TERMINATION_UNIX = 143;

    /**
     * The exit value returned by {@link Process#waitFor()} if the process was terminated by
     * {@link Process#destroy()} on a MS Windows machine.
     */
    private static final int EXIT_VALUE_FOR_TERMINATION_WINDOWS = 1;

    private final Logger operationLog;

    private final Logger machineLog;

    public static List<String> createSshCommand(String command, File sshExecutable, String host)
    {
        ArrayList<String> wrappedCmd = new ArrayList<String>();
        List<String> sshCommand = Arrays.asList(sshExecutable.getPath(), "-T", host);
        wrappedCmd.addAll(sshCommand);
        wrappedCmd.add(command);
        return wrappedCmd;
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param commandLine The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code>
     *         otherwise.
     */
    public static boolean runAndLog(final List<String> commandLine, final Logger operationLog,
            final Logger machineLog)
    {
        return new ProcessExecutionHelper(operationLog, machineLog).runAndLog(commandLine, 0L);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param commandLine The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return The process result.
     */
    public static ProcessResult run(final List<String> commandLine, final Logger operationLog,
            final Logger machineLog)
    {
        return new ProcessExecutionHelper(operationLog, machineLog).runWithoutWatchdog(commandLine);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli
     *            seconds. If the process is not finished after that time, it will be terminated by
     *            a watch dog.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code>
     *         otherwise.
     */
    public static boolean runAndLog(final List<String> cmd, final long millisToWaitForCompletion,
            final Logger operationLog, final Logger machineLog)
    {
        return new ProcessExecutionHelper(operationLog, machineLog).runAndLog(cmd,
                millisToWaitForCompletion);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli
     *            seconds. If the process is not finished after that time, it will be terminated by
     *            a watch dog.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return The process result.
     */
    public static ProcessResult run(final List<String> cmd, final long millisToWaitForCompletion,
            final Logger operationLog, final Logger machineLog)
    {
        return new ProcessExecutionHelper(operationLog, machineLog).runWithWatchdog(cmd,
                millisToWaitForCompletion);
    }

    /**
     * Returns <code>true</code> if the <var>exitValue</var> indicates that the process has been
     * terminated on the Operating System level.
     */
    public static boolean isProcessTerminated(final int exitValue)
    {
        if (OSUtilities.isWindows())
        {
            return exitValue == EXIT_VALUE_FOR_TERMINATION_WINDOWS;
        } else
        {
            return exitValue == EXIT_VALUE_FOR_TERMINATION_UNIX;
        }
    }

    /**
     * Returns the stdout (and stderr if {@link ProcessBuilder#redirectErrorStream(boolean)} has
     * been called with <code>true</code>).
     */
    public static List<String> readProcessOutputLines(final Process processOrNull,
            final Logger machineLog)
    {
        final List<String> processOutput = new ArrayList<String>();
        if (processOrNull == null)
        {
            return processOutput;
        }
        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(processOrNull.getInputStream()));
        try
        {
            String ln;
            while ((ln = reader.readLine()) != null)
            {
                processOutput.add(ln);
            }
        } catch (final IOException e)
        {
            machineLog.warn(String.format("IOException when reading stdout, msg='%s'.", e
                    .getMessage()));
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
        return processOutput;
    }

    //
    // Implementation
    //

    private ProcessExecutionHelper(final Logger operationLog, final Logger machineLog)
    {
        this.operationLog = operationLog;
        this.machineLog = machineLog;
    }

    private final ProcessResult runWithWatchdog(final List<String> commandLine,
            final long millisoWaitForCompletion)
    {
        assert millisoWaitForCompletion > 0L : "Unspecified time out.";
        final ProcessWatchdog processWatchdog = new ProcessWatchdog(millisoWaitForCompletion);
        try
        {
            final Process process = launchProcess(commandLine);
            processWatchdog.start(process);
            try
            {
                process.waitFor();
                processWatchdog.stop();
            } catch (final InterruptedException e)
            {
                process.destroy();
                operationLog.warn(String.format("Execution of %s interrupted after timeout.",
                        commandLine));
            }
            return createResult(commandLine, process, processWatchdog.isProcessKilled());
        } catch (final IOException ex)
        {
            return createNotStartedResult(commandLine, ex);
        }
    }

    private final ProcessResult runWithoutWatchdog(final List<String> commandLine)
    {
        try
        {
            final Process process = launchProcess(commandLine);
            try
            {
                process.waitFor();
            } catch (final InterruptedException e)
            {
                process.destroy();
                operationLog.warn(String.format("Execution of %s interrupted after timeout.",
                        commandLine));
            }
            return createResult(commandLine, process, false);
        } catch (final IOException ex)
        {
            return createNotStartedResult(commandLine, ex);
        }
    }

    private final Process launchProcess(final List<String> commandLine) throws IOException
    {
        final ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
        processBuilder.redirectErrorStream(true);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Executing command: " + commandLine);
        }
        // NOTE 2008-02-04, Tomasz Pylak: This operation can get blocked. I've observed it when ln
        // was executed on NAS
        // file system mounted locally.
        final Process process = processBuilder.start();
        return process;
    }

    private final ProcessResult createNotStartedResult(final List<String> commandLine,
            final IOException ex)
    {
        machineLog.error(String.format("Cannot execute executable %s", commandLine), ex);
        return ProcessResult.createNotStarted(commandLine, operationLog, machineLog);
    }

    private final ProcessResult createResult(final List<String> commandLine,
            final Process processOrNull, final boolean isInterrupted)
    {
        if (processOrNull == null)
        {
            return ProcessResult.createNotStarted(commandLine, operationLog, machineLog);
        } else
        {
            if (isInterrupted)
            {
                return ProcessResult.createWaitingInterrupted(processOrNull, commandLine,
                        operationLog, machineLog);
            } else
            {
                return ProcessResult.create(processOrNull, commandLine, operationLog, machineLog);
            }
        }
    }

    private final boolean runAndLog(final List<String> cmd, final long millisToWaitForCompletion)
    {
        final ProcessResult result;
        if (millisToWaitForCompletion > 0L)
        {
            result = runWithWatchdog(cmd, millisToWaitForCompletion);
        } else
        {
            result = runWithoutWatchdog(cmd);
        }
        result.log();
        result.destroyProcess();
        return result.isOK();
    }

    public final static void logProcessExecution(final String commandName, final int exitValue,
            final List<String> processOutput, final Logger operationLog, final Logger machineLog)
    {
        if (exitValue != EXIT_VALUE_OK)
        {
            logProcessExitValue(Level.WARN, operationLog, commandName, exitValue);
            logProcessOutput(Level.WARN, machineLog, commandName, processOutput);
        } else if (operationLog.isDebugEnabled())
        {
            logProcessExitValue(Level.DEBUG, operationLog, commandName, exitValue);
            logProcessOutput(Level.DEBUG, machineLog, commandName, processOutput);
        }
    }

    private final static void logProcessExitValue(final Level logLevel, final Logger operationLog,
            final String commandName, final int exitValue)
    {
        assert logLevel != null;
        assert operationLog != null;
        assert commandName != null;

        if (isProcessTerminated(exitValue))
        {
            operationLog.log(logLevel, String.format("[%s] process was destroyed.", commandName));
        } else
        {
            operationLog.log(logLevel, String.format("[%s] process returned with exit value %d.",
                    commandName, exitValue));
        }
    }

    private final static void logProcessOutput(final Level logLevel, final Logger machineLog,
            final String commandName, final List<String> processOutputLines)
    {
        assert logLevel != null;
        assert machineLog != null;
        assert commandName != null;
        assert processOutputLines != null;

        if (processOutputLines.size() == 0)
        {
            return;
        }
        machineLog.log(logLevel, String.format("[%s] output:", commandName));
        for (final String ln : processOutputLines)
        {
            if (ln.trim().length() > 0)
            {
                machineLog.log(logLevel, String.format("\"%s\"", ln));
            }
        }
    }
}
