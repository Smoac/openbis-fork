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

import static ch.systemsx.cisd.common.utilities.SystemTimeProvider.SYSTEM_TIME_PROVIDER;

import java.io.File;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.DirectoryScanningTimerTask;
import ch.systemsx.cisd.common.filesystem.FaultyPathDirectoryScanningHandler;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IStoreHandler;
import ch.systemsx.cisd.common.filesystem.IStoreItemFilter;
import ch.systemsx.cisd.common.filesystem.QuietPeriodFileFilter;
import ch.systemsx.cisd.common.filesystem.StoreItem;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkDirectoryScanningHandler;
import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.datamover.common.MarkerFile;
import ch.systemsx.cisd.datamover.filesystem.FileStoreFactory;
import ch.systemsx.cisd.datamover.filesystem.RemoteMonitoredMoverFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IExtendedFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathMover;
import ch.systemsx.cisd.datamover.filesystem.intf.IRecoverableTimerTaskFactory;
import ch.systemsx.cisd.datamover.utils.DataCompletedFilter;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;
import ch.systemsx.cisd.datamover.utils.StoreItemFilterBank;

/**
 * The <code>IRecoverableTimerTaskFactory</code> implementation which processes files in the
 * <code>incoming</code> directory.
 * 
 * @author Tomasz Pylak
 */
