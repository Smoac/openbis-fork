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

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Class that keeps around the result of running an Operating System process.
 * <p>
 * Since the process output can only ever be read once from a process, it need to be kept around if
 * it is needed more than once. This is what this class is good for.
 * </p>
 */
public final class ProcessResult
{
    private final boolean hasBlocked;

    private final List<String> commandLine;

    private final String commandName;

    private final Logger operationLog;

    private final Logger machineLog;

    private final int exitValue;

    private final List<String> outputLines;

    private final boolean run;

    /**
     * Creates a <code>ProcessResult</code> for a process which normally terminates.
     */
    public final static ProcessResult create(final Process process, final List<String> commandLine,
            final Logger operationLog, final Logger machineLog, final List<String> outputLines)
    {
        return new ProcessResult(process, false, commandLine, operationLog, machineLog, outputLines);
    }

    /**
     * Creates a <code>ProcessResult</code> for a process which did not start at all.
     */
    public static ProcessResult createNotStarted(final List<String> commandLine,
            final Logger operationLog, final Logger machineLog)
    {
        return new ProcessResult(null, false, commandLine, operationLog, machineLog, Collections
                .<String> emptyList());
    }

    /**
     * Creates a <code>ProcessResult</code> for a process which blocked and could not be
     * terminated. So we stopped waiting for it.
     */
    public static ProcessResult createWaitingInterrupted(final Process process,
            final List<String> commandLine, final Logger operationLog, final Logger machineLog,
            final List<String> outputLines)
    {
        return new ProcessResult(process, true, commandLine, operationLog, machineLog, outputLines);
    }

    private ProcessResult(final Process processOrNull, final boolean hasBlocked,
            final List<String> commandLine, final Logger operationLog, final Logger machineLog,
            final List<String> outputLines)
    {
        this.commandLine = commandLine;
        this.commandName = new File(commandLine.get(0)).getName();
        this.hasBlocked = hasBlocked;
        this.operationLog = operationLog;
        this.machineLog = machineLog;
        this.exitValue = getExitValue(processOrNull, hasBlocked);
        this.outputLines = outputLines;
        this.run = processOrNull != null;
    }

    private final static int getExitValue(final Process processOrNull, final boolean hasBlocked)
    {
        if (processOrNull != null && hasBlocked == false)
        {
            return processOrNull.exitValue();
        }
        return ProcessExecutionHelper.NO_EXIT_VALUE;
    }

    /**
     * Returns the command line that belongs to this process.
     */
    public final List<String> getCommandLine()
    {
        return commandLine;
    }

    /**
     * Returns the name of the command that belongs to this process.
     */
    public final String getCommandName()
    {
        return commandName;
    }

    /**
     * Returns the lines of the process output.
     */
    public final List<String> getProcessOutput()
    {
        return outputLines;
    }

    public final int exitValue()
    {
        return exitValue;
    }

    public final boolean isOK()
    {
        return exitValue() == ProcessExecutionHelper.EXIT_VALUE_OK;
    }

    /**
     * Returns <code>true</code> if the process has been run at all.
     */
    public final boolean isRun()
    {
        return run;
    }

    /**
     * Returns <code>true</code> if the process could not been terminated after the timeout and we
     * stopped waiting for it.
     */
    public final boolean hasBlocked()
    {
        return hasBlocked;
    }

    /**
     * Returns <code>true</code> if the process has been terminated on the <i>Operating System</i>
     * level.
     */
    public final boolean isTerminated()
    {
        return ProcessExecutionHelper.isProcessTerminated(exitValue());
    }

    public final void log()
    {
        if (isOK() == false)
        {
            logProcessExitValue(Level.WARN);
            logProcessOutput(Level.WARN);
        } else if (operationLog.isDebugEnabled())
        {
            logProcessExitValue(Level.DEBUG);
            logProcessOutput(Level.DEBUG);
        }
    }

    private final void logProcessExitValue(final Level logLevel)
    {
        if (isRun() == false)
        {
            operationLog
                    .log(logLevel, String.format("[%s] process could not be run.", commandName));
        } else if (isTerminated())
        {
            operationLog.log(logLevel, String.format("[%s] process was destroyed.", commandName));
        } else if (hasBlocked())
        {
            operationLog.log(logLevel, String.format(
                    "[%s] process has blocked and could not be destroyed.", commandName));
        } else
        {
            operationLog.log(logLevel, String.format("[%s] process returned with exit value %d.",
                    commandName, exitValue()));
        }
    }

    private final void logProcessOutput(final Level logLevel)
    {
        assert logLevel != null;

        final List<String> processOutputLines = getProcessOutput();
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