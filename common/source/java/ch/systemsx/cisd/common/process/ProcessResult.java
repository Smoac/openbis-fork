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
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Class that keeps around the result of running an Operating System process.
 * <p>
 * Since the process output can only ever be read once from a process, it need to be kept around if it is needed more
 * than once. This is what this class is good for.
 */
public final class ProcessResult
{
    private final Process processOrNull; // null if not started

    private final boolean hasBlocked;

    private final List<String> commandLine;

    private final String commandName;

    private final Logger operationLog;

    private final Logger machineLog;

    private List<String> outputLines;

    // process finished or was terminated after timeout
    public static ProcessResult create(Process process, List<String> commandLine, Logger operationLog, Logger machineLog)
    {
        return new ProcessResult(process, false, commandLine, operationLog, machineLog);
    }

    // process could not start at all
    public static ProcessResult createNotStarted(List<String> commandLine, Logger operationLog, Logger machineLog)
    {
        return new ProcessResult(null, false, commandLine, operationLog, machineLog);
    }

    // process started, but was blocked and could not be terminated, so we stopped waiting for it
    public static ProcessResult createWaitingInterrupted(Process process, List<String> commandLine,
            Logger operationLog, Logger machineLog)
    {
        return new ProcessResult(process, true, commandLine, operationLog, machineLog);
    }

    private ProcessResult(Process processOrNull, boolean hasBlocked, List<String> commandLine, Logger operationLog,
            Logger machineLog)
    {
        this.commandLine = commandLine;
        this.commandName = new File(commandLine.get(0)).getName();
        this.processOrNull = processOrNull;
        this.hasBlocked = hasBlocked;
        this.operationLog = operationLog;
        this.machineLog = machineLog;
        this.outputLines = null;
    }

    /**
     * Calls the {@link Process#destroy()} method which explicitly closes some file handles.
     * <p>
     * <i>Note that one must not call {#link {@link #getProcessOutput()} for the first time after this method has been
     * called.</i>
     * <p>
     * Whether it is necessary to call this method depends on the JRE. For some JREs there occur {@link IOException}s
     * with code 24 ("Too many open files") when running processes with high frequency without calling this method.
     */
    public void destroyProcess()
    {
        if (processOrNull != null)
        {
            processOrNull.destroy();
        }
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
     * Returns the lines of the process output.
     */
    public List<String> getProcessOutput()
    {
        if (outputLines == null)
        {
            outputLines = ProcessExecutionHelper.readProcessOutputLines(processOrNull, machineLog);
        }
        return outputLines;
    }

    public int exitValue()
    {
        if (hasBlocked == false && processOrNull != null)
        {
            return processOrNull.exitValue();
        } else
        {
            return ProcessExecutionHelper.NO_EXIT_VALUE;
        }
    }

    public boolean isOK()
    {
        return exitValue() == ProcessExecutionHelper.EXIT_VALUE_OK;
    }

    /**
     * Returns <code>true</code> if the process has been run at all.
     */
    public boolean isRun()
    {
        return processOrNull != null;
    }

    /**
     * Returns <code>true</code> if the process could not been terminated after the timeout and we stopped waiting for
     * it.
     */
    public boolean hasBlocked()
    {
        return hasBlocked;
    }

    /**
     * Returns <code>true</code> if the process has been terminated on the Operating System level.
     */
    public boolean isTerminated()
    {
        return ProcessExecutionHelper.isProcessTerminated(exitValue());
    }

    public void log()
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

    private void logProcessExitValue(final Level logLevel)
    {
        if (isRun() == false)
        {
            operationLog.log(logLevel, String.format("[%s] process could not be run.", commandName));
        } else if (isTerminated())
        {
            operationLog.log(logLevel, String.format("[%s] process was destroyed.", commandName));
        } else if (hasBlocked())
        {
            operationLog.log(logLevel, String.format("[%s] process has blocked and could not be destroyed.",
                    commandName));
        } else
        {
            operationLog.log(logLevel, String.format("[%s] process returned with exit value %d.", commandName,
                    exitValue()));
        }
    }

    private void logProcessOutput(final Level logLevel)
    {
        assert logLevel != null;

        final List<String> processOutputLines = getProcessOutput();
        if (processOutputLines.size() == 0)
        {
            return;
        }
        machineLog.log(logLevel, String.format("[%s] output:", commandName));
        for (String ln : processOutputLines)
        {
            if (ln.trim().length() > 0)
            {
                machineLog.log(logLevel, String.format("\"%s\"", ln));
            }
        }
    }

}