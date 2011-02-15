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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.namedthread.NamedCallable;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.ExecutionResult;
import ch.systemsx.cisd.common.concurrent.ExecutionStatus;
import ch.systemsx.cisd.common.utilities.ITerminable;

/**
 * Utility to execute a command from a command line and log all events.
 * 
 * @author Bernd Rinn
 */
public final class ProcessExecutionHelper
{

    private static final int BUFFER_SIZE = 4096;

    /**
     * Strategy on whether to read the process output or not.
     */
    public enum OutputReadingStrategy
    {
        /** Never read the output. */
        NEVER,

        /**
         * Hint that the output of the process is only requested if the process failed in some way. <br/>
         * <i>Note that the implementation may read the output anyway.</i>
         */
        ON_ERROR,

        /** Always read the output. */
        ALWAYS;
    }

    /**
     * Role for handling the process I/O.
     */
    public interface IProcessIOHandler
    {
        /**
         * Method that gets the process' <code>stdin</code>, <code>stdout</code> and
         * <code>stderr</code> and is expected to handlt the I/O of the process.
         */
        public void handle(OutputStream stdin, InputStream stdout, InputStream stderr)
                throws IOException;
    }

    /**
     * Record to store process and its output.
     */
    private final class ProcessRecord
    {
        private final Process process;

        private final List<String> processTextOutput;

        private final List<String> processErrorOutput;

        private final ByteArrayOutputStream processBinaryOutput;

        ProcessRecord(final Process process)
        {
            this.process = process;
            if (binaryOutput)
            {
                this.processTextOutput = null;
                this.processErrorOutput = new ArrayList<String>();
                this.processBinaryOutput = new ByteArrayOutputStream();
            } else
            {
                this.processTextOutput = new ArrayList<String>();
                this.processErrorOutput = null;
                this.processBinaryOutput = null;
            }
        }

        Process getProcess()
        {
            return process;
        }

        List<String> getTextProcessOutput()
        {
            return processTextOutput;
        }

        ByteArrayOutputStream getBinaryProcessOutput()
        {
            return processBinaryOutput;
        }

        List<String> getErrorProcessOutput()
        {
            return processErrorOutput;
        }
    }

    /**
     * The default strategy for when to read the process output.
     */
    public static final OutputReadingStrategy DEFAULT_OUTPUT_READING_STRATEGY =
            OutputReadingStrategy.ALWAYS;

    /** Corresponds to a short timeout of 1/10 s. */
    private static final long SHORT_TIMEOUT = 100;

    /** Corresponds to a short timeout of 1/100 s. */
    private static final long PAUSE_MILLIS = 10;

    /** The executor service handling the threads that OS processes are spawned in. */
    private static final ExecutorService executor = new NamingThreadPoolExecutor("osproc")
            .corePoolSize(10).daemonize();

    /** The counter to draw the <var>processNumber</var> from. */
    private static final AtomicInteger processCounter = new AtomicInteger();

    private final Logger operationLog;

    private final Logger machineLog;

    /** Read-only! */
    private final List<String> commandLine;

    private final long millisToWaitForCompletion;

    private final OutputReadingStrategy outputReadingStrategy;

    private final boolean binaryOutput;

    private final IProcessIOHandler processIOHandlerOrNull;

    /** The number used in thread names to distinguish the process. */
    private final int processNumber;

    /** The name of the thread that the {@link ProcessExecutionHelper} is instantiated from. */
    private final String callingThreadName;

    // Use this reference to make sure the process is as dead as you can get it to be.
    private final AtomicReference<ProcessRecord> processWrapper;

