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
import java.util.TimerTask;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.IStoreHandler;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IScannedStore;
import ch.systemsx.cisd.datamover.common.MarkerFile;
import ch.systemsx.cisd.datamover.filesystem.FileStoreFactory;
import ch.systemsx.cisd.datamover.filesystem.RemoteMonitoredMoverFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.FileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IExtendedFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathMover;
import ch.systemsx.cisd.datamover.filesystem.intf.IRecoverableTimerTaskFactory;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;
import ch.systemsx.cisd.datamover.utils.QuietPeriodFileFilter;

/**
 * @author Tomasz Pylak on Sep 7, 2007
 */
public class IncomingProcessor implements IRecoverableTimerTaskFactory
{
    /**
     * The number of consecutive errors of listing the incoming directories that are not reported in the log to avoid
     * mailbox flooding.
     */
    private final static int NUMBER_OF_ERRORS_IN_LISTING_IGNORED = 2;

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, IncomingProcessor.class);

    private static final ISimpleLogger simpleOperationLog = new Log4jSimpleLogger(operationLog);

    private final Parameters parameters;

    private final IFileSysOperationsFactory factory;

    private final IPathMover pathMover;

    private final LocalBufferDirs bufferDirs;

    private final IFileStore incomingStore;

    private final String prefixForIncoming;

    private final QuietPeriodFileFilter quietPeriodFileFilter;

    public static final DataMoverProcess createMovingProcess(Parameters parameters,
            IFileSysOperationsFactory factory, LocalBufferDirs bufferDirs)
    {
        final IncomingProcessor processor = new IncomingProcessor(parameters, factory, bufferDirs);

        return processor.create();
    }

    private IncomingProcessor(Parameters parameters, IFileSysOperationsFactory factory,
            LocalBufferDirs bufferDirs)
    {
        this.parameters = parameters;
        this.prefixForIncoming = parameters.getPrefixForIncoming();
        this.incomingStore = parameters.getIncomingStore(factory);
        this.pathMover = factory.getMover();
        this.factory = factory;
        this.bufferDirs = bufferDirs;
        this.quietPeriodFileFilter = new QuietPeriodFileFilter(incomingStore, parameters);
    }

    public TimerTask createRecoverableTimerTask()
    {
        return new IncomingProcessorRecoveryTask();
    }

    private DataMoverProcess create()
    {
        final IStoreHandler pathHandler = createIncomingMovingPathHandler();

        final DirectoryScanningTimerTask movingTask =
                new DirectoryScanningTimerTask(createIncomingStoreScanner(), bufferDirs
                        .getCopyInProgressDir(), pathHandler, NUMBER_OF_ERRORS_IN_LISTING_IGNORED);
        return new DataMoverProcess(movingTask, "Mover of Incoming Data", this);
    }

    private IScannedStore createIncomingStoreScanner()
    {
        return new IScannedStore()
            {
                public boolean exists(StoreItem item)
                {
                    return incomingStore.exists(item);
                }

                public String getLocationDescription(StoreItem item)
                {
                    return incomingStore.getLocationDescription(item);
                }

                public StoreItem[] tryListSortedReadyToProcess(ISimpleLogger loggerOrNull)
                {
                    // Older items will be handled before newer items.
                    // This becomes important when doing online quality control of measurements.
                    StoreItem[] items = incomingStore.tryListSortByLastModified(loggerOrNull);
                    if (items == null)
                    {
                        return null;
                    }
                    return filterReadyToProcess(items);
                }
            };
    }

    private StoreItem[] filterReadyToProcess(StoreItem[] items)
    {
        Vector<StoreItem> result = new Vector<StoreItem>();
        for (StoreItem item : items)
        {
            if (isReadyToProcess(item))
            {
                result.add(item);
            }
        }
        return result.toArray(StoreItem.EMPTY_ARRAY);
    }

    private boolean isReadyToProcess(StoreItem item)
    {
        if (item.getName().startsWith(Constants.DELETION_IN_PROGRESS_PREFIX))
        {
            return false;
        }
        return quietPeriodFileFilter.accept(item);
    }

    private IStoreHandler createIncomingMovingPathHandler()
    {
        return new IStoreHandler()
            {
                public void handle(StoreItem sourceItem)
                {
                    IExtendedFileStore extendedFileStore = incomingStore.tryAsExtended();
                    if (extendedFileStore == null)
                    {
                        moveFromRemoteIncoming(sourceItem);
                    } else
                    {
                        moveFromLocalIncoming(extendedFileStore, sourceItem);
                    }
                }
            };
    }

    private void moveFromLocalIncoming(IExtendedFileStore sourceStore, StoreItem sourceItem)
    {
        sourceStore.tryMoveLocal(sourceItem, bufferDirs.getCopyCompleteDir(), parameters
                .getPrefixForIncoming());
    }

    private void moveFromRemoteIncoming(StoreItem sourceItem)
    {
        // 1. move from incoming: copy, delete, create copy-finished-marker
        final File copyInProgressDir = bufferDirs.getCopyInProgressDir();
        moveFromRemoteToLocal(sourceItem, incomingStore, copyInProgressDir);
        final File copiedFile = new File(copyInProgressDir, sourceItem.getName());
        if (copiedFile.exists() == false)
        {
            return;
        }

        // 2. Move to final directory, delete marker
        final File markerFile = MarkerFile.createCopyFinishedMarker(copiedFile);
        tryMoveFromInProgressToFinished(copiedFile, markerFile, bufferDirs.getCopyCompleteDir());
    }

    private File tryMoveFromInProgressToFinished(File copiedFile, File markerFileOrNull,
            File copyCompleteDir)
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

    private void moveFromRemoteToLocal(StoreItem sourceItem, IFileStore sourceStore,
            File localDestDir)
    {
        createRemotePathMover(sourceStore,
                FileStoreFactory.createLocal(localDestDir, "local", factory)).handle(sourceItem);
    }

    private IStoreHandler createRemotePathMover(IFileStore sourceDirectory,
            FileStore destinationDirectory)
    {
        return RemoteMonitoredMoverFactory
                .create(sourceDirectory, destinationDirectory, parameters);
    }

    private File tryMoveLocal(File sourceFile, File destinationDir, String prefixTemplate)
    {
        return pathMover.tryMove(sourceFile, destinationDir, prefixTemplate);
    }

    // ------------------- recovery ------------------------

    class IncomingProcessorRecoveryTask extends TimerTask
    {
        @Override
        public void run()
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Recovery starts.");
            }
            if (incomingStore.isRemote())
            {
                recoverIncomingInProgress(bufferDirs.getCopyInProgressDir(), bufferDirs
                        .getCopyCompleteDir());
            }
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Recovery is finished.");
            }
        }

        private void recoverIncomingInProgress(File copyInProgressDir, File copyCompleteDir)
        {
            final File[] files = FileUtilities.tryListFiles(copyInProgressDir, simpleOperationLog);
            if (files == null || files.length == 0)
            {
                return; // directory is empty, no recovery is needed
            }

            for (File file : files)
            {
                if (MarkerFile.isDeletionInProgressMarker(file))
                {
                    continue;
                }
                recoverIncomingAfterShutdown(file, copyCompleteDir);
            }
        }

        private void recoverIncomingAfterShutdown(File unfinishedFile, File copyCompleteDir)
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
                    if (incomingStore.exists(new StoreItem(localCopy.getName())))
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
    }
}
