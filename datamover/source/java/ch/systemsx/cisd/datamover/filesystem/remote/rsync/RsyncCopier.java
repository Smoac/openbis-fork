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

package ch.systemsx.cisd.datamover.filesystem.remote.rsync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathCopier;
import ch.systemsx.cisd.datamover.filesystem.remote.rsync.RsyncVersionChecker.RsyncVersion;

/**
 * A class that encapsulates the <code>rsync</code> call for doing an archive copy.
 * 
 * @author Bernd Rinn
 */
public class RsyncCopier implements IPathCopier
{

    /** The maximal period to wait for the <code>rsync</code> list process to finish before killing it. */
    private static final int MAX_INACTIVITY_PERIOD_RSYNC_LIST = 30 * 1000;

    /**
     * The {@link Status} returned if the process was terminated by {@link Process#destroy()}.
     */
    protected static final Status TERMINATED_STATUS = new Status(StatusFlag.RETRIABLE_ERROR, "Process was terminated.");

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, RsyncCopier.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, RsyncCopier.class);

    private static final Status INTERRUPTED_STATUS = new Status(StatusFlag.RETRIABLE_ERROR, "Process was interrupted.");

    private final String rsyncExecutable;

    private final RsyncVersion rsyncVersion;
    
    private final String sshExecutable;

    private final List<String> additionalCmdLineFlags;
    
    private final boolean overwrite;

    /**
     * If <code>true</code>, the file system of the destination directory requires that already existing files and
     * directories on the remote side are removed before the copy process is started.
     */
    private final boolean destinationDirectoryRequiresDeletionBeforeCreation;

    /**
     * A reference to the {@link Process} that performs the copy. Note that the reference will be <code>null</code>,
     * if currently no copy process is running.
     */
    private final AtomicReference<Process> copyProcessReference;

    /**
     * Constructs an <code>RsyncCopier</code>.
     * 
     * @param rsyncExecutable The <code>rsync</code> binary to call for copying.
     * @param sshExecutableOrNull The <code>ssh</code> binary to use for creating tunnels, or <code>null</code>, if
     *            no <code>ssh</code> is available on this machine.
     * @param destinationDirectoryRequiresDeletionBeforeCreation If <code>true</code>, already existing files and
     *            directories on the remote side will be deleted before starting the copy process (no overwriting of
     *            paths).
     */
    public RsyncCopier(File rsyncExecutable, File sshExecutableOrNull, 
            boolean destinationDirectoryRequiresDeletionBeforeCreation, boolean overwrite, String... cmdLineFlags)
    {
        assert rsyncExecutable != null && rsyncExecutable.exists();
        assert sshExecutableOrNull == null || rsyncExecutable.exists();

        this.rsyncExecutable = rsyncExecutable.getAbsolutePath();
        this.rsyncVersion = RsyncVersionChecker.getVersion(rsyncExecutable.getAbsolutePath());
        this.sshExecutable = (sshExecutableOrNull != null) ? sshExecutableOrNull.getPath() : null;
        this.destinationDirectoryRequiresDeletionBeforeCreation = destinationDirectoryRequiresDeletionBeforeCreation;
        this.copyProcessReference = new AtomicReference<Process>(null);
        this.overwrite = overwrite;
        if (cmdLineFlags.length > 0)
        {
            this.additionalCmdLineFlags = Arrays.asList(cmdLineFlags);
        } else
        {
            this.additionalCmdLineFlags = null;
        }
    }
    
    private boolean rsyncSupportsAppend()
    {
        assert rsyncVersion != null;
        
        return rsyncVersion.isNewerOrEqual(2, 6, 7);
    }
    
    private boolean isOverwriteMode()
    {
        return overwrite || destinationDirectoryRequiresDeletionBeforeCreation || (rsyncSupportsAppend() == false);
    }

    public Status copy(File sourcePath, File destinationDirectory)
    {
        return copy(sourcePath, null, destinationDirectory, null);
    }

    public Status copyFromRemote(File sourcePath, String sourceHost, File destinationDirectory)
    {
        return copy(sourcePath, sourceHost, destinationDirectory, null);
    }

    public Status copyToRemote(File sourcePath, File destinationDirectory, String destinationHost)
    {
        return copy(sourcePath, null, destinationDirectory, destinationHost);
    }

    private Status copy(File sourcePath, String sourceHostOrNull, File destinationDirectory,
            String destinationHostOrNull)
    {
        assert sourcePath != null;
        assert sourceHostOrNull != null || sourcePath.exists() : logNonExistent(sourcePath);
        assert destinationDirectory != null;
        assert destinationHostOrNull != null || destinationDirectory.isDirectory() : logNonExistent(sourcePath);
        assert sourceHostOrNull == null || destinationHostOrNull == null; // only one side can be remote

        try
        {
            final ProcessBuilder copyProcessBuilder =
                    new ProcessBuilder(createCommandLine(sourcePath, sourceHostOrNull, destinationDirectory,
                            destinationHostOrNull));
            copyProcessBuilder.redirectErrorStream(true);
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Executing command: " + copyProcessBuilder.command().toString());
            }
            final Process copyProcess = copyProcessBuilder.start();
            copyProcessReference.set(copyProcess);

            final int exitValue = copyProcess.waitFor();
            logRsyncExitValue(copyProcess);
            return createStatus(exitValue);
        } catch (IOException e)
        {
            machineLog.error(String.format("Cannot execute rsync binary %s", rsyncExecutable), e);
            return new Status(StatusFlag.FATAL_ERROR, String.format("ProcessBuilder: %s", e.getMessage()));
        } catch (InterruptedException e)
        {
            // Shouldn't happen because this is called in a timer, anyway, it's just another error condition.
            return INTERRUPTED_STATUS;
        } finally
        {
            copyProcessReference.set(null);
        }
    }

    private String logNonExistent(File path)
    {
        if (path == null)
        {
            return "null";
        } else
        {
            return "path '" + path.getAbsolutePath() + "' does not exist";
        }
    }

    private List<String> createCommandLine(File sourcePath, String sourceHost, File destinationDirectory,
            String destinationHost)
    {
        assert sourcePath != null && (sourceHost != null || sourcePath.exists());
        assert destinationDirectory != null && (destinationHost != null || destinationDirectory.isDirectory());
        assert (destinationHost != null && sshExecutable != null) || (destinationHost == null);
        assert (sourceHost != null && sshExecutable != null) || (sourceHost == null);

        final List<String> standardParameters = Arrays.asList("--archive", "--delete", "--inplace");
        final List<String> commandLineList = new ArrayList<String>();
        commandLineList.add(rsyncExecutable);
        commandLineList.addAll(standardParameters);
        if (isOverwriteMode())
        {
            commandLineList.add("--whole-file");
        } else
        {
            commandLineList.add("--append");
        }
        if (sshExecutable != null && destinationHost != null)
        {
            commandLineList.add("--rsh");
            commandLineList.add(getSshExecutableArgument(sshExecutable));
        }
        if (additionalCmdLineFlags != null)
        {
            commandLineList.addAll(additionalCmdLineFlags);
        }
        commandLineList.add(buildPath(sourceHost, sourcePath, false));
        commandLineList.add(buildPath(destinationHost, destinationDirectory, true));

        return commandLineList;
    }

    private static String getSshExecutableArgument(String sshExecutable)
    {
        if (OSUtilities.isWindows())
        {
            return toUnix(sshExecutable) + " -oBatchMode=yes";
        } else
        {
            return sshExecutable + " -oBatchMode=yes";
        }
    }

    private static String buildPath(String host, File resource, boolean isDirectory)
    {
        if (null == host)
        {
            String path = resource.getAbsolutePath();
            if (isDirectory)
                path += File.separator;
            return toUnix(path);
        } else
        {
            String path = resource.getPath();
            if (isDirectory)
                path += File.separator;
            // We must not use the absolute path here because that is the business of the destination host.
            return host + ":" + toUnix(path);
        }
    }

    /**
     * Since <code>rsync</code> under Windows is from Cygwin, we need to translate the path into a Cygwin path.
     */
    private static String toUnix(String path)
    {
        if (OSUtilities.isWindows() == false)
        {
            return path;
        }
        String resultPath = path.replace('\\', '/');
        if (resultPath.charAt(1) == ':') // Get rid of drive letters.
        {
            resultPath = "/cygdrive/" + resultPath.charAt(0) + resultPath.substring(2);
        }
        return resultPath;
    }

    private static Status createStatus(final int exitValue)
    {
        if (ProcessExecutionHelper.isProcessTerminated(exitValue))
        {
            return TERMINATED_STATUS;
        }
        final StatusFlag flag = RsyncExitValueTranslator.getStatus(exitValue);
        if (StatusFlag.OK.equals(flag))
        {
            return Status.OK;
        }
        return new Status(flag, RsyncExitValueTranslator.getMessage(exitValue));
    }

    private static void logRsyncExitValue(final Process copyProcess)
    {
        final int exitValue = copyProcess.exitValue();
        final List<String> processOutput = ProcessExecutionHelper.readProcessOutputLines(copyProcess, machineLog);
        ProcessExecutionHelper.logProcessExecution("rsync", exitValue, processOutput, operationLog, machineLog);
    }

    /**
     * Terminates the copy process by calling {@link Process#destroy()}, if a copy process is currently running. If no
     * copy process is running, the method will return immediately.
     */
    public boolean terminate()
    {
        final Process copyProcess = copyProcessReference.get();
        if (copyProcess != null)
        {
            copyProcess.destroy();
            return true;
        } else
        {
            return false;
        }
    }

    /**
     * Checks whether the <code>rsync</code> can be executed and has a version >= 2.6.0.
     * 
     * @throws ConfigurationFailureException If the check fails.
     */
    public void check()
    {
        if (machineLog.isDebugEnabled())
        {
            machineLog.debug(String.format("Testing rsync executable '%s'", rsyncExecutable));
        }
        if (rsyncVersion == null)
        {
            if (OSUtilities.executableExists(rsyncExecutable))
            {
                throw new ConfigurationFailureException(String.format("Rsync executable '%s' is invalid.",
                        rsyncExecutable));
            } else
            {
                throw new ConfigurationFailureException(String.format("Rsync executable '%s' does not exist.",
                        rsyncExecutable));
            }
        }
        if (rsyncVersion.isNewerOrEqual(2, 6, 0) == false)
        {
            throw new ConfigurationFailureException(String.format(
                    "Rsync executable '%s' is too old (required: 2.6.0, found: %s)", rsyncExecutable, rsyncVersion
                            .getVersionString()));
        }
        if (machineLog.isInfoEnabled())
        {
            machineLog.info(String.format("Using rsync executable '%s', version %s, mode: %s", rsyncExecutable, rsyncVersion
                    .getVersionString(), (isOverwriteMode() ? "overwrite" : "append")));
        }
    }

    public boolean existsRemotely(File destinationDirectory, String destinationHost)
    {
        assert destinationDirectory != null;
        assert destinationHost != null;

        final String destination = buildPath(destinationHost, destinationDirectory, true);
        final ProcessBuilder listProcessBuilder =
                new ProcessBuilder(rsyncExecutable, "--rsh", getSshExecutableArgument(sshExecutable), destination)
                        .redirectErrorStream(true);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Executing command: " + listProcessBuilder.command().toString());
        }
        try
        {
            final Process listProcess = listProcessBuilder.start();
            final Timer watchDogTimer = initWatchDog(listProcess);
            final int exitValue = listProcess.waitFor();
            watchDogTimer.cancel();
            logRsyncExitValue(listProcess);
            return Status.OK == createStatus(exitValue);
        } catch (IOException ex)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Error trying to list '%s'", destination), ex);
            }
            return false;
        } catch (InterruptedException ex)
        {
            return false;
        }
    }

    private Timer initWatchDog(final Process listProcess)
    {
        TimerTask killer = new TimerTask()
            {
                @Override
                public void run()
                {
                    machineLog.warn("Destroying stalled rsync list process.");
                    listProcess.destroy();
                }
            };
        Timer watchDog = new Timer();
        watchDog.schedule(killer, MAX_INACTIVITY_PERIOD_RSYNC_LIST);
        return watchDog;
    }

}
