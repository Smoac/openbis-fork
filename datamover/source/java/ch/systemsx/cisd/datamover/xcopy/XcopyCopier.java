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

package ch.systemsx.cisd.datamover.xcopy;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.datamover.IPathCopier;
import ch.systemsx.cisd.datamover.helper.CmdLineHelper;

/**
 * A class that encapsulates the <code>xcopy</code> call for doing an archive copy.
 * 
 * @author Bernd Rinn
 */
public class XcopyCopier implements IPathCopier
{

    /**
     * The {@link Status} returned if the process was terminated by {@link Process#destroy()}.
     */
    public static final Status TERMINATED_STATUS = new Status(StatusFlag.RETRIABLE_ERROR, "Process was terminated.");

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, XcopyCopier.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, XcopyCopier.class);

    private static final Status INTERRUPTED_STATUS = new Status(StatusFlag.RETRIABLE_ERROR, "Process was interrupted.");

    private final String xcopyExecutable;

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
     * Constructs an <code>XcopyCopier</code>.
     * 
     * @param xcopyBinary The <code>xcopy</code> binary to call for copying.
     * @param destinationDirectoryRequiresDeletionBeforeCreation If <code>true</code>, already existing files and
     *            directories on the remote side will be deleted before starting the copy process (no overwriting of
     *            paths).
     */
    public XcopyCopier(File xcopyBinary, boolean destinationDirectoryRequiresDeletionBeforeCreation)
    {
        assert xcopyBinary != null && xcopyBinary.exists();

        this.xcopyExecutable = xcopyBinary.getAbsolutePath();
        this.destinationDirectoryRequiresDeletionBeforeCreation = destinationDirectoryRequiresDeletionBeforeCreation;
        this.copyProcessReference = new AtomicReference<Process>(null);
    }

    public Status copy(File sourcePath, File destinationDirectory)
    {
        assert sourcePath != null;
        assert sourcePath.exists();
        assert destinationDirectory != null;
        assert destinationDirectory.isDirectory();

        try
        {
            final File destinationPath = new File(destinationDirectory, sourcePath.getName());
            if (destinationDirectoryRequiresDeletionBeforeCreation && destinationPath.exists())
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(
                            "Remove path '%s' since it exists and the remote file system doesn't support overwriting.",
                            destinationPath));
                }
                // Remove it now, otherwise the Cellera NAS server will not allow xcopy to perform.
                // Error message from xcopy: "File creation error - A required privilege is not held by the client."
                FileUtilities.deleteRecursively(destinationPath);
            }
            // Since xcopy does not update the time stamp while copying, this is the only way to ensure that
            // the inactivity monitor won't get in the way by terminating the copy process.
            //
            final long modificationTime = sourcePath.lastModified();
            final long now = System.currentTimeMillis();
            final boolean successSettingModificationTime = sourcePath.setLastModified(now);
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format("Setting last modification time of path '%s' to %2$tF %2$tT %s",
                        sourcePath, now, successSettingModificationTime ? "" : "FAILED"));
            }
            try
            {
                final ProcessBuilder copyProcessBuilder =
                        new ProcessBuilder(xcopyExecutable, "\"" + sourcePath.getAbsolutePath() + "\"", "\""
                                + destinationDirectory.getAbsolutePath() + "\"", "/E", "/I", "/Q", "/H", "/R", "/K",
                                "/O", "/X", "/Y").redirectErrorStream(true);
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug("Executing command: " + copyProcessBuilder.command().toString());
                }
                final Process copyProcess = copyProcessBuilder.start();
                copyProcessReference.set(copyProcess);

                final int exitValue = copyProcess.waitFor();
                logXcopyExitValue(exitValue, copyProcess);
                return createStatus(exitValue);
            } finally
            {
                // Restore modification time.
                final boolean successResettingModificationTimeSource = sourcePath.setLastModified(modificationTime);
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(
                            "Re-setting last modification time of path '%s' to %2$tF %2$tT %s", sourcePath,
                            modificationTime, successResettingModificationTimeSource ? "" : "FAILED"));
                }
                final boolean successResettingModificationTimeDestination =
                        destinationPath.setLastModified(modificationTime);
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(
                            "Re-setting last modification time of path '%s' to %2$tF %2$tT %s", destinationPath,
                            modificationTime, successResettingModificationTimeDestination ? "" : "FAILED"));
                }
            }
        } catch (IOException e)
        {
            machineLog.error(String.format("Cannot execute xcopy binary %s", xcopyExecutable), e);
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

    private Status createStatus(final int exitValue)
    {
        if (CmdLineHelper.processTerminated(exitValue))
        {
            return TERMINATED_STATUS;
        }
        if (exitValue == 0)
        {
            return Status.OK;
        } else
        {
            // We don't know anything about xcopy errors - assume that retrying makes sense.
            return new Status(StatusFlag.RETRIABLE_ERROR, String
                    .format("Xcopy had an error, exit value: %s", exitValue));
        }
    }

    private void logXcopyExitValue(final int exitValue, final Process copyProcess)
    {
        CmdLineHelper.logProcessExitValue(exitValue, copyProcess, "xcopy", operationLog, machineLog);
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
     * Checks whether the <code>xcopy</code> can be executed and has an appropriate help message.
     * 
     * @throws EnvironmentFailureException If the check fails.
     */
    public void check()
    {
        final String EXPECTED_HELP_MESSAGE = "Copies files and directory trees.";
        if (machineLog.isDebugEnabled())
        {
            machineLog.debug(String.format("Testing xcopy executable '%s'", xcopyExecutable));
        }
        final String helpMessage = getHelpMessage();
        if (EXPECTED_HELP_MESSAGE.equals(helpMessage) == false)
        {
            if (OSUtilities.executableExists(xcopyExecutable))
            {
                throw new EnvironmentFailureException(String.format("Xcopy executable '%s' is invalid.",
                        xcopyExecutable));
            } else
            {
                throw new EnvironmentFailureException(String.format("Xcopy executable '%s' does not exist.",
                        xcopyExecutable));
            }
        }
        if (machineLog.isInfoEnabled())
        {
            machineLog.info(String.format("Using xcopy executable '%s'", xcopyExecutable));
        }
    }

    private String getHelpMessage()
    {
        BufferedReader reader = null;
        try
        {
            final Process process = new ProcessBuilder(xcopyExecutable, "/?").start();
            if (machineLog.isDebugEnabled())
            {
                machineLog.debug("Waiting for xcopy process to finish.");
            }
            final long TIME_TO_WAIT_FOR_COMPLETION = 2 * 1000;
            final Timer watchDog = new Timer("Xcopy Watch Dog");
            watchDog.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        // For some strange reason, that's the normal case with "xcopy /?". :-(
                        process.destroy();
                    }
                }, TIME_TO_WAIT_FOR_COMPLETION);
            final int exitValue = process.waitFor();
            watchDog.cancel();
            if (machineLog.isDebugEnabled())
            {
                machineLog.debug(String.format("Xcopy process finished with exit value %s", exitValue));
            }
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (IOException e)
        {
            return null;
        } catch (InterruptedException e)
        {
            // This should never happen.
            throw new CheckedExceptionTunnel(e);
        } finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                } catch (IOException e)
                {
                    // Ignore.
                }
            }
        }
    }

    /**
     * <em>Must not be called with <code>destinationHost != null</code> since this copier does not support explicitly 
     * specifying a destination host.</em>
     * 
     * @throws IllegalStateException If <var>sourceHost</var> or <var>destinationHost</var> is not <code>null</code>.
     */
    public Status copy(File sourcePath, String sourceHost, File destinationDirectory, String destinationHost)
    {
        assert sourcePath != null;
        assert destinationDirectory != null;

        if (sourceHost == null && destinationHost == null)
        {
            return copy(sourcePath, destinationDirectory);
        } else
        {
            throw new IllegalStateException(
                    "Explicitely specifying a source or destination host is not supported by this copier.");
        }
    }

    /**
     * <em>Must not be called since this copier does not support explicitely specifying a destination host.</em>
     * 
     * @throws IllegalStateException Whenever it is called.
     */
    public boolean exists(File destinationDirectory, String destinationHost)
    {
        throw new IllegalStateException("Explicitely specifying a destination host is not supported by this copier.");
    }

    /**
     * @return <code>false</code>
     */
    public boolean supportsExplicitHost()
    {
        return false;
    }

}
