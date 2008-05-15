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

package ch.systemsx.cisd.datamover.filesystem.remote;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.IStoreHandler;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.common.MarkerFile;
import ch.systemsx.cisd.datamover.filesystem.intf.IExtendedFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IStoreCopier;
import ch.systemsx.cisd.datamover.intf.ITimingParameters;

/**
 * A class that moves files and directories to remote directories. This class monitors the copy
 * process and, if necessary, notifies an administrator of failures.
 * 
 * @author Bernd Rinn
 */
public final class RemotePathMover implements IStoreHandler
{

    /**
     * Number of milliseconds to wait before considering that the directory is not fully accessible.
     */
    public static final long DIRECTORY_ACCESSIBLE_TIMEOUT_MILLIS = 3000L;

    private static final String START_COPYING_PATH_TEMPLATE = "Start copying path '%s' to '%s'.";

    private static final String START_COPYING_PATH_RETRY_TEMPLATE =
            "Start copying path '%s' to '%s' [retry: %d].";

    private static final String FINISH_COPYING_PATH_TEMPLATE =
            "Finish copying path '%s' to '%s' [time: %.2f s].";

    private static final String REMOVED_PATH_TEMPLATE = "Removed path '%s'.";

    private static final String COPYING_PATH_TO_REMOTE_FAILED =
            "Copying path '%s' to remote directory '%s' failed: %s";

    private static final String MOVING_PATH_TO_REMOTE_FAILED_TEMPLATE =
            "Moving path '%s' to remote directory '%s' failed.";

    private static final String REMOVING_LOCAL_PATH_FAILED_TEMPLATE =
            "Removing local path '%s' failed (%s).";

    private static final String FAILED_TO_CREATE_FILE_TEMPLATE =
            "Failed to create file '%s' in '%s'";

