package ch.systemsx.cisd.datamover;

import java.io.File;
import java.io.FileFilter;
import java.util.Timer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.NamePrefixFileFilter;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IPathHandler;
import ch.systemsx.cisd.datamover.common.MarkerFile;
import ch.systemsx.cisd.datamover.filesystem.RemoteMonitoredMoverFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathMover;
import ch.systemsx.cisd.datamover.filesystem.intf.IReadPathOperations;
import ch.systemsx.cisd.datamover.utils.FileStore;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;
import ch.systemsx.cisd.datamover.utils.QuietPeriodFileFilter;

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

/**
 * @author Tomasz Pylak on Sep 7, 2007
 */
public class IncomingProcessor
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, IncomingProcessor.class);

    private static final ISimpleLogger errorLog = new Log4jSimpleLogger(Level.ERROR, operationLog);
    
    private final Parameters parameters;

    private final IFileSysOperationsFactory factory;

    private final IReadPathOperations incomingReadOperations;
    
    private final IPathMover pathMover;

    private final LocalBufferDirs bufferDirs;

    private final boolean isIncomingRemote;

    private final String prefixForIncoming;

    public static final ITerminable startupMovingProcess(Parameters parameters, IFileSysOperationsFactory factory,
            LocalBufferDirs bufferDirs, IPathHandler localProcessor)
    {
        IncomingProcessor processor = new IncomingProcessor(parameters, factory, bufferDirs);
        FileStore incomingStore = parameters.getIncomingStore();

        processor.recoverIncomingAfterShutdown(incomingStore, localProcessor);
        return processor.startupIncomingMovingProcess(incomingStore, localProcessor);
    }

    private IncomingProcessor(Parameters parameters, IFileSysOperationsFactory factory, LocalBufferDirs bufferDirs)
    {
        this.parameters = parameters;
        this.prefixForIncoming = parameters.getPrefixForIncoming();
        this.isIncomingRemote = parameters.getTreatIncomingAsRemote();
        this.incomingReadOperations = factory.getReadPathOperations();
        this.pathMover = factory.getMover();
        this.factory = factory;
        this.bufferDirs = bufferDirs;
    }

    private void recoverIncomingAfterShutdown(FileStore incomingStore, IPathHandler localProcessor)
    {
        new IncomingProcessorRecovery().recoverIncomingAfterShutdown(incomingStore, localProcessor);
    }

    private ITerminable startupIncomingMovingProcess(FileStore incomingStore, IPathHandler localProcessor)
    {
        IPathHandler pathHandler = createIncomingMovingPathHandler(incomingStore.getHost(), localProcessor);
        FileFilter filter = createQuietPeriodFilter();

        final DirectoryScanningTimerTask movingTask =
                new DirectoryScanningTimerTask(incomingStore.getPath(), filter, pathHandler);
        final Timer movingTimer = new Timer("Mover of Incoming Data");
        // The moving task is scheduled at fixed rate. It makes sense especially if the task is moving data from the
        // remote share. The rationale behind this is that if new items are
        // added to the source directory while the remote timer task has been running for a long time, busy moving data,
        // the task shoulnd't sit idle for the check time when there is actually work to do.
        movingTimer.scheduleAtFixedRate(movingTask, 0, parameters.getCheckIntervalMillis());
        return asTerminable(movingTimer);
    }

    private static ITerminable asTerminable(final Timer timer)
    {
        return new ITerminable()
            {
                public boolean terminate()
                {
                    timer.cancel();
                    return true;
                }
            };
    }

    private FileFilter createQuietPeriodFilter()
    {
        FileFilter quitePeriodFilter = new QuietPeriodFileFilter(parameters, incomingReadOperations);
        FileFilter filterDeletionMarkers = new NamePrefixFileFilter(Constants.DELETION_IN_PROGRESS_PREFIX, false);
        FileFilter filter = combineFilters(filterDeletionMarkers, quitePeriodFilter);
        return filter;
    }

    private static FileFilter combineFilters(final FileFilter filter1, final FileFilter filter2)
    {
        return new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return filter1.accept(pathname) && filter2.accept(pathname);
                }
            };
    }

    private IPathHandler createIncomingMovingPathHandler(final String sourceHostOrNull,
            final IPathHandler localProcessor)
    {
        return new IPathHandler()
            {
                public boolean handle(File sourceFile)
                {
                    if (isIncomingRemote)
                    {
                        return moveFromRemoteIncoming(sourceFile, sourceHostOrNull, localProcessor);
                    } else
                    {
                        return moveFromLocalIncoming(sourceFile, localProcessor);
                    }
                }
            };
    }

    private boolean moveFromLocalIncoming(File source, IPathHandler localProcessor)
    {
        final File finalFile = tryMoveLocal(source, bufferDirs.getCopyCompleteDir(), parameters.getPrefixForIncoming());
        if (finalFile == null)
        {
            return false;
        }
        return localProcessor.handle(finalFile);
    }

    private boolean moveFromRemoteIncoming(File source, String sourceHostOrNull, IPathHandler localProcessor)
    {
        // 1. move from incoming: copy, delete, create copy-finished-marker
        final File copyInProgressDir = bufferDirs.getCopyInProgressDir();
        final boolean ok = moveFromRemoteToLocal(source, sourceHostOrNull, copyInProgressDir);
        if (ok == false)
        {
            return false;
        }
        final File copiedFile = new File(copyInProgressDir, source.getName());
        assert copiedFile.exists() : copiedFile.getAbsolutePath();
        final File markerFile = MarkerFile.createCopyFinishedMarker(copiedFile);
        assert markerFile.exists() : markerFile.getAbsolutePath();

        // 2. Move to final directory, delete marker
        final File finalFile = tryMoveFromInProgressToFinished(copiedFile, markerFile, bufferDirs.getCopyCompleteDir());
        if (finalFile == null)
        {
            return false;
        }

        // 3. schedule local processing, always successful
        localProcessor.handle(finalFile);
        return true;
    }

    private File tryMoveFromInProgressToFinished(File copiedFile, File markerFileOrNull, File copyCompleteDir)
    {
        final File finalFile = tryMoveLocal(copiedFile, copyCompleteDir, prefixForIncoming);
        if (finalFile != null)
        {
            if (markerFileOrNull != null)
            {
                markerFileOrNull.delete(); // process even if marker file could not be deleted
            }
            return finalFile;
        } else
        {
            return null;
        }
    }

    private boolean moveFromRemoteToLocal(File source, String sourceHostOrNull, File localDestDir)
    {
        return createRemotePathMover(sourceHostOrNull, localDestDir, null).handle(source);
    }

    private IPathHandler createRemotePathMover(String sourceHost, File destinationDirectory, String destinationHost)
    {
        return RemoteMonitoredMoverFactory.create(sourceHost, destinationDirectory, destinationHost, factory, parameters);
    }

    private File tryMoveLocal(File sourceFile, File destinationDir, String prefixTemplate)
    {
        return pathMover.tryMove(sourceFile, destinationDir, prefixTemplate);
    }

    // ------------------- recovery ------------------------

    class IncomingProcessorRecovery
    {
        public void recoverIncomingAfterShutdown(FileStore incomingStore, IPathHandler localProcessor)
        {
            if (isIncomingRemote)
            {
                recoverIncomingInProgress(incomingStore, bufferDirs.getCopyInProgressDir(), bufferDirs
                        .getCopyCompleteDir());
            }
            recoverIncomingCopyComplete(bufferDirs.getCopyCompleteDir(), localProcessor);
        }

        private void recoverIncomingInProgress(FileStore incomingStore, File copyInProgressDir, File copyCompleteDir)
        {
            final File[] files = incomingReadOperations.tryListFiles(copyInProgressDir, errorLog);
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
                recoverIncomingAfterShutdown(file, incomingStore, copyCompleteDir);
            }
        }

        private void recoverIncomingAfterShutdown(File unfinishedFile, FileStore incomingStore, File copyCompleteDir)
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
                    File incomingDir = incomingStore.getPath();
                    File originalInIncoming = new File(incomingDir, localCopy.getName());
                    if (incomingReadOperations.exists(originalInIncoming))
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

        // schedule processing of all resources which were previously copied
        private void recoverIncomingCopyComplete(File copyCompleteDir, IPathHandler localProcessor)
        {
            final File[] files = incomingReadOperations.tryListFiles(copyCompleteDir, errorLog);
            if (files == null || files.length == 0)
            {
                return; // directory is empty, no recovery is needed
            }

            for (File file : files)
            {
                localProcessor.handle(file);
            }
        }
    }
}
