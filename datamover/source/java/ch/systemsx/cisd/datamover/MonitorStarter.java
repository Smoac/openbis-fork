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
import java.io.FileFilter;
import java.util.Timer;
import java.util.TimerTask;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask;
import ch.systemsx.cisd.common.utilities.IntraFSPathMover;
import ch.systemsx.cisd.common.utilities.NamePrefixFileFilter;
import ch.systemsx.cisd.common.utilities.RegexFileFilter;
import ch.systemsx.cisd.common.utilities.DirectoryScanningTimerTask.IPathHandler;
import ch.systemsx.cisd.common.utilities.RegexFileFilter.PathType;

/**
 * A class that starts up the monitoring processes, based on the parameters provided.
 * 
 * @author Bernd Rinn
 */
public class MonitorStarter
{
    private final static String LOCAL_IN_PROGRESS_DIR = "in-progress";

    private final static String LOCAL_READY_TO_MOVE_DIR = "ready-to-move";

    private final static String LOCAL_TEMP_DIR = "tmp";

    private final Parameters parameters;

    private final IFileSysOperationsFactory operations;

    private final File inProgressDir; // here data are copied from incoming

    private final File readyToMoveDir;// from here data are moved to outgoing directory

    private final File tempDir;// auxiliary directory used if we need to make a copy of incoming data

    public MonitorStarter(Parameters parameters, IFileSysOperationsFactory operations)
    {
        this.parameters = parameters;
        this.operations = operations;
        File buffer = parameters.getBufferStore().getPath();
        this.inProgressDir = ensureDirectoryExists(buffer, LOCAL_IN_PROGRESS_DIR);
        this.readyToMoveDir = ensureDirectoryExists(buffer, LOCAL_READY_TO_MOVE_DIR);
        this.tempDir = ensureDirectoryExists(buffer, LOCAL_TEMP_DIR);
    }

    public void start()
    {
        startupIncomingMovingProcess(parameters.getIncomingStore());
        startupOutgoingMovingProcess(readyToMoveDir, parameters.getOutgoingStore());
    }

    private void startupIncomingMovingProcess(FileStore incomingStore)
    {
        final File manualInterventionDir = parameters.getManualInterventionDirectory();
        final RegexFileFilter cleansingFilter = new RegexFileFilter();
        if (parameters.getCleansingRegex() != null)
        {
            cleansingFilter.add(PathType.FILE, parameters.getCleansingRegex());
        }
        final RegexFileFilter manualInterventionFilter = new RegexFileFilter();
        if (parameters.getManualInterventionRegex() != null)
        {
            manualInterventionFilter.add(PathType.ALL, parameters.getManualInterventionRegex());
        }
        IPathHandler pathHandler =
                createIncomingMovingPathHandler(incomingStore.getHost(), manualInterventionDir,
                        manualInterventionFilter, cleansingFilter);

        final DirectoryScanningTimerTask movingTask =
                new DirectoryScanningTimerTask(incomingStore.getPath(), new QuietPeriodFileFilter(parameters,
                        operations), pathHandler);
        final Timer movingTimer = new Timer("Mover of Incomming Data");
        schedule(movingTimer, movingTask, 0, parameters.getCheckIntervalMillis(), parameters.getTreatIncomingAsRemote());
    }

    private IPathHandler createIncomingMovingPathHandler(String sourceHost, File manualInterventionDir,
            RegexFileFilter manualInterventionFilter, RegexFileFilter cleansingFilter)
    {
        IPathHandler moveFromIncoming = createPathMoverToLocal(sourceHost, inProgressDir);
        IPathHandler processMoved =
                createProcessMovedFile(readyToMoveDir, tempDir, parameters.tryGetExtraCopyStore(), operations);
        IPathHandler moveAndProcess = createMoveAndProcess(moveFromIncoming, inProgressDir, processMoved);
        IPathHandler manualInterventionMover = createPathMoverToLocal(sourceHost, manualInterventionDir);
        CleansingPathHandlerDecorator cleansingOrMover =
                new CleansingPathHandlerDecorator(cleansingFilter, moveAndProcess);
        return new GatePathHandlerDecorator(manualInterventionFilter, cleansingOrMover, manualInterventionMover);
    }

    private static IPathHandler createProcessMovedFile(File destDirectory, File tempDir,
            FileStore extraCopyStoreOrNull, IFileSysOperationsFactory operations)
    {
        FileFilter cleanMarkers = new NamePrefixFileFilter(Constants.IS_FINISHED_PREFIX, true);
        IPathHandler moveToDone = new IntraFSPathMover(destDirectory);
        IPathHandler processHandler;
        if (extraCopyStoreOrNull != null)
        {
            IPathImmutableCopier copier = operations.getImmutableCopier();
            IPathHandler extraCopyHandler = createExtraCopyHandler(tempDir, extraCopyStoreOrNull.getPath(), copier);
            processHandler = combineHandlers(extraCopyHandler, moveToDone);
        } else
        {
            processHandler = moveToDone;
        }
        return new CleansingPathHandlerDecorator(cleanMarkers, processHandler);
    }