    private static final String FAILED_TO_COPY_FILE_TO_REMOTE_TEMPLATE =
            "Failed to copy file '%s' from '%s' to remote (%s)";

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, RemotePathMover.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RemotePathMover.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, RemotePathMover.class);

    private final IFileStore sourceDirectory;

    private final IFileStore destinationDirectory;

    private final IStoreCopier copier;

    private final CopyActivityMonitor monitor;

    private final long intervallToWaitAfterFailure;

    private final int maximalNumberOfRetries;

    /**
     * Creates a <var>PathRemoteMover</var>.
     * 
     * @param sourceDirectory The directory to move paths from.
     * @param destinationDirectory The directory to move paths to.
     * @param copier Copies items from source to destination
     * @param monitor The activity monitor to inform about actions.
     * @param timingParameters The timing parameters used for monitoring and reporting stall
     *            situations.
     * @throws ConfigurationFailureException If the destination directory is not fully accessible.
     */
    public RemotePathMover(final IFileStore sourceDirectory, final IFileStore destinationDirectory,
            final IStoreCopier copier, final CopyActivityMonitor monitor,
            final ITimingParameters timingParameters) throws ConfigurationFailureException
    {
        assert sourceDirectory != null;
        assert destinationDirectory != null;
        assert monitor != null;
        assert timingParameters != null;
        assert sourceDirectory.tryAsExtended() != null
                || destinationDirectory.tryAsExtended() != null;

        final String errorMsg =
                destinationDirectory
                        .tryCheckDirectoryFullyAccessible(DIRECTORY_ACCESSIBLE_TIMEOUT_MILLIS);
        if (StringUtils.isNotBlank(errorMsg))
        {
            throw new ConfigurationFailureException(errorMsg);
        }
        this.sourceDirectory = sourceDirectory;
        this.destinationDirectory = destinationDirectory;
        this.copier = copier;
        this.monitor = monitor;
        this.intervallToWaitAfterFailure = timingParameters.getIntervalToWaitAfterFailure();
        this.maximalNumberOfRetries = timingParameters.getMaximalNumberOfRetries();

        assert intervallToWaitAfterFailure >= 0;
        assert maximalNumberOfRetries >= 0;
    }

    private final Status copyAndMonitor(final StoreItem item)
    {
        monitor.start(item);
        final Status copyStatus = copier.copy(item);
        monitor.stop();
        return copyStatus;
    }

    private final void removeAndMark(final StoreItem item)
    {
        remove(item);
        markAsFinished(item);
    }

    private final boolean checkTargetAvailable()
    {
        final String msg =
                destinationDirectory
                        .tryCheckDirectoryFullyAccessible(DIRECTORY_ACCESSIBLE_TIMEOUT_MILLIS);
        if (msg != null)
        {
            machineLog.error(msg);
            return false;
        }
        return true;
    }

    private final void remove(final StoreItem sourceItem)
    {
        final StoreItem removalInProgressMarkerFile = tryMarkAsDeletionInProgress(sourceItem);
        final Status removalStatus = sourceDirectory.delete(sourceItem);
        removeDeletionMarkerFile(removalInProgressMarkerFile);

        if (Status.OK.equals(removalStatus) == false)
        {
            notificationLog.error(String.format(REMOVING_LOCAL_PATH_FAILED_TEMPLATE,
                    getSrcPath(sourceItem), removalStatus));
        } else if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format(REMOVED_PATH_TEMPLATE, getSrcPath(sourceItem)));
        }
    }

    private final boolean isDeletionInProgress(final StoreItem item)
    {
        final StoreItem markDeletionInProgressMarkerFile =
                MarkerFile.createDeletionInProgressMarker(item);
        return getDeletionMarkerStore().exists(markDeletionInProgressMarkerFile);
    }

    private final StoreItem tryMarkAsDeletionInProgress(final StoreItem item)
    {
        final StoreItem markDeletionInProgressMarkerFile =
                MarkerFile.createDeletionInProgressMarker(item);
        if (createFileInside(getDeletionMarkerStore(), markDeletionInProgressMarkerFile))
        {
            return markDeletionInProgressMarkerFile;
        } else
        {
            machineLog.error(String.format(
                    "Cannot create deletion-in-progress marker file for path '%s' [%s]", item,
                    markDeletionInProgressMarkerFile));
            return null;
        }
    }

    private final void removeDeletionMarkerFile(final StoreItem markerOrNull)
    {
        if (markerOrNull != null)
        {
            final Status status = getDeletionMarkerStore().delete(markerOrNull);
            if (status.equals(Status.OK) == false)
            {
                machineLog.error(String.format("Cannot remove marker file '%s'", getPath(
                        destinationDirectory, markerOrNull)));
            }
        }
    }

    private final IExtendedFileStore getDeletionMarkerStore()
    {
        IExtendedFileStore fileStore = destinationDirectory.tryAsExtended();
        if (fileStore == null)
        {
            fileStore = sourceDirectory.tryAsExtended();
        }
        assert fileStore != null;
        return fileStore;
    }

    // Creates a finish-marker inside destination directory.
    private final boolean markAsFinished(final StoreItem item)
    {
        final StoreItem markerItem = MarkerFile.createCopyFinishedMarker(item);
        IExtendedFileStore extendedFileStore = destinationDirectory.tryAsExtended();
        if (extendedFileStore != null)
        {
            // We create the marker directly inside the destination directory
            return createFileInside(extendedFileStore, markerItem);
        } else
        {
            // When destination is remote, we put the item directory in the source directory and
            // copy it to destination.
            extendedFileStore = sourceDirectory.tryAsExtended();
            assert extendedFileStore != null;
            return markOnSourceLocalAndCopyToRemoteDestination(extendedFileStore, markerItem);
        }
    }

    private final boolean markOnSourceLocalAndCopyToRemoteDestination(
            final IExtendedFileStore sourceFileStore, final StoreItem markerFile)
    {
        try
        {
            if (createFileInside(sourceFileStore, markerFile) == false)
            {
                return false;
            }
            final Status copyStatus = copyAndMonitor(markerFile);
            if (StatusFlag.OK.equals(copyStatus.getFlag()))
            {
                return true;
            } else
            {
                machineLog.error(String.format(FAILED_TO_COPY_FILE_TO_REMOTE_TEMPLATE, markerFile,
                        sourceFileStore, copyStatus.toString()));
                return false;
            }
        } finally
        {
            sourceFileStore.delete(markerFile);
        }
    }

    private final static boolean createFileInside(final IExtendedFileStore directory,
            final StoreItem item)
    {
        final boolean success = directory.createNewFile(item);
        if (success == false)
        {
            machineLog.error(String.format(FAILED_TO_CREATE_FILE_TEMPLATE, item, directory));
        }
        return success;
    }

    private String getSrcPath(final StoreItem item)
    {
        return getPath(sourceDirectory, item);
    }

    private final static String getPath(final IFileStore directory, final StoreItem item)
    {
        return item + " inside " + directory;
    }

    //
    // IStoreHandler
    //

    public final boolean mayHandle(final StoreItem item)
    {
        final HighwaterMarkWatcher highwaterMarkWatcher =
                destinationDirectory.getHighwaterMarkWatcher();
        assert highwaterMarkWatcher.getPath() != null : "Remote path not set";
        highwaterMarkWatcher.run();
        return highwaterMarkWatcher.isBelow() == false;
    }

    public final void handle(final StoreItem item)
    {
        if (isDeletionInProgress(item))
        {
            // This is a recovery situation: we have been interrupted removing the path and now
            // finish the job.
            if (operationLog.isInfoEnabled())
            {
                operationLog.info(String.format("Detected recovery situation: '%s' has been "
                        + "interrupted in deletion phase, finishing up.", getSrcPath(item)));
            }
            removeAndMark(item);
            return;
        }
        int tryCount = 0;
        do
        {
            if (operationLog.isInfoEnabled())
            {
                if (tryCount > 0) // This is a retry
                {
                    operationLog.info(String.format(START_COPYING_PATH_RETRY_TEMPLATE,
                            getSrcPath(item), destinationDirectory, tryCount));
                } else
                {
                    operationLog.info(String.format(START_COPYING_PATH_TEMPLATE, getSrcPath(item),
                            destinationDirectory));
                }
            }
            // TODO 2008-03-17, Bernd Rinn: There needs to be a limit for how often we take this
            // exit without sending
            // a notification email.
            if (checkTargetAvailable() == false)
            {
                return;
            }
            final long startTime = System.currentTimeMillis();
            final Status copyStatus = copyAndMonitor(item);
            if (StatusFlag.OK.equals(copyStatus.getFlag()))
            {
                if (operationLog.isInfoEnabled())
                {
                    final long endTime = System.currentTimeMillis();
                    operationLog.info(String.format(FINISH_COPYING_PATH_TEMPLATE, getSrcPath(item),
                            destinationDirectory, (endTime - startTime) / 1000.0));
                }
                removeAndMark(item);
                return;
            } else
            {
                operationLog.warn(String.format(COPYING_PATH_TO_REMOTE_FAILED, getSrcPath(item),
                        destinationDirectory, copyStatus));
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
            } catch (final InterruptedException e)
            {
                // We don't expect to get interrupted, but even if, there is no need to handle this
                // here.
            }
        } while (true);
        notificationLog.error(String.format(MOVING_PATH_TO_REMOTE_FAILED_TEMPLATE,
                getSrcPath(item), destinationDirectory));
    }

}
