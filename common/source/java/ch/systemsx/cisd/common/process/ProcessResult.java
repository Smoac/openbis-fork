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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.concurrent.ExecutionStatus;

/**
 * Class that keeps around the result of running an Operating System process.
 * <p>
 * Since the process output can only ever be read once from a process, it need to be kept around if
 * it is needed more than once. This is what this class is good for.
 */
public final class ProcessResult
{
    /**
     * The value indicating the process execution went OK.
     */
    public static final int EXIT_VALUE_OK = 0;

    /**
     * The value indicating that there is no exit value available for a process execution.
     */
    public static final int NO_EXIT_VALUE = -1;

    /**
     * The exit value returned by {@link Process#waitFor()} if the process was terminated by
     * {@link Process#destroy()} on a MS Windows machine.
     */
    private static final int EXIT_VALUE_FOR_TERMINATION_WINDOWS = 1;

    /**
     * The exit value returned by {@link Process#waitFor()} if the process was terminated by
     * {@link Process#destroy()} on a UNIX machine.
     */
    private static final int EXIT_VALUE_FOR_TERMINATION_UNIX = 143;

    private final ExecutionStatus status;

    private final String startupFailureMessage;

    private final int exitValue;

    private final List<String> commandLine;

    private final String commandName;

    private final int processNumber;

    private final Logger operationLog;

    private final Logger machineLog;

    private final boolean outputAvailable;

    private final List<String> output;

    /**
     * Returns <code>true</code> if the <var>exitValue</var> indicates that the process has been
     * terminated on the Operating System level.
     */
    public static boolean isProcessTerminated(final int exitValue)
    {
        if (OSUtilities.isWindows())
        {
            return exitValue == ProcessResult.EXIT_VALUE_FOR_TERMINATION_WINDOWS;
        } else
        {
            return exitValue == ProcessResult.EXIT_VALUE_FOR_TERMINATION_UNIX;
        }
    }

    public static boolean isProcessOK(final int exitValue)
    {
        return (exitValue == EXIT_VALUE_OK);
    }

    public ProcessResult(final List<String> commandLine, final int processNumber,
            final ExecutionStatus status, final String startupFailureMessageOrNull,
            final int exitValue, final List<String> processOutputOrNull, final Logger operationLog,
            final Logger machineLog)
    {
        this.commandLine = commandLine;
        this.commandName = ProcessExecutionHelper.getCommandName(commandLine);
        this.processNumber = processNumber;
        this.status = status;
        this.startupFailureMessage =
                (startupFailureMessageOrNull == null) ? "" : startupFailureMessageOrNull;
        this.exitValue = exitValue;
        this.outputAvailable = (processOutputOrNull != null);
        if (outputAvailable)
        {
            this.output = Collections.unmodifiableList(processOutputOrNull);

        } else
        {
            this.output = Collections.emptyList();

        }
        this.operationLog = operationLog;
        this.machineLog = machineLog;
    }

    /**
     * Returns the command line that belongs to this process.
     */
    public List<String> getCommandLine()
    {
        return commandLine;
    }

    /**
     * Returns the name of the command that belongs to this process.
     */
    public String getCommandName()
    {
        return commandName;
    }

    /**
     * Returns a number identifying the process that this result is for.
     */
    public int getProcessNumber()
    {
        return processNumber;
    }

    /**
     * Returns <code>true</code> if the output (<code>stdout</code> and <code>stderr</code>) is
     * available (note that even if it available it may still be empty).
     */
    public boolean isOutputAvailable()
    {
        return outputAvailable;
    }

    /**
     * Returns the output of the process (<code>stdout</code> and <code>stderr</code>). If it not
     * available (see {@link #isOutputAvailable()}, an empty list is returned.
     */
    public List<String> getOutput()
    {
        return output;
    }

    /**
     * Returns the exit value of the process, or {@link #NO_EXIT_VALUE}, if the value is not
     * available.
     */
    public int getExitValue()
    {
        return exitValue;
    }

    /**
     * Returns the message that was given when the process failed to startup. If the process didn't
     * fail on startup, an empty String is returned.
     */
    public String getStartupFailureMessage()
    {
        return startupFailureMessage;
    }

    /**
     * Returns <code>true</code>, if the process has completed successfully.
     */
    public boolean isOK()
    {
        return isProcessOK(exitValue);
    }

    /**
     * Returns <code>true</code> if the process has been run at all.
     */
    public boolean isRun()
    {
        return StringUtils.isBlank(startupFailureMessage);
    }

    /**
     * Returns <code>true</code> if the process has been terminated on the Operating System level.
     */
    public boolean isTerminated()
    {
        return ProcessResult.isProcessTerminated(getExitValue());
    }

    /**
     * Returns <code>true</code>, if the process has timed out on the Java level.
     */
    public boolean isTimedOut()
    {
        return ExecutionStatus.TIMED_OUT.equals(status);
    }

    /**
     * Returns <code>true</code>, if the Java thread that the process was running in got
     * interrupted.
     */
    public boolean isInterruped()
    {
        return ExecutionStatus.INTERRUPTED.equals(status);
    }

    /**
     * Logs the outcome of the process execution.
     */
    public void log()
    {

        if (isOK() == false)
        {
            logCommandLine(Level.WARN);
            logProcessExitValue(Level.WARN);
            logProcessOutput(Level.WARN);
        } else if (operationLog.isDebugEnabled())
        {
            logProcessExitValue(Level.DEBUG);
            logProcessOutput(Level.DEBUG);
        }
    }

    private void logCommandLine(final Level logLevel)
    {
        operationLog.log(logLevel, String.format("P%d-{%s} had command line: %s", processNumber,
                commandName, getCommandLine()));
    }

    private void logProcessExitValue(final Level logLevel)
    {
        if (isRun() == false)
        {
            operationLog.log(logLevel, String.format("P%d-{%s} process has not started up: '%s'.",
                    processNumber, commandName, startupFailureMessage));
        } else if (isTimedOut())
        {
            operationLog.log(logLevel, String.format("P%d-{%s} process has timed out.",
                    processNumber, commandName));
        } else if (isInterruped())
        {
            operationLog.log(logLevel, String.format("P%d-{%s} thread was interrupted.",
                    processNumber, commandName));
        } else if (isTerminated())
        {
            operationLog.log(logLevel, String.format("P%d-{%s} process was terminated.",
                    processNumber, commandName));
        } else
        {
            operationLog.log(logLevel, String.format(
                    "P%d-{%s} process returned with exit value %d.", processNumber, commandName,
                    getExitValue()));
        }
    }

    private void logProcessOutput(final Level logLevel)
    {
        assert logLevel != null;

        final List<String> processOutputLines = getOutput();
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