    private static IPathHandler combineHandlers(final IPathHandler first, final IPathHandler second)
    {
        return new IPathHandler()
            {
                public boolean handle(File path)
                {
                    boolean firstOk = first.handle(path);
                    boolean secondOk = second.handle(path);
                    return firstOk && secondOk;
                }
            };
    }

    private static IPathHandler createExtraCopyHandler(final File tempDir, final File finalDir,
            final IPathImmutableCopier copier)
    {
        // making a copy can take some time, so we do that in the temporary directory. Than we move it from temporary
        // the final destination. In this way external process can start moving data from final destination as soon as
        // they appear there.
        final IPathHandler moveToFinal = new IntraFSPathMover(finalDir);

        return new IPathHandler()
            {
                public boolean handle(File path)
                {
                    File copiedFile = copier.tryCopy(path, tempDir);
                    if (copiedFile == null)
                        return false;
                    return moveToFinal.handle(copiedFile);
                }
            };
    }

    private static IPathHandler createMoveAndProcess(final IPathHandler moveFromIncoming, final File destinationDir,
            final IPathHandler processMovedFile)
    {
        return new IPathHandler()
            {
                public boolean handle(File path)
                {
                    boolean ok = moveFromIncoming.handle(path);
                    if (ok)
                    {
                        // create path in destination directory
                        File movedFile = new File(destinationDir, path.getName());
                        File markFile = new File(destinationDir, Constants.IS_FINISHED_PREFIX + path.getName());
                        assert movedFile.exists();
                        assert markFile.exists();
                        markFile.delete(); // process even if mark file could not be deleted
                        ok = processMovedFile.handle(movedFile);
                    }
                    return ok;
                }
            };
    }

    private IPathHandler createPathMoverToLocal(String sourceHost, final File localDestDir)
    {
        if (parameters.getTreatIncomingAsRemote())
        {
            return createRemotePathMover(sourceHost, localDestDir, /* local host */null);
        } else
        {
            return new IntraFSPathMover(localDestDir);
        }
    }

    private IPathHandler createRemotePathMover(String sourceHost, File destinationDirectory, String destinationHost)
    {
        IPathCopier copier = operations.getCopier(destinationDirectory);
        CopyActivityMonitor monitor = new CopyActivityMonitor(destinationDirectory, operations, copier, parameters);
        IPathRemover remover = operations.getRemover();
        return new RemotePathMover(destinationDirectory, destinationHost, monitor, remover, copier, sourceHost,
                parameters);
    }

    private void startupOutgoingMovingProcess(File srcDir, FileStore destDir)
    {
        final File outgoingDirectory = destDir.getPath();
        final String outgoingHost = destDir.getHost();
        final IPathHandler remoteMover = createRemotePathMover(null, outgoingDirectory, outgoingHost);
        final DirectoryScanningTimerTask remoteMovingTask =
                new DirectoryScanningTimerTask(srcDir, new NamePrefixFileFilter(Constants.IS_FINISHED_PREFIX, false),
                        remoteMover);
        final Timer remoteMovingTimer = new Timer("Remote Mover");

        // Implementation notes:
        // The startup of the remote moving task is delayed for half the time of the check interval. Thus the
        // incoming
        // moving task should have enough time to finish its job.
        schedule(remoteMovingTimer, remoteMovingTask, parameters.getCheckIntervalMillis() / 2, parameters
                .getCheckIntervalMillis(), true);
    }

    private void schedule(Timer timer, TimerTask task, long delay, long period, boolean isRemote)
    {
        // The remote moving task is scheduled at fixed rate. The rationale behind this is that if new items are
        // added to the source directory while the remote timer task has been running for a long time, busy moving data
        // to or from
        // remote, the task shoulnd't sit idle for the check time when there is actually work to do.
        if (isRemote)
        {
            timer.scheduleAtFixedRate(task, delay, period);
        } else
        {
            timer.schedule(task, delay, period);
        }
    }

    private static File ensureDirectoryExists(File dir, String newDirName)
    {
        File dataDir = new File(dir, newDirName);
        if (!dataDir.exists())
        {
            if (!dataDir.mkdir())
                throw new EnvironmentFailureException("Could not create local data directory " + dataDir);
        }
        return dataDir;
    }
}