public class IncomingProcessor implements IRecoverableTimerTaskFactory
{
    /**
     * The number of consecutive errors of listing the incoming directories that are not reported in
     * the log to avoid mailbox flooding.
     */
    private final static int NUMBER_OF_ERRORS_IN_LISTING_IGNORED = 2;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, IncomingProcessor.class);

    private static final ISimpleLogger simpleOperationLog = new Log4jSimpleLogger(operationLog);

    private final Parameters parameters;

    private final IPathMover pathMover;

    private final LocalBufferDirs bufferDirs;

    private final IFileStore incomingStore;

    private final String prefixForIncoming;

    private final IStoreItemFilter storeItemFilter;

    private final String markerFileName;

    private final String errorMarkerFileName;

    private final String successorMarkerFileName;

    private final IStoreHandler remotePathMover;

    public static final DataMoverProcess createMovingProcess(final Parameters parameters,
            final String markerFile, final String errorMarkerFile,
            final String successorMarkerFile, final IFileSysOperationsFactory factory,
            final LocalBufferDirs bufferDirs)
    {
        return createMovingProcess(parameters, markerFile, errorMarkerFile, successorMarkerFile,
                factory, SYSTEM_TIME_PROVIDER, bufferDirs);
    }

    static final DataMoverProcess createMovingProcess(final Parameters parameters,
            final String markerFile, final String errorMarkerFile,
            final String successorMarkerFile, final IFileSysOperationsFactory factory,
            final ITimeProvider timeProvider, final LocalBufferDirs bufferDirs)
    {
        final IncomingProcessor processor =
                new IncomingProcessor(parameters, markerFile, errorMarkerFile, successorMarkerFile,
                        factory, timeProvider, bufferDirs);
        return processor.create();
    }

    private IncomingProcessor(final Parameters parameters, final String markerFileName,
            final String errorMarkerFileName, final String successorMarkerFileName,
            final IFileSysOperationsFactory factory, final ITimeProvider timeProvider,
            final LocalBufferDirs bufferDirs)
    {
        this.parameters = parameters;
        this.markerFileName = markerFileName;
        this.errorMarkerFileName = errorMarkerFileName;
        this.successorMarkerFileName = successorMarkerFileName;
        this.prefixForIncoming = parameters.getPrefixForIncoming();
        this.incomingStore = parameters.getIncomingStore(factory);
        this.pathMover = factory.getMover();
        this.bufferDirs = bufferDirs;
        this.storeItemFilter = createFilter(timeProvider);
        this.remotePathMover =
                createRemotePathMover(incomingStore, FileStoreFactory.createLocal(bufferDirs
                        .getCopyInProgressDir(), "local", factory, false));

    }

    private final IStoreItemFilter createFilter(final ITimeProvider timeProvider)
    {
        final StoreItemFilterBank filterBank = new StoreItemFilterBank();
        filterBank.add(createQuitePeriodFilter(timeProvider));
        final File dataCompletedScript = parameters.getDataCompletedScript();
        if (dataCompletedScript != null)
        {
            final long timeout = parameters.getDataCompletedScriptTimeout();
            filterBank.add(new DataCompletedFilter(incomingStore, dataCompletedScript, timeout));
        }
        return filterBank;
    }

    private QuietPeriodFileFilter createQuitePeriodFilter(final ITimeProvider timeProvider)
    {
        return new QuietPeriodFileFilter(incomingStore, parameters.getQuietPeriodMillis(),
                timeProvider, DatamoverConstants.IGNORED_ERROR_COUNT_BEFORE_NOTIFICATION);
    }

    private IStoreHandler createRemotePathMover(final IFileStore sourceDirectory,
            final IFileStore destinationDirectory)
    {
        return RemoteMonitoredMoverFactory
                .create(sourceDirectory, destinationDirectory, parameters);
    }

    public TimerTask createRecoverableTimerTask()
    {
        return new IncomingProcessorRecoveryTask();
    }

    private final DataMoverProcess create()
    {
        final File copyInProgressDir = bufferDirs.getCopyInProgressDir();
        final HighwaterMarkWatcher highwaterMarkWatcher =
                new HighwaterMarkWatcher(bufferDirs.getBufferDirHighwaterMark());
        final IStoreHandler pathHandler =
                DataMover.wrapHandleWithLogging(createIncomingMovingPathHandler(),
                        "STARTED_TRANSFER", null);
        final HighwaterMarkDirectoryScanningHandler directoryScanningHandler =
                new HighwaterMarkDirectoryScanningHandler(new FaultyPathDirectoryScanningHandler(
                        copyInProgressDir, pathHandler), highwaterMarkWatcher, copyInProgressDir);
        final DirectoryScanningTimerTask movingTask =
                new DirectoryScanningTimerTask(
                        new FileScannedStore(incomingStore, storeItemFilter),
                        directoryScanningHandler, pathHandler, NUMBER_OF_ERRORS_IN_LISTING_IGNORED);
        final TimerTask timerTask =
                DataMover.createTimerTaskForMarkerFileProtocol(movingTask, markerFileName,
                        errorMarkerFileName, successorMarkerFileName);
        return new DataMoverProcess(timerTask, "Mover of Incoming Data", this)
            {

                //
                // DataMoverProcess
                //

                @Override
                public final boolean terminate()
                {
                    movingTask.stopRun();
                    return super.terminate();
                }
            };
    }

    private IStoreHandler createIncomingMovingPathHandler()
    {
        return new IStoreHandler()
            {

                //
                // IStoreHandler
                //

                public final void handle(final StoreItem sourceItem)
                {
                    final IExtendedFileStore extendedFileStore = incomingStore.tryAsExtended();
                    if (extendedFileStore == null)
                    {
                        moveFromRemoteIncoming(sourceItem);
                    } else
                    {
                        moveFromLocalIncoming(extendedFileStore, sourceItem);
                    }
                }

                public boolean isStopped()
                {
                    return (incomingStore.tryAsExtended() == null) ? remotePathMover.isStopped()
                            : false;
                }
            };
    }

    private void moveFromLocalIncoming(final IExtendedFileStore sourceStore,
            final StoreItem sourceItem)
    {
        sourceStore.tryMoveLocal(sourceItem, bufferDirs.getCopyCompleteDir(), parameters
                .getPrefixForIncoming());
    }

    private void moveFromRemoteIncoming(final StoreItem sourceItem)
    {
        // 1. move from incoming: copy, delete, create copy-finished-marker
        moveFromRemoteToLocal(sourceItem);
        final File copiedFile = new File(bufferDirs.getCopyInProgressDir(), sourceItem.getName());
        if (copiedFile.exists() == false)
        {
            return;
        }

        // 2. Move to final directory, delete marker
        final File markerFile = MarkerFile.createCopyFinishedMarker(copiedFile);
        tryMoveFromInProgressToFinished(copiedFile, markerFile, bufferDirs.getCopyCompleteDir());
    }

    private File tryMoveFromInProgressToFinished(final File copiedFile,
            final File markerFileOrNull, final File copyCompleteDir)
    {
        final File finalFile = tryMoveLocal(copiedFile, copyCompleteDir, prefixForIncoming);
        if (finalFile != null)
        {
            if (markerFileOrNull != null)
            {
                if (markerFileOrNull.exists() == false)
                {
                    operationLog.error("Could not find expected copy-finished-mrker file "
                            + markerFileOrNull.getAbsolutePath());
                } else
                {
                    markerFileOrNull.delete(); // process even if marker file could not be deleted
                }
            }
            return finalFile;
        } else
        {
            return null;
        }
    }

    private void moveFromRemoteToLocal(final StoreItem sourceItem)
    {
        remotePathMover.handle(sourceItem);
    }

    private File tryMoveLocal(final File sourceFile, final File destinationDir,
            final String prefixTemplate)
    {
        return pathMover.tryMove(sourceFile, destinationDir, prefixTemplate);
    }

    //
    // Helper classes
    //

    private final class IncomingProcessorRecoveryTask extends TimerTask
    {

        private final void recoverIncomingInProgress(final File copyInProgressDir,
                final File copyCompleteDir)
        {
            final File[] files = FileUtilities.tryListFiles(copyInProgressDir, simpleOperationLog);
            if (files == null || files.length == 0)
            {
                return; // directory is empty, no recovery is needed
            }

            for (final File file : files)
            {
                if (MarkerFile.isDeletionInProgressMarker(file)
                        || MarkerFile.isFaultyPathsFile(file))
                {
                    continue;
                }
                recoverIncomingAfterShutdown(file, copyCompleteDir);
            }
        }

        private final void recoverIncomingAfterShutdown(final File unfinishedFile,
                final File copyCompleteDir)
        {
            if (MarkerFile.isCopyFinishedMarker(unfinishedFile))
            {
                final File markerFile = unfinishedFile;
                final File localCopy = MarkerFile.extractOriginalFromCopyFinishedMarker(markerFile);
                if (localCopy.exists())
                {
                    // copy and marker exist - do nothing, recovery will be done for copied resource
                } else
                {
                    // copy finished, resource moved, but marker was not deleted
                    markerFile.delete();
                }
            } else
            // handle local copy
            {
                final File localCopy = unfinishedFile;
                final File markerFile = MarkerFile.createCopyFinishedMarker(localCopy);
                if (markerFile.exists())
                {
                    // copy and marker exist - copy finished, but copied resource not moved
                    tryMoveFromInProgressToFinished(localCopy, markerFile, copyCompleteDir);
                } else
                // no marker
                {
                    final BooleanStatus exists =
                            incomingStore.exists(new StoreItem(localCopy.getName()));
                    if (exists.isSuccess())
                    {
                        // partial copy - nothing to do, will be copied again
                    } else
                    {
                        // move finished, but marker not created
                        tryMoveFromInProgressToFinished(localCopy, null, copyCompleteDir);
                    }
                }
            }
        }

        //
        // TimerTask
        //

        @Override
        public final void run()
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Recovery starts.");
            }
            recoverIncomingInProgress(bufferDirs.getCopyInProgressDir(), bufferDirs
                    .getCopyCompleteDir());
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Recovery is finished.");
            }
        }
    }
}
