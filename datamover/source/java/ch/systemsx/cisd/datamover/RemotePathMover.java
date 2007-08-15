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

package ch.systemsx.cisd.datamover;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask;
import ch.systemsx.cisd.common.utilities.FileUtilities;

/**
 * A class that moves files and directories to remote directories. This class monitors the copy process and, if
 * necessary, notifies an administrator of failures.
 * 
 * @author Bernd Rinn
 */
public final class RemotePathMover implements DirectoryScanningTimerTask.IPathHandler
{

    private static final String START_COPYING_PATH_TEMPLATE = "Start copying path '%s' to '%s'.";

    private static final String START_COPYING_PATH_RETRY_TEMPLATE = "Start copying path '%s' to '%s' [retry: %d].";

    private static final String FINISH_COPYING_PATH_TEMPLATE = "Finish copying path '%s' to '%s' [time: %.2f s].";

    private static final String REMOVED_PATH_TEMPLATE = "Removed path '%s'.";

    private static final String COPYING_PATH_TO_REMOTE_FAILED = "Copying path '%s' to remote directory '%s' failed: %s";

    private static final String MOVING_PATH_TO_REMOTE_FAILED_TEMPLATE =
            "Moving path '%s' to remote directory '%s' failed.";

    private static final String REMOVING_LOCAL_PATH_FAILED_TEMPLATE = "Removing local path '%s' failed (%s).";

    private static final String FAILED_TO_CREATE_MARK_FILE_TEMPLATE = "Failed to create mark file '%s'";

