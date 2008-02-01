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
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param commandLine The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code> otherwise.
     */
    public static boolean runAndLog(List<String> commandLine, Logger operationLog, Logger machineLog)
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
    public static ProcessResult run(List<String> commandLine, Logger operationLog, Logger machineLog)
    {
        return new ProcessExecutionHelper(operationLog, machineLog).run(commandLine, 0L);
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
    public static boolean runAndLog(List<String> cmd, long millisToWaitForCompletion, Logger operationLog,
            Logger machineLog)
    {
        return new ProcessExecutionHelper(operationLog, machineLog).runAndLog(cmd, millisToWaitForCompletion);
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
        return new ProcessExecutionHelper(operationLog, machineLog).run(cmd, millisToWaitForCompletion);
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

    // access to this class should be synchronized on itself
    private static class ProcessExitSatus
    {
        private boolean isTerminated;

        private boolean isInterrupted;

        public ProcessExitSatus(boolean isTerminated)
        {
            this.isTerminated = isTerminated;
            this.isInterrupted = false;
        }

        public void setStopped()
        {
            isTerminated = true;
        }

        public boolean isRunning()
        {
            return isTerminated == false;
        }

        public void setInterruptedAfterTimeout()
        {
            setStopped();
            isInterrupted = true;
        }

        public boolean isInterruptedAfterTimeout()
        {
            return isInterrupted;
        }
    }

    private ProcessResult run(List<String> commandLine, long millisoWaitForCompletion)
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
            return createNotStartedResult(commandLine, ex);
        }

        ProcessExitSatus exitStatus = new ProcessExitSatus(false);
        final Timer watchDogOrNull =
                tryCreateWatchDog(process, millisoWaitForCompletion, commandLine.get(0), exitStatus);
        boolean isInterrupted = false;
        try
        {
            Thread.interrupted(); // clear 'interrupted' status
            process.waitFor();
            synchronized (exitStatus)
            {
                exitStatus.setStopped(); // mark, that the process terminated and does not block anymore
            }
        } catch (InterruptedException ex)
        {
            logInterruption(process, exitStatus, ex);
            isInterrupted = true;
        } finally
        {
            if (watchDogOrNull != null)
            {
                watchDogOrNull.cancel();
            }
        }
        return createResult(commandLine, process, isInterrupted);
    }

    private ProcessResult createNotStartedResult(List<String> commandLine, IOException ex)
    {
        machineLog.error(String.format("Cannot execute executable %s", commandLine), ex);
        return ProcessResult.createNotStarted(commandLine, operationLog, machineLog);
    }

    private ProcessResult createResult(List<String> commandLine, final Process process, boolean isInterrupted)
    {
        if (isInterrupted)
        {
            return ProcessResult.createWaitingInterrupted(process, commandLine, operationLog, machineLog);
        } else
        {
            return ProcessResult.create(process, commandLine, operationLog, machineLog);
        }
    }

    private void logInterruption(final Process process, ProcessExitSatus terminationStatus, InterruptedException ex)
    {
        if (terminationStatus.isInterruptedAfterTimeout() == false) // have NOT been stopped by the watchDog
        {
            machineLog.error(String.format("Execution of %s interrupted", process), ex);
        } else
        {
            operationLog.warn(String.format("Execution of %s interrupted after timeout", process));
        }
    }

    /*
     * isTerminated is passed by reference. Access to it should be synchronized on process variable
     */
    private Timer tryCreateWatchDog(final Process process, final long millisToWaitForCompletion, String commandForLog,
            final ProcessExitSatus exitStatus)
    {
        final Timer watchDogOrNull;
        if (millisToWaitForCompletion > 0L)
        {
            final Thread processThread = Thread.currentThread();
            watchDogOrNull = new Timer(String.format("Watch Dog [%s]", commandForLog));
            watchDogOrNull.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        operationLog.warn(String.format("Destroy process since it didn't finish in %d milli seconds",
                                millisToWaitForCompletion));
                        process.destroy();
                        sleep(millisToWaitForCompletion / 2); // allow the process to termiante normally
                        synchronized (exitStatus)
                        {
                            // Interrupt waiting for the process termination if we still wait.
                            if (exitStatus.isRunning() && processThread.isInterrupted() == false)
                            {
                                exitStatus.setInterruptedAfterTimeout();
                                operationLog.info(String.format(
                                        "Interrupting waiting for the process %s by the watchDog", process));
                                // stop waiting for the process. We need this, because sometimes the child process,
                                // which is an external program, gets stuck and cannot be destroyed. We do not want the
                                // whole system to hang because of that.
                                processThread.interrupt();
                            }
                        }
                    }

                    private void sleep(final long millisToWait)
                    {
                        try
                        {
                            Thread.sleep(millisToWait);
                        } catch (InterruptedException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }, millisToWaitForCompletion);
        } else
        {
            watchDogOrNull = null;
        }
        return watchDogOrNull;
    }

    private boolean runAndLog(List<String> cmd, long millisToWaitForCompletion)
    {
        final ProcessResult result = run(cmd, millisToWaitForCompletion);
        result.log();
        result.destroyProcess();
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