    /**
     * Log the <var>result</var> and return the {@link ProcessResult#isOK()} flag.
     * 
     * @throws InterruptedExceptionUnchecked If the <var>result</var> has the
     *             {@link ProcessResult#isInterruped()} flag set.
     */
    public static final boolean log(ProcessResult result) throws InterruptedExceptionUnchecked
    {
        result.log();
        if (result.isInterruped())
        {
            throw new InterruptedExceptionUnchecked();
        }
        return result.isOK();
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param stopOnInterrupt If <code>true</code>, an {@link InterruptedExceptionUnchecked} will be
     *            thrown if the thread got interrupted, otherwise, the
     *            {@link ProcessResult#isInterruped()} flag will be set.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and
     *             <var>stopOnInterrupt</var> is <code>true</code>.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, boolean stopOnInterrupt) throws InterruptedExceptionUnchecked
    {
        return new ProcessExecutionHelper(cmd, ConcurrencyUtilities.NO_TIMEOUT,
                DEFAULT_OUTPUT_READING_STRATEGY, null, false, operationLog, machineLog)
                .run(stopOnInterrupt);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog) throws InterruptedExceptionUnchecked
    {
        return run(cmd, operationLog, machineLog, true);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code>
     *         otherwise.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    public static boolean runAndLog(final List<String> cmd, final Logger operationLog,
            final Logger machineLog) throws InterruptedExceptionUnchecked
    {
        return log(run(cmd, operationLog, machineLog, false));
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in
     *            milli-seconds. If the process is not finished after that time, it will be
     *            terminated by a watch dog. Use {@link ConcurrencyUtilities#NO_TIMEOUT} if you do
     *            not want any timeout.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code>
     *         otherwise.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    public static boolean runAndLog(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion)
            throws InterruptedExceptionUnchecked
    {
        return log(run(cmd, operationLog, machineLog, false, millisToWaitForCompletion));
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param stopOnInterrupt If <code>true</code>, an {@link InterruptedExceptionUnchecked} will be
     *            thrown if the thread got interrupted, otherwise, the
     *            {@link ProcessResult#isInterruped()} flag will be set.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli
     *            seconds. If the process is not finished after that time, it will be terminated by
     *            a watch dog.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and
     *             <var>stopOnInterrupt</var> is <code>true</code>.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final boolean stopOnInterrupt,
            final long millisToWaitForCompletion) throws InterruptedExceptionUnchecked
    {
        return new ProcessExecutionHelper(cmd, millisToWaitForCompletion,
                DEFAULT_OUTPUT_READING_STRATEGY, null, false, operationLog, machineLog)
                .run(stopOnInterrupt);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli
     *            seconds. If the process is not finished after that time, it will be terminated by
     *            a watch dog.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion)
            throws InterruptedExceptionUnchecked
    {
        return run(cmd, operationLog, machineLog, true, millisToWaitForCompletion);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in
     *            milli-seconds. If the process is not finished after that time, it will be
     *            terminated by a watch dog.
     * @param outputReadingStrategy The strategy for when to read the output (both
     *            <code>stdout</code> and <code>sterr</code>) of the process.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code>
     *         otherwise.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted.
     */
    public static boolean runAndLog(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion,
            final OutputReadingStrategy outputReadingStrategy) throws InterruptedExceptionUnchecked
    {
        return log(run(cmd, operationLog, machineLog, millisToWaitForCompletion,
                outputReadingStrategy, false));
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli
     *            seconds. If the process is not finished after that time, it will be terminated by
     *            a watch dog.
     * @param outputReadingStrategy The strategy for when to read the output (both
     *            <code>stdout</code> and <code>sterr</code>) of the process.
     * @param stopOnInterrupt If <code>true</code>, throw a {@link InterruptedExceptionUnchecked} if
     *            the thread gets interrupted while waiting on the future.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and
     *             <var>stopOnInterrupt</var> is <code>true</code>.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion,
            final OutputReadingStrategy outputReadingStrategy, final boolean stopOnInterrupt)
            throws InterruptedExceptionUnchecked
    {
        return new ProcessExecutionHelper(cmd, millisToWaitForCompletion, outputReadingStrategy,
                null, false, operationLog, machineLog).run(stopOnInterrupt);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli
     *            seconds. If the process is not finished after that time, it will be terminated by
     *            a watch dog.
     * @param outputReadingStrategy The strategy for when to read the output (both
     *            <code>stdout</code> and <code>sterr</code>) of the process.
     * @param binaryOutput If <code>true</code>, the process is expected to produce binary output on
     *            <code>stdout</code>.
     * @param stopOnInterrupt If <code>true</code>, throw a {@link InterruptedExceptionUnchecked} if
     *            the thread gets interrupted while waiting on the future.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and
     *             <var>stopOnInterrupt</var> is <code>true</code>.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion,
            final OutputReadingStrategy outputReadingStrategy, final boolean binaryOutput,
            final boolean stopOnInterrupt) throws InterruptedExceptionUnchecked
    {
        return new ProcessExecutionHelper(cmd, millisToWaitForCompletion, outputReadingStrategy,
                null, binaryOutput, operationLog, machineLog).run(stopOnInterrupt);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli
     *            seconds. If the process is not finished after that time, it will be terminated by
     *            a watch dog.
     * @param outputReadingStrategy The strategy for when to read the output (both
     *            <code>stdout</code> and <code>sterr</code>) of the process.
     * @param processIOHandler The handler in charge of dealing with the process input and output.
     *            Beware that if the process reaches the I/O buffer limit and this handler doesn't
     *            read the output, the process may hang indefinitely.
     * @param stopOnInterrupt If <code>true</code>, throw a {@link InterruptedExceptionUnchecked} if
     *            the thread gets interrupted while waiting on the future.
     * @return The process result.
     * @throws InterruptedExceptionUnchecked If the thread got interrupted and
     *             <var>stopOnInterrupt</var> is <code>true</code>.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion,
            final OutputReadingStrategy outputReadingStrategy,
            final IProcessIOHandler processIOHandler, final boolean stopOnInterrupt)
            throws InterruptedExceptionUnchecked
    {
        return new ProcessExecutionHelper(cmd, millisToWaitForCompletion, outputReadingStrategy,
                processIOHandler, false, operationLog, machineLog).run(stopOnInterrupt);
    }

    /** Handler to a running process. Allows to wait for the result and stop the process. */
    public interface IProcessHandler extends ITerminable
    {
        /**
         * Blocks until the result of the process is available and returns it.
         * 
         * @throws InterruptedExceptionUnchecked If the thread got interrupted.
         */
        ProcessResult getResult() throws InterruptedExceptionUnchecked;

        /**
         * Blocks until the result of the process is available and returns it, or returns a time out
         * if the result is not available after <var>millisToWaitForCompletion</var> milli-seconds.
         * 
         * @throws InterruptedExceptionUnchecked If the thread got interrupted.
         */
        ProcessResult getResult(final long millisToWaitForCompletion);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>. Does not block waiting for a
     * result.
     * 
     * @param cmd The command line to run.
     * @param outputReadingStrategy The strategy about when to read the output from the process.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return The handler which allows to wait for the result or terminate the process.
     */
    public static IProcessHandler runUnblocking(final List<String> cmd,
            final OutputReadingStrategy outputReadingStrategy, Logger operationLog,
            final Logger machineLog)
    {
        return new ProcessExecutionHelper(cmd, ConcurrencyUtilities.NO_TIMEOUT,
                outputReadingStrategy, null, false, operationLog, machineLog).runUnblocking();
    }

    /**
     * Returns the name of the command represented by <var>commandLine</var>.
     */
    final static String getCommandName(final List<String> commandLine)
    {
        return new File(commandLine.get(0)).getName();
    }

    /**
     * Returns the command represented by <var>commandLine</var>.
     */
    private final static String getCommand(final List<String> commandLine)
    {
        return StringUtils.join(commandLine, ' ');
    }

    //
    // Implementation
    //

    private final int getExitValue(final Process process)
    {
        try
        {
            return process.exitValue();
        } catch (final IllegalThreadStateException ex)
        {
            return ProcessResult.NO_EXIT_VALUE;
        }
    }

    /**
     * Reads the <code>stdout</code> and <code>stderr</code> of <var>process</var>.
     */
    private final void readProcessOutput(final ProcessRecord processRecord, final boolean discard)
    {
        readProcessOutput(processRecord, -1, discard);
    }

    /**
     * Reads the <code>stdout</code> and <code>stderr</code> of <var>process</var>. If
     * <code>maxBytes > 0</code>, read not more than so many bytes.
     */
    private final void readProcessOutput(final ProcessRecord processRecord, final long maxBytes,
            final boolean discard)
    {
        assert processRecord != null;
        assert machineLog != null;

        final Process process = processRecord.getProcess();
        if (binaryOutput)
        {
            try
            {
                copy(process, processRecord, maxBytes, discard);
            } catch (final IOException e)
            {
                machineLog.warn(String.format("IOException when reading stdout/stderr, msg='%s'.",
                        e.getMessage()));
            }
            final List<String> errorOutput = processRecord.getErrorProcessOutput();
            final BufferedReader errorReader =
                    new BufferedReader(new InputStreamReader(process.getErrorStream()));
            readProcessOutputLines(errorOutput, errorReader, discard);
        } else
        {
            final List<String> processOutput = processRecord.getTextProcessOutput();
            final BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            readProcessOutputLines(processOutput, reader, discard);
        }
    }

    protected void copy(final Process process, final ProcessRecord processRecord,
            final long maxBytes, final boolean discard) throws IOException
    {
        final InputStream input = process.getInputStream();
        final OutputStream output = processRecord.getBinaryProcessOutput();
        final byte[] buffer = new byte[BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while ((maxBytes <= 0 || count < maxBytes) && input.available() > 0
                && -1 != (n = input.read(buffer)))
        {
            if (discard == false)
            {
                output.write(buffer, 0, n);
            }
            count += n;
        }
    }

    /**
     * Returns the <code>stdout</code> and <code>stderr</code> of the <var>process</var> in
     * <var>processRecord.getProcessOutput()</var>.
     */
    private final void readProcessOutputLines(final List<String> processOutput,
            final BufferedReader reader, final boolean discard)
    {
        assert processOutput != null;
        assert reader != null;
        assert machineLog != null;

        try
        {
            while (reader.ready())
            {
                final String line = reader.readLine();
                if (line == null)
                {
                    break;
                }
                if (discard == false)
                {
                    processOutput.add(line);
                }
            }
        } catch (final IOException e)
        {
            machineLog.warn(String.format("IOException when reading stdout/stderr, msg='%s'.",
                    e.getMessage()));
        }
    }

    /**
     * Returns <code>true</code>, if an I/O handler for the process I/O is available.
     */
    private boolean hasIOHandler()
    {
        return processIOHandlerOrNull != null;
    }

    /**
     * The class that performs the actual calling and interaction with the Operating System process.
     * Since we observed hangs of several process-related methods we call all of this in a separate
     * thread.
     */
    private class ProcessRunner implements NamedCallable<ProcessResult>
    {
        private final Process launch() throws IOException
        {
            final ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
            if (binaryOutput == false && processIOHandlerOrNull == null)
            {
                processBuilder.redirectErrorStream(true);
            }
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Running command: " + getCommand(commandLine));
            }
            return processBuilder.start();
        }

        //
        // NamedCallable
        //

        public final ProcessResult call() throws Exception
        {
            try
            {
                final Process process = launch();
                try
                {
                    ProcessRecord processRecord = new ProcessRecord(process);
                    processWrapper.set(processRecord);

                    int exitValue = ProcessResult.NO_EXIT_VALUE;
                    if (hasIOHandler() == false)
                    {
                        final boolean discardOutput =
                                (outputReadingStrategy == OutputReadingStrategy.NEVER);
                        while (exitValue == ProcessResult.NO_EXIT_VALUE)
                        {
                            readProcessOutput(processRecord, discardOutput);
                            exitValue = getExitValue(process);
                            if (exitValue == ProcessResult.NO_EXIT_VALUE)
                            {
                                ConcurrencyUtilities.sleep(PAUSE_MILLIS);
                            }
                        }
                        processWrapper.set(null);
                        readProcessOutput(processRecord, discardOutput);
                        if (binaryOutput)
                        {
                            return new ProcessResult(commandLine, processNumber,
                                    ExecutionStatus.COMPLETE, "", exitValue, processRecord
                                            .getBinaryProcessOutput().toByteArray(),
                                    processRecord.getErrorProcessOutput(), operationLog, machineLog);
                        } else
                        {
                            return new ProcessResult(commandLine, processNumber,
                                    ExecutionStatus.COMPLETE, "", exitValue,
                                    processRecord.getTextProcessOutput(), operationLog, machineLog);
                        }
                    } else
                    {
                        final Future<?> future = executor.submit(new Runnable()
                            {
                                public void run()
                                {
                                    try
                                    {
                                        processIOHandlerOrNull.handle(process.getOutputStream(),
                                                process.getInputStream(), process.getErrorStream());
                                    } catch (IOException ex)
                                    {
                                        throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                                    }
                                }
                            });
                        exitValue = process.waitFor();
                        processWrapper.set(null);
                        final ExecutionResult<?> result =
                                ConcurrencyUtilities.getResult(future, SHORT_TIMEOUT);
                        switch (result.getStatus())
                        {
                            case COMPLETE:
                                break;
                            case EXCEPTION:
                                final Throwable th = result.tryGetException();
                                final Throwable cause =
                                        (th == null) ? new RuntimeException("Unknown exception.")
                                                : (th instanceof Error) ? (Error) th
                                                        : CheckedExceptionTunnel
                                                                .unwrapIfNecessary((Exception) th);
                                machineLog
                                        .warn(String
                                                .format("Exception when reading stdout/stderr, type='%s', msg='%s'.",
                                                        cause.getClass().getSimpleName(),
                                                        cause.getMessage()));
                                break;
                            case INTERRUPTED:
                                machineLog.warn("Interrupted when reading stdout/stderr.");
                                break;
                            case TIMED_OUT:
                                machineLog.warn("Timeout when reading stdout/stderr.");
                                break;
                        }
                        return new ProcessResult(commandLine, processNumber,
                                ExecutionStatus.COMPLETE, "", exitValue, null, operationLog,
                                machineLog);
                    }
                } finally
                {
                    IOUtils.closeQuietly(process.getErrorStream());
                    IOUtils.closeQuietly(process.getInputStream());
                    IOUtils.closeQuietly(process.getOutputStream());
                }
            } catch (final Exception ex)
            {
                machineLog
                        .error(String.format("Exception when launching: [%s, %s]", ex.getClass()
                                .getSimpleName(), StringUtils.defaultIfEmpty(ex.getMessage(),
                                "NO MESSAGE")));
                throw ex;
            }
        }

        public String getCallableName()
        {
            return callingThreadName + "::run-P" + processNumber + "-{"
                    + getCommandName(commandLine) + "}";
        }
    }

    /**
     * The class that performs the destruction of a process that has timed-out. We do this in a
     * separate thread because we have observed that, depending on the operating system and Java
     * version, processes can hang indefinitely on launching.
     */
    private final class ProcessKiller implements NamedCallable<ProcessResult>
    {
        private final ExecutionStatus status;

        private ProcessKiller(ExecutionStatus status)
        {
            this.status = status;
        }

        //
        // NamedCallable
        //

        public final ProcessResult call()
        {
            final ProcessRecord processRecord = processWrapper.getAndSet(null);
            if (processRecord != null)
            {
                final Process process = processRecord.getProcess();
                process.destroy(); // Note: this also closes the I/O streams.
                if (machineLog.isInfoEnabled())
                {
                    machineLog.info(String.format("Killed '%s'.", getCommand(commandLine)));
                }
                final int exitValue = getExitValue(processRecord.getProcess());
                if (binaryOutput)
                {
                    return new ProcessResult(commandLine, processNumber, status, "", exitValue,
                            processRecord.getBinaryProcessOutput().toByteArray(),
                            processRecord.getErrorProcessOutput(), operationLog, machineLog);
                } else
                {
                    return new ProcessResult(commandLine, processNumber, status, "", exitValue,
                            processRecord.getTextProcessOutput(), operationLog, machineLog);
                }
            } else
            {
                return null; // Value signals that the ProcessRunner got us.
            }
        }

        public final String getCallableName()
        {
            return callingThreadName + "::kill-P" + processNumber + "-{"
                    + getCommandName(commandLine) + "}";
        }
    }

    private ProcessExecutionHelper(final List<String> commandLine,
            final long millisToWaitForCompletion,
            final OutputReadingStrategy outputReadingStrategy,
            final IProcessIOHandler processIOHandlerOrNull, final boolean binaryOutput,
            final Logger operationLog, final Logger machineLog)
    {
        this.processNumber = processCounter.getAndIncrement();
        this.callingThreadName = Thread.currentThread().getName();
        this.operationLog = operationLog;
        this.machineLog = machineLog;
        // Backward compatibility. Do not remove this!!!
        if (millisToWaitForCompletion == ConcurrencyUtilities.IMMEDIATE_TIMEOUT)
        {
            this.millisToWaitForCompletion = ConcurrencyUtilities.NO_TIMEOUT;
        } else
        {
            this.millisToWaitForCompletion = millisToWaitForCompletion;
        }
        this.outputReadingStrategy = outputReadingStrategy;
        this.processIOHandlerOrNull = processIOHandlerOrNull;
        this.binaryOutput = binaryOutput;
        this.commandLine = Collections.unmodifiableList(commandLine);
        this.processWrapper = new AtomicReference<ProcessRecord>();
    }

    private final IProcessHandler runUnblocking()
    {
        final Future<ProcessResult> runnerFuture = launchProcessExecutor();
        return new IProcessHandler()
            {
                private final boolean stopOnInterruption = true;

                public boolean terminate()
                {
                    ExecutionResult<ProcessResult> executionResult =
                            killProcess(ExecutionStatus.TIMED_OUT, stopOnInterruption);
                    return (executionResult.tryGetResult() != null);
                }

                public ProcessResult getResult()
                {
                    return getProcessResult(stopOnInterruption, runnerFuture,
                            millisToWaitForCompletion);
                }

                public ProcessResult getResult(
                        @SuppressWarnings("hiding") final long millisToWaitForCompletion)
                {
                    return getProcessResult(stopOnInterruption, runnerFuture,
                            millisToWaitForCompletion);
                }
            };
    }

    private final ProcessResult run(final boolean stopOnInterrupt)
    {
        final Future<ProcessResult> runnerFuture = launchProcessExecutor();
        return getProcessResult(stopOnInterrupt, runnerFuture, millisToWaitForCompletion);
    }

    private ProcessResult getProcessResult(final boolean stopOnInterrupt,
            final Future<ProcessResult> runnerFuture, final long millisToWaitForCompletionOverride)
    {
        // when runUnblocking is used it is possible that we are hanging here while other thread
        // runs the killer. We will get COMPLETE status and null as the ProcessResult. We have to
        // change that status.
        ExecutionResult<ProcessResult> executionResult =
                getExecutionResult(runnerFuture, millisToWaitForCompletionOverride);
        if (executionResult.getStatus() != ExecutionStatus.COMPLETE)
        {
            executionResult = killProcess(executionResult.getStatus(), stopOnInterrupt);

            // If the process killer did not find anything to kill, then try to get the original
            // process result. We could have had a raise condition here.
            if (executionResult.tryGetResult() == null)
            {
                executionResult =
                        ConcurrencyUtilities.getResult(runnerFuture,
                                ConcurrencyUtilities.IMMEDIATE_TIMEOUT);
            }
        }
        checkStop(executionResult.getStatus(), stopOnInterrupt);
        final ProcessResult result = executionResult.tryGetResult();
        if (result != null)
        {
            return result;
        } else
        {
            ExecutionStatus status = executionResult.getStatus();
            if (status == ExecutionStatus.COMPLETE)
            {
                // see the note above about termination from other thread
                status = ExecutionStatus.INTERRUPTED;
            }
            if (binaryOutput)
            {
                return new ProcessResult(commandLine, processNumber, status,
                        tryGetStartupFailureMessage(executionResult.tryGetException()),
                        ProcessResult.NO_EXIT_VALUE, null, null, operationLog, machineLog);
            } else
            {
                return new ProcessResult(commandLine, processNumber, status,
                        tryGetStartupFailureMessage(executionResult.tryGetException()),
                        ProcessResult.NO_EXIT_VALUE, null, operationLog, machineLog);
            }
        }
    }

    private ExecutionResult<ProcessResult> killProcess(ExecutionStatus executionStatus,
            boolean stopOnInterrupt)
    {
        final Future<ProcessResult> killerFuture =
                executor.submit(new ProcessKiller(executionStatus));
        checkStop(executionStatus, stopOnInterrupt);
        return ConcurrencyUtilities.getResult(killerFuture, SHORT_TIMEOUT);
    }

    private static ExecutionResult<ProcessResult> getExecutionResult(
            final Future<ProcessResult> runnerFuture, long millisToWaitForCompletion)
    {
        return ConcurrencyUtilities.getResult(runnerFuture, millisToWaitForCompletion, false, null);
    }

    private Future<ProcessResult> launchProcessExecutor()
    {
        return executor.submit(new ProcessRunner());
    }

    private final static void checkStop(ExecutionStatus executionStatus, boolean stopOnInterrupt)
            throws InterruptedExceptionUnchecked
    {
        if (stopOnInterrupt && ExecutionStatus.INTERRUPTED.equals(executionStatus))
        {
            throw new InterruptedExceptionUnchecked();
        }
    }

    private final static String tryGetStartupFailureMessage(final Throwable throwableOrNull)
    {
        if (throwableOrNull != null && throwableOrNull instanceof IOException)
        {
            return throwableOrNull.getMessage();
        } else
        {
            return null;
        }
    }

}