    private static final String FAILED_TO_COPY_MARK_FILE_TO_REMOTE_TEMPLATE =
            "Failed to copy mark file '%s' to remote (%s)";

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, RemotePathMover.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, RemotePathMover.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, RemotePathMover.class);

    private final File destinationDirectory;

    private final String destinationHost;

    private final IPathCopier copier;

    private final IPathRemover remover;
    
    private final String sourceHost;

    private final CopyActivityMonitor monitor;

    private final long intervallToWaitAfterFailure;

    private final int maximalNumberOfRetries;

    /**
     * Creates a <var>PathRemoteMover</var>.
     * 
     * @param destinationDirectory The directory to move paths to.
     * @param destinationHost The host to move paths to, or <code>null</code>, if <var>destinationDirectory</var> is
     *            a remote share.
     * @param monitor The activity monitor to inform about actions.
     * @param remover Allows to remove files.
     * @param copier Allows to copy files
     * @param sourceHost The host to move paths from, or <code>null</code>, if data will be moved from the local file system
     * @param timingParameters The timing parametes used for monitoring and reporting stall situations.
     */
    public RemotePathMover(File destinationDirectory, String destinationHost, CopyActivityMonitor monitor,
            IPathRemover remover, IPathCopier copier, String sourceHost, ITimingParameters timingParameters)
    {
        assert destinationDirectory != null;
        assert monitor != null;
        assert remover != null;
        assert copier != null;
        assert timingParameters != null;
        assert FileUtilities.checkDirectoryFullyAccessible(destinationDirectory, "destination") == null : FileUtilities
                .checkDirectoryFullyAccessible(destinationDirectory, "destination");

        this.destinationDirectory = destinationDirectory;
        this.destinationHost = destinationHost;
        this.monitor = monitor;
        this.copier = copier;
        this.remover = remover;
        this.sourceHost = sourceHost;
        this.intervallToWaitAfterFailure = timingParameters.getIntervalToWaitAfterFailure();
        this.maximalNumberOfRetries = timingParameters.getMaximalNumberOfRetries();

        assert copier != null;
        assert remover != null;
        assert intervallToWaitAfterFailure >= 0;
        assert maximalNumberOfRetries >= 0;
    }

    public boolean handle(File path)
    {
        int tryCount = 0;
        do
        {
            if (operationLog.isInfoEnabled())
            {
                if (tryCount > 0) // This is a retry
                {
                    operationLog.info(String.format(START_COPYING_PATH_RETRY_TEMPLATE, path.getPath(),
                            destinationDirectory.getPath(), tryCount));
                } else
                {
                    operationLog.info(String.format(START_COPYING_PATH_TEMPLATE, path.getPath(), destinationDirectory
                            .getPath()));
                }
            }
            final long startTime = System.currentTimeMillis();
            monitor.start(path);
            final Status copyStatus = copier.copy(path, sourceHost, destinationDirectory, destinationHost);
            monitor.stop();
            if (StatusFlag.OK.equals(copyStatus.getFlag()))
            {
                if (operationLog.isInfoEnabled())
                {
                    final long endTime = System.currentTimeMillis();
                    operationLog.info(String.format(FINISH_COPYING_PATH_TEMPLATE, path.getPath(), destinationDirectory
                            .getPath(), (endTime - startTime) / 1000.0));
                }
                final Status removalStatus = remover.remove(path);
                if (Status.OK.equals(removalStatus) == false)
                {
                    // We don't retry this, because the path is local and removal really shouldn't fail.
                    notificationLog.error(String.format(REMOVING_LOCAL_PATH_FAILED_TEMPLATE, path, removalStatus));
                } else if (operationLog.isInfoEnabled())
                {
                    operationLog.info(String.format(REMOVED_PATH_TEMPLATE, path.getPath()));
                }
                // Note: we return true even if removal of the directory failed. There is no point in retrying the
                // operation.
                return markAsFinished(path);
            } else
            {
                operationLog.warn(String.format(COPYING_PATH_TO_REMOTE_FAILED, path.getPath(), destinationDirectory
                        .getPath(), copyStatus));
                if (StatusFlag.FATAL_ERROR.equals(copyStatus.getFlag()))
                {
                    break;
                }
            }
            // Leave the loop if we have re-tried it too often.
            ++tryCount;
            if (tryCount > maximalNumberOfRetries)
            {
                break;
            }
            try
            {
                Thread.sleep(intervallToWaitAfterFailure);
            } catch (InterruptedException e)
            {
                // We don't expect to get interrupted, but even if, there is no need to handle this here.
            }
        } while (true);

        notificationLog.error(String.format(MOVING_PATH_TO_REMOTE_FAILED_TEMPLATE, path, destinationDirectory));
        return false;
    }

    private boolean markAsFinished(File path)
    {
        if (destinationHost == null)
        {
            return markAsFinishedLocal(path);
        } else
        {
            return markAsFinishedRemote(path);
        }
    }

    private boolean markAsFinishedLocal(File path)
    {
        final File markFile = new File(destinationDirectory, Constants.IS_FINISHED_PREFIX + path.getName());
        try
        {
            markFile.createNewFile();
            final boolean success = markFile.exists();
            if (success == false)
            {
                machineLog.error(String.format(FAILED_TO_CREATE_MARK_FILE_TEMPLATE, markFile.getAbsoluteFile()));
            }
            return success;
        } catch (IOException e)
        {
            machineLog.error(String.format(FAILED_TO_CREATE_MARK_FILE_TEMPLATE, markFile.getAbsoluteFile()), e);
            return false;
        }
    }

    private boolean markAsFinishedRemote(File path)
    {
        final File markFile = new File(path.getParent(), Constants.IS_FINISHED_PREFIX + path.getName());
        try
        {
            markFile.createNewFile();
            monitor.start(path);
            final Status copyStatus = copier.copy(markFile, sourceHost, destinationDirectory, destinationHost);
            monitor.stop();
            if (StatusFlag.OK.equals(copyStatus.getFlag()))
            {
                return true;
            } else
            {
                machineLog.error(String.format(FAILED_TO_COPY_MARK_FILE_TO_REMOTE_TEMPLATE, markFile.getAbsoluteFile(),
                        copyStatus.toString()));
                return false;
            }

        } catch (IOException e)
        {
            machineLog.error(String.format(FAILED_TO_CREATE_MARK_FILE_TEMPLATE, markFile.getAbsoluteFile()), e);
            return false;
        } finally
        {
            markFile.delete();
        }
    }

}
