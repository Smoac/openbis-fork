package ch.systemsx.cisd.common.utilities;

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
    private final Process processOrNull;

    private final List<String> commandLine;

    private final String commandName;

    private final Logger operationLog;

    private final Logger machineLog;

    private List<String> outputLines;

    ProcessResult(Process processOrNull, List<String> commandLine, Logger operationLog, Logger machineLog)
    {
        this.commandLine = commandLine;
        this.commandName = new File(commandLine.get(0)).getName();
        this.processOrNull = processOrNull;
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
        if (processOrNull != null)
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
        return exitValue() != ProcessExecutionHelper.NO_EXIT_VALUE;
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