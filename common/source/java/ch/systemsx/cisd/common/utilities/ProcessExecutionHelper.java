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

package ch.systemsx.cisd.common.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Utility to execute a command from a command line and log all events.
 * 
 * @author Tomasz Pylak
 * @author Bernd Rinn
 */
public class ProcessExecutionHelper
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

    /**
     * Class that keeps around the result of running an Operating System process.
     * <p>
     * Since the process output can only ever be read once from a process, it need to be kept around if it is needed
     * more than once. This is what this class is good for.
     */
    public static class ProcessResult
    {
        private final Process processOrNull;

        private final List<String> commandLine;

        private final String commandName;

        private final Logger operationLog;

        private final Logger machineLog;

        private List<String> outputLines;

        private ProcessResult(Process processOrNull, List<String> commandLine, Logger operationLog, Logger machineLog)
        {
            this.commandLine = commandLine;
            this.commandName = new File(commandLine.get(0)).getName();
            this.processOrNull = processOrNull;
            this.operationLog = operationLog;
            this.machineLog = machineLog;
            this.outputLines = null;
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
                outputLines = readProcessOutputLines(processOrNull, machineLog);
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
                return NO_EXIT_VALUE;
            }
        }

        public boolean isOK()
        {
            return exitValue() == EXIT_VALUE_OK;
        }

        /**
         * Returns <code>true</code> if the process has been run at all.
         */
        public boolean isRun()
        {
            return exitValue() != NO_EXIT_VALUE;
        }

        /**
         * Returns <code>true</code> if the process has been terminated on the Operating System level.
         */
        public boolean isTerminated()
        {
            return isProcessTerminated(exitValue());
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

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param commandLine The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code> otherwise.
     */
    public static boolean runAndLog(List<String> commandLine, Logger operationLog, Logger machineLog)
    {
        return new ProcessExecutionHelper(operationLog, machineLog).run(commandLine, 0L);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param commandLine The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return The process result.
     */
    public static ProcessResult run(List<String> commandLine, Logger operationLog, Logger machineLog)
    {
        return new ProcessExecutionHelper(operationLog, machineLog).tryRun(commandLine, 0L);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli seconds. If the process is
     *            not finished after that time, it will be terminated by a watch dog.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code> otherwise.
     */
    public static boolean runAndLog(List<String> cmd, long millisToWaitForCompletion, Logger operationLog, Logger machineLog)
    {
        return new ProcessExecutionHelper(operationLog, machineLog).run(cmd, millisToWaitForCompletion);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli seconds. If the process is
     *            not finished after that time, it will be terminated by a watch dog.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return The process result.
     */
    public static ProcessResult run(List<String> cmd, long millisToWaitForCompletion, Logger operationLog,
            Logger machineLog)
    {
        return new ProcessExecutionHelper(operationLog, machineLog).tryRun(cmd, millisToWaitForCompletion);
    }

    /**
     * Returns <code>true</code> if the <var>exitValue</var> indicates that the process has been terminated on the
     * Operating System level.
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
     * Returns the stdout (and stderr if {@link ProcessBuilder#redirectErrorStream(boolean)} has been called with
     * <code>true</code>).
     */
    public static List<String> readProcessOutputLines(Process processOrNull, Logger machineLog)
    {
        final List<String> processOutput = new ArrayList<String>();
        if (processOrNull == null)
        {
            return processOutput;
        }
        final BufferedReader reader = new BufferedReader(new InputStreamReader(processOrNull.getInputStream()));
        try
        {
            String ln;
            while ((ln = reader.readLine()) != null)
            {
                processOutput.add(ln);
            }
        } catch (IOException e)
        {
            machineLog.warn(String.format("IOException when reading stdout, msg='%s'.", e.getMessage()));
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
        return processOutput;
    }

    //
    // Implementation
    //

    private ProcessExecutionHelper(Logger operationLog, Logger machineLog)
    {
        this.operationLog = operationLog;
        this.machineLog = machineLog;
    }

    private ProcessResult tryRun(List<String> commandLine, long millisoWaitForCompletion)
    {
        final ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
        processBuilder.redirectErrorStream(true);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Executing command: " + commandLine);
        }
        final Process process;
        try
        {
            process = processBuilder.start();
        } catch (IOException ex)
        {
            machineLog.error(String.format("Cannot execute executable %s", commandLine), ex);
            return new ProcessResult(null, commandLine, operationLog, machineLog);
        }

        final Timer watchDogOrNull = tryCreateWatchDog(process, millisoWaitForCompletion, commandLine.get(0));

        try
        {
            process.waitFor();
        } catch (InterruptedException ex)
        {
            machineLog.error(String.format("Execution of %s interupted", process), ex);
        } finally
        {
            if (watchDogOrNull != null)
            {
                watchDogOrNull.cancel();
            }
        }
        return new ProcessResult(process, commandLine, operationLog, machineLog);
    }

    private Timer tryCreateWatchDog(final Process process, final long millisToWaitForCompletion, String commandForLog)
    {
        final Timer watchDogOrNull;
        if (millisToWaitForCompletion > 0L)
        {
            watchDogOrNull = new Timer(String.format("Watch Dog [%s]", commandForLog));
            watchDogOrNull.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        operationLog.warn(String.format("Destroy process since it didn't finish in %d milli seconds",
                                millisToWaitForCompletion));
                        process.destroy();
                    }
                }, millisToWaitForCompletion);
        } else
        {
            watchDogOrNull = null;
        }
        return watchDogOrNull;
    }

    private boolean run(List<String> cmd, long millisToWaitForCompletion)
    {
        final ProcessResult result = tryRun(cmd, millisToWaitForCompletion);
        result.log();
        return result.isOK();
    }

    public static void logProcessExecution(String commandName, int exitValue, List<String> processOutput,
            Logger operationLog, Logger machineLog)
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

    private static void logProcessExitValue(final Level logLevel, Logger operationLog, String commandName, int exitValue)
    {
        assert logLevel != null;
        assert operationLog != null;
        assert commandName != null;

        if (isProcessTerminated(exitValue))
        {
            operationLog.log(logLevel, String.format("[%s] process was destroyed.", commandName));
        } else
        {
            operationLog.log(logLevel, String.format("[%s] process returned with exit value %d.", commandName,
                    exitValue));
        }
    }

    private static void logProcessOutput(final Level logLevel, Logger machineLog, String commandName,
            List<String> processOutputLines)
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
        for (String ln : processOutputLines)
        {
            if (ln.trim().length() > 0)
            {
                machineLog.log(logLevel, String.format("\"%s\"", ln));
            }
        }
    }
}
