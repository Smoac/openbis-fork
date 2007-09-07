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

import static ch.systemsx.cisd.datamover.filesystem.remote.rsync.RsyncVersionChecker.getVersion;

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
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.datamover.filesystem.common.CmdLineHelper;
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

    private final String sshExecutable;

    private final List<String> additionalCmdLineFlags;

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
     * @param sshExecutable The <code>ssh</code> binary to use for creating tunnels, or <code>null</code>, if no
     *            <code>ssh</code> is available on this machine.
     * @param destinationDirectoryRequiresDeletionBeforeCreation If <code>true</code>, already existing files and
     *            directories on the remote side will be deleted before starting the copy process (no overwriting of
     *            paths).
     */
    public RsyncCopier(File rsyncExecutable, File sshExecutable,
            boolean destinationDirectoryRequiresDeletionBeforeCreation, String... cmdLineFlags)
    {
        assert rsyncExecutable != null && rsyncExecutable.exists();
        assert sshExecutable == null || rsyncExecutable.exists();

        this.rsyncExecutable = rsyncExecutable.getAbsolutePath();
        this.sshExecutable = (sshExecutable != null) ? sshExecutable.getPath() : null;
        this.destinationDirectoryRequiresDeletionBeforeCreation = destinationDirectoryRequiresDeletionBeforeCreation;
        this.copyProcessReference = new AtomicReference<Process>(null);
        if (cmdLineFlags.length > 0)
        {
            this.additionalCmdLineFlags = Arrays.asList(cmdLineFlags);
        } else
        {
            this.additionalCmdLineFlags = null;
        }
    }

    public Status copy(File sourcePath, File destinationDirectory)
    {
        return copy(sourcePath, null, destinationDirectory, null);
    }

    public Status copy(File sourcePath, String sourceHost, File destinationDirectory, String destinationHost)
    {
        assert sourcePath != null;
        assert sourceHost != null || sourcePath.exists();
        assert destinationDirectory != null;
        assert destinationHost != null || destinationDirectory.isDirectory();
        assert sourceHost == null || destinationHost == null; // only one side can be remote

        final File destinationPath = new File(destinationDirectory, sourcePath.getName());
        if (destinationDirectoryRequiresDeletionBeforeCreation && destinationPath.exists())
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format(
                        "Remove path '%s' since it exists and the remote file system doesn't support overwriting.",
                        destinationPath));
            }
            // TODO 2007-08-13 Tomasz Pylak: Bernd, what if destinationHost is set?
            FileUtilities.deleteRecursively(destinationPath);
        }
        try
        {
            final ProcessBuilder copyProcessBuilder =
                    new ProcessBuilder(createCommandLine(sourcePath, sourceHost, destinationDirectory, destinationHost));
            copyProcessBuilder.redirectErrorStream(true);
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Executing command: " + copyProcessBuilder.command().toString());
            }
            final Process copyProcess = copyProcessBuilder.start();
            copyProcessReference.set(copyProcess);

            final int exitValue = copyProcess.waitFor();
            logRsyncExitValue(exitValue, copyProcess);
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

    private List<String> createCommandLine(File sourcePath, String sourceHost, File destinationDirectory,
            String destinationHost)
    {
        assert sourcePath != null && (sourceHost != null || sourcePath.exists());
        assert destinationDirectory != null && (destinationHost != null || destinationDirectory.isDirectory());
        assert (destinationHost != null && sshExecutable != null) || (destinationHost == null);
        assert (sourceHost != null && sshExecutable != null) || (sourceHost == null);

        final List<String> standardParameters = Arrays.asList("--archive", "--delete", "--inplace", "--whole-file");
        final List<String> commandLineList = new ArrayList<String>();
        commandLineList.add(rsyncExecutable);
        commandLineList.addAll(standardParameters);
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
        if (CmdLineHelper.processTerminated(exitValue))
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

    private static void logRsyncExitValue(final int exitValue, final Process copyProcess)
    {
        CmdLineHelper.logProcessExitValue(exitValue, copyProcess, "rsync", operationLog, machineLog);
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
        final RsyncVersion version = getVersion(rsyncExecutable);
        if (version == null)
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
        if (version.getMajorVersion() < 2 || (version.getMajorVersion() == 2 && version.getMinorVersion() < 6))
        {
            throw new ConfigurationFailureException(String.format(
                    "Rsync executable '%s' is too old (required: 2.6.0, found: %s)", rsyncExecutable, version
                            .getVersionString()));
        }
        if (machineLog.isInfoEnabled())
        {
            machineLog.info(String.format("Using rsync executable '%s', version %s", rsyncExecutable, version
                    .getVersionString()));
        }
    }

    public boolean exists(File destinationDirectory, String destinationHost)
    {
        assert destinationDirectory != null && destinationDirectory.isDirectory();
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
            logRsyncExitValue(exitValue, listProcess);
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

    /**
     * @return <code>true</code>
     */
    public boolean supportsExplicitHost()
    {
        return true;
    }

}